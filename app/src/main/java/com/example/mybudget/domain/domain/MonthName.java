package com.example.mybudget.domain.domain;

public class MonthName {
    private final String fullName;
    private final String shortName;

    public MonthName(String fullName, String shortName)
    {
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }
}
