package checkmo.realtime.internal.exception;

import lombok.Getter;

@Getter
public class RealtimeException extends RuntimeException {

    private final RealtimeErrorStatus errorStatus;

    public RealtimeException(RealtimeErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }

    public RealtimeException(RealtimeErrorStatus errorStatus, Throwable cause) {
        super(errorStatus.getMessage(), cause);
        this.errorStatus = errorStatus;
    }
}
