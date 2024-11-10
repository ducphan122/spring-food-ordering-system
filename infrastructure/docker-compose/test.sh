#!/bin/bash

# Get the list of connectors from the API
CONNECTORS=$(curl -s http://localhost:8083/connectors)

# Expected connectors
EXPECTED_CONNECTORS=(
    "order-restaurant-connector"
    "payment-order-connector"
    "order-payment-connector"
    "restaurant-order-connector"
)

# Remove brackets and quotes from the response and convert to array
ACTUAL_CONNECTORS=$(echo $CONNECTORS | tr -d '[]"' | tr ',' ' ')

# Flag to track if all connectors are found
ALL_FOUND=true

# Check each expected connector
for connector in "${EXPECTED_CONNECTORS[@]}"; do
    if echo "$ACTUAL_CONNECTORS" | grep -q "$connector"; then
        echo "✅ Found connector: $connector"
    else
        echo "❌ Missing connector: $connector"
        ALL_FOUND=false
    fi
done

# Report status without exiting
if [ "$ALL_FOUND" = true ]; then
    echo "All connectors are present!"
else
    echo "Some connectors are missing!"
fi

read -p "Press [Enter] to close the window..."
# TODO: Do something else