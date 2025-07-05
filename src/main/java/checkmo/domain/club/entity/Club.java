package checkmo.domain.club.entity;

import checkmo.domain.category.entity.ClubCategory;
import checkmo.domain.vote.entity.Vote;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String profileImgUrl;

    private boolean isOpen = false;

    private String purpose;

    private String participants;

    private String insta;

    private String kakao;

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<ClubMember> clubMembers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<ClubCategory> clubCategories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<Meeting> meetings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();
}
