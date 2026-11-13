package sn.devbrik.briques

import kotlin.system.exitProcess

/**
 * Tests de B10 sans dépendance :
 * kotlinc *.kt -include-runtime -d b10.jar && java -cp b10.jar sn.devbrik.briques.MasquageTelephoneTestKt
 */
private var total = 0
private var echecs = 0

private fun egal(obtenu: String, attendu: String, cas: String) {
    total++
    if (obtenu != attendu) {
        echecs++
        println("ÉCHEC $cas : attendu « $attendu », obtenu « $obtenu »")
    }
}

fun main() {
    egal(MasquageTelephone.masquer("+221771234567"), "+22177*****67", "international")
    egal(MasquageTelephone.masquer("00221771234567"), "0022177*****67", "préfixe 00")
    egal(MasquageTelephone.masquer("771234567"), "77*****67", "local")
    egal(MasquageTelephone.masquer("77 123 45 67"), "77*****67", "avec espaces")
    egal(MasquageTelephone.masquer("+33612345678"), "+33*******78", "autre indicatif")
    egal(MasquageTelephone.masquer("12345"), "*****", "trop court : tout masqué")
    egal(MasquageTelephone.masquer("771234567", '#'), "77#####67", "symbole personnalisé")

    egal(
        MasquageTelephone.masquerDansTexte("Paiement refusé pour +221 77 123 45 67 (tentative 3)"),
        "Paiement refusé pour +22177*****67 (tentative 3)",
        "dans un message",
    )
    egal(
        MasquageTelephone.masquerDansTexte("Appels : 771234567, 781112233."),
        "Appels : 77*****67, 78*****33.",
        "plusieurs numéros",
    )
    egal(
        MasquageTelephone.masquerDansTexte("Commande 2026 de 15000 FCFA, id=ab771234567"),
        "Commande 2026 de 15000 FCFA, id=ab771234567",
        "nombres courts et identifiants inchangés",
    )
    egal(MasquageTelephone.masquerDansTexte(null), "", "null")

    println("${total - echecs} / $total vérifications réussies")
    if (echecs > 0) exitProcess(1)
}
