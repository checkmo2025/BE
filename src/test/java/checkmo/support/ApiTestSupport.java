package checkmo.support;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import checkmo.authentication.internal.config.properties.JwtProperties;
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
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private JwtProperties jwtProperties;

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
    private final Map<String, String> refreshTokensByUserId = new ConcurrentHashMap<>();

    @BeforeEach
    void setUpApiTestSupport() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        redisValueOperations = mock();
        redisHashOperations = mock();
        stringRedisValueOperations = mock();
        refreshTokensByUserId.clear();

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
        when(tokenCacheService.getRefreshToken(anyString()))
                .thenAnswer(invocation -> refreshTokensByUserId.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            refreshTokensByUserId.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(tokenCacheService).saveRefreshToken(anyString(), anyString());
        when(tokenCacheService.compareAndRotateRefreshToken(
                anyString(),
                anyString(),
                anyString(),
                org.mockito.ArgumentMatchers.any(Duration.class)
        ))
                .thenAnswer(invocation -> rotateRefreshTokenIfCurrent(
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2)
                ));
        when(tokenCacheService.saveBlacklistToken(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            refreshTokensByUserId.remove(invocation.getArgument(0));
            return null;
        }).when(tokenCacheService).deleteRefreshToken(anyString());
        when(tokenCacheService.deleteRefreshTokenIfMatches(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String userId = invocation.getArgument(0);
                    String expectedRefreshToken = invocation.getArgument(1);
                    AtomicBoolean deleted = new AtomicBoolean(false);
                    refreshTokensByUserId.compute(userId, (ignored, currentRefreshToken) -> {
                        if (expectedRefreshToken.equals(currentRefreshToken)) {
                            deleted.set(true);
                            return null;
                        }
                        return currentRefreshToken;
                    });
                    return deleted.get();
                });
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

    protected TestUser createSocialUser() {
        return createUser(Role.USER, true, "Pass123!", "KAKAO_");
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
        return createUserWithId(role, profileCompleted, rawPassword, id);
    }

    private TestUser createUser(Role role, boolean profileCompleted, String rawPassword, String idPrefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return createUserWithId(role, profileCompleted, rawPassword, idPrefix + suffix);
    }

    private TestUser createUserWithId(Role role, boolean profileCompleted, String rawPassword, String id) {
        String email = id + "@example.com";
        String nickName = role.name().toLowerCase() + id.substring(Math.max(0, id.length() - 8));

        AuthUser authUser = AuthUser.builder()
                .id(id)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .profileCompleted(profileCompleted)
                .nickName(profileCompleted ? nickName : null)
                .build();
        authRepository.save(authUser);

        Member member = Member.builder()
                .id(id)
                .email(email)
                .name("테스트")
                .phoneNumber("01012345678")
                .nickName(profileCompleted ? nickName : null)
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

    protected String expiredSignedRefreshToken(TestUser user) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        Key key = Keys.hmacShaKeyFor(keyBytes);
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.id())
                .expiration(new Date(now - 1_000L))
                .signWith(key)
                .compact();
    }

    protected void saveRefreshTokenInCacheFake(String userId, String refreshToken) {
        refreshTokensByUserId.put(userId, refreshToken);
    }

    protected boolean rotateRefreshTokenIfCurrent(
            String userId,
            String expectedRefreshToken,
            String newRefreshToken
    ) {
        AtomicBoolean rotated = new AtomicBoolean(false);
        refreshTokensByUserId.compute(userId, (ignored, currentRefreshToken) -> {
            if (expectedRefreshToken.equals(currentRefreshToken)) {
                rotated.set(true);
                return newRefreshToken;
            }
            return currentRefreshToken;
        });
        return rotated.get();
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
