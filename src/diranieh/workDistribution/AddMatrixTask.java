package diranieh.workDistribution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AddMatrixTask implements  Runnable {
    private final Matrix _a;
    private final Matrix _b;
    private final Matrix _c;
    private final ExecutorService _executorService;

    public  AddMatrixTask(Matrix a, Matrix b, Matrix c, ExecutorService executorService) {
        _a = a;
        _b = b;
        _c = c;
        _executorService = executorService;
    }

    @Override
    public void run() {
        try {
            if (_a.getDimension() == 1) {
                double sum = _a.get(0,0) + _b.get(0,0);
                _c.set(0, 0, sum);
            } else {
                // Split input matrices into 4 n/2 x n/2
                Matrix[][] splitA = _a.split();
                Matrix[][] splitB = _b.split();
                Matrix[][] splitC = _c.split();
                Future<?>[][] future = (Future<?>[][])new Future[2][2];

                // Submit the split matrices into the thread pool
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        var task = new AddMatrixTask(splitA[i][j], splitB[i][j], splitC[i][j], _executorService);
                        future[i][j] = _executorService.submit(task);
                    }
                }

                // Wait until all sub-computations have finished
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        future[i][j].get();
                    }
                }
            }
        }
        catch (Exception ex) {
            System.out.println(ex   );
        }
    }
}
