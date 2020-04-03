Feature: Open Address Concurrent hash set

  Scenario Outline: hashset should add and remove
    Given capacity is <capacity>
    And open address implementation is <implementation>
    When I add <addedNumbers>
    And I remove <removedNumbers>
    Then <remainingNumbers> should exist
    Examples:
| capacity | implementation   | addedNumbers          | removedNumbers   | remainingNumbers   |
| 3        | "ThreadUnSafe"   | ""                    | ""               | ""                 |
| 3        | "ThreadUnSafe"   | ""                    | "1,2,3,4,5,6"    | ""                 |
| 3        | "ThreadUnSafe"   | "1,2,3,4,5"           | ""               | "1,2,3,4,5"        |
| 3        | "ThreadUnSafe"   | "1,2,3,4,5,6,7"       | ""               | "1,2,3,4,5,6,7"    |
| 3        | "ThreadUnSafe"   | "1,1,1"               | ""               | "1"                |
| 3        | "ThreadUnSafe"   | "1,2,1,2,1,2,1,2,1,2" | ""               | "1,2"              |
| 3        | "ThreadUnSafe"   | "1,2,3,4,5,6,7"       | "1,2,3,4,5,6,7"  | ""                 |
| 3        | "ThreadUnSafe"   | "1,2,3"               | "1,2,3,4,5,6"    | ""                 |
| 3        | "ThreadUnSafe"   | "1,2,3,4,5,6,7,8"     | "1,3,5,7"        | "2,4,6,8"          |
| 3        | "ThreadUnSafe"   | "1,2,3,4,5,6,7"       | "1,3,5,7"        | "2,4,6"            |

  Scenario Outline: hashset should add and remove large numbers
    # This scenario concentrates on resizing
    Given capacity is <capacity>
    And open address implementation is <implementation>
    When I add <addCount> numbers to the list
    And I remove the same numbers
    Then hash set should be empty
    Examples:
| capacity | implementation   | addCount |
| 10       | "ThreadUnSafe"   | 1000     |
| 100      | "ThreadUnSafe"   | 1000     |
| 100      | "ThreadUnSafe"   | 1000000  |
| 100      | "ThreadUnSafe"   | 10000000 |

  Scenario Outline: cuckoo hashset concurrent access
    Given capacity is 10
    And open address implementation is <implementation>
    And I create <threadCount> threads
    And Each of my threads adds <itemsPerThread> new items
    When all my threads add items
    Then all items from all threads are added
    And total hashset item count is <totalItemCount>
    Examples:
  | threadCount | itemsPerThread | totalItemCount | implementation   |
  | 1           | 3              | 3              | "Coarse"         |
  | 2           | 2              | 4              | "Coarse"         |
  | 4           | 2              | 8              | "Coarse"         |
  | 8           | 8              | 64             | "Coarse"         |
  | 10          | 100            | 1000           | "Coarse"         |
  | 50          | 100            | 5000           | "Coarse"         |

