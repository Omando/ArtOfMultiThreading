Feature: Shared Counter
    Testing shared counter with sequential and concurrent access

  # getAndIncrement increments and returns prior count
  Scenario: sequential access
    getAndIncrement gets the count then increments, so the final count is 1
    less than the current count
    Given Single threaded
    When I increment counter 7 times
    Then count will be 6

  Scenario Outline: concurrent access
    getAndIncrement gets the count then increments, so the final count is 1
    less than the current count
    Given There are <threadCount> threads
    And Each thread counts <incrementCount> times
    When multiple threads count
    Then final count be <finalCount>
    Examples:`
    |threadCount|incrementCount|finalCount |
    |2          |2             |3          |
    |4          |2             |7          |
    |8          |8             |63         |
    |7          |5             |34         |



