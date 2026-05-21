#!/bin/bash

# 1. Git 저장소 여부 확인
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    echo "Error: Not a git repository."
    exit 1
fi

# 2. 변경 사항(diff) 추출
# 현재 작업 트리와 최신 커밋 간의 모든 차이점을 변수에 저장
DIFF_DATA=$(git diff HEAD)

if [ -z "$DIFF_DATA" ]; then
    echo "리뷰할 변경 사항이 없습니다."
    exit 0
fi

# 3. AI 리뷰 요청 프롬프트 설정
# AI가 판정 결과를 명확히 알 수 있도록 'PASS' 또는 'FAIL'을 첫 줄에 출력하도록 요청
PROMPT="당신은 10년 차 시니어 백엔드/DevOps 엔지니어입니다. 
다음 git diff 내용을 리뷰하고, 치명적인 버그나 보안 취약점이 없다면 첫 줄에 반드시 'PASS'라고 적어주세요. 
만약 수정이 필요한 문제가 있다면 첫 줄에 'FAIL'이라고 적고 그 이유를 상세히 설명해주세요.

[GIT DIFF]
$DIFF_DATA"

echo "AI 코드 리뷰 진행 중..."

# 4. gemini 호출 및 결과 수신
# 터미널에 설치된 gemini 명령어를 사용하여 리뷰 결과를 가져옴
RESPONSE=$(gemini "$PROMPT")

# 5. 결과 판독 (첫 번째 줄에서 PASS/FAIL 여부 확인)
# 대소문자 구분 없이 처리하기 위해 소문자로 변환 후 비교
STATUS=$(echo "$RESPONSE" | head -n 1 | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')

if [[ "$STATUS" == *"pass"* ]]; then
    echo "리뷰 통과!"
    echo "--------------------------------------------------"
    echo "$RESPONSE"
    echo "--------------------------------------------------"

    # 6. 자동 커밋 및 푸시 실행
    git add .
    git commit -m "[AI 자동 커밋] 코드 업데이트"
    
    echo "깃허브로 푸시 중..."
    git push origin main
    
    echo "성공: 변경 사항이 커밋되고 깃허브에 푸시되었습니다."
else
    echo "리뷰 실패: 수정이 필요한 항목이 발견되었습니다."
    echo "--------------------------------------------------"
    echo "$RESPONSE"
    echo "--------------------------------------------------"
    exit 1
fi
