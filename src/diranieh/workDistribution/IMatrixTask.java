package diranieh.workDistribution;

import java.util.concurrent.ExecutionException;

public interface IMatrixTask {
    Matrix add(Matrix a, Matrix b) throws ExecutionException, InterruptedException;
    Matrix multiply(Matrix a, Matrix b) throws ExecutionException, InterruptedException;
}
