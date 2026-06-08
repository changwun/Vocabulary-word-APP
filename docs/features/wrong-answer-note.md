# 기획 문서: 나만의 단어장 (오답 노트)

## 1. 배경 및 목적
유저가 퀴즈에서 틀린 단어를 단순히 확인하는 것에 그치지 않고, 별도의 단어장에 저장하여 반복 학습할 수 있도록 함으로써 서비스 리텐션과 학습 효과를 극대화함.

## 2. 요구사항 개요
- **저장**: 퀴즈 완료 시 틀린 단어를 유저별 오답 테이블에 자동 저장.
- **조회**: 마이페이지 내 '오답 노트' 섹션에서 틀린 단어 목록(단어, 뜻, 틀린 횟수 등)을 확인.
- **관리**: 복습이 완료된 단어는 '학습 완료' 처리를 통해 목록에서 제거.
- **통계**: 단어별로 몇 번 틀렸는지 누적 횟수를 기록하여 취약 단어 파악.

## 3. 데이터 모델 (Data Model)

### WrongAnswer (오답 엔티티)
| 필드명 | 타입 | 설명 | 비고 |
| :--- | :--- | :--- | :--- |
| id | Long | 식별자 | PK |
| user_id | Long | 사용자 ID | FK (User) |
| word_id | Long | 단어 ID | FK (Word) |
| count | Integer | 틀린 횟수 | 기본값 1, 중복 발생 시 증가 |
| created_at | DateTime | 최초 등록일 | |
| updated_at | DateTime | 최근 오답일 | |

## 4. API 명세 (API Specification)

### 4.1 오답 목록 조회
- **Endpoint**: `GET /api/wrong-answers`
- **Request Header**: `Authorization: Bearer {token}`
- **Response Body**:
```json
[
  {
    "id": 1,
    "wordId": 101,
    "english": "apple",
    "korean": "사과",
    "wrongCount": 3,
    "lastAttemptAt": "2023-10-27T10:00:00"
  },
  ...
]
```

### 4.2 오답 상세 조회 (선택 사항)
- **Endpoint**: `GET /api/wrong-answers/{id}`

### 4.3 학습 완료 처리 (삭제)
- **Endpoint**: `DELETE /api/wrong-answers/{id}`
- **Description**: 유저가 해당 단어를 외웠다고 판단하여 목록에서 제거함.

## 5. UI/UX 설계 (UI Design)

### 5.1 마이페이지 내 섹션 배치
- **위치**: 'Quiz Preference' 섹션 하단에 'My Wrong Answers' 섹션 추가.
- **형태**: 카드형 리스트 또는 테이블 형태.

### 5.2 컴포넌트 구성
- **Header**: "오답 노트 (N개)"
- **List Item**:
  - 좌측: 단어 (English / Korean)
  - 중앙: 틀린 횟수 (예: "3회 틀림") - 뱃지 형태
  - 우측: '학습 완료' 버튼 (체크 아이콘)
- **Empty State**: "틀린 단어가 없습니다. 완벽해요! 👏"

## 6. 시스템 흐름 (System Flow)

1. **퀴즈 종료**: `QuizService.completeQuiz` 로직 내에서 오답이 발생한 경우.
2. **데이터 기록**:
   - 기존에 해당 유저/단어 조합의 오답 기록이 있는지 확인.
   - 있다면 `count`를 +1 하고 `updated_at` 갱신.
   - 없다면 신규 레코드 생성.
3. **프론트엔드 연동**: 마이페이지 진입 시 오답 API를 호출하여 목록 렌더링.
4. **학습 완료**: 유저가 버튼 클릭 시 삭제 API 호출 후 클라이언트 상태 업데이트.
