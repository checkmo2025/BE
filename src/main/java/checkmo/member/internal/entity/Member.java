package checkmo.member.internal.entity;

import checkmo.common.BaseEntity;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.time.LocalDateTime;
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
@Table(
        indexes = {
                @Index(name = "idx_member_deactivated_at", columnList = "deactivated_at")
        }
)
public class Member extends BaseEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(length = 20, nullable = false)
    private String nickName;

    @Column(length = 40)
    private String description;

    private String imgUrl;

    private LocalDateTime deactivatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTerms> memberTerms = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL)
    private List<Follow> followers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL)
    private List<Follow> followings = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_interest_categories",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Set<MemberInterestCategory> interestCategories = new HashSet<>();

    public void updateAdditionalInfo(String nickName, String name, String phoneNumber, String description) {
        this.nickName = nickName != null ? nickName : "";
        this.name = name != null ? name : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.description = description;
    }

    public void updateProfile(String description, String imgUrl) {
        this.description = description != null ? description : "";
        this.imgUrl = imgUrl != null ? imgUrl : "";
    }

    public void updateInterestCategories(Set<MemberInterestCategory> newCategories) {
        this.interestCategories.clear();
        if (newCategories != null) {
            this.interestCategories.addAll(newCategories);
        }
    }

    public void updateImageAndInterestCategories(String imgUrl, Set<MemberInterestCategory> newCategories) {
        if (imgUrl != null) this.imgUrl = imgUrl;

        if (newCategories != null) {
            this.interestCategories.clear();
            this.interestCategories.addAll(newCategories);
        }
    }

    public static boolean isSameMember(String memberId1, String memberId2) {
        return memberId1.equals(memberId2);
    }

    public void verifyNotSelf(String memberId) {
        if (this.id.equals(memberId)) {
            throw new MemberException(MemberErrorStatus.MEMBER_CANNOT_FOLLOW_SELF);
        }
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void deactivate() {
        this.deactivatedAt = LocalDateTime.now();
    }

    public boolean isDeactivated() {
        return deactivatedAt != null;
    }

    public boolean isActive() {
        return deactivatedAt == null;
    }
}
