#!/bin/bash
echo "$(date): AfterAllowTraffic - 트래픽 전환 완료!"

# 트래픽 전환 후 상태 확인
echo "트래픽 전환 후 상태 확인..."
cd /home/ubuntu/deployment

# 애플리케이션 로그 확인
echo "최근 애플리케이션 로그:"
docker compose logs --tail=10

echo "Blue/Green 배포가 성고옹"