package checkmo.member.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
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
public class Member extends BaseEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickName;

    @Column(length = 20, nullable = false)
    private String description;

    private String imgUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime deactivated;

    private boolean isProfileCompleted;

    @Builder.Default
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL)
    private List<Follow> followers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL)
    private List<Follow> followings = new ArrayList<>();

    // 회원 관심 카테고리 (ENUM으로 관리)
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_interest_categories",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Set<MemberInterestCategory> interestCategories = new HashSet<>();

    // 프로필 추가 정보 업데이트
    public void updateAdditionalInfo(String nickName, String description, String imgUrl) {
        this.nickName = nickName != null ? nickName : "";
        this.description = description != null ? description : "";
        this.imgUrl = imgUrl != null ? imgUrl : "";
    }

    // 프로필 완료 상태 업데이트
    public void completeProfile() {
        this.isProfileCompleted = true;
    }

    // 프로필 수정
    public void updateProfile(String description, String imgUrl) {
        this.description = description != null ? description : "";
        this.imgUrl = imgUrl != null ? imgUrl : "";
    }

    // 관심 카테고리 업데이트
    public void updateInterestCategories(Set<MemberInterestCategory> newCategories) {
        this.interestCategories.clear();
        if (newCategories != null) {
            this.interestCategories.addAll(newCategories);
        }
    }

    public enum Role {
        USER, ADMIN
    }
}
