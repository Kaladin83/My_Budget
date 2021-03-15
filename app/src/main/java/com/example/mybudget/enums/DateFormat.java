package com.example.mybudget.enums;

public enum DateFormat {
    TIMESTAMP("yyyy/MM/dd HH:mm:ss"),
    DATE("yyyy/MM/dd"),
    PAY("yyyy/MM");

    public final String format;

    DateFormat(String format){
        this.format = format;
    }
}
