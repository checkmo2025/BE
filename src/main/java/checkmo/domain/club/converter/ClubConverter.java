package checkmo.domain.club.converter;

import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubConverter {

    // =====================================================
    // ClubResponseDTO → ClubSharedDTO 변환
    // =====================================================

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    /**
     * MeetingCreateRequestDTO -> Meeting 엔티티 변환
     */
    public static Meeting fromMeetingCreateRequestDTOToMeeting(
            MeetingRequestDTO.MeetingCreateRequestDTO request
    ) {
        return Meeting.builder()
                .title(request.getTitle())
                .meetingTime(request.getMeetingTime())
                .location(request.getLocation())
                .content(request.getContent())
                .generation(request.getGeneration())
                .tag(request.getTag())
                .build();
    }

    // =====================================================
    // 기타 메서드
    // =====================================================

    /**
     * Meeting 엔티티로부터 Notice 엔티티 변환(자동 생성)
     */
    public static Notice fromMeetingToNotice(Meeting meeting) {
        return Notice.builder()
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .important(true)
                .tag("모임")
                .build();
    }

    // =====================================================
    // Private Methods
    // =====================================================

}
