package diranieh.workDistribution.boundedDequeue;

import diranieh.workDistribution.workStealing.BoundedSequentialDequeue;
import io.cucumber.java8.En;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoundedDequeueStepDefinitions implements En {
    private  Exception _exception;
    private BoundedSequentialDequeue<Integer> _dequeue;
    List<Integer> _pushedItems;
    List<Integer> _poppedItems;
    public BoundedDequeueStepDefinitions() {
        When("popBottom is called on an empty dequeue", () -> {
            try {
                BoundedSequentialDequeue<Integer> dequeue = new BoundedSequentialDequeue<>(5);
                dequeue.popBottom();
            } catch (IllegalStateException e) {
                _exception = e;
            }
        });

        Then("An exception is thrown", () -> {
            Assert.assertNotNull(_exception);
        });

        When("popTop is called on an empty dequeue", () -> {
            try {
                BoundedSequentialDequeue<Integer> dequeue = new BoundedSequentialDequeue<>(5);
                dequeue.popTop();
            } catch (IllegalStateException e) {
                _exception = e;
            }
        });

        Given("bounded queue with capacity {int}", (Integer capacity) -> {
            _dequeue = new BoundedSequentialDequeue<>(capacity);
        });

        When("more items than capacity are pushed", () -> {

            // Push up to capacity
            int capacity = _dequeue.get_Capacity();
            for (int i = 0; i < capacity; i++) {
                _dequeue.pushBottom(i);
            }

            // Pushing more throws an exception
            try {
                _dequeue.pushBottom(1);
            } catch (ArrayIndexOutOfBoundsException e) {
                _exception = e;
            }
        });

        When("{int} are items are pushed", (Integer itemCount) -> {
            _pushedItems = new ArrayList<>(itemCount);
            for (int i = 0; i < itemCount; i++) {
                _pushedItems.add(i);
                _dequeue.pushBottom(i);
            }
        });
        And("{int} items are popped from the bottom", (Integer itemCount) -> {
            Integer[] items = new Integer[itemCount];
            for (int i = 0; i < itemCount; i++) {
                Integer item = _dequeue.popBottom();
                items[itemCount - 1 - i] =  item;
            }
            _poppedItems = Arrays.asList(items);
        });

        And("popped items are same as pushed items", () -> {
            for (int i = 0; i < _poppedItems.size(); i++) {
                Assert.assertTrue(_pushedItems.get(i).equals(_poppedItems.get(i)));
            }
        });

        Then("bounded queue is empty", () -> {
            Assert.assertTrue(_dequeue.isEmpty());
        });

        And("{int} items are popped from the top", (Integer itemCount) -> {
            _poppedItems = new ArrayList<>(itemCount);
            for (int i = 0; i < itemCount; i++) {
                _poppedItems.add(_dequeue.popTop());
            }
        });

        When("{int} items are pushed and popped alternatively from bottom", (Integer itemCount) -> {
            _poppedItems = new ArrayList<>(itemCount);
            _pushedItems = new ArrayList<>(itemCount);
            for (int i = 0; i < itemCount; i++) {
                _pushedItems.add(i);
                _dequeue.pushBottom(i);

                Integer item = _dequeue.popBottom();
                _poppedItems.add(item);
            }
        });
    }
}
