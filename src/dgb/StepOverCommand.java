package dgb;

public class StepOverCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public StepOverCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        debugger.stepOver();
        return "Step-over exécuté.";
    }
}
