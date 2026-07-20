# Prometheus/Grafana 모니터링 확인 가이드

Issue: #296
Branch: `feat/296/prometheus-grafana-monitoring`

이 문서는 모니터링이 실제로 동작하는지 확인하는 절차다. 운영 서버에 접속한 뒤 위에서부터 순서대로 확인하면 된다.

## 한눈에 보는 흐름

```text
1. Docker 컨테이너가 떠 있는지 확인
2. Spring Boot가 management port에서 /actuator/prometheus를 내보내는지 확인
3. Prometheus가 app:8081/actuator/prometheus를 수집하는지 확인
4. Grafana에 접속해서 대시보드가 보이는지 확인
5. 실제 API를 한 번 호출하고 Grafana 숫자가 바뀌는지 확인
```

## 0. 준비

운영 EC2에 SSH로 접속한다.

```bash
ssh ubuntu@<ec2-host>
```

프로젝트가 배포된 디렉토리로 이동한다.

```bash
cd /home/ubuntu/app
```

서버에는 아래가 준비되어 있어야 한다.

| 필요 항목 | 이유 |
| --- | --- |
| Docker Engine | `app`, `prometheus`, `grafana`, `nginx` 컨테이너 실행 |
| Docker Compose plugin | `docker compose up -d` 실행 |
| AWS CLI | 배포 workflow가 EC2에서 ECR 로그인할 때 사용 |
| ECR 접근 권한 | 백엔드 앱 이미지를 pull할 때 필요 |
| Docker Hub 접근 가능 네트워크 | `prom/prometheus`, `grafana/grafana-oss` 이미지를 pull할 때 필요 |
| `/home/ubuntu/app/monitoring` 디렉토리 | Prometheus/Grafana 설정 파일 마운트 |
| 충분한 디스크 공간 | Prometheus/Grafana 데이터 volume 저장 |

Prometheus와 Grafana를 서버 OS에 직접 설치할 필요는 없다. Docker Compose가 이미지를 받아서 컨테이너로 실행한다.

Grafana 실행에는 `.env` 또는 배포 환경에 아래 값이 있어야 한다.

```text
GRAFANA_ADMIN_USER=<Grafana 로그인 ID>
GRAFANA_ADMIN_PASSWORD=<Grafana 로그인 비밀번호>
GRAFANA_PORT=3000
```

`GRAFANA_PORT`는 없으면 기본값 `3000`을 사용한다.

GitHub Actions 배포를 사용할 때는 repository Actions secrets에 최소한 아래 두 값을 추가한다.

```text
GRAFANA_ADMIN_USER
GRAFANA_ADMIN_PASSWORD
```

`GRAFANA_PORT`, `GRAFANA_ROOT_URL`은 필요할 때만 Actions secrets에 추가한다. 배포 workflow가 이 값들을 `.env`에 기록한 뒤 EC2로 전송한다.

## 1. 컨테이너 실행 확인

```bash
docker compose ps
```

정상이면 최소한 아래 컨테이너들이 `running` 또는 `healthy` 상태여야 한다.

```text
checkmo-app
checkmo-prometheus
checkmo-grafana
```

`checkmo-prometheus`나 `checkmo-grafana`가 없으면 모니터링 컨테이너를 올린다.

```bash
docker compose up -d prometheus grafana
```

Grafana가 바로 종료되면 환경 변수가 빠졌을 가능성이 높다.

```bash
docker compose logs grafana
```

`GRAFANA_ADMIN_USER is required` 또는 `GRAFANA_ADMIN_PASSWORD is required`가 보이면 `.env`에 Grafana 계정 값을 추가해야 한다.

## 2. 백엔드 메트릭 endpoint 확인

앱 컨테이너 안에서 management port의 `/actuator/prometheus`가 열리는지 확인한다.

```bash
docker compose exec app curl -fsS http://localhost:8081/actuator/prometheus | head
```

정상이면 이런 형태의 텍스트가 나온다.

```text
# HELP ...
# TYPE ...
```

아래 이름 중 일부가 보이면 Actuator와 Micrometer가 정상 동작하는 상태다.

```text
http_server_requests_seconds_count
jvm_memory_used_bytes
hikaricp_connections_active
```

아무것도 안 나오거나 실패하면 앱 로그를 본다.

```bash
docker compose logs app
```

확인할 설정은 다음이다.

| 확인할 것 | 정상 기준 |
| --- | --- |
| `build.gradle` | `spring-boot-starter-actuator`, `micrometer-registry-prometheus` 포함 |
| `application.yml` | `spring.profiles.include`에 `monitoring` 포함 |
| `application-monitoring.yml` | `management.endpoints.web.exposure.include`에 `prometheus` 포함 |

## 3. Prometheus 수집 확인

Prometheus가 백엔드에서 메트릭을 가져오는지 확인한다.

```bash
docker compose exec prometheus promtool query instant http://localhost:9090 'up{job="checkmo-app"}'
```

정상 예시는 다음과 같다.

```text
up{instance="app:8081", job="checkmo-app"} => 1
```

의미는 다음과 같다.

| 값 | 의미 |
| --- | --- |
| `1` | Prometheus가 백엔드 메트릭을 정상 수집 중 |
| `0` | target은 등록됐지만 수집 실패 |
| 결과 없음 | Prometheus 설정에 `checkmo-app` job이 없거나 Prometheus가 아직 설정을 못 읽음 |

수집이 실패하면 Prometheus 설정을 확인한다.

```bash
docker compose exec prometheus cat /etc/prometheus/prometheus.yml
```

아래 설정이 있어야 한다.

```yaml
scrape_configs:
  - job_name: checkmo-app
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - app:8081
```

그 다음 Prometheus 로그를 확인한다.

```bash
docker compose logs prometheus
```

## 4. Grafana 접속 확인

Grafana는 외부에 바로 열지 않고 EC2 localhost에만 열려 있다. 로컬 PC에서 보려면 SSH 터널을 연다.

로컬 PC 터미널에서 실행한다.

```bash
ssh -L 3000:localhost:3000 ubuntu@<ec2-host>
```

그 다음 브라우저에서 접속한다.

```text
http://localhost:3000
```

로그인 정보는 `.env`에 넣은 값이다.

```text
ID: GRAFANA_ADMIN_USER
PW: GRAFANA_ADMIN_PASSWORD
```

접속 후 아래 순서로 확인한다.

```text
Dashboards
-> Checkmo
-> Checkmo Backend Overview
```

대시보드가 보이면 Grafana provisioning이 정상 동작한 것이다.

## 5. Grafana datasource 확인

Grafana 화면에서 Prometheus 연결이 정상인지 확인한다.

```text
Connections
-> Data sources
-> Prometheus
-> Save & test
```

정상이면 `Successfully queried the Prometheus API`와 비슷한 성공 메시지가 나온다.

실패하면 datasource URL이 아래 값인지 확인한다.

```text
http://prometheus:9090
```

Grafana 컨테이너는 Prometheus와 같은 Docker network에 있기 때문에 `localhost:9090`이 아니라 `prometheus:9090`을 사용해야 한다.

## 6. 실제 API 호출 후 숫자 변화 확인

모니터링은 요청이 들어와야 숫자가 늘어난다. 서버에 안전한 요청을 몇 번 보낸다.

운영 서버 안에서 확인한다.

```bash
docker compose exec app curl -fsS http://localhost:8080/health
```

외부 공개 API가 정상 배포되어 있다면 로컬 PC에서도 앱 API를 몇 번 호출한다.

```bash
curl -fsS https://<api-domain>/health
```

그 다음 Grafana 대시보드에서 시간 범위를 `Last 15 minutes`로 두고 새로고침한다.

확인할 패널은 다음이다.

| 패널 | 정상 확인 기준 |
| --- | --- |
| Request rate | API 호출 후 선이 움직이거나 값이 증가 |
| Response time | 요청 처리 시간이 표시 |
| 5xx rate | 정상 요청만 보냈다면 0에 가까움 |
| JVM memory | 메모리 사용량이 표시 |
| HikariCP | active/idle connection 값이 표시 |

## 7. Custom metric 확인

Custom metric은 해당 기능을 실제로 호출해야 생긴다.

Prometheus에서 Aladin API metric이 쌓였는지 확인한다.

```bash
docker compose exec prometheus promtool query instant http://localhost:9090 'checkmo_aladin_client_requests_total'
```

추천 cache metric을 확인한다.

```bash
docker compose exec prometheus promtool query instant http://localhost:9090 'checkmo_book_recommendation_cache_requests_total'
```

배너성 API metric을 확인한다.

```bash
docker compose exec prometheus promtool query instant http://localhost:9090 'checkmo_banner_api_duration_seconds_count'
```

결과가 없으면 보통 둘 중 하나다.

| 상황 | 의미 |
| --- | --- |
| 해당 API를 아직 호출하지 않음 | metric은 첫 호출 이후 생성됨 |
| API 호출은 했는데 결과 없음 | 코드 경로가 다른 API를 탔거나 Prometheus scrape 전일 수 있음 |

이 경우 해당 API를 한 번 호출한 뒤 15초 정도 기다리고 다시 확인한다. Prometheus scrape 주기가 15초이기 때문이다.

## 8. 장애 상황에서 어디를 볼지

### 서버가 느리다

Grafana에서 먼저 본다.

```text
Checkmo Backend Overview
-> Response time
-> endpoint별 p95
```

같이 확인할 패널은 다음이다.

| 같이 볼 것 | 판단 |
| --- | --- |
| Aladin API duration | 외부 API 때문에 느린지 확인 |
| Recommendation cache hit/miss | cache miss 때문에 느린지 확인 |
| HikariCP pending | DB connection을 기다리는지 확인 |
| JVM memory / GC | Java 메모리나 GC 문제인지 확인 |

### 500 에러가 늘어난다

Grafana에서 5xx가 늘어난 endpoint를 확인한 뒤 Sentry로 이동한다.

```text
Grafana: 어떤 endpoint에서 5xx가 늘었는지 확인
Sentry: 같은 시간대의 예외 상세 확인
```

Grafana는 "어디서 얼마나 자주 터졌는지"를 보고, Sentry는 "왜 터졌는지"를 본다.

### Aladin timeout이 의심된다

Prometheus에서 timeout 결과만 확인한다.

```bash
docker compose exec prometheus promtool query instant http://localhost:9090 'checkmo_aladin_client_requests_total{result="timeout"}'
```

값이 계속 증가하면 Aladin API timeout이 실제로 발생하고 있는 것이다.

## 9. 최종 정상 기준

아래가 모두 만족되면 1차 모니터링은 정상 도입된 상태로 보면 된다.

| 체크 | 정상 기준 |
| --- | --- |
| 컨테이너 | `checkmo-app`, `checkmo-prometheus`, `checkmo-grafana` 실행 중 |
| Actuator | `/actuator/prometheus`에서 Prometheus 형식 텍스트 출력 |
| Prometheus | `up{job="checkmo-app"}` 값이 `1` |
| Grafana 접속 | SSH 터널 후 `http://localhost:3000` 로그인 가능 |
| Dashboard | `Checkmo Backend Overview` 표시 |
| 기본 metric | 요청 수, 응답 시간, JVM, HikariCP 값 표시 |
| custom metric | 관련 API 호출 후 Aladin/cache/banner metric 표시 |

## 외부 공개 여부 확인

Prometheus와 Actuator는 외부에 공개하면 안 된다.

운영 서버 밖의 로컬 PC에서 아래 URL이 열리지 않아야 한다.

```text
http://<ec2-public-ip>:9090
http://<api-domain>/actuator/prometheus
```

Grafana도 현재는 EC2 localhost에만 바인딩되어 있으므로, SSH 터널 없이 외부에서 바로 열리지 않는 것이 정상이다. `/actuator/prometheus`는 app port `8080`이 아니라 management port `8081`에서만 제공되며, `8081`은 Docker 내부 network에서 Prometheus가 수집하는 용도로만 사용한다.

운영자가 Grafana를 상시 접속해야 한다면 SSH 터널, VPN, 인증 reverse proxy, SSO 중 하나를 별도 결정해야 한다.

## 자주 나는 문제

| 증상 | 확인할 것 |
| --- | --- |
| Grafana 컨테이너가 바로 종료됨 | `GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD` 누락 |
| Dashboard가 비어 있음 | 시간 범위가 너무 짧거나 API 요청이 아직 없음 |
| Prometheus 값이 없음 | scrape 주기 15초 대기 후 재확인 |
| `up{job="checkmo-app"}`가 0 | 앱 health, Docker network, `/actuator/prometheus` 응답 확인 |
| custom metric이 없음 | 해당 기능 API를 실제로 호출했는지 확인 |
| 앱이 로컬에서 안 뜸 | 모니터링 문제가 아니라 DB/Redis 운영 profile 환경 변수 문제일 수 있음 |

## 중지

모니터링만 중지하려면 Prometheus와 Grafana를 멈춘다.

```bash
docker compose stop prometheus grafana
```

백엔드 앱은 계속 동작한다.
