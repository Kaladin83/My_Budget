package com.example.mybudget.domain.dtos;

/**
 * Statistics by date and category
 * */
public class TableStatistics {
    private final String payDate;
    private final String category;
    private final double min;
    private final double max;
    private final double avg;
    private final double sum;
    private final int cnt;

    public TableStatistics(String payDate, String category, double min, double max, double avg, double sum, int cnt) {
        this.payDate = payDate;
        this.category = category;
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.sum = sum;
        this.cnt = cnt;
    }

    public String getPayDate() {
        return payDate;
    }

    public String getCategory() {
        return category;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getAvg() {
        return avg;
    }

    public double getSum() {
        return sum;
    }

    public int getCnt() {
        return cnt;
    }
}
