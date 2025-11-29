## 🏛️ Architecture

이 프로젝트는 **Spring Modulith**를 활용하여 모놀리식 환경에서 도메인 모듈 분리를 구현했습니다.

기존에는 Facade 패턴을 통해 도메인 간 경계를 나누었지만, 이는 코드로 강제할 수 없는 팀 간 약속에 불과했고 연관관계 생성을 위해 예외적으로 도메인 간의 참조를 허용하는 등 허점이 존재했습니다.
하지만 Spring Modulith를 도입해서 **패키지 구조 자체가 모듈 경계를 강제**하고, **자동화된 검증 테스트가 위반 사항을 검사**하는 구조로 변경했습니다.

각 도메인 모듈은 명확히 정의된 **Public API**와 **이벤트**를 통해서만 통신하며, 외부 도메인 모듈은 내부 구현에 대해서 알지 못하게 추상화, 캡슐화 시켰습니다.

프로젝트의 아키텍처 변화 과정과 현재 구조에 대한 상세한 내용은 아래 문서들을 참고해주세요.

* **[🏛️ Facade 패턴 회고: 시도와 한계](./docs/01_facade_legacy.md)**
* **[🏛️ Spring Modulith 아키텍처 (현재)](./docs/02_spring_modulith.md)**

---

## 📈 Module Dependency Graph

아래의 각 모듈간의 의존성 UML은 Spring Modulith의 [Documenting Application Modules](https://docs.spring.io/spring-modulith/reference/documentation.html)를 참고하여 문서화한 결과입니다.

* [각 모듈들의 의존성 확인하기](./docs/module_graph.md)

---

## 🖥️ Server Architecture

<div align="center">
    <a href="./docs/images/Checkmo_Server_Architecture.png">
    <img src="./docs/images/Checkmo_Server_Architecture.png" alt="서버 아키텍처" width="800"/>
  </a>
</div>

---

## 🚀 Deployment Architecture

<div align="center">
    <a href="./docs/images/Checkmo_AWS_Deploy_Architecture.png">
    <img src="./docs/images/Checkmo_AWS_Deploy_Architecture.png" alt="서버 아키텍처" width="800"/>
  </a>
</div>

---

## 🛠️ Tech Stacks

<div align="center">

**Backend**<br>
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/spring boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/spring data jpa-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<br>

**Database**<br>
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
<img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
<img src="https://img.shields.io/badge/amazon elasticache-2E73B8?style=for-the-badge&logo=amazonaws&logoColor=white">
<br>

**Infra & DevOps**<br>
<img src="https://img.shields.io/badge/amazon aws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
<img src="https://img.shields.io/badge/amazon ecr-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white">

<img src="https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white">
<img src="https://img.shields.io/badge/aws codedeploy-759C3E?style=for-the-badge&logo=amazonaws&logoColor=white">
<img src="https://img.shields.io/badge/amazon s3-E05243?style=for-the-badge&logo=amazons3&logoColor=white">
<br>

**Tools**<br>
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
</div>

---

## 🤝 Development Conventions

프로젝트의 원활한 협업을 위해 Git 브랜치 전략, 커밋 메시지, 코드 스타일 등 통일된 개발 컨벤션을 따르고 있습니다. 자세한 내용은 아래 문서를 참고해주세요.

* **[🚀 팀 개발 컨벤션 바로가기](./docs/conventions.md)**

---

## ⚙️ Development Environment

프로젝트 실행에 필요한 환경 변수(.env) 설정 및 Spring 설정 파일(`application.yml`) 관리 규칙은 아래 문서를 참고해주세요.

* **[⚙️ 개발 환경 설정 가이드](./docs/environment_setup.md)**

---