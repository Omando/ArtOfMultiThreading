package diranieh.skipLists;

import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.Arrays;
import java.util.function.Predicate;

public class SkipListStepDefinitions implements En {
    SkipListSet<Integer> skipList;
    public SkipListStepDefinitions() {
        Given("a skip list", () -> {
            skipList = new SkipListSet<>();
        });
        When("I add {string}", (String addedNumbers) -> {
            Arrays.stream(addedNumbers.split(","))
                    .filter(s -> !s.isEmpty())
                    .map(s -> Integer.parseInt(s))
                    .forEach(number -> skipList.add(number));
        });
        And("I remove {string}", (String removedNumbers) -> {
            Arrays.stream(removedNumbers.split(","))
                    .filter(s -> !s.isEmpty())
                    .map(s -> Integer.parseInt(s))
                    .forEach(number -> skipList.remove(number));

        });
        Then("{string} should exist", (String remainingNumbers) -> {
            if (remainingNumbers.isEmpty())
                Assert.assertTrue(skipList.count() == 0);
            else
                Arrays.stream(remainingNumbers.split(","))
                        .filter(Predicate.not(String::isEmpty))
                        .map(Integer::parseInt)
                        .forEach(number -> Assert.assertTrue(skipList.contains(number)));
        });
    }
}
