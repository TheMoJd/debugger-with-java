package dgb.commands;

import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

public class ContinueCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public ContinueCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        debugger.continueExecution();
        return "Exécution poursuivie jusqu'au prochain breakpoint.";
    }
}
