package model;
import java.util.List;

public class Lot {
    private int idlot;
    private int priority;
    private double earlieststartdate;
    private int wafercount;
    private List<Operation> operations;

    public Lot(int idlot, int priority, double earlieststartdate, int wafercount, list<Operation> operations) {
        this.idlot = idlot;
        this.priority = priority;
        this.earlieststartdate = earlieststartdate;
        this.wafercount = wafercount;
        this.operations = operations;
    }

    //getters
    public int getIdlot() { return idlot;}
    public int getPriority() { return priority;}
    public double getEarlieststartdate() { return earlieststartdate;}
    public int getWafercount() { return wafercount;}
    public List<Operation> getOperations() { return operations;}
    //setters
    public void setIdlot(int idlot) { this.idlot = idlot;}
    public void setPriority(int priority) { this.priority = priority;}
    public void setEarlieststartdate(double earlieststartdate) { this.earlieststartdate = earlieststartdate;}
    public void setWafercount(int wafercount) { this.wafercount = wafercount;}
    public void setOperations(List<Operation> operations) { this.operations = operations;}


}