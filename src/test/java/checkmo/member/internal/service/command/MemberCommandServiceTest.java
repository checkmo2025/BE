package checkmo.member.internal.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.authentication.AuthenticationAPI;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.exception.MemberErrorStatus;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.FollowRepository;
import checkmo.member.internal.repository.MemberBlockRepository;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.web.dto.MemberRequestDTO;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

class MemberCommandServiceTest {

    @Test
    void 닉네임_비교키_유일_제약_위반만_닉네임_중복_오류로_변환한다() {
        TestFixture fixture = new TestFixture("UK_member_nickname_key");

        assertThatThrownBy(fixture::updateNickname)
                .isInstanceOf(MemberException.class)
                .satisfies(exception -> assertThat(((MemberException) exception).getErrorCode())
                        .isEqualTo(MemberErrorStatus.NICKNAME_ALREADY_EXISTS));
    }

    @Test
    void 다른_데이터_무결성_오류는_닉네임_중복으로_숨기지_않는다() {
        TestFixture fixture = new TestFixture("UK_member_legacy_id");

        assertThatThrownBy(fixture::updateNickname)
                .isSameAs(fixture.dataIntegrityViolationException);
    }

    private static class TestFixture {

        private final MemberRepository memberRepository = mock(MemberRepository.class);
        private final MemberRequestDTO.MemberProfileUpdate request = mock(MemberRequestDTO.MemberProfileUpdate.class);
        private final DataIntegrityViolationException dataIntegrityViolationException;
        private final MemberCommandService memberCommandService;

        private TestFixture(String constraintName) {
            Member member = Member.builder()
                    .id(1L)
                    .build();
            member.updateNickname("기존Nick");

            when(memberRepository.findByIdAndDeactivatedAtIsNull(1L)).thenReturn(Optional.of(member));
            when(memberRepository.existsByNickNameKey("새nick")).thenReturn(false);
            when(request.getNickname()).thenReturn("새Nick");

            var constraintViolation = new org.hibernate.exception.ConstraintViolationException(
                    "unique constraint violation",
                    new SQLException("duplicate key"),
                    constraintName
            );
            dataIntegrityViolationException = new DataIntegrityViolationException(
                    "could not execute statement",
                    constraintViolation
            );
            doThrow(dataIntegrityViolationException).when(memberRepository).flush();

            memberCommandService = new MemberCommandService(
                    mock(AuthenticationAPI.class),
                    memberRepository,
                    mock(FollowRepository.class),
                    mock(MemberBlockRepository.class),
                    mock(ApplicationEventPublisher.class)
            );
        }

        private void updateNickname() {
            memberCommandService.updateProfile(1L, request);
        }
    }
}
