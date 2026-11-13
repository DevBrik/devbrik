# Controle rapide avant soumission (dossier section 20)
$ko = 0
function Verif($nom, $ok) { if ($ok) { Write-Host "[OK] $nom" -ForegroundColor Green } else { Write-Host "[KO] $nom" -ForegroundColor Red; $script:ko++ } }
Verif ".env non suivi par git" (-not (git ls-files deploy/.env))
Verif ".env.example present" (Test-Path deploy/.env.example)
Verif "docker-compose.yml present" (Test-Path deploy/docker-compose.yml)
Verif "README.md present" (Test-Path README.md)
Verif "docs/api.md present" (Test-Path docs/api.md)
Verif "docs/IA.md present" (Test-Path docs/IA.md)
Verif "docs/LICENCES.md present" (Test-Path docs/LICENCES.md)
Verif "maquettes exportees (>= 14 PNG)" ((Get-ChildItem docs/maquettes -Filter *.png -ErrorAction SilentlyContinue | Measure-Object).Count -ge 14)
Verif "aucun A CONFIRMER restant dans LICENCES" (-not (Select-String -Path docs/LICENCES.md -Pattern 'A CONFIRMER' -Quiet))
Verif "aucune section IA a completer" (-not (Select-String -Path docs/IA.md -Pattern 'a completer' -Quiet))
Verif "aucun innerHTML / bypassSecurityTrust" (-not (Select-String -Path frontend/src -Recurse -Pattern 'innerHTML|bypassSecurityTrust' -Quiet -ErrorAction SilentlyContinue))
Verif "10 briques de l'equipe" ((Get-ChildItem briques-equipe -Directory -Filter B*).Count -eq 10)
Verif "aucun secret evident dans le code" (-not (git grep -nIE '(password|secret)\s*[:=]\s*"[^"$]{6,}"' -- backend/src/main 2>$null))
if ($ko -gt 0) { Write-Host "`n$ko point(s) a corriger." -ForegroundColor Yellow; exit 1 } else { Write-Host "`nTout est bon." -ForegroundColor Green }