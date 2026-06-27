# RN Expo 푸시 알림 개발 요구사항 정리

> 작성 기준: 2026-06-22 KST PM 요구사항
> RN 기준 코드: `checkmo_rn` `ios-version-3`
> 백엔드 기준: Checkmo `notification` 모듈의 기존 인앱 알림 API

## 1. 목표

현재 인앱 알림 기능에 Expo Push Service 기반 OS 푸시를 연결한다.

앱은 로그인한 회원과 설치 단위 `ExpoPushToken`을 서버에 연결하고, 포그라운드, 백그라운드, 종료 상태에서 알림을 수신하거나 탭했을 때 기존 화면으로 안전하게 이동해야 한다.

## 2. 확정 정책

- 푸시 공급자는 Expo Push Service만 사용한다. FCM/APNs 직접 발송은 1차 범위가 아니다.
- 첫 로그인 또는 업데이트 후 첫 인증 세션에서 안내 UI를 먼저 보여준다.
- 사용자가 안내 UI에서 알림 받기를 선택한 경우에만 OS 권한을 요청한다.
- 사용자가 나중에를 선택하거나 OS 권한을 거부한 뒤에는 자동 재요청하지 않는다.
- 이후 권한 변경은 마이페이지 알림 관리에서 시스템 설정으로 이동시켜 처리한다.
- 기존 유형별 알림 토글은 인앱 알림과 OS 푸시를 함께 제어한다.
- 로그아웃 상태에서 푸시를 탭하면 로그인 후 대상 상세가 아니라 내 알림으로 이동한다.
- 커스텀 사운드, rich image, action button, silent/background data push, OS 앱 아이콘 badge 숫자는 제외한다.

## 3. 현재 확인된 상태

RN 앱 쪽 현재 상태:

- Expo SDK는 `~54.0.35`, React Native는 `0.81.5`다.
- EAS project ID는 `429392b9-bdb4-4d90-9bb5-1cec1fe58fcd`다.
- `expo-notifications`와 푸시 보조 패키지는 아직 설치돼 있지 않다.
- `app.json`에 `expo-notifications` config plugin, Android FCM 설정, notification icon이 없다.
- `ios/app/app.entitlements`에 `aps-environment`가 없다.
- Android manifest에 `POST_NOTIFICATIONS` 권한이 없다.
- 인앱 알림 조회, 미리보기, 읽음 처리, 설정 토글은 `notificationApi.ts`, `AppHeader`, `useNotificationState`에 구현돼 있다.
- `resolveNotificationTarget()`은 6개 알림 유형을 Story, Meeting, UserProfile, My 화면으로 변환한다.
- `NavigationContainer` ref가 없어 종료 상태 알림 탭을 navigator 준비 후 처리할 전역 진입점이 없다.
- `AuthGate`는 로그인 복원, 로그인 완료, 로그아웃 상태를 제공하므로 푸시 생명주기 기준으로 재사용한다.

백엔드 쪽 현재 상태:

- `GET /api/v1/notifications`로 인앱 알림 목록을 조회한다.
- `GET /api/v1/notifications/preview`로 읽지 않은 알림 미리보기를 조회한다.
- `PATCH /api/v1/notifications/{notificationId}/read`로 알림을 읽음 처리한다.
- `GET /api/v1/notifications/settings`로 알림 설정을 조회한다.
- `PATCH /api/v1/notifications/settings/{settingType}`로 유형별 알림 설정을 토글한다.
- 현재 알림 유형은 `LIKE`, `COMMENT`, `FOLLOW`, `JOIN_CLUB`, `CLUB_MEETING_CREATED`, `CLUB_NOTICE_CREATED`다.
- 현재 서버에는 Expo push token 등록, 디바이스 해제, Expo Push Service 발송, receipt 처리 API가 없다.

## 4. RN 의존성과 네이티브 설정

SDK 54 호환 버전을 Expo가 선택하도록 다음 명령을 사용한다.

```bash
npx expo install expo-notifications expo-device expo-constants expo-application expo-crypto
```

패키지별 책임:

- `expo-notifications`: 권한, ExpoPushToken, Android channel, 수신 listener, 응답 listener
- `expo-device`: 실제 기기 여부 확인
- `expo-constants`: EAS project ID 조회
- `expo-application`: native 앱 버전과 build number 조회
- `expo-crypto`: 설치 단위 UUID 생성
- 기존 `expo-secure-store`: 설치 UUID, 권한 안내 상태, 마지막 등록 token 저장

`app.json`에는 `expo-notifications` config plugin을 추가한다.

```json
[
  "expo-notifications",
  {
    "icon": "./assets/notification-icon.png",
    "color": "#8B5E3C",
    "sounds": []
  }
]
```

네이티브 설정 주의사항:

- `assets/notification-icon.png`는 Android 규격의 흰색 단색, 투명 배경 PNG로 새로 제작한다.
- 앱 아이콘을 그대로 축소해 사용하지 않는다.
- Android channel ID는 서버 payload와 동일한 `checkmo-default`로 고정한다.
- channel 이름은 `책모 알림`, importance는 `HIGH`, vibration은 기본값, sound는 `default`를 사용한다.
- native 디렉터리를 추적 중이므로 config plugin 추가 후 `npx expo prebuild`를 실행하고 iOS entitlement와 Android manifest diff를 검토한다.
- 기본 절차에서 `prebuild --clean`은 사용하지 않는다.

Android 자격증명:

- Firebase에서 Android 앱 `kr.co.checkmo.app`을 등록한다.
- `google-services.json`을 프로젝트 루트에 두고 `.gitignore`에 추가한다.
- `app.json`의 `android.googleServicesFile`을 `./google-services.json`으로 지정한다.
- Firebase FCM v1 service account key를 EAS Credentials에 업로드한다.
- Play 제출용 `google-service-account.json`과 FCM의 `google-services.json`을 혼용하지 않는다.

iOS 자격증명:

- Apple Developer에서 `kr.co.checkmo.app` App ID의 Push Notifications capability를 활성화한다.
- EAS Credentials에서 APNs Key를 생성하거나 기존 유효 키를 연결한다.
- prebuild 결과의 `ios/app/app.entitlements`에 빌드 환경에 맞는 `aps-environment`가 반영되는지 확인한다.
- development와 production 빌드 모두 실제 iPhone에서 검증한다.
- iOS simulator 수신 결과는 완료 기준에 포함하지 않는다.

## 5. 서버 API 계약

### 5.1 푸시 디바이스 등록 또는 갱신

```http
PUT /api/v1/notifications/push-devices
Content-Type: application/json
Cookie: access token
```

```json
{
  "installationId": "70cb28d6-1118-4c26-bf14-a73edfde97df",
  "expoPushToken": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]",
  "platform": "IOS",
  "appVersion": "1.0.1",
  "buildNumber": "12"
}
```

```json
{
  "isSuccess": true,
  "code": "COMMON_200",
  "message": "성공입니다.",
  "result": {
    "deviceId": 142,
    "active": true,
    "registeredAt": "2026-06-22T12:00:00"
  }
}
```

요구사항:

- 등록은 idempotent upsert다.
- 같은 설치에서 token이나 계정이 바뀌면 서버가 기존 행을 갱신한다.
- raw `ExpoPushToken`을 URL, 일반 로그, Toast, Sentry breadcrumb에 남기지 않는다.

### 5.2 푸시 디바이스 해제

```http
DELETE /api/v1/notifications/push-devices/{installationId}
Cookie: access token
```

요구사항:

- 해제는 idempotent 해야 한다.
- 같은 요청을 반복해도 성공해야 한다.
- DELETE 실패가 로그아웃을 막지 않아야 한다.

### 5.3 RN wire type

```ts
type PushPlatform = 'IOS' | 'ANDROID';

type PushDeviceRegistration = {
  installationId: string;
  expoPushToken: string;
  platform: PushPlatform;
  appVersion: string;
  buildNumber: string;
};
```

## 6. RN 로컬 상태와 권한 UX

SecureStore key:

```text
checkmo.push.installation-id.v1
checkmo.push.permission-rationale.v1
checkmo.push.last-token.v1
```

상태 관리 요구사항:

- `installationId`가 없으면 `Crypto.randomUUID()`로 만들고 앱 재설치 전까지 재사용한다.
- 권한 안내 상태는 `accepted`, `later` 중 하나만 저장한다.
- OS의 실제 권한 상태는 매번 API로 조회한다.
- `AuthGate`가 `isReady && isLoggedIn`이 된 뒤 권한 상태를 조회한다.
- 이미 `granted`이면 안내 UI 없이 token을 발급하고 서버에 등록한다.
- `undetermined`이고 안내 상태가 없으면 공용 `DialogOverlay` 기반 안내 UI를 한 번 표시한다.
- 알림 받기를 선택하면 안내 상태를 저장하고 `requestPermissionsAsync()`를 호출한다.
- 나중에를 선택하면 `later`를 저장하고 자동 안내를 다시 띄우지 않는다.
- `denied`이면 OS 요청 API를 반복 호출하지 않는다.
- 마이페이지 알림 관리 진입 및 앱이 background에서 active로 돌아올 때 권한 상태를 다시 읽는다.

마이페이지 OS 권한 상태 행:

| OS 상태 | 표시 | 액션 |
| --- | --- | --- |
| `granted` | 푸시 알림 허용됨 | 없음 |
| `undetermined` | 푸시 알림을 허용해 주세요 | 안내 UI 다시 열기 |
| `denied` | 기기 설정에서 알림이 꺼져 있습니다 | 설정에서 허용하기 |

## 7. RN 서비스 구조

신규 모듈:

- `src/services/api/pushDeviceApi.ts`: 등록, 해제 API와 wire type 소유
- `src/services/push/pushStorage.ts`: SecureStore key, UUID, 마지막 token 관리
- `src/services/push/pushNotificationService.ts`: channel, 권한, token 발급, token 변경 listener
- `src/services/push/pushPayload.ts`: payload 검증 및 `NotificationItem` 변환
- `src/components/common/PushNotificationCoordinator.tsx`: 인증, navigation, 수신 listener 연결
- `src/components/common/PushPermissionPrompt.tsx`: 최초 안내 UI

등록 흐름:

1. `AuthGate`와 navigator가 준비될 때까지 기다린다.
2. 로그인 상태에서 실제 기기 여부와 OS 권한을 확인한다.
3. Android에서는 token 발급 전에 `checkmo-default` channel을 생성한다.
4. `Constants.expoConfig.extra.eas.projectId`를 우선 사용하고 `Constants.easConfig.projectId`를 fallback으로 사용한다.
5. `getExpoPushTokenAsync({ projectId })`로 token을 발급한다.
6. 설치 UUID와 앱 정보를 조합해 서버에 `PUT`한다.
7. 성공한 token만 `last-token`에 저장한다.
8. `addPushTokenListener`에서 token이 변경되면 같은 payload로 다시 `PUT`한다.

네트워크 실패 정책:

- 로그인이나 앱 사용을 막지 않는다.
- coordinator가 앱 active 전환 또는 다음 authenticated mount에서 다시 등록한다.
- 한 세션에서 무한 재시도하지 않도록 in-flight promise를 공유한다.

로그아웃과 계정 전환:

- 명시적 로그아웃은 `logoutSession()`보다 먼저 설치 UUID DELETE를 best-effort 호출한다.
- DELETE 실패가 로그아웃을 막지 않는다.
- SecureStore의 설치 UUID는 유지하고 마지막 token 캐시만 삭제한다.
- 다음 계정 로그인 시 `PUT` upsert가 설치 소유자를 새 회원으로 덮어쓴다.
- 세션 만료로 강제 로그아웃된 경우 서버 DELETE가 불가능할 수 있으므로 다음 인증 성공 시 반드시 재등록한다.

## 8. 푸시 수신, 탭, 라우팅

foreground handler는 모듈 초기화 시 한 번만 설정한다.

```ts
{
  shouldShowBanner: true,
  shouldShowList: true,
  shouldPlaySound: true,
  shouldSetBadge: false,
}
```

foreground 수신 정책:

- foreground 수신 시 자동 읽음 처리하지 않는다.
- `AppHeader` 미리보기와 unread 표시를 재조회할 수 있도록 앱 내부 refresh signal을 발행한다.

payload v1:

```json
{
  "schemaVersion": 1,
  "notificationId": 991,
  "notificationType": "COMMENT",
  "domainId": 1234,
  "sourceId": 5678,
  "displayName": "hy_0716"
}
```

payload 규칙:

- 필수 필드는 `schemaVersion`, `notificationId`, `notificationType`, `displayName`이다.
- 선택 필드는 `domainId`, `sourceId`다.
- 알 수 없는 schema, 알 수 없는 type, 잘못된 숫자는 throw하지 않고 fallback 대상으로 변환한다.
- `formatNotificationText()`의 모임형 문구는 백엔드 계획의 공통 문구 표로 교정해 인앱과 OS 푸시 문구를 동일하게 유지한다.

응답 처리:

- 실행 중 탭은 `addNotificationResponseReceivedListener()`로 받는다.
- 종료 상태 탭은 시작 시 `getLastNotificationResponseAsync()`로 읽는다.
- 동일 `notificationId`와 response identifier를 메모리에서 dedupe한다.
- `NavigationContainer` ref의 `isReady()`와 `AuthGate`의 `isReady`를 모두 만족할 때까지 pending response 한 건을 보관한다.
- 새 response가 들어오면 가장 최근 사용자 액션을 우선한다.
- 로그인 중이면 `resolveNotificationTarget()`을 재사용해 이동하고 `markNotificationAsRead()`를 best-effort 호출한다.
- 로그아웃 중이면 `requireAuth()`를 실행하고 로그인 완료 callback에서 My > ALARM으로 이동한다.
- payload 검증 실패 또는 대상 ID 누락도 My > ALARM으로 이동한다.

## 9. 구현 순서

1. 패키지, notification icon, app config를 추가하고 native prebuild diff를 확정한다.
2. push storage와 device API를 구현한다.
3. 권한, token service와 Android channel을 구현한다.
4. 최초 안내 UI와 마이페이지 OS 권한 상태를 연결한다.
5. `NavigationContainer` ref와 coordinator를 추가한다.
6. foreground refresh, live response, cold-start response를 연결한다.
7. 명시적 로그아웃 해제를 연결한다.
8. EAS 자격증명을 등록하고 development build 실제 기기 QA를 수행한다.

## 10. 테스트와 QA

정적 검증:

```bash
npm run check
npx expo prebuild
git diff -- ios android
```

실제 기기 QA 매트릭스:

- iOS/Android 최초 로그인: 안내 표시, 나중에, 허용, 거부
- 이미 허용된 업데이트 사용자: 안내 없이 token 등록
- 마이페이지: OS 상태 표시, 설정 앱 이동, 복귀 후 상태 갱신
- foreground: banner/list/sound 표시, unread refresh, 자동 읽음 방지
- background: 탭 후 6개 유형별 기존 대상 이동
- 종료 상태: cold-start 후 `AuthGate`와 navigator 준비 뒤 한 번만 이동
- 로그아웃 상태 탭: 로그인 후 내 알림 이동
- token 변경: 같은 설치 UUID로 `PUT` 재등록
- 계정 A 로그아웃 후 B 로그인: 서버 소유자 B로 변경
- malformed/unknown payload: crash 없이 내 알림 fallback
- 네트워크 실패: 앱 진입과 로그아웃을 막지 않고 다음 active/login에서 복구

완료 처리에서 제외할 환경:

- Expo Go
- Android emulator
- iOS simulator

## 11. 완료 기준

- 실제 iOS/Android development build에서 Expo Push Service 알림을 수신한다.
- 최초 안내와 OS 권한 요청이 확정 정책대로 한 번만 동작한다.
- 로그인, 로그아웃, 계정 전환, token 변경 시 서버 디바이스 연결이 일관된다.
- foreground, background, 종료 상태와 6개 알림 유형의 이동이 정상이다.
- 잘못된 payload나 서버 오류가 앱 crash 또는 인증 우회를 만들지 않는다.
- `npm run check`와 Expo doctor가 통과한다.
- native 변경 diff가 검토된다.

## 12. 백엔드에서 후속 구현해야 할 항목

현재 백엔드에는 인앱 알림 저장과 조회는 있으나 OS 푸시 발송 표면은 없다. Expo 푸시 연동을 위해 다음 항목이 필요하다.

- `PUT /api/v1/notifications/push-devices` 추가
- `DELETE /api/v1/notifications/push-devices/{installationId}` 추가
- 설치 단위 push device 저장 테이블 추가
- 회원, 설치 UUID, ExpoPushToken, 플랫폼, 앱 버전, 빌드 번호, 활성 여부 저장
- 같은 설치 UUID의 token 또는 회원 변경을 upsert로 처리
- 로그아웃 시 idempotent deactivate/delete 처리
- 기존 `NotificationSetting`의 유형별 토글을 OS 푸시 발송 여부에도 적용
- 인앱 알림 생성 성공 후 Expo Push Service 발송 연결
- Expo push payload v1 생성
- raw `ExpoPushToken`이 일반 로그와 Sentry breadcrumb에 남지 않도록 로깅 정책 적용
- Expo receipt 처리와 invalid token 비활성화 정책 결정

## 13. 개발 전 확인 질문

- Expo Push Service 발송은 백엔드가 직접 수행하는가, 별도 worker나 scheduler를 둘 것인가?
- Expo receipt 조회와 invalid token 비활성화는 1차 범위에 포함하는가?
- push device는 물리 삭제와 비활성화 중 어떤 방식으로 해제할 것인가?
- 한 회원이 여러 기기를 사용하는 경우 모든 활성 기기에 발송하는가?
- 동일 설치 UUID가 다른 회원으로 로그인하면 기존 회원 연결을 즉시 대체하는가?
- 알림 문구의 source of truth는 백엔드 payload title/body인가, RN의 `formatNotificationText()`인가?
- `notificationId`가 없는 운영성 공지나 마케팅 푸시를 허용할 계획이 있는가?
- Expo Push Service 장애 시 인앱 알림 생성 성공을 유지하고 푸시만 실패로 기록하는가?
