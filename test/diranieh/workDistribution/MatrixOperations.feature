Feature: MatrixOperations

  Scenario: Adding two n x n matrices
  Given Matrix A
    | 1  | 2  | 3  | 4  |
    | 5  | 6  | 7  | 8  |
    | 9  | 10 | 11 | 12 |
    | 13 | 14 | 15 | 16 |
  And Matrix B
    | 13 | 14 | 15 | 16 |
    | 9  | 10 | 11 | 12 |
    | 5  | 6  | 7  | 8  |
    | 1  | 2  | 3  | 4  |
  When I add A and B
  Then I get Matrix C
    | 14 | 16 | 18 | 20 |
    | 14 | 16 | 18 | 20 |
    | 14 | 16 | 18 | 20 |
    | 14 | 16 | 18 | 20 |

  Scenario: Multiplying two 2 x 2 matrices
    Given Matrix A
      | 1  | 2  |
      | 5  | 6  |
    And Matrix B
      | 5  | 6  |
      | 1  | 2  |
    When I multiply A and B
    Then I get Matrix C
      | 7  | 10  |
      | 31 | 42  |

  Scenario: Multiplying two 4 x 4 matrices
    Given Matrix A
    | 1  | 2  | 3  | 4  |
    | 5  | 6  | 7  | 8  |
    | 9  | 10 | 11 | 12 |
    | 13 | 14 | 15 | 16 |
    And Matrix B
    | 13 | 14 | 15 | 16 |
    | 9  | 10 | 11 | 12 |
    | 5  | 6  | 7  | 8  |
    | 1  | 2  | 3  | 4  |
    When I multiply A and B
    Then I get Matrix C
    | 50  | 60  | 70  | 80  |
    | 162 | 188 | 214 | 240 |
    | 274 | 316 | 358 | 400 |
    | 386 | 444 | 502 | 560 |
