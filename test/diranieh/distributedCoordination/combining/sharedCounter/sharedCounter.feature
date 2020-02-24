Feature: Shared Counter
    Testing shared counter with sequential and concurrent access

  Scenario: sequential access
    Given Tree size is 5
    When I increment counter 7 times
    Then count will be 7

#  Scenario Outline: concurrent access
#    Given There are <threadCount> threads
#    And Each thread counts <incrementCount> times
#    And tree size is <size>
#    When multiple threads count
#    Then final count be <finalCount>
#    Examples:
#    |size|threadCount|incrementCount|finalCount|
#    |4   |2          |2             |4         |
#    |8   |2          |2             |4         |
#    |8   |4          |4             |16        |

