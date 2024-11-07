## What are we testing?
- We are mainly testing the concurrent and optimistic locking mechanism of the Order Payment Saga.
- In this integration test, we will create simple sql scripts to set up and clean up the test data.

## Optimistic Locking Test
In a database, optimistic locking is a strategy used to ensure data consistency when multiple transactions are trying to update the same data concurrently. It works by checking if the data has been modified by another transaction before committing the changes. This is typically implemented using a version number or timestamp.

In the SQL schema, the `payment_outbox` and `restaurant_approval_outbox` tables have a `version` column, which is likely used for optimistic locking. The idea is that when a transaction reads a row, it also reads the version number. When it tries to update the row, it checks if the version number is the same as when it was read. If the version number has changed, it means another transaction has modified the row, and the current transaction should be aborted or retried.

We might need to comment out the unique index on `saga_id` and `saga_status` because these indexes enforce uniqueness constraints that can interfere with the optimistic locking mechanism. If the unique index is in place, it might prevent certain updates that are necessary for testing or observing the behavior of optimistic locking, especially if the updates involve changing the `saga_status` or `saga_id` in a way that would violate the uniqueness constraint.

By commenting out the unique index, we allow the `version` column to be the primary mechanism for detecting concurrent updates, which is the essence of optimistic locking. This setup allows us to observe how optimistic locking handles concurrent transactions without interference from other constraints.

## Threads with CountDownLatch
Instead of using `thread.join()` to wait for threads to complete, we can use a `CountDownLatch` to synchronize multiple threads. 

- **CountDownLatch**: Initialize it with a count equal to the number of threads. Each thread calls `latch.countDown()` when it finishes its task.
- **Main Thread**: Calls `latch.await()` to wait until the latch count reaches zero, ensuring all threads have completed before proceeding.

This approach provides a more flexible and robust way to handle concurrent execution, especially when we need to ensure all threads complete their tasks, even if exceptions occur.

## How to run the test?
- Run with a specific test: go to order-container -> mvn test -Dtest=OrderPaymentSagaTest#testDoublePaymentWithThreads
- Run with all tests: go to order-container -> mvn test