package model;

public class Recipe {
    private String value;

    public Recipe(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Recipe other) {
        return this.value.equals(other.value);
    }
    public int hashCode() {
        return value.hashCode();
    }
}