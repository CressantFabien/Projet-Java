import java.util.*;
import model.*;
import graph.*;
import solver.*;

public class Main { 

    public static void main(String[] args) {
        System.out.println("--- DÉBUT DU TEST SUJET COMPLEXE (GOULOT D'ÉTRANGLEMENT) ---");

        // --- 1. Données du Scénario Complexe ---
        Recipe recipeClean = new Recipe("CLEAN_X");
        Recipe recipeOxide = new Recipe("FURNACE_OXIDE_800C");
        Recipe recipeNitride = new Recipe("FURNACE_NITRIDE_900N"); // Recette spéciale

        // Machines (Cleaning + 2 Fours)
        // (id, capacity, setup, uploading, interbatch, initialsetup)
        Machine cleaner1 = new Machine(1, 2, 2.0, 1.0, 1.0, 0.0);
        Machine furnace1 = new Machine(2, 4, 5.0, 3.0, 2.0, 0.0); // Spécialisé "Oxide"
        Machine furnace2 = new Machine(3, 4, 5.0, 3.0, 2.0, 0.0); // Flexible (Goulot) "Oxide" + "Nitride"

        // Lot 101: Clean -> Oxide (Release 0, Prio 10)
        List<Operation> lot1Ops = new ArrayList<>();
        Lot lot101 = new Lot(101, 10, 0.0, 25, lot1Ops); // release=0
        Operation op1_1 = new Operation(11, 30.0, 5.0, 60.0, recipeClean, lot101); // 30min, Tmax 60
        Operation op1_2 = new Operation(12, 120.0, 0.0, Double.POSITIVE_INFINITY, recipeOxide, lot101); // 2h
        lot1Ops.add(op1_1);
        lot1Ops.add(op1_2);

        // Lot 102: Clean -> Oxide (Release 0, Prio 5)
        List<Operation> lot2Ops = new ArrayList<>();
        Lot lot102 = new Lot(102, 5, 0.0, 25, lot2Ops); // release=0
        Operation op2_1 = new Operation(21, 30.0, 5.0, 60.0, recipeClean, lot102); // 30min, Tmax 60
        Operation op2_2 = new Operation(22, 120.0, 0.0, Double.POSITIVE_INFINITY, recipeOxide, lot102); // 2h
        lot2Ops.add(op2_1);
        lot2Ops.add(op2_2);

        // Lot 103: Clean -> Nitride (Release 30, Prio 8)
        List<Operation> lot3Ops = new ArrayList<>();
        Lot lot103 = new Lot(103, 8, 30.0, 20, lot3Ops); // release=30 (arrive en retard)
        Operation op3_1 = new Operation(31, 30.0, 5.0, 60.0, recipeClean, lot103); // 30min, Tmax 60
        Operation op3_2 = new Operation(32, 180.0, 0.0, Double.POSITIVE_INFINITY, recipeNitride, lot103); // 3h
        lot3Ops.add(op3_1);
        lot3Ops.add(op3_2);
        
        List<Lot> allLots = Arrays.asList(lot101, lot102, lot103);

        // --- 2. Schedule Initial (Solution "Naïve" qui crée le goulot) ---
        
        // Cleaner1 (Cap 2): [B1(op1_1, op2_1)] -> [B2(op3_1)] (op3_1 doit attendre release=30)
        // Furnace1 (Cap 4): [] <-- Inutilisée !
        // Furnace2 (Cap 4): [B3(op1_2, op2_2)] -> [B4(op3_2)] <-- Goulot d'étranglement !

        // Crée des listes MODIFIABLES pour les opérations dans les batches
        List<Operation> opsB1 = new ArrayList<>(Arrays.asList(op1_1, op2_1)); // Batch Clean 1 (Cap 2/2)
        List<Operation> opsB2 = new ArrayList<>(Arrays.asList(op3_1));         // Batch Clean 2 (Cap 1/2)
        List<Operation> opsB3 = new ArrayList<>(Arrays.asList(op1_2, op2_2)); // Batch Oxide (Cap 2/4)
        List<Operation> opsB4 = new ArrayList<>(Arrays.asList(op3_2));         // Batch Nitride (Cap 1/4)

        // Les start times sont à 0.0 ; le validateur (graphe disjonctif) les calculera.
        Batch batchClean1 = new Batch(1, opsB1, 0.0, cleaner1);
        Batch batchClean2 = new Batch(2, opsB2, 0.0, cleaner1);
        
        // C'est ici le "piège" : on assigne B3 à furnace2 au lieu de furnace1
        Batch batchOxide_Naive = new Batch(3, opsB3, 0.0, furnace2); 
        Batch batchNitride = new Batch(4, opsB4, 0.0, furnace2); // (N'a pas le choix)

        Map<Machine, List<Batch>> scheduleMap = new HashMap<>();
        scheduleMap.put(cleaner1, new ArrayList<>(Arrays.asList(batchClean1, batchClean2)));
        scheduleMap.put(furnace1, new ArrayList<>()); // Vide au départ
        scheduleMap.put(furnace2, new ArrayList<>(Arrays.asList(batchOxide_Naive, batchNitride)));

        // Horizon 24h = 1440 minutes
        Schedule initialSchedule = new Schedule(0.0, 1440.0, scheduleMap, false); 

        // --- 3. Initialiser les Outils ---
        DisjuctiveGrapheValidator validator = new DisjuctiveGrapheValidator(allLots); 
        SimulatedAnnealingSolver solver = new SimulatedAnnealingSolver(
            validator,
            1000.0,  // Température initiale
            0.9998,  // Refroidissement lent
            50000    // Itérations
        );

        // --- 4. Lancer le Solver ---
        System.out.println("Lancement du solver sur un planning 'naïf'...");
        Schedule finalSchedule = solver.solve(initialSchedule);

        // --- 5. Afficher les Résultats Détaillés ---
        System.out.println("-------------------------");
        System.out.println("TEST SUJET TERMINÉ.");
        // Afficher le score initial tel que calculé par le solver (après validation)
        System.out.println("Score Initial (calculé par le solver) : " + String.format("%.2f", initialSchedule.getF()));

        if (finalSchedule != null) {
            System.out.println("Score Final : " + String.format("%.2f", finalSchedule.getF()));
            System.out.println("Planning Final Faisable : " + finalSchedule.isFeasible());

            if (finalSchedule.isFeasible()) {
                System.out.println("\nPlanning Final Détaillé:");
                for (Map.Entry<Machine, List<Batch>> entry : finalSchedule.getMachinetobatchmap().entrySet()) {
                    System.out.println("  Machine ID: " + entry.getKey().getIdmachine());
                    List<Batch> sortedBatches = entry.getValue().stream()
                        .sorted(Comparator.comparingDouble(Batch::getStarttime))
                        .toList();
                        
                    for (Batch b : sortedBatches) { // On trie par start time pour la lisibilité
                        System.out.print("    -> Batch ID: " + b.getIdbatch()
                                         + " (Start: " + String.format("%.1f", b.getStarttime())
                                         + ") Ops: [");
                        StringJoiner sj = new StringJoiner(", ");
                        if (b.getOperationlist() != null) {
                            for(Operation o : b.getOperationlist()){
                                sj.add("Op"+o.getIdoperation()
                                     +"(Lot"+o.getAssociatedlot().getIdlot()+")");
                            }
                        }
                        System.out.println(sj.toString() + "]");
                    }
                }
            }
        } else {
            System.out.println("Le solver n'a pas retourné de solution.");
        }
        System.out.println("-------------------------");
    }
}