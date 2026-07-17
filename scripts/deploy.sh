#!/usr/bin/env bash
# Deploy do SRE Operations Dashboard (build + subida + smoke tests).
set -Eeuo pipefail

BASE_URL="${BASE_URL:-http://localhost}"
MAX_WAIT_SECONDS="${MAX_WAIT_SECONDS:-180}"

log() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
error_exit() { printf '\033[1;31mERRO: %s\033[0m\n' "$*" >&2; show_logs; exit 1; }

show_logs() {
  printf '\n\033[1;33m--- Últimas linhas de log do backend ---\033[0m\n'
  docker compose logs --tail 50 backend || true
  printf '\n\033[1;33m--- Estado dos containers ---\033[0m\n'
  docker compose ps || true
}

trap 'error_exit "Falha na linha $LINENO"' ERR

require_env_file() {
  log "Validando arquivo .env"
  if [ ! -f .env ]; then
    error_exit "Arquivo .env não encontrado. Execute: cp .env.example .env e ajuste os valores."
  fi
  echo "Arquivo .env encontrado."
}

pull_latest() {
  if [ -d .git ]; then
    log "Atualizando código (git pull)"
    git pull --ff-only || echo "Aviso: git pull falhou; seguindo com o código local."
  else
    log "Diretório sem git; pulando git pull"
  fi
}

build_images() {
  log "Build das imagens Docker"
  docker compose build
}

start_containers() {
  log "Subindo containers"
  docker compose up -d
}

wait_for_backend() {
  log "Aguardando backend ficar saudável (máx. ${MAX_WAIT_SECONDS}s)"
  local elapsed=0
  until curl -fsS "$BASE_URL/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; do
    if [ "$elapsed" -ge "$MAX_WAIT_SECONDS" ]; then
      error_exit "Backend não ficou saudável em ${MAX_WAIT_SECONDS}s."
    fi
    sleep 5
    elapsed=$((elapsed + 5))
    echo "  aguardando... (${elapsed}s)"
  done
  echo "Backend saudável."
}

smoke_tests() {
  log "Executando testes HTTP básicos"
  curl -fsS "$BASE_URL/api/services" > /dev/null || error_exit "GET /api/services falhou."
  echo "GET /api/services OK"
  curl -fsS "$BASE_URL/api/dashboard/summary" > /dev/null || error_exit "GET /api/dashboard/summary falhou."
  echo "GET /api/dashboard/summary OK"
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/")
  [ "$code" = "200" ] || error_exit "Frontend respondeu HTTP $code."
  echo "Frontend OK (HTTP 200)"
}

show_status() {
  log "Estado final dos containers"
  docker compose ps
}

main() {
  require_env_file
  pull_latest
  build_images
  start_containers
  wait_for_backend
  smoke_tests
  show_status
  printf '\n\033[1;32mDeploy concluído com sucesso.\033[0m\n'
}

main "$@"
