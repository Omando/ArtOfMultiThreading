package diranieh.workDistribution.matrixOperations;

/* Internal representation of a matrix. Assumes the matrix is n x n where
 n is a power of 2 */
public class Matrix {
    private final int _n;           //
    private final double[][] _matrix;
    private int _rowOffset;
    private int _colOffset;

    // Use this constructor to create an uninitialized n x n matrix
    public Matrix(int dimension) {
        _n =  dimension;
        _matrix = new double[_n][_n];
    }

    // Use this constructor when splitting a matrix
    public Matrix(int dimension, double[][] matrix, int rowOffset, int colOffset) {
        _n = dimension;
        _matrix = matrix;
        _rowOffset = rowOffset;
        _colOffset = colOffset;
    }

    public int getDimension() {
        return _n;
    }

    // Get and set particular element in a matrix
    public double get(int row, int col) {
        // Take into account any requires displacement
        return _matrix[row + _rowOffset][col + _colOffset];
    }

    public void set(int row, int col, double value) {
        // Take into account any requires displacement
        _matrix[row + _rowOffset][col + _colOffset] = value;
    }

    /* Splits the underlying matrix (_matrix) field into 4 matrices, each  is n/2 x n/2
     One approach to splitting a matrix would be to **copy** values from the matrix into
     the sub-matrices. With this approach, changes to the original matrix are not reflected
     into the sub-matrices, and vice versa.
     The other approach (used below) is to use the underlying matrix as a *backing field*.
     This means that each sub-matrix is a view into the original matrix. To accomplish
     this we use _rowDisplacement and _colDisplacement fields to correctly position each
     sub-matrix over the original matrix. With this approach, any changes to the original
     matrix are directly reflected in the sub-matrices, and vice versa */
    public Matrix[][] split() {
        // Each split matrix has a dimension n = _n/2
        int n = _n / 2;      // recall _n is a power of 2

        // Create an array of 2 x 2  matrices
        Matrix[][] subMatrices = new Matrix[2][2];

        // Initialize each of the 4 matrices (2 rows and 2 cols).
        subMatrices[0][0] = new Matrix(n, _matrix, _rowOffset, _colOffset);
        subMatrices[0][1] = new Matrix(n, _matrix, _rowOffset, _colOffset + n);
        subMatrices[1][0] = new Matrix(n, _matrix, _rowOffset + n, _colOffset);
        subMatrices[1][1] = new Matrix(n, _matrix, _rowOffset + n, _colOffset + n);

        return subMatrices;
    }
}

