#!/bin/bash
echo "새 버전의 애플리케이션을 시작"
cd /home/ubuntu/deployment

# 1. Docker Compose로 모든 서비스 시작
echo "Docker Compose로 서비스를 시작"
docker compose up -d --remove-orphans

# 2. 불필요한 Docker 이미지 정리
echo "불필요한 Docker 이미지를 정리"
docker image prune -f

echo "배포가 성공!"