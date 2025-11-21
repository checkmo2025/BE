package checkmo.member.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(MemberErrorStatus status) {
        super(status);
    }
}
