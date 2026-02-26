package checkmo.member.internal.service.command;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberReport;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberReportRepository;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.web.dto.MemberRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberReportCommandService {

    private final MemberRepository memberRepository;
    private final MemberReportRepository memberReportRepository;

    public Long createReport(String reporterId, MemberRequestDTO.CreateReport request) {
        Member reporter = memberRepository.findByIdAndDeactivatedAtIsNull(reporterId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        Member reportedMember = memberRepository.findByNickName(request.getReportedMemberNickname())
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (reporter.getId().equals(reportedMember.getId())) {
            throw new MemberException(MemberErrorStatus.CANNOT_REPORT_SELF);
        }

        MemberReport report = MemberReport.builder()
                .reporter(reporter)
                .reportedMember(reportedMember)
                .reportType(request.getReportType())
                .content(request.getContent())
                .build();

        MemberReport savedReport = memberReportRepository.save(report);
        return savedReport.getId();
    }
}
