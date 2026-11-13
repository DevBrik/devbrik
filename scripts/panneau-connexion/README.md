# Visuels du panneau de connexion (M04)

Les visuels de droite des pages de connexion et d'inscription sont **pré-rendus** : dégradé, aperçu de la page, arrondi et fusion « Lumière tamisée » sont appliqués dans l'image, avec les réglages Penpot de la maquette (page 4). Le navigateur n'a plus qu'à faire défiler des images finies.

- `sources/*.png` : captures de l'application (1300 × 800, données fictives) pour chaque phrase.
- `rendu.py` : compose chaque visuel (1100 × 800, rendu en ×2 ; ajusté à la hauteur du panneau et coupé à droite) avec Chrome (Playwright).

Régénérer après un changement d'écran :

```bash
pip install playwright        # Chrome ou Chromium doit être installé
python3 scripts/panneau-connexion/rendu.py scripts/panneau-connexion/sources /tmp/visuels
for n in publier briques tester demandes classement; do
  cwebp -q 78 /tmp/visuels/$n.png -o frontend/public/assets/apercus/panneau-$n.webp   # ou ImageMagick : convert … -quality 78
done
```
Dans `rendu.py`, adapter `executable_path` au chemin de Chrome sur votre poste.
