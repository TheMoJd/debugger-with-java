package dgb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTimeTravelDebugger {
    private final ExecutionTimeline timeline = new ExecutionTimeline();
    private int pc = 0;  // compteur d'instructions fictif
    private Map<String, Object> locals; // ex. variables locales
    private Map<Integer, Object> heap;  // la mémoire
    private List<NonDeterministicCallRecord> calls; // log d'appels

    public MyTimeTravelDebugger() {
        // Initialisation
        locals = new HashMap<>();
        heap = new HashMap<>();
        calls = new ArrayList<>();
        // On enregistre l'état initial
        recordCurrentState();
    }

    /**
     * Fait un step en avant : incrémente pc, simule une exécution,
     * etc. (code minimaliste pour l'exemple).
     */
    public void stepForward() {
        // Simuler l'exécution d'une instruction
        pc++;
        // Mettre à jour locals/heap en fonction de l'instruction...
        // ...

        // Ensuite, on enregistre le nouvel état
        recordCurrentState();
    }


    public void stepBack() {
        timeline.stepBack(); // recule l'index de 1
        reloadState(timeline.getCurrentState());
    }

    public void stepBack(int n) {
        timeline.stepBack(n);
        reloadState(timeline.getCurrentState());
    }

    private void recordCurrentState() {
        // Cloner les structures si nécessaire (deep copy)
        // pour l'exemple, on fait un "new HashMap<>(locals)" etc.
        var snapshotLocals = new HashMap<>(locals);
        var snapshotHeap = new HashMap<>(heap);
        var snapshotCalls = new ArrayList<>(calls);

        ExecutionState st = new ExecutionState(
                pc,
                snapshotLocals,
                snapshotHeap,
                snapshotCalls
        );
        timeline.recordState(st);
    }

    /**
     * Recharge l'état : on rétablit pc, locals, heap...
     */
    private void reloadState(ExecutionState st) {
        if (st == null) return;
        this.pc = st.getPc();

        // On remplace nos structures par celles du snapshot
        this.locals = new HashMap<>(st.getLocalVars());
        this.heap   = new HashMap<>(st.getHeap());
        this.calls  = new ArrayList<>(st.getCallsSoFar());

        System.out.println("Rechargé l'état : " + st);
    }

    // autres méthodes (ex. handleNonDeterministicCall) ...
}
