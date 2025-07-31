package checkmo.domain.club.converter;

import checkmo.domain.book.entity.Book;
import checkmo.domain.club.entity.BookRecommend;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.MemberVote;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.domain.member.entity.Member;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

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
            MeetingRequestDTO.MeetingCreateRequestDTO request,
            Book proxyBook
    ) {
        return Meeting.builder()
                .title(request.getTitle())
                .meetingTime(request.getMeetingTime())
                .location(request.getLocation())
                .content(request.getContent())
                .generation(request.getGeneration())
                .tag(request.getTag())
                .book(proxyBook)
                .build();
    }

    /**
     * ClubRequestDTO.ClubDetailDTO -> Club 엔티티 변환
     */
    public static Club fromClubDetailDTOToClub(ClubRequestDTO.ClubDetailDTO dto) {
        return Club.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .profileImgUrl(dto.getProfileImageUrl())
                .open(dto.isOpen())
                .region(dto.getRegion())
                .insta(dto.getInsta())
                .kakao(dto.getKakao())
                .participantTypes(dto.getParticipantTypes())
                .build();
    }

    /**
     * Club 엔티티 -> ClubRequestDTO.ClubDetailDTO 변환
     */
    public static ClubResponseDTO.ClubDetailDTO fromClubToClubDetailDTO(Club club, List<Long> categoryIds) {
        return ClubResponseDTO.ClubDetailDTO.builder()
                .clubId(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .profileImageUrl(club.getProfileImgUrl())
                .open(club.isOpen())
                .category(categoryIds)
                .region(club.getRegion())
                .participantTypes(club.getParticipantTypes())
                .insta(club.getInsta())
                .kakao(club.getKakao())
                .build();
    }

    /**
     * CreateBookRecommendDTO -> BookRecommend 엔티티
     */
    public static BookRecommend fromCreateBookRecommendDTOToEntity(
            ClubRequestDTO.CreateBookRecommendDTO request,
            Book proxyBook,
            ClubMember clubMember
    ) {
        return BookRecommend.builder()
                .content(request.getContent())
                .rate(request.getRate())
                .tag(request.getTag())
                .clubMember(clubMember)
                .book(proxyBook)
                .bookId(proxyBook.getId())
                .build();
    }

    /**
     * BookRecommend 엔티티 → BookRecommendDetailDTO
     */
    public static ClubResponseDTO.BookRecommendDetailDTO toBookRecommendDetailDTO(
            BookRecommend bookRecommend,
            BookSharedDTO.BasicInfoDTO bookInfo,
            MemberSharedDTO.BasicInfoDTO authorInfo,
            String currentMemberNickname
    ) {
        return ClubResponseDTO.BookRecommendDetailDTO.builder()
                .id(bookRecommend.getId())
                .content(bookRecommend.getContent())
                .rate(bookRecommend.getRate())
                .tag(bookRecommend.getTag())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .isAuthor(authorInfo.getNickname().equals(currentMemberNickname))
                .build();
    }

    /**
     * BookRecommendDTO 리스트 → BookRecommendListDTO 변환
     */
    public static ClubResponseDTO.BookRecommendListDTO toBookRecommendListDTO(
            List<ClubResponseDTO.BookRecommendDetailDTO> dtoList,
            boolean hasNext,
            Long lastCursorId
    ) {
        return ClubResponseDTO.BookRecommendListDTO.builder()
                .bookRecommendList(dtoList)
                .hasNext(hasNext)
                .nextCursor(lastCursorId)
                .pageSize(dtoList.size())
                .build();
    }

    /**
     * Notice 엔티티 → PureNoticeDTO 변환
     */
    public static ClubResponseDTO.PureNoticeDTO toPureNoticeDTO(Notice notice) {
        return ClubResponseDTO.PureNoticeDTO.builder()
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
            ClubRequestDTO.CreateClubVoteDTO request,
            Club club
    ) {
        return Vote.builder()
                .title(request.getTitle())
                .tag("투표")
                .important(request.isImportant())
                .item1(request.getItem1())
                .item2(request.getItem2())
                .item3(request.getItem3())
                .item4(request.getItem4())
                .item5(request.getItem5())
                .isAnonymity(request.isAnonymity())
                .isDuplication(request.isDuplication())
                .startTime(request.getStartTime())
                .deadline(request.getDeadline())
                .clubId(club.getId())
                .club(club)
                .build();
    }

    /**
     * CreateClubNoticeDTO -> Notice 엔티티 변환 (모임과 연결되지 않은 순수 공지사항)
     */
    public static Notice fromCreateNoticeDTOToNotice(
            ClubRequestDTO.CreateClubNoticeDTO request,
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
    public static ClubResponseDTO.EachItemDTO toEachItemDTO(
            String item,
            boolean isSelected,
            List<MemberSharedDTO.BasicInfoDTO> votedMembers
    ) {
        return ClubResponseDTO.EachItemDTO.builder()
                .item(item)
                .isSelected(isSelected)
                .voteCount(votedMembers.size())
                .votedMembers(votedMembers)
                .build();
    }

    /**
     * Vote 엔티티 → VoteDTO 변환
     */
    public static ClubResponseDTO.VoteDTO toVoteDTO(Vote vote, List<ClubResponseDTO.EachItemDTO> itemDTOs) {
        return ClubResponseDTO.VoteDTO.builder()
                .id(vote.getId())
                .title(vote.getTitle())
                .important(vote.isImportant())
                .tag("투표")
                .items(itemDTOs)
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
