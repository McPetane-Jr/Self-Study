# Week 3 Prometheus:


## Clean up
Phase summary: what this phase achieves
1.  Cleaned up after PR #14, using git switch dev, git pull, and git branch -d.
2. Checked what changed with git log, so we don't duplicate teammates' work.
3. Reconnected the VPN and ran a quick cluster health check, to give us a baseline.
4. Looked up chart versions so we can pin them.
5. Created feature/week3-prometheus, ready for the first Argo Application file.

Notes recap: previous step
Summarised progress and a Claude Doc: Week 3, CI done, Part A next.
---
## Week 3:
Your Week 3 to-do list

### Groundwork (done)

Get cluster access: VPN and SSH. [done]
Explore the cluster and Argo CD to get a baseline. [done]
Read the Week 3 README, CONTRIBUTING and docs. [done]
CI: a GitHub Actions YAML-lint check, PR #14 merged. [done]
CD: Argo CD already deploys whatever merges to dev. Kieren built this in Week 2, and we've now learned to use it. [done]
Part A: create the branch and download the Prometheus chart's defaults. [done]
Inspect the settings and find the traps (no default storage class, alertmanager disk). [done]
---
### Part A: Prometheus and Grafana
8. Write prometheus.yaml. [in progress, Steps 11 and 12]
9. Commit, push, open a PR, get a review, and merge. [next]
10. Verify Argo deploys it (Synced/Healthy, pods Running, PVC Bound). []
11. Verify Prometheus data, not just pods, using the up query. []
12. Add Grafana as an Argo Application. []
13. Git-managed dashboards: node CPU/memory/disk, Kubernetes health, and platform health. []
---

### Part B: Wazuh and Suricata (with the teammate who owns Ansible)
14. Wazuh agents and the edge manager through Ansible. []
15. Suricata in IDS mode on edge-01. []
16. Wazuh indexer and dashboard through Argo, with SealedSecrets. []
17. One fresh, benign detection drill, with timestamps. []
---

### Part C: Student Platform (shared with the frontend role)
18. CI builds the image into GHCR with an immutable sha-<commit> tag. []
19. PostgreSQL and the platform deployed through Argo. []
20. Prove persistence, login, and TLS in a real browser. []
---

### Finish
21. More CI checks: Kubernetes manifest validation, secret scan, Terraform/Ansible checks. [optional extra]
22. Ask Kieren about branch protection on dev. [open question]
23. Part D: a provenance manifest recorded with no secrets. []
24. Week 3 acceptance gate and the final PR into dev. []
---

## 8. Write prometheus.yaml.
Phase summary: what we did
- Checked the chart's real settings and found the traps: no default storage class, an alertmanager disk that would never start, and no resource requests.
- Confirmed Prometheus runs as a Deployment, which matches the README.
- Wrote gitops/applications/prometheus.yaml with a pinned chart, cinder-ssd storage, retention, and modest resources, with alertmanager and pushgateway off.
- Next: test with yamllint, then commit, push and open the PR.

Notes recap: previous step
- Downloaded the chart's values.yaml (1264 lines) and found its main sections.
- Spotted that the default disk has no storage class and no size cap.
- Planned the Application file around a monitoring namespace and the cinder-ssd class.
---
Recall that a pod is one running app and a PVC is a pod's request for a disk.
