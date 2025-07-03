package checkmo.domain.member.entity;

import checkmo.domain.follow.entity.Follow;
import checkmo.domain.member.entity.enums.Role;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL)
    private List<Follow> followers;

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL)
    private List<Follow> followings;
}
