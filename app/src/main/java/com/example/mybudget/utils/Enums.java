package com.example.mybudget.utils;

import com.example.mybudget.R;

import java.util.Arrays;

public class Enums {

    public enum Mode {
        NIGHT_MODE, DAY_MODE
    }

    public enum Fragment {
        EDIT, MAIN_RECYCLER, CATEGORY_PICKER, CHARTS, COLOR_PICKER, FAVOURITE_COLORS
    }

    public enum Action {
        DELETE_CATEGORY, ADD_CATEGORY, RESTORE_CATEGORY, UPDATE_CATEGORY, ADD_ITEM, REMOVE_ITEM, UPDATE_STATISTICS,
        INSERT_STATISTICS
    }

    public enum DateFormat {
        TIMESTAMP("yyyy/MM/dd HH:mm:ss"),
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

    public enum Id {
        NEW_CATEGORY_RADIO(R.id.new_category_radio),
        EXISTING_CATEGORY_RADIO(R.id.existing_category_radio),
        DIALOG_CANCEL_BUTTON(R.id.dialog_cancel_button),
        DIALOG_OK_BUTTON(R.id.dialog_ok_button),
        MONTHLY_EXPENSES_TXT(R.id.monthly_expenses_txt),
        ALL_EXPENSES_TXT(R.id.all_expenses_txt),
        CATEGORY_RADIO(R.id.category_radio),
        SUBCATEGORY_RADIO(R.id.subcategory_radio),
        ADD_CATEGORY_BUTTON(R.id.add_category_btn) ,
        UPDATE_CATEGORY_COLOR(R.id.update_btn),
        SEEK_BAR_RED(R.id.red_bar),
        SEEK_BAR_GREEN(R.id.green_bar),
        SEEK_BAR_BLUE(R.id.blue_bar);

        public int value;

        public static Id getId(int givenId) {
            return Arrays.stream(values())
                    .filter(id -> id.value == givenId)
                    .findFirst().orElse(null);
        }

        Id(int id) {
            this.value = id;
        }
    }
}
