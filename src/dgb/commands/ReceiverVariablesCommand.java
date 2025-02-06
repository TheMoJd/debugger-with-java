package dgb.commands;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import dgb.DebuggerCommand;
import dgb.ScriptableDebugger;

import java.util.List;

public class ReceiverVariablesCommand implements DebuggerCommand {
    private ScriptableDebugger debugger;

    public ReceiverVariablesCommand(ScriptableDebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public Object execute(String[] args) {
        try {
            StackFrame frame = debugger.getCurrentFrame();
            ObjectReference receiver = frame.thisObject();
            if (receiver == null) {
                return "Aucun receiver.";
            }
            ReferenceType refType = receiver.referenceType();
            List<Field> fields = refType.allFields();
            StringBuilder sb = new StringBuilder("Receiver Variables:\n");
            for (Field field : fields) {
                Value value = receiver.getValue(field);
                sb.append(field.name()).append(" -> ").append(value).append("\n");
            }
            return sb.toString();
        } catch(Exception e) {
            return "Erreur lors de la récupération des variables du receiver: " + e.getMessage();
        }
    }
}
