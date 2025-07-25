package checkmo.domain.club.converter;

import checkmo.domain.book.entity.Book;
import checkmo.domain.club.entity.BookRecommend;
import checkmo.domain.club.entity.Club;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.club.entity.announcement.Notice;
import checkmo.domain.club.entity.meeting.BookReview;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.domain.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.domain.club.web.dto.club.ClubResponseDTO;
import checkmo.domain.club.web.dto.meeting.MeetingRequestDTO;
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
     * BookReviewDTO <-> BookReview 엔티티 변환
     */
    public static BookReview fromBookReviewDTOToBookReview(BookShelfRequestDTO.BookReviewDTO request) {
        return BookReview.builder()
                .description(request.getDescription())
                .rate(request.getRate())
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
     * BookReview 엔티티 + MemberSharedDTO -> BookReviewDTO 변환
     */
    public static BookShelfResponseDTO.BookReviewDTO fromBookReviewAndMemberSharedDTOToBookReviewDTO(
            BookReview bookReview,
            MemberSharedDTO.BasicInfoDTO memberSharedDTO
    ) {
        return BookShelfResponseDTO.BookReviewDTO.builder()
                .bookReviewId(bookReview.getId())
                .description(bookReview.getDescription())
                .rate(bookReview.getRate())
                .authorInfo(memberSharedDTO)
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

    // =====================================================
    // Entity -> Entity 변환
    // =====================================================

    /**
     * Meeting 엔티티 -> Notice 엔티티 변환 (자동 생성)
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
    // DTO -> DTO 변환
    // =====================================================

    /**
     * BookReviewDTO 리스트 -> BookReviewListDTO 변환
     */
    public static BookShelfResponseDTO.BookReviewListDTO fromBookReviewDTOListToBookReviewListDTO(
            List<BookShelfResponseDTO.BookReviewDTO> bookReviewList,
            boolean hasNext,
            Long nextCursor
    ) {
        return BookShelfResponseDTO.BookReviewListDTO.builder()
                .bookReviewList(bookReviewList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    // =====================================================
    // Private Methods
    // =====================================================

}
