package checkmo.clubNotice.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"club_member_id", "vote_id"})
})
public class ClubMemberVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean item1;
    private boolean item2;
    private boolean item3;
    private boolean item4;
    private boolean item5;

    @Column(name = "club_member_id", nullable = false)
    private Long clubMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id")
    private Vote vote;
}
