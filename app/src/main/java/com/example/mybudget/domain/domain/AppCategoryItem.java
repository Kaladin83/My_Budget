package com.example.mybudget.domain.domain;

public class AppCategoryItem {
    private final String category;
    private final int color;

    public AppCategoryItem(String category, int color) {
        this.category = category;
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public int getColor() {
        return color;
    }
}
