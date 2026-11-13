"""Tests de B5 : python3 -m unittest -v

Vecteurs connus : RFC 4231 (HMAC-SHA256), cas de test 1 et 2.
"""

import unittest

from signature_hmac import calculer_signature, verifier_avec_horodatage, verifier_signature

RFC4231_CAS1 = (b"\x0b" * 20, b"Hi There", "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7")
RFC4231_CAS2 = (b"Jefe", b"what do ya want for nothing?", "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843")


class TestSignature(unittest.TestCase):
    def test_vecteurs_rfc4231(self):
        for cle, donnees, attendu in (RFC4231_CAS1, RFC4231_CAS2):
            self.assertEqual(calculer_signature(cle, donnees), attendu)

    def test_signature_valide(self):
        cle, donnees, attendu = RFC4231_CAS2
        self.assertTrue(verifier_signature(cle, donnees, attendu))
        self.assertTrue(verifier_signature(cle, donnees, attendu.upper()))
        self.assertTrue(verifier_signature("Jefe", "what do ya want for nothing?", attendu))

    def test_signature_invalide(self):
        cle, donnees, attendu = RFC4231_CAS2
        self.assertFalse(verifier_signature(cle, donnees + b" ", attendu))
        self.assertFalse(verifier_signature(b"autre", donnees, attendu))
        self.assertFalse(verifier_signature(cle, donnees, attendu[:-1] + "0"))
        self.assertFalse(verifier_signature(cle, donnees, None))
        self.assertFalse(verifier_signature(cle, donnees, ""))
        self.assertFalse(verifier_signature(b"", donnees, attendu))

    def test_prefixe(self):
        cle, donnees, attendu = RFC4231_CAS2
        self.assertTrue(verifier_signature(cle, donnees, "sha256=" + attendu, prefixe="sha256="))
        self.assertFalse(verifier_signature(cle, donnees, attendu, prefixe="sha256="))

    def test_horodatage(self):
        secret, corps, instant = b"secret", b'{"montant":15000}', 1_762_950_000
        signature = calculer_signature(secret, f"{instant}.".encode() + corps)
        self.assertTrue(verifier_avec_horodatage(secret, corps, signature, instant, maintenant=instant + 10))
        self.assertTrue(verifier_avec_horodatage(secret, corps, signature, str(instant), maintenant=instant))
        self.assertFalse(verifier_avec_horodatage(secret, corps, signature, instant, maintenant=instant + 301))
        self.assertFalse(verifier_avec_horodatage(secret, corps, signature, "abc", maintenant=instant))
        self.assertFalse(verifier_avec_horodatage(secret, corps, signature, instant + 1, maintenant=instant))


if __name__ == "__main__":
    unittest.main()
