package com.example.mybudget.domain.domain;

public class DayExpense {
    private final String month;
    private final String day;
    private final String dayName;
    private final String sum;

    public DayExpense(String month, String day, String dayName, String sum) {
        this.month = month;
        this.day = day;
        this.dayName = dayName;
        this.sum = sum;
    }

    public String getDay() {
        return day;
    }

    public String getDayName() {
        return dayName;
    }

    public String getMonth() {
        return month;
    }

    public String getSum() {
        return sum;
    }
}
