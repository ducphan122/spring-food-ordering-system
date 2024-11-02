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
