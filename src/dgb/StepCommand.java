package dgb;

public class StepCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public StepCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        // On suppose que la méthode step() configure un stepping sur le thread courant.
        debugger.step();
        return "Step exécuté.";
    }
}
