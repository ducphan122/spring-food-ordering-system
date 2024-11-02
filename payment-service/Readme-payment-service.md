# payment-domain

## payment-domain-core

### Entity

- Payment (AggregateRoot)
- CreditEntry (BaseEntity)
- CreditHistory (BaseEntity)
We can make CreditEntry and CreditHistory as separate AggregateRoots, here we just use BaseEntity because they dont have any complex logic or Entities that need in a consistent state

Another approach:
- We can include List of CreditEntry and CreditHistory inside Payment AggregateRoot. 
- Why List or Multiple Credit Transactions per Payment: If a single payment can involve multiple credit transactions, such as partial credits, refunds, or adjustments, maintaining a list of CreditEntry instances allows you to track each individual transaction separately.
Audit and History Tracking: For detailed auditing purposes, having multiple entries can provide a clearer history of all credit-related actions associated with a payment.

### Domain Service

- PaymentDomainServiceImpl
- The validation performs two important checks:
  - Balance Check: Ensures that the total debits (money spent) never exceeds total credits (money received). In other words, a customer can't spend more than they have.
  - Consistency Check: Verifies that the current balance (creditEntry.getTotalCreditAmount()) equals the historical calculation of (total credits - total debits). This ensures the system's current balance matches what it should be based on transaction history.
Example:
If a customer deposits $100 → CREDIT transaction of +$100
If they make a payment of $30 → DEBIT transaction of -$30
Their balance should be $70, and the system validates this by:
Total Credits ($100) - Total Debits ($30) = Current Balance ($70)

**Validate and Initiate Payment**
- Validate payment details
- Initialize payment state
- Check if customer has enough credit
- Deduct payment amount from customer's credit
- Record DEBIT transaction in history
- Validate transaction history consistency by
  - Checking total debits don't exceed total credits
  - Verifying current balance matches historical calculations
- If validations pass:
  - Updates payment status to COMPLETED
  - Creates PaymentCompletedEvent
- If validations fail:
  - Updates payment status to FAILED
  - Creates PaymentFailedEvent

**Validate and Cancel Payment**
- Validate payment details
- Add payment amount back to customer's credit (refund)
- Record CREDIT transaction in history
- Return CANCELLED or FAILED payment event based on validation results


## payment-application-service

- In this layer, we implement `PaymentRequestMessageListenerImpl`, which is interface defined in ports/input/message/listener/PaymentRequestMessageListener.
- Compare with order-service, order-service has 2 layer for ports/input, which is ports/input/service and ports/input/message.listener. Because order-service is microservice that is entrypoint for client(rest-api), thats why it has input/service. For this payment-service, it is not entrypoint for client, it is just a microservice that is triggered by order-service or other services. Thats why it has only input/message.listener.

**PaymentRequestMessageListenerImpl**
1. persistPayment:
- Initial Request Processing: Receives a PaymentRequest, Maps PaymentRequest DTO to Payment domain entity
- Fetch Required Data: Gets CreditEntry, CreditHistory list for the customer
- Domain Service -> Validate and Initiate Payment
- Persist Changes: Saves Payment entity (with paymentStatus already set via domain service methods)
- If validation success: save updated CreditEntry (new balance) and only the last record of CreditHistory (the DEBIT transaction) that was added via domain service methods
- Return Result: Returns PaymentEvent (either Completed or Failed)
The key point is that this follows a typical domain-driven design pattern where:
- Application layer (`PaymentRequestHelper`) coordinates the overall flow
- Domain layer (`PaymentDomainService`) contains all business logic and validation rules
- Persistence only happens after all domain rules are satisfied
- The entire operation is transactional (annotated with `@Transactional`)
2. Fire events
- Based on the paymentEvent returned from domain service (via persistPayment method), we fire the corresponding event to message broker.

### Refactoring fireEvent with help of Object Oriented Programming

- Currently, in PaymentRequestMessageListenerImpl, we use if-else statement to check the type of paymentEvent and then use the correct publisher defined in ports/output/message/publisher to fire the corresponding event to message broker. This approach is not optimal,because the more events we have the if else block become larger -> we should use Object Oriented Programming to improve this.
- Idea, we want the event returned from domain service can has the fire method and inside that method, it will call the correct publisher (its Representative) to fire the event to message broker. However,
**Problem:**
- the event itself is from domain layer, how can it have the publisher Representative, because publisher is from outside (as interface in ports/output/message/publisher and as implementation in messaging module)
- Futhermore, application-service has dependency on domain core, now if we add publisher to domain core event, the domain core will have dependency on application-service -> circular dependency. And also, domain core should not depends on anything outside as much as possible.

**Solution:**
- we can use construct injection to inject the publisher to the event when we create the event. Then in the event, it has the fire method to call the publisher to fire the current event object to message broker. See PaymentCompletedEvent, PaymentCancelledEvent, PaymentFailedEvent in domain core.
- Now comes to part, where do we get the publisher. Remember, domain events are returned from domain service methods, so we can add the publisher to domain service methods as parameters. And who calls the domain service methods? -> Application service, it has domain service Bean and call domain service methods via this bean. In this layer, we also have dependency on kafka publisher, so we can inject the publisher to application service, and then pass it to domain service methods as parameters. See PaymentRequestHelper in application service.
- Then in PaymentRequestMessageListenerImpl, we call the persistPayment method in PaymentRequestHelper, and it will return the PaymentEvent, and then we use that event and call fire method in the event to fire the event to message broker.