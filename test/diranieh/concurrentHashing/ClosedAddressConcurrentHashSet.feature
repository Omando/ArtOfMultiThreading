Feature: Closed Address Concurrent hash set

  # bucket threshold is the average number of items in each bucket
  Scenario Outline: hashset should add and remove
  Given capacity is 5 and bucket threshold is 2
  And implementation is <implementation>
  When I add the following items
      |1|
      |2|
      |3|
      |4|
      |5|
    And I remove the following items
      |2|
      |3|
      |4|
    Then only these items should exist
      |1|
      |5|
    Examples:
    |implementation  |
    |"Coarse"        |
    |"Striped"       |
    |"Refined"       |

    # bucket threshold is the average number of items in each bucket
  Scenario Outline: hashset should increase capacity
    Given capacity is 5 and bucket threshold is 2
    And implementation is <implementation>
    When I add 15 numbers
    Then capacity should increase to 10
    Examples:
      |implementation   |
      |"Coarse"         |
      |"Striped"        |
      |"Refined"        |

    # bucket threshold is the average number of items in each bucket
  Scenario Outline: hashset concurrent access
    Given capacity is 5 and bucket threshold is 10
    And implementation is <implementation>
    And There are <threadCount> threads
    And Each thread adds <itemCount> new items
    When multiple threads add
    Then total item count is <totalItemCount>
    And all items are added from all threads
    Examples:
      | threadCount | itemCount | totalItemCount |implementation   |
      |1            |3          |3               |"Coarse"         |
      |2            |2          |4               |"Coarse"         |
      |4            |2          |8               |"Coarse"         |
      |8            |8          |64              |"Coarse"         |
      |10           |10         |100             |"Coarse"         |
      |1            |3          |3               |"Striped"        |
      |2            |2          |4               |"Striped"        |
      |4            |2          |8               |"Striped"        |
      |8            |8          |64              |"Striped"        |
      |10           |10         |100             |"Striped"        |
      |1            |3          |3               |"Refined"        |
      |2            |2          |4               |"Refined"        |
      |4            |2          |8               |"Refined"        |
      |8            |8          |64              |"Refined"        |
      |10           |10         |100             |"Refined"        |


    # Add other scenarios for removing ...