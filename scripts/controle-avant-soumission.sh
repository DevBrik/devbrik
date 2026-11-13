#!/usr/bin/env bash
# Contrôle rapide avant soumission (dossier, section 20). Usage : ./scripts/controle-avant-soumission.sh
cd "$(dirname "$0")/.." || exit 1
ko=0
verif() { if eval "$2" >/dev/null 2>&1; then echo "[OK] $1"; else echo "[KO] $1"; ko=$((ko + 1)); fi; }

verif "deploy/.env non suivi par Git"                 "! git ls-files --error-unmatch deploy/.env"
verif "application-local.properties non suivi"        "! git ls-files --error-unmatch backend/src/main/resources/application-local.properties"
verif "deploy/.env.example présent"                   "test -f deploy/.env.example"
verif "docker-compose.yml présent"                    "test -f deploy/docker-compose.yml"
verif "README.md présent"                             "test -f README.md"
verif "docs/api.md présent"                           "test -f docs/api.md"
verif "docs/IA.md présent"                            "test -f docs/IA.md"
verif "docs/LICENCES.md présent"                      "test -f docs/LICENCES.md"
verif "maquettes exportées (>= 14 PNG)"               "test \$(ls docs/maquettes/*.png | wc -l) -ge 14"
verif "aucun « A CONFIRMER » dans LICENCES"           "! grep -q 'A CONFIRMER' docs/LICENCES.md"
verif "aucun innerHTML / bypassSecurityTrust"         "! grep -rnE 'innerHTML|bypassSecurityTrust' frontend/src"
verif "aucun console.log dans le frontend"            "! grep -rn 'console.log' frontend/src --include=*.ts"
verif "aucun #EA580C en dur dans les composants"      "! grep -rniE '#EA580C' frontend/src/app"
verif "aucun gabarit ou style en ligne (3 fichiers)"  "! grep -rlE '^\s+(template|styles):' frontend/src/app --include=*.component.ts"
verif "aucun secret évident dans le backend"          "! git grep -nIE '(password|secret)\s*[:=]\s*\"[^\"\$]{6,}\"' -- backend/src/main"
verif "10 briques de l'équipe"                        "test \$(ls -d briques-equipe/B*/ | wc -l) -eq 10"

if [ "$ko" -gt 0 ]; then echo; echo "$ko point(s) à corriger."; exit 1; fi
echo; echo "Tout est bon."
