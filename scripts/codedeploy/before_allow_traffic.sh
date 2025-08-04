#!/bin/bash
echo "$(date): BeforeAllowTraffic - 트래픽 전환 준비"

# 애플리케이션이 완전히 준비되었는지 최종 확인
cd /home/ubuntu/deployment

echo "최종 헬스체크 수행"
for i in {1..10}; do
    if curl -f http://localhost/health > /dev/null 2>&1; then
        echo "애플리케이션 헬스체크 성공! 트래픽 전환 준비 완료"
        exit 0
    fi
    echo "⏳ 헬스체크 시도 $i/10..."
    sleep 3
done

echo "애플리케이션이 준비되지 않음, 트래픽 전환 중단 ㅠ"
exit 1