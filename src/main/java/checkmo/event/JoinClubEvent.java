package checkmo.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JoinClubEvent {
    private final String memberId; // 클럽에 가입한 회원의 ID
    private final String clubId;    // 가입한 클럽의 ID
    private final String clubName;  // 가입한 클럽의 이름
}
