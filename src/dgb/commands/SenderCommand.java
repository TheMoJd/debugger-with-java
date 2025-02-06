package dgb.commands;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

import java.util.List;

public class SenderCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public SenderCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            ThreadReference thread = debugger.getCurrentThread();
            List<StackFrame> frames = thread.frames();
            if (frames.size() < 2) {
                return "Aucune méthode appelante (sender) disponible.";
            }
            // La frame 0 est la méthode courante, la frame 1 est celle appelante.
            StackFrame senderFrame = frames.get(1);
            ObjectReference sender = senderFrame.thisObject();
            if (sender == null) {
                return "Le sender est null (peut-être méthode statique).";
            }
            return "Sender: " + sender.toString();
        } catch(Exception e) {
            return "Erreur lors de la récupération du sender: " + e.getMessage();
        }
    }
}
