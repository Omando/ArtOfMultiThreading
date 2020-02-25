Feature: Shared Counter
    Testing shared counter with sequential and concurrent access

  # getAndIncrement increments and returns prior count
  Scenario: sequential access
    Given There is 1 thread
    When I increment counter 7 times
    Then count will be 6

  Scenario Outline: concurrent access
    Given There are <threadCount> threads
    And Each thread counts <incrementCount> times
    When multiple threads count
    Then final count be <finalCount>
    Examples:
    |size|threadCount|incrementCount|finalCount|
    |4   |2          |2             |3         |
    |8   |2          |2             |3         |
    |8   |4          |4             |15        |

