package dgb.commands;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

import java.util.List;

public class TemporariesCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public TemporariesCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            StackFrame frame = debugger.getCurrentFrame();
            List<LocalVariable> vars = frame.visibleVariables();
            StringBuilder sb = new StringBuilder("Temporaries:\n");
            for (LocalVariable var : vars) {
                Value value = frame.getValue(var);
                sb.append(var.name()).append(" -> ").append(value).append("\n");
            }
            return sb.toString();
        } catch(Exception e) {
            return "Erreur lors de la récupération des temporaries: " + e.getMessage();
        }
    }
}

