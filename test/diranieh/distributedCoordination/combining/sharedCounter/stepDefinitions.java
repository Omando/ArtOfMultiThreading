package diranieh.distributedCoordination.combining.sharedCounter;

import diranieh.distributedCoordination.combining.CombiningTree;
import io.cucumber.java8.En;
import org.junit.jupiter.api.Assertions;

// Note that StepClass must implement cucmber.api.java8.En interface and step
// methods should be inside the constructor of test class.
public class stepDefinitions implements En {
        private CombiningTree sharedCounter;
        private int finalCount;
        public stepDefinitions() {

            /* Sequential steps*/
            Given("There is {int} thread", (Integer threadCount) -> {
                int width = threadCount * 2;
                sharedCounter = new CombiningTree(width);
            });

            When("I increment counter {int} times", (Integer count) -> {
                for(int i = 0; i < count; i++)
                    finalCount = sharedCounter.getAndIncrement();

            });

            Then("count will be {int}", (Integer expectedCount) -> {
                Assertions.assertEquals(expectedCount, finalCount);
            });

            /* Concurrent steps*/
            Given("There are {int} threads", (Integer threadCount) -> {
                int width = threadCount * 2;
                sharedCounter = new CombiningTree(width);
            });

            Given("Each thread counts {int} times", (Integer int1) -> {
                // Write code here that turns the phrase above into concrete actions
                throw new io.cucumber.java8.PendingException();
            });

            When("multiple threads count", () -> {
                // Write code here that turns the phrase above into concrete actions
                throw new io.cucumber.java8.PendingException();
            });

            Then("final count be {int}", (Integer int1) -> {
                // Write code here that turns the phrase above into concrete actions
                throw new io.cucumber.java8.PendingException();
            });
        }
}
