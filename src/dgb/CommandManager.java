package dgb;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class CommandManager {
    private final Map<String, DebuggerCommand> commandMap = new HashMap<>();

    public void registerCommand(String name, DebuggerCommand command) {
        commandMap.put(name.toLowerCase(), command);
    }

    public Object executeCommand(String input, ScriptableDebugger scriptableDebugger) {
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) {
            return "Commande vide.";
        }
        String cmdName = tokens[0].toLowerCase();
        DebuggerCommand cmd = commandMap.get(cmdName);
        if (cmd == null) {
            return "Commande inconnue: " + cmdName;
        }
        // arguments éventuels
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        return cmd.execute(args);
    }
}
