Feature: Unbounded concurrent priority queues
  Testing unbounded concurrent priority queues

  Scenario Outline: PQ unbounded sequential
    Given priority capacity is <capacity>
    And priority queue implementation is <implementation>
    When I add the following items to the priority queue
      | item    | priority |
      | "Item3" | 3        |
      | "Item0" | 0        |
      | "Item4" | 4        |
      | "Item2" | 2        |
      | "Item6" | 6        |
      | "Item7" | 7        |
      | "Item1" | 1        |
      | "Item11"| 1        |
      | "Item5" | 5        |
    And I call removeMin 9 times
    Then I should get these items in this order
      | "Item0"  |
      | "Item1"  |
      | "Item11" |
      | "Item2"  |
      | "Item3"  |
      | "Item4"  |
      | "Item5"  |
      | "Item6"  |
      | "Item7"  |
    Examples:
      | implementation |capacity|
      | "Sequential"   |5       |
