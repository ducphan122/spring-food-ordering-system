

# Project Structure

- infrastructure: run docker compose to start kafka cluster, zookeeper and create topics
- common: common classes that can be used across different modules
- microservices: order, customer, restaurant, payment


- we use mvn clean install to build and install the jar file. You can also build each service separately by running mvn clean install at each service folder.
- use depgraph-maven-plugin to generate the dependency graph: mvn com.github.ferstl:depgraph-maven-plugin:aggregate -DcreateImage=true -DreduceEdges=false -Dscope=compile "-Dincludes=com.spring.food.ordering.system:*"
- For locally purpose. After running all docker compose. Open each service/container folder -> run command: mvn spring-boot:run (comment the build steps in pom.xml for faster build time)

## Installation
1. Run all command in infrastructure readme (alternatively, run start-up.sh and shutdown.sh to start and stop the services)
  - Make sure ncat is available. https://serverspace.io/support/help/how-to-install-ncat-tool-on_windows-and-linux/ 
  - kcat we use via docker
2. mvn clean install at root folder to build all services
3. Start 4 services. At service root folder, run command: mvn -pl service-container spring-boot:run
4. Make sure 4 debezium connectors are created, using POSTMAN -> GET 'localhost:8083/connectors'


## Endpoints

### POST /customers
- http://localhost:8184/customers
- We include customerId in the request body because other pre-populated sql data in other services expected this id
{
  "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
  "username": "user_1",
  "firstName": "First",
  "lastName": "User"
}

### POST /orders
- http://localhost:8181/orders
{
  "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
  "restaurantId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45",
  "address": {
    "street": "street_1",
    "postalCode": "1000AB",
    "city": "Amsterdam"
  },
  "price": 200.00,
  "items": [
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",
      "quantity": 1,
      "price": 50.00,
      "subTotal": 50.00
    },
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",
      "quantity": 3,
      "price": 50.00,
      "subTotal": 150.00
    }
  ]
}
- This will return the orderId, status and trackingId
- Use the command to read message from kafka: docker run -it --network=host edenhill/kcat:1.7.1 -b localhost:19092 -C -t payment-request
- Alternatively, use kafka-ui at http://localhost:9000/.

### GET /orders/{trackingId}
- Get the order status. Happy flow will be from state PENDING -> PAID -> APPROVED

### POST /payments/top-up
- http://localhost:8182/payments/top-up
{
  "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
  "amount": 1000.00
}
- this will add 1000.00 to the customer's credit entry and update credit history with type "CREDIT"

### GET /connectors
- http://localhost:8083/connectors
- This will return the list of debezium connectors

**Flows**
- Client sends Order JSON via POST to Order Controller.
- Order Service validates and saves order in the database.
- Order Service publishes Payment Request to Kafka (payment-request topic).
- Payment Service consumes Payment Request, processes payment, and publishes Payment Completed to Kafka (payment-response topic).
- Order Service consumes Payment Completed, updates order status to "Paid", and publishes Order Paid to Kafka (restaurant-approval-request topic).
- Restaurant Service consumes Order Paid, approves the order, and publishes Order Approved to Kafka (restaurant-approval-response topic).
- Order Service consumes Order Approved and updates the order status to "Approved".
- Client can query the order status through the Tracking Endpoint.

**Ordering unavailable product**
- Client Initiates Order: Sends Order JSON via POST to Order Controller.
Order Service Validates Order: Validates order details and saves it with status "Pending".
- Publish Payment Request: Order Service publishes Payment Request to payment-request Kafka topic.
- Payment Service Processes Payment: Consumes Payment Request, processes payment, and publishes Payment - Completed to payment-response topic.
- Update Order to "Paid": Order Service consumes Payment Completed, updates order status to "Paid", and publishes Order Paid to restaurant-approval-request topic.
- Restaurant Service Attempts Approval: Consumes Order Paid, detects product is unavailable, rejects order, and publishes Order Rejected to restaurant-approval-response topic.
- Handle Order Rejection: Order Service consumes Order Rejected, updates status to "Canceling", and publishes Order Canceled to payment-request topic.
- Rollback Payment: Payment Service consumes Order Canceled, rolls back payment, and publishes Payment Canceled to payment-response topic.
- Finalize Order Cancellation: Order Service consumes Payment Canceled and updates order status to "Canceled".
- Client Queries Status: Client checks order status via Tracking Endpoint and sees "Canceled" with failure message.

**Insufficient Credit**
- Client Initiates Order: Sends Order JSON via POST to Order Controller with total amount exceeding available credit.
- Order Service Validates Order: Validates order details and saves it with status "Pending".
- Publish Payment Request: Order Service publishes Payment Request to payment-request Kafka topic.
- Payment Service Attempts Payment: Consumes Payment Request, detects insufficient credits, and publishes Payment Failed to payment-response topic.
- Handle Payment Failure: Order Service consumes Payment Failed, updates order status to "Canceled", and logs failure reason.
- Client Queries Status: Client checks order status via Tracking Endpoint and sees "Canceled" with insufficient credit message.


### POST /payments/top-up
- make a POST request to http://localhost:8282/payments/top-up, with body
{
  "customerId": "d215b5f8-0249-4dc5-89a3-51fd148cfb41",
  "amount": 100.00
}
This should add 100.00 to the customer's credit entry and update credit history with type "CREDIT"








