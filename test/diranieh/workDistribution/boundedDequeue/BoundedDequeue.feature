Feature:BoundedDequeue

  Scenario: Popping bottom item from an empty dequeue throws an exception
    When popBottom is called on an empty dequeue
    Then An exception is thrown

  Scenario: Popping top item from an empty dequeue throws an exception
    When popTop is called on an empty dequeue
    Then An exception is thrown

  Scenario: Pushing beyond capacity throws an exception
    Given bounded queue with capacity 5
    When more items than capacity are pushed
    Then An exception is thrown

  Scenario: Pushing and popping equal items from top leaves an empty dequeue
    Given bounded queue with capacity 5
    When 4 are items are pushed
    And 4 items are popped from the top
    Then bounded queue is empty
    And popped items are same as pushed items

  Scenario: Pushing and popping equal items from bottom leaves an empty dequeue
    Given bounded queue with capacity 5
    When 4 are items are pushed
    And 4 items are popped from the bottom
    Then bounded queue is empty
    And popped items are same as pushed items

  Scenario: Pushing and popping equal items alternatively from bottom leaves an empty dequeue
    Given bounded queue with capacity 5
    When 10 items are pushed and popped alternatively from bottom
    Then bounded queue is empty
    And popped items are same as pushed items

  Scenario: Pushing from bottom and popping equal number from top should reset indexes
    Given bounded queue with capacity 5
    When 4 are items are pushed
    And 4 items are popped from the top
    Then bounded queue is empty
    And popped items are same as pushed items
    And indexes are reset