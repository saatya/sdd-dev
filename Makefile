IMAGE   := hello-k8s-sidecar:1.0.0
CLUSTER := hello
NS      := hello

.PHONY: test build kind-up load deploy verify logs clean

test:            ## Run spec tests (AC-1..AC-8)
	mvn -B test

build:           ## Build the container image
	docker build -t $(IMAGE) .

kind-up:         ## Create local cluster with ./node-logs mounted as the node's disk
	mkdir -p node-logs
	kind create cluster --config kind-config.yaml

load: build      ## Push the local image into the kind node
	kind load docker-image $(IMAGE) --name $(CLUSTER)

deploy:          ## Apply manifests and wait for rollout
	kubectl apply -k k8s/
	kubectl -n $(NS) rollout status deploy/hello-app --timeout=120s

verify:          ## AC-9..AC-12: call the API, then confirm lines landed on node disk
	@kubectl -n $(NS) port-forward svc/hello-app 8080:80 >/dev/null 2>&1 & echo $$! > .pf.pid; sleep 3
	@curl -s "localhost:8080/api/v1/hello?name=Satya"; echo
	@curl -s -o /dev/null -w "invalid name -> HTTP %{http_code}\n" "localhost:8080/api/v1/hello?name=%3Cx%3E"
	@kill `cat .pf.pid` && rm -f .pf.pid
	@sleep 1
	@echo "--- files on node disk (./node-logs) ---"; ls -l node-logs/
	@grep -h "greeting" node-logs/*.log | tail -5

logs:            ## Tail the sidecar's stdout
	kubectl -n $(NS) logs -l app=hello-app -c log-shipper -f --prefix

clean:
	kind delete cluster --name $(CLUSTER)
	rm -rf node-logs target
