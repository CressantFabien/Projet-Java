package solver;
import model.*;
import java.util.*;

public class Neighborhood {
    private Random rand;

    public Neighborhood(){
        this.rand = new Random();
    }

    public Schedule generateRandomNeighbor(Schedule originalSchedule){
       // On clone le schedule original pour créer un voisin
        Schedule neighborSchedule = originalSchedule.clone();

        // Tire un nombre aléatoire
        double randomValue = rand.nextDouble();
       // 50% batch move, 25% operation move, 25% operation switch
        if(randomValue < 0.5){
            applyBatchMove(neighborSchedule);
        } else if(randomValue < 0.75){
            applyOperationMove(neighborSchedule);
        } else{
            applyOperationSwitch(neighborSchedule);
        }

        return neighborSchedule;
    }

    private void applyBatchMove(Schedule schedule){
        // Récupérer la map machine -> batches
        Map<Machine, List<Batch>> machineMap = schedule.getMachinetobatchmap();

        if (machineMap.size() < 2) {
            return; // Pas assez de machines pour déplacer un batch
        }

        // Trouver une machine source aléatoire
        List<Machine> machines = new ArrayList<>();
        for (Machine m : machineMap.keySet()) {
            if (machineMap.get(m).size() > 0) {
                machines.add(m);
            }
        }
        if (machines.size() < 1) {
            return; // Pas assez de machines avec des batches
        }
        // Sélectionner une machine source aléatoire, ses batches, et un batch à déplacer et le retirer
        Machine sourceMachine = machines.get(rand.nextInt(machines.size()));
        List<Batch> sourceBatches = machineMap.get(sourceMachine);
        int batchIndex = rand.nextInt(sourceBatches.size());
        Batch batchToMove = sourceBatches.remove(batchIndex);

        // Sélectionner une machine cible aléatoire différente et ses batches, parmis toutes las machines
        List<Machine> allmachines = new ArrayList<>(machineMap.keySet());
        Machine targetMachine = allmachines.get(rand.nextInt(allmachines.size()));
        List<Batch> targetBatches = machineMap.get(targetMachine);

        int targetIndex = 0;
        targetIndex = rand.nextInt(targetBatches.size() + 1); // +1 pour permettre l'insertion à la fin
        targetBatches.add(targetIndex, batchToMove);

        // Mettre à jour la map dans le schedule
        batchToMove.setAssociatedmachine(targetMachine);
    }

    private void applyOperationMove(Schedule schedule) {
        // Recuperer tous les batches
        Map<Machine, List<Batch>> machineMap = schedule.getMachinetobatchmap();
        List<Batch> allBatches = new ArrayList<>();
        for (List<Batch> batchList : machineMap.values()) {
            allBatches.addAll(batchList);
        }
        if (allBatches.isEmpty()) return; // Rien à faire
    
        //Choisir un Batch A au hasard (qui a plus d'une op)
        List<Batch> candidateBatchesA = new ArrayList<>();
        for (Batch batch : allBatches) {
            if (batch.getOperationlist().size() > 1) {
                candidateBatchesA.add(batch);
            }
        }
        // S'il n'y a pas de batch avec > 1 op, on ne peut pas faire ce mouvement
        if (candidateBatchesA.isEmpty()) return; 
    
        Batch batchA = candidateBatchesA.get(rand.nextInt(candidateBatchesA.size()));
        
        //Retirer une Opération de ce Batch
        List<Operation> operationsA = batchA.getOperationlist();
        int opIndex = rand.nextInt(operationsA.size()); // Index pour la ré-insertion si besoin
        Operation operationToMove = operationsA.remove(opIndex);
        Recipe recipeToMatch = operationToMove.getRecipe();
        Lot lotToMove = operationToMove.getAssociatedlot();
    
        // Trouver un autre Batch B au hasard qui est "compatible"
        List<Batch> candidateBatchesB = new ArrayList<>();
        for (Batch batch : allBatches) {
            if (batch == batchA) continue;
            
            // Cas 1: Le batch est vide, il est toujours compatible
            if (batch.getOperationlist().isEmpty()) {
                candidateBatchesB.add(batch);
                continue;
            }
    
            // Cas 2: Le batch n'est pas vide
            // Vérifier la recette
            if (batch.getOperationlist().get(0).getRecipe().equals(recipeToMatch)) {
                // Vérifier la capacité de la machine
                Set<Lot> lotsInBatch = new HashSet<>();
                for (Operation op : batch.getOperationlist()) {
                    lotsInBatch.add(op.getAssociatedlot());
                }
                lotsInBatch.add(lotToMove); // Ajoute le lot de l'op qu'on veut déplacer
    
                if (lotsInBatch.size() <= batch.getAssociatedmachine().getCapacity()) {
                    candidateBatchesB.add(batch);
                }
            }
        }
    
        //Ajouter l'Opération O au Batch B (si on en a trouvé un)
        if (candidateBatchesB.size() > 0) {
            Batch batchB = candidateBatchesB.get(rand.nextInt(candidateBatchesB.size()));
            batchB.getOperationlist().add(operationToMove);
        }
        //Sinon, créer un nouveau Batch
        else {
            // --- CORRECTION APPLIQUÉE ---
            // L'objectif est de placer le nouveau batch sur n'importe quelle machine,
            // pas seulement la machine d'origine.
    
            // 1. Récupérer TOUTES les machines disponibles
            List<Machine> allMachines = new ArrayList<>(machineMap.keySet());
    
            // 2. Vérification de sécurité (devrait être impossible si batchA existe)
            if (allMachines.isEmpty()) { 
                operationsA.add(opIndex, operationToMove); // Annuler : remettre l'op
                return;
            }
    
            // 3. Choisir une machine cible AU HASARD
            Machine machineForNewBatch = allMachines.get(rand.nextInt(allMachines.size()));
            // --- FIN DE LA CORRECTION ---
    
            // Créer le nouveau batch avec l'opération
            List<Operation> newBatchOps = new ArrayList<>();
            newBatchOps.add(operationToMove);
            
            // Assumer que votre constructeur Batch(id, ops, start, machine)
            // associe bien l'opération et la machine
            Batch newBatch = new Batch(-1, newBatchOps, 0.0, machineForNewBatch); 
            
            // Ajouter ce nouveau batch à la liste de la machine cible (aléatoire)
            machineMap.get(machineForNewBatch).add(newBatch);
        }
    }
    

    private void applyOperationSwitch(Schedule schedule) {
        Map<Machine, List<Batch>> machineMap = schedule.getMachinetobatchmap();

        //Recuperer tous les batches QUI NE SONT PAS VIDES
        List<Batch> allNonEmptyBatches = new ArrayList<>();
        for (List<Batch> batchList : machineMap.values()) {
            for (Batch batch : batchList) {
                if (!batch.getOperationlist().isEmpty()) {
                    allNonEmptyBatches.add(batch);
                }
            }
        }

        // On a besoin d'au moins 2 opérations au total (ex: 2 batches de 1 op)
        if (allNonEmptyBatches.size() < 2 && (allNonEmptyBatches.size() == 1 && allNonEmptyBatches.get(0).getOperationlist().size() < 2)) {
            return;
        }

        // Choisir un Batch A au hasard (parmi les non-vides)
        Batch batchA = allNonEmptyBatches.get(rand.nextInt(allNonEmptyBatches.size()));
        
        // Retirer une Opération O_A de ce Batch
        List<Operation> operationsA = batchA.getOperationlist();
        int opIndexA = rand.nextInt(operationsA.size());
        Operation operationToMoveA = operationsA.remove(opIndexA);
        Recipe recipeToMatch = operationToMoveA.getRecipe();

        // Trouver un autre Batch B "compatible"
        List<Batch> candidateBatchesB = new ArrayList<>();
        for (Batch batchB : allNonEmptyBatches) {
            // Doit être un batch différent
            if (batchB == batchA) continue;
            if (batchB.getOperationlist().get(0).getRecipe().equals(recipeToMatch)) {
                candidateBatchesB.add(batchB);
            }
        }

        // Si on n'a trouvé aucun candidat pour l'échange
        if (candidateBatchesB.isEmpty()) {
            operationsA.add(opIndexA, operationToMoveA); 
            return; 
        }

        // Choisir un Batch B et en retirer une Opération O_B
        Batch batchB = candidateBatchesB.get(rand.nextInt(candidateBatchesB.size()));
        List<Operation> operationsB = batchB.getOperationlist();
        
        int opIndexB = rand.nextInt(operationsB.size());
        Operation operationToMoveB = operationsB.remove(opIndexB);

        //Mettre O_A dans Batch B et O_B dans Batch A (L'ÉCHANGE)
        batchB.getOperationlist().add(operationToMoveA);
        batchA.getOperationlist().add(operationToMoveB);
    }
    
}