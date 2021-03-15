package com.example.mybudget.domain.dtos;

public class ItemToAdd {
    private final String otherCategoryName;
    private final String parentCategoryName;
    private final String givenCategoryName;
    private final double amount;
    private final String description;

    public ItemToAdd(String otherCategoryName, String parentCategoryName, String givenCategoryName, double amount,
                     String description) {
        this.otherCategoryName = otherCategoryName;
        this.parentCategoryName = parentCategoryName;
        this.givenCategoryName = givenCategoryName;
        this.amount = amount;
        this.description = description;
    }

    public String getOtherCategoryName() {
        return otherCategoryName;
    }

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public String getGivenCategoryName() {
        return givenCategoryName;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
