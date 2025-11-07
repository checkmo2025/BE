package checkmo.member.service.query;

import checkmo.common.apiPayload.code.status.ErrorStatus;
import checkmo.common.apiPayload.exception.GeneralException;
import checkmo.member.entity.Member;
import checkmo.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {

    // 자신의 Repository
    private final MemberRepository memberRepository;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickName(nickname);
    }

    /**
     * 회원 기본 정보 조회
     *
     * @param memberId 회원 ID
     * @return 회원 기본 정보 DTO
     */
    @Override
    public Member getMemberBasicInfo(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Member getMemberProfile(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public List<Object[]> getMemberBasicInfoMapForShare(List<String> memberIds) {
        return memberRepository.findIdNicknameAndImgUrlByIdIn(memberIds);
    }

    @Override
    public Member getOtherProfile(String targetMemberNickname) {
        return memberRepository.findByNickName(targetMemberNickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public String getMemberIdByNickname(String nickname) {
        return memberRepository.findIdByNickName(nickname)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<String, String> getMemberIdsByNicknames(List<String> nicknames) {
        List<Object[]> results = memberRepository.findNicknameAndIdByNicknameIn(nicknames);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // key: nickname
                        row -> (String) row[1]  // value: memberId
                ));
    }

    @Override
    public String getMemberNicknameById(String memberId) {
        return memberRepository.findNicknameById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds) {
        var results = memberRepository.findIdAndNicknameByIdIn(memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // memberId
                        row -> (String) row[1]  // nickname
                ));
    }

    @Override
    public List<Object[]> getMemberNicknamesAndProfileImagesByMemberIds(List<String> memberIds) {
        return memberRepository.findIdNicknameAndImgUrlByIdIn(memberIds);
    }
}
