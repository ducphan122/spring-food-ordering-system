# payment-domain

## payment-application-service

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

 