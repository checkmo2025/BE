package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {
}