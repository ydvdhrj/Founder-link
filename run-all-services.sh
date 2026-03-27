#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/logs"
mkdir -p "${LOG_DIR}"

PIDS=()
NAMES=()

auto_select_java17() {
  local java_major
  java_major="$(java -version 2>&1 | awk -F\" '/version/ {print $2}' | awk -F. '{if ($1=="1") print $2; else print $1}')" || true

  if [[ -n "${java_major}" ]] && (( java_major >= 17 )); then
    return 0
  fi

  # Try well-known JDK 17 paths
  for candidate in /usr/lib/jvm/java-17-openjdk-amd64 /usr/lib/jvm/java-17 /usr/lib/jvm/openjdk-17; do
    if [[ -x "${candidate}/bin/java" ]]; then
      export JAVA_HOME="${candidate}"
      export PATH="${JAVA_HOME}/bin:${PATH}"
      echo "Auto-selected JAVA_HOME=${JAVA_HOME}"
      return 0
    fi
  done

  echo "ERROR: Java 17+ is required but could not be found."
  echo "Install JDK 17 or set JAVA_HOME manually:"
  echo "  export JAVA_HOME=/path/to/jdk-17"
  echo "  export PATH=\$JAVA_HOME/bin:\$PATH"
  exit 1
}

ensure_service_alive() {
  local service_name="$1"
  local pid="$2"
  local log_file="$3"

  # Give Maven/Boot a moment to fail fast if environment is wrong.
  sleep 3
  if ! kill -0 "${pid}" 2>/dev/null; then
    echo "ERROR: ${service_name} exited right after startup."
    echo "Last log lines from ${log_file}:"
    if [[ -f "${log_file}" ]]; then
      tail -n 25 "${log_file}" 2>/dev/null || true
    else
      echo "(log file not created yet)"
    fi
    exit 1
  fi
}

run_service() {
  local service_dir="$1"
  local service_name="$2"
  local log_file="${LOG_DIR}/${service_name}.log"

  echo "Starting ${service_name}..."
  (
    cd "${ROOT_DIR}/${service_dir}"
    if [[ -x "./mvnw" ]]; then
      ./mvnw spring-boot:run
    else
      mvn spring-boot:run
    fi
  ) >"${log_file}" 2>&1 &

  local pid=$!
  PIDS+=("${pid}")
  NAMES+=("${service_name}")
  echo "  -> PID ${pid}, logs: ${log_file}"
  ensure_service_alive "${service_name}" "${pid}" "${log_file}"
}

shutdown_all() {
  echo
  echo "Stopping all services..."
  for i in "${!PIDS[@]}"; do
    local pid="${PIDS[$i]}"
    local name="${NAMES[$i]}"
    if kill -0 "${pid}" 2>/dev/null; then
      echo "  -> Stopping ${name} (${pid})"
      kill "${pid}" 2>/dev/null || true
    fi
  done
  wait || true
  echo "All services stopped."
}

trap shutdown_all INT TERM EXIT

auto_select_java17

echo "FounderLink startup sequence"
echo "Logs folder: ${LOG_DIR}"
echo

# Core platform first
run_service "eureka-server" "eureka-server"
sleep 8
run_service "config-server" "config-server"
sleep 8

# Business services
run_service "auth-service" "auth-service"
run_service "user-service" "user-service"
run_service "startup-service" "startup-service"
run_service "investment-service" "investment-service"
run_service "team-service" "team-service"
run_service "messaging-service" "messaging-service"
run_service "notification-service" "notification-service"

# Gateway last
sleep 6
run_service "api-gateway" "api-gateway"

echo
echo "All services launched."
echo "Press Ctrl+C to stop everything."
echo

wait
