package dgb;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

public class FrameCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public FrameCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            VirtualMachine vm = debugger.getVm();
            ThreadReference thread = vm.allThreads().getFirst();
            // Récupération de la frame courante depuis le contexte du thread débogué.
            StackFrame frame = debugger.getCurrentFrame();
            return "Frame courante: " + (thread.frame(0));
        } catch (Exception e) {
            return "Erreur lors de la récupération de la frame: " + e.getMessage();
        }
    }
}

