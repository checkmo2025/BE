package checkmo.chatbot.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.member.MemberAPI;
import org.junit.jupiter.api.Test;

class PiiMaskingServiceTest {

    private final MemberAPI memberAPI = mock(MemberAPI.class);
    private final PiiMaskingService piiMaskingService = new PiiMaskingService(memberAPI);

    @Test
    void masksResidentRegistrationNumberWithHyphen() {
        String result = piiMaskingService.maskPatterns("제 주민등록번호는 900101-1234567 이에요");

        assertThat(result).isEqualTo("제 주민등록번호는 [주민등록번호] 이에요");
    }

    @Test
    void masksResidentRegistrationNumberWithoutHyphen() {
        String result = piiMaskingService.maskPatterns("9001011234567 확인해주세요");

        assertThat(result).isEqualTo("[주민등록번호] 확인해주세요");
    }

    @Test
    void masksPhoneNumberWithHyphen() {
        String result = piiMaskingService.maskPatterns("연락처는 010-1234-5678 입니다");

        assertThat(result).isEqualTo("연락처는 [전화번호] 입니다");
    }

    @Test
    void masksPhoneNumberWithoutHyphen() {
        String result = piiMaskingService.maskPatterns("01012345678 로 연락주세요");

        assertThat(result).isEqualTo("[전화번호] 로 연락주세요");
    }

    @Test
    void masksEmail() {
        String result = piiMaskingService.maskPatterns("제 이메일은 test.user@example.com 이에요");

        assertThat(result).isEqualTo("제 이메일은 [이메일] 이에요");
    }

    @Test
    void masksPasswordValueAfterKeyword() {
        String result = piiMaskingService.maskPatterns("비밀번호는 abc1234!입니다");

        assertThat(result).isEqualTo("비밀번호는 [비밀번호]");
    }

    @Test
    void doesNotAlterTextWithoutPii() {
        String result = piiMaskingService.maskPatterns("책 이야기 글쓰기는 어디서 하나요?");

        assertThat(result).isEqualTo("책 이야기 글쓰기는 어디서 하나요?");
    }

    @Test
    void masksOwnNicknameWhenLoggedIn() {
        when(memberAPI.fetchNickname(1L)).thenReturn("책모지기");

        String result = piiMaskingService.mask("제 닉네임은 책모지기 입니다", 1L);

        assertThat(result).isEqualTo("제 닉네임은 [닉네임] 입니다");
    }

    @Test
    void doesNotAttemptNicknameMaskingWhenAnonymous() {
        String result = piiMaskingService.mask("비로그인 사용자의 텍스트입니다", null);

        assertThat(result).isEqualTo("비로그인 사용자의 텍스트입니다");
    }

    @Test
    void handlesBlankInput() {
        assertThat(piiMaskingService.mask("", 1L)).isEqualTo("");
        assertThat(piiMaskingService.mask(null, 1L)).isNull();
    }
}
