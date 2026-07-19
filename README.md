# 레시피 하나로 일상이 특별해지는 순간
# Dishcovery

Dishcovery는 사용자가 직접 레시피를 등록하고, 공유하고, 서로 소통하며,
요리에 필요한 재료를 포인트로 구매할 수 있는 레시피 커뮤니티 서비스입니다.
웹(Thymeleaf)과 iOS 앱(SwiftUI)이 하나의 Spring Boot 서버를 함께 사용합니다.

## 주요 기능

- **레시피**: 등록/수정/삭제, 검색, 댓글, 좋아요, AI 레시피 추천(OpenAI 연동)
- **상점**: 포인트로 재료 구매, 리뷰(구매자만 작성 가능)/문의, 구매 내역 조회
- **계정**: 회원가입(이메일 인증), 로그인(JWT), 비밀번호 찾기/재설정, 닉네임 변경, 회원 탈퇴
- **포인트**: 레시피 조회 시 작성자에게 포인트 적립, 상점에서 포인트로 구매

## 기술 스택

**서버** (`server/`)
- Java 17, Spring Boot 3.5.6
- Spring Web, Spring Security, MyBatis (MySQL)
- Thymeleaf (웹 화면), JWT(jjwt) 기반 인증
- Spring Mail (이메일 인증/비밀번호 재설정), OpenAI API 연동 (AI 레시피 추천)

**웹**: 서버의 Thymeleaf 템플릿(`server/src/main/resources/templates`)

**iOS 앱** (`app/`)
- SwiftUI, iOS 16.4+
- 서버의 REST API(`/api/**`)를 통해 웹과 동일한 기능 제공

## 프로젝트 구조

```
Dishcovery/
├── server/   # Spring Boot 백엔드 (웹 화면 + REST API)
└── app/      # iOS 클라이언트 (SwiftUI)
```

## 시작하기

서버 실행 전 `server/src/main/resources/application.properties.example`을
참고해 `application.properties`를 만들고 DB, JWT, 메일, OpenAI 설정값을 채워주세요.

```bash
cd server
./gradlew bootRun
```

iOS 앱은 `app/Dishcovery.xcodeproj`를 Xcode로 열어 실행합니다.
`app/Dishcovery/Common/Constants.swift`의 `API.baseURL`을 서버 주소에 맞게 설정해야 합니다.
