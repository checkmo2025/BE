package checkmo.common.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.authentication.AuthenticationAPI;
import checkmo.book.internal.service.BookRecommendationService;
import checkmo.book.internal.service.query.AladinApiService;
import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.book.web.dto.BookResponseDTO;
import checkmo.infra.s3.internal.listener.S3EventListener;
import checkmo.infra.s3.internal.service.S3Service;
import checkmo.member.MemberEvent;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import checkmo.member.internal.service.command.MemberCommandService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

class SentryBackgroundCaptureTest {

    @Test
    @SuppressWarnings("unchecked")
    void capturesRecommendationRefreshFailureWhileReturningFallbackEmptyList() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        AladinApiService aladinApiService = mock(AladinApiService.class);
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        RuntimeException failure = new RuntimeException("aladin unavailable");
        BookRecommendationService service = new BookRecommendationService(
                redisTemplate,
                aladinApiService,
                captureClient
        );
        when(aladinApiService.retrieveRecommendedBooks()).thenThrow(failure);

        BookResponseDTO.BookList response = service.refreshDailyRecommendedBooks();

        assertThat(response.getDetailInfoList()).isEmpty();
        assertThat(captureClient.captured()).containsExactly(failure);
    }

    @Test
    void doesNotCaptureRecommendationSchedulerFallbackEmptyList() {
        BookRecommendationService recommendationService = mock(BookRecommendationService.class);
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        BookRecommendationScheduler scheduler = new BookRecommendationScheduler(recommendationService, captureClient);
        BookResponseDTO.BookList emptyFallback = BookResponseDTO.BookList.builder()
                .detailInfoList(List.of())
                .hasNext(false)
                .currentPage(null)
                .build();
        when(recommendationService.refreshDailyRecommendedBooks()).thenReturn(emptyFallback);

        assertDoesNotThrow(scheduler::updateDailyRecommendedBooks);

        assertThat(captureClient.captured()).isEmpty();
    }

    @Test
    void capturesUnexpectedRecommendationSchedulerFailureWithoutThrowing() {
        BookRecommendationService recommendationService = mock(BookRecommendationService.class);
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        BookRecommendationScheduler scheduler = new BookRecommendationScheduler(recommendationService, captureClient);
        RuntimeException failure = new RuntimeException("scheduler refresh failed");
        doThrow(failure).when(recommendationService).refreshDailyRecommendedBooks();

        assertDoesNotThrow(scheduler::updateDailyRecommendedBooks);

        assertThat(captureClient.captured()).containsExactly(failure);
    }

    @Test
    void capturesExpiredMemberCleanupFailuresOnceAndContinuesWithRemainingMembers() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        AuthenticationAPI authenticationAPI = mock(AuthenticationAPI.class);
        MemberCommandService memberCommandService = mock(MemberCommandService.class);
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        MemberCleanupScheduler scheduler = new MemberCleanupScheduler(
                memberRepository,
                authenticationAPI,
                memberCommandService,
                captureClient
        );
        RuntimeException firstFailure = new RuntimeException("delete failed first");
        RuntimeException secondFailure = new RuntimeException("delete failed second");
        Member first = Member.builder().id("member-1").build();
        Member second = Member.builder().id("member-2").build();
        when(memberRepository.findAllByDeactivatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(first, second));
        doThrow(firstFailure).when(memberCommandService).deleteMember("member-1");
        doThrow(secondFailure).when(memberCommandService).deleteMember("member-2");

        assertDoesNotThrow(scheduler::cleanupExpiredDeactivatedMembers);

        verify(memberCommandService).deleteMember("member-1");
        verify(memberCommandService).deleteMember("member-2");
        assertThat(captureClient.captured()).containsExactly(firstFailure);
        assertThat(firstFailure.getSuppressed()).containsExactly(secondFailure);
    }

    @Test
    void capturesS3DeleteFailureAndKeepsListenerFallbackBehavior() {
        S3Service s3Service = mock(S3Service.class);
        RecordingSentryCaptureClient captureClient = new RecordingSentryCaptureClient();
        S3EventListener listener = new S3EventListener(s3Service, captureClient);
        RuntimeException failure = new RuntimeException("s3 delete failed");
        when(s3Service.extractKeyFromUrl("https://bucket.s3.ap-northeast-2.amazonaws.com/images/a.png"))
                .thenReturn("images/a.png");
        doThrow(failure).when(s3Service).deleteImage("images/a.png");

        assertDoesNotThrow(() -> listener.handleDeleteProfileImageEvent(
                new MemberEvent.DeleteProfileImage("https://bucket.s3.ap-northeast-2.amazonaws.com/images/a.png")
        ));

        assertThat(captureClient.captured()).containsExactly(failure);
    }
}
