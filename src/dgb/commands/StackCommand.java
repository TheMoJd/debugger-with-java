package dgb.commands;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

import java.util.List;

public class StackCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public StackCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            ThreadReference thread = debugger.getCurrentThread();
            List<StackFrame> frames = thread.frames();
            StringBuilder s = new StringBuilder();
            for (StackFrame frame : frames) {
                s.append(frame.location().toString()).append("\n");
            }
            if (s.length() == 0) {
                return "Aucune pile d'appels.";
            } else {
                return s.toString();
            }
        } catch(Exception e) {
            return "Erreur lors de la récupération de la pile d'appels: " + e.getMessage();
        }
    }
}
