package com.example.mybudget.domain.domain;

public class Statistics {
    private final double min;
    private final double max;
    private final double avg;
    private final double sum;
    private final int cnt;
    public Statistics(double min, double max, double avg, double sum, int cnt) {
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.sum = sum;
        this.cnt = cnt;
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
