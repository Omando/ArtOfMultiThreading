package diranieh.distributedCoordination.combining.sharedCounter;


import diranieh.distributedCoordination.combining.CombiningTree;
import io.cucumber.java8.En;
import org.junit.jupiter.api.Assertions;

// Note that StepClass must implement cucmber.api.java8.En interface and step
// methods should be inside the constructor of test class.
// --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm5SMFormatter
public class stepDefinitions implements En {
        private CombiningTree sharedCounter;
        private int finalCount;
        public stepDefinitions() {
            Given("^Tree size is (\\d+)$", (Integer size) -> {
                sharedCounter = new CombiningTree(size);
            });

            When("^I increment counter (\\d+) times$", (Integer count) -> {
                for(int i = 0; i < count; i++)
                    finalCount = sharedCounter.getAndIncrement();

            });

            Then("^count will be (\\d+)$", (Integer expectedCount) -> {
                Assertions.assertEquals(expectedCount, finalCount);
            });
        }
}
