package com.example.mybudget.domain.domain;

/**
 * Category class - Holds all the data of categories
 */
public class Category {
    private final String name;
    private final String parent;
    private final int color;
    private Integer icon;
    private String otherName;
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Category(String name, String parent, int color) {
        this.name = name;
        this.parent = parent;
        this.color = color;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
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

    public Integer getIcon() {
        return icon;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
