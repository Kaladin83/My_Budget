package com.example.mybudget.helpers;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.example.mybudget.Data.Analyzer;
import com.example.mybudget.Data.DataBase;
import com.example.mybudget.R;
import com.example.mybudget.domain.domain.AppCategoryItem;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.CategoryTarget;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.ItemDrawer;
import com.example.mybudget.domain.domain.MonthName;
import com.example.mybudget.domain.dtos.ItemToAdd;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.domain.dtos.TableStatistics;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static com.example.mybudget.utils.Enums.DateFormat.PAY;
import static com.example.mybudget.utils.Enums.DateFormat.TIMESTAMP;
import static com.example.mybudget.utils.Enums.Level.CATEGORY_LVL;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class DataHelper {
    private final DataBase dataBase;
    private final Analyzer analyzer;
    private static DataHelper dataHelper;
    private Item lastAddedItem;
    private List<Item> listOfItems = new ArrayList<>();
    private List<Category> listOfCategories = new ArrayList<>();
    private List<String> listOfMonths = new ArrayList<>();
    private List<Integer> listOfColors = new ArrayList<>();
    private List<ItemDrawer> listOfCombinedItems = new ArrayList<>();
    private List<AppCategoryItem> listOfCategoryItems = new ArrayList<>();
    private final Map<String, MonthlyStatistics> monthlyStatisticsMap = new HashMap<>();
    private Map<String, CategoryTarget> categoryTargets = new HashMap<>();
    private Map<String, MonthName> monthsMap = new HashMap<>();

    public Map<String, MonthName> getAllMonths() {
        return monthsMap;
    }

    public MonthName getMonthNames(String monthNumber) {
        return Objects.requireNonNull(monthsMap.get(monthNumber));
    }

    public List<String> getListOfMonths() {
        return listOfMonths;
    }

    public void setListOfMonths() {
        listOfMonths = new ArrayList<>(monthlyStatisticsMap.keySet());
    }

    public CategoryTarget getTargetPerCategory(String category) {
        return categoryTargets.get(category) == null ? new CategoryTarget("", 0) : categoryTargets.get(category);
    }

    public void setCategoryTargets(Map<String, CategoryTarget> categoryTargets) {
        this.categoryTargets = categoryTargets;
    }

    public List<Item> getListOfItems() {
        return listOfItems;
    }

    public List<Item> getListOfItems(Predicate<Item> predicate) {
        return listOfItems.stream().filter(predicate)
                .collect(toList());
    }

    public void setListOfItems(List<Item> list) {
        listOfItems = list;
    }

    public List<ItemDrawer> getListOfCombinedItems() {
        return listOfCombinedItems;
    }

    public void setListOfCombinedItems(List<ItemDrawer> list) {
        listOfCombinedItems = list;
    }

    public void setInitialListOfCombinedItems() {
        listOfCombinedItems = Utils.getItemsFromStatistics(cat -> cat.getParent().equals("")).stream()
                .map(item -> new ItemDrawer(item, false, false, CATEGORY_LVL))
                .collect(toList());
    }

    public void setListOfCategoryItems() {
        listOfCategoryItems = Utils.getItemsFromStatistics(cat -> true).stream()
                .map(item -> new AppCategoryItem(item.getCategory(), Utils.findColor(item.getCategory())))
                .collect(toList());
    }

    public List<AppCategoryItem> getListOfCategoryItems() {
        return listOfCategoryItems;
    }

    public List<Category> getListOfCategories() {
        return listOfCategories;
    }

    public void setListOfCategories(List<Category> listOfCategories) {
        this.listOfCategories = Utils.sortCategoriesByName(listOfCategories);
    }

    public List<Integer> getListOfColors() {
        return listOfColors;
    }

    public void setListOfColors(List<Integer> colors) {
        listOfColors = new ArrayList<>(colors);
    }

    private DataHelper(Context context) {
        this.dataBase = new DataBase(this, context);
        this.analyzer = new Analyzer();
        setAllMonths();
    }

    public static DataHelper getDataHelper(Context context) {
        if (dataHelper == null)
        {
            synchronized (DataHelper.class)
            {
                if (dataHelper == null)
                {
                    dataHelper = new DataHelper(context);
                }
            }
        }
        return dataHelper;
    }

    private void setAllMonths() {
        List<MonthName> values = ImmutableList.of(new MonthName("January", "JEN"),
                new MonthName("February", "FEB"), new MonthName("March", "MAR"),
                new MonthName("April", "APR"), new MonthName("May", "MAY"),
                new MonthName("June", "JUN"), new MonthName("July", "JUL"),
                new MonthName("August", "AUG"), new MonthName("September", "SEP"),
                new MonthName("October", "OCT"), new MonthName("November", "NOV"),
                new MonthName("December", "DEC"));
        monthsMap = JavaUtils.mapOf(ImmutableList.of("01", "02", "03", "04", "05", "06", "07", "09", "10", "11", "12"), values);
    }

    public void deleteItems(String month) {
        dataBase.deleteAllMonthlyItems(month);
        //dataBase.deleteTarget();
    }

    public void populateInitialCategories(Context context) {
        dataBase.clearAllColors();
        dataBase.insertColor(ContextCompat.getColor(context, R.color.blue_3));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.blue_4));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.red_3));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.red_5));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.green_3));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.green_4));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.yellow_3));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.yellow_2));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.purple_3));
        dataBase.insertColor(ContextCompat.getColor(context, R.color.purple_4));

        dataBase.clearAllCategories();
        dataBase.insertCategory(new Category("Home", "", ContextCompat.getColor(context, R.color.blue_3)),
                "icon_cat_home");
        dataBase.insertCategory(new Category("Food", "", ContextCompat.getColor(context, R.color.blue_4)), "icon_cat_food");
        dataBase.insertCategory(new Category("Cafes", "", ContextCompat.getColor(context, R.color.red_3)), "icon_cat_restaurant");
        dataBase.insertCategory(new Category("Clothes", "", ContextCompat.getColor(context, R.color.yellow_3)),
                "icon_cat_clothes");
        dataBase.insertCategory(new Category("Car", "", ContextCompat.getColor(context, R.color.green_4)), "icon_cat_car");
        dataBase.insertCategory(new Category("Fuel", "", ContextCompat.getColor(context, R.color.blue)), "icon_cat_fuel");
        dataBase.insertCategory(new Category("Pets", "", ContextCompat.getColor(context, R.color.yellow_2)), "icon_cat_pets");
        dataBase.insertCategory(new Category("Transportation", "", ContextCompat.getColor(context, R.color.red)),
                "icon_cat_transportation");
        dataBase.insertCategory(new Category("Vacation", "", ContextCompat.getColor(context, R.color.green)),
                "icon_cat_vacation");
        dataBase.insertCategory(new Category("Other", "", ContextCompat.getColor(context, R.color.purple_4)), null);
    }

    public void fetchData(String payDate) {
        dataBase.fetchCategories();
        dataBase.fetchAndPopulateAllItems(payDate);
        dataBase.fetchAllColors();
        dataBase.fetchTargets();
        prepareStatistics();
        setListOfCategoryItems();
        setInitialListOfCombinedItems();
    }

    /* -----------------------   Category Manipulations   ----------------------- */

    public void addCategory(Category category) {
        dataBase.insertCategory(category, null);
    }

    public void changeCategoryColor(Category category, int newColorColor) {
        dataBase.updateCategoryColor(category.getName(), newColorColor);
        dataBase.fetchCategories();
    }

    public void changeCategoryName(Category category, String newCategoryName) {
        dataBase.updateItemsCategory(Utils.toTitleCase(newCategoryName), category.getName());
        dataBase.deleteCategory(category);
        dataBase.fetchCategories();
        updateMonthlyStatistics(Utils.getCurrentDate(PAY), dataBase.getItemsStatistics(Utils.getCurrentDate(PAY)));
//        dataBase.fetchAndPopulateAllItems();
//        dataBase.fetchMonthlyStatistics(Utils.getCurrentDate(PAY));
    }

    public void removeCategory(Category category) {
        dataBase.deleteCategory(category);
    }

    /* -----------------------   Color Manipulations   ----------------------- */

    public void removeColor(Integer deletedItem) {
        dataBase.deleteColor(deletedItem);
    }

    public void addColor(Integer color) {
        dataBase.insertColor(color);
    }

    /* -----------------------   Item Manipulations   ----------------------- */

    public void addItemFromVoice(List<String> matches) {
        ItemToAdd itemToAdd = analyzer.getItemFromVoice(matches);
        if (itemToAdd != null)
        {
            addItem(itemToAdd.getDefaultCategoryName(), itemToAdd.getParentCategoryName(), itemToAdd.getGivenCategoryName(),
                    itemToAdd.getAmount(), itemToAdd.getDescription());
        }
    }

    public void addItemFromText(String category, double amount, String description) {
        ItemToAdd itemToAdd = analyzer.getItemFromEdit(category, amount, description);
        addItem(itemToAdd.getDefaultCategoryName(), itemToAdd.getParentCategoryName(), itemToAdd.getGivenCategoryName(),
                itemToAdd.getAmount(), itemToAdd.getDescription());
    }

    /**
     * Handling the add item from Edit form and through Voice
     * <p>
     * Inserting the item and if 'All parentName' exists:
     * 1. Creating 'All parentName' (if not exists) and changing the parent name of child categories with 'All parentName'
     * 2. Updating defaultName column with 'All parentName' of Category table, inserting new category and updating statistics
     * 3. Updating the list with updated data
     */
    public void addItem(String defaultCategoryName, String categoryName, String givenCategoryName, double amount, String description) {
        boolean defaultExists = listOfCategories.stream().anyMatch(cat -> cat.getName().equals(defaultCategoryName));
        if (!defaultCategoryName.equals("") && !defaultExists)
        {
            listOfCategories.stream()
                    .filter(cat -> cat.getParent().equals(categoryName))
                    .forEach(cat -> dataBase.updateChildCategoryParentName(cat.getName(), defaultCategoryName));
            Category allCat = new Category(defaultCategoryName, "", Color.WHITE);
            dataBase.insertCategory(allCat, null);
            CategoryTarget t = categoryTargets.get(categoryName);
            updateTarget(ImmutableMap.of(defaultCategoryName, t == null ? 0 : t.getSum()), Utils.getCurrentDate(PAY));
            dataBase.updateDefaultCategoryName(defaultCategoryName, defaultCategoryName);
            if (!givenCategoryName.equals(categoryName))
            {
                dataBase.updateChildCategoryParentName(givenCategoryName, defaultCategoryName);
            }
            dataBase.updateChildCategoryParentName(categoryName, defaultCategoryName);
            dataBase.fetchCategories();
        }
        String payDate = Utils.getCurrentDate(PAY);
        Item item = new Item.Builder(givenCategoryName, Utils.getCurrentDate(TIMESTAMP), payDate)
                .withAmount(amount).withDescription(description).build();
        setLastAddedItem(item);
        dataBase.insertItem(item);
        dataBase.fetchAndPopulateAllItems(item.getPayDate());
        updateMonthlyStatistics(payDate, dataBase.getItemsStatistics(payDate));
    }

    public void restoreItems(List<Item> itemsToRestore) {
        String payDate = itemsToRestore.get(0).getPayDate();
        itemsToRestore.forEach(item -> {
            dataBase.insertItem(item);
            listOfItems.add(item);
        });
        updateMonthlyStatistics(payDate, dataBase.getItemsStatistics(payDate));
    }

    public void removeItems(List<Item> itemsToDelete) {
        String payDate = itemsToDelete.get(0).getPayDate();
        itemsToDelete.forEach(item -> {
            dataBase.deleteItem(item);
            listOfItems.remove(item);
        });
        updateMonthlyStatistics(payDate, dataBase.getItemsStatistics(payDate));
    }

    public void updateDescription(Item selectedItem, String description, int position) {
        if (selectedItem.getDescription() == null || !selectedItem.getDescription().equals(description))
        {
            Item item = Item.copyItemWithDescription(selectedItem, description);
            listOfCombinedItems.get(position).setItem(item);
            dataBase.updateItemDescription(item);
            dataBase.fetchAndPopulateAllItems(item.getPayDate());
        }
    }

    public Item getLastAddedItem() {
        return lastAddedItem == null ?
                getListOfItems().stream()
                        .max((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                        .orElse(new Item()) : lastAddedItem;
    }

    public void setLastAddedItem(Item lastAddedItem) {
        this.lastAddedItem = lastAddedItem;
    }

    /* -----------------------   Statistics Manipulations   ----------------------- */
    public void prepareStatistics() {
        List<TableStatistics> statistics = dataBase.getAllItemsStatistics();
        Map<String, List<TableStatistics>> groupedByMonth = statistics.stream()
                .collect(groupingBy(TableStatistics::getPayDate));
        groupedByMonth.entrySet().forEach(entry -> updateMonthlyStatistics(entry.getKey(), entry.getValue()));
    }

    private void updateMonthlyStatistics(String date, List<TableStatistics> statistics) {
        double min = statistics.stream().mapToDouble(TableStatistics::getMin).min().orElse(0);
        double max = statistics.stream().mapToDouble(TableStatistics::getMax).max().orElse(0);
        List<String> minCategories = getCategoryNames(stat -> stat.getMin() == min, statistics);
        List<String> maxCategories = getCategoryNames(stat -> stat.getMax() == max, statistics);

        //Create a stats map with subcategories and categories without children (subcategories) only
        Map<String, Statistics> stats = statistics.stream().collect(Collectors.toMap(TableStatistics::getCategory,
                s -> new Statistics(s.getMin(), s.getMax(), Utils.round(s.getAvg()), s.getSum(), s.getCnt())));

        //Add total for all categories to stats map
        int count = (int) getSum(stats, Statistics::getCnt);
        double sum = getSum(stats, Statistics::getSum);
        stats.put(Utils.TOTAL, new Statistics(min, max, Utils.round(sum / count), sum, count));

        //Add categories that have children to stats map
        addParentCategoriesToStatisticsMap(stats);

        monthlyStatisticsMap.put(date, new MonthlyStatistics(stats, minCategories, maxCategories));
        setListOfMonths();
    }

    private void addParentCategoriesToStatisticsMap(Map<String, Statistics> stats) {
        Map<String, Map<String, Statistics>> parentsAndStats = new HashMap<>();
        for (Map.Entry<String, Statistics> stat : stats.entrySet())
        {
            String parent = Utils.getParentCategoryName(stat.getKey());
            if (!parent.equals(""))
            {
                List<String> children = Utils.getCategoriesNames(cat -> cat.getParent().equals(parent));
                Map<String, Statistics> statistic = Objects.requireNonNull(parentsAndStats.getOrDefault(parent, new HashMap<>()));
                statistic.putAll(stats.entrySet().stream()
                        .filter(s -> children.contains(s.getKey()))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
                parentsAndStats.put(parent, statistic);
            }
        }

        parentsAndStats.entrySet().forEach(s -> {
            int count = (int) getSum(s.getValue(), Statistics::getCnt);
            double sum = getSum(s.getValue(), Statistics::getSum);
            stats.put(s.getKey(), new Statistics(getMin(s.getValue()), getMax(s.getValue()), Utils.round(sum / count), sum,
                    count));
        });
    }

    private double getMin(Map<String, Statistics> values) {
        return values.values().stream().mapToDouble(Statistics::getMin).min().orElse(0);
    }

    private double getMax(Map<String, Statistics> values) {
        return values.values().stream().mapToDouble(Statistics::getMax).max().orElse(0);
    }

    private double getSum(Map<String, Statistics> values, ToDoubleFunction<Statistics> func) {
        return values.values().stream().mapToDouble(func).sum();
    }

    public MonthlyStatistics getMonthlyStatistics(String date) {
        return monthlyStatisticsMap.get(date);
    }

    private List<String> getCategoryNames(Predicate<TableStatistics> predicate, List<TableStatistics> stats) {
        return stats.stream()
                .filter(predicate)
                .map(TableStatistics::getCategory)
                .collect(toList());
    }

    public void updateTarget(Map<String, Integer> targets, String payDate) {
        targets.entrySet().forEach(e -> {
            if (categoryTargets.get(e.getKey()) == null)
            {
                dataBase.insertTarget(e.getKey(), e.getValue());
            }
            else
            {
                dataBase.updateTarget(e.getKey(), e.getValue(), payDate);
            }
        });
        dataBase.fetchTargets();
    }
}
