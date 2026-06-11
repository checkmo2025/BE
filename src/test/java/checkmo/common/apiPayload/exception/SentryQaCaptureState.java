package checkmo.common.apiPayload.exception;

import java.util.concurrent.atomic.AtomicInteger;

public class SentryQaCaptureState {

    private final AtomicInteger count = new AtomicInteger();

    public void increment() {
        count.incrementAndGet();
    }

    public int count() {
        return count.get();
    }

    public void reset() {
        count.set(0);
    }
}
