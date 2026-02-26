package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberReport;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {

    @EntityGraph(attributePaths = {"reportedMember"})
    List<MemberReport> findByReportedMemberNickName(String nickName);

    @Modifying
    @Query("DELETE FROM MemberReport mr WHERE mr.reporter.id = :memberId OR mr.reportedMember.id = :memberId")
    void deleteAllByMemberId(String memberId);
}
