# 🏛️ 아키텍처 설계: 엔티티 관계 설정 원칙

저희는 **도메인 간의 완벽한 분리**를 목표로 설계를 했습니다. 하지만 이 원칙은 JPA를 사용하여 새로운 객체를 생성하고 관계를 맺어주는 과정에서 심각한 딜레마를 마주하게 했습니다.

---

## 1. 문제점: 객체 생성 시점의 딜레마

가장 큰 문제는 **"다른 도메인을 참조하는 새 객체를 어떻게 생성할 것인가?"** 였습니다.

저희는 아키텍처의 대전제로 두 가지 중요한 규칙을 세웠습니다.

1.  **Facade 접근 원칙**: 도메인들이 서로 다른 외부 도메인에 접근할 때에는 **Query Facade**를 통해서만 접근한다. 이 규칙은 도메인 간의 의존성을 최소화하고, 각 도메인의 독립성을 보장하는 핵심 원칙입니다.
2.  **JPA 연관관계 유지**: 모놀리식 환경의 이점을 살려, JPA가 제공하는 강력한 연관관계 매핑과 기능들은 그대로 활용한다.

그런데 `bookStory` 도메인에서 새로운 `BookStory` 엔티티를 생성하는 순간, 이 두 가지 규칙이 서로 부딪히며 문제가 생겼습니다. `BookStory`는 `Member`와 관계를 맺어야 하므로, JPA는 `newBookStory.setMember(member)`처럼 `Member` **객체**를 넘겨주기를 기대합니다.

`BookStory`가 이 `Member` 객체를 얻어오는 과정에서, 외부 도메인인 `Member` 객체를 가져오려면 반드시 `MemberRepository`를 주입받아와서 사용해야 하는 문제가 발생했습니다.
* **방법 1: `MemberRepository` 직접 주입 (규칙 위반)** : 
  `BookStoryCommandService`가 `MemberRepository`를 직접 주입받는 것은, **"도메인은 다른 도메인의 Repository를 알 수 없다"** 는 저희 팀 규칙에 어긋나는 방식입니다.

    ```java
    // BookStoryCommandService 내부
    @Autowired
    private MemberRepository memberRepository; // ❌ 아키텍처 붕괴의 시작점
    ```

* **방법 2: Facade에서 엔티티 반환 (규칙 위반)** : 
  `MemberQueryFacade`가 `Member` 객체를 반환해주는 것 역시 **"Facade는 오직 약속된 공유 DTO만 반환한다"** 는 우리의 제3원칙을 위반하며, `Member` 엔티티의 내부 구조를 외부에 노출하게 되었습니다.

이처럼 저희는 **객체를 생성하기 위해 규칙을 깨야만 하는** 딜레마에 빠졌습니다.

---

## 2. 해결책: Facade를 통한 '엔티티 프록시' 참조

저희는 이 문제를 해결하기 위해, **Facade에 관계 설정을 위한 특수 목적의 메소드를 추가**하는 방식을 사용했습니다.

### 원리

> **"A 도메인이 B 도메인과의 관계 설정을 위해 B의 참조가 필요할 때, B의 Repository를 직접 호출하는 대신, B의 QueryFacade에게 '프록시' 객체를 요청한다."**

이 프록시 객체는 실제 데이터베이스 조회를 발생시키지 않으면서도, JPA에게 관계를 맺어주기에 필요한 정보인 ID만을 담고 있는 가짜 객체입니다.

### 구현

이를 위해 저희는 `JpaRepository`가 기본으로 제공하는 **`getReferenceById()`** 메서드를 활용했습니다. 이 메소드는 DB 조회 없이 ID를 가진 프록시 객체를 즉시 반환합니다.

그래서 `MemberQueryFacade`에 다음과 같은 약속된 인터페이스를 추가했습니다.

**`MemberQueryFacade.java`**
```java
public interface MemberQueryFacade {
    // ... 기존 DTO 반환 메서드들 ...

    /**
     * 다른 도메인에서 관계 설정을 위해 Member 엔티티의 프록시(참조)를 조회합니다.
     * 이 메서드는 실제 DB 조회를 발생시키지 않으며, 오직 관계 설정 용도로만 사용되어야 합니다.
     */
    Member findMemberReferenceById(String memberId);
}
```
**`MemberQueryFacadeImpl.java`**
```java
@Service
@RequiredArgsConstructor
public class MemberQueryFacadeImpl implements MemberQueryFacade {
    private final MemberRepository memberRepository;

    @Override
    public Member findMemberReferenceById(String memberId) {
        // DB 조회 없이, ID를 가진 프록시(껍데기) 객체만 즉시 반환합니다.
        return memberRepository.getReferenceById(memberId);
    }
}
```
물론 이 방식도 객체 생성 시점에는 외부 도메인의 객체 타입(Member, Book 등등)을 알아야만 한다는 단점과 저희의 규칙 위반 여전합니다. 하지만 저희는 이 최소한의 결합을 허용하는 과정에서 프록시 객체를 활용해서 JPA의 연관관계를 유지하면서 외부 객체의 의존성을 최소로 하려는 노력을 하였습니다.

---

## 3. 최종 사용법: 규칙을 지키는 객체 생성
`BookStoryCommandServiceImpl`은 이제 다른 도메인의 참조를 얻어와 새로운 객체를 생성할 수 있습니다.

**`BookStoryCommandServiceImpl.java`**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class BookStoryCommandServiceImpl implements BookStoryCommandService {

    private final BookStoryRepository bookStoryRepository;
    private final MemberQueryFacade memberQueryFacade; // 다른 도메인의 Facade와 소통
    private final BookQueryFacade bookQueryFacade;     // 다른 도메인의 Facade와 소통

    @Override
    public Long createBookStory(String memberId, BookStoryRequestDTO.BookStoryCreateRequestDTO request) {

        // 1. Facade를 통해 다른 도메인의 프록시 객체를 얻어옵니다.
        //    이 과정에서 DB SELECT 쿼리는 발생하지 않습니다.
        Member authorProxy = memberQueryFacade.findMemberReferenceById(memberId);
        Book bookProxy = bookQueryFacade.findBookReferenceById(request.getBookInfo().getIsbn());

        // 2. 새로운 BookStory 엔티티를 생성하고,
        BookStory newBookStory = BookStoryConverter.toBookStory(request);

        // 3. 프록시 객체를 사용하여 안전하게 관계를 설정합니다.
        newBookStory.setAuthor(authorProxy);
        newBookStory.setBook(bookProxy);

        // 4. 엔티티를 저장합니다.
        bookStoryRepository.save(newBookStory);

        return newBookStory.getId();
    }
}
```

### ✨ 현실적인 최선의 선택...
이 'Facade를 통한 프록시 참조' 패턴은 우리가 마주했던 객체 생성 시점의 문제를 해결하는 현실적인 최선의 선택이었습니다.
후에 진정한 MSA로 전환할 때는 이 문제를 완전히 해결할 수 있을 것으로 기대합니다. 하지만 현재로서는 이 패턴을 통해 도메인 간의 의존성을 최소화하면서도 JPA의 연관관계를 유지하는 방법을 찾았습니다.
* **논리적 도메인 분리** : 프록시 객체는 오직 관계를 맺는 용도로만 사용될 뿐, 내부 상태는 절대 참조하지 않으므로 도메인 간의 논리적 결합을 최소화하는 목표를 달성할 수 있었습니다.

* **JPA 기능 유지** : 백그라운드에서는 여전히 JPA가 트랜잭션과 영속성을 완벽하게 관리해줍니다.

* **성능 최적화** : 관계 설정을 위해 불필요한 SELECT 쿼리를 발생시키지 않으므로 성능적으로 매우 효율적입니다.

저희 프로젝트는 위와 같은 방식을 통해  **"MSA를 모방한 모놀리식" 아키텍처**를 구현하는 가장 현실적인 방법을 선택했습니다.
