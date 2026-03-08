package checkmo.realtime.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresentationRequestMessage {
    @NotNull(message = "발제 ID는 필수입니다.")
    private Long topicId;

    @NotNull(message = "발제 선택 여부는 필수입니다.")
    private Boolean isSelected;
}
