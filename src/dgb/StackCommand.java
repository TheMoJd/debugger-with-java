package dgb;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
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
            StringBuilder sb = new StringBuilder("Call Stack:\n");
            for (int i = 0; i < frames.size(); i++) {
                sb.append("Frame ").append(i)
                        .append(": ").append(frames.get(i).location())
                        .append("\n");
            }
            return sb.toString();
        } catch(Exception e) {
            return "Erreur lors de la récupération de la pile d'appels: " + e.getMessage();
        }
    }
}
