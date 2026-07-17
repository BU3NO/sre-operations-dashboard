.PHONY: dev build up down logs test health deploy

# Sobe apenas o banco e roda backend/frontend localmente (requer Java 21 e Node)
dev:
	docker compose up -d postgres
	@echo "PostgreSQL no ar. Rode o backend com: cd backend && mvn spring-boot:run"
	@echo "E o frontend com: cd frontend && npm start"

build:
	docker compose build

up:
	docker compose up -d --build

down:
	docker compose down

logs:
	docker compose logs -f backend

test:
	docker run --rm -v "$(CURDIR)/backend":/app -v sre-maven-repo:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn -B test

health:
	bash scripts/check.sh

deploy:
	bash scripts/deploy.sh
