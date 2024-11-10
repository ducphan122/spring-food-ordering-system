- Create kubernetes cluster and deploy microservices into that cluster
- run kafka in kubernetes using cp helm chart, which is used for Confluent Kafka images
- Then push docker images into Google Kubernetes Engine (GKE) and run whole application in Google Cloud

## Concepts

- Nodes: The physical servers or virtual machines that creates a kubernetes cluster.
- Pods: The smallest unit of execution in kubernetes. They are deployable units, and consists of one or more containers, which internally consists of one or more application.
- Controllers: Used to deploy, manage and scale the pods.
- Services: Used to expose deployments through NodePort or LoadBalancer.
- NodePort: Exposes the service on a static port on the node IP address. NodePorts are in the 30000-32767 range by default.
  - When exposing a Kubernetes service via NodePort, you can access it through http://<NodeIP>:<NodePort> in the browser, where NodeIP is the IP address of any node in the cluster.
- LoadBalancer: Exposes a single external ip, and internally holds multiple ports to distribute the load.

### NodePort 
  - It's one of three ways to expose a service in Kubernetes (others being ClusterIP and LoadBalancer)
  - Creates a port mapping that works like this:
    - External Traffic -> NodePort (30000-32767) -> Service -> Pod
  - Example: If you set NodePort to 30080, the service will be accessible from:
    - localhost:30080 (if running locally)
    - <any-node-ip>:30080 (if running on a cluster)
  - Use cases:
    - Development and testing environments
    - When you need a simple way to expose services externally
    - When you don't have a cloud provider's load balancer available
  - Limitations:
    - Only one service per port
    - Can only use ports in the 30000-32767 range
    - If your node/VM IP address changes, you need to deal with the change
  
## Instructions
1. Kubernetes Cluster
First, we want to create a local kubernetes cluster using docker desktop (which use kind under the hood).
- docker desktop -> setting -> kubernetes -> enable then kubectl get nodes

2. Confluent Platform Helm Chart
- opensource to use cp-helm-charts locally. clone the repo

3. Install helm 
- scoop install helm

4. Install
- terminal to infra folder, run command: helm install local-confluent-kafka helm/cp-helm-charts --version 0.6.0
  - local-confluent-kafka is the name of our kafka helm cluster
  - helm/cp-helm-charts is the directory of the helm chart that we clone from step 2
