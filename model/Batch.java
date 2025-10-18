package model;
import java.util.List;
import java.util.Objects;

public class Batch implements Cloneable {
    private int idbatch;
    private List<Operation> operationlist;
    private double starttime;
    private Machine associatedmachine;

    //constructor
    public Batch(int idbatch, liste<Operation> operationlist, double starttime, Machine associatedmachine) {
        this.idbatch = idbatch;
        this.operationlist = operationlist;
        this.starttime = starttime;
        this.associatedmachine = associatedmachine;
    }
    //getters and setters
    public int getIdbatch() {
        return idbatch;
    }
    public void setIdbatch(int idbatch) {
        this.idbatch = idbatch;
    }
    public liste<Operation> getOperationlist() {
        return operationlist;
    }
    public void setOperationlist(liste<Operation> operationlist) {
        this.operationlist = operationlist;
    }
    public double getStarttime() {
        return starttime;
    }
    public void setStarttime(double starttime) {
        this.starttime = starttime;
    }
    public Machine getAssociatedmachine() {
        return associatedmachine;
    }
    public void setAssociatedmachine(Machine associatedmachine) {
        this.associatedmachine = associatedmachine;
    }

    public Batch clone(){
        try {
            return (Batch) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }
}

