package checkmo.apiPayload.exception;

import checkmo.apiPayload.code.ErrorReasonDTO;
import checkmo.apiPayload.code.status.ErrorStatus;
import lombok.Getter;

@Getter
//@AllArgsConstructor
public class GeneralException extends RuntimeException {

//    private BaseErrorCode code; //기본적인 에러 코드 정보를 저장
    private final ErrorStatus errorStatus; //일반적인 HTTP 에러 코드 및 메시지
    private final String detailMessage; // 사용자가 입력한 메시지를 저장

    public ErrorReasonDTO getErrorReason() {
        return this.errorStatus.getReason();
    }

    public ErrorReasonDTO getErrorReasonHttpStatus(){
        return this.errorStatus.getReasonHttpStatus();
    }

//    // 기본 생성자 (에러 코드만 받는 경우)
//    public GeneralException(ErrorStatus errorStatus) {
//        super(errorStatus.getMessage());
//        this.errorStatus = errorStatus;
//    }
//
//    // 상세 메시지를 추가할 수 있는 생성자
//    public GeneralException(ErrorStatus errorStatus, String message) {
//        super(message);
//        this.errorStatus = errorStatus;
//    }

    public GeneralException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
        this.detailMessage = null; // 상세 메시지가 없는 경우 기본 null 처리
    }

    public GeneralException(ErrorStatus errorStatus, String message) {
        super(message);
        this.errorStatus = errorStatus;
        this.detailMessage = message; // 입력한 메시지를 저장
    }
}
