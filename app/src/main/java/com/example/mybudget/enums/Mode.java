package com.example.mybudget.enums;

import java.util.Arrays;

public enum Mode {
    NIGHT_MODE ("Night Mode"),
    DAY_MODE("Day Mode");

    public String value;

    public static Mode getEnumValueByString(String val) {
        return Arrays.stream(Mode.values())
                .filter(s -> val.equals(s.value))
                .findFirst()
                .orElse(Mode.NIGHT_MODE);
    }

    Mode (String value){
        this.value = value;
    }
}
