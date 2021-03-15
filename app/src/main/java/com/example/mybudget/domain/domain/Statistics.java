package com.example.mybudget.domain.domain;

import androidx.annotation.NonNull;

/**
 * Statistics class - Holds all the statistics data of expenses
 */
public class Statistics {
    private String payDate;
    private double sum;
    private double mean;
    private String category;

    public Statistics(String payDate, double sum, String category)
    {
        setSum(sum);
        setPayDate(payDate);
        setCategory(category);
    }

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @NonNull
    @Override
    public String toString() {
        System.out.println("------------------------------ statistics -----------------------------");
        return this.category + ": "+ this.payDate + ": "+ this.sum;
    }
}
