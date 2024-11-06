This is a test class for `OrderApplicationService` which handles order creation in your food ordering system. Here's what it tests:

1. **Happy Path**: `testCreateOrder()` - Tests successful order creation
2. **Validation Cases**:
   - `testCreateOrderWithWrongTotalPrice()` - Ensures orders with mismatched total prices are rejected
   - `testCreateOrderWithWrongProductPrice()` - Verifies product price validation
   - `testCreateOrderWithPassiveRestaurant()` - Checks that orders can't be created for inactive restaurants

Key Points:
1. Use `@BeforeEach` to set up default happy-path mocks
2. Override specific mocks in individual test methods for error scenarios
3. Verify both successful and failed interactions
4. Mock all external dependencies (repositories, publishers)
5. Don't mock the actual domain service (`OrderDomainService`) as it contains the business logic 

- Test business logic in isolation
- Verify correct interaction with external dependencies
- Test both success and failure scenarios
- Maintain readable and maintainable tests