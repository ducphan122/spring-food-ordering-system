## Restaurant-domain
- Logical flow when `approveOrder` is called in `RestaurantApprovalRequestMessageListenerImpl` in _restaurant-application-service_

1. `RestaurantApprovalRequestMessageListenerImpl.approveOrder()` is called with a `RestaurantApprovalRequest`
   - Delegates to `RestaurantApprovalRequestHelper.persistOrderApproval()`
   - Fires the resulting event

2. `RestaurantApprovalRequestHelper.persistOrderApproval()`:
   - Logs processing message
   - Creates empty failure messages list
   - Calls `findRestaurant()` to get restaurant entity
   - Calls `RestaurantDomainService.validateOrder()`
   - Saves order approval to repository
   - Returns the approval event

3. `RestaurantApprovalRequestHelper.findRestaurant()`:
   - Maps approval request to Restaurant entity using `RestaurantDataMapper`
   - Queries repository for restaurant information
   - Throws `RestaurantNotFoundException` if not found
   - Updates restaurant entity with:
     - Active status
     - Product details (name, price, availability)
     - Order ID
   - Returns updated restaurant entity

4. `RestaurantDomainService.validateOrder()`:
   - Calls `Restaurant.validateOrder()` with failure messages list
   - Logs validation message
   - If no failures:
     - Constructs APPROVED order approval
     - Creates `OrderApprovedEvent`
   - If failures exist:
     - Constructs REJECTED order approval  
     - Creates `OrderRejectedEvent`

5. `Restaurant.validateOrder()`:
   - Validates order payment status
   - Validates product availability
   - Validates total amount matches products
   - Adds any validation failures to messages list

6. Back in `approveOrder()`, the event's `fire()` method is called:
   - For `OrderApprovedEvent`: Publishes via `OrderApprovedMessagePublisher`
   - For `OrderRejectedEvent`: Publishes via `OrderRejectedMessagePublisher`
