package checkmo.clubManagement.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Club extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String profileImgUrl;

    @Column(nullable = false)
    private boolean open;

    @Builder.Default
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
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "club_interest_categories",
            joinColumns = @JoinColumn(name = "club_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Set<ClubInterestCategory> interestCategories = new HashSet<>();

    public void addClubMember(ClubMember clubMember) {
        this.clubMembers.add(clubMember);
        clubMember.setClub(this);
    }

    public void updateField(
            String name,
            String description,
            String profileImgUrl,
            List<ParticipantType> participantTypes,
            String region,
            String insta,
            String kakao
    ) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (profileImgUrl != null) {
            this.profileImgUrl = profileImgUrl;
        }

        // open 필드는 수정 불가 → 반영하지 않음

        if (participantTypes != null) {
            this.participantTypes.clear();
            this.participantTypes.addAll(participantTypes);
        }
        if (region != null) {
            this.region = region;
        }
        if (insta != null) {
            this.insta = insta;
        }
        if (kakao != null) {
            this.kakao = kakao;
        }
    }

    public void updateInterestCategories(Set<ClubInterestCategory> categories) {
        if (categories == null) {
            return;
        }
        this.interestCategories.clear();
        this.interestCategories.addAll(categories);
    }

    public boolean isDifferent(String clubName) {
        return !this.name.equals(clubName);
    }

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

}
