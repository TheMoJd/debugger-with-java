package dgb.commands;

import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

public class StepBackCommand implements DebuggerCommand {
    private final ScriptableDebugger debugger;

    public StepBackCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) throws InterruptedException {
        return debugger.stepBack(1);
    }
}
