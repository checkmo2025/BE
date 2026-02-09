package checkmo.clubNotice.internal.converter;

import checkmo.clubMeeting.ClubMeetingExternalDTO.DetailInfo;
import checkmo.clubNotice.internal.entity.ClubMemberVote;
import checkmo.clubNotice.internal.entity.Notice;
import checkmo.clubNotice.internal.entity.NoticeComment;
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
    public static Notice toNotice(ClubNoticeRequestDTO.CreateClubNotice request, NoticeTag tag, Long clubId) {
        return Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .important(request.isImportant())
                .tag(tag)
                .meetingId(request.getMeetingId())
                .clubId(clubId)
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
                .item6(selected.contains(6))
                .build();
    }

    public static NoticeComment toNoticeComment(
            ClubNoticeRequestDTO.CreateClubNoticeComment request,
            Long clubMemberId
    ) {
        return NoticeComment.builder()
                .content(request.getContent())
                .clubMemberId(clubMemberId)
                .build();
    }

    // ========== DTO 변환 ==========
    public static ClubNoticeResponseDTO.ClubNoticePreview toClubNoticePreview(Notice notice) {
        return ClubNoticePreview.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .important(notice.isImportant())
                .tagItem(ClubNoticeTagItem.from(notice.getTag()))
                .createdAt(notice.getCreatedAt())
                .build();
    }

    public static ClubNoticeResponseDTO.ClubNoticeDetail toClubNoticeDetail(
            Notice notice,
            DetailInfo meetingDetail,
            ClubNoticeResponseDTO.VoteDetail voteDetail
    ) {
        return ClubNoticeResponseDTO.ClubNoticeDetail.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(ClubNoticeTagItem.from(notice.getTag()))
                .imageUrls(notice.getImageUrls())
                .createdAt(notice.getCreatedAt())
                .meetingDetail(meetingDetail)
                .voteDetail(voteDetail)
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
                .selected(isSelected)
                .voteCount(votedMembers.size())
                .votedMembers(votedMembers)
                .build();
    }

    public static ClubNoticeResponseDTO.NoticeComment toNoticeComment(
            NoticeComment comment,
            MemberExternalDTO.BasicInfo memberInfo
    ) {
        return ClubNoticeResponseDTO.NoticeComment.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .authorInfo(memberInfo)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
