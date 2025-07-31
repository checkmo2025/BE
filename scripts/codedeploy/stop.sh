#!/bin/bash
echo "이전 버전의 애플리케이션을 중지"
cd /home/ubuntu/deployment

# compose.yml 파일이 있으면 실행 중인 컨테이너를 중지하고 삭제합니다.
if [ -f compose.yml ]; then
    docker compose -f compose.yml down --remove-orphans
    echo "기존 컨테이너가 중지 및 삭제"
else
    echo "compose.yml 파일이 없어서 중지할 것 컨테이너가 없는 상태.
fi