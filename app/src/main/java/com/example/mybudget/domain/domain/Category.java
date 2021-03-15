package com.example.mybudget.domain.domain;

/**
 * Category class - Holds all the data of categories
 */
public class Category {
    private String name;
    private String parent;
    private String otherName;
    private int color;

    public Category(String name, String parent, int color) {
        setName(name);
        setParent(parent);
        setColor(color);
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public int getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }
}
