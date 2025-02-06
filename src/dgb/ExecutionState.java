package dgb;

import java.util.Map;

public class ExecutionState {
    public int pc;
    public Map<String, Object> variables;  // variables locales
    public Map<Integer, Object> heap;      // représentation simplifiée de la mémoire (id -> objet)
    public List<NonDeterministicCallRecord> callsSoFar;


}