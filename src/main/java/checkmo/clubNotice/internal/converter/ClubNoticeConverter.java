package checkmo.clubNotice.internal.converter;

import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreatedEvent;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
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

    public static ClubNoticeResponseDTO.PureNotice toPureNoticeDTO(Notice notice) {
        return ClubNoticeResponseDTO.PureNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(notice.getTag())  // "공지"
                .build();
    }

    public static Vote toVote(ClubNoticeRequestDTO.CreateClubVote request, Long clubId) {
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

    public static List<ClubNoticeResponseDTO.EachItem> toEachItemDTOList(List<String> items) {
        return items.stream()
                .map(item -> ClubNoticeResponseDTO.EachItem.builder()
                        .item(item)
                        .isSelected(false)       // 기본값
                        .voteCount(0)            // 기본값
                        .votedMembers(List.of()) // 빈 리스트
                        .build())
                .toList();
    }

    public static ClubMemberVote toClubMemberVote(
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

    public static Notice toNotice(ClubNoticeRequestDTO.CreateClubNotice request, Long clubId) {
        return Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .important(request.isImportant())
                .tag("공지")
                .clubId(clubId)
                .build();
    }

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

    public static ClubNoticeResponseDTO.VoteNotice toVoteNoticeDTO(
            Vote vote,
            List<ClubNoticeResponseDTO.EachItem> itemDTOs
    ) {
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

    public static ClubNoticeResponseDTO.MeetingNotice toMeetingNoticeDTO(
            Notice notice,
            DetailInfo detailInfo
    ) {
        return ClubNoticeResponseDTO.MeetingNotice.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(notice.getTag())
                .detailInfoDTO(detailInfo)
                .build();
    }

    public static Notice toNotice(ClubMeetingCreatedEvent event) {
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
