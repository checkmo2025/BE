package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.MemberReport;
import checkmo.member.internal.repository.MemberReportRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReportQueryService {

    private final MemberReportRepository memberReportRepository;

    public List<MemberReport> retrieveReportsByReportedMemberNickname(String nickname) {
        return memberReportRepository.findByReportedMemberNickName(nickname);
    }
}
