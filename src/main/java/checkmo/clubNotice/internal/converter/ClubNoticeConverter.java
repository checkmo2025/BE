package checkmo.clubNotice.internal.converter;

import static checkmo.clubMeeting.internal.converter.ClubMeetingConverter.fromMeetingAndBookSharedDTOToMeetingInfoDTO;

import checkmo.book.BookExternalDTO;
import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubMeeting.internal.entity.Meeting;
import checkmo.clubNotice.internal.entity.MemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.member.MemberExternalDTO;
import checkmo.member.internal.entity.Member;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubNoticeConverter {

    /**
     * ClubResponseDTO.ClubNoticeListDTO 변환
     */
    public static ClubNoticeResponseDTO.ClubNoticeListDTO toClubNoticeListDTO(
            List<ClubNoticeResponseDTO.NoticeItem> noticeItems,
            boolean hasNext,
            Long nextCursor,
            boolean isStaff
    ) {
        List<ClubNoticeResponseDTO.NoticeItem> safeList =
                (noticeItems == null) ? List.of() : List.copyOf(noticeItems);

        return ClubNoticeResponseDTO.ClubNoticeListDTO.builder()
                .noticeList(safeList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(safeList.size())
                .isStaff(isStaff)
                .build();

    }

    /**
     * ClubResponseDTO.NoticeItem -> ClubResponseDTO.ClubNoticeWithClubDTO
     */
    public static ClubNoticeResponseDTO.ClubNoticeWithClubDTO toClubNoticeWithClubDTO(Notice notice,
                                                                                      ClubNoticeResponseDTO.NoticeItem noticeItemDTO) {
        var club = notice.getClub();
        if (club == null) {
            // 클럽 정보가 아예 없을 경우 null 처리
            return ClubNoticeResponseDTO.ClubNoticeWithClubDTO.builder()
                    .clubId(null)
                    .clubName(null)
                    .notice(noticeItemDTO)
                    .build();
        }
        return ClubNoticeResponseDTO.ClubNoticeWithClubDTO.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .notice(noticeItemDTO)
                .build();
    }

    /**
     * ClubResponseDTO.MemberNoticeListDTO 변환
     */
    public static ClubNoticeResponseDTO.MemberNoticeListDTO toMemberNoticeListDTO(
            List<ClubNoticeResponseDTO.ClubNoticeWithClubDTO> memberNoticeItems,
            boolean hasNext,
            Long nextCursor
    ) {
        List<ClubNoticeResponseDTO.ClubNoticeWithClubDTO> safeList =
                (memberNoticeItems == null) ? List.of() : List.copyOf(memberNoticeItems);

        return ClubNoticeResponseDTO.MemberNoticeListDTO.builder()
                .noticeList(safeList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(safeList.size())
                .build();

    }

    /**
     * ClubResponseDTO.VoteDTO -> ClubResponseDTO.ClubNoticeWithClubDTO
     */
    public static ClubNoticeResponseDTO.ClubNoticeWithClubDTO toClubNoticeWithClubDTO(Vote vote,
                                                                                      ClubNoticeResponseDTO.VoteDTO voteDTO) {
        return ClubNoticeResponseDTO.ClubNoticeWithClubDTO.builder()
                .clubId(vote.getClub().getId())
                .clubName(vote.getClub().getName())
                .notice(voteDTO)
                .build();
    }


    /**
     * Notice 엔티티 → PureNoticeDTO 변환
     */
    public static ClubNoticeResponseDTO.PureNoticeDTO toPureNoticeDTO(Notice notice) {
        return ClubNoticeResponseDTO.PureNoticeDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(notice.getTag())  // "공지"
                .build();
    }

    /**
     * CreateClubVoteDTO + Club -> Vote 엔티티 변환
     */
    public static Vote fromCreateVoteDTOToVote(
            ClubNoticeRequestDTO.CreateClubVoteDTO request,
            Club club
    ) {
        return Vote.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .tag("투표")
                .important(request.isImportant())
                .item1(request.getItem1())
                .item2(request.getItem2())
                .item3(request.getItem3())
                .item4(request.getItem4())
                .item5(request.getItem5())
                .anonymity(request.isAnonymity())
                .duplication(request.isDuplication())
                .startTime(request.getStartTime())
                .deadline(request.getDeadline())
                .clubId(club.getId())
                .club(club)
                .build();
    }

    /**
     * 투표 항목 리스트 → EachItemDTO 리스트 변환 기본값: isSelected = false, voteCount = 0, votedMembers = 빈 리스트
     */
    public static List<ClubNoticeResponseDTO.EachItemDTO> toEachItemDTOListFromItems(List<String> items) {
        return items.stream()
                .map(item -> ClubNoticeResponseDTO.EachItemDTO.builder()
                        .item(item)
                        .isSelected(false)       // 기본값
                        .voteCount(0)            // 기본값
                        .votedMembers(List.of()) // 빈 리스트
                        .build())
                .toList();
    }

    /**
     * VoteResultDTO -> MemberVote 엔티티
     */
    public static MemberVote fromVoteRequestToMemberVote(
            Vote vote,
            String memberId,
            Member memberProxy,
            ClubNoticeRequestDTO.VoteResultDTO request
    ) {
        return MemberVote.builder()
                .vote(vote)
                .voteId(vote.getId())
                .memberId(memberId)
                .member(memberProxy)
                .item1(request.isItem1())
                .item2(request.isItem2())
                .item3(request.isItem3())
                .item4(request.isItem4())
                .item5(request.isItem5())
                .build();
    }

    /**
     * CreateClubNoticeDTO -> Notice 엔티티 변환 (모임과 연결되지 않은 순수 공지사항)
     */
    public static Notice fromCreateNoticeDTOToNotice(
            ClubNoticeRequestDTO.CreateClubNoticeDTO request,
            Club club) {
        return Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .important(request.isImportant())
                .tag("공지")
                .club(club)
                .build();
    }

    /**
     * 투표 항목 → EachItemDTO 변환
     */
    public static ClubNoticeResponseDTO.EachItemDTO toEachItemDTO(
            String item,
            boolean isSelected,
            List<MemberExternalDTO.BasicInfo> votedMembers
    ) {
        return ClubNoticeResponseDTO.EachItemDTO.builder()
                .item(item)
                .isSelected(isSelected)
                .voteCount(votedMembers.size())
                .votedMembers(votedMembers)
                .build();
    }

    /**
     * Vote 엔티티 → VoteDTO 변환
     */
    public static ClubNoticeResponseDTO.VoteDTO toVoteDTO(Vote vote, List<ClubNoticeResponseDTO.EachItemDTO> itemDTOs) {
        return ClubNoticeResponseDTO.VoteDTO.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .content(vote.getContent())
                .important(vote.isImportant())
                .anonymity(vote.isAnonymity())
                .duplication(vote.isDuplication())
                .startTime(vote.getStartTime())
                .deadline(vote.getDeadline())
                .tag("투표")
                .items(itemDTOs)
                .build();
    }

    /**
     * Notice 엔티티 + BookExternalDTO.BasicInfoDTO -> ClubResponseDTO.MeetingNoticeDTO 변환
     */
    public static ClubNoticeResponseDTO.MeetingNoticeDTO toMeetingNoticeDTO(Notice notice,
                                                                            BookExternalDTO.BasicInfo bookInfo) {
        return ClubNoticeResponseDTO.MeetingNoticeDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(notice.getTag())
                .meetingInfoDTO(fromMeetingAndBookSharedDTOToMeetingInfoDTO(notice.getMeeting(), bookInfo))
                .build();
    }

    // =====================================================
    // Entity -> Entity 변환
    // =====================================================

    /**
     * Meeting 엔티티 -> Notice 엔티티 변환 (자동 생성)
     */
    public static Notice fromMeetingToNotice(Meeting meeting, Club club) {
        return Notice.builder()
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .important(true)
                .tag("모임")
                .club(club)
                .clubId(club.getId())
                .build();
    }

    // =====================================================
    // DTO -> DTO 변환
    // =====================================================

}
