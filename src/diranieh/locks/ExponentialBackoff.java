package diranieh.locks;

import java.util.Random;

public class ExponentialBackoff {
    private final int _maxDelay;
    private final Random _random;
    private int _currentDelay;

    public ExponentialBackoff(int min, int max) {
        _currentDelay = min;    // initial minimum delay. Makes no sense if back off it too short
        _maxDelay = max;        // prevents threads from backing off for very long times
        _random = new Random(); // random delay between zero and current delay
    }

    public void backOff() throws InterruptedException {
        // Initially we start with a delay up to the given minimum, then exponentially
        // increment the delay by a power of 2 for each successive back off
        int delay = _random.nextInt(_currentDelay);
        if (_currentDelay < _maxDelay) { // double limit if less than max
            _currentDelay = 2 * _currentDelay;
        }
        Thread.sleep(delay);
    }
}
