# Continuous Intergaration and Continuous deployment/development

This role has 2 parts to it CI and CD.
- CI: When developer push some code onto github, automated checks for errors are done.
- CD: When checks are completed, that code can now be prepared for release or automatically deployed to the system.
---
## Tools used:
1. Argo CD (For continuous deployment)
    - A tool **INSIDE the cluster**
    - It watches a git folder
    - It then makes the cluster match whatever git says
    - This style is called **GitOps**, meaning git is the only source of truth for what should be running
    - Hanldes only **kubernetes files** (files called YAML manifests).

2. Github Actions  (For continuous integration)
    - This is a buitl-in github robot
    - It runs checks whenever one pushes or opens a pull request
---
## Phase 2:
This is where I create the branch and and write the first Github Actions workflow

### Phase 1 outcome:
I was able to now read the 3 files that run the whole cluster

All 3 files jave the same skeleton. That skeleton is an **Argo CD** application, which says "deploy this thing"
#### Files Controlling the cluster
```
apiVersion / kind   # "this is an Argo Application" (a label, I won't change it)
metadata            # its name, and the namespace argocd where Argo keeps it
spec:
  source            # WHAT to deploy and where to get it (github/...)
  destination       # WHERE in the cluster to put it (kubernetes...)
  syncPolicy        # HOW Argo should behave (Prune & selfheal)

```

**Namespace** --> 

#### Ways to deploy an application

|  | 1. taefik.yaml | 2. ingress-smoke-test.yaml |
|---|---|---|

Both patterns will be used since Prometheus and Grafana usually arrive as Helm charts, so you write an Application file with a `values`: block. Our own platform pieces will live in resources/ folders.

**The root app is the boss**

##### Adsitional info about the root app
root-application.yaml says: 
    “Watch the folder gitops/applications on the dev branch, recurse into subfolders, and deploy every Application file you find there. Prune and self-heal.”

That has two consequences for you:

Adding a new Application file to gitops/applications/ and merging to dev is how you deploy something. Nothing else is needed.
Deleting a file or merging a broken one hits the real cluster. Because of prune, removing a file removes the app. That’s the strongest reason CI checks matter. They catch mistakes before the merge.

---

### What CI will check

These are the first jobs for your workflow:

1. YAML is valid: no indentation typos.
2. Kubernetes files are valid: the files in gitops/resources/ match Kubernetes’ expected shape.
3. No secrets in plaintext: a scan for passwords or keys.
4. Terraform and Ansible are tidy: formatting and lint checks.

### Steps taken
1. Made the branch

2. Created the github workflow
3.
4. 


