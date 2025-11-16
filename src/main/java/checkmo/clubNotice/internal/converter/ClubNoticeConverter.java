package checkmo.clubNotice.internal.converter;

import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreatedEvent;
import checkmo.clubMeeting.ClubMeetingExternalDTO;
import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubNoticeConverter {

    /**
     * ClubResponseDTO.ClubNoticeListDTO 변환
     */
    public static ClubNoticeResponseDTO.ClubNoticeList toClubNoticeListDTO(
            List<ClubNoticeResponseDTO.NoticeItem> noticeItems,
            boolean hasNext,
            Long nextCursor,
            boolean isStaff
    ) {
        List<ClubNoticeResponseDTO.NoticeItem> safeList =
                (noticeItems == null) ? List.of() : List.copyOf(noticeItems);

        return ClubNoticeResponseDTO.ClubNoticeList.builder()
                .noticeList(safeList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(safeList.size())
                .isStaff(isStaff)
                .build();

    }

    /**
     * Notice 엔티티 → PureNoticeDTO 변환
     */
    public static ClubNoticeResponseDTO.PureNotice toPureNoticeDTO(Notice notice) {
        return ClubNoticeResponseDTO.PureNotice.builder()
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
            ClubNoticeRequestDTO.CreateClubVote request,
            Long clubId
    ) {
        return checkmo.clubNotice.internal.entity.Vote.builder()
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
                .clubId(clubId)
                .build();
    }

    /**
     * 투표 항목 리스트 → EachItemDTO 리스트 변환 기본값: isSelected = false, voteCount = 0, votedMembers = 빈 리스트
     */
    public static List<ClubNoticeResponseDTO.EachItem> toEachItemDTOListFromItems(List<String> items) {
        return items.stream()
                .map(item -> ClubNoticeResponseDTO.EachItem.builder()
                        .item(item)
                        .isSelected(false)       // 기본값
                        .voteCount(0)            // 기본값
                        .votedMembers(List.of()) // 빈 리스트
                        .build())
                .toList();
    }

    /**
     * VoteResultDTO -> ClubMemberVote 엔티티
     */
    public static ClubMemberVote fromVoteRequestToMemberVote(
            Vote vote,
            Long clubMemberId,
            ClubNoticeRequestDTO.VoteResult request
    ) {
        return ClubMemberVote.builder()
                .vote(vote)
                .voteId(vote.getId())
                .clubMemberId(clubMemberId)
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
            ClubNoticeRequestDTO.CreateClubNotice request,
            Long clubId
    ) {
        return Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .important(request.isImportant())
                .tag("공지")
                .clubId(clubId)
                .build();
    }

    /**
     * 투표 항목 → EachItemDTO 변환
     */
    public static ClubNoticeResponseDTO.EachItem toEachItemDTO(
            String item,
            boolean isSelected,
            List<MemberExternalDTO.BasicInfo> votedMembers
    ) {
        return ClubNoticeResponseDTO.EachItem.builder()
                .item(item)
                .isSelected(isSelected)
                .voteCount(votedMembers.size())
                .votedMembers(votedMembers)
                .build();
    }

    /**
     * Vote 엔티티 → VoteDTO 변환
     */
    public static ClubNoticeResponseDTO.VoteNotice toVoteDTO(Vote vote, List<ClubNoticeResponseDTO.EachItem> itemDTOs) {
        return ClubNoticeResponseDTO.VoteNotice.builder()
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
     * Notice 엔티티 + MeetingInfo -> ClubResponseDTO.MeetingNoticeDTO 변환
     */
    public static ClubNoticeResponseDTO.MeetingNotice toMeetingNoticeDTO(
            Notice notice,
            ClubMeetingExternalDTO.MeetingInfo meetingInfo
    ) {
        return ClubNoticeResponseDTO.MeetingNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(notice.getTag())
                .meetingInfoDTO(meetingInfo)
                .build();
    }

    public static Notice fromMeetingCreatedEventToNotice(ClubMeetingCreatedEvent event) {
        return Notice.builder()
                .clubId(event.clubId())
                .meetingId(event.meetingId())
                .meetingVersion(event.version())
                .title(event.title())
                .content(event.content())
                .tag("모임")
                .important(true)
                .build();
    }
}
