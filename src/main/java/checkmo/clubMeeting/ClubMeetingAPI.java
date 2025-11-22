package checkmo.clubMeeting;

import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import java.util.Map;
import java.util.Set;

/**
 * 미팅에 대한 정보를 다른 모듈에 제공하는 Public한 API
 */
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
}
