package dgb;

import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;

public class MethodCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public MethodCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            StackFrame frame = debugger.getCurrentFrame();
            Method method = frame.location().method();
            return "Méthode en cours: " + method.name();
        } catch(Exception e) {
            return "Erreur lors de la récupération de la méthode courante: " + e.getMessage();
        }
    }
}
