package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberReport;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {

    @EntityGraph(attributePaths = {"reportedMember"})
    List<MemberReport> findByReportedMemberNickName(String nickName);
}