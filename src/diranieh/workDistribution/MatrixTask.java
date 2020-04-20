package diranieh.workDistribution;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* All matrices are assumed to be n x n */
public class MatrixTask implements IMatrixTask {
    private static final ExecutorService _executor = Executors.newFixedThreadPool(5);

    @Override
    public Matrix add(Matrix a, Matrix b) throws ExecutionException, InterruptedException {

        // Create the result matrix with the same dimension
        int n = a.getDimension();
        Matrix result =  new Matrix(n);

        // Add a and b matrices concurrently
        Future<?> future = _executor.submit( new AddMatrixTask(a, b, result));
        future.get();
        return result;
    }

    @Override
    public Matrix multiply(Matrix a, Matrix b) throws ExecutionException, InterruptedException {
        // Create the result matrix with the same dimension
        int n = a.getDimension();
        Matrix result =  new Matrix(n);

        // Multiply matrices a and b concurrently
        Future<?> future = _executor.submit( new MultiplyMatrixTask(a, b, result));
        future.get();
        return result;
    }
}
