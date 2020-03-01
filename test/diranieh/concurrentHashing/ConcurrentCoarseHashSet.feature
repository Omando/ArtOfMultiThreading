Feature: Concurrent hash set

  Scenario: hash set should add and remove
    Given capacity is 5 and bucket threshold is 2
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

  Scenario: hash set should increase capacity
    Given capacity is 5 and bucket threshold is 2
    When user adds 15 numbers
    Then capacity should increase to 10

    # Todo: add scenario for concurrent access