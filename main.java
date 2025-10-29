import java.util.*;
import model.*;
import graph.*;
import solver.*;

public class main {

    public static void main(String[] args) {
        System.out.println("--- DÉBUT DU TEST SIMPLE ---");

        // --- 1. Données Minimalistes ---
        Recipe recipeA = new Recipe("R_A");
        Machine machine1 = new Machine(1, 2, 1.0, 1.0, 1.0, 0.0);
        Lot lot1 = new Lot(101, 1, 0.0, 1, new ArrayList<>());
        Operation op1 = new Operation(1, 5.0, 0.0, Double.POSITIVE_INFINITY, recipeA, lot1);
        lot1.setOperations(Collections.singletonList(op1));

        List<Lot> allLots = Collections.singletonList(lot1);

        // --- 2. Schedule Initial Très Simple ---
        Batch batch1 = new Batch(1, Collections.singletonList(op1), 0.0, machine1);

        Map<Machine, List<Batch>> scheduleMap = new HashMap<>();
        scheduleMap.put(machine1, new ArrayList<>(Collections.singletonList(batch1)));

        Schedule initialSchedule = new Schedule(0.0, 100.0, scheduleMap, false);

        // --- 3. Initialiser les Outils ---
        DisjuctiveGrapheValidator validator = new DisjuctiveGrapheValidator(allLots);
        SimulatedAnnealingSolver solver = new SimulatedAnnealingSolver(
            validator,
            100.0,  // Température initiale
            0.95,   // Taux de refroidissement
            1000    // Nombre d'itérations
        );

        // --- 4. Lancer le Solver ---
        System.out.println("Lancement du solver...");
        Schedule finalSchedule = solver.solve(initialSchedule);

        // --- 5. Afficher le Résultat ---
        System.out.println("-------------------------");
        System.out.println("TEST SIMPLE TERMINÉ.");
        if (finalSchedule != null) {
            System.out.println("Score final : " + finalSchedule.getF());
            System.out.println("Planning final faisable : " + finalSchedule.isFeasible());
        } else {
            System.out.println("Le solver n'a pas retourné de solution.");
        }
        System.out.println("-------------------------");
    }
}
