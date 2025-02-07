package dgb;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import dgb.commands.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class ScriptableDebugger {

    private Class<?> debugClass;
    private VirtualMachine vm;
    private ThreadReference currentThread;

    // Notre gestionnaire de trace
    private final TimeTravelManager timeManager = new TimeTravelManager();

    // Le connecteur lance la VM cible
    public VirtualMachine connectAndLaunchVM()
            throws IOException, IllegalConnectorArgumentsException, VMStartException {

        LaunchingConnector launchingConnector =
                Bootstrap.virtualMachineManager().defaultConnector();

        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());

        return launchingConnector.launch(arguments);
    }

    // Méthode d’attachement à la classe debuggee
    public void attachTo(Class<?> debuggeeClass) {
        this.debugClass = debuggeeClass;
        try {
            vm = connectAndLaunchVM();
            enableClassPrepareRequest(vm);

            startDebugger();

        } catch (IOException
                 | IllegalConnectorArgumentsException
                 | VMStartException e) {
            e.printStackTrace();
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected: " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Active la requête “ClassPrepare” pour savoir quand la classe debuggee sera chargée
    private void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest =
                vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName());
        classPrepareRequest.enable();
    }

    boolean doStepBack = false;  // Reset du flag step-back


    private void startDebugger() throws VMDisconnectedException, InterruptedException {

        while (!doStepBack) {  // Tant que l'on ne veut pas faire un step-back
            try {
                EventSet eventSet = vm.eventQueue().remove(); // Attend un événement
                for (Event event : eventSet) {
                    System.out.println(event.toString());

                    if (event instanceof ClassPrepareEvent cpe) {
                        // Ajout des breakpoints
                        setBreakPoint(debugClass.getName(), 6);
                        setBreakPoint(debugClass.getName(), 10);
                        setBreakPoint(debugClass.getName(), 14);
                    }
                    else if (event instanceof BreakpointEvent bpe) {
                        currentThread = bpe.thread();
                        recordStepInfo(bpe);
                        String command = waitForCommand();
                        handleCommand(command, bpe);

                        // Si step-back a été demandé, on stoppe immédiatement
                        if (doStepBack) {
                            break;
                        }
                    }
                    else if (event instanceof StepEvent se) {
                        currentThread = se.thread();
                        recordStepInfo(se);
                        String command = waitForCommand();
                        handleCommand(command, se);

                        // Si step-back a été demandé, on stoppe immédiatement
                        if (doStepBack) {
                            break;
                        }
                    }
                    else if (event instanceof VMDisconnectEvent) {
                        System.out.println("===End of program.");
                        try (InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                             OutputStreamWriter writer = new OutputStreamWriter(System.out)) {
                            reader.transferTo(writer);
                            writer.flush();
                        } catch (IOException e) {
                            System.out.println("Target VM input stream reading error.");
                        }
                        return; // Fin de la boucle => fin du débogueur
                    }
                }

                if (doStepBack) {
                    break;
                }

                vm.resume();

            } catch (VMDisconnectedException e) {
                System.out.println("La VM a été déconnectée. Arrêt du débogueur.");
                return;  // On sort immédiatement
            }
        }

        if (doStepBack) {
            System.out.println("Rejoue l'exécution en arrière...");
            Object result = stepBack(1); // Exécute step-back (vous pouvez modifier pour stepBack(n))
            System.out.println(result);

            startDebugger();
        }
    }


    // Lit une commande sur stdin
    private String waitForCommand() {
        System.out.println("Entrez une commande (ex: 'step', 'continue', 'step-back'): ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Gère la commande saisie (step, continue, step-back...).
     */
    public void handleCommand(String command, LocatableEvent event) throws InterruptedException {
        if (command == null) {
            return;
        }
        String cmd = command.toLowerCase().trim();
        switch (cmd) {
            case "step" -> {
                enableStepRequest(event);
                vm.resume(); // on reprend, un StepEvent surviendra
                return;
            }
            case "continue" -> {
                vm.resume(); // on reprend l’exécution sans step
            }
            case "step-back" -> {
                Object result = stepBack(1);
                System.out.println(result);
            }
            case "frame" -> {
                frame();
            }
            case "receiver" -> {
                receiver();
            }

            case "receiver-variables" -> {
                variables();
            }

            case "sender" -> {
                sender();
            }

            case "method" -> {
                method();
            }

            case "step-over" -> {
                stepOver();
                vm.resume(); // on reprend, un StepEvent surviendra
                return;
            }

            case "exit" -> {
                vm.exit(0);
            }
            default -> {
                // S’il y a plus de commandes, vous pouvez les parser ou faire appel à un CommandManager
                System.out.println("Commande inconnue : " + command);
                //vm.resume();
            }
        }
    }
    public void frame() {
        StackFrame frame = getCurrentFrame();
        System.out.println("Frame: " + frame.location());
    }

    /**
     * Méthode “naïve” pour revenir N pas en arrière :
     *  1) dispose la VM
     *  2) relance
     *  3) rejoue jusqu’au stepIndex ciblé
     */
    public Object stepBack(int n) throws InterruptedException {
        int currentStepIndex = timeManager.getCurrentStepIndex();
        int targetStepIndex = currentStepIndex - n;

        if (targetStepIndex < 0) {
            return "Impossible de remonter plus loin (début).";
        }

        // 1) Dispose la VM
        if (vm != null) {
            vm.dispose();
        }

        // 2) Relance la VM
        try {
            vm = connectAndLaunchVM();
            enableClassPrepareRequest(vm);
        } catch (Exception e) {
            return "Erreur lors du relancement de la VM : " + e.getMessage();
        }

        // 3) Rejoue jusqu’au stepIndex désiré
        replayUntilStep(targetStepIndex);

        // 4) État actuel :
        DebugStep currentStep = timeManager.getSteps().get(targetStepIndex);
        return "Revenu en arrière au step " + targetStepIndex + " : " +
                currentStep.getClassName() + ":" + currentStep.getLineNumber();
    }
    // src/dgb/ScriptableDebugger.java

    public void receiver() {
        try {
            StackFrame frame = getCurrentFrame();
            if (frame != null) {
                ObjectReference thisObject = frame.thisObject();
                System.out.println("Receiver: " + thisObject);
            } else {
                System.out.println("No current frame available.");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving receiver: " + e.getMessage());
        }
    }

    public void variables() {
        try {
            StackFrame frame = getCurrentFrame();
            if (frame != null) {
                Map<LocalVariable, Value> visibleVariables = frame.getValues(frame.visibleVariables());
                System.out.println("Variables: " + visibleVariables);
            } else {
                System.out.println("No current frame available.");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving variables: " + e.getMessage());
        }
    }

    public void sender() {
        try {
            StackFrame frame = getCurrentFrame();
            if (frame != null) {
                Location location = frame.location();
                System.out.println("Sender: " + location.declaringType().name() + "." + location.method().name());
            } else {
                System.out.println("No current frame available.");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving sender: " + e.getMessage());
        }
    }

    public void method() {
        try {
            StackFrame frame = getCurrentFrame();
            if (frame != null) {
                Method method = frame.location().method();
                System.out.println("Method: " + method.name());
            } else {
                System.out.println("No current frame available.");
            }
        } catch (Exception e) {
            System.out.println("Error retrieving method: " + e.getMessage());
        }
    }



    private void replayUntilStep(int targetStepIndex) throws InterruptedException {
        // On vide l’historique, puisqu’on repart de zéro
        timeManager.clearSteps();

        // On avance tant que le stepIndex courant < targetStepIndex
        while (timeManager.getCurrentStepIndex() < targetStepIndex) {
            EventSet eventSet = vm.eventQueue().remove();
            for (Event event : eventSet) {
                if (event instanceof StepEvent se) {
                    recordStepInfo(se);
                } else if (event instanceof BreakpointEvent bpe) {
                    recordStepInfo(bpe);
                }
                // On n’attend pas de commande utilisateur en mode replay
            }
            vm.resume();
        }
    }

    /**
     * Enregistre l’emplacement courant (class, méthode, ligne) dans TimeTravelManager.
     */
    private void recordStepInfo(LocatableEvent event) {
        int nextStepIndex = timeManager.getCurrentStepIndex() + 1;
        Location loc = event.location();

        DebugStep step = new DebugStep(
                nextStepIndex,
                loc.declaringType().name(),
                loc.method().name(),
                loc.lineNumber()
        );
        timeManager.recordStep(step);
    }

    public ThreadReference getCurrentThread() {
        return currentThread;
    }

    /**
     * Active une StepRequest (STEP_MIN, STEP_OVER) pour avancer d’une instruction.
     */
    public void enableStepRequest(LocatableEvent event) {
        ThreadReference thread = event.thread();
        EventRequestManager erm = vm.eventRequestManager();

        // supprimer d’anciennes StepRequests
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(thread)) {
                erm.deleteEventRequest(sr);
            }
        }

        // créer la StepRequest
        StepRequest stepRequest = erm.createStepRequest(
                thread,
                StepRequest.STEP_MIN,
                StepRequest.STEP_OVER
        );
        stepRequest.addCountFilter(1);
        stepRequest.enable();

        System.out.println("Stepping enabled for thread: " + thread.name());
    }

    /**
     * Pose un breakpoint dans “className” à la ligne “lineNumber”.
     */
    public void setBreakPoint(String className, int lineNumber) {
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(className)) {
                try {
                    var locations = targetClass.locationsOfLine(lineNumber);
                    if (locations != null && !locations.isEmpty()) {
                        Location loc = locations.get(0);
                        BreakpointRequest bp = vm.eventRequestManager().createBreakpointRequest(loc);
                        bp.enable();
                        System.out.println("Breakpoint set in " + className + " at line " + lineNumber);
                    } else {
                        System.out.println("Aucune location trouvée à la ligne " + lineNumber);
                    }
                } catch (AbsentInformationException aie) {
                    System.out.println("Infos de debug absentes pour " + className + ". Compilez avec -g.");
                }
            }
        }
    }

    /**
     * Méthodes step(), stepOver(), continueExecution() si besoin d’autres commandes
     */
    public void step() {
        if (currentThread == null) {
            System.out.println("Aucun thread pour step()");
            return;
        }
        EventRequestManager erm = vm.eventRequestManager();
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(currentThread)) {
                erm.deleteEventRequest(sr);
            }
        }
        StepRequest stepRequest = erm.createStepRequest(
                currentThread,
                StepRequest.STEP_MIN,
                StepRequest.STEP_INTO
        );
        stepRequest.addCountFilter(1);
        stepRequest.enable();
        System.out.println("Step (STEP_INTO) activé pour " + currentThread.name());
    }

    public void stepOver() {
        if (currentThread == null) {
            System.out.println("Aucun thread pour stepOver()");
            return;
        }
        EventRequestManager erm = vm.eventRequestManager();
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(currentThread)) {
                erm.deleteEventRequest(sr);
            }
        }
        StepRequest stepRequest = erm.createStepRequest(
                currentThread,
                StepRequest.STEP_LINE,
                StepRequest.STEP_OVER
        );
        stepRequest.addCountFilter(1);
        stepRequest.enable();
        System.out.println("Step-over activé pour " + currentThread.name());
    }

    public void continueExecution() {
        vm.resume();
        System.out.println("Exécution poursuivie jusqu’au prochain breakpoint.");
    }

    public StackFrame getCurrentFrame() {
        try {
            return currentThread.frame(0);
        } catch (IncompatibleThreadStateException | IndexOutOfBoundsException e) {
            System.out.println("Erreur lors de la récupération du frame courant : " + e.getMessage());
            return null;
        }
    }

    public VirtualMachine getVm() {
        return vm;
    }
}
