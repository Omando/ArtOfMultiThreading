Feature: skip lists

  Scenario Outline: skip list should add and remove
    Given a skip list
    When I add <addedNumbers>
    And I remove <removedNumbers>
    Then <remainingNumbers> should exist
    Examples:
    | addedNumbers    | removedNumbers   | remainingNumbers   |
    | "1,2,3,4,5,6,7" | ""               | "1,2,3,4,5,6,7"    |

