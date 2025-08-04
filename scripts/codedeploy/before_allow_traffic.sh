#!/bin/bash
echo "$(date): BeforeAllowTraffic - 트래픽 전환 준비"

cd /home/ubuntu/deployment
HEALTH_CHECK_URL="http://localhost/health"

sleep 5

echo "최종 헬스체크 수행 ($HEALTH_CHECK_URL)"
for i in {1..10}; do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_CHECK_URL)

    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "✅ 애플리케이션 헬스체크 성공! (상태 코드: $HTTP_STATUS) 트래픽 전환 준비 완료"
        exit 0
    else
        echo "⏳ 헬스체크 시도 $i/10... 실패 (응답 코드: $HTTP_STATUS)"
    fi
    sleep 3
done

echo "----------------------------------------------------"
echo "❌ 애플리케이션이 준비되지 않음, 트래픽 전환 중단"
echo "마지막 헬스체크 시도에 대한 상세 정보:"
curl -v $HEALTH_CHECK_URL
echo "----------------------------------------------------"
exit 1