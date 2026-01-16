package checkmo.clubNotice.internal.converter;

import checkmo.clubManagement.ClubManagementExternalDTO.MembershipInfo;
import checkmo.clubMeeting.ClubMeetingEvent.ClubMeetingCreatedEvent;
import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.NoticeTag;
import checkmo.clubNotice.internal.entity.Vote;
import checkmo.clubNotice.web.dto.ClubNoticeRequestDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.ClubNoticePreview;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.ClubNoticeTagItem;
import checkmo.clubNotice.web.dto.ClubNoticeResponseDTO.EachItem;
import checkmo.member.MemberExternalDTO;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubNoticeConverter {

    // ========== Entity 변환 ==========
    public static Notice toNotice(ClubNoticeRequestDTO.CreateClubNotice request, Long clubId) {
        return Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .important(request.isImportant())
                .tag(NoticeTag.decideTag(request.getVote() != null, request.getMeetingId() != null))
                .meetingId(request.getMeetingId())
                .meetingVersion(request.getMeetingVersion())
                .vote(toVote(request.getVote()))
                .clubId(clubId)
                .build();
    }

    public static Vote toVote(ClubNoticeRequestDTO.CreateClubVote request) {
        if (request == null) {
            return null;
        }
        return Vote.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .item1(request.getItem1())
                .item2(request.getItem2())
                .item3(request.getItem3())
                .item4(request.getItem4())
                .item5(request.getItem5())
                .anonymity(request.isAnonymity())
                .duplication(request.isDuplication())
                .startTime(request.getStartTime())
                .deadline(request.getDeadline())
                .build();
    }

    public static Notice toNotice(ClubMeetingCreatedEvent event) {
        return Notice.builder()
                .title(event.title())
                .content(event.content())
                .important(true)
                .tag(NoticeTag.MEETING)
                .meetingId(event.meetingId())
                .meetingVersion(event.version())
                .clubId(event.clubId())
                .vote(null)
                .build();
    }

    public static ClubMemberVote toClubMemberVote(
            Vote vote,
            Long clubMemberId,
            ClubNoticeRequestDTO.VoteResult request
    ) {
        List<Integer> selected = request.getSelectedItemNumbers();
        return ClubMemberVote.builder()
                .vote(vote)
                .clubMemberId(clubMemberId)
                .item1(selected.contains(1))
                .item2(selected.contains(2))
                .item3(selected.contains(3))
                .item4(selected.contains(4))
                .item5(selected.contains(5))
                .build();
    }

    // ========== DTO 변환 ==========
    public static ClubNoticeResponseDTO.ClubNoticePreview toClubNoticePreview(Notice notice) {
        return ClubNoticePreview.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .important(notice.isImportant())
                .tagItem(ClubNoticeTagItem.from(notice.getTag()))
                .build();
    }

    public static ClubNoticeResponseDTO.ClubNoticeDetail toClubNoticeDetail(
            Notice notice,
            DetailInfo meetingDetail,
            ClubNoticeResponseDTO.VoteDetail voteDetail,
            MembershipInfo membershipInfo
    ) {
        return ClubNoticeResponseDTO.ClubNoticeDetail.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(ClubNoticeTagItem.from(notice.getTag()))
                .meetingDetail(meetingDetail)
                .voteDetail(voteDetail)
                .isStaff(membershipInfo.isStaff())
                .build();
    }

    public static ClubNoticeResponseDTO.VoteDetail toVoteDetail(Vote vote, List<EachItem> items) {
        return ClubNoticeResponseDTO.VoteDetail.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .content(vote.getContent())
                .anonymity(vote.isAnonymity())
                .duplication(vote.isDuplication())
                .startTime(vote.getStartTime())
                .deadline(vote.getDeadline())
                .items(items)
                .build();
    }

    public static ClubNoticeResponseDTO.EachItem toEachItem(
            int itemNumber,
            String item,
            boolean isSelected, // 현재 로그인한 멤버가 해당 항목에 투표했는지 여부
            List<MemberExternalDTO.BasicInfo> votedMembers
    ) {
        return ClubNoticeResponseDTO.EachItem.builder()
                .itemNumber(itemNumber)
                .item(item)
                .isSelected(isSelected)
                .voteCount(votedMembers.size())
                .votedMembers(votedMembers)
                .build();
    }
}
