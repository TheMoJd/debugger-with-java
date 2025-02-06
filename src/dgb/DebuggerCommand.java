package dgb;

public interface DebuggerCommand {
    /**
     * Exécute la commande avec les arguments donnés.
     * @param args les arguments de la commande
     * @return un objet résultat (ou message) issu de l'exécution de la commande
     */
    Object execute(String[] args) throws InterruptedException;
}
