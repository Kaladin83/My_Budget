package com.example.mybudget.utils;

public class Enums {

    public enum Mode {
        NIGHT_MODE, DAY_MODE;
    }

    public enum Fragment {
        EDIT, MAIN_RECYCLER, CATEGORY_PICKER, CHARTS, COLOR_PICKER, FAVOURITE_COLORS;
    }

    public enum Action {
        DELETE_CATEGORY, ADD_CATEGORY, RESTORE_CATEGORY, UPDATE_CATEGORY, ADD_ITEM, REMOVE_ITEM, UPDATE_STATISTICS,
        INSERT_STATISTICS;
    }

    public enum DateFormat {
        TIMESTAMP("yyyy/MM/dd HH:mm:ss"),
        DATE("yyyy/MM/dd"),
        PAY("yyyy/MM");

        final String value;

        DateFormat(String format) {
            this.value = format;
        }
    }

    public enum Level {
        CATEGORY_LVL(1),
        SUB_CATEGORY_LVL(2),
        ITEM_LVL(3);

        public final int value;

        Level(int level) {
            this.value = level;
        }
    }
}
