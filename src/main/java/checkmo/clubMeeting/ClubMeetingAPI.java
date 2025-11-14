package checkmo.clubMeeting;

import checkmo.clubMeeting.ClubMeetingExternalDTO.MeetingInfo;
import java.util.Map;
import java.util.Set;

public interface ClubMeetingAPI {

    /**
     * 특정 모임의 상세 정보를 조회합니다.
     *
     * @param meetingId 조회할 모임 ID
     * @return 조회된 모임 상세 정보 DTO
     */
    ClubMeetingExternalDTO.MeetingInfo getMeeting(Long meetingId);

    /**
     * 특정 모임들의 상세 정보를 조회합니다.
     *
     * @param meetingIds 조회할 모임 IDs
     * @return 조회된 모임 ID와 상세 정보 DTO의 맵
     */
    Map<Long, MeetingInfo> getMeetings(Set<Long> meetingIds);
}
