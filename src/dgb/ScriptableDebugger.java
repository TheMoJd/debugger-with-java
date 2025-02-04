package dgb;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.StepRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class ScriptableDebugger {

    private Class debugClass;
    private VirtualMachine vm;

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

    public void startDebugger() throws VMDisconnectedException, InterruptedException {
        EventSet eventSet = null;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                System.out.println(event.toString());

                // Si l'événement est un ClassPrepareEvent, place le breakpoint.
                if (event instanceof ClassPrepareEvent) {
                    // Ici, on suppose que le breakpoint doit être sur la ligne 6 (celle de la méthode main par exemple)
                    setBreakPoint(debugClass.getName(), 6);
                }

                // Lorsque le breakpoint est atteint, on configure le stepping.
                if (event instanceof BreakpointEvent) {
                    // Arrêt sur breakpoint : attend la commande utilisateur
                    String command = waitForCommand();
                    if ("step".equalsIgnoreCase(command)) {
                        enableStepRequest((LocatableEvent) event);
                    }
                    // Sinon, le programme continuera (pas de stepping)
                }

                if (event instanceof StepEvent) {
                    // À chaque step, reprendre le contrôle et attendre la commande
                    String command = waitForCommand();
                    if ("step".equalsIgnoreCase(command)) {
                        enableStepRequest((LocatableEvent) event);
                    }
                }

                // Interception de l'événement de déconnexion de la VM
                if (event instanceof VMDisconnectEvent) {
                    System.out.println("===End of program.");
                    InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                    OutputStreamWriter writer = new OutputStreamWriter(System.out);
                    try {
                        reader.transferTo(writer);
                        writer.flush();
                    } catch (IOException e) {
                        System.out.println("Target VM input stream reading error.");
                    }
                    return;  // Sortie de la boucle pour éviter de traiter d'autres événements
                }

                vm.resume();
            }
        }
    }

    public void enableStepRequest(LocatableEvent event) {
        // Récupérer le thread sur lequel le breakpoint a été atteint.
        ThreadReference thread = event.thread();

        // Créer une demande de stepping pour ce thread.
        // - STEP_MIN : la granularité minimale (s’arrête à la prochaine instruction disponible sur la même ligne).
        // - STEP_OVER : le type de stepping qui passe par-dessus les appels de méthode (vous pouvez utiliser STEP_INTO pour entrer dans les méthodes).
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(
                thread,
                StepRequest.STEP_MIN,
                StepRequest.STEP_OVER
        );

        // Active la demande de stepping
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


}
