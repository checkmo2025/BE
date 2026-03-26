package checkmo.realtime.web.websocket.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequestMessage {
    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 400, message = "메시지 내용은 최대 400자까지 허용됩니다.")
    private String content;
}
