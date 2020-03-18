package diranieh.concurrentHashing;

import diranieh.utilities.Set;
import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.Arrays;
import java.util.function.Predicate;

public class OpenAddressStepDefinition implements En {
    private int capacity;
    private int itemCount;
    private Set<Integer> hashSet;

    public OpenAddressStepDefinition() {

        // Could not get this to work!
        /*ParameterType("integerList", "(-?[0-9]+(,\\\\\\\\s*-?[0-9]+)*)",  (String numbers) -> {
            return Arrays.stream(numbers.split(","))
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);
                });*/

        Given("capacity is {int}", (Integer capacity) -> {
            this.capacity = capacity;
        });

        And("open address implementation is {string}", (String implementation) -> {
            switch (implementation) {
                case "ThreadUnSafe":
                    hashSet = HashSetFactory.getOpenAddressNonThreadSafeCuckoo(capacity);
                    break;
                    // Add others
            }
        });

        When("I add {string}", (String addedNumbers) -> {
            Arrays.stream(addedNumbers.split(","))
                    .filter( s -> !s.isEmpty())
                    .map(s -> Integer.parseInt(s))
                    .forEach(number -> hashSet.add(number));
        });

        And("I remove {string}", (String removedNumbers) -> {
            Arrays.stream(removedNumbers.split(","))
                    .filter(Predicate.not(String::isEmpty))
                    .map((Integer::parseInt))
                    .forEach(number -> hashSet.remove(number));
        });

        Then("{string} should exist", (String remainingNumbers) -> {
            if (remainingNumbers.isEmpty())
                Assert.assertTrue(hashSet.isEmpty());
            else
                Arrays.stream(remainingNumbers.split(","))
                    .filter(Predicate.not(String::isEmpty))
                    .map(Integer::parseInt)
                    .forEach(number -> Assert.assertTrue(hashSet.contains(number)));
        });

        When("I add {int} numbers to the list", (Integer count) -> {
            itemCount = count;
            for (int i = 0; i < itemCount; i++) {
                hashSet.add(i);
            }
        });

        And("I remove the same numbers", () -> {
            for (int i = 0; i < itemCount; i++) {
                hashSet.remove(i);
            }
        });

        Then("hash set should be empty", () -> {
            Assert.assertTrue(hashSet.isEmpty());
        });
    }
}
