package dgb;

import java.util.List;
import java.util.Map;

/**
 * Représente un état complet de l'exécution à un instant t.
 */

public class ExecutionState {

    // Indique la position courante dans le code (ex: instruction ou ligne).
    private final int pc;

    // Variables locales : par exemple (nomVariable -> valeur).
    private final Map<String, Object> localVars;

    // Représentation (simplifiée) de la mémoire dynamique,
    // par exemple (idObjet -> contenu de l'objet).
    private final Map<Integer, Object> heap;

    // Log des appels non-déterministes rencontrés jusqu'à présent
    // (lecture clavier, random, etc.).
    private final List<NonDeterministicCallRecord> callsSoFar;

    public ExecutionState(int pc,
                          Map<String, Object> localVars,
                          Map<Integer, Object> heap,
                          List<NonDeterministicCallRecord> callsSoFar)
    {
        this.pc = pc;
        // Pour éviter les effets de bord, on peut cloner ici
        // (copie profonde) si nécessaire
        this.localVars = localVars;
        this.heap = heap;
        this.callsSoFar = callsSoFar;
    }

    public int getPc() {
        return pc;
    }

    public Map<String, Object> getLocalVars() {
        return localVars;
    }

    public Map<Integer, Object> getHeap() {
        return heap;
    }

    public List<NonDeterministicCallRecord> getCallsSoFar() {
        return callsSoFar;
    }

    @Override
    public String toString() {
        return "ExecutionState{" +
                "pc=" + pc +
                ", localVars=" + localVars +
                ", heap=" + heap +
                ", callsSoFar=" + callsSoFar +
                '}';
    }
}
