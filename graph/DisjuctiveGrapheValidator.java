package graph;

import java.util.*;
import model.*;

public class DisjuctiveGrapheValidator {
   private List<Arc> BaseConjArcs;
   private Map<Operation, Node> operationNodeMap;
   private Node node_0;
   private List<Node> nodes;
   private Map<Batch, Node> tempBatchNodeMap;

   public DisjuctiveGrapheValidator(List<Lot> alllots){
         this.BaseConjArcs = new ArrayList<>();
         this.operationNodeMap = new HashMap<>();
         this.nodes = new ArrayList<>();
         this.tempBatchNodeMap = new HashMap<>();

         //création du noeud de départ
        this.node_0 = new Node(null);
        this.node_0.setDistance(0);
        this.nodes.add(node_0);

         // créations des noeuds pour chaque opération
         for(Lot lot : alllots){
              for(Operation op : lot.getOperations()){
                Node node = new Node(op);
                nodes.add(node);
                operationNodeMap.put(op, node);
              }
         }
    
         // créations arc predecedence
         //arc départ
            for(Lot lot : alllots){
                List<Operation> ops = lot.getOperations();
                Operation firstOp = ops.get(0);
                Node firstNode = operationNodeMap.get(firstOp);
                Arc arcFromStart = new Arc(node_0, firstNode, lot.getEarlieststartdate());
                BaseConjArcs.add(arcFromStart);
    
        //arcs entre opérations
                for(int i = 0; i < ops.size() - 1; i++){
                    Operation currentOp = ops.get(i);
                    Operation nextOp = ops.get(i + 1);
                    Node currentNode = operationNodeMap.get(currentOp);
                    Node nextNode = operationNodeMap.get(nextOp);
                    //Arc mintimelag
                    double minDelay = currentOp.getMindelayafter() + currentOp.getDuration();
                    Arc arcmindelay = new Arc(currentNode, nextNode, minDelay);
                    BaseConjArcs.add(arcmindelay);
                    //Arc maxtimelag
                    double maxDelay = currentOp.getMaxdelayafter() + currentOp.getDuration();
                    Arc arcmaxdelay = new Arc(nextNode, currentNode, -maxDelay);
                    BaseConjArcs.add(arcmaxdelay);
                }
            }
 
   }

   public boolean validateSchedule(Schedule scheduleToTest) {
    
    resetNodeDistances();

    List<Arc> dynamicArcs = buildDynamicNodesAndArcs(scheduleToTest);

    boolean isFeasible = runBellmanFord(dynamicArcs);

    scheduleToTest.setFeasible(isFeasible);
    if (isFeasible) {
        updateScheduleStartTimes(scheduleToTest);
        // Ici, tu ajouteras aussi le calcul de la fonction objectif 'f'
        // scheduleToTest.setF(...); 
    } else {
        scheduleToTest.setF(Double.NEGATIVE_INFINITY);
    }

    cleanupDynamicNodes();

    return isFeasible;
}
// ASSISTANT 1 : Réinitialise les distances des nœuds avant chaque validation   
  private void resetNodeDistances() {
      for (Node node : this.nodes) {
          if (node == this.node_0) {
              node.setDistance(0.0); // Le nœud 0 est toujours à 0
          } else {
              node.setDistance(Double.NEGATIVE_INFINITY);
          }
          node.setNodePredecessor(null); // Réinitialiser le prédécesseur
      }
  }
  
// ASSISTANT 2 : Construit les arcs dynamiques (Batch et Séquencement)
  private List<Arc> buildDynamicNodesAndArcs(Schedule scheduleToTest) {
      List<Arc> dynamicArcs = new ArrayList<>();
      
      // Map temporaire pour les nœuds fictifs de batch de CE schedule
      this.tempBatchNodeMap.clear();
  
      // Étape 1: Créer les nœuds fictifs de Batch
      for (List<Batch> batchList : scheduleToTest.getMachinetobatchmap().values()) {
          for (Batch batch : batchList) {
              Node batchNode = new Node(null); // Nouveau nœud fictif
              this.nodes.add(batchNode); // On l'ajoute temporairement
              this.tempBatchNodeMap.put(batch, batchNode);
          }
      }
  
      // Étape 2: Créer les arcs Batch-Opération
      for (Batch batch : this.tempBatchNodeMap.keySet()) {
          Node batchNode = this.tempBatchNodeMap.get(batch);
          for (Operation op : batch.getOperationlist()) {
              Node opNode = this.operationNodeMap.get(op); 
              dynamicArcs.add(new Arc(batchNode, opNode, 0.0));
              dynamicArcs.add(new Arc(opNode, batchNode, 0.0));
          }
      }
  
      // Étape 3: Créer les arcs de Séquencement Machine 
      for (Map.Entry<Machine, List<Batch>> entry : scheduleToTest.getMachinetobatchmap().entrySet()) {
          Machine machine = entry.getKey();
          List<Batch> machineSequence = entry.getValue();
          
          for (int i = 0; i < machineSequence.size() - 1; i++) {
              Batch batchA = machineSequence.get(i);
              Batch batchB = machineSequence.get(i + 1);
  
              Node nodeA = this.tempBatchNodeMap.get(batchA);
              Node nodeB = this.tempBatchNodeMap.get(batchB);
  
              // Poids = Durée de A + Délais machine
              double durationA = batchA.getOperationlist().get(0).getDuration();
              double delay = machine.getUploadingtime() + 
                             machine.getInterbatchtime() + 
                             machine.getSetuptime();
              double weight = durationA + delay;
  
              dynamicArcs.add(new Arc(nodeA, nodeB, weight));
          }
      }
    
  
      return dynamicArcs;
  }
  
  //ASSISTANT 3 : Exécute l'algorithme de Bellman-Ford.
  private boolean runBellmanFord(List<Arc> dynamicArcs) {

    // 1. Créer la liste complète de tous les arcs (statiques + dynamiques)
    List<Arc> allArcs = new ArrayList<>(this.BaseConjArcs);
    allArcs.addAll(dynamicArcs);

    // On récupère le nombre total de nœuds (statiques + temporaires)
    // this.nodes contient tous les nœuds à ce stade (statiques + batchs fictifs)
    int nodeCount = this.nodes.size();

    // 2. Boucle de Relaxation (N-1 fois)
    // On cherche le PLUS LONG CHEMIN (d'où la condition >)
    for (int i = 0; i < nodeCount - 1; i++) {
        boolean relaxed = false; // Optimisation : si on ne relaxe rien, on peut s'arrêter

        for (Arc arc : allArcs) {
            Node u = arc.getBeginnode(); // Nœud de départ
            Node v = arc.getEndnode();   // Nœud d'arrivée
            double weight = arc.getWeight();

            // Si le nœud de départ n'est pas encore atteint (distance = -inf),
            // on ne peut pas propager de chemin depuis lui.
            if (u.getDistance() == Double.NEGATIVE_INFINITY) {
                continue;
            }

            // C'est la relaxation du plus long chemin :
            // Si le chemin via U est meilleur (plus long) que le chemin actuel vers V...
            if (u.getDistance() + weight > v.getDistance()) {
                // ... on met à jour la distance de V
                v.setDistance(u.getDistance() + weight);
                // On met à jour le prédécesseur (pour le débogage)
                v.setNodePredecessor(u); // Utilise ton setter
                relaxed = true;
            }
        }

        // Si on a fait un tour complet sans rien changer, c'est qu'on a fini.
        if (!relaxed) {
            break;
        }
    }

    // 3. Boucle de Détection de Cycle (1 fois)
    // On refait un tour pour voir si on peut ENCORE améliorer un chemin.
    for (Arc arc : allArcs) {
        Node u = arc.getBeginnode();
        Node v = arc.getEndnode();
        double weight = arc.getWeight();

        if (u.getDistance() == Double.NEGATIVE_INFINITY) {
            continue;
        }

        // Si on peut ENCORE relaxer un arc, cela veut dire qu'il y a un
        // cycle de poids positif. Le planning est impossible !
        if (u.getDistance() + weight > v.getDistance()) {
            return false; // INFAISABLE
        }
    }

    // Si on arrive ici, aucun cycle positif n'a été trouvé.
    return true; // FAISABLE
}
  
  //ASSISTANT 4 : Met à jour les 'startTime' dans les objets Batch.
  private void updateScheduleStartTimes(Schedule scheduleToTest) {
      for (List<Batch> batchList : scheduleToTest.getMachinetobatchmap().values()) {
          for (Batch batch : batchList) {
              Operation op = batch.getOperationlist().get(0);
              Node opNode = this.operationNodeMap.get(op);
              
              batch.setStarttime(opNode.getDistance());
          }
      }
  }
//ASSISTANT 5 : Nettoie les nœuds dynamiques après validation
  private void cleanupDynamicNodes() {
    this.nodes.removeAll(this.tempBatchNodeMap.values());
    this.tempBatchNodeMap.clear();
}
}
