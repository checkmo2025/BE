package checkmo.member.entity;

import checkmo.bookStory.entity.BookStory;
import checkmo.bookStory.entity.BookStoryLiked;
import checkmo.bookStory.entity.Comment;
import checkmo.clubManagement.entity.ClubMember;
import checkmo.clubNotice.entity.MemberVote;
import checkmo.common.BaseEntity;
import checkmo.notification.entity.Notification;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<BookStory> bookStories = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<BookStoryLiked> bookStoryLikedList = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    private List<Notification> receivedNotifications = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberCategory> memberCategories = new ArrayList<>();

    // 사용자 입장에서 자신이 보낸(sender) 알림은 필요하지 않으므로 제외
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ClubMember> clubMembers = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberVote> memberVotes = new ArrayList<>();

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

    public enum Role {
        USER, ADMIN
    }
}
