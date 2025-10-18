package graph;

import model.Operation;

class Node{
    private Operation nodeoperation;
    private double distance;
    private Node nodepredecessor;

    public Node(Operation nodeoperation){
        this.nodeoperation = nodeoperation;
        this.distance = Double.NEGATIVE_INFINITY;
        this.nodepredecessor = null;
    }

    // Getters
    public Operation getNodeoperation() {return nodeoperation;}
    public double getDistance() {return distance;} 
    public Node getNodePredecessor() {return nodepredecessor;}

    //setters
    public void setNodeoperation(Operation nodeoperation) { this.nodeoperation = nodeoperation; }
    public void setDistance(double distance) { this.distance = distance;}
    public void setNodePredecessor(Node nextnode) { this.nodepredecessor = nextnode;}
}