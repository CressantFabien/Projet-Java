package graph;

import java.util.*;
import model.*;

public class DisjuctuveGrapheValidator {
   private List<Arc> BaseConjArcs;
   private Map<Operation, Node> operationNodeMap;
   private Node node_0;
   private List<Node> nodes;

   public DisjuctuveGrapheValidator(List<Lot> alllots){
         this.BaseConjArcs = new ArrayList<>();
         this.operationNodeMap = new HashMap<>();
         this.nodes = new ArrayList<>();

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
                    BaseConjArcs.add(arcBetweenOps);
                    //Arc maxtimelag
                    double maxDelay = currentOp.getMaxdelayafter() + currentOp.getDuration();
                    Arc arcmaxdelay = new Arc(nextNode, currentNode, -maxDelay);
                    BaseConjArcs.add(arcmaxdelay);
                }
            }
 
   }

   public boolean validateschedule(Schedule schedule){
    
   }
   
   //ASSISTANT 1 : Remet à zéro les distances de tous les nœuds
  private void resetNodeDistances() {
      System.out.println("LOG: Réinitialisation des distances des nœuds...");
      for (Node node : this.nodes) {
          if (node == this.node_0) {
              node.setDistance(0.0); // Le nœud 0 est toujours à 0
          } else {
              node.setDistance(Double.NEGATIVE_INFINITY);
          }
          node.setNodepredecessor(null); // Réinitialiser le prédécesseur
      }
  }
  
  // ASSISTANT 2 : Construit les arcs dynamiques (Batch et Séquencement)
  private List<Arc> buildDynamicArcs(Schedule scheduleToTest) {
      System.out.println("LOG: Construction des arcs dynamiques...");
      List<Arc> dynamicArcs = new ArrayList<>();
      
      // Map temporaire pour les nœuds fictifs de batch de CE schedule
      Map<Batch, Node> batchNodeMap = new HashMap<>();
  
      // Étape 1: Créer les nœuds fictifs de Batch
      for (List<Batch> batchList : scheduleToTest.getMachinetobatchmap().values()) {
          for (Batch batch : batchList) {
              Node batchNode = new Node(null); // Nouveau nœud fictif
              this.nodes.add(batchNode); // On l'ajoute temporairement
              batchNodeMap.put(batch, batchNode);
          }
      }
  
      // Étape 2: Créer les arcs de Synchronisation (LOG 5)
      for (Batch batch : batchNodeMap.keySet()) {
          Node batchNode = batchNodeMap.get(batch);
          for (Operation op : batch.getOperationlist()) {
              Node opNode = this.operationNodeMap.get(op); // On récupère le nœud statique
              
              // Arcs de synchro (poids 0)
              dynamicArcs.add(new Arc(batchNode, opNode, 0.0));
              dynamicArcs.add(new Arc(opNode, batchNode, 0.0));
          }
      }
  
      // Étape 3: Créer les arcs de Séquencement Machine (LOG 6)
      for (Map.Entry<Machine, List<Batch>> entry : scheduleToTest.getMachinetobatchmap().entrySet()) {
          Machine machine = entry.getKey();
          List<Batch> machineSequence = entry.getValue();
          
          for (int i = 0; i < machineSequence.size() - 1; i++) {
              Batch batchA = machineSequence.get(i);
              Batch batchB = machineSequence.get(i + 1);
  
              Node nodeA = batchNodeMap.get(batchA);
              Node nodeB = batchNodeMap.get(batchB);
  
              // Poids = Durée de A + Délais machine
              double durationA = batchA.getOperationlist().get(0).getDuration();
              double delay = machine.getUploadingtime() + 
                             machine.getInterbatchtime() + 
                             machine.getSetuptime();
              double weight = durationA + delay;
  
              dynamicArcs.add(new Arc(nodeA, nodeB, weight));
          }
      }
      
      // Étape 4: Retirer les nœuds fictifs temporaires (ils ne servent plus)
      this.nodes.removeAll(batchNodeMap.values());
  
      return dynamicArcs;
  }
  
  //ASSISTANT 3 : Exécute l'algorithme de Bellman-Ford.
  private boolean runBellmanFord(List<Arc> dynamicArcs) {
      System.out.println("LOG: Lancement de Bellman-Ford...");
      
      // 1. Créer la liste complète des arcs
      List<Arc> allArcs = new ArrayList<>(this.BaseConjArcs);
      allArcs.addAll(dynamicArcs);
  
      // TODO: Écrire la logique de Bellman-Ford
      // 2. Boucle de Relaxation (N-1 fois)
      // 3. Boucle de Détection de Cycle (1 fois)
      
      // Pour l'instant, on dit que c'est toujours faisable :
      return true; 
  }
  
  //ASSISTANT 4 : Met à jour les 'startTime' dans les objets Batch.
  private void updateScheduleStartTimes(Schedule scheduleToTest) {
      System.out.println("LOG: Mise à jour des dates de début...");
      for (List<Batch> batchList : scheduleToTest.getMachinetobatchmap().values()) {
          for (Batch batch : batchList) {
              Operation op = batch.getOperationlist().get(0);
              Node opNode = this.operationNodeMap.get(op);
              
              batch.setStarttime(opNode.getDistance());
          }
      }
  }
}
