Feature: Bounded concurrent priority queues
  Testing array-based and tree-based bounded concurrent priority queues

  @SingleThreaded
  Scenario Outline: PQ array-based and sequential access
    Given priority range is 5
    And priority queue implementation is <implementation>
    When I add the following items to the priority queue
      | item    | priority |
      | "Item3" | 3        |
      | "Item0" | 0        |
      | "Item4" | 4        |
      | "Item2" | 2        |
      | "Item1" | 1        |
    And I call removeMin 5 times
    Then I should get these items in this order
      | "Item0" |
      | "Item1" |
      | "Item2" |
      | "Item3" |
      | "Item4" |
    Examples:
    | implementation |
    | "ArrayBased"   |

  @SingleThreaded
  Scenario Outline: PQ tree-based and sequential access
    Given tree height is <treeheight>
    And priority queue implementation is <implementation>
    When I add the following items to the priority queue
      | item    | priority |
      | "Item3" | 3        |
      | "Item0" | 0        |
      | "Item4" | 4        |
      | "Item2" | 2        |
      | "Item7" | 7        |
      | "Item1" | 1        |
      | "Item6" | 6        |
      | "Item5" | 5        |

    And I call removeMin 8 times
    Then I should get these items in this order
      | "Item0" |
      | "Item1" |
      | "Item2" |
      | "Item3" |
      | "Item4" |
      | "Item5" |
      | "Item6" |
      | "Item7" |
    Examples:
      | implementation |treeheight|
      | "TreeBased"    |3         |


  Scenario Outline: PQ array-based and concurrent access
    Given priority range is 5
    And priority queue implementation is <implementation>
    And there are <threadCount> threads acting on the priority queue
    When each thread adds the following items to the priority queue
      | item    | priority |
      | "Item3" | 3        |
      | "Item0" | 0        |
      | "Item4" | 4        |
      | "Item2" | 2        |
      | "Item1" | 1        |
    And threads remove items until the priority queue is empty
    Then I should get these items in this order and each item repeated <threadCount> times
      | "Item0" |
      | "Item1" |
      | "Item2" |
      | "Item3" |
      | "Item4" |
    Examples:
      | implementation | threadCount|
      | "ArrayBased"   |5           |

  Scenario Outline: PQ tree-based and concurrent access
    Given tree height is <treeheight>
    And priority queue implementation is <implementation>
    And there are <threadCount> threads acting on the priority queue
    When each thread adds the following items to the priority queue
      | item    | priority |
      | "Item3" | 3        |
      | "Item0" | 0        |
      | "Item4" | 4        |
      | "Item2" | 2        |
      | "Item1" | 1        |
    And threads remove items until the priority queue is empty
    Then I should get these items in this order and each item repeated <threadCount> times
      | "Item0" |
      | "Item1" |
      | "Item2" |
      | "Item3" |
      | "Item4" |
    Examples:
      | implementation | threadCount|treeheight|
      | "TreeBased"    |5           |3         |
