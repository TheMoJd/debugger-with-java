package dgb.commands;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

public class ReceiverCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public ReceiverCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            StackFrame frame = debugger.getCurrentFrame();
            ObjectReference receiver = frame.thisObject();
            if (receiver == null) {
                return "Aucun receiver (méthode statique ou receiver null).";
            }
            return "Receiver: " + receiver.toString();
        } catch(Exception e) {
            return "Erreur lors de la récupération du receiver: " + e.getMessage();
        }
    }
}
