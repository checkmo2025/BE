package checkmo.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.bookStory.internal.scheduler.BookStoryViewScheduler;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberTerms;
import checkmo.member.internal.entity.Terms;
import checkmo.member.internal.entity.TermsType;
import checkmo.member.internal.exception.MemberException;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.repository.MemberTermsRepository;
import checkmo.member.internal.repository.TermsRepository;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import checkmo.member.internal.service.command.MemberTermsCommandService;
import checkmo.member.internal.service.command.TermsAgreementCommand;
import checkmo.member.internal.service.query.MemberTermsQueryService;
import checkmo.support.SpringTest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringTest
class TermsDomainServiceTest {

    private final TermsRepository termsRepository;
    private final MemberTermsRepository memberTermsRepository;
    private final MemberRepository memberRepository;
    private final MemberTermsQueryService memberTermsQueryService;
    private final MemberTermsCommandService memberTermsCommandService;
    private final JdbcTemplate jdbcTemplate;

    @MockitoBean
    BookRecommendationScheduler bookRecommendationScheduler;

    @MockitoBean
    BookStoryViewScheduler bookStoryViewScheduler;

    @MockitoBean
    MemberCleanupScheduler memberCleanupScheduler;

    TermsDomainServiceTest(
            TermsRepository termsRepository,
            MemberTermsRepository memberTermsRepository,
            MemberRepository memberRepository,
            MemberTermsQueryService memberTermsQueryService,
            MemberTermsCommandService memberTermsCommandService,
            JdbcTemplate jdbcTemplate
    ) {
        this.termsRepository = termsRepository;
        this.memberTermsRepository = memberTermsRepository;
        this.memberRepository = memberRepository;
        this.memberTermsQueryService = memberTermsQueryService;
        this.memberTermsCommandService = memberTermsCommandService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("delete from member_terms");
        jdbcTemplate.execute("delete from terms");
        jdbcTemplate.execute("delete from member");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void 활성_약관은_표시_순서대로_조회한다() {
        Terms marketing = saveTerms(TermsType.MARKETING, "마케팅", true, false);
        Terms privacy = saveTerms(TermsType.PRIVACY_COLLECTION, "개인정보", true, true);
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);
        Terms thirdParty = saveTerms(TermsType.THIRD_PARTY_PROVISION, "제3자", true, false);
        saveTerms(TermsType.SERVICE_TERMS, "비활성 서비스", false, true);

        List<Terms> activeTerms = memberTermsQueryService.retrieveActiveTerms();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(activeTerms)
                    .extracting(Terms::getTermsType)
                    .containsExactly(
                            TermsType.SERVICE_TERMS,
                            TermsType.PRIVACY_COLLECTION,
                            TermsType.THIRD_PARTY_PROVISION,
                            TermsType.MARKETING
                    );
            softly.assertThat(activeTerms)
                    .extracting(Terms::getId)
                    .containsExactly(service.getId(), privacy.getId(), thirdParty.getId(), marketing.getId());
        });
    }

    @Test
    void 활성_약관_종류가_중복되면_서버_불변식_위반으로_실패한다() {
        saveTerms(TermsType.SERVICE_TERMS, "서비스 v1", true, true);
        saveTerms(TermsType.SERVICE_TERMS, "서비스 v2", true, true);

        assertThatThrownBy(memberTermsQueryService::retrieveActiveTerms)
                .isInstanceOf(MemberException.class);
    }

    @Test
    void 약관_동의_수정도_활성_약관_종류가_중복되면_서버_불변식_위반으로_실패한다() {
        Member member = saveMember();
        Terms service = saveTerms(TermsType.SERVICE_TERMS, "서비스 v1", true, true);
        saveTerms(TermsType.SERVICE_TERMS, "서비스 v2", true, true);

        assertThatThrownBy(() -> memberTermsCommandService.updateAgreements(
                member.getId(),
                List.of(new TermsAgreementCommand(service.getId(), true))
        )).isInstanceOf(MemberException.class);
    }

    @Test
    void 회원_약관_상태는_생성일시와_id_내림차순의_최신_이력으로_계산한다() {
        Member member = saveMember();
        Terms terms = saveTerms(TermsType.MARKETING, "마케팅", true, false);
        MemberTerms first = saveMemberTerms(member, terms, true);
        MemberTerms latest = saveMemberTerms(member, terms, false);
        jdbcTemplate.update(
                "update member_terms set created_at = CURRENT_TIMESTAMP(6), updated_at = CURRENT_TIMESTAMP(6)"
        );

        Map<Long, MemberTerms> latestTermsByTermsId = memberTermsQueryService.retrieveLatestMemberTermsByTermsId(
                member.getId(),
                List.of(terms.getId())
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(latestTermsByTermsId).containsOnlyKeys(terms.getId());
            softly.assertThat(latestTermsByTermsId.get(terms.getId()).getId()).isEqualTo(latest.getId());
            softly.assertThat(latestTermsByTermsId.get(terms.getId()).isAgreed()).isFalse();
            softly.assertThat(first.getId()).isLessThan(latest.getId());
        });
    }

    @Test
    void 필수_약관은_비동의로_제출할_수_없다() {
        Member member = saveMember();
        Terms requiredTerms = saveTerms(TermsType.SERVICE_TERMS, "서비스", true, true);

        assertThatThrownBy(() -> memberTermsCommandService.updateAgreements(
                member.getId(),
                List.of(new TermsAgreementCommand(requiredTerms.getId(), false))
        )).isInstanceOf(MemberException.class);

        assertThat(memberTermsRepository.countByMember_IdAndTerms_Id(member.getId(), requiredTerms.getId()))
                .isZero();
    }

    @Test
    void 선택_약관은_동의_상태가_바뀌면_새_이력을_추가한다() {
        Member member = saveMember();
        Terms optionalTerms = saveTerms(TermsType.MARKETING, "마케팅", true, false);

        memberTermsCommandService.updateAgreements(
                member.getId(),
                List.of(new TermsAgreementCommand(optionalTerms.getId(), true))
        );
        memberTermsCommandService.updateAgreements(
                member.getId(),
                List.of(new TermsAgreementCommand(optionalTerms.getId(), false))
        );
        memberTermsCommandService.updateAgreements(
                member.getId(),
                List.of(new TermsAgreementCommand(optionalTerms.getId(), true))
        );
        Map<Long, MemberTerms> latestTermsByTermsId = memberTermsQueryService.retrieveLatestMemberTermsByTermsId(
                member.getId(),
                List.of(optionalTerms.getId())
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberTermsRepository.countByMember_IdAndTerms_Id(member.getId(), optionalTerms.getId()))
                    .isEqualTo(3);
            softly.assertThat(latestTermsByTermsId.get(optionalTerms.getId()).isAgreed()).isTrue();
        });
    }

    @Test
    void 최신_상태와_같은_제출은_멱등이며_이력을_추가하지_않는다() {
        Member member = saveMember();
        Terms optionalTerms = saveTerms(TermsType.MARKETING, "마케팅", true, false);

        memberTermsCommandService.updateAgreements(
                member.getId(),
                List.of(new TermsAgreementCommand(optionalTerms.getId(), true))
        );
        memberTermsCommandService.updateAgreements(
                member.getId(),
                List.of(new TermsAgreementCommand(optionalTerms.getId(), true))
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberTermsRepository.countByMember_IdAndTerms_Id(member.getId(), optionalTerms.getId()))
                    .isOne();
        });
    }

    private Terms saveTerms(TermsType termsType, String title, boolean active, boolean required) {
        return termsRepository.save(Terms.builder()
                .termsType(termsType)
                .title(title)
                .termUrl("https://example.com/" + UUID.randomUUID())
                .version((int) termsRepository.count() + 1)
                .active(active)
                .required(required)
                .build());
    }

    private Member saveMember() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return memberRepository.save(Member.builder()
                .id(Math.abs(UUID.randomUUID().getMostSignificantBits()))
                .legacyId("LOCAL_" + suffix)
                .email(suffix + "@example.com")
                .name("테스트")
                .phoneNumber("01000000000")
                .nickName("terms" + suffix)
                .description("")
                .build());
    }

    private MemberTerms saveMemberTerms(Member member, Terms terms, boolean agreed) {
        return memberTermsRepository.save(MemberTerms.builder()
                .member(member)
                .terms(terms)
                .agreed(agreed)
                .build());
    }
}
