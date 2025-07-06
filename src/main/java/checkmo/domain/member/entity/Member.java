package checkmo.domain.member.entity;

import checkmo.domain.bookStory.entity.BookStory;
import checkmo.domain.bookStory.entity.BookStoryLiked;
import checkmo.domain.bookStory.entity.Comment;
import checkmo.domain.category.entity.MemberCategory;
import checkmo.domain.club.entity.ClubMember;
import checkmo.domain.notification.entity.Notification;
import checkmo.domain.club.entity.announcement.MemberVote;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity {

    public enum Role {
        USER, ADMIN
    }

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

    // 사용자 입장에서 자신이 보낸(sender) 알림은 필요하지 않으므로 제외

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberCategory> memberCategories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ClubMember> clubMembers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberVote> memberVotes = new ArrayList<>();
}
