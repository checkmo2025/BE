package checkmo.clubMeeting;

import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import java.util.Map;
import java.util.Set;

public interface ClubMeetingAPI {

    /**
     * 특정 모임의 상세 정보를 조회합니다.
     *
     * @param meetingId 조회할 모임 ID
     * @return 조회된 모임 상세 정보 DTO
     */
    DetailInfo fetchMeetingDetailInfo(Long meetingId);

    /**
     * 특정 모임들의 상세 정보를 조회합니다.
     *
     * @param meetingIds 조회할 모임 IDs
     * @return 조회된 모임 ID와 상세 정보 DTO의 맵
     */
    Map<Long, DetailInfo> fetchMeetingDetailInfoByMeetingIds(Set<Long> meetingIds);

    /**
     * 특정 동아리 내에 모임이 존재하는지 확인합니다.
     *
     * @param clubId    동아리 ID
     * @param meetingId 모임 ID
     */
    boolean isMeetingInClub(Long clubId, Long meetingId);
}
