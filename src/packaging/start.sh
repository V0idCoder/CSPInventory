#!/usr/bin/env sh
set -eu

BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
APP_DIR="$BASE_DIR/app"
JAR_FILE="$(ls "$APP_DIR"/csp-inventory-*.jar | head -n 1)"

if [ -z "${CSPINVENTORY_HOME:-}" ]; then
  CSPINVENTORY_HOME="$BASE_DIR/CSPInventory"
fi

mkdir -p "$CSPINVENTORY_HOME/models" "$CSPINVENTORY_HOME/backups" "$CSPINVENTORY_HOME/data"

exec java -Dcspinventory.home="$CSPINVENTORY_HOME" -jar "$JAR_FILE"
