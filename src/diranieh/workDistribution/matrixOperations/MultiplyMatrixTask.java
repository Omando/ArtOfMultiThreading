package diranieh.workDistribution.matrixOperations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Given matrices A and B (both n x n and n is a power of 2), decompose A and B each
 * into four submatrices A00, A01, A10, & A11 and B00, B01, B10, and B11 and perform
 * these matrix multiplications in parallel:
 *  A00 . B00
 *  A01 . B10
 *  A00 . B01
 *  A01 . B11
 *  A10 . B00
 *  A11 . B10
 *  A10 . B01
 *  A11 . B11
 * When the multiplications are done, perform the following four sums in parallel
 *  A00 . B00 + A01 + B10
 *  A00 . B01 + A01 . B11
 *  A10 . B00 + A11 . B10
 *  A10 . B01 + A11 . B11
 */
public class MultiplyMatrixTask implements  Runnable {
    private final Matrix _a;
    private final Matrix _b;
    private final Matrix _c;
    private final ExecutorService _executorService;
    private final Matrix _lhs, _rhs;      // scratch matrices used for intermediate  result

    public MultiplyMatrixTask(Matrix a, Matrix b, Matrix c, ExecutorService executorService) {
        _a = a;
        _b = b;
        _c = c;
        _executorService = executorService;

        // These two matrices are used to hold temp result while calculating the
        // multiplication result. In the document on page 45, _lhs and _rhs are
        // represented by the blue and red matrices
        _lhs = new Matrix(_a.getDimension());
        _rhs = new Matrix(_a.getDimension());
    }

    @Override
    public void run() {
        try {
            if (_a.getDimension() == 1) {
                double sum = _a.get(0,0) * _b.get(0,0);
                _c.set(0, 0, sum);
            } else {
                // Split input matrices into 4 n/2 x n/2
                Matrix[][] splitA = _a.split();
                Matrix[][] splitB = _b.split();
                Matrix[][] splitLhs = _lhs.split();
                Matrix[][] splitRhs = _rhs.split();
                Future<?>[][] lhsFuture = (Future<?>[][])new Future[2][2];
                Future<?>[][] rhsFuture = (Future<?>[][])new Future[2][2];

                // Submit the split matrices into the thread pool
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        var lshTask = new MultiplyMatrixTask(splitA[i][0], splitB[0][j], splitLhs[i][j], _executorService);
                        lhsFuture[i][j] = _executorService.submit(lshTask);

                        var rhsTask = new MultiplyMatrixTask(splitA[i][1], splitB[1][j], splitRhs[i][j], _executorService);
                        rhsFuture[i][j] = _executorService.submit(rhsTask);
                    }
                }

                // Wait until all sub-computations have finished
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        lhsFuture[i][j].get();
                        rhsFuture[i][j].get();
                    }
                }

                // Once the left- and right-hand sides have been computed, we can simply add them
                var addTask = new AddMatrixTask(_lhs, _rhs, _c, _executorService);
                Future<?> futureAdd = _executorService.submit(addTask);
                Object v = futureAdd.get();
            }
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
