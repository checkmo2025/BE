package checkmo.common.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecordingSentryCaptureClient implements SentryCaptureClient {

    private final List<Throwable> captured = new ArrayList<>();

    @Override
    public void captureException(Throwable exception) {
        captured.add(exception);
    }

    public int count() {
        return captured.size();
    }

    public List<Throwable> captured() {
        return Collections.unmodifiableList(captured);
    }
}
