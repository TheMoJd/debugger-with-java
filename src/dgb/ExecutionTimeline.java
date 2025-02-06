package dgb;

import java.util.ArrayList;
import java.util.List;

/**
 * Timeline qui stocke une séquence d'états de l'exécution.
 */
public class ExecutionTimeline {

    private final List<ExecutionState> states = new ArrayList<>();
    private int currentIndex = -1;


    public void recordState(ExecutionState state) {
        states.add(state);
        currentIndex = states.size() - 1;
    }

    public ExecutionState getCurrentState() {
        if (currentIndex >= 0 && currentIndex < states.size()) {
            return states.get(currentIndex);
        }
        return null;
    }

    public void stepBack() {
        stepBack(1);
    }

    public void stepBack(int n) {
        currentIndex = Math.max(0, currentIndex - n);
    }

    public void stepForward(int n) {
        currentIndex = Math.min(states.size() - 1, currentIndex + n);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int size() {
        return states.size();
    }
}
