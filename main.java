import graph.*;
import model.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Création de quelques objets factices pour tester la compilation
        Recipe recipe1 = new Recipe("R1");

        // Création de lots et opérations

        Lot lot1 = new Lot(1, 1, 0.0, 10, new ArrayList<>()); //idlot,priority,earlieststartdate,wafercount, operations
        Lot lot2 = new Lot(2, 2, 5.0, 20, new ArrayList<>());

        Operation op1 = new Operation(1, 3.0, 1.0, 5.0, recipe1, lot1); //idoperation, duration, mindelayafter,maxdelayafter, recipe, associatedlot
        Operation op2 = new Operation(2, 2.0, 0.5, 4.0, recipe1, lot1);
        Operation op3 = new Operation(3, 1.5, 0.5, 3.0, recipe1, lot2);

        // Ajouter les opérations aux lots
        lot1.setOperations(Arrays.asList(op1, op2));
        lot2.setOperations(Collections.singletonList(op3));

        // Créer une liste de lots
        List<Lot> allLots = Arrays.asList(lot1, lot2);

        // Instancier le validateur de graphe
        DisjuctuveGrapheValidator validator = new DisjuctuveGrapheValidator(allLots);

        System.out.println("Disjunctive Graph Validator créé avec succès !");

        // Optionnel : créer un schedule factice pour tester la méthode (vide pour l'instant)
        Map<Machine, List<Batch>> machineToBatch = new HashMap<>();
        Schedule schedule = new Schedule(0.0, 10.0, machineToBatch, true);

        // Ici on pourrait appeler la méthode de validation (même si elle n'est pas encore implémentée)
        // boolean result = validator.validateschedule(schedule);
        // System.out.println("Résultat de validation : " + result);
    }
}