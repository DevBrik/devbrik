package sn.devbrik.briques

/**
 * B10 · Masquage d'un numéro de téléphone dans les journaux.
 *
 * Les journaux ne doivent pas contenir de numéros complets (donnée personnelle).
 * On garde l'indicatif, les deux premiers et les deux derniers chiffres :
 * « +221771234567 » → « +22177*****67 ». Les séparateurs (espaces, points, tirets) sont acceptés.
 */
object MasquageTelephone {

    /** Suite de 9 à 15 chiffres, éventuellement précédée de « + » ou « 00 », séparateurs autorisés. */
    private val NUMERO = Regex("""(?<![\w+])(?:\+|00)?\d(?:[ .\-]?\d){8,14}(?!\w)""")

    /**
     * Masque un numéro isolé. Garde « + » ou « 00 » et l'indicatif 221 s'il est présent,
     * puis deux chiffres au début et deux à la fin. Moins de 6 chiffres : tout est masqué.
     */
    fun masquer(numero: String, symbole: Char = '*'): String {
        val brut = numero.trim()
        val prefixe = when {
            brut.startsWith("+") -> "+"
            brut.startsWith("00") -> "00"
            else -> ""
        }
        var chiffres = brut.drop(prefixe.length).filter { it.isDigit() }
        var indicatif = ""
        if (prefixe.isNotEmpty() && chiffres.startsWith("221")) {
            indicatif = "221"
            chiffres = chiffres.drop(3)
        }
        if (chiffres.length < 6) {
            return prefixe + indicatif + symbole.toString().repeat(chiffres.length)
        }
        val milieu = symbole.toString().repeat(chiffres.length - 4)
        return prefixe + indicatif + chiffres.take(2) + milieu + chiffres.takeLast(2)
    }

    /** Masque tous les numéros présents dans un message de journal, le reste est inchangé. */
    fun masquerDansTexte(texte: String?, symbole: Char = '*'): String {
        if (texte.isNullOrEmpty()) return texte ?: ""
        return NUMERO.replace(texte) { masquer(it.value, symbole) }
    }
}
