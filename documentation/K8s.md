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

4. Install Confluent Platform Helm Chart
- terminal to infra folder, run command: helm install local-confluent-kafka helm/cp-helm-charts --version 0.6.0
  - local-confluent-kafka is the name of our kafka helm cluster
  - helm/cp-helm-charts is the directory of the helm chart that we clone from step 2
  - make sure to changed policy/v1beta1 -> policy/v1
  - Make sure to copy the zookeeper host printed on terminal, we will need it in when creating topics
  - For development locally, "local-confluent-kafka-cp-zookeeper-headless" is the name of the zookeeper service

5. Create deployments
- kubectl apply -f kafka-client.yml
- kubectl apply -f postgres-deployment.yml

6. Create topics
- kubectl cp create-topics.sh kafka-client:/kafka-client-storage
- kubectl exec -it kafka-client -- /bin/bash
- cd ../..& cd kafka-client-storage
- sh create-topics.sh local-confluent-kafka-cp-zookeeper-headless
- exit

Notes: the $1 in the script allows us to pass in the zookeeper host as parameter

7. Build docker images and start deployments
- Check all pom.xml in each microservice folder container, make sure the build command is uncommented. If you make no changes to the code, you can comment the build command.
- mvn clean install at root
- docker images | grep food.ordering.system
- docker images | Where-Object {$_ -like "*food.ordering.system*"}
- kubectl apply -f application-deployment-local.yml

8. Test
- POST /customers
- POST /orders
- GET /orders/{orderTrackingId}
- CHeck orderStatus from PENDING -> PAID -> APPROVED

9. Delete kafka helm chart and kafka client deployment
- helm uninstall local-confluent-kafka
- kubectl delete -f kafka-client.yml -f postgres-deployment.yml -f application-deployment-local.yml


## Horizontal Pod Autoscaler (HPA) Implementation

* HorizontalPodAutoscaler was added for multiple services (order, payment, restaurant, and customer)
* Configuration set with name "order-deployment-hpa" in default namespace
* Scaling parameters:
  - Minimum replicas: 2
  - Maximum replicas: 4
  - CPU utilization target: 85%

### Database Initialization Changes

* Disabled data source initialization for multiple instances
* Changed SPRING_DATASOURCE_INITIALIZATION-MODE to "never"
* This prevents multiple instances from trying to initialize the database simultaneously
* Environment variable added to all services to prevent startup error

Database initialization in Spring Boot is a process where the application sets up database schemas and loads initial data
By default, Spring Boot attempts to initialize the database when the application starts
This can cause issues in multi-instance deployments where multiple instances try to initialize the database simultaneously

1. Multiple Instance Problems:
  - When running multiple instances, concurrent initialization attempts can cause conflicts
  - Database locks can occur when multiple instances try to create schemas simultaneously
  - Data inconsistency can arise from parallel data loading

2. Production Considerations:
  - Database initialization should typically be done only once during initial deployment
  - Production environments should use proper database migration tools (like Flyway or Liquibase)
  - Schema and data should be managed through version-controlled migration scripts

### Deployment Process

* Updated application deployment files pushed to GitHub
* Existing deployments deleted (except Kafka and Postgres)
* New deployment applied with horizontal scaling configurations
* Verification showed multiple instances (2 pods per service)
* Single LoadBalancer IP maintained for each service with load distribution

### Key Technical Points

* Load balancing remains transparent to external clients
* Each service maintains a single external IP despite multiple instances
* Scaling based on CPU utilization (configurable to use other metrics)
* Proper cleanup process to avoid unnecessary cloud resource costs
