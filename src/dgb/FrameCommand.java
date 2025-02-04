package dgb;

import com.sun.jdi.StackFrame;

public class FrameCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public FrameCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            // Récupération de la frame courante depuis le contexte du thread débogué.
            StackFrame frame = debugger.getCurrentFrame();
            return "Frame courante: " + frame.toString();
        } catch (Exception e) {
            return "Erreur lors de la récupération de la frame: " + e.getMessage();
        }
    }
}

