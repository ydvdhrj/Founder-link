#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"

echo "Stopping any old local Java processes on service ports..."
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8761 8888; do
  pid="$(ss -tlnp 2>/dev/null | awk -v p=":${port} " '$0 ~ p { if (match($0,/pid=[0-9]+/)) print substr($0,RSTART+4,RLENGTH-4)}' | head -1 || true)"
  if [[ -n "${pid}" ]]; then
    echo " - killing pid ${pid} on port ${port}"
    kill "${pid}" || true
  fi
done

echo "Building docker images..."
docker compose -f "${COMPOSE_FILE}" build

echo "Starting docker stack..."
docker compose -f "${COMPOSE_FILE}" up -d

echo
echo "FounderLink docker stack started."
echo "Gateway:  http://localhost:8080/swagger-ui.html"
echo "Eureka:   http://localhost:8761"
echo "Config:   http://localhost:8888/actuator/health"
echo "Zipkin:   http://localhost:9411"
echo "RabbitMQ: http://localhost:15672"
