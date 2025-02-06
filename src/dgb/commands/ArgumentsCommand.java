package dgb.commands;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

import java.util.List;

public class ArgumentsCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public ArgumentsCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            StackFrame frame = debugger.getCurrentFrame();
            List<LocalVariable> vars = frame.visibleVariables();
            StringBuilder sb = new StringBuilder("Arguments:\n");
            for (LocalVariable var : vars) {
                if (var.isArgument()) {
                    Value value = frame.getValue(var);
                    sb.append(var.name()).append(" -> ").append(value).append("\n");
                }
            }
            return sb.toString();
        } catch(Exception e) {
            return "Erreur lors de la récupération des arguments: " + e.getMessage();
        }
    }
}
