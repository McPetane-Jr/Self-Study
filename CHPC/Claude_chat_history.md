Good news: the download worked, and the file already tells us two things we needed to know.

Where you are in the repo
Folder: ~/collab-agent-scc26 in WSL.
Branch: feature/week3-prometheus, created from an up-to-date dev.
Changes so far: none. The downloaded file is in /tmp, outside the repo, so git status should still say “working tree clean”.
Where the new file will go: gitops/applications/prometheus.yaml, next to traefik.yaml.
What your output means
1264 lines means the download worked. A wrong address would have given a 1-line “404: Not Found”. The first line mentions a values.schema.json, which is a rulebook the chart checks your settings against. This may mean a mistyped setting fails loudly instead of being silently ignored, which is good, though I haven’t confirmed how strict this chart is.

The main sections (line numbers are where each starts):

Section What it is
server (line 126) Prometheus itself, the part that collects and stores the numbers
configmapReload (line 30) A small helper that reloads Prometheus when its settings change
alertmanager (line 1198) Sends alerts. We don’t need it yet.
kube-state-metrics (line 1241) Reports the health of Kubernetes objects (pods, nodes)
prometheus-node-exporter (line 1249) Reports each machine’s CPU, memory and disk
prometheus-pushgateway (line 1257) A drop-box for short-lived jobs. Also not needed yet.
Recall that the last three small sections are extra packages the chart installs alongside Prometheus. They’re what you’ll need for the README’s “node CPU/memory/filesystem” and “Kubernetes node/pod health” dashboard.

persistentVolume: enabled: true (line 534) means Prometheus will ask for a disk by default. Recall that a PVC (PersistentVolumeClaim) is a pod’s request for a disk, and persistent means the data survives restarts.

Important catch: I rechecked your earlier kubectl get storageclass output. None of the three classes is marked (default). If we leave the setting blank, Prometheus’s disk request has no storage class to use, and it will sit in Pending forever. So we must name cinder-ssd explicitly. This is the kind of problem that’s easy to miss, because Argo might show the app as created while the pod never starts.

retention: "15d" and retentionSize: "" mean:

Retention is how long Prometheus keeps old data before deleting it. Here that’s 15 days, which fits our six-week lab.
retentionSize is an optional cap on disk use. Empty means no cap, so a full disk could crash Prometheus. We’ll set a cap a bit below the disk size.
Step 9: Read the last few settings we need
Still read-only. This shows the disk-size and storage-class settings, what alertmanager’s switch looks like, and the resources block:

bash
sed -n '543,575p' /tmp/prom-values.yaml
sed -n '1198,1264p' /tmp/prom-values.yaml
grep -n -A 4 "^ statefulSet:" /tmp/prom-values.yaml
grep -n -B 2 -A 8 "^ resources:" /tmp/prom-values.yaml
git status
sed -n '543,575p' prints only lines 543 to 575 (-n means “print only what I ask for”, p means “print”).
grep -A 4 means 4 lines after a match, and -B 2 means 2 lines before.
statefulSet matters because the README’s command uses deploy/prometheus-server. A Deployment runs as a plain pod group, and a StatefulSet is for apps that need stable identities. This check confirms which one we get by default.
resources shows the default CPU and memory reservations for Prometheus.
git status should confirm you’re on feature/week3-prometheus with nothing changed.
Paste the output (it’s a lot of lines, and that’s fine). Then we’ll write prometheus.yaml together.

Phase summary: what we did
Downloaded and read the chart’s real default settings for version 30.0.0, instead of guessing.
Found the main sections: Prometheus, kube-state-metrics, node-exporter, alertmanager, pushgateway, and the config reloader.
Spotted two risks before they became bugs: no default storage class exists (so we must name cinder-ssd), and there’s no size cap on stored data.
Next: read the last settings and write gitops/applications/prometheus.yaml.
Notes recap: previous step
Interpreted the Phase 3a checks: the repo is tidy, nothing new has been merged, the VPN is alive, and all pods are Running.
Chose the chart versions to pin: Prometheus 30.0.0 and Grafana 10.5.15, with a caution that 30.0.0 is a major version.
Planned to verify the settings against the real values.yaml before writing the Application file.

pride@DESKTOP-211UNDE:~/collab-agent-scc26$ sed -n '543,575p' /tmp/prom-values.yaml sed -n '1198,1264p' /tmp/prom-values.yaml grep -n -A 4 "^ statefulSet:" /tmp/prom-values.yaml grep -n -B 2 -A 8 "^ resources:" /tmp/prom-values.yaml git status ## Ref: https://kubernetes.io/docs/concepts/s

PASTED
Alfter explaining the next phase can you also give me a summary of our to-do for week 3
e.g.

CI - created yaml check [done]
CD - .... [done]
pushed - uploaded to github [done]
Start with part A of the repo [done]
Download the Prometheus Helm chart's default configuration. [done]
Inspect it [done]
Deploy through gitop []
Verify Prometheus data, not just pods []
etc till the last step
Your output answered three questions, and one of them is a problem we avoid by naming the storage class.

Where you are in the repo
Folder: ~/collab-agent-scc26.
Branch: feature/week3-prometheus.
Changes: none yet (git status says clean). The next file is gitops/applications/prometheus.yaml.
What your output means

1. Disk settings (persistentVolume)

accessModes: ReadWriteOnce means one machine at a time can use the disk read-write. Recall that a PVC is a pod’s request for a disk. That’s fine for one Prometheus pod, and Cinder supports it.
size: 8Gi is the default disk size. We’ll use 10Gi for headroom, as the README asks.
The storage class is undefined by default, so “no storageClassName spec is set” and it relies on a default class. Your cluster has no default class, so the disk request would sit in Pending forever. We’ll set cinder-ssd explicitly. 2. statefulSet: enabled: false

This means Prometheus runs as a Deployment by default. Recall that a Deployment is Kubernetes’ standard way to run a pod and keep it alive.
That matches the README’s kubectl exec deploy/prometheus-server. My earlier guess that the README uses this chart looks right. 3. resources: {}

It’s empty, so nothing is reserved. Recall that a request is the amount of CPU or memory reserved for a pod, and a limit is the maximum it may use. We’ll set modest numbers so the scheduler can plan. 4. Alertmanager and pushgateway

Both are enabled by default, and alertmanager asks for its own 2Gi disk with no storage class. That’s a second Pending disk. We don’t need alerts yet, so we’ll switch it off, along with the pushgateway.
kube-state-metrics and node-exporter stay on. They feed the README’s node and Kubernetes health dashboards.
Step 10: Verify two setting names (read-only)
bash
grep -n "storageClass" /tmp/prom-values.yaml
grep -n -A 3 "^ strategy:" /tmp/prom-values.yaml
The first shows the exact key name for the storage class. My plan assumes server.persistentVolume.storageClass, and this confirms it.
The second shows the Deployment’s update strategy, which is how it replaces the old pod during an upgrade. A disk that only one pod can use must be released first, so I want to see Recreate (stop the old pod, then start the new one) and not a rolling update.
If either output surprises you, paste it before the next step.
Step 11: Create the Application file
Use nano and paste the block (don’t type it):

bash
nano gitops/applications/prometheus.yaml
yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
name: prometheus
namespace: argocd
spec:
project: default
source:
repoURL: https://prometheus-community.github.io/helm-charts
chart: prometheus
targetRevision: 30.0.0
helm:
values: |
server:
retention: 15d
retentionSize: 8GB
persistentVolume:
enabled: true
storageClass: cinder-ssd
size: 10Gi
resources:
requests:
cpu: 250m
memory: 512Mi
limits:
memory: 1Gi
alertmanager:
enabled: false
prometheus-pushgateway:
enabled: false
destination:
server: https://kubernetes.default.svc
namespace: monitoring
syncPolicy:
automated:
prune: true
selfHeal: true
syncOptions: - CreateNamespace=true
Save with Ctrl+O, Enter, Ctrl+X. Every part mirrors traefik.yaml:

chart and targetRevision pin the exact package version.
values: | holds our overrides. Recall that these replace the chart’s defaults.
retentionSize: 8GB stops Prometheus filling the 10Gi disk.
The memory limit is a safety cap, and I left out a CPU limit on purpose.
CreateNamespace=true creates the monitoring namespace automatically.
The numbers (250m, 512Mi, 1Gi) are a reasonable starting guess for a small lab, not a measured result. We’ll check real usage later and adjust.

Step 12: Test it before committing
bash
head -n 3 gitops/applications/prometheus.yaml
tail -n 3 gitops/applications/prometheus.yaml
yamllint -c .yamllint.yaml gitops/; echo "exit code: $?"
git status
head and tail check that nothing was pasted into the wrong place.
yamllint is the same check your CI robot will run, so you find problems before pushing.
You want exit code: 0, and git status should show only prometheus.yaml as new.
Your Week 3 to-do list
Groundwork (done)

Get cluster access: VPN and SSH. [done]
Explore the cluster and Argo CD to get a baseline. [done]
Read the Week 3 README, CONTRIBUTING and docs. [done]
CI: a GitHub Actions YAML-lint check, PR #14 merged. [done]
CD: Argo CD already deploys whatever merges to dev. Kieren built this in Week 2, and we’ve now learned to use it. [done]
Part A: create the branch and download the Prometheus chart’s defaults. [done]
Inspect the settings and find the traps (no default storage class, alertmanager disk). [done]
Part A: Prometheus and Grafana 8. Write prometheus.yaml. [in progress, Steps 11 and 12] 9. Commit, push, open a PR, get a review, and merge. [next] 10. Verify Argo deploys it (Synced/Healthy, pods Running, PVC Bound). [] 11. Verify Prometheus data, not just pods, using the up query. [] 12. Add Grafana as an Argo Application. [] 13. Git-managed dashboards: node CPU/memory/disk, Kubernetes health, and platform health. []

Part B: Wazuh and Suricata (with the teammate who owns Ansible) 14. Wazuh agents and the edge manager through Ansible. [] 15. Suricata in IDS mode on edge-01. [] 16. Wazuh indexer and dashboard through Argo, with SealedSecrets. [] 17. One fresh, benign detection drill, with timestamps. []

Part C: Student Platform (shared with the frontend role) 18. CI builds the image into GHCR with an immutable sha-<commit> tag. [] 19. PostgreSQL and the platform deployed through Argo. [] 20. Prove persistence, login, and TLS in a real browser. []

Finish 21. More CI checks: Kubernetes manifest validation, secret scan, Terraform/Ansible checks. [optional extra] 22. Ask Kieren about branch protection on dev. [open question] 23. Part D: a provenance manifest recorded with no secrets. [] 24. Week 3 acceptance gate and the final PR into dev. []

Phase summary: what we did
Checked the chart’s real settings and found the traps: no default storage class, an alertmanager disk that would never start, and no resource requests.
Confirmed Prometheus runs as a Deployment, which matches the README.
Wrote gitops/applications/prometheus.yaml with a pinned chart, cinder-ssd storage, retention, and modest resources, with alertmanager and pushgateway off.
Next: test with yamllint, then commit, push and open the PR.
Notes recap: previous step
Downloaded the chart’s values.yaml (1264 lines) and found its main sections.
Spotted that the default disk has no storage class and no size cap.
Planned the Application file around a monitoring namespace and the cinder-ssd class.
Here's what I have
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ grep -n "storageClass" /tmp/prom-values.yaml
grep -n -A 3 "^ strategy:" /tmp/prom-values.yaml
570: ## If defined, storageClassName: <storageClass>
571: ## If set to "-", storageClassName: "", which disables dynamic provisioning
572: ## If undefined (the default) or set to null, no storageClassName spec is
576: # storageClass: "-"
1218: ## If defined, storageClassName: <storageClass>
1219: ## If set to "-", storageClassName: "", which disables dynamic provisioning
1220: ## If undefined (the default) or set to null, no storageClassName spec is
1223: # storageClass: "-"
471: strategy:
472- type: Recreate
473-
474- ## hostAliases allows adding entries to /etc/hosts inside the containers
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ nano gitops/applications/prometheus.yaml
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ head -n 3 gitops/applications/prometheus.yaml
tail -n 3 gitops/applications/prometheus.yaml
yamllint -c .yamllint.yaml gitops/; echo "exit code: $?"
git status
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
selfHeal: true
syncOptions: - CreateNamespace=true
exit code: 0
On branch feature/week3-prometheus
Untracked files:
(use "git add <file>..." to include in what will be committed)
gitops/applications/prometheus.yaml

nothing added to commit but untracked files present (use "git add" to track)
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ head -n 3 gitops/applications/prometheus.yaml
tail -n 3 gitops/applications/prometheus.yaml
yamllint -c .yamllint.yaml gitops/; echo "exit code: $?"
git status
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
selfHeal: true
syncOptions: - CreateNamespace=true
exit code: 0
On branch feature/week3-prometheus
Untracked files:
(use "git add <file>..." to include in what will be committed)
gitops/applications/prometheus.yaml

nothing added to commit but untracked files present (use "git add" to track)
pride@DESKTOP-211UNDE:~/collab-agent-scc26$

Everything checks out, so you’re ready to commit.

Where you are in the repo
Folder: ~/collab-agent-scc26.
Branch: feature/week3-prometheus.
Changes: one new file, gitops/applications/prometheus.yaml, still untracked (not yet saved into Git).
What your results confirm

# storageClass: "-" at line 576: this is the exact key name, and it’s a commented-out example inside the persistentVolume block. So server.persistentVolume.storageClass in our file is correct. The other hit, line 1223, belongs to alertmanager, which we switched off.

strategy: type: Recreate (line 471): when Prometheus is upgraded, the old pod stops first and the new one starts after, which suits a disk that only one pod can use. Recall that ReadWriteOnce means exactly that: one machine at a time.
yamllint exit code 0: your file is valid YAML, and your own CI robot would pass it.
git status: only prometheus.yaml is new, with nothing stray.
One honest limit: yamllint checks the YAML’s shape, not whether the chart accepts the settings. The settings I used (retention, retentionSize, persistentVolume.size, resources, and the enabled switches) all match lines you printed from the real values.yaml, so I’m fairly confident. The final proof comes when Argo deploys it.

This PR is different from #14
Your last PR only added a check. This one changes the live cluster the moment it merges, because the root app watches dev and deploys every new file. Recall that this is GitOps: Git is the wish list and Argo makes the cluster match it. After the merge, Argo will:

create a monitoring namespace;
install Prometheus, kube-state-metrics, and node-exporter (a pod on each of the 3 machines);
ask Cinder for a 10Gi disk.
That’s why a reviewer matters here, and why you should not merge it yourself.

Step 13: Stage and review
bash
git add gitops/applications/prometheus.yaml
git diff --cached
git add stages that one file, meaning it’s chosen for the next save-point.
git diff --cached shows what you’re about to save. Press q to leave the viewer.
Look for +apiVersion at the top, + targetRevision: 30.0.0, and + storageClass: cinder-ssd.
Remember the last PR: if a line appears in the wrong place, fix it before committing.
Step 14: Commit and push
bash
git commit -m "gitops: add Prometheus Argo CD application (chart 30.0.0)"
git push -u origin feature/week3-prometheus
The gitops: prefix says which layer the change belongs to, following CONTRIBUTING’s <type>: <what and why> format.
If the push asks for a login, use the same method that worked last time.
Step 15: Open the pull request
On GitHub, click Compare & pull request. Check that base says dev and compare says feature/week3-prometheus. Set the title to gitops: add Prometheus Argo CD application. Then select all the description text and replace it with:

## What changed?

Adds an Argo CD Application (`gitops/applications/prometheus.yaml`) that installs the prometheus-community `prometheus` Helm chart, pinned to version 30.0.0, into a new `monitoring` namespace. Settings: 10Gi Cinder SSD disk, 15d retention capped at 8GB, modest CPU/memory requests, alertmanager and pushgateway disabled. kube-state-metrics and node-exporter stay enabled for the Week 3 dashboards.

## Layer / owner

- [ ] OpenStack / Terraform
- [ ] Ansible / host configuration
- [ ] Kubernetes / Cilium / Cinder
- [ ] GitOps / Argo CD / ingress
- [x] Observability / security
- [ ] Student Project Platform
- [ ] Agent Control Plane / Hermes
- [ ] Project-specific work
- [ ] Documentation / paper / poster

## Validation evidence

Checked the setting names against the chart's real values.yaml for 30.0.0 (server.persistentVolume.storageClass/size, server.retention/retentionSize, server.resources, alertmanager.enabled, prometheus-pushgateway.enabled, and server.strategy: Recreate). Ran `yamllint -c .yamllint.yaml gitops/` locally (exit code 0). The "CI / yaml-lint" check on this PR is further evidence. After merge I will verify on k8s-cp-01: the Argo app is Synced/Healthy, pods are Running, the PVC is Bound, and the `up` query returns 1 for the scrape targets.

## Risk / rollback

Creates the `monitoring` namespace, a 10Gi PVC on cinder-ssd, and a node-exporter pod on every node. No secrets, ports or public exposure are added. The cluster has no default storage class, so storageClass is set explicitly to avoid a Pending PVC.

Rollback: revert this PR. Because Argo prune is on, the app is removed. Note the cinder-ssd reclaim policy is Delete, so reverting also deletes the Prometheus data disk.

## Checklist

- [x] I branched from `dev` and this PR targets `dev` (unless this is the reviewed release PR).
- [x] No plaintext credentials, kubeconfigs, private keys or Terraform state are committed.
- [x] Permanent Kubernetes desired-state changes are in Git rather than `kubectl edit`.
- [ ] CI/status checks pass where configured.
- [ ] Documentation/evidence was updated when behaviour changed.
      Tick the CI box yourself once the green tick appears. Then click Create pull request, ask Kieren for a review, and tell him: “This one deploys when merged, so please look at the storage class and resources.”

Your Week 3 to-do status
Items 1 to 8 are done. You’re on item 9: commit, push, open a PR, review, merge. After the merge comes item 10, verifying the deploy on the cluster. The rest of the list is unchanged from my last message.

Phase summary: what we did
Verified the last two setting names (storageClass and the Recreate update strategy) against the chart.
Created and tested prometheus.yaml with yamllint (exit code 0).
Reviewed the staged file, then committed and pushed the branch.
Opened a PR with a risk-aware description, because this one deploys to the real cluster.
Notes recap: previous step
Read the disk, resource and alertmanager settings in the Prometheus chart.
Found two traps: no default storage class, so we name cinder-ssd, and alertmanager would request a second disk, so we disable it.
Wrote the Application plan: a monitoring namespace, a pinned chart 30.0.0, a 10Gi disk, and retention settings.
Listed the full Week 3 to-do with progress.
Documentation/evidence was updated when behaviour changed.
What about this checkbox?

Leave it unticked for now. This time the answer is different from PR #14.

Why it’s different from last time
Last time (PR #14): adding a check changed almost nothing about how the system behaves, and no document mentioned CI, so “N/A” was honest.
This time: behaviour does change. Once merged, the cluster gets a monitoring system. The box says “documentation/evidence was updated”, and you haven’t yet written any docs or collected any evidence that it works. Recall that “evidence” here means proof, like command output or screenshots, that something does what you claim.
Ticking it now would claim something that isn’t true yet.

What to write in the description
Change that last line to:

- [ ] Documentation/evidence was updated when behaviour changed (Pending: deployment evidence will be added as a PR comment after merge and verification. No docs mention monitoring yet.)
      How you earn the tick later
      After the merge, on k8s-cp-01, you’ll run the checks from item 10 of the to-do list:

the Argo app shows Synced/Healthy;
the pods are Running;
the PVC is Bound;
the up query returns 1.
Copy those outputs into a comment on the PR (with nothing secret in them), then edit the description and tick the box. That evidence is also what the Week 3 acceptance gate asks for (“real targets/queries return current data”), so you’re collecting it once and using it twice.

A smaller option
If you’d rather keep things moving, tick it only when you have both parts: the verification output posted, and a short note somewhere in the repo explaining where Prometheus lives. That note can be a separate small PR later.

Notes recap: previous step
Handled the last PR checkbox (“Documentation/evidence was updated”). I left it unticked with a “Pending” note.
Learned the difference from PR #14: this PR changes the live cluster, so real evidence is needed.
Plan: after the merge, verify on the cluster, post the outputs as a PR comment, then tick the box.
Checks successful, PR's been merged let's move on to the next phase

Now the real test begins. Merging only put the wish into Git. This phase checks that the cluster actually followed it.

Phase 3b: Verify the Prometheus deployment
Recall that GitOps means Git holds the wish list and Argo CD makes the cluster match it. The root app watches dev and deploys any new file in gitops/applications/. Argo checks Git every few minutes, so your app may need a short wait before it appears.

Where you are in the repo
Folder: ~/collab-agent-scc26.
Branch: still feature/week3-prometheus on your laptop, which is now merged.
Remote dev: now contains prometheus.yaml, so your local dev is behind.
Step 1: Tidy your laptop
bash
cd ~/collab-agent-scc26
git switch dev
git pull --ff-only
git branch -d feature/week3-prometheus
ls gitops/applications
git pull --ff-only downloads the merge. Recall that “fast-forward only” refuses anything messy.
git branch -d deletes your finished local branch. If it says “not found”, that’s fine. We saw the same last time.
ls should now list prometheus.yaml alongside the 6 older files.
Step 2: Log in to the cluster machine
bash
sudo wg show wg0
ssh rocky@172.26.1.11
export KUBECONFIG=/home/rocky/.kube/config
If wg show says “No such device”, run sudo wg-quick up wg0 first.
Recall that your prompt changing to rocky@k8s-cp-01 means every command now runs on the cluster machine. The README says kubectl belongs here and nowhere else.
Step 3: Did Argo deploy it?
These are the README’s own commands. They only read.

bash
kubectl -n argocd get applications
kubectl -n monitoring get pods,pvc
kubectl -n monitoring get svc
get applications should now list prometheus. If it isn’t there yet, wait about three minutes and run it again.
get pods,pvc lists two kinds of thing at once (the comma means “and”). Recall that a pod is one running app and a PVC is a pod’s request for a disk.
get svc lists Services, the stable internal addresses other apps use to reach a pod.
What a healthy result looks like:

Thing Expected
Argo app prometheus Synced and Healthy
prometheus-server pod Running, probably 2/2 (Prometheus plus its config-reload helper)
kube-state-metrics pod Running, 1/1
prometheus-node-exporter pods 3, one per machine, all Running
PVC prometheus-server Bound, 10Gi, cinder-ssd
Why three node-exporter pods? It reports each machine’s own CPU, memory and disk, so Kubernetes runs one on every node. That’s what makes node dashboards possible.

If something is Pending: a disk can look Pending briefly. Recall that WaitForFirstConsumer means the disk is only created once a pod that needs it has been placed on a node. If the pod is also stuck, run:

bash
kubectl -n monitoring get events --sort-by=.lastTimestamp | tail -n 20
get events shows what Kubernetes has been doing, and --sort-by=.lastTimestamp puts them in time order.
| tail -n 20 keeps the 20 most recent lines.
Paste the output and we’ll read it together. Don’t fix anything with kubectl edit. The README says to fix the problem in Git and let Argo apply it.
Step 4: Check the data, not just the pods
The README warns that a Running pod only proves the process started. It doesn’t prove anything is being measured. So we ask Prometheus itself:

bash
kubectl -n monitoring exec deploy/prometheus-server -c prometheus-server -- \
 wget -qO- 'http://127.0.0.1:9090/api/v1/query?query=up' | sed 's/{"metric"/\n{"metric"/g'
What each part does:

kubectl exec runs a command inside a running container.
deploy/prometheus-server means “the pod managed by the Deployment named prometheus-server”. Recall that a Deployment is Kubernetes’ standard way to keep a pod running. We checked this earlier from the chart’s defaults.
-c prometheus-server picks which container, since the pod has two.
-- marks the end of kubectl’s options. Everything after it is the command to run inside.
wget -qO- downloads quietly (-q) and prints the result on screen (-O-).
127.0.0.1:9090 is Prometheus’s own address, as seen from inside its pod.
/api/v1/query?query=up asks the question “is each thing I’m supposed to measure answering?”
| sed ... puts each result on its own line so it’s readable.
How to read the answer:

Each line is one target, meaning one thing Prometheus tries to measure.
"job":"..." names the group, and "instance":"..." names the exact address.
The final "value":[time,"1"] means the measurement succeeded. A "0" means it failed.
Don’t panic if a few targets show 0. We’ll look at which ones, and why. Write down the real job names you see. The README says never to guess label names, and we’ll need these exact names for the Grafana dashboards in the next phase.

Step 5: Save your evidence
Copy the outputs of Steps 3 and 4 into a comment on your merged PR, with nothing secret in them (there’s nothing secret in this output). Then edit the PR description and tick “Documentation/evidence was updated”. This is the proof the README’s acceptance gate asks for (“real targets/queries return current data”).

When you’re done, type exit to return to your laptop.

Paste me these
The output of the three get commands in Step 3.
The output of the up query in Step 4.
Your Week 3 to-do status
Items 1 to 9 are done, so PR merged. Items 10 and 11 are in progress now: Argo deployed, then data verified. Next is item 12, adding Grafana.

Phase summary: what we’re doing
Tidying: updating your laptop’s dev after the merge.
Checking the deploy: Argo app status, pods, disk, and services in the monitoring namespace.
Checking the data: the up query, to prove Prometheus is actually measuring things.
Saving the evidence for the PR and the acceptance gate.
Notes recap: previous step
Decided the last PR checkbox (“evidence updated”) stays unticked until we have real deployment proof.
Merged the Prometheus PR after the CI check passed.

[rocky@k8s-cp-01 ~]$ kubectl -n argocd get applications NAME SYNC STATUS HEALTH STATUS cert-manager Synced Healthy cert-manager-config Synced Healthy cinder-csi Synced Healthy ingress-smoke-test Synced Progressing promethe

PASTED
Step 5: Save your evidence Copy the outputs of Steps 3 and 4 into a comment on your merged PR, with nothing secret in them (there's nothing secret in this output). Then edit the PR description and tick "Documentation/evidence was updated". This is the proof the README's acceptance gate asks for ("real targets/queries return current data"). When you're done, type exit to return to your laptop. Paste me these 1. The output of the three get commands in Step 3. 2. The output of the up query in Step 4. Your Week 3 to-do status Items 1 to 9 are done, so PR merged. Items 10 and 11 are in progress now: Argo deployed, then data verified. Next is item 12, adding Grafana.
Here are the outputs, just to confirm, this what you want me to paste as a comment:

If so then, lets move on

Almost, but don’t paste the whole up dump. The three get outputs are fine as they are. The up output is about 25 very long lines, and the repo is public, so a PR comment is visible to anyone. CONTRIBUTING.md says to redact environment details that aren’t needed for review. A short summary proves the same thing and exposes less.

Step 1: Make a compact version of the data check (on k8s-cp-01)
bash
kubectl -n monitoring exec deploy/prometheus-server -c prometheus-server -- \
 wget -qO- 'http://127.0.0.1:9090/api/v1/query?query=count%20by%20%28job%29%20%28up%29' | sed 's/{"metric"/\n{"metric"/g'

kubectl -n monitoring exec deploy/prometheus-server -c prometheus-server -- \
 wget -qO- 'http://127.0.0.1:9090/api/v1/query?query=sum%20by%20%28job%29%20%28up%29' | sed 's/{"metric"/\n{"metric"/g'
Recall that up gives one line per target, with 1 for “answering” and 0 for “not answering”.
count by (job) (up) asks “how many targets does each job have?”, and sum by (job) (up) asks “how many of them are up?”. If the two numbers match for every job, everything is answering.
The %20, %28 and %29 are how spaces and brackets are written inside a web address.
The output is one short line per job instead of 23 long ones.
Step 2: Post the evidence comment on the merged PR
Use this in a PR comment, with the real outputs pasted inside the code fences:

Post-merge verification on k8s-cp-01:

Argo CD: `prometheus` is Synced / Healthy.

Pods and PVC in `monitoring`: (paste the `get pods,pvc` output)

Prometheus data check: all scrape targets report up (count equals sum for every job): (paste the two compact outputs)
Then edit the PR description and tick “Documentation/evidence was updated”. This is the proof the README’s gate asks for (“real targets/queries return current data”).

What your output proves
Argo: prometheus is Synced and Healthy. Recall that this means the cluster matches Git and the app is running properly.
Pods: the server is 2/2, kube-state-metrics is 1/1, and there are 3 node-exporter pods, one per machine.
Disk: the PVC is Bound, 10Gi, cinder-ssd. The storage-class trap from earlier didn’t bite.
Data: I counted 23 targets and every one reports "1". That’s your acceptance-gate evidence.
The real names you needed (for the dashboards)
The README said never to guess label names. Here are the real ones from your output.

Job What it covers Targets
kubernetes-nodes The machines (Kubernetes’ own view) 3
kubernetes-nodes-cadvisor Per-container CPU and memory 3
kubernetes-service-endpoints node-exporter, kube-state-metrics, CoreDNS, cilium-envoy 10
kubernetes-pods cert-manager, traefik, cilium-operator 5
kubernetes-api-servers The Kubernetes API 1
prometheus Prometheus itself 1
Things that will matter when we build dashboards:

node-exporter and kube-state-metrics don’t have their own job. They sit inside kubernetes-service-endpoints, so we pick them out with the service label.
For node-exporter, instance is an IP and port (like 172.26.1.11:9100), while kubernetes-nodes uses the machine name. The node label holds the machine name for both, so that’s the label to match on.
cAdvisor gives CPU and memory per container, which is what the README’s “Student Project Platform workload health” panel will need once that app exists.
Phase 3c begins: Grafana (the safe way)
Before writing any file, one check. Last time I told you to pin Grafana chart 10.5.15. While looking into it, I found that Grafana Labs has been moving several of its Helm charts (Loki and Tempo, confirmed) to a new community repository, grafana-community, and leaving the old one for enterprise users. I couldn’t confirm whether the plain grafana chart is among them. So we check before using it. This only reads:

bash
curl -s https://grafana.github.io/helm-charts/index.yaml | awk '/^ grafana:$/{f=1} f&&/^ (appVersion|deprecated|description|version):/{print} f&&/^ version:/{exit}'
Recall that the same awk pattern gave you the version numbers earlier. This time it prints four lines for the newest Grafana chart: appVersion, deprecated (if present), description and version.
If the description starts with “DEPRECATED”, or you see deprecated: true, the chart has moved, and we’ll use the community repo instead.
appVersion is the Grafana software version inside the package, and version is the version of the package. Recall that the chart version is the version of the package, not the software.
Paste the output. Then we’ll download that chart’s values.yaml and read it the way we did for Prometheus.

Where you are in the repo
Folder: ~/collab-agent-scc26 on your laptop, on dev if you did the tidy last time.
Branch next: feature/week3-grafana, which we’ll create after the chart check.
Files so far: gitops/applications/prometheus.yaml is now on dev.
Your Week 3 to-do status
Items 1 to 11 are done (Prometheus is deployed and the data is verified, once the evidence comment is posted). Next is item 12: Grafana as an Argo Application. Then 13: Git-managed dashboards. Everything after that is unchanged.

Phase summary: what we did
Verified the Prometheus deployment: Argo Healthy, pods Running, the disk Bound.
Verified the data: 23 targets, all up.
Recorded the real job and label names that the dashboards need.
Prepared the evidence comment for the PR, in a compact form.
Started Grafana with a check on whether its chart has moved.
Notes recap: previous step
Merged the Prometheus PR after the CI check passed.
Checked the cluster: the app is Synced/Healthy, the disk Bound, and all targets report up.
Next: decide which Grafana chart to use, then write grafana.yaml.
So do I paste this

Post merge verification on k8s-cp-01:
Argo CD: `prometheus` is Synced / Healthy.

Pods and PVC in `monitoring`:

NAME READY STATUS RESTARTS AGE
pod/prometheus-kube-state-metrics-674c5d9995-m88ss 1/1 Running 0 43m
pod/prometheus-prometheus-node-exporter-2t6kx 1/1 Running 0 43m
pod/prometheus-prometheus-node-exporter-9ct89 1/1 Running 0 43m
pod/prometheus-prometheus-node-exporter-n2kf8 1/1 Running 0 43m
pod/prometheus-server-598cd8c4bb-2g4mj 2/2 Running 0 43m

Prometheus data check: all scrape targets report up (count equals sum for every job): {"status":"success","data":{"resultType":"vector","result":[
{"metric":{"job":"kubernetes-pods"},"value":[1795518901.984,"5"]},
{"metric":{"job":"kubernetes-service-endpoints"},"value":[1795518901.984,"10"]},
{"metric":{"job":"kubernetes-nodes-cadvisor"},"value":[1795518901.984,"3"]},
{"metric":{"job":"kubernetes-nodes"},"value":[1795518901.984,"3"]},
{"metric":{"job":"kubernetes-api-servers"},"value":[1795518901.984,"1"]},
{"metric":{"job":"prometheus"},"value":[1795518901.984,"1"]}]}}{"status":"success","data":{"resultType":"vector","result":[
{"metric":{"job":"kubernetes-pods"},"value":[1795518902.127,"5"]},
{"metric":{"job":"kubernetes-service-endpoints"},"value":[1795518902.127,"10"]},
{"metric":{"job":"kubernetes-nodes-cadvisor"},"value":[1795518902.127,"3"]},
{"metric":{"job":"kubernetes-nodes"},"value":[1795518902.127,"3"]},
{"metric":{"job":"kubernetes-api-servers"},"value":[1795518902.127,"1"]},
{"metric":{"job":"prometheus"},"value":[1795518902.127,"1"]}]}}[rocky@k8s-cp-01 ~]$
?

Nearly, but not quite as it stands. The content is right, and there are four small fixes.

What to fix
The PVC output is missing. You wrote “Pods and PVC” but only pasted the pods. The disk line is the one that proves storage worked.
The two data results are glued together (}}{"status"...), and neither is labelled. Anyone reading it can’t tell which is the count and which is the sum.
There’s a stray terminal prompt ([rocky@k8s-cp-01 ~]$) on the last line, which is just noise.
Nothing is inside a code block. On GitHub, plain pasted output loses its line breaks and spacing, so the tables turn into a mess. Wrapping each output in triple backticks keeps it readable.
Your numbers are correct. Every job’s count equals its sum (5/5, 10/10, 3/3, 3/3, 1/1, 1/1), so all 23 targets are up. Recall that “count equals sum” works because up is 1 for a working target and 0 for a broken one, so the sum only matches the count if nothing is broken.

Paste this instead
I turned the raw JSON into a small table. It says the same thing and is easier to read. Copy everything between the lines, including the backticks:

Post-merge verification on k8s-cp-01:

**Argo CD**

```
NAME         SYNC STATUS   HEALTH STATUS
prometheus   Synced        Healthy
```

**Pods and PVC in `monitoring`**

```
NAME                                                 READY   STATUS    RESTARTS   AGE
pod/prometheus-kube-state-metrics-674c5d9995-m88ss   1/1     Running   0          43m
pod/prometheus-prometheus-node-exporter-2t6kx        1/1     Running   0          43m
pod/prometheus-prometheus-node-exporter-9ct89        1/1     Running   0          43m
pod/prometheus-prometheus-node-exporter-n2kf8        1/1     Running   0          43m
pod/prometheus-server-598cd8c4bb-2g4mj               2/2     Running   0          43m

NAME                                      STATUS   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
persistentvolumeclaim/prometheus-server   Bound    10Gi       RWO            cinder-ssd     43m
```

(PVC volume ID trimmed.)

**Prometheus data check**

Queried `count by (job) (up)` and `sum by (job) (up)` on the Prometheus API from inside the server pod. For every job the count equals the sum, so all 23 targets report up.

| job                          | targets (count) | up (sum) |
| ---------------------------- | --------------- | -------- |
| kubernetes-pods              | 5               | 5        |
| kubernetes-service-endpoints | 10              | 10       |
| kubernetes-nodes-cadvisor    | 3               | 3        |
| kubernetes-nodes             | 3               | 3        |
| kubernetes-api-servers       | 1               | 1        |
| prometheus                   | 1               | 1        |

Where it goes
Open your merged Prometheus PR on GitHub.
Scroll to the comment box at the bottom.
Paste the block, and click the Preview tab to check the table and boxes look right.
Click Comment.
Then tick the PR checkbox
Click ... at the top right of the PR description, then Edit.
Change - [ ] Documentation/evidence was updated when behaviour changed (Pending: ...) to - [x] Documentation/evidence was updated when behaviour changed (post-merge verification posted as a comment).
Click Update comment.
Recall that this is the evidence the README’s acceptance gate asks for: “real targets/queries return current data”.

What’s still waiting on you
Last time I asked for the Grafana chart check (the curl ... | awk line that shows whether the chart is deprecated). Run that on your laptop and paste the output, and we’ll continue with Grafana.

Notes recap: previous step
Prepared the PR evidence comment. The raw up output was too long, so we summarised it as a table (23 targets, all up).
Learned why to redact: the repo is public, so PR comments should only show what a reviewer needs.
Recorded the real job names for the dashboards: kubernetes-nodes, kubernetes-nodes-cadvisor, kubernetes-service-endpoints, kubernetes-pods, kubernetes-api-servers, and prometheus.
Started Grafana with a check on whether its Helm chart has moved to a community repository.
Let's move on
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ curl -s https://grafana.github.io/helm-charts/index.yaml | awk '/^ grafana:$/{f=1} f&&/^    (appVersion|deprecated|description|version):/{print} f&&/^    version:/{exit}'
    appVersion: 12.3.1
    deprecated: true
    description: The leading tool for querying and visualizing time series and metrics.
    version: 10.5.15
pride@DESKTOP-211UNDE:~/collab-agent-scc26$

The result changes the plan, and checking first paid off.

What your output means
deprecated: true
Deprecated means the chart’s own authors have marked it “don’t use this anymore”. It’s like a product that’s been discontinued.
Grafana Labs has been moving several charts (Loki and Tempo confirmed) to a community-run repository, grafana-community. I haven’t confirmed the same for Grafana’s own chart, but this flag, together with that pattern, says to look there.
Why it matters: an abandoned chart stops getting fixes, including security fixes. Pinning to 10.5.15 would have meant deploying something frozen in time.
Note that appVersion: 12.3.1 is the Grafana software inside the package, and version: 10.5.15 is the package itself. Recall that these are two different numbers.
An idea for later (it’s a CI/CD job): a CI check that fails when a pinned chart has been marked deprecated would catch this automatically. Put that on your wish list for after Week 3.

Where you are in the repo
Folder: ~/collab-agent-scc26.
Branch: dev, with no new branch yet.
Changes: none.
Step 1: Check the community repo (read-only)
bash
curl -s https://grafana-community.github.io/helm-charts/index.yaml | awk '/^ grafana:$/{f=1} f&&/^    (appVersion|deprecated|description|version):/{print} f&&/^    version:/{exit}'
This is the same command as before, pointed at the new repository. Recall that awk finds the grafana: entry and prints its first few lines.
You want a description, an appVersion and a version, and no deprecated: true.
Chart numbers may have restarted at the move. For Tempo, the old 1.24.4 became 2.2.3 in the new repo. So don’t expect 10.5.15 here. Use whatever it prints.
If it prints nothing, tell me. It would mean the Grafana chart isn’t in that repo, and we’d search again.
Step 2: Download its default settings
bash
VER=$(curl -s https://grafana-community.github.io/helm-charts/index.yaml | awk '/^ grafana:$/{f=1} f&&/^    version:/{print $2; exit}')
echo "$VER"
curl -s "https://raw.githubusercontent.com/grafana-community/helm-charts/grafana-$VER/charts/grafana/values.yaml" -o /tmp/grafana-values.yaml
wc -l /tmp/grafana-values.yaml
head -n 3 /tmp/grafana-values.yaml
VER=$(...) stores the command’s output in a variable named VER. Recall that you used the same $( ) trick earlier to read your private key into PRIV without printing it.
echo "$VER" shows what was stored, so you can see the version.
The second curl builds the web address using $VER, then saves the chart’s values.yaml into /tmp, outside your repo.
wc -l counts the lines. Prometheus’s file had 1264, and this one should be many hundreds as well.
If wc -l says 1, or the file starts with 404: Not Found, the address I guessed for the version label is wrong. Paste what you see and we’ll find the right one. I haven’t confirmed how this repo names its releases.
Step 3: Create your branch
bash
git switch -c feature/week3-grafana
git branch
You should see \* feature/week3-grafana.

Paste me these
The output of Step 1 (the community repo check).
The output of Step 2 (the version, line count and the first 3 lines).
Then we’ll read the settings the same way we did for Prometheus: storage, resources, dashboards, and how to switch on the “sidecar” that loads dashboards from Git. Recall that the README wants dashboards stored in Git, not built by hand in the web page.

Your Week 3 to-do status
Items 1 to 11 are done. Item 12 (Grafana as an Argo Application) is in progress:

12a. Check the chart’s status: done (the old repo is deprecated).
12b. Find the right chart and read its settings: now.
12c. Write gitops/applications/grafana.yaml: next.
12d. PR, review, merge, verify: after that.
Then item 13 is Git-managed dashboards, and the rest of the list stays the same.

Phase summary: what we did
Discovered the old Grafana chart is deprecated, which is why we check before pinning.
Switched to the grafana-community repository.
Started reading the new chart’s real settings.
Created the feature/week3-grafana branch.
Notes recap: previous step
Fixed the PR evidence comment: added the missing PVC output, turned the raw JSON into a table, and wrapped the outputs in code blocks.
Ticked the evidence checkbox after posting the comment.
Ran the Grafana chart check, which found the old chart marked deprecated.
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ curl -s https://grafana-community.github.io/helm-charts/index.yaml | awk '/^ grafana:$/{f=1} f&&/^    (appVersion|deprecated|description|version):/{print} f&&/^    version:/{exit}'
    appVersion: 13.3.1
    description: The leading tool for querying and visualizing time series and metrics.
    version: 14.5.0
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ VER=$(curl -s https://grafana-community.github.io/helm-charts/index.yaml | awk '/^  grafana:$/{f=1} f&&/^ version:/{print $2; exit}')
echo "$VER"
curl -s "https://raw.githubusercontent.com/grafana-community/helm-charts/grafana-$VER/charts/grafana/values.yaml" -o /tmp/grafana-values.yaml
wc -l /tmp/grafana-values.yaml
head -n 3 /tmp/grafana-values.yaml
14.5.0
2074 /tmp/grafana-values.yaml

# $schema: ./values.schema.json

global:
pride@DESKTOP-211UNDE:~/collab-agent-scc26$ git switch -c feature/week3-grafana
git branch
Switched to a new branch 'feature/week3-grafana'
dev

- feature/week3-grafana
  main

Both checks worked, and the picture is now clear.

What your results mean
Community repo, no deprecated line: the chart is alive here, and this is where we pin it.
version: 14.5.0 and appVersion: 13.3.1: the numbering restarted at the move, as I warned. Recall that the chart version (14.5.0) is the package and the app version (13.3.1) is the Grafana software inside it.
14.5.0 echoed back, and 2074 lines: the download address I guessed was correct, so your /tmp/grafana-values.yaml is the real settings file. It’s longer than Prometheus’s (1264 lines), because Grafana has many options.
Branch: feature/week3-grafana is created.
Where you are in the repo
Folder: ~/collab-agent-scc26.
Branch: feature/week3-grafana, with no file changes yet.
File to create later: gitops/applications/grafana.yaml.
Also coming: a folder gitops/resources/monitoring/ for the dashboards stored as files.
What I expect we’ll have to handle
From how this chart usually works, these are the traps. I haven’t verified them for 14.5.0, which is why the next step reads the real file.

Storage. Grafana saves its own data (users, settings) on a disk, and persistence is usually off by default. We want it on, on cinder-ssd, like Prometheus. Recall that the cluster has no default storage class, so we must name it.
The admin password. If we don’t set one, the chart makes a random password. With Argo, that password may get regenerated on every sync, which would lock you out. And a password written into grafana.yaml would sit in plaintext in a public repo, which the README forbids. The answer is a SealedSecret, an encrypted secret that’s safe to keep in Git. Recall that you saw one already: cinder-csi-cloud-config-sealed.yaml.
Update strategy. A disk that only one pod can use needs the old pod stopped before the new one starts. Prometheus’s chart used Recreate for this. We need to check what Grafana defaults to.
Dashboards from Git. The README says not to build the final dashboard by hand in the web page. The chart has a sidecar, a helper container that watches for labelled ConfigMaps and loads them as dashboards. Recall that a ConfigMap is a small piece of stored text, and you saw one in the Week 2 check.
Connecting to Prometheus. Grafana needs a datasource, meaning the address of Prometheus. It’s the Service you saw: prometheus-server on port 80 in the monitoring namespace.
Step 1: Read the real settings (read-only)
bash
grep -n -E "^(persistence|admin|adminUser|adminPassword|sidecar|datasources|dashboardProviders|dashboards|dashboardsConfigMaps|ingress|resources|deploymentStrategy|service|replicas):" /tmp/grafana-values.yaml
grep -n -m1 -A 30 "^persistence:" /tmp/grafana-values.yaml
grep -n -m1 -B 1 -A 12 "^admin:" /tmp/grafana-values.yaml
grep -n -m1 -A 4 "^deploymentStrategy:" /tmp/grafana-values.yaml
The first command lists which of these top-level sections exist, and where. Recall that grep -n -E prints matching lines with their line numbers, and | means “or”. If a section from my list is missing, the chart changed, and we adapt.
-m1 means “stop after the first match”, which keeps the output short.
-A 30 prints 30 lines after a match, and -B 1 prints 1 line before it.
The admin: block should show how to point Grafana at an existing secret for the password.
Step 2: Read the repo’s own secrets guide
The docs folder had a file we haven’t opened yet:

bash
less docs/SECRETS-AND-LOCAL-FILES.md
less opens a file for reading. Use the arrow keys to scroll and q to quit.
It should explain how the team creates SealedSecrets. That matters because the tool that makes them (kubeseal) normally needs access to the cluster, and the README says not to put the cluster’s admin kubeconfig on your laptop.
Paste the output of Step 1, plus the key parts of Step 2. If the file is long, paste the sections about SealedSecrets.

A question for Kieren
He made cinder-csi-cloud-config-sealed.yaml, so ask: “How did you create the SealedSecret without a kubeconfig on your laptop? Is there a public certificate file I can use with kubeseal?” The answer decides how we make Grafana’s password safely.

Your Week 3 to-do status
Items 1 to 11 are done. Item 12 (Grafana as an Argo Application):

12a. Check the chart’s status: done.
12b. Find the right chart and read its settings: in progress (downloaded, now reading).
12c. Make the admin password a SealedSecret: next.
12d. Write grafana.yaml, then PR, review, merge, verify: after that.
Item 13 (Git-managed dashboards) is next, and the rest of the list is unchanged.

Phase summary: what we did
Confirmed the grafana-community chart is current and not deprecated.
Chose to pin chart 14.5.0 (Grafana 13.3.1 inside).
Downloaded the chart’s values.yaml (2074 lines) into /tmp.
Created feature/week3-grafana.
Listed the five things to get right: storage, the admin password, the update strategy, dashboards from Git, and the Prometheus connection.
Notes recap: previous step
Found the old Grafana chart marked deprecated: true, so we avoid pinning something abandoned.
Switched to the community repository and downloaded its settings file.
Started the Grafana branch.

Over 90% of your 5-hour limit used · Resets at 2:50 PM
