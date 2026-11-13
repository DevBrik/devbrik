package sn.devbrik.briques;

/**
 * Tests de B1 sans dépendance : javac -d out *.java && java -cp out sn.devbrik.briques.ValidationTelephoneTest
 */
public final class ValidationTelephoneTest {

    private static int echecs = 0;
    private static int total = 0;

    private static void verifier(boolean condition, String message) {
        total++;
        if (!condition) {
            echecs++;
            System.out.println("ÉCHEC : " + message);
        }
    }

    private static void leve(Runnable action, String message) {
        try {
            action.run();
            verifier(false, message + " (aucune exception)");
        } catch (IllegalArgumentException attendue) {
            verifier(true, message);
        }
    }

    public static void main(String[] args) {
        for (String n : new String[] {"771234567", "761234567", "701234567", "751234567", "781234567",
                "+221771234567", "00221771234567", " 77 123 45 67 "}) {
            verifier(ValidationTelephone.estValide(n), n + " doit être valide");
        }
        for (String n : new String[] {"", "   ", "721234567", "77123456", "7712345678", "+33612345678",
                "abc", "+221 77 123 456"}) {
            verifier(!ValidationTelephone.estValide(n), "« " + n + " » doit être refusé");
        }
        verifier(!ValidationTelephone.estValide(null), "null doit être refusé");

        verifier(ValidationTelephone.normaliser("771234567").equals("+221771234567"), "normaliser local");
        verifier(ValidationTelephone.normaliser("00221771234567").equals("+221771234567"), "normaliser 00221");
        verifier(ValidationTelephone.normaliser("+221 77 123 45 67").equals("+221771234567"), "normaliser avec espaces");
        verifier(ValidationTelephone.versLocal("+221781234567").equals("781234567"), "versLocal");
        leve(() -> ValidationTelephone.normaliser("123"), "normaliser refuse un numéro invalide");

        System.out.println(total - echecs + " / " + total + " vérifications réussies");
        if (echecs > 0) {
            System.exit(1);
        }
    }
}
