# 기획 문서: 회원 정보 수정 (User Profile Update)

## 1. 배경 및 목적
사용자가 가입 후 이름이나 연락처가 변경되었을 때, 이를 서비스 내에서 직접 수정할 수 있도록 하여 데이터의 최신성을 유지하고 당첨 알림 등 서비스 이용에 차질이 없도록 함.

## 2. 요구사항 개요
- **수정 대상**: 사용자 이름(`username`), 전화번호(`phoneNumber`).
- **제한 사항**: 
  - **이메일(email)**: 고유 식별자(ID) 역할을 하므로 수정이 불가능함.
  - **전화번호 중복 체크**: 번호 수정 시 이미 다른 사용자가 사용 중인 번호라면 수정을 제한해야 함.
- **포맷팅**: 전화번호 입력 시 자동 하이픈(`-`) 처리를 통해 데이터 일관성 유지.

## 3. API 명세 (API Specification)

### 3.1 내 정보 수정
- **Endpoint**: `PUT /api/user/me`
- **Request Header**: `Authorization: Bearer {token}`
- **Request Body**:
```json
{
  "username": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```
- **Response Body**:
  - **Success (200 OK)**:
    ```json
    {
      "username": "홍길동",
      "phoneNumber": "010-1234-5678",
      "email": "user@example.com",
      "raffleCount": 5,
      "quizMode": "EN_TO_KO"
    }
    ```
  - **Error (400 Bad Request)**: 전화번호 중복 등 벨리데이션 실패 시.
    ```json
    {
      "message": "이미 사용 중인 전화번호입니다."
    }
    ```

## 4. UI/UX 설계 (UI Design)

### 4.1 정보 수정 흐름 (Flow)
1. **조회 모드**: 마이페이지 'User Profile' 섹션 우측에 '수정' 아이콘(또는 버튼) 배치.
2. **편집 모드 전환**: '수정' 클릭 시, 텍스트가 입력 폼(`input`)으로 전환됨.
   - `username`: 일반 텍스트 입력.
   - `phoneNumber`: 숫자 입력 및 자동 하이픈 적용.
   - `email`: `disabled` 처리 또는 텍스트로 유지하여 수정 불가임을 명시.
3. **저장/취소**: 폼 하단에 '저장' 및 '취소' 버튼 노출.
   - **저장**: API 호출 후 성공 시 다시 조회 모드로 복귀하며 변경된 데이터 반영.
   - **취소**: 변경 사항을 무시하고 초기 데이터 상태로 복귀.

### 4.2 컴포넌트 상세
- **Input Styling**: 기존 `Auth.tsx`의 스타일(rounded-xl, border-2)을 계승하여 통일감 유지.
- **Phone Number Formatting**: `Auth.tsx`에 구현된 `formatPhoneNumber` 로직을 재사용하여 `010-0000-0000` 형태 강제.

## 5. 시스템 흐름 (System Flow)

1. **상태 관리**: `MyPage` 컴포넌트 내에서 `isEditing` (boolean) 상태를 두어 모드 전환 관리.
2. **벨리데이션**: 
   - 프론트엔드: 이름 공백 체크, 전화번호 형식(13자리) 체크.
   - 백엔드: DB 내 `phoneNumber` Unique 제약 조건 확인 및 중복 에러 반환.
3. **업데이트**: 성공 시 전역 상태 또는 `userInfo` 상태를 업데이트하여 즉각적인 피드백 제공.
