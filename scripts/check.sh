#!/usr/bin/env bash
# Verificação rápida de saúde da stack do SRE Operations Dashboard.
set -Eeuo pipefail

BASE_URL="${BASE_URL:-http://localhost}"
FAILURES=0

log() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
ok() { printf '\033[1;32m  [OK]\033[0m %s\n' "$*"; }
fail() { printf '\033[1;31m  [FALHOU]\033[0m %s\n' "$*"; FAILURES=$((FAILURES + 1)); }

check_containers() {
  log "Containers ativos"
  docker compose ps
  local running
  running=$(docker compose ps --status running --quiet | wc -l)
  if [ "$running" -ge 3 ]; then
    ok "$running containers em execução"
  else
    fail "Esperados pelo menos 3 containers em execução, encontrados $running"
  fi
}

check_backend_health() {
  log "Health check do backend ($BASE_URL/actuator/health)"
  if curl -fsS "$BASE_URL/actuator/health" | grep -q '"status":"UP"'; then
    ok "Backend saudável (status UP)"
  else
    fail "Backend não respondeu UP em $BASE_URL/actuator/health"
  fi
}

check_frontend() {
  log "Página principal do frontend ($BASE_URL/)"
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/")
  if [ "$code" = "200" ]; then
    ok "Frontend respondeu HTTP 200"
  else
    fail "Frontend respondeu HTTP $code"
  fi
}

check_api() {
  log "Endpoint da API ($BASE_URL/api/services)"
  if curl -fsS "$BASE_URL/api/services" | grep -q '\['; then
    ok "API respondeu com uma lista de serviços"
  else
    fail "API não respondeu corretamente em $BASE_URL/api/services"
  fi
}

check_resources() {
  log "Recursos do servidor"
  echo "--- Disco ---"
  df -h / | tail -n 1
  echo "--- Memória ---"
  free -h 2>/dev/null || vm_stat 2>/dev/null || echo "Comando de memória não disponível neste sistema"
}

main() {
  check_containers
  check_backend_health
  check_frontend
  check_api
  check_resources

  echo
  if [ "$FAILURES" -eq 0 ]; then
    printf '\033[1;32mTodas as verificações passaram.\033[0m\n'
  else
    printf '\033[1;31m%d verificação(ões) falharam.\033[0m\n' "$FAILURES"
    exit 1
  fi
}

main "$@"
