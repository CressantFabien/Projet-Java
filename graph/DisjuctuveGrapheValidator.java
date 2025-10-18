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

}
