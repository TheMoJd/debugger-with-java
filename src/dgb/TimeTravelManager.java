package dgb;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.List;
public class TimeTravelManager {
    // On stocke l’historique des pas réalisés
    private List<DebugStep> steps = new ArrayList<>();

    // Enregistre un nouveau pas dans l’historique
    public void recordStep(DebugStep step) {
        steps.add(step);
    }

    public List<DebugStep> getSteps() {
        return steps;
    }

    // Récupère le numéro de pas courant (par ex. la taille de la liste)
    public int getCurrentStepIndex() {
        return steps.size() - 1;
    }

    public void clearSteps() {
        steps.clear();
    }
}
