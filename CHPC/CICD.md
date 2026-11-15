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

#### 2. Created the github workflow

GitHub Actions is GitHub’s built-in robot. When something happens in the repo (the trigger), GitHub starts a temporary Linux machine (the runner), and that machine follows a list of instructions you wrote

- **Workflow**: An instruction file, kept in `.github/workflows/`.
- **Job**: Groups of steps that run on one runner
- **Step**: An action, that action's either
    1. `uses`: borrow a ready-made-action
    2. `run`: type a shel command

Our first workflow is deliberately tiny. On every pull request to dev, it checks that all YAML files in gitops/ are valid. A typo that breaks YAML could stop Argo from deploying, so this catches it before the merge.

##### Workflow
1. Created a dir `.github/workflows`
2. Wrote a file `ci.yaml` in the dir
###### The file
```
name: CI

on:
  pull_request:
    branches: [dev]

permissions:
  contents: read

jobs:
  yaml-lint:
    runs-on: ubuntu-latest
    steps:
      - name: Check out the code
        uses: actions/checkout@v4

      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: "3.12"

      - name: Lint YAML in gitops/
        run: |
          pip install yamllint
          yamllint -d relaxed gitops/
```
###### What each part of the file means
- `name`: CI -> is the label shown in GitHub’s “Checks” list.
- `on: pull_request: branches: [dev]` -> is the trigger: run whenever a PR targets dev.
- `permissions: contents: read` -> gives the robot read-only access to the repo. This is the principle of least privilege (only what’s needed).
- `jobs: yaml-lint:` -> is the name of our one job.
- `runs-on: ubuntu-latest` -> says use a fresh Ubuntu machine.
- `uses: actions/checkout@v4` -> downloads your code onto the runner. Without it, the runner has no files.
- `uses: actions/setup-python@v5` -> installs Python so we can use pip.
- `run: | ` -> starts a block of shell commands. pip install yamllint installs the checker.
- `yamllint -d relaxed gitops/` -> checks every YAML file in gitops/. -d relaxed picks a gentle rule set (style issues are warnings, and only real syntax errors fail).

###### Test failure
Tested using:
```
sudo apt install -y yamllint
yamllint -d relaxed gitops/
echo "exit code: $?"
```
- $? holds the result of the last command. 0 means success, and anything else means a failure.
- Warnings may appear, which is fine. We’re looking for exit code: 0.
- A sealed secret file is long, but with relaxed it shouldn’t matter.

Exit code gotten is 1

The error was because I assumed the relaxed rule set wouldn’t complain about missing end-of-file newlines, and in this version it does.

###### The fix
To fix the error I turned that one rule off in a config file
- CI still catches yaml mistakes

1. Wrote a file `nano .yamllint.yaml` with:
```
extends: relaxed

rules:
  new-line-at-end-of-file: disable

```
- `extends: relaxed` means “start from the gentle rule set”.
- `rules:` then lists changes to it.
- `new-line-at-end-of-file: disable` switches that one rule off.
2. Replaced `yamllint -d relaxed gitops/` with `yamllint -c .yamllint.yaml gitops/` to point the workflow at it.
- -d relaxed meant “use a built-in rule set”.
- -c .yamllint.yaml means “use the config file at this path” (-c is for config).

Which then worked
3.
4. 


