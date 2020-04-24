package diranieh.workDistribution.matrixOperations;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* All matrices are assumed to be n x n */
public class MatrixTask implements IMatrixTask {
    // Creates a thread pool that creates new threads as needed, but will reuse
    // previously constructed threads when they are available.
    private static final ExecutorService _executor = Executors.newCachedThreadPool();

    @Override
    public Matrix add(Matrix a, Matrix b) throws ExecutionException, InterruptedException {

        // Create the result matrix with the same dimension
        int n = a.getDimension();
        Matrix result =  new Matrix(n);

        // Add a and b matrices concurrently
        Future<?> future = _executor.submit( new AddMatrixTask(a, b, result, _executor));
        future.get();
        return result;
    }

    @Override
    public Matrix multiply(Matrix a, Matrix b) throws ExecutionException, InterruptedException {
        // Create the result matrix with the same dimension
        int n = a.getDimension();
        Matrix result =  new Matrix(n);

        // Multiply matrices a and b concurrently
        Future<?> future = _executor.submit( new MultiplyMatrixTask(a, b, result, _executor));
        future.get();
        return result;
    }
}
