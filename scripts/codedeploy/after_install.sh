#!/bin/bash
echo "파일 권한을 설정하고 새 이미지를 다운"
cd /home/ubuntu/deployment

# 1. 파일 권한 설정
sudo chown -R ubuntu:ubuntu .
chmod +x ./scripts/codedeploy/*.sh

# 2. AWS ECR 로그인
echo "AWS ECR에 로그인"
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 667183278509.dkr.ecr.ap-northeast-2.amazonaws.com

# 3. 최신 Docker 이미지 다운로드 (pull)
echo "최신 Docker 이미지를 다운로드"
docker compose pull app

echo "AfterInstall 단계가 완료"