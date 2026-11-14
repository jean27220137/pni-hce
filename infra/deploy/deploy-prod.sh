#!/usr/bin/env bash
# deploy-prod.sh — Despliegue a producción en AWS EC2 (Nodo Sur · Ayacucho)
# Invocado manualmente desde el stage "Despliegue a producción" del Jenkinsfile
# Requisitos: SSH configurado, Docker instalado en el host, variables de entorno definidas
#
# Variables esperadas (definidas como credenciales en Jenkins):
#   EC2_HOST        — IP o hostname del servidor EC2
#   EC2_USER        — Usuario SSH (ej: ec2-user)
#   EC2_KEY_PATH    — Ruta al archivo .pem en el agente Jenkins
#   DB_PASSWORD     — Contraseña de PostgreSQL en producción
#   REGISTRY        — Registro de imágenes (ej: ghcr.io/unsch/pni-hce)
#   IMAGE_TAG       — Tag de imagen a desplegar (ej: 1.2.0 o el commit SHA)

set -euo pipefail

: "${EC2_HOST:?}"
: "${EC2_USER:?}"
: "${EC2_KEY_PATH:?}"
: "${DB_PASSWORD:?}"
: "${REGISTRY:?}"
: "${IMAGE_TAG:?}"

echo "==> Desplegando PNI-HCE ${IMAGE_TAG} en ${EC2_HOST}"

SSH="ssh -i ${EC2_KEY_PATH} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST}"

# 1. Copiar docker-compose de producción
scp -i "${EC2_KEY_PATH}" -o StrictHostKeyChecking=no \
    infra/docker-compose.prod.yml \
    "${EC2_USER}@${EC2_HOST}:/opt/pni-hce/docker-compose.yml"

# 2. Ejecutar en el servidor remoto
$SSH bash << EOF
  set -euo pipefail
  cd /opt/pni-hce

  export REGISTRY="${REGISTRY}"
  export IMAGE_TAG="${IMAGE_TAG}"
  export DB_PASSWORD="${DB_PASSWORD}"

  echo "--- Pull imágenes ---"
  docker compose pull

  echo "--- Despliegue sin downtime ---"
  docker compose up -d --remove-orphans

  echo "--- Estado final ---"
  docker compose ps
EOF

echo "==> Despliegue completado."
