package model;
import java.util.Objects;

public class Operation {
    private int idoperation;
    private double duration;
    private double mindelayafter;
    private double maxdelayafter;
    private Recipe recipe;
    private Lot associatedlot;

    public Operation(int idoperation, double duration, double mindelayafter, double maxdelayafter, Recipe recipe, Lot associatedlot) {
        this.idoperation = idoperation;
        this.duration = duration;
        this.mindelayafter = mindelayafter;
        this.maxdelayafter = maxdelayafter;
        this.recipe = recipe;
        this.associatedlot = associatedlot;
    }
    //getters
    public int getIdoperation() { return idoperation;}
    public double getDuration() { return duration;}
    public double getMindelayafter() { return mindelayafter;}
    public double getMaxdelayafter() { return maxdelayafter;}
    public Recipe getRecipe() { return recipe;}
    public Lot getAssociatedlot() { return associatedlot;}
    //setters
    public void setIdoperation(int idoperation) { this.idoperation = idoperation;}
    public void setDuration(double duration) { this.duration = duration;}
    public void setMindelayafter(double mindelayafter) { this.mindelayafter = mindelayafter;}
    public void setMaxdelayafter(double maxdelayafter) { this.maxdelayafter = maxdelayafter;}
    public void setRecipe(Recipe recipe) { this.recipe = recipe;}
    public void setAssociatedlot(Lot associatedlot) { this.associatedlot = associatedlot;}

    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return idoperation == operation.idoperation;
    }
    public int hashCode() {
        return Integer.hashCode(idoperation);
    }

}