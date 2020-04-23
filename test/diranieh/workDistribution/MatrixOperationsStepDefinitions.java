package diranieh.workDistribution;


import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.List;

public class MatrixOperationsStepDefinitions implements En {
    private Matrix a, b, c;
    public MatrixOperationsStepDefinitions() {
        Given("Matrix A", (DataTable rawData) -> {
            List<List<Double>> numbers = rawData.asLists(Double.class);
            a = new Matrix(numbers.size());

            int n = numbers.size();
            for (int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++)
                a.set(i, j, numbers.get(i).get(j));
            }
        });
        And("Matrix B",  (DataTable rawData) -> {
            List<List<Double>> numbers = rawData.asLists(Double.class);
            b = new Matrix(numbers.size());

            int n = numbers.size();
            for (int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++)
                    b.set(i, j, numbers.get(i).get(j));
            }
        });

        When("I add A and B", () -> {
            IMatrixTask matrixTask = new MatrixTask();
            c = matrixTask.add(a, b);
        });
        When("I multiply A and B", () -> {
            IMatrixTask matrixTask = new MatrixTask();
            c = matrixTask.multiply(a, b);
        });
        Then("I get Matrix C",  (DataTable rawData) -> {
            List<List<Double>> numbers = rawData.asLists(Double.class);

            int n = numbers.size();
            for (int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++) {
                    double expectedValue = numbers.get(i).get(j);
                    double calculatedValye = c.get(i, j);
                    Assert.assertEquals(expectedValue,calculatedValye, 0.00001);
                }
            }
        });
    }
}
