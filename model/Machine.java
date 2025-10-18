package model;
import java.util.*;

public class Machine {
    private int idmachine;
    private int capacity;
    private double setuptime;
    private double uploadingtime;
    private double interbatchtime;
    private double initialsetuptime;

    public Machine(int idmachine, int capacity, double setuptime, double uploadingtime, double interbatchtime, double initialsetuptime) {
        this.idmachine = idmachine;
        this.capacity = capacity;
        this.setuptime = setuptime;
        this.uploadingtime = uploadingtime;
        this.interbatchtime = interbatchtime;
        this.initialsetuptime = initialsetuptime;
    }
    //getters
    public int getIdmachine() { return idmachine;}
    public int getCapacity() { return capacity;}
    public double getSetuptime() { return setuptime;}
    public double getUploadingtime() { return uploadingtime;}
    public double getInterbatchtime() { return interbatchtime;}
    public double getInitialsetuptime() { return initialsetuptime;}

    //setters
    public void setIdmachine(int idmachine) { this.idmachine = idmachine;}
    public void setCapacity(int capacity) { this.capacity = capacity;}
    public void setSetuptime(double setuptime) { this.setuptime = setuptime;}
    public void setUploadingtime(double uploadingtime) { this.uploadingtime = uploadingtime;}
    public void setInterbatchtime(double interbatchtime) { this.interbatchtime = interbatchtime;}
    public void setInitialsetuptime(double initialsetuptime) { this.initialsetuptime = initialsetuptime;}

    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return idmachine == machine.idmachine;
    }

    public int hashCode() {
        return Integer.hashCode(idmachine);
    }
}