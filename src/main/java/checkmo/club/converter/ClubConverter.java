package checkmo.club.converter;

import checkmo.book.entity.Book;
import checkmo.club.entity.BookRecommend;
import checkmo.club.entity.Club;
import checkmo.club.entity.ClubCategory;
import checkmo.club.entity.ClubMember;
import checkmo.club.entity.announcement.MemberVote;
import checkmo.club.entity.announcement.Notice;
import checkmo.club.entity.announcement.Vote;
import checkmo.club.entity.meeting.*;
import checkmo.club.web.dto.MembershipResponseDTO;
import checkmo.club.web.dto.bookshelf.BookShelfRequestDTO;
import checkmo.club.web.dto.bookshelf.BookShelfResponseDTO;
import checkmo.club.web.dto.club.ClubRequestDTO;
import checkmo.club.web.dto.club.ClubResponseDTO;
import checkmo.club.web.dto.meeting.MeetingRequestDTO;
import checkmo.club.web.dto.meeting.MeetingResponseDTO;
import checkmo.domain.member.entity.Member;
import checkmo.global.dto.BookSharedDTO;
import checkmo.global.dto.CategorySharedDTO;
import checkmo.global.dto.ClubSharedDTO;
import checkmo.global.dto.MemberSharedDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClubConverter {

    // =====================================================
    // ClubResponseDTO → ClubSharedDTO 변환
    // =====================================================

    // =====================================================
    // Entity ↔ DTO 변환
    // =====================================================

    /**
     * ClubMember 엔티티 -> MembershipResponseDTO.MembershipDTO 변환
     */
    public static MembershipResponseDTO.MembershipDTO fromClubMembertoMembershipDTO(ClubMember clubMember) {
        return MembershipResponseDTO.MembershipDTO.builder()
                .clubMemberId(clubMember.getId())
                .clubMemberStatus(clubMember.getClubMemberStatus().name())
                .updatedAt(clubMember.getUpdatedAt())
                .build();
    }

    /**
     * ClubResponseDTO.ClubDetailResponseDTO -> ClubResponseDTO.MyPageClubListDTO 변환
     */
    public static ClubResponseDTO.MyPageClubListDTO toMyPageClubListDTO(
            List<ClubResponseDTO.ClubDetailResponseDTO> clubList,
            boolean hasNext,
            Long nextCursor
    ) {
        return ClubResponseDTO.MyPageClubListDTO.builder()
                .clubList(clubList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * Club 리스트 → ClubResponseDTO.ClubListDTO 변환
     */
    public static ClubResponseDTO.ClubListDTO toClubListDTO(
            List<ClubResponseDTO.ClubWithMyStatusDTO> clubList,
            boolean hasNext,
            Long nextCursor) {

        List<ClubResponseDTO.ClubWithMyStatusDTO> safeList =
                (clubList == null) ? List.of() : List.copyOf(clubList);

        return ClubResponseDTO.ClubListDTO.builder()
                .clubList(safeList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(safeList.size())
                .build();
    }

    /**
     * Club 가입 정보 -> ClubMember 엔티티 변환
     */
    public static ClubMember toClubMemberEntity(
            Club club,
            Member member,
            ClubMember.ClubMemberStatus status,
            String joinMessage
    ) {
        return ClubMember.builder()
                .clubMemberStatus(status)
                .joinMessage(joinMessage)
                .club(club)
                .member(member)
                .build();
    }

    /**
     * Club -> ClubResponseDTO.ClubInfoDTO
     */
    public static ClubResponseDTO.ClubInfoDTO toClubInfoDTO(Club club) {
        return ClubResponseDTO.ClubInfoDTO.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .open(club.isOpen())
                .build();
    }

    /**
     * ClubSharedDTO.MyClubInfo -> ClubResponseDTO.ClubInfoDTO
     */
    public static ClubResponseDTO.ClubInfoDTO toClubInfoDTOFromMyClubInfo(ClubSharedDTO.MyClubInfo myClubInfo) {
        return ClubResponseDTO.ClubInfoDTO.builder()
                .clubId(myClubInfo.getClubId())
                .clubName(myClubInfo.getClubName())
                .open(null)
                .build();
    }

    /**
     * ClubMemberDTO 리스트 → ClubResponseDTO.ClubMemberListDTO 변환
     */
    public static ClubResponseDTO.ClubMemberListDTO toClubMemberListDTO(List<ClubResponseDTO.ClubMemberDTO> dtoList, boolean hasNext, Long lastId) {
        return ClubResponseDTO.ClubMemberListDTO.builder()
                .clubMembers(dtoList)
                .hasNext(hasNext)
                .nextCursor(lastId)
                .pageSize(dtoList.size())
                .isStaff(true) // 항상 true
                .build();
    }

    /**
     * ClubMember 엔티티 + MemberSharedDTO.BasicInfoDTO -> ClubResponseDTO.ClubMemberDTO 변환
     */
    public static ClubResponseDTO.ClubMemberDTO toClubMemberDTO(ClubMember targetMember, MemberSharedDTO.BasicInfo memberInfo) {
        return ClubResponseDTO.ClubMemberDTO.builder()
                .clubMemberId(targetMember.getId())
                .basicInfo(memberInfo)
                .joinMessage(targetMember.getJoinMessage())
                .clubMemberStatus(targetMember.getClubMemberStatus().name())
                .build();
    }

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
     * ClubRequestDTO.ClubDetailDTO -> CategoryIdListDTO
     */
    public static CategorySharedDTO.CategoryIdList toCategoryListRequestDTO(ClubRequestDTO.ClubDetailDTO dto) {
        return CategorySharedDTO.CategoryIdList.builder()
                .categoryIdList(dto.getCategory())
                .build();
    }

    /**
     * Club 엔티티 -> ClubRequestDTO.ClubDetailDTO 변환
     */
    public static ClubResponseDTO.ClubDetailDTO fromClubToClubDetailDTO(Club club, List<Long> categoryIds, boolean isStaff) {
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
                .isStaff(isStaff)
                .build();
    }

    public static ClubResponseDTO.ClubDetailResponseDTO fromClubToResponseDTO(
            Club club, List<CategorySharedDTO.CategoryInfo> categories, boolean isStaff) {

        List<String> categoryNames = categories.stream()
                .map(CategorySharedDTO.CategoryInfo::getName)
                .toList();

        return ClubResponseDTO.ClubDetailResponseDTO.builder()
                .clubId(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .profileImageUrl(club.getProfileImgUrl())
                .open(club.isOpen())
                .category(categoryNames)
                .region(club.getRegion())
                .participantTypes(club.getParticipantTypes())
                .insta(club.getInsta())
                .kakao(club.getKakao())
                .isStaff(isStaff)
                .build();
    }

    /**
     * Club 엔티티 + 카테고리 이름 리스트 → ClubDetailResponseDTO 변환 (효율적 버전)
     */
    public static ClubResponseDTO.ClubDetailResponseDTO fromClubToResponseDTOWithCategoryNames(
            Club club, List<String> categoryNames, boolean isStaff) {

        return ClubResponseDTO.ClubDetailResponseDTO.builder()
                .clubId(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .profileImageUrl(club.getProfileImgUrl())
                .open(club.isOpen())
                .category(categoryNames)
                .region(club.getRegion())
                .participantTypes(club.getParticipantTypes())
                .insta(club.getInsta())
                .kakao(club.getKakao())
                .isStaff(isStaff)
                .build();
    }

    /**
     * ClubResponseDTO.ClubNoticeListDTO 변환
     */
    public static ClubResponseDTO.ClubNoticeListDTO toClubNoticeListDTO(
            List<ClubResponseDTO.NoticeItem> noticeItems,
            boolean hasNext,
            Long nextCursor,
            boolean isStaff
    ) {
        List<ClubResponseDTO.NoticeItem> safeList =
                (noticeItems == null) ? List.of() : List.copyOf(noticeItems);

        return ClubResponseDTO.ClubNoticeListDTO.builder()
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
    public static ClubResponseDTO.ClubNoticeWithClubDTO toClubNoticeWithClubDTO(Notice notice, ClubResponseDTO.NoticeItem noticeItemDTO) {
        var club = notice.getClub();
        if (club == null) {
            // 클럽 정보가 아예 없을 경우 null 처리
            return ClubResponseDTO.ClubNoticeWithClubDTO.builder()
                    .clubId(null)
                    .clubName(null)
                    .notice(noticeItemDTO)
                    .build();
        }
        return ClubResponseDTO.ClubNoticeWithClubDTO.builder()
                .clubId(club.getId())
                .clubName(club.getName())
                .notice(noticeItemDTO)
                .build();
    }

    /**
     * ClubResponseDTO.VoteDTO -> ClubResponseDTO.ClubNoticeWithClubDTO
     */
    public static ClubResponseDTO.ClubNoticeWithClubDTO toClubNoticeWithClubDTO(Vote vote, ClubResponseDTO.VoteDTO voteDTO) {
        return ClubResponseDTO.ClubNoticeWithClubDTO.builder()
                .clubId(vote.getClub().getId())
                .clubName(vote.getClub().getName())
                .notice(voteDTO)
                .build();
    }

    /**
     * ClubResponseDTO.MemberNoticeListDTO 변환
     */
    public static ClubResponseDTO.MemberNoticeListDTO toMemberNoticeListDTO(
            List<ClubResponseDTO.ClubNoticeWithClubDTO> memberNoticeItems,
            boolean hasNext,
            Long nextCursor
    ) {
        List<ClubResponseDTO.ClubNoticeWithClubDTO> safeList =
                (memberNoticeItems == null) ? List.of() : List.copyOf(memberNoticeItems);

        return ClubResponseDTO.MemberNoticeListDTO.builder()
                .noticeList(safeList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .pageSize(safeList.size())
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
                .title(request.getTitle())
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
            BookSharedDTO.BasicInfo bookInfo,
            MemberSharedDTO.BasicInfo authorInfo,
            String currentMemberNickname,
            boolean isStaff
    ) {
        return ClubResponseDTO.BookRecommendDetailDTO.builder()
                .id(bookRecommend.getId())
                .title(bookRecommend.getTitle())
                .content(bookRecommend.getContent())
                .rate(bookRecommend.getRate())
                .tag(bookRecommend.getTag())
                .bookInfo(bookInfo)
                .authorInfo(authorInfo)
                .isAuthor(authorInfo.getNickname().equals(currentMemberNickname))
                .isStaff(isStaff)
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
     * 투표 항목 리스트 → EachItemDTO 리스트 변환
     * 기본값: isSelected = false, voteCount = 0, votedMembers = 빈 리스트
     */
    public static List<ClubResponseDTO.EachItemDTO> toEachItemDTOListFromItems(List<String> items) {
        return items.stream()
                .map(item -> ClubResponseDTO.EachItemDTO.builder()
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
            ClubRequestDTO.VoteResultDTO request
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
            List<MemberSharedDTO.BasicInfo> votedMembers
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
     * TopicDTO -> Topic 엔티티 변환
     */
    public static Topic fromTopicDTOToTopic(
            BookShelfRequestDTO.TopicDTO topicDTO
    ) {
        return Topic.builder()
                .description(topicDTO.getDescription())
                .build();
    }

    /**
     * Topic 리스트 + 작성자 정보 맵 + 회원 ID -> List<BookShelfResponseDTO.TopicDTO> 변환
     */
    public static List<BookShelfResponseDTO.TopicDTO> fromTopicListAndAuthorInfoMapAndMemberIdToTopicDTOList(
            List<Topic> topics,
            Map<String, MemberSharedDTO.BasicInfo> authorInfoMap,
            String memberId
    ) {
        return topics.stream()
                .map(topic -> fromTopicAndMemberSharedDTOToTopicDTO(
                        topic,
                        authorInfoMap.get(topic.getClubMember().getMemberId()),
                        memberId
                ))
                .toList();
    }

    /**
     * Notice 엔티티 + BookSharedDTO.BasicInfoDTO -> ClubResponseDTO.MeetingNoticeDTO 변환
     */
    public static ClubResponseDTO.MeetingNoticeDTO toMeetingNoticeDTO(Notice notice, BookSharedDTO.BasicInfo bookInfo) {
        return ClubResponseDTO.MeetingNoticeDTO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .important(notice.isImportant())
                .tag(notice.getTag())
                .meetingInfoDTO(fromMeetingAndBookSharedDTOToMeetingInfoDTO(notice.getMeeting(), bookInfo))
                .build();
    }

    /**
     * Meeting 엔티티 + BookSharedDTO.BasicInfoDTO -> BookShelfResponseDTO.BookShelfInfoDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfInfoDTO fromMeetingAndBookSharedDTOToBookShelfInfoDTO(
            Meeting meeting,
            BookSharedDTO.BasicInfo bookSharedDTO
    ) {
        BookShelfResponseDTO.MeetingInfoDTO meetingInfoDTO = fromMeetingToBookshelfMeetingInfoDTO(meeting);

        return BookShelfResponseDTO.BookShelfInfoDTO.builder()
                .meetingInfo(meetingInfoDTO)
                .bookInfo(bookSharedDTO)
                .build();
    }

    /**
     * Meeting 엔티티 -> BookShelfResponseDTO.MeetingInfoDTO 변환
     */
    public static BookShelfResponseDTO.MeetingInfoDTO fromMeetingToBookshelfMeetingInfoDTO(Meeting meeting) {
        return BookShelfResponseDTO.MeetingInfoDTO.builder()
                .meetingId(meeting.getId())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .averageRate(meeting.calculateAverageRate())
                .build();
    }

    /**
     * Meeting 엔티티 + BookSharedDTO.DetailInfoDTO + BookShelfResponseDTO.TopicListDTO -> BookShelfResponseDTO.BookShelfDetailDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfDetailDTO fromBookShelfDTOToBookShelfDetailDTO(
            Meeting meeting,
            BookSharedDTO.DetailInfo bookSharedDTO,
            BookShelfResponseDTO.TopicListDTO topicListDTO,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.BookShelfDetailDTO.builder()
                .meetingInfo(fromMeetingToBookshelfMeetingInfoDTO(meeting))
                .bookDetailInfo(bookSharedDTO)
                .topicList(topicListDTO)
                .membership(membershipDTO)
                .build();
    }

    /**
     * Meeting 엔티티 + BooksharedDTO -> MeetingInfoDTO 변환
     */
    public static MeetingResponseDTO.MeetingInfoDTO fromMeetingAndBookSharedDTOToMeetingInfoDTO(
            Meeting meeting,
            BookSharedDTO.BasicInfo bookInfo
    ) {
        return MeetingResponseDTO.MeetingInfoDTO.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .meetingTime(meeting.getMeetingTime())
                .location(meeting.getLocation())
                .content(meeting.getContent())
                .generation(meeting.getGeneration())
                .tag(meeting.getTag())
                .bookInfo(bookInfo)
                .build();
    }

    /**
     * List<Meeting> -> List<MeetingResponseDTO.MeetingInfoDTO> 변환
     */
    public static MeetingResponseDTO.CalendarMeetingDTO fromMeetingListToMCalendarMeetingDTO(
            List<Meeting> meetings,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.CalendarMeetingDTO.builder()
                .meetingInfoList(meetings.stream()
                        .map(meeting -> fromMeetingAndBookSharedDTOToMeetingInfoDTO(meeting, null))
                        .toList())
                .membership(membershipDTO)
                .build();
    }

    /**
     * Topic 엔티티 + MemberSharedDTO.BasicInfoDTO + 팀 번호 리스트 -> MeetingResponseDTO.TopicDTO 변환
     */
    public static MeetingResponseDTO.TopicDTO fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
            Topic topic,
            MemberSharedDTO.BasicInfo authorSharedDTO,
            List<Integer> teamNumbers
    ) {
        return MeetingResponseDTO.TopicDTO.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorSharedDTO)
                .teamNumbers(teamNumbers)
                .build();
    }

    /**
     * MemberSharedDTO.BasicInfoDTO + teamNumber -> MeetingResponseDTO.MeetingMemberDTO 변환
     */
    public static MeetingResponseDTO.MeetingMemberDTO fromMemberSharedDTOAndTeamNumberToMeetingMemberDTO(
            MemberSharedDTO.BasicInfo memberSharedDTO,
            Integer teamNumber
    ) {
        return MeetingResponseDTO.MeetingMemberDTO.builder()
                .memberInfo(memberSharedDTO)
                .teamNumber(teamNumber)
                .build();
    }

    /**
     * Meeting 엔티티 + BookSharedDTO.BasicInfoDTO + Topic 리스트 + 팀별 Topic 리스트 -> MeetingResponseDTO.MeetingDetailDTO 변환
     */
    public static MeetingResponseDTO.MeetingDetailDTO fromMeetingAndBookSharedDTOEtcToMeetingDetailDTO(
            Meeting meeting,
            BookSharedDTO.BasicInfo bookSharedDTO,
            List<Topic> topics,
            Map<Long, List<Integer>> topicIdToSelectTeamNumbers,
            List<Team> teams,
            Map<Integer, List<TeamTopic>> teamTopicsGroupingByTeamNumber,
            Map<String, MemberSharedDTO.BasicInfo> authorInfoMap,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        MeetingResponseDTO.MeetingInfoDTO meetingInfoDTO = ClubConverter.fromMeetingAndBookSharedDTOToMeetingInfoDTO(meeting, bookSharedDTO);

        List<MeetingResponseDTO.TopicDTO> topicDTOList = fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(topics, authorInfoMap, topicIdToSelectTeamNumbers);

        List<MeetingResponseDTO.TeamTopicDTO> teamTopicDTOList = teams.stream()
                .sorted(Comparator.comparing(Team::getTeamNumber)) // 팀 번호 기준 정렬
                .map(team -> {
                    List<TeamTopic> teamTopics =
                            teamTopicsGroupingByTeamNumber.get(team.getTeamNumber());

                    List<MeetingResponseDTO.TopicDTO> teamTopicDTOs = teamTopics.stream()
                            .map(tt -> ClubConverter.fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
                                    tt.getTopic(),
                                    authorInfoMap.get(tt.getTopic().getClubMember().getMemberId()),
                                    null // TeamTopicDTO-TopicDTO에서는 teamNumbers 필드가 NULL이어야 함
                            ))
                            .toList();

                    return fromTopicDTOListToTeamTopicDTO(team.getTeamNumber(), teamTopicDTOs, null);
                })
                .toList();
        return fromMeetingInfoDTOAndTopicDTOListAndTeamTopicDTOListToTopicDTO(
                meetingInfoDTO,
                topicDTOList,
                teamTopicDTOList,
                membershipDTO
        );
    }

    /**
     * Topic 리스트 + Topic별 팀 선택 정보 + 작성자 정보 맵 -> List<MeetingResponseDTO.TopicDTO> 변환
     */
    public static List<MeetingResponseDTO.TopicDTO> fromTopicListAndTopicSelectionAndMemberSharedDTOToTopicDTOList(
            List<Topic> topics,
            Map<String, MemberSharedDTO.BasicInfo> authorInfoMap,
            Map<Long, List<Integer>> topicIdToSelectTeamNumbers
    ) {
        return topics.stream()
                .map(topic -> ClubConverter.fromTopicAndMemberSharedDTOAndTeamNumberListToTopicDTO(
                        topic,
                        authorInfoMap.get(topic.getClubMember().getMemberId()),
                        topicIdToSelectTeamNumbers.getOrDefault(topic.getId(), List.of())
                ))
                .toList();
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

    /**
     * BookReview 엔티티 + MemberSharedDTO -> BookReviewDTO 변환
     */
    public static BookShelfResponseDTO.BookReviewDTO fromBookReviewAndMemberSharedDTOToBookReviewDTO(
            BookReview bookReview,
            MemberSharedDTO.BasicInfo memberSharedDTO
    ) {
        return BookShelfResponseDTO.BookReviewDTO.builder()
                .bookReviewId(bookReview.getId())
                .description(bookReview.getDescription())
                .rate(bookReview.getRate())
                .authorInfo(memberSharedDTO)
                .build();
    }

    /**
     * BookReviewDTO 리스트 -> BookReviewListDTO 변환
     */
    public static BookShelfResponseDTO.BookReviewListDTO fromBookReviewDTOListToBookReviewListDTO(
            List<BookShelfResponseDTO.BookReviewDTO> bookReviewList,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.BookReviewListDTO.builder()
                .bookReviewList(bookReviewList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<TopicDTO> -> BookShelfResponseDTO.TopicListDTO 변환
     */
    public static BookShelfResponseDTO.TopicListDTO fromTopicDTOListToTopicListDTOForBookshelf(
            List<BookShelfResponseDTO.TopicDTO> topicListDTOs,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.TopicListDTO.builder()
                .topics(topicListDTOs)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<BookShelfInfoDTO> -> BookShelfListDTO 변환
     */
    public static BookShelfResponseDTO.BookShelfListDTO fromBookShelfInfoDTOListToBookShelfListDTO(
            List<BookShelfResponseDTO.BookShelfInfoDTO> bookShelfInfoDTOs,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return BookShelfResponseDTO.BookShelfListDTO.builder()
                .bookShelfInfoList(bookShelfInfoDTOs)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<ClubSharedDTO.MyClubInfo> -> ClubSharedDTO.MyClubList 변환
     */
    public static ClubSharedDTO.MyClubList fromClubInfoListToMyClubList(
            List<ClubSharedDTO.MyClubInfo> clubInfoList
    ) {
        return ClubSharedDTO.MyClubList.builder()
                .clubList(clubInfoList)
                .build();
    }

    /**
     * List<MeetingResponseDTO.MeetingInfoDTO> -> MeetingListDTO 변환
     */
    public static MeetingResponseDTO.MeetingListDTO fromMeetingInfoDTOListToMeetingListDTO(
            List<MeetingResponseDTO.MeetingInfoDTO> meetingInfoDTOList,
            boolean hasNext,
            Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.MeetingListDTO.builder()
                .meetingInfoList(meetingInfoDTOList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<MeetingResponseDTO.TopicDTO> -> MeetingResponseDTO.TopicListDTO 변환
     */
    public static MeetingResponseDTO.TopicListDTO fromTopicDTOListToTopicListDTOForMeeting(
            List<MeetingResponseDTO.TopicDTO> topicList,
            boolean hasNext,
            Long nextCursor
    ) {
        return MeetingResponseDTO.TopicListDTO.builder()
                .topics(topicList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * List<MeetingResponseDTO.TopicDTO> -> MeetingResponseDTO.TeamTopicDTO 변환
     */
    public static MeetingResponseDTO.TeamTopicDTO fromTopicDTOListToTeamTopicDTO(
            Integer teamNumber,
            List<MeetingResponseDTO.TopicDTO> topicList,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.TeamTopicDTO.builder()
                .teamNumber(teamNumber)
                .topics(topicList)
                .membership(membershipDTO)
                .build();
    }

    /**
     * MeetingResponseDTO.MeetingInfoDTO + List<TopicDTO> + List<TeamTopicDTO> -> MeetingResponseDTO.MeetingDetailDTO 변환
     */
    public static MeetingResponseDTO.MeetingDetailDTO fromMeetingInfoDTOAndTopicDTOListAndTeamTopicDTOListToTopicDTO(
            MeetingResponseDTO.MeetingInfoDTO meetingInfoDTO,
            List<MeetingResponseDTO.TopicDTO> topicDTOList,
            List<MeetingResponseDTO.TeamTopicDTO> teamTopicDTOList,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.MeetingDetailDTO.builder()
                .meetingInfo(meetingInfoDTO)
                .topics(topicDTOList)
                .teams(teamTopicDTOList)
                .membership(membershipDTO)
                .build();
    }

    public static MeetingResponseDTO.TopicDTOList fromTopicDTOListAndMembershipDTOToTopicListDTO(
            List<MeetingResponseDTO.TopicDTO> topicDTOs,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.TopicDTOList.builder()
                .topics(topicDTOs)
                .membership(membershipDTO)
                .build();
    }

    /**
     * 팀 번호 + List<MemberSharedDTO> -> MeetingResponseDTO.TeamMemberDTO 변환
     */
    public static MeetingResponseDTO.TeamMemberDTO fromTeamNumberAndMemberSharedDTOToTeamMemberDTO(
            Integer teamNumber,
            List<MemberSharedDTO.BasicInfo> memberSharedDTOs,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.TeamMemberDTO.builder()
                .teamNumber(teamNumber)
                .members(memberSharedDTOs)
                .membership(membershipDTO)
                .build();
    }

    /**
     * List<MeetingResponseDTO.MeetingMemberDTO> -> MeetingResponseDTO.MeetingMemberListDTO 변환
     */
    public static MeetingResponseDTO.MeetingMemberListDTO fromMeetingMemberDTOListToMeetingMemberListDTO(
            List<MeetingResponseDTO.MeetingMemberDTO> meetingMemberDTOList,
            boolean hasNext, Long nextCursor,
            MembershipResponseDTO.MembershipDTO membershipDTO
    ) {
        return MeetingResponseDTO.MeetingMemberListDTO.builder()
                .members(meetingMemberDTOList)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .membership(membershipDTO)
                .build();
    }

    // =====================================================
    // Parameter ->  DTO 변환
    // =====================================================

    /**
     * 파라미터 -> MeetingResponseDTO.TopicSelectionDTO 변환
     */
    public static MeetingResponseDTO.TopicSelectionDTO fromParametersToTopicSelectionDTO(Long topicId, Integer teamNumber, Boolean isSelected) {
        return MeetingResponseDTO.TopicSelectionDTO.builder()
                .topicId(topicId)
                .teamNumber(teamNumber)
                .isSelected(isSelected)
                .build();
    }

    // =====================================================
    // Private Methods
    // =====================================================

    /**
     * Topic 엔티티 -> BookShelfResponseDTO.TopicDTO 변환
     */
    private static BookShelfResponseDTO.TopicDTO fromTopicAndMemberSharedDTOToTopicDTO(
            Topic topic,
            MemberSharedDTO.BasicInfo authorSharedDTO,
            String memberId
    ) {
        return BookShelfResponseDTO.TopicDTO.builder()
                .topicId(topic.getId())
                .content(topic.getDescription())
                .authorInfo(authorSharedDTO)
                .isAuthor(topic.getClubMember().getMemberId().equals(memberId))
                .build();
    }

    // =====================================================
    // ClubCategory 관련 변환
    // =====================================================

    /**
     * List<ClubCategory> → CategoryInfoList 변환
     */
    public static Map<Long, List<CategorySharedDTO.CategoryInfo>> fromClubCategoriesToCategoryInfoListMap(
            List<ClubCategory> allClubCategories
    ) {
        return allClubCategories.stream()
                .collect(Collectors.groupingBy(
                        ClubCategory::getClubId,
                        Collectors.mapping(cc -> CategorySharedDTO.CategoryInfo.builder()
                                        .id(cc.getCategory().getId())
                                        .name(cc.getCategory().getName())
                                        .build(),
                                Collectors.toList())
                ));
    }

    /**
     * List<ClubCategory> → 클럽별 카테고리 이름 Map 변환
     */
    public static Map<Long, List<String>> fromClubCategoriesToCategoryNamesMap(
            List<ClubCategory> allClubCategories
    ) {
        return allClubCategories.stream()
                .collect(Collectors.groupingBy(
                        ClubCategory::getClubId,
                        Collectors.mapping(cc -> cc.getCategory().getName(), Collectors.toList())
                ));
    }

    /**
     * List<ClubCategory> → 클럽별 카테고리 ID Map 변환
     */
    public static Map<Long, List<Long>> fromClubCategoriesToCategoryIdMap(
            List<ClubCategory> allClubCategories
    ) {
        return allClubCategories.stream()
                .collect(Collectors.groupingBy(
                        ClubCategory::getClubId,
                        Collectors.mapping(ClubCategory::getCategoryId, Collectors.toList())
                ));
    }
}