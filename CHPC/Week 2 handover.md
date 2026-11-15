# SCC26 — Week 2 Handover: Kubernetes, GitOps, Ingress & VPN Access


## Week 2's Goal:
**Week 2 focuses on establishing the Kubernetes environment and implementing GitOps so that applications deployed to the cluster are managed through Git rather than manually.**

---

## 1. Project overview

**Repository:** `collab-agent-scc26`  
**Current working branch:** `feature/week2-kubernetes`  
**Target integration branch:** `dev`  
**Project:** Human–Agent Collaboration — Discord Operations Gateway

The project aims to build an AI chatbot/interface that allows users to interact with platform agents through Discord and potentially Telegram, while preserving:

- User identity and authentication.
- Authorisation and access control.
- Persistent conversation history.
- Auditability of agent actions.
- Least-privilege access to infrastructure and services.
---

## 2. Infrastructure already deployed

The Kubernetes cluster is already operational.

### Nodes

| Node | IP address | Role |
|---|---|---|
| `edge-01` | Check private inventory | WireGuard gateway and edge services |
| `k8s-cp-01` | `172.26.1.11` | Kubernetes control plane |
| `k8s-worker-01` | `172.26.1.21` | Kubernetes worker |
| `k8s-worker-02` | `172.26.1.22` | Kubernetes worker |

The Kubernetes API endpoint is:

`172.26.1.10:6443`

HAProxy provides this endpoint and currently forwards traffic to the control-plane node. It is an API load balancer, not an application ingress controller.

### Verified components

| Component | Status |
|---|---|
| Kubernetes v1.36.4 | Operational |
| containerd v2.3.4 | Operational |
| Cilium CNI | Operational |
| Argo CD v3.5.0 | Operational |
| Sealed Secrets v0.39.1 | Operational |
| Cinder CSI | Operational |
| OpenStack persistent storage | Tested successfully |
| WireGuard infrastructure | Implemented and merged into `dev` |

All three Kubernetes nodes were reporting `Ready`.

Cilium connectivity tests passed, with 82 tests successful and no failures.

Cinder persistent storage was tested: a PVC was bound, a marker file was written, the original pod was deleted, and the data remained accessible after recreating the pod.

## 3. WireGuard VPN access

**Your teammate must connect to the project's WireGuard VPN before accessing the private cluster network.**

The WireGuard gateway infrastructure was implemented during Week 1 and merged into `dev`.

Relevant repository history:

- `12c40c2` — Add WireGuard VPN gateway playbook.
- `dde749a` — Update security group to allow WireGuard UDP port and SSH via WireGuard.
- `d2e4250` — Complete Week 1 VPN infrastructure setup.
- `2fac1b5` — Terraform changes to allow VPN access to the cluster.

The WireGuard gateway is associated with `edge-01`. The Ansible playbook responsible for configuring it is:

`infrastructure/ansible/playbooks/edge.yml`

The security group permits WireGuard UDP traffic and SSH access through the VPN.

### 3.1 What the teammate needs

They need:

1. A WireGuard client installed on their computer.
2. An individual WireGuard peer configuration generated for them.
3. The peer added to the server-side WireGuard configuration.
4. SSH access to the Kubernetes control-plane node.

Do not give them your own WireGuard private key or reuse your VPN configuration.

### 3.2 Generate their WireGuard key pair

On the teammate's computer, they should install WireGuard tools and generate their own key pair.

On Linux:

```bash
wg genkey | tee privatekey | wg pubkey > publickey
```

This generates:

- `privatekey` — must remain on the teammate's computer.
- `publickey` — can be sent to the VPN administrator.

**Only the public key should be shared. Never send the private key.**

### 3.3 Add the teammate as a VPN peer

The VPN administrator must:

1. Obtain the teammate's public key.
2. Allocate an unused VPN address from the project's WireGuard VPN subnet.
3. Add a peer entry to the server-side WireGuard configuration on `edge-01`.
4. Apply the updated configuration.
5. Generate an individual client configuration containing the teammate's private key, server public key, endpoint, VPN address and appropriate routes.
6. Send the client configuration securely to the teammate.

The exact VPN subnet, UDP port, public endpoint and server configuration should be taken from the existing private Ansible inventory and WireGuard configuration.

Do not invent a new VPN subnet or overwrite existing peers.

### 3.4 Connect to the VPN

On Linux, once the teammate has received their client configuration:

```bash
sudo wg-quick up ./teammate.conf
```

To verify the connection:

```bash
sudo wg show
ip -brief address
ip route
```

The WireGuard interface should appear, and the VPN peer should show a successful recent handshake after traffic is exchanged.

To disconnect:

```bash
sudo wg-quick down ./teammate.conf
```

The exact client command may differ depending on the operating system. Windows and macOS users can import the supplied configuration through the official WireGuard application.

### 3.5 Access the Kubernetes control plane

Once connected, the teammate should test SSH access to the control-plane node using the project's VPN-reachable address.

```bash
ssh rocky@172.26.1.11
```

This assumes the VPN routing and SSH configuration permit access to that address. If the connection fails, check the actual VPN routes and private inventory rather than assuming the address is publicly reachable.

After SSH access is established, Kubernetes commands can be run on the control-plane node:

```bash
KUBECONFIG=/home/rocky/.kube/config /usr/bin/kubectl get nodes -o wide
```

Other useful commands:

```bash
KUBECONFIG=/home/rocky/.kube/config /usr/bin/kubectl get pods -A

KUBECONFIG=/home/rocky/.kube/config /usr/bin/kubectl get applications -n argocd

KUBECONFIG=/home/rocky/.kube/config /usr/bin/kubectl get svc -A
```

The existing administrator kubeconfig is on the control-plane node at:

`/home/rocky/.kube/config`

**Do not distribute this administrator kubeconfig to the teammate.** They can inspect the cluster through SSH while connected to the VPN. If they need direct `kubectl` access from their own computer, create a separate Kubernetes identity with appropriately restricted RBAC permissions and issue a dedicated kubeconfig.

### 3.6 Troubleshooting

If the teammate cannot connect:

| Symptom | What to check |
|---|---|
| No WireGuard handshake | Public endpoint, UDP port, firewall/security group and peer configuration |
| Handshake but no cluster access | VPN AllowedIPs, routes and edge-node forwarding |
| VPN works but SSH fails | SSH service, SSH keys, firewall rules and VPN-reachable address |
| SSH works but kubectl fails | Kubeconfig location, permissions and Kubernetes API availability |

The existing SSH access restrictions should not be relaxed simply to make VPN setup easier.

## 4. Kubernetes architecture

### Nodes and responsibilities

- `k8s-cp-01`: Kubernetes control plane.
- `k8s-worker-01`: Kubernetes worker.
- `k8s-worker-02`: Kubernetes worker.
- `edge-01`: external networking and WireGuard gateway; not a Kubernetes node.

Workloads are scheduled onto the cluster nodes according to Kubernetes scheduling rules. Each Kubernetes node runs containerd and kubelet.

### API load balancer

The Kubernetes API endpoint is:

`172.26.1.10:6443`

HAProxy forwards requests to the control-plane API at `172.26.1.11:6443`.

This is for Kubernetes API traffic, not application traffic.

## 5. GitOps architecture

Argo CD is already installed and operational.

The root Argo CD Application is configured to:

- Monitor the repository.
- Track the `dev` branch.
- Monitor `gitops/applications/`.
- Discover application manifests recursively.
- Automatically synchronise changes.
- Prune resources removed from Git.
- Self-heal resources that drift from the desired Git configuration.

**Important:** Argo CD currently watches `dev`, not `feature/week2-kubernetes`.

Changes made on the feature branch will not automatically deploy to the cluster. The completed work should be merged into `dev` through one pull request before integration testing.

The existing Cinder CSI application is already synchronised and healthy.

## 6. Repository structure

The relevant repository layout is:

```text
collab-agent-scc26/
│
├── infrastructure/
│   ├── ansible/
│   │   ├── inventories/
│   │   │   └── private/
│   │   │       └── hosts.yml
│   │   ├── play