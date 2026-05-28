package com.sfcomparator.model;

public class Field {
    private String name;
    private String type;

    public Field(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Field other)) return false;
        return name.equals(other.name) && type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
