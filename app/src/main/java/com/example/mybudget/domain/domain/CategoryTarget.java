package com.example.mybudget.domain.domain;

public class CategoryTarget {
    private final String payDate;
    private final int sum;

    public CategoryTarget(String payDate, int sum) {
        this.payDate = payDate;
        this.sum = sum;
    }

    public String getPayDate() {
        return payDate;
    }

    public int getSum() {
        return sum;
    }
}
