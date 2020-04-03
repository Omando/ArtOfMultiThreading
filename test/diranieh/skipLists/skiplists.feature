Feature: skip lists

  Scenario Outline: skip list should add and remove
    Given a skip list
    When I add <addedNumbers>
    And I remove <removedNumbers>
    Then <remainingNumbers> should exist
    Examples:
    | addedNumbers          | removedNumbers   | remainingNumbers   |
    | ""                    | ""               | ""                 |
    | ""                    | "1,2,3,4,5,6"    | ""                 |
    | "1,2,3,4,5"           | ""               | "1,2,3,4,5"        |
    | "1,2,3,4,5,6,7"       | ""               | "1,2,3,4,5,6,7"    |
    | "1,1,1"               | ""               | "1"                |
    | "1,2,1,2,1,2,1,2,1,2" | ""               | "1,2"              |
    | "1,2,3,4,5,6,7"       | "1,2,3,4,5,6,7"  | ""                 |
    | "1,2,3"               | "1,2,3,4,5,6"    | ""                 |
    | "1,2,3,4,5,6,7,8"     | "1,3,5,7"        | "2,4,6,8"          |
    | "1,2,3,4,5,6,7"       | "1,3,5,7"        | "2,4,6"            |