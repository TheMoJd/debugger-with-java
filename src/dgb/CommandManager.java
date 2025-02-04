package dgb;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class CommandManager {
    private Map<String, DebuggerCommand> commandMap = new HashMap<>();

    /**
     * Enregistre une commande sous un nom donné.
     * @param name le nom de la commande
     * @param command l'instance de DebuggerCommand
     */
    public void registerCommand(String name, DebuggerCommand command) {
        commandMap.put(name.toLowerCase(), command);
    }

    /**
     * Exécute la commande lue sur la ligne de commande.
     * @param input la ligne saisie par l'utilisateur
     * @return le résultat de l'exécution de la commande
     */
    public Object executeCommand(String input) {
        // Découpage de l'entrée pour extraire la commande et ses arguments
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) {
            return "Commande vide.";
        }
        String commandName = tokens[0].toLowerCase();
        DebuggerCommand command = commandMap.get(commandName);
        if (command == null) {
            return "Commande inconnue: " + commandName;
        }
        // Les arguments sont les tokens suivants
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
        return command.execute(args);
    }
}
