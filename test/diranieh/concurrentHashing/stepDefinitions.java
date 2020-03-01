package diranieh.concurrentHashing;

import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class stepDefinitions implements En {

    private ConcurrentCoarseHashSet<Integer> hashSet;

    public stepDefinitions() {
        Given("capacity is {int} and bucket threshold is {int}",
                (Integer capacity, Integer threshold) -> {
            hashSet = new ConcurrentCoarseHashSet<>(capacity, threshold);
        });

        When("I add the following items", (DataTable data) -> {
            List<Integer> numbers = data.asList(Integer.class);
            numbers.forEach(number -> hashSet.add(number) );
        });

        And("I remove the following items", (DataTable data) -> {
            List<Integer> numbers = data.asList(Integer.class);
            numbers.forEach(number -> hashSet.remove(number) );
        });

        Then("only these items should exist", (DataTable data) -> {
            List<Integer> numbers = data.asList(Integer.class);
            Assertions.assertEquals(2, hashSet.size);
            numbers.forEach(number -> {
                Assertions.assertTrue( hashSet.contains(number));
            });
        });

        When("user adds {int} numbers", (Integer itemCount) -> {
            for (int i = 0; i < itemCount; i++) {
                hashSet.add(i);
            }
        });

        Then("capacity should increase to {int}", (Integer newCapacity) -> {
            Assertions.assertEquals(newCapacity, hashSet.table.length);
        });
    }
}
