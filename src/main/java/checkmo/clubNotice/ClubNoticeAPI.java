package checkmo.clubNotice;

import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;

public interface ClubNoticeAPI {

    /**
     * ClubQueryService 모임의 전체 공지사항 목록을 최신순으로 조회합니다. (내부용)
     *
     * @param clubId        모임 ID
     * @param memberId      조회자 회원 ID
     * @param cursorId      페이징 커서 ID
     * @param onlyImportant 중요 공지사항만 조회할지 여부
     * @param size          조회할 개수
     * @return 전체 공지사항 목록 DTO
     */
    ClubNoticeResponseDTO.ClubNoticeListDTO getLatestNotices(Long clubId, String memberId, Long cursorId,
                                                             boolean onlyImportant, Integer size);

    /**
     * 특정 회원이 가입한 모든 클럽의 최신 소식을 조회합니다. 홈 화면 피드를 구성할 때 사용됩니다.
     *
     * @param memberId 조회할 회원의 ID
     * @param size     조회할 개수
     * @return 모든 클럽의 최신 소식이 통합된 미리보기 DTO
     */
    ClubNoticeResponseDTO.MemberNoticeListDTO getNoticeForHome(String memberId, Long cursorId, boolean onlyImportant,
                                                               Integer size);

    /**
     * ClubQueryService 공지사항(투표 포함)의 상세 정보를 조회합니다. (내부용)
     *
     * @param clubId   모임 ID
     * @param noticeId 조회할 공지사항 ID
     * @return 공지사항 상세 정보 DTO
     */
    ClubNoticeResponseDTO.ClubNoticeDetailDTO getNoticeDetail(Long clubId, Long noticeId, String tag, String memberId);

}
