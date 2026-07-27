package checkmo.chatbot.internal.service;

import checkmo.member.MemberAPI;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 챗봇에 입력된 사용자 발화를 LLM에 전송하거나 DB/로그에 저장하기 전에 개인정보(PII)를 마스킹한다.
 * 원문은 어디에도 저장하지 않고, 마스킹된 텍스트만 사용하도록 반드시 이 서비스를 거친 결과만 사용한다.
 *
 * 규칙 기반 정규식 마스킹이므로 완전한 탐지를 보장하지 않는다. 특히 닉네임 마스킹은
 * 임의의 제3자 닉네임을 일반화해 탐지할 수 없어, 로그인한 본인의 닉네임만 치환하는 것으로 범위를 한정한다.
 */
@Service
@RequiredArgsConstructor
public class PiiMaskingService {

    // 주민등록번호: 하이픈 포함/미포함, 공백 없는 6자리-7자리 숫자
    private static final Pattern RESIDENT_REGISTRATION_NUMBER_PATTERN =
            Pattern.compile("\\d{6}-\\d{7}|(?<!\\d)\\d{13}(?!\\d)");

    // 전화번호: 010/011/016/017/018/019로 시작하는 국내 휴대폰 번호
    private static final Pattern PHONE_NUMBER_PATTERN =
            Pattern.compile("01[016789]-?\\d{3,4}-?\\d{4}");

    // 이메일
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");

    // "비밀번호는/비번은/pw는/password:" 등 키워드 뒤에 실제 값처럼 보이는 토큰(영문/숫자/기호 조합,
    // 4자 이상)만 마스킹한다. 값 부분을 일반 \S+로 잡으면 "비밀번호가 기억이 안나요", "비밀번호 변경은
    // 어디서 해요?" 같은 정상 질문/설명 문장까지 오탐해 훼손하므로, 한글은 매칭 대상에서 제외한다.
    // 완전한 일반화는 불가능한 휴리스틱이다.
    private static final Pattern PASSWORD_VALUE_PATTERN =
            Pattern.compile("(?i)(비밀번호|비번|패스워드|password|pw)(\\s*[:은는이가]?\\s*)([A-Za-z0-9!@#$%^&*()_+=\\-]{4,})");

    private final MemberAPI memberAPI;

    public String mask(String rawText, Long memberId) {
        if (!StringUtils.hasText(rawText)) {
            return rawText;
        }

        String masked = maskPatterns(rawText);
        masked = maskOwnNickname(masked, memberId);

        return masked;
    }

    /**
     * 회원 조회가 필요 없는 순수 정규식 기반 마스킹만 수행한다. (테스트하기 쉽도록 분리)
     */
    String maskPatterns(String rawText) {
        String masked = rawText;
        masked = RESIDENT_REGISTRATION_NUMBER_PATTERN.matcher(masked).replaceAll("[주민등록번호]");
        masked = PHONE_NUMBER_PATTERN.matcher(masked).replaceAll("[전화번호]");
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("[이메일]");
        masked = PASSWORD_VALUE_PATTERN.matcher(masked).replaceAll("$1$2[비밀번호]");

        return masked;
    }

    private String maskOwnNickname(String text, Long memberId) {
        if (memberId == null) {
            return text;
        }

        String nickname = memberAPI.fetchNickname(memberId);
        if (!StringUtils.hasText(nickname)) {
            return text;
        }

        return text.replace(nickname, "[닉네임]");
    }
}
