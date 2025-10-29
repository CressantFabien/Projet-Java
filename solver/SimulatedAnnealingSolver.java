package solver;

import model.*; 
import graph.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SimulatedAnnealingSolver {

    private final Neighborhood neighborhood; 
    private final DisjuctiveGrapheValidator validator; 
    private final double initialTemperature;
    private final double coolingRate;
    private final int maxIterations; 
    private final Random rand;


    public SimulatedAnnealingSolver(DisjuctiveGrapheValidator validator, double initialTemp, double cooling, int iterations) {
        this.validator = validator;
        this.initialTemperature = initialTemp;
        this.coolingRate = cooling;
        this.maxIterations = iterations;
        this.neighborhood = new Neighborhood(); 
        this.rand = new Random();
    }

    public Schedule solve(Schedule initialSchedule) {
        System.out.println("LOG: Lancement de l'optimisation par Recuit Simulé...");
    
        // 1. Valider la solution initiale
        boolean initialFeasible = validator.validateSchedule(initialSchedule); // Renommé pour clarté
        if (!initialFeasible) {
            System.err.println("ERREUR: La solution initiale n'est pas faisable !");
            return initialSchedule;
        }
    
        // ===>>> APPEL MANQUANT 1 : Calculer le score initial <<<===
        initialSchedule.setF(calculateObjective(initialSchedule));
        System.out.println("Score initial : " + initialSchedule.getF()); // Pour vérifier
    
        Schedule currentSchedule = initialSchedule;
        Schedule bestSchedule = initialSchedule.clone();
        double currentTemperature = this.initialTemperature;
    
        // 2. La grande boucle d'optimisation
        for (int i = 0; i < this.maxIterations; i++) {
    
            // a) Générer un voisin
            Schedule neighborSchedule = neighborhood.generateRandomNeighbor(currentSchedule);
    
            // b) Valider le voisin
            boolean neighborIsFeasible = validator.validateSchedule(neighborSchedule);
    
            // c) Décider si on accepte le voisin
            if (neighborIsFeasible) {
    
                // ===>>> APPEL MANQUANT 2 : Calculer le score du voisin <<<===
                neighborSchedule.setF(calculateObjective(neighborSchedule));
    
                // Calculer la différence de score (delta f)
                double currentScore = currentSchedule.getF();
                double neighborScore = neighborSchedule.getF();
                double delta = neighborScore - currentScore;
    
                // Appliquer le critère d'acceptation
                if (accept(delta, currentTemperature)) {
                    currentSchedule = neighborSchedule;
    
                    // Si ce voisin est le meilleur trouvé jusqu'à présent...
                    if (currentSchedule.getF() > bestSchedule.getF()) {
                        bestSchedule = currentSchedule.clone();
                        System.out.println("    -> Nouvelle meilleure solution trouvée (Score: " + bestSchedule.getF() + ") à l'itération " + i);
                    }
                }
            }
            // (Si le voisin n'est pas faisable, on l'ignore)
    
            // d) Refroidir la température
            currentTemperature *= this.coolingRate;
    
            // Afficher la progression (optionnel)
            if (i > 0 && i % (maxIterations / 10) == 0) { // Afficher tous les 10%
                 System.out.println("Itération " + i + " / " + maxIterations + " | Temp: " + String.format("%.2f", currentTemperature) + " | Score actuel: " + String.format("%.2f", currentSchedule.getF()) + " | Meilleur: " + String.format("%.2f", bestSchedule.getF()));
            }
        }
    
        System.out.println("LOG: Optimisation terminée.");
        System.out.println("Meilleur score final trouvé : " + bestSchedule.getF());
        return bestSchedule;
    }


    
    private boolean accept(double delta, double temperature) {
        // Si c'est une amélioration, on accepte toujours
        if (delta > 0) {
            return true;
        }
        // Si c'est moins bien, on accepte avec une probabilité qui dépend de la différence et de la température
        // Attention: Si temperature devient très proche de 0, Math.exp peut déborder
        if (temperature < 1e-9) return false; // Eviter division par zero / overflow
        
        // Probabilité = exp(delta / T)
        return Math.exp(delta / temperature) > Math.random(); // Math.random() donne un nombre entre 0.0 et 1.0
    }

    private double calculateObjective(Schedule schedule){
        double alpha = 1; // au lieu de 601
        double beta = 200; // au lieu de 1500001
        double gamma = 10; // au lieu de 41

        double fmov = 0;
        double fbatch = 0;
        double fbatchSum = 0.0;
        double fxfactor = 0;
        double fxfactorSum = 0.0;

        double T = schedule.getTimehorizon();

        Map<Machine, List<Batch>> machineMap = schedule.getMachinetobatchmap();
         List<Batch> allBatches = new ArrayList<>();
         List<Machine> allMachines = new ArrayList<>();
        for (List<Batch> batchList : machineMap.values()) {
            allBatches.addAll(batchList);
        }
        
        //Calcul de fmov
        for (Batch batch : allBatches) {
            if (batch.getOperationlist().isEmpty()) {
                 continue;
            }
            double t = batch.getStarttime();
            for (Operation op : batch.getOperationlist()) {
                double teta = 0;
                double p = op.getDuration();
                if(t <= T){ //condition sur teta
                    if(t + p <= T) {teta = 1;}
                    else{ teta = (T-t)/p; }
                }
                //Calcul de la somme
                fmov += teta * (op.getAssociatedlot().getWafercount());
            }
         }

        //Calcul de fbatch
        int batchCountStartedBeforeT = 0; // Dénominateur |B^T|
        for (Batch batch : allBatches) {
            // Est-ce que le batch démarre avant l'horizon T?
            if (batch.getStarttime() < T) { 
                batchCountStartedBeforeT++;
                Machine machine = batch.getAssociatedmachine();
                int Rk = machine.getCapacity(); // Capacité machine
                
                // Zk: Nombre de recettes qualifiées. A METTRE A JOUR si tu l'ajoutes à Machine
                double Zk = 1.0; // Placeholder, car Zk manque dans ta classe Machine
                
                // Taille du batch = nombre d'opérations
                int batchSize = batch.getOperationlist().size(); 
                
                // Somme du numérateur
                if (Rk + (Zk / 100.0) > 0) { // Éviter division par zéro
                     fbatchSum += batchSize / (Rk + (Zk / 100.0));
                }
            }
        }

        // Calcul final de fbatch (attention à la division par zéro)
        if (batchCountStartedBeforeT > 0) {
        fbatch = fbatchSum / batchCountStartedBeforeT;
         }

        //Calcul de fxfactor
        int completedLotCount = 0; // Dénominateur |J^T|
    
        // On itère sur tous les LOTS uniques du schedule
        Set<Lot> allLotsInSchedule = new HashSet<>();
        for (Batch batch : allBatches) {
            for (Operation op : batch.getOperationlist()) {
                allLotsInSchedule.add(op.getAssociatedlot());
            }
        }
        for (Lot lot : allLotsInSchedule) {
            List<Operation> lotOps = lot.getOperations();
            if (lotOps.isEmpty()) continue; // Skip les lots sans opérations
    
            // Trouver la DERNIÈRE opération du lot
            Operation lastOp = lotOps.get(lotOps.size() - 1);
            
            // Trouver le Batch qui contient cette dernière opération
            Batch batchContainingLastOp = null;
            for (Batch b : allBatches) {
                if (b.getOperationlist().contains(lastOp)) {
                    batchContainingLastOp = b;
                    break;
                }
            }
    
            // Si la dernière op n'est pas dans le schedule, le lot n'est pas complété
            if (batchContainingLastOp == null) continue;
    
            // Récupérer les infos nécessaires pour la formule
            double t_last = batchContainingLastOp.getStarttime(); // t_in_i
            double p_last = lastOp.getDuration(); // p_in_i
            double completionTime = t_last + p_last; // Fin de la dernière opération
    
            // Vérifier si le LOT est COMPLÉTÉ avant T [cite: 218]
            if (completionTime <= T) {
                completedLotCount++; // Incrémenter le dénominateur
                
                int c = lot.getPriority(); // c_i
                double r = lot.getEarlieststartdate(); // r_i
    
                // Calculer la partie de la somme pour ce lot
                if (p_last > 0) { // Éviter division par zéro
                    fxfactorSum += c * (completionTime - r) / p_last;
                }
            }
        }
    
        // Calcul final de fxfactor (attention à la division par zéro)
        if (completedLotCount > 0) {
            fxfactor = fxfactorSum / completedLotCount;
        }



        //Calcul final
        return alpha * fmov + beta * fbatch - gamma * fxfactor;
    }
}