package dgb;


/**
 * Représente un appel non-déterministe capturé :
 * exemple "System.in.read()" qui retourne 42.
 */
public class NonDeterministicCallRecord {

    // Par exemple, l'index (stepNumber) où s'est produit l'appel.
    private final int stepNumber;

    // La valeur effectivement lue (42, "Bonjour", etc.).
    private final Object valueRead;

    // Nom de l'appel ou type, si besoin
    private final String callName;

    public NonDeterministicCallRecord(int stepNumber, Object valueRead, String callName) {
        this.stepNumber = stepNumber;
        this.valueRead = valueRead;
        this.callName = callName;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public Object getValueRead() {
        return valueRead;
    }

    public String getCallName() {
        return callName;
    }

    @Override
    public String toString() {
        return "NonDeterministicCallRecord{" +
                "stepNumber=" + stepNumber +
                ", valueRead=" + valueRead +
                ", callName='" + callName + '\'' +
                '}';
    }
}