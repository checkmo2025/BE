package checkmo.domain.club.entity;

import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.club.entity.announcement.Vote;
import checkmo.domain.club.entity.meeting.Meeting;
import checkmo.domain.club.web.dto.club.ClubRequestDTO;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Club extends BaseEntity {

    public enum ParticipantType {
        STUDENT("대학생"),
        WORKER("직장인"),
        ONLINE("온라인"),
        CLUB("동아리"),
        MEETING("모임"),
        OFFLINE("대면");

        private final String description;

        ParticipantType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String profileImgUrl;

    @Column(nullable = false)
    private boolean open;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "club_participants", joinColumns = @JoinColumn(name = "club_id"))
    @Column(name = "participant_type")
    private List<ParticipantType> participantTypes = new ArrayList<>();

    private String region;

    private String insta;

    private String kakao;

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<ClubMember> clubMembers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<ClubCategory> clubCategories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.REMOVE)
    private List<Meeting> meetings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();

    public void addMeeting(Meeting meeting) {
        this.meetings.add(meeting);
        meeting.setClub(this);
    }

    public void addClubMember(ClubMember clubMember) {
        this.clubMembers.add(clubMember);
        clubMember.setClub(this);
    }

    public void updateFromDetailDTO(ClubRequestDTO.ClubDetailDTO dto) {
        if (dto.getName() != null) {
            this.name = dto.getName();
        }
        if (dto.getDescription() != null) {
            this.description = dto.getDescription();
        }
        if (dto.getProfileImageUrl() != null) {
            this.profileImgUrl = dto.getProfileImageUrl();
        }

        // open 필드는 수정 불가 → 반영하지 않음

        if (dto.getParticipantTypes() != null) {
            this.participantTypes.clear();
            this.participantTypes.addAll(dto.getParticipantTypes());
        }
        if (dto.getRegion() != null) {
            this.region = dto.getRegion();
        }
        if (dto.getInsta() != null) {
            this.insta = dto.getInsta();
        }
        if (dto.getKakao() != null) {
            this.kakao = dto.getKakao();
        }
    }

}
