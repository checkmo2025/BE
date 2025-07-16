# 💻 Backend Members

| [**모두까기** / **임경표**](https://github.com/MODUGGAGI) |  [**채이** / **이채은**](https://github.com/chaechaen)  | [**송글송글** / **신지윤**](https://github.com/Yoon0221) | [**지니** / **정효정**](https://github.com/zjhj0814) |
|:----------------------:|:----------------------:|:----------------------:|:--------------------:|
|       팀장 🧑🏻‍💻       |       팀원 👩🏻‍💻       |       팀원 👩🏻‍💻       |      팀원 👩🏻‍💻      |

---
# 🚀 백엔드 개발 컨벤션

---

## 🌳 깃 브랜치 전략

* `main` 브랜치:
    * **디폴트 브랜치**
    * 👑 팀장만 직접 관리하고 머지
* `develop` 브랜치:
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

## 🤝 PR (Pull Request) 전략

* `main` 브랜치 PR:
    * 👑 **팀장만 승인 및 머지** 가능
* **그 외 브랜치 PR**:
    * 👥 `main` 브랜치 외 다른 브랜치로 머지할 때는 **최소 1명 이상의 추가 승인** 후 머지 가능

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
# ⚙️ 개발 환경 설정

## 1. 환경 변수 (.env)

프로젝트에 필요한 주요 정보(DB 접속 정보, JWT 시크릿 키 등)는 **환경 변수**로 관리합니다.

1.  프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 해당 파일에 환경변수들을 추가해주세요. (`.gitignore`에 등록되어 있어 원격 저장소에는 올라가지 않습니다.)
2.  백엔드 `[Notion 페이지]` -> `[필요한 자료들]` -> `.env` 페이지에 추가한 환경 변수들을 업데이트해주세요.

## 2. Spring 설정 파일 (application.yml)

새로운 설정 정보(예: `aws`, `oauth`)를 추가할 때는 `application.yml`에 직접 작성하지 말아주세요.

1.  `src/main/resources` 경로에 `application-OOO.yml` 형식으로 새로운 설정 파일을 생성합니다. (예: `application-aws.yml`)
2.  생성한 파일을 `application.yml`의 `spring.profiles.include` 부분에 추가하여 적용합니다.

**예시:**

```yaml
# application.yml

spring:
  profiles:
    include:
      - db
      - redis
      - aws   # 새로 추가된 설정