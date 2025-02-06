package dgb.commands;

import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

public class StepBackNCommand implements DebuggerCommand {
    private final ScriptableDebugger debugger;

    public StepBackNCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) throws InterruptedException {
        if (args.length < 2) {
            return "Usage: step-back-n <number>";
        }
        int n = Integer.parseInt(args[1]);
        return debugger.stepBack(n);
    }
}
