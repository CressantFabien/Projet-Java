package model;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
package graph;

public class Schedule implements Cloneable {
    public double f;
    public double timehorizon;
    public Map<Machine, list<Batch>> machinetobatchmap;
    public boolean feasible;

    public Schedule(double f, double timehorizon, Map<Machine, list<Batch>> machinetobatchmap, boolean feasible){
        this.f = f;
        this.timehorizon = timehorizon;
        this.machinetobatchmap = machinetobatchmap;
        this.feasible = feasible;
    }

    //getters
    public double getF() { return f;}
    public double getTimehorizon() { return timehorizon;}
    public Map<Machine, list<Batch>> getMachinetobatchmap() { return machinetobatchmap;}
    public boolean isFeasible() { return feasible;}
    //setters
    public void setF(double f) { this.f = f;}
    public void setTimehorizon(double timehorizon) { this.timehorizon = timehorizon;}
    public void setMachinetobatchmap(Map<Machine, list<Batch>> machinetobatchmap) { this.machinetobatchmap = machinetobatchmap;}
    public void setFeasible(boolean feasible) { this.feasible = feasible;}

    public Schedule clone(){
        try {
            return (Schedule) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }
}   