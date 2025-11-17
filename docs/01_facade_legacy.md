# 🏛️ Facade 패턴 회고: 시도와 한계

---

## 1. 왜 Facade 패턴을 도입했는가?

저희는 기존에 단일 서버 환경의 **모놀리식(Monolithic) 아키텍처**로 프로젝트를 개발했습니다.

프로젝트를 진행하면서 **마이크로서비스 아키텍처(MSA)** 라는 개념을 처음 접하게 되었고, 각 도메인이 물리적으로 분리된 서버에서 독립적으로 동작하는 구조에 큰 영감을 받았습니다. 특히 도메인 간의 명확한 경계와 독립성이 인상 깊었습니다.

"모놀리식 환경이지만, 도메인을 논리적으로 분리해서 마치 별도의 서버처럼 독립적으로 동작하게 만들 수는 없을까?"

이런 고민 끝에 **Facade 패턴**을 도입하여 각 도메인이 오직 Facade를 통해서만 소통하도록 설계했습니다. MSA의 도메인 분리 철학을 모놀리식 환경에서 구현해보는 시도였습니다.

### 패키지 구조의 변경

기존에 진행한 프로젝트에서는 레이어드 아키텍처(`service`, `entity`, `repository` 계층 분리)를 사용했지만   
도메인간의 낮은 결합도를 구현하기 위해 **도메인(Domain) 중심 패키지 구조**로 전환했습니다.

가장 중요한 규칙:
> **각 도메인은 자신의 경계 밖을 절대 직접 참조할 수 없다**

다른 도메인의 정보나 기능이 필요할 경우, 반드시 해당 도메인의 **Facade**를 통해서만 접근하도록 팀 규칙을 정했습니다.

---

## 2. 우리가 정한 Facade 설계 원칙 3가지

### 원칙 1: 도메인 간 직접 접근 금지

> 어떤 도메인도 다른 도메인의 서비스, 리포지토리, 엔티티를 직접 참조하거나 호출할 수 없다.

```java
// ❌ 잘못된 방식: 다른 도메인의 Repository 직접 주입
@Service
@RequiredArgsConstructor
public class BookStoryCommandServiceImpl {
    private final BookStoryRepository bookStoryRepository;
    private final MemberRepository memberRepository;  // ❌ 외부 도메인 Repository 직접 참조

    public Long createBookStory(String memberId, BookStoryCreateDTO dto) {
        Member member = memberRepository.findById(memberId);
        // ...
    }
}

// ✅ 올바른 방식: Facade를 통한 접근
@Service
@RequiredArgsConstructor
public class BookStoryCommandServiceImpl {
    private final BookStoryRepository bookStoryRepository;
    private final MemberQueryFacade memberQueryFacade;  // ✅ Facade 사용

    public Long createBookStory(String memberId, BookStoryCreateDTO dto) {
        // Facade를 통해 필요한 정보 조회
        MemberSharedDTO memberInfo = memberQueryFacade.getMemberBasicInfo(memberId);
        // ...
    }
}
```

### 원칙 2:  엔티티 관계와 ID 필드의 공존

JPA의 이점을 사용하기 위해 연관관계 매핑(`@ManyToOne`, `@OneToMany`)은 유지하기로 하였습니다.  
그리고 코드 레벨에서는 연관된 객체에 대한 직접 참조를 팀 규칙으로 금지했습니다.

**문제**: 연관된 객체의 ID를 어떻게 조회할 것인가?

**해결**: 읽기 전용 ID 필드 추가

```java
@Entity
public class BookStory extends BaseEntity {
    // 실제 관계 매핑 (직접 접근 금지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 연관 객체의 ID를 안전하게 조회하기 위한 읽기 전용 필드
    @Column(name = "member_id", insertable = false, updatable = false)
    private String memberId;

    // ❌ 금지: member.getId()
    // ✅ 허용: getMemberId()
}
```

이를 통해 `bookStory.getMemberId()`로 다른 도메인의 객체를 로딩하지 않고 외래 키 ID를 조회할 수 있었습니다.

### 원칙 3: 공유 DTO를 통한 데이터 통신

> 도메인의 경계를 넘나드는 모든 데이터는 반드시 공유 DTO(SharedDTO)여야 한다.

```java
// MemberQueryFacade.java
public interface MemberQueryFacade {
    // ✅ 공유 DTO 반환
    MemberSharedDTO.BasicInfo getMemberBasicInfo(String memberId);

    // ❌ 엔티티 직접 반환 금지
    // Member getMember(String memberId);
}
```

Facade 메서드는 항상 `global.dto` 패키지에 정의된 공유 DTO 형태로 데이터를 반환했습니다.

---

## 3. Facade 패턴의 한계와 문제점

### 문제 1: 팀 약속에 불과함

위에서 정한 원칙들은 컴파일러나 테스트 도구로 검증할 수 없는 팀원 간의 약속에 불과했습니다.

```java
// 이렇게 해도 컴파일 에러가 발생하지 않음
@Service
public class SomeService {
    private final MemberRepository memberRepository;  // ❌ 규칙 위반

    public void someMethod() {
        memberRepository.findById("123");
    }
}
```

- 실수로 규칙을 위반해도 컴파일 에러가 발생하지 않아서 알아차리지 못할 수도 있음
- 코드 리뷰에 의존해서 팀원들이 직접 확인해주어야 함

### 문제 2: 엔티티 관계 설정 시 예외 허용

새로운 엔티티를 생성할 때에는 JPA를 위해 어쩔 수 없이 연관된 엔티티를 설정해야 합니다.  
하지만 외부 도메인으로 데이터를 넘길 때에는 항상 공유 DTO를 넘기기로 **원칙3**에서 정했기 때문에 방법이 없었습니다. 

- 그래서 엔티티를 생성할 때 관계 설정의 경우에만 프록시 객체를 통한 엔티티 직접 반환 메서드를 추가했습니다.

```java
// MemberQueryFacade.java
public interface MemberQueryFacade {
    // 일반적인 DTO 반환 메서드
    MemberSharedDTO.BasicInfo getMemberBasicInfo(String memberId);

    // ❌ 예외: 관계 설정을 위한 프록시 반환 (원칙 3 위반)
    Member findMemberReferenceById(String memberId);
}

// 사용하는 쪽
@Service
public class BookStoryCommandService {
    private final MemberQueryFacade memberQueryFacade;

    public Long createBookStory(String memberId, CreateDTO dto) {
        // 엔티티 타입(Member)을 알아야 하므로 결합도 증가
        Member memberProxy = memberQueryFacade.findMemberReferenceById(memberId);  // ❌
        BookStory bookStory = BookStory.builder()
            .member(memberProxy)  // JPA가 객체를 요구
            .build();
        // ...
    }
}
```

하지만 이 방식은 서로 다른 도메인의 정보를 알아야만 한다는 문제가 여전히 남았습니다.
- 여전히 외부 도메인의 엔티티 타입(ex. `Member`)을 알아야 함
- Facade가 DTO만 반환한다는 원칙 3을 위반

### 문제 3: Facade가 단순 서비스를 연결만 하는 경우에는 불필요한 Facade를 거치는 느낌

```java
// 단순 조회의 경우
public class MemberQueryFacadeImpl implements MemberQueryFacade {
    private final MemberQueryService memberQueryService;

    @Override
    public MemberSharedDTO.BasicInfo getMemberBasicInfo(String memberId) {
        // 그냥 Service 메서드를 1:1로 호출만 함
        return memberQueryService.getMemberBasicInfo(memberId);
    }
}
```

단순 조회의 경우 Facade가 내부 Service를 1:1로 호출하는 구조가 되어, "굳이 Facade가 필요한가?"라는 의문이 생겼습니다.

---

## 4. 결론: Facade에서 Spring Modulith로

이러한 한계들로 인해 저희는 **Spring Modulith**로 전환을 결정했습니다.

Facade 패턴의 시도를 통해 배운 점:
- 도메인 분리의 중요성 인식
- 모듈 간 통신 방식에 대한 고민
- 엔티티 관계 설정은 근본적인 해결이 필요

Spring Modulith가 이러한 문제들을 어떻게 해결했는지는 [Spring Modulith 아키텍처 문서](./02_spring_modulith.md)를 참고해주세요.