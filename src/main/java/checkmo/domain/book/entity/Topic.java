package checkmo.domain.book.entity;

import checkmo.domain.club.entity.MemberTeam;
import checkmo.domain.team.entity.TeamTopic;
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
public class Topic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_team_id")
    private MemberTeam memberTeam;

    @Builder.Default
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<TeamTopic> teamTopics = new ArrayList<>();
}
