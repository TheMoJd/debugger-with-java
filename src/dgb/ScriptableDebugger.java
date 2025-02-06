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

    private Class debugClass;
    private VirtualMachine vm;
    private ThreadReference currentThread;
    private TimeTravelManager timeManager = new TimeTravelManager();
    private int stepCounter = 0;

    public ThreadReference getCurrentThread() {
        return currentThread;
    }

    private String waitForCommand() {
        System.out.println("Entrez une commande (tapez 'step' pour effectuer un pas, autre chose pour continuer) : ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());
        VirtualMachine vm = launchingConnector.launch(arguments);
        return vm;
    }

    public void attachTo(Class debuggeeClass) {

        this.debugClass = debuggeeClass;
        try {
            vm = connectAndLaunchVM();
            enableClassPrepareRequest(vm);
            startDebugger();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } catch (VMStartException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected: " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName());
        classPrepareRequest.enable();
    }

    private void startDebugger() throws VMDisconnectedException, InterruptedException {
        EventSet eventSet = null;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                System.out.println(event.toString());

                switch (event) {
                    case ClassPrepareEvent classPrepareEvent -> {
                        setBreakPoint(debugClass.getName(), 6);
                        setBreakPoint(debugClass.getName(), 10); // Additional breakpoint
                        setBreakPoint(debugClass.getName(), 14); // Additional breakpoint
                    }
                    case BreakpointEvent breakpointEvent -> {
                        currentThread = breakpointEvent.thread();
                        String command = waitForCommand();
                        if ("step".equalsIgnoreCase(command)) {
                            enableStepRequest(breakpointEvent);
                            vm.resume();
                        }
                    }
                    case StepEvent stepEvent -> {
                        currentThread = stepEvent.thread();
                        String command = waitForCommand();
                        if ("step".equalsIgnoreCase(command)) {
                            enableStepRequest(stepEvent);
                        }
                    }
                    case VMDisconnectEvent vmDisconnectEvent -> {
                        System.out.println("===End of program.");
                        InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                        OutputStreamWriter writer = new OutputStreamWriter(System.out);
                        try {
                            reader.transferTo(writer);
                            writer.flush();
                        } catch (IOException e) {
                            System.out.println("Target VM input stream reading error.");
                        }
                        return;
                    }
                    default -> {
                    }
                }

                vm.resume();
            }
        }
    }

    public void enableStepRequest(LocatableEvent event) {
        ThreadReference thread = event.thread();
        EventRequestManager erm = vm.eventRequestManager();

        // 1) Supprimer l'ancienne requête s'il y en avait une
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(thread)) {
                erm.deleteEventRequest(sr);
            }
        }

        // 2) Créer une nouvelle StepRequest
        StepRequest stepRequest = erm.createStepRequest(
                thread,
                StepRequest.STEP_MIN,
                StepRequest.STEP_OVER
        );

        // 3) Ajouter un countFilter(1) pour que la requête s'annule d'elle-même
        stepRequest.addCountFilter(1);

        // 4) L'activer
        stepRequest.enable();

        System.out.println("Stepping enabled for thread: " + thread.name());
    }


    public void setBreakPoint(String className, int lineNumber) {
        // Parcourir toutes les classes chargées dans la VM
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(className)) {
                try {
                    // Récupérer la liste des locations correspondant à la ligne souhaitée
                    // La méthode locationsOfLine renvoie une List<Location>
                    java.util.List<Location> locations = targetClass.locationsOfLine(lineNumber);
                    if (locations != null && !locations.isEmpty()) {
                        // Prendre la première location (celle où l'instruction débute)
                        Location location = locations.get(0);
                        // Créer une requête de breakpoint sur cette location
                        BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                        bpReq.enable();
                        System.out.println("Breakpoint set in " + className + " at line " + lineNumber);
                    } else {
                        System.out.println("Aucune location trouvée à la ligne " + lineNumber + " dans " + className);
                    }
                } catch (AbsentInformationException aie) {
                    System.out.println("Information de débogage absente pour " + className + ". Assurez-vous de compiler avec les informations de debug (-g).");
                    aie.printStackTrace();
                }
            }
        }
    }

    public void step() {
        if (currentThread == null) {
            System.out.println("Aucun thread suspendu pour exécuter un step.");
            return;
        }

        EventRequestManager erm = vm.eventRequestManager();

        // Supprimez toute StepRequest existante pour ce thread
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(currentThread)) {
                erm.deleteEventRequest(sr);
            }
        }

        // Créez une nouvelle StepRequest pour currentThread
        // Utilisez STEP_MIN pour avancer instruction par instruction
        // Utilisez STEP_INTO pour entrer dans les appels de méthode
        StepRequest stepRequest = erm.createStepRequest(
                currentThread,
                StepRequest.STEP_MIN,
                StepRequest.STEP_INTO
        );

        // Le filtre count à 1 signifie qu'après un pas, la requête s'annule
        stepRequest.addCountFilter(1);
        stepRequest.enable();

        System.out.println("Step (STEP_INTO) activé sur le thread : " + currentThread.name());
    }

    public void stepOver() {
        if (currentThread == null) {
            System.out.println("Aucun thread suspendu pour exécuter un step-over.");
            return;
        }

        EventRequestManager erm = vm.eventRequestManager();

        // Supprimer toute demande de step existante pour éviter les conflits
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(currentThread)) {
                erm.deleteEventRequest(sr);
            }
        }

        // Créer une nouvelle StepRequest
        // Utilisation de STEP_LINE pour sauter directement à la prochaine ligne
        // et de STEP_OVER pour ne pas entrer dans les méthodes appelées
        StepRequest stepRequest = erm.createStepRequest(
                currentThread,
                StepRequest.STEP_LINE,   // granularité : passer directement à la prochaine ligne
                StepRequest.STEP_OVER    // ne pas entrer dans les appels de méthode
        );

        // Filtre count à 1 : on s'arrête dès le prochain événement de step
        stepRequest.addCountFilter(1);
        stepRequest.enable();

        System.out.println("Step-over activé sur le thread : " + currentThread.name());
    }


    public void continueExecution() {
        // Optionnel : supprimer les StepRequest en cours pour éviter d'interrompre l'exécution
        EventRequestManager erm = vm.eventRequestManager();
        for (StepRequest sr : erm.stepRequests()) {
            if (sr.thread().equals(currentThread)) {
                erm.deleteEventRequest(sr);
            }
        }

        vm.resume();
        System.out.println("Exécution poursuivie jusqu'au prochain breakpoint.");
    }


    public StackFrame getCurrentFrame() throws Exception {
        if (currentThread == null) {
            throw new Exception("Aucun thread suspendu pour récupérer la frame courante.");
        }

        // currentThread.frame(0) renvoie la frame en haut de la pile
        try {
            return currentThread.frame(0);
        } catch (IncompatibleThreadStateException itse) {
            throw new Exception("Erreur lors de la récupération de la frame : " + itse.getMessage(), itse);
        }
    }


    // Méthode pour lancer la boucle de commandes
    public void launchCommandLoop() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        CommandManager cmdManager = new CommandManager();

        // Enregistrement des commandes de base
        cmdManager.registerCommand("step", new StepCommand(this));
        cmdManager.registerCommand("step-over", new StepOverCommand(this));
        cmdManager.registerCommand("continue", new ContinueCommand(this));
        cmdManager.registerCommand("frame", new FrameCommand(this));
        cmdManager.registerCommand("stack", new StackCommand(this));
        cmdManager.registerCommand("arguments", new ArgumentsCommand(this));
        cmdManager.registerCommand("method", new MethodCommand(this));
        cmdManager.registerCommand("receiver", new ReceiverCommand(this));
        cmdManager.registerCommand("temporaries", new TemporariesCommand(this));
        cmdManager.registerCommand("receiver-variables", new ReceiverVariablesCommand(this));
        cmdManager.registerCommand("sender", new SenderCommand(this));
        cmdManager.registerCommand("sender", new SenderCommand(this));

        System.out.println("Interface de commande du debugger lancée.");
        System.out.println("Entrez une commande (ex: 'step', 'continue', 'frame', ...):");

        try {
            String input;
            while ((input = reader.readLine()) != null) {
                Object result = cmdManager.executeCommand(input, this);
                System.out.println(result);
                //rester en attente d'une nouvelle commande pour chaque step.
                if (currentThread != null) {
                    startDebugger();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public VirtualMachine getVm() {
        return vm;
    }
}
