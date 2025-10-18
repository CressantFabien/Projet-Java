coucou
package graph;

class Arc{
    private Node beginnode;
    private Node endnode;
    private double weight;

    public Arc(Node beginnode, Node endnode, double weight) {
        this.beginnode = beginnode;
        this.endnode = endnode;
        this.weight = weight;
    }
    //getters 
    public Node getBeginnode() { return beginnode; }
    public Node getEndnode() { return endnode; }
    public double getWeight() { return weight; }
    //setters
    public void setBeginnode(Node beginnode) { this.beginnode = beginnode; }
    public void setEndnode(Node endnode) { this.endnode = endnode; }
    public void setWeight(double weight) { this.weight = weight; }
    
}