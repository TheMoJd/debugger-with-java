package dgb;

public class DebugStep {
    private final int stepIndex;          // position dans l’historique
    private final String className;       // nom de la classe
    private final String methodName;      // nom de la méthode
    private final int lineNumber;         // numéro de la ligne

    public DebugStep(int stepIndex, String className, String methodName, int lineNumber) {
        this.stepIndex = stepIndex;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }

    // Getters...

    public int getStepIndex() {
        return stepIndex;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }


}
