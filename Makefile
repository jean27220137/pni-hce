# Makefile — PNI-HCE
# Uso: make <target>

.PHONY: dev down build test-load e2e logs clean

## Levantar ambiente de desarrollo completo (infra + servicios)
dev:
	docker compose -f infra/docker-compose.dev.yml up -d --build

## Detener y eliminar contenedores (mantiene volúmenes)
down:
	docker compose -f infra/docker-compose.dev.yml down

## Detener y eliminar contenedores + volúmenes (reset completo)
reset:
	docker compose -f infra/docker-compose.dev.yml down -v

## Build de todos los servicios Java
build:
	for svc in services/mpi services/consent services/audit services/gateway; do \
		echo "=== Building $$svc ==="; \
		(cd $$svc && mvn -B -ntp clean package -DskipTests); \
	done

## Ejecutar tests de todos los servicios (requiere Docker para Testcontainers)
test:
	for svc in services/mpi services/consent services/audit services/gateway; do \
		echo "=== Testing $$svc ==="; \
		(cd $$svc && mvn -B -ntp verify); \
	done

## Tests de carga (requiere servicios corriendo + k6 instalado)
test-load-mpi:
	k6 run tests/load/mpi-load-test.js

test-load-historia:
	k6 run tests/load/historia-load-test.js

test-load-consent:
	k6 run tests/load/consent-load-test.js

## Tests E2E con Playwright (requiere servicios corriendo + ng serve)
e2e:
	cd tests/e2e && npm install && npx playwright test

## Frontend dev server
frontend:
	cd frontend && npx ng serve

## Ver logs de todos los contenedores
logs:
	docker compose -f infra/docker-compose.dev.yml logs -f

## Levantar solo Jenkins + SonarQube
ci:
	docker compose -f infra/ci/docker-compose.ci.yml up -d

ci-down:
	docker compose -f infra/ci/docker-compose.ci.yml down
