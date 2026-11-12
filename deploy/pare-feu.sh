#!/usr/bin/env bash
# Pare-feu : SSH, HTTP, HTTPS uniquement. A lancer en root.
set -euo pipefail
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable
ufw status verbose