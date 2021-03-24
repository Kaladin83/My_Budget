package com.example.mybudget.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.mybudget.R;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.dtos.MonthlyStatistics;
import com.example.mybudget.domain.dtos.Statistics;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.helpers.DataHelper;
import com.google.common.collect.ImmutableMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.example.mybudget.utils.Enums.*;
import static com.example.mybudget.utils.Enums.DateFormat.PAY;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Utilities. All functionality that is common to multiple classes
 */

public class Utils implements Constants {

    private static DataHelper dataHelper;
    public static final String TOTAL = "Total";

    public static void setApplicationContext(Context context) {

        dataHelper = DataHelper.getDataHelper(context);
    }

    public static Drawable createBorder(int radius, int color, int stroke, int strokeColor) {
        GradientDrawable gd;
        gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radius);
        gd.setStroke(stroke, strokeColor);
        return gd;
    }

    public static int getThemeStrokeColor(Activity activity) {
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorOnSecondary, typedValue, true);
        return typedValue.data;
    }
    public static int getAttrColor(Activity activity, int attrColor) {
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(attrColor, typedValue, true);
        return typedValue.data;
    }

    public static int getColor(int color, Context context){
        return ContextCompat.getColor(context, color);
    }

    public static void closeKeyboard(EditText edit, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    public static void openKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static int findColor(String category) {
        return dataHelper.getListOfCategories().stream()
                .filter(cat -> cat.getName().equals(category))
                .map(Category::getColor)
                .findFirst()
                .orElse(Color.parseColor("#FFFFFF"));
    }

    public static boolean validateTextInput(String string) {
        return string.matches("[a-zA-Z]+");
    }

    public static List<Item> sortItemsByDate(List<Item> list) {
        list.sort((v0, v1) -> v0.getDate().compareTo(v1.getDate()));
        return list;
    }

    public static List<Category> sortCategoriesByName(List<Category> list) {
        List<Category> parents = list.stream()
                .filter(c -> c.getParent().equals(""))
                .sorted((v0, v1) -> v0.getName().compareTo(v1.getName()))
                .collect(toList());
        List<Category> children = list.stream()
                .filter(c -> !c.getParent().equals(""))
                .collect(toList());

        List<Category> combineSortedList = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++)
        {
            combineSortedList.add(parents.get(i));
            for (int j = 0; j < children.size(); j++)
            {
                if (parents.get(i).getName().equals(children.get(j).getParent()))
                {
                    combineSortedList.add(children.get(j));
                }
            }
        }
        return combineSortedList;
    }

    public static String findMaxDate(String categoryName) {
        return dataHelper.getListOfItems().stream()
                .filter(item -> isCategoryInSubCategories(item.getCategory(), categoryName )|| item.getCategory().equals(categoryName))
                .map(Item::getDate)
                .max(String::compareTo)
                .orElse("");
    }

    /** For Category: Categories that don't have otherName **/
    public static Predicate<Category> getCategoryNoOtherNamePredicate() {
        return cat -> cat.getOtherName().equals("");
    }

    private static boolean isCategoryInSubCategories(String category, String parentCategory) {
        return dataHelper.getListOfCategories().stream()
                .filter(cat -> cat.getName().equals(category))
                .anyMatch(cat -> cat.getParent().equals(parentCategory));
    }

    /**
     * @param predicate
     *      true: returns Categories and Subcategories
     *      cat -> cat.getParent().equals(""):  returns only parent categories
     *      cat -> !cat.getParent().equals(""): returns only subcategories
     * @return combined items for categories or subcategories which created from statistics
     * */
    public static List<Item> getItemsFromStatistics(Predicate<Category> predicate) {
        return Utils.getCategoriesStats(predicate).keySet().stream()
                .map(cat -> new Item.Builder(cat, findMaxDate(cat), Utils.getCurrentDate(PAY)).build())
                .collect(toList());
    }

    /**
     * @param selectedCategory category to get statistics for
     * @return Statistics for given category for current date
     * */
    public static Statistics getStatsForCategory(String selectedCategory) {
        return getStatsForCategory(getCurrentDate(DateFormat.PAY), selectedCategory);
    }

    /**
     * @param date statistics to fetch for
     * @param selectedCategory category to get statistics for
     * @return Statistics for given category for current date
     * */
    public static Statistics getStatsForCategory(String date, String selectedCategory) {
        MonthlyStatistics monthlyStats = dataHelper.getMonthlyStatistics(date);
        if (monthlyStats == null || monthlyStats.getStatistics().get(selectedCategory) == null)
        {
            return new Statistics(0, 0, 0, 0, 0);
        }
        return monthlyStats.getStatistics().get(selectedCategory);
    }

    /**
     * Statistics for current date
     * @param predicate
     *          true: returns Categories and Subcategories
     *          cat -> cat.getParent().equals(""):  returns only parent categories
     *          cat -> !cat.getParent().equals(""): returns only subcategories
     * @return the statistics of categories when key: Category name, Value: Statistics of that category
     * */
    public static Map<String, Statistics> getCategoriesStats(Predicate<Category> predicate) {
        return getCategoriesStats(getCurrentDate(DateFormat.PAY), predicate);
    }

    /**
     * @param date statistics to fetch for
     * @param predicate
     *          true: returns Categories and Subcategories
     *          cat -> cat.getParent().equals(""):  returns only parent categories
     *          cat -> !cat.getParent().equals(""): returns only subcategories
     * @return the statistics of categories when key: Category name, Value: Statistics of that category
     * */
    public static Map<String, Statistics> getCategoriesStats(String date, Predicate<Category> predicate) {
        MonthlyStatistics monthlyStatistics = dataHelper.getMonthlyStatistics(date);
        if (monthlyStatistics == null)
        {
            return ImmutableMap.of();
        }
        List<String> categories = Utils.getCategoriesNames(predicate);
        return monthlyStatistics.getStatistics().entrySet().stream().filter(e -> categories.contains(e.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Gets the categories names according to predicate.
     * cat -> true : returns all the categories names (categories and sub categories)
     * cat -> parentName == "" : returns all categories' names
     * cat -> parentName != "" : returns all sub categories names
     * cat -> parentName == 'name' : returns all the children names of given father
     */
    public static List<String> getCategoriesNames(Predicate<Category> categoryPredicate) {
        return dataHelper.getListOfCategories().stream()
                .filter(categoryPredicate)
                .map(Category::getName)
                .collect(toList());
    }

    /**
     * Gets the parent category name of a given sub category.
     */
    public static String getParentCategoryName(String subCategory) {
        return dataHelper.getListOfCategories().stream()
                .filter(sub -> subCategory.equals(sub.getName()))
                .map(Category::getParent)
                .findFirst().orElse("");
    }

    public static int getScreenWidth(@NonNull Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(@NonNull Activity activity) {
        int navigationBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            navigationBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }

        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();

        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels - navigationBarHeight;
    }

    public static float getLogicalDensity(@NonNull Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return displayMetrics.density;
    }

    public static String getCurrentDate(DateFormat dateFormat) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFormat.value));
    }

    public static String getDate(DateFormat dateFormat, LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(dateFormat.value));
    }

    public static String getDBMonthFormat(String date, Map<String, String> monthsMap) {
        String month = monthsMap.entrySet().stream()
                .filter(en -> en.getValue().equals(date.substring(0, 3)))
                .map(Map.Entry::getKey)
                .findFirst().orElse("");
        return date.substring(4) + "/" + month;
    }

    public static double firstFoundWordsToNumber(String input) {
        Map<String, String> validValues = JavaUtils.mapOf("zero", "0", "one", "1", "two", "2", "three", "3", "four", "4",
                "five", "5", "six", "6", "seven", "7", "eight", "8", "nine", "9", "ten", "10", "eleven", "11", "twelve", "12",
                "thirteen", "13", "fourteen", "14", "fifteen", "15", "sixteen", "16", "seventeen", "17", "eighteen", "18",
                "nineteen", "19", "twenty", "20", "thirty", "30", "forty", "40", "fifty", "50", "sixty", "60", "seventy", "70",
                "eighty", "80", "ninety", "90", "hundred", "100", "thousand", "1000", "million", "1000000");
        String[] words = input.split(" ");
        double result = 0;

        for (String word : words)
        {
            String value = validValues.get(word.trim().toLowerCase());
            if (result > 0 && value == null)
            {
                return 0;
            }
            if (value != null)
            {
                result += Double.parseDouble(value);
            }
        }
        return result;
    }

    public static double round(double val) {
        return ((int) (val * 100)) / 100d;
    }
}