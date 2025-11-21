package checkmo.member.internal.service.query;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.repository.projection.MemberIdAndNicknameProjection;
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

    private final MemberRepository memberRepository;

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickName(nickname);
    }

    @Override
    public Member getMemberBasicInfo(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Member getMemberProfile(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public List<MemberBasicInfoProjection> getMemberBasicInfoMapForShare(List<String> memberIds) {
        return memberRepository.findIdNicknameAndImgUrlByIdIn(memberIds);
    }

    @Override
    public Member getOtherProfile(String targetMemberNickname) {
        return memberRepository.findByNickName(targetMemberNickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public String getMemberIdByNickname(String nickname) {
        return memberRepository.findIdByNickName(nickname)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<String, String> getMemberIdsByNicknames(List<String> nicknames) {
        List<MemberIdAndNicknameProjection> results = memberRepository.findNicknameAndIdByNicknameIn(nicknames);
        return results.stream()
                .collect(Collectors.toMap(
                        MemberIdAndNicknameProjection::getNickName, // key: nickname
                        MemberIdAndNicknameProjection::getId      // value: memberId
                ));
    }

    @Override
    public String getMemberNicknameById(String memberId) {
        return memberRepository.findNicknameById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public Map<String, String> getMemberNicknamesByMemberIds(List<String> memberIds) {
        List<MemberIdAndNicknameProjection> results = memberRepository.findIdAndNicknameByIdIn(memberIds);
        return results.stream()
                .collect(Collectors.toMap(
                        MemberIdAndNicknameProjection::getId,       // key: memberId
                        MemberIdAndNicknameProjection::getNickName  // value: nickname
                ));
    }

    @Override
    public List<MemberBasicInfoProjection> getMemberNicknamesAndProfileImagesByMemberIds(List<String> memberIds) {
        return memberRepository.findIdNicknameAndImgUrlByIdIn(memberIds);
    }
}
