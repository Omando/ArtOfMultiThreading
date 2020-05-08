Feature: Spinning lock implementations

  Scenario Outline: locking
    Given <threadCount> threads are running
    And locking implementation is <implementation>
    And Each running thread increments a shared counter <incrementCount>
    When multiple threads increment the counter
    Then Final count is <finalCount>
    Examples:
      | implementation   | threadCount | incrementCount | finalCount |
      | "ALock"          | 1           | 3              | 3          |
      | "ALock"          | 10          | 10             | 100        |
      | "BackoffLock"    | 1           | 3              | 3          |
      | "BackoffLock"    | 10          | 2000           | 20000      |
      | "CLHLock"        | 1           | 3              | 3          |
      | "CLHLock"        | 10          | 1000          | 10000      |
      | "MCSLock"        | 1           | 3              | 3          |
      | "MCSLock"        | 5           | 1000           | 5000       |
