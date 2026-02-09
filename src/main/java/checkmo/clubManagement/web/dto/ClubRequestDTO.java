package checkmo.clubManagement.web.dto;

import checkmo.clubManagement.internal.entity.ClubInterestCategory;
import checkmo.clubManagement.internal.entity.ClubMemberStatus;
import checkmo.clubManagement.internal.entity.ClubParticipantType;
import checkmo.clubManagement.internal.excepetion.ClubManagementErrorStatus;
import checkmo.clubManagement.internal.excepetion.ClubManagementException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.EnumSet;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ClubRequestDTO {

    public enum ClubSearchInputFilter {
        NAME,
        REGION;
    }

    public enum ClubSearchOutputFilter {
        ALL,
        STUDENT,
        WORKER,
        ONLINE,
        CLUB,
        MEETING,
        OFFLINE;

        public ClubParticipantType toClubParticipantTypeOrNull() {
            return switch (this) {
                case ALL -> null;
                case STUDENT -> ClubParticipantType.STUDENT;
                case WORKER -> ClubParticipantType.WORKER;
                case ONLINE -> ClubParticipantType.ONLINE;
                case CLUB -> ClubParticipantType.CLUB;
                case MEETING -> ClubParticipantType.MEETING;
                case OFFLINE -> ClubParticipantType.OFFLINE;
            };
        }
    }

    public enum ClubMemberStatusFilter {
        ALL,
        ACTIVE,
        MEMBER,
        STAFF,
        OWNER,
        PENDING,
        WITHDRAWN,
        KICKED;

        public EnumSet<ClubMemberStatus> toClubMemberStatusesOrNull() {
            return switch (this) {
                case ALL -> null;
                case ACTIVE -> ClubMemberStatus.activeStatuses();
                case MEMBER -> EnumSet.of(ClubMemberStatus.MEMBER);
                case STAFF -> EnumSet.of(ClubMemberStatus.STAFF);
                case OWNER -> EnumSet.of(ClubMemberStatus.OWNER);
                case PENDING -> EnumSet.of(ClubMemberStatus.PENDING);
                case WITHDRAWN -> EnumSet.of(ClubMemberStatus.WITHDRAWN);
                case KICKED -> EnumSet.of(ClubMemberStatus.KICKED);
            };
        }
    }

    public enum ClubMemberStatusUpdateCommand {
        APPROVE,
        REJECT,

        CHANGE_ROLE,
        TRANSFER_OWNER,

        KICK,
    }

    public record ClubSearchFilter(
            @Schema(description = "검색하려는 키워드", example = "북", maxLength = 40)
            String keyword,
            @Schema(description = """
                    입력 필터(독서모임을 어떤 것으로 검색할지 결정)
                    - NAME: 모임명 기준 검색
                    - REGION: 지역명 기준 검색
                    """,
                    example = "NAME")
            ClubSearchInputFilter inputFilter,
            @Schema(description = """
                    출력 필터(검색 결과로 어떤 모임을 받을지 결정)
                    - ALL: 모든 모임
                    - STUDENT: 대학생 대상 모임
                    - WORKER: 직장인 대상 모임
                    - ONLINE: 온라인 모임
                    - CLUB: 동아리 모임
                    - MEETING: 소규모 모임
                    - OFFLINE: 오프라인 모임
                    """,
                    example = "ALL")
            ClubSearchOutputFilter outputFilter
    ) {
        public ClubSearchFilter {
            keyword = (keyword == null) ? "" : keyword.trim();
            if (keyword.length() > 40) {
                throw new ClubManagementException(ClubManagementErrorStatus.CLUB_SEARCH_KEYWORD_TOO_LONG);
            }
            inputFilter = (inputFilter == null) ? ClubSearchInputFilter.NAME : inputFilter;
            outputFilter = (outputFilter == null) ? ClubSearchOutputFilter.ALL : outputFilter;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ClubMemberStatusAction {
        @NotNull
        @Schema(description = """
                수행할 명령어
                - APPROVE: 가입 승인 (PENDING -> MEMBER)
                - REJECT: 가입 거절 (PENDING 삭제)
                - CHANGE_ROLE: 회원/운영진 역할 변경 (MEMBER <-> STAFF)
                - TRANSFER_OWNER: 모임 소유권 이전 (actor: OWNER -> STAFF, target: MEMBER/STAFF -> OWNER)
                - KICK: 강제 탈퇴 (MEMBER/STAFF -> KICKED)
                """)
        ClubMemberStatusUpdateCommand command;
        @Schema(description = "변경할 상태 (CHANGE_ROLE일 때만 필요, 다른 command에서는 무시됨)")
        ClubMemberStatus status; // CHANGE_ROLE일 때만 필요

        @AssertTrue(message = "CHANGE_ROLE 요청에서는 status는 MEMBER 또는 STAFF만 허용됩니다.")
        private boolean isValidStatusWhenChangeRole() {
            if (command != ClubMemberStatusUpdateCommand.CHANGE_ROLE) {
                return true;
            }
            return status == ClubMemberStatus.MEMBER || status == ClubMemberStatus.STAFF;
        }

        @AssertTrue(message = "CHANGE_ROLE 요청에서는 status가 필수입니다.")
        private boolean isStatusPresentWhenChangeRole() {
            if (command != ClubMemberStatusUpdateCommand.CHANGE_ROLE) {
                return true;
            }
            return status != null;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class JoinClub {
        @NotNull(message = "가입 메시지는 null일 수 없습니다.")
        @Size(max = 300, message = "가입 메시지는 300자 이하로 입력해주세요.")
        private String joinMessage;
    }

    @Getter
    @NoArgsConstructor
    public static class ClubDetail {
        @NotBlank(message = "클럽 이름은 필수 입력입니다.")
        @Size(min = 1, max = 40, message = "클럽 이름은 1자 이상 40자 이하로 입력해주세요.")
        private String name;
        @NotBlank(message = "클럽 설명은 필수 입력입니다.")
        @Size(max = 500, message = "클럽 설명은 500자 이하로 입력해주세요.")
        private String description;
        @Size(max = 255, message = "프로필 이미지 URL은 255자 이하로 입력해주세요.")
        private String profileImageUrl;
        private boolean isOpen;
        @NotBlank(message = "활동 지역은 필수 입력입니다.")
        @Size(max = 40, message = "활동 지역은 40자 이하로 입력해주세요.")
        private String region;
        @NotNull(message = "관심 카테고리는 null일 수 없습니다.")
        @Size(min = 1, max = 6, message = "관심 카테고리는 1개 이상 6개 이하로 선택해주세요.")
        private List<ClubInterestCategory> category;
        @NotNull(message = "활동 대상은 null일 수 없습니다.")
        @Size(min = 1, max = 6, message = "활동 대상은 1개 이상 6개 이하로 선택해주세요.")
        private List<ClubParticipantType> participantTypes;
        @Size(max = 4, message = "컨택 정보는 4개 이하로 입력해주세요.")
        @Valid
        private List<Contact> links;
    }

    @Getter
    @NoArgsConstructor
    public static class Contact {
        @NotBlank(message = "링크는 필수입니다.")
        @Size(max = 100, message = "링크는 100자 이하로 입력해주세요.")
        private String link;
        @Size(max = 20, message = "대체 텍스트는 20자 이하로 입력해주세요.")
        private String label;
    }
}
