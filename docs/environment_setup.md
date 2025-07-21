# ⚙️ 개발 환경 설정

## 1. 환경 변수 (.env)

프로젝트에 필요한 주요 정보(DB 접속 정보, JWT 시크릿 키 등)는 **환경 변수**로 관리

1.  프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 해당 파일에 환경변수들을 추가하기. (`.gitignore`에 등록되어 있어 깃허브에는 따로 올라가지 않음.)
2.  백엔드 `[Notion 페이지]` -> `[필요한 자료들]` -> `.env` 페이지에 추가한 환경 변수들 업데이트하기.

## 2. Spring 설정 파일 (application.yml)

새로운 설정 정보(예: `aws`, `oauth`)를 추가할 때는 `application.yml`에 직접 작성하지 않기

1.  `src/main/resources` 경로에 `application-OOO.yml` 형식으로 새로운 설정 파일을 생성. (예: `application-aws.yml`)
2.  생성한 파일을 `application.yml`의 `spring.profiles.include` 부분에 추가하여 적용.

**예시:**

```yaml
# application.yml

spring:
  profiles:
    include:
      - db
      - redis
      - aws   # 새로 추가된 설정