"""B5 · Vérification d'une signature de rappel (webhook) HMAC-SHA256, générique.

Le fournisseur calcule HMAC-SHA256(secret, corps brut) et l'envoie en hexadécimal dans un en-tête.
Le serveur recalcule la signature sur le corps **brut** (octets reçus, avant tout décodage JSON)
et la compare en temps constant. Option : horodatage signé pour refuser les rejeux.

Aucun fournisseur n'est visé : adaptez le nom de l'en-tête, le préfixe éventuel (« sha256= »)
et la construction du message signé à la documentation officielle de votre fournisseur.
"""

from __future__ import annotations

import hashlib
import hmac
import time
from typing import Optional, Union

Octets = Union[bytes, bytearray, str]


def _octets(valeur: Octets) -> bytes:
    return valeur.encode("utf-8") if isinstance(valeur, str) else bytes(valeur)


def calculer_signature(secret: Octets, message: Octets) -> str:
    """Renvoie HMAC-SHA256(secret, message) en hexadécimal minuscule."""
    return hmac.new(_octets(secret), _octets(message), hashlib.sha256).hexdigest()


def verifier_signature(
    secret: Octets,
    corps: Octets,
    signature_recue: Optional[str],
    prefixe: str = "",
) -> bool:
    """True si la signature reçue correspond au corps. Comparaison en temps constant."""
    if not signature_recue or not secret:
        return False
    recue = signature_recue.strip()
    if prefixe:
        if not recue.startswith(prefixe):
            return False
        recue = recue[len(prefixe):]
    attendue = calculer_signature(secret, corps)
    return hmac.compare_digest(attendue, recue.lower())


def verifier_avec_horodatage(
    secret: Octets,
    corps: Octets,
    signature_recue: Optional[str],
    horodatage: Union[int, str, None],
    tolerance_secondes: int = 300,
    maintenant: Optional[float] = None,
) -> bool:
    """Variante anti-rejeu : la signature porte sur « horodatage.corps » et l'horodatage
    (secondes Unix) doit être à moins de tolerance_secondes de l'heure actuelle."""
    try:
        instant = int(horodatage)  # type: ignore[arg-type]
    except (TypeError, ValueError):
        return False
    actuel = time.time() if maintenant is None else maintenant
    if abs(actuel - instant) > tolerance_secondes:
        return False
    message = _octets(str(instant)) + b"." + _octets(corps)
    return verifier_signature(secret, message, signature_recue)
