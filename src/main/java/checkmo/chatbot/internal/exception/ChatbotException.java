package checkmo.chatbot.internal.exception;

import checkmo.common.apiPayload.exception.GeneralException;

public class ChatbotException extends GeneralException {

    public ChatbotException(ChatbotErrorStatus status) {
        super(status);
    }

    public ChatbotException(ChatbotErrorStatus status, Throwable cause) {
        super(status, cause);
    }
}
