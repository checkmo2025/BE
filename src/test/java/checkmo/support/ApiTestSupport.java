package checkmo.support;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.entity.AuthUser;
import checkmo.authentication.internal.entity.Role;
import checkmo.authentication.internal.repository.AuthRepository;
import checkmo.authentication.internal.security.jwt.JwtToken;
import checkmo.authentication.internal.security.jwt.JwtTokenProvider;
import checkmo.authentication.internal.security.jwt.TokenCacheService;
import checkmo.book.BookAPI;
import checkmo.book.BookExternalDTO;
import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.bookStory.internal.scheduler.BookStoryViewScheduler;
import checkmo.infra.email.internal.service.EmailSender;
import checkmo.infra.s3.internal.service.S3Service;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.MemberRepository;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

@ApiTest
public abstract class ApiTestSupport {

    @LocalServerPort
    int port;

    @Autowired
    protected AuthRepository authRepository;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    protected TokenCacheService tokenCacheService;

    @MockitoBean
    protected RestTemplate restTemplate;

    @MockitoBean
    protected RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    protected StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    protected EmailSender emailSender;

    @MockitoBean
    protected S3Service s3Service;

    @MockitoBean
    protected BookAPI bookAPI;

    @MockitoBean
    protected CacheManager cacheManager;

    @MockitoBean
    BookRecommendationScheduler bookRecommendationScheduler;

    @MockitoBean
    BookStoryViewScheduler bookStoryViewScheduler;

    @MockitoBean
    MemberCleanupScheduler memberCleanupScheduler;

    protected ValueOperations<String, Object> redisValueOperations;
    protected HashOperations<String, Object, Object> redisHashOperations;
    protected ValueOperations<String, String> stringRedisValueOperations;
    private final Map<String, Cache> testCaches = new ConcurrentHashMap<>();

    @BeforeEach
    void setUpApiTestSupport() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        redisValueOperations = mock();
        redisHashOperations = mock();
        stringRedisValueOperations = mock();

        when(redisTemplate.opsForValue()).thenReturn(redisValueOperations);
        when(redisTemplate.opsForHash()).thenReturn(redisHashOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(redisTemplate.expire(anyString(), org.mockito.ArgumentMatchers.any(Duration.class))).thenReturn(true);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringRedisValueOperations);
        when(stringRedisTemplate.hasKey(anyString())).thenReturn(false);
        when(cacheManager.getCache(anyString()))
                .thenAnswer(invocation -> testCaches.computeIfAbsent(
                        invocation.getArgument(0),
                        name -> new ConcurrentMapCache(name, false)
                ));
        when(bookAPI.fetchOrCreateBook(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookAPI.fetchBookBasicInfo(anyString())).thenAnswer(invocation -> {
            String bookId = invocation.getArgument(0);
            return BookExternalDTO.BasicInfo.builder()
                    .bookId(bookId)
                    .title("테스트 책")
                    .author("테스트 저자")
                    .imgUrl("https://example.com/book.png")
                    .build();
        });
        when(bookAPI.fetchBookDetailInfo(anyString())).thenAnswer(invocation -> {
            String bookId = invocation.getArgument(0);
            return BookExternalDTO.DetailInfo.builder()
                    .bookId(bookId)
                    .title("테스트 책")
                    .author("테스트 저자")
                    .imgUrl("https://example.com/book.png")
                    .publisher("테스트 출판사")
                    .description("테스트 설명")
                    .build();
        });
        when(bookAPI.fetchBookBasicInfoByBookIds(org.mockito.ArgumentMatchers.anyList())).thenAnswer(invocation -> {
            List<String> bookIds = invocation.getArgument(0);
            return bookIds.stream().collect(Collectors.toMap(
                    bookId -> bookId,
                    bookId -> BookExternalDTO.BasicInfo.builder()
                            .bookId(bookId)
                            .title("테스트 책")
                            .author("테스트 저자")
                            .imgUrl("https://example.com/book.png")
                            .build()
            ));
        });

        when(tokenCacheService.isAccessTokenBlacklisted(anyString())).thenReturn(false);
        when(tokenCacheService.getRefreshToken(anyString())).thenReturn(null);
        when(tokenCacheService.saveRefreshToken(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        when(tokenCacheService.saveBlacklistToken(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(tokenCacheService).deleteRefreshToken(anyString());
    }

    @AfterEach
    void tearDownApiTestSupport() {
        RestAssured.reset();
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.queryForList(
                        """
                                select table_name
                                from information_schema.tables
                                where lower(table_schema) = 'public'
                                  and table_type in ('BASE TABLE', 'TABLE')
                                """,
                        String.class
                )
                .forEach(tableName -> jdbcTemplate.execute("delete from " + quoteIdentifier(tableName)));
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    protected TestUser createUser() {
        return createUser(Role.USER, true);
    }

    protected TestUser createIncompleteUser() {
        return createUser(Role.USER, false);
    }

    protected TestUser createAdmin() {
        return createUser(Role.ADMIN, true);
    }

    protected TestUser createUserWithPassword(String rawPassword) {
        return createUser(Role.USER, true, rawPassword);
    }

    private TestUser createUser(Role role, boolean profileCompleted) {
        return createUser(role, profileCompleted, "Pass123!");
    }

    private TestUser createUser(Role role, boolean profileCompleted, String rawPassword) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String id = role == Role.USER ? "LOCAL_" + suffix : role.name().toLowerCase() + "-" + suffix;
        String email = id + "@example.com";
        String nickName = role.name().toLowerCase() + suffix;

        AuthUser authUser = AuthUser.builder()
                .id(id)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .profileCompleted(profileCompleted)
                .nickName(nickName)
                .build();
        authRepository.save(authUser);

        Member member = Member.builder()
                .id(id)
                .email(email)
                .name("테스트")
                .phoneNumber("01012345678")
                .nickName(profileCompleted ? nickName : "")
                .description("api test fixture")
                .build();
        memberRepository.save(member);

        JwtToken token = jwtTokenProvider.generateToken(jwtTokenProvider.getAuthenticationFromMemberId(id));
        return new TestUser(id, email, nickName, role, profileCompleted, token.getAccessToken(), token.getRefreshToken());
    }

    protected Cookie accessTokenCookie(TestUser user) {
        return new Cookie.Builder("accessToken", user.accessToken())
                .setPath("/")
                .build();
    }

    protected Cookie refreshTokenCookie(TestUser user) {
        return new Cookie.Builder("refreshToken", user.refreshToken())
                .setPath("/")
                .build();
    }

    protected record TestUser(
            String id,
            String email,
            String nickName,
            Role role,
            boolean profileCompleted,
            String accessToken,
            String refreshToken
    ) {
    }
}
