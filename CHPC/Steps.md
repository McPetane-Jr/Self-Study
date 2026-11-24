<!-- This file is for organising the logs of what I'm doing -->

# 13: Git-managed dashboards:

**Context**:
We want Grafana to automatically load our three required dashboards (Node CPU/Memory/Disk, Kubernetes Health, and Platform Health) straight from Git instead of clicking around in the Grafana web UI. To make that happen, we are going to create a gitops/resources/monitoring/ folder containing three Kubernetes ConfigMap files labeled grafana_dashboard: "1", plus one Argo CD Application file (gitops/applications/monitoring-dashboards.yaml) that tells Argo CD to deploy those files into the monitoring namespace so Grafana's sidecar picks them up automatically.   

**Files we are creating now:**
1. gitops/applications/monitoring-dashboards.yaml (Argo CD Application watching gitops/resources/monitoring)
2. gitops/resources/monitoring/dashboard-node-health.yaml (CPU, memory, disk usage per node using kubernetes-nodes / kubernetes-nodes-cadvisor / node_* metrics)   
3. gitops/resources/monitoring/dashboard-k8s-health.yaml (Pod states, restarts, deployments, API server health using kubernetes-service-endpoints / kubernetes-api-servers / kube_* metrics)   4. gitops/resources/monitoring/dashboard-platform-health.yaml (Prometheus scrape targets up, scrape duration, and monitoring stack health using prometheus / kubernetes-pods)   

## Step 1: Create the Argo CD Application and the three dashboard ConfigMaps (on WSL):
 In ~/collab-agent-2026/

```
mkdir -p gitops/resources/monitoring

cat << 'EOF' > gitops/applications/monitoring-dashboards.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: monitoring-dashboards
  namespace: argocd
  annotations:
    argocd.argoproj.io/sync-wave: "1"
spec:
  project: default
  source:
    repoURL: https://github.com/chpc-tech-eval/collab-agent-scc26.git
    targetRevision: dev
    path: gitops/resources/monitoring
  destination:
    server: https://kubernetes.default.svc
    namespace: monitoring
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
EOF

cat << 'EOF' > gitops/resources/monitoring/dashboard-node-health.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboard-node-health
  namespace: monitoring
  labels:
    grafana_dashboard: "1"
data:
  node-health.json: |
    {
      "uid": "scc26-node-health",
      "title": "Node CPU, Memory & Disk",
      "tags": ["scc26", "nodes"],
      "timezone": "browser",
      "schemaVersion": 39,
      "version": 1,
      "refresh": "30s",
      "panels": [
        {
          "id": 1,
          "type": "timeseries",
          "title": "Node CPU Utilization (%)",
          "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "100 - (avg by (instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
              "legendFormat": "{{instance}}"
            }
          ]
        },
        {
          "id": 2,
          "type": "timeseries",
          "title": "Node Memory Utilization (%)",
          "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100",
              "legendFormat": "{{instance}}"
            }
          ]
        },
        {
          "id": 3,
          "type": "timeseries",
          "title": "Root Filesystem Disk Usage (%)",
          "gridPos": {"h": 8, "w": 24, "x": 0, "y": 8},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "(1 - (node_filesystem_avail_bytes{mountpoint=\"/\",fstype!=\"rootfs\"} / node_filesystem_size_bytes{mountpoint=\"/\",fstype!=\"rootfs\"})) * 100",
              "legendFormat": "{{instance}}"
            }
          ]
        }
      ]
    }
EOF

cat << 'EOF' > gitops/resources/monitoring/dashboard-k8s-health.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboard-k8s-health
  namespace: monitoring
  labels:
    grafana_dashboard: "1"
data:
  k8s-health.json: |
    {
      "uid": "scc26-k8s-health",
      "title": "Kubernetes Cluster Health",
      "tags": ["scc26", "kubernetes"],
      "timezone": "browser",
      "schemaVersion": 39,
      "version": 1,
      "refresh": "30s",
      "panels": [
        {
          "id": 1,
          "type": "stat",
          "title": "Pods by Phase",
          "gridPos": {"h": 6, "w": 12, "x": 0, "y": 0},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "sum by (phase) (kube_pod_status_phase == 1)",
              "legendFormat": "{{phase}}"
            }
          ]
        },
        {
          "id": 2,
          "type": "stat",
          "title": "Ready Nodes",
          "gridPos": {"h": 6, "w": 12, "x": 12, "y": 0},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "sum(kube_node_status_condition{condition=\"Ready\",status=\"true\"})",
              "legendFormat": "Ready Nodes"
            }
          ]
        },
        {
          "id": 3,
          "type": "timeseries",
          "title": "Container Restarts (15m increase)",
          "gridPos": {"h": 8, "w": 24, "x": 0, "y": 6},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "sum by (namespace, pod) (increase(kube_pod_container_status_restarts_total[15m])) > 0",
              "legendFormat": "{{namespace}}/{{pod}}"
            }
          ]
        }
      ]
    }
EOF

cat << 'EOF' > gitops/resources/monitoring/dashboard-platform-health.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboard-platform-health
  namespace: monitoring
  labels:
    grafana_dashboard: "1"
data:
  platform-health.json: |
    {
      "uid": "scc26-platform-health",
      "title": "Platform & Observability Health",
      "tags": ["scc26", "platform"],
      "timezone": "browser",
      "schemaVersion": 39,
      "version": 1,
      "refresh": "30s",
      "panels": [
        {
          "id": 1,
          "type": "stat",
          "title": "Scrape Targets Up by Job",
          "gridPos": {"h": 8, "w": 12, "x": 0, "y": 0},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "sum by (job) (up)",
              "legendFormat": "{{job}}"
            }
          ]
        },
        {
          "id": 2,
          "type": "timeseries",
          "title": "Prometheus Scrape Duration (s)",
          "gridPos": {"h": 8, "w": 12, "x": 12, "y": 0},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "avg by (job) (scrape_duration_seconds)",
              "legendFormat": "{{job}}"
            }
          ]
        },
        {
          "id": 3,
          "type": "timeseries",
          "title": "Container Memory Working Set (MiB)",
          "gridPos": {"h": 8, "w": 24, "x": 0, "y": 8},
          "datasource": {"type": "prometheus", "uid": "Prometheus"},
          "targets": [
            {
              "expr": "sum by (namespace) (container_memory_working_set_bytes{container!=\"\"}) / 1024 / 1024",
              "legendFormat": "{{namespace}}"
            }
          ]
        }
      ]
    }
EOF
```

## Step 2: Lint, commit, and push feature/week3-dashboards (on WSL):

**Right now we are checking that all four new YAML files pass the repo's yamllint check, committing them to feature/week3-dashboards, and pushing the branch to GitHub so you can open the PR into dev.**

```
yamllint -c .yamllint.yaml gitops/; echo "exit code: $?"
git add gitops/applications/monitoring-dashboards.yaml gitops/resources/monitoring/
git status
git commit -m "feat(observability): add Git-managed Grafana dashboards via Argo CD"
git push -u origin feature/week3-dashboards
```

yamllint passed (exit code: 0, only line-length warnings on the PromQL strings inside JSON, which .yamllint.yaml configures as warnings rather than errors) and feature/week3-dashboards is pushed to GitHub

---

We created the Argo CD Application (monitoring-dashboards.yaml) and three Grafana dashboard ConfigMaps (node-health, k8s-health, and platform-health) in gitops/resources/monitoring/, passed yamllint (exit code: 0), and pushed feature/week3-dashboards to GitHub—so once this PR merges, Argo CD will deploy those ConfigMaps and Grafana's sidecar will automatically load all three dashboards with zero manual clicking in the UI.

## Push & Clean
1. Tested and pushed:
```
yamllint -c .yamllint.yaml gitops/; echo "exit code: $?"
git add gitops/applications/monitoring-dashboards.yaml gitops/resources/monitoring/
git status
git commit -m "feat(observability): add Git-managed Grafana dashboards via Argo CD"
git push -u origin feature/week3-dashboards
```
2. Requested a PR, then it got merged
3. **Tidy your laptop (~/collab-agent-scc26 on WSL)**
```
cd ~/collab-agent-scc26
git switch dev
git pull --ff-only
git branch -d feature/week3-dashboards
```

## Verify Argo CD, ConfigMaps, and Grafana loaded all 3 dashboards (on k8s-cp-01)

checking on k8s-cp-01 that Argo CD synced all three dashboard ConfigMaps into monitoring and querying Grafana's API to prove Grafana automatically discovered and loaded all three dashboards (scc26-node-health, scc26-k8s-health, and scc26-platform-health).

```
export KUBECONFIG=/home/rocky/.kube/config
kubectl -n argocd get applications
kubectl -n monitoring get configmap -l grafana_dashboard=1

GRAFANA_PASS=$(kubectl -n monitoring get secret grafana-admin -o jsonpath='{.data.admin-password}' | base64 -d)
kubectl -n monitoring port-forward svc/grafana 13000:80 >/dev/null 2>&1 &
PF_PID=$!
sleep 2
curl -s -u "admin:${GRAFANA_PASS}" 'http://127.0.0.1:13000/api/search?type=dash-db' && echo ""
kill $PF_PID 2>/dev/null || true
unset GRAFANA_PASS
```

**Output**:
```
[rocky@k8s-cp-01 ~]$ export KUBECONFIG=/home/rocky/.kube/config
kubectl -n argocd get applications
kubectl -n monitoring get configmap -l grafana_dashboard=1

GRAFANA_PASS=$(kubectl -n monitoring get secret grafana-admin -o jsonpath='{.data.admin-password}' | base64 -d)
kubectl -n monitoring port-forward svc/grafana 13000:80 >/dev/null 2>&1 &
PF_PID=$!
sleep 2
curl -s -u "admin:${GRAFANA_PASS}" 'http://127.0.0.1:13000/api/search?type=dash-db' && echo ""
kill $PF_PID 2>/dev/null || true
unset GRAFANA_PASS
NAME                    SYNC STATUS   HEALTH STATUS
cert-manager            Synced        Healthy
cert-manager-config     Synced        Healthy
cinder-csi              Synced        Healthy
grafana                 Synced        Healthy
ingress-smoke-test      Synced        Progressing
monitoring-dashboards   Synced        Healthy
prometheus              Synced        Healthy
root                    Synced        Healthy
traefik                 Synced        Healthy
NAME                                DATA   AGE
grafana-dashboard-k8s-health        1      56m
grafana-dashboard-node-health       1      56m
grafana-dashboard-platform-health   1      56m
[1] 519100
[{"id":4079880192385024,"uid":"scc26-k8s-health","orgId":1,"title":"Kubernetes Cluster Health","uri":"db/kubernetes-cluster-health","url":"/d/scc26-k8s-health/kubernetes-cluster-health","slug":"","type":"dash-db","tags":["kubernetes","scc26"],"isStarred":false,"sortMeta":0,"isDeleted":false},{"id":4079879592599552,"uid":"scc26-node-health","orgId":1,"title":"Node CPU, Memory \u0026 Disk","uri":"db/node-cpu-memory-and-disk","url":"/d/scc26-node-health/node-cpu-memory-and-disk","slug":"","type":"dash-db","tags":["nodes","scc26"],"isStarred":false,"sortMeta":0,"isDeleted":false},{"id":4079880746033152,"uid":"scc26-platform-health","orgId":1,"title":"Platform \u0026 Observability Health","uri":"db/platform-and-observability-health","url":"/d/scc26-platform-health/platform-and-observability-health","slug":"","type":"dash-db","tags":["platform","scc26"],"isStarred":false,"sortMeta":0,"isDeleted":false}]
[rocky@k8s-cp-01 ~]$

```
---

# Part B — Wazuh and Suricata

Our entire Prometheus + Grafana + dashboard stack (Steps 1–4 of Week 3) is complete, we are moving to Part B: **Wazuh and Suricata** (Step 5 of Week 3). 

First, we are checking what Ansible playbooks and inventory files already exist in infrastructure/ansible/ so we know whether wazuh-agents.yml, edge.yml, or security.yml is already built and ready to run.
