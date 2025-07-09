package checkmo.domain.club.dto.meeting;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingRequestDTO {

    @Getter
    @NoArgsConstructor
    public static class MeetingCreateRequestDTO {

        private String title;
        private String content; // 모임 내용 설명 (공지사항 내용)
        private LocalDateTime meetingTime; // 미팅 날짜, 시간
        private String location; // 모임 장소
        private String isbn; // 사용하는 책 ISBN
        private int generation; // 기수
        private List<String> nicknameList; // 참여 인원들 (닉네임 리스트)
    }

    @Getter
    @NoArgsConstructor
    public static class TopicDTO {
        private String description; // 토픽 내용
    }

    @Getter
    @NoArgsConstructor
    public static class MeetingTeamDTO {
        private List<TeamMemberDTO> teamMemberDTOList;
    }

    @Getter
    @NoArgsConstructor
    public static class TeamMemberDTO {
        private Integer teamNumber; // 팀 번호
        private List<String> nicknameList; // 팀원들의 닉네임 리스트
    }

}
