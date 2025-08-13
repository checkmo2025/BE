# 🚀 백엔드 개발 컨벤션

---

## 🌳 깃 브랜치 전략

* `develop` 브랜치:
    * **디폴트 브랜치**  이자 배포되는 **배포 브랜치**
    * 👑 팀장만 직접 관리하고 머지
    * 🛠️ **개발이 완료된 기능**들이 통합되는 브랜치
    * 새로운 기능 개발 시 이 브랜치를 source로 만들기!
* **새로운 브랜치 명명 규칙**:
    * `feat/[이슈 번호]/[기능명]` (예: `feat/123/user-login`)
        * 💡 **새로운 기능**을 개발할 때 사용
        * Lable 중 `✨ feature` 사용
    * `refactor/[이슈 번호]/[기능명]` (예: `refactor/456/user-service`)
        * ♻️ **코드 리팩토링**을 진행할 때 사용
        * Lable 중 `♻️ refactor` 사용
    * `bug/[이슈 번호]/[기능명]` (예: `bug/789/null-pointer-exception`)
        * 🐞 **버그 수정**을 진행할 때 사용
        * Lable 중 `🐛 bug` 사용

---
## 📝 커밋 컨벤션

커밋 메시지 양식, gitmoji 사용해도 좋아용

* ✨`feat`: **새로운 기능 추가**
    * (예: `feat: 사용자 로그인 기능 구현`)
* ♻️`refactor`: **코드 리팩토링**
    * (예: `refactor: User 엔티티 필드명 개선`)
* 🐛`bug`: **버그 수정**
    * (예: `bug: 회원 가입 시 비밀번호 유효성 검사 오류 수정`)
* 📝`docs`: **문서 수정**
    * (예: `docs: README.md 업데이트`)
* ✅`test`: **테스트 코드 추가/수정**
    * (예: `test: UserService 단위 테스트 추가`)
* 📦`build`: **빌드 시스템 또는 외부 의존성 관련 변경**
    * (예: `build: Spring Boot 버전 업데이트`)
* 🚀`ci`: **CI 설정 파일 변경**
    * (예: `ci: GitHub Actions 설정 추가`)
* 🔨`chore`: **그 외 자잘한 변경 사항**
    * (예: `chore: 불필요한 콘솔 로그 제거`)
* 🎨`style`: **코드 포맷팅, 세미콜론 누락 등 코드 동작에 영향을 주지 않는 변경**
    * (예: `style: 코드 컨벤션에 맞게 포맷팅 적용`)

---
## 🤝 PR (Pull Request) 전략

* `develop` 브랜치 PR:
    * 👑 **팀장만 승인 및 머지** 가능
* **그 외 브랜치 PR**:
    * 👥 `develop` 브랜치 외 다른 브랜치로 머지할 때는 **최소 1명 이상의 추가 승인** 후 머지 가능

---
## 🧑‍💻 코드 컨벤션 (Java)

* **클래스명**: `PascalCase` : 첫글자와 이어지는 단어의 첫글자를 대문자로 표기하는 방법
    * 예: `UserService`, `ProductRepository`
* **변수명**: `camelCase` : 첫단어는 소문자로 표기하지만, 이어지는 단어의 첫글자는 대문자로 표기하는 방법
    * 예: `userName`, `orderId`, `totalAmount`
* **DB 컬럼명**: `snake_case` : 모든 단어를 소문자로 표기하고, 단어를 언더바(_) 로 연결하는 방법
    * 예: `user_name`, `product_price`, `created_at`
* **상수**: `UPPER_CASE` : 모든 단어를 대문자로 표기하고, 단어를 언더바(_) 로 연결하는 방법
    * 예: `MAX_RETRIES`, `DEFAULT_PAGE_SIZE`

---

## 🏗️ 아키텍처 기반 코딩 컨벤션

### 📋 **Interface 작성 규칙**

**`Service Interface`에는 반드시 상세한 JavaDoc을 작성합니다.**

```java
/**
 * 독서 클럽 조회 서비스
 *
 * 독서 클럽 자체에 대한 조회 기능을 담당
 * ex) 독서 클럽 목록 조회, 검색 기능, 특정 독서 클럽 상세 정보 조회 등을 처리
 */
public interface ClubQueryService {

    /**
     * 특정 상태의 모임 회원 목록을 조회합니다.
     *
     * @param clubId 모임 ID
     * @param memberId 요청자 회원 ID (권한 확인용)
     * @param status 조회할 상태 ("MEMBER", "STAFF", "PENDING", "BLOCKED", "ALL" 중 하나)
     * @param cursorId 페이징 커서 ID
     * @param pageable 페이징 정보
     * @return ClubMember 엔티티 리스트 (순수 객체만 반환)
     */
    List<ClubMember> getClubMemberListByStatus(Long clubId, String memberId, String status, Long cursorId, Pageable pageable);

    /**
     * 다음 페이지가 존재하는지 확인합니다.
     *
     * @param clubId 모임 ID
     * @param status 조회할 상태
     * @param lastId 현재 페이지의 마지막 ID
     * @return true: 다음 페이지 있음, false: 마지막 페이지
     */
    boolean hasNextPage(Long clubId, String status, Long lastId);
}
```
### ✅ Interface 작성 원칙:
- 클래스 설명에 해당 서비스의 전체적인 역할과 책임을 명시
- 각 메서드마다 목적, 파라미터, 리턴값, 예외 상황을 상세히 기술
- `@param`, `@return` 태그 적극 활용
- 순수 엔티티나 기본 타입만 반환하도록 설계 (DTO 변환은 `Facade`에서 담당)

---
### 🔧 Service 구현체 작성 규칙

**`Service` 구현체에는 순수한 비즈니스 로직만 포함하고, 각 로직 단계별로 상세한 주석을 작성합니다.**

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryServiceImpl implements ClubQueryService {

    private final ClubMemberRepository clubMemberRepository;
    private final ClubMemberQueryService clubMemberQueryService;

    /**
     * 특정 상태의 모임 회원 목록을 조회합니다.
     */
    @Override
    public List<ClubMember> getClubMemberListByStatus(Long clubId, String memberId, String status, Long cursorId, Pageable pageable) {

        // 1. 클럽 유효성 검증
        validateClub(clubId);

        // 2. 요청자 권한 확인 (운영진만 조회 가능)
        ClubMember requester = clubMemberQueryService.validateClubMember(clubId, memberId);
        if (!requester.isStaff()) {
            throw new GeneralException(ErrorStatus.CLUB_STAFF_ONLY);
        }

        // 3. 상태별 회원 목록 조회 (순수 엔티티만 반환)
        if ("ALL".equals(status)) {
            return clubMemberRepository.findAllMembersByClubIdWithCursor(clubId, cursorId, pageable);
        } else {
            ClubMember.ClubMemberStatus memberStatus = ClubMember.ClubMemberStatus.valueOf(status);
            return clubMemberRepository.findMembersByStatusWithCursor(clubId, memberStatus, cursorId, pageable);
        }
    }

    @Override
    public boolean hasNextPage(Long clubId, String status, Long lastId) {
        // 다음 페이지 존재 여부만 확인 (간단한 boolean 반환)
        return clubMemberRepository.existsNextPage(clubId, status, lastId);
    }
}
```
### ✅ Service 구현체 작성 원칙:

- 각 로직 단계마다 번호를 매겨 순서를 명확하게 표시
- 순수한 엔티티 조회와 비즈니스 검증에만 집중
- DTO 변환, 페이징 처리 로직은 포함하지 않음
- 예외 상황 처리 로직에 대한 설명 추가
- 복잡한 조건문의 경우 인라인 주석으로 의미 설명

---

### 🎯 Facade 패턴 구현 규칙

**`Facade`는 `Service`의 순수 로직을 조합하고 DTO 변환, 페이징, 추가 검증 등의 부가 로직을 담당합니다.**

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubQueryFacadeImpl implements ClubQueryFacade {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ClubQueryService clubQueryService;              // 핵심 비즈니스 로직
    private final MemberQueryFacade memberQueryFacade;           // 다른 도메인과의 협력

    /**
     * 특정 상태의 모임 회원 목록을 조회합니다. (외부 도메인용)
     */
    @Override
    public ClubResponseDTO.ClubMemberListDTO getClubMemberListByStatus(Long clubId, String memberId, String clubMemberStatus, Long cursorId, Integer size) {

        // 1. 커서 초기화 (Facade에서 페이징 로직 처리)
        Long cursor = (cursorId == null || cursorId == 0L) ? Long.MAX_VALUE : cursorId;

        // 2. 페이지 크기 결정 (size가 null 또는 0 이하이면 기본값 사용)
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
        Pageable pageable = PageRequest.of(0, pageSize + 1); // +1로 다음 페이지 존재 여부 확인

        // 3. Service에서 순수 엔티티 조회
        List<ClubMember> clubMembers = clubQueryService.getClubMemberListByStatus(clubId, memberId, clubMemberStatus, cursor, pageable);

        // 4. Facade에서 페이징 처리
        boolean hasNext = clubMembers.size() > pageSize;
        if (hasNext) {
            clubMembers = clubMembers.subList(0, pageSize); // 마지막 요소 제거
        }
        Long nextCursor = hasNext && !clubMembers.isEmpty() ?
                clubMembers.get(clubMembers.size() - 1).getId() : null;

        // 5. 회원 정보 배치 조회 (다른 도메인과의 협력)
        List<String> memberIds = clubMembers.stream()
                .map(ClubMember::getMemberId)
                .toList();
        Map<String, MemberSharedDTO.BasicInfoDTO> memberInfoMap = memberQueryFacade.getMemberBasicInfoMap(memberIds);

        // 6. DTO 변환 및 조합 (Facade의 핵심 역할)
        List<ClubResponseDTO.ClubMemberDTO> memberDTOList = clubMembers.stream()
                .map(clubMember -> {
                    MemberSharedDTO.BasicInfoDTO memberInfo = memberInfoMap.get(clubMember.getMemberId());
                    return ClubConverter.toClubMemberDTO(clubMember, memberInfo);
                })
                .toList();

        // 7. 최종 응답 DTO 생성 (Converter 사용)
        return ClubConverter.toClubMemberListDTO(memberDTOList, hasNext, nextCursor);
    }
}
```
### ✅ Facade 구현 규칙:
- `Service` 호출 + DTO 변환/조합 역할에 집중
- 페이징, 정렬, 필터링 등 부가 로직 처리
- 여러 `Service` 조합으로 복합적인 유스케이스 구현
- 다른 도메인과의 협력을 통한 데이터 보강
- `Converter`를 통한 DTO 변환으로 일관된 변환 로직 유지

---
### 🔄 도메인 간 통신 규칙

**도메인 간 데이터 교환은 반드시 `Facade`를 통해 이루어지며, 공유 DTO를 사용합니다.**

```java
@Service
@RequiredArgsConstructor
public class NotificationCommandServiceImpl implements NotificationCommandService {
    
    // ✅ 올바른 예시: 다른 도메인의 Facade 사용
    private final MemberQueryFacade memberQueryFacade;  
    private final ClubQueryFacade clubQueryFacade;
    
    // ❌ 잘못된 예시: 다른 도메인의 Service/Repository 직접 사용
    // private final MemberRepository memberRepository;
    // private final ClubService clubService;
    
    public void sendClubJoinNotification(String memberId, Long clubId) {
        
        // 다른 도메인 정보는 Facade를 통해서만 조회
        MemberSharedDTO.BasicInfoDTO memberInfo = memberQueryFacade.getMemberBasicInfoForShare(memberId);
        ClubSharedDTO.BasicInfoDTO clubInfo = clubQueryFacade.getClubBasicInfoForShare(clubId);
        
        // 비즈니스 로직 수행...
    }
}
```
### ✅ 도메인 간 통신 원칙:

- 절대 다른 도메인의 `Service`, `Repository` 직접 참조 금지
- `Facade` 인터페이스를 통해서만 다른 도메인과 소통
- `SharedDTO` 패키지의 `공유 DTO`만 사용하여 데이터 교환
- 엔티티 객체는 절대 도메인 경계를 넘나들면 안됨

---
### 🗃️ Entity 설계 규칙
**연관관계는 JPA 매핑을 유지하되, 코드에서는 ID 필드를 통해 안전하게 접근합니다.**
```java
@Entity
public class BookStory extends BaseEntity {

    // ✅ JPA 연관관계 매핑 (영속성 관리용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // ✅ 읽기 전용 ID 필드 (코드에서 안전한 접근용)
    @Column(name = "member_id", insertable = false, updatable = false)
    private String memberId;
    
    // ❌ 잘못된 사용: bookStory.getMember.getId() 직접 접근 금지
    // public String getMember() {
    //     return this.member;  // 다른 도메인 객체 로딩 위험
    // }
    
    // ✅ 올바른 사용: 읽기 전용 ID 필드 사용
    public String getMemberId() {
        return this.memberId;  // 안전한 ID 접근
    }
}
```

### ✅ Entity 설계 원칙:
- JPA 연관관계는 영속성 관리를 위해 유지
- 읽기 전용 ID 필드 `(insertable=false, updatable=false)` 추가
- 코드에서는 ID 필드만 사용하여 다른 도메인 객체 로딩 방지
- 연관 객체에 직접 접근하는 `getter` 사용 금지