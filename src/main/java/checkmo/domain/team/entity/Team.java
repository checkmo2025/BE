package checkmo.domain.team.entity;

import checkmo.domain.club.entity.Meeting;
import checkmo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;


    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<TeamTopic> teamTopics;
}
