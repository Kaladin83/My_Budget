package com.example.mybudget.helpers;

import android.content.Context;
import android.graphics.Color;

import com.example.mybudget.Data.Analyzer;
import com.example.mybudget.Data.DataBase;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.ItemDrawer;
import com.example.mybudget.domain.dtos.ItemToAdd;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.domain.dtos.TableStatistics;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static com.example.mybudget.utils.Enums.DateFormat.PAY;
import static com.example.mybudget.utils.Enums.DateFormat.TIMESTAMP;
import static com.example.mybudget.utils.Enums.Level.CATEGORY_LVL;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;


public class DataHelper implements Constants {

    private final DataBase dataBase;
    private final Analyzer analyzer;

    private static DataHelper dataHelper;

    private Item lastAddedItem;

    private List<Item> listOfItems = new ArrayList<>();
    private List<Category> listOfCategories = new ArrayList<>();
    private List<String> listOfMonths = new ArrayList<>();
    private List<Integer> listOfColors = new ArrayList<>();
    private List<ItemDrawer> listOfCombinedItems = new ArrayList<>();
    private final Map<String, MonthlyStatistics> monthlyStatisticsMap = new HashMap<>();

    public List<String> getListOfMonths() {
        return listOfMonths;
    }

    public void setListOfMonths() {
        listOfMonths = new ArrayList<>(monthlyStatisticsMap.keySet());
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

    public void deleteItems(String month) {
        dataBase.deleteAllMonthlyItems(month);
    }

    public void populateInitialCategories() {
        dataBase.clearAllColors();
        dataBase.insertColor(BLUE_3);
        dataBase.insertColor(BLUE_4);
        dataBase.insertColor(RED_4);
        dataBase.insertColor(RED_5);
        dataBase.insertColor(GREEN_3);
        dataBase.insertColor(GREEN_4);
        dataBase.insertColor(YELLOW_3);
        dataBase.insertColor(YELLOW_4);
        dataBase.insertColor(PURPLE_3);
        dataBase.insertColor(PURPLE_4);

        dataBase.clearAllCategories();
        dataBase.insertCategory(new Category("home", "", BLUE_3));
        dataBase.insertCategory(new Category("food", "", BLUE_4));
        dataBase.insertCategory(new Category("cafes", "", RED_4));
        dataBase.insertCategory(new Category("clothes", "", YELLOW_4));
        dataBase.insertCategory(new Category("car", "", GREEN_4));
        dataBase.insertCategory(new Category("other", "", PURPLE_4));
    }

    public void fetchData(String payDate) {
        dataBase.fetchCategories();
        dataBase.fetchAndPopulateAllItems(payDate);
        dataBase.fetchAllColors();
        prepareStatistics();
        setInitialListOfCombinedItems();
        setListOfMonths();
    }

    /* -----------------------   Category Manipulations   ----------------------- */

    public void addCategory(Category category) {
        dataBase.insertCategory(category);
    }

    public void changeCategoryColor(Category category, int newColorColor) {
        category.setColor(newColorColor);
        dataBase.updateCategoryColor(category);
    }

    public void changeCategoryName(Category category, String newCategoryName) {
        dataBase.updateItemsCategory(newCategoryName, category.getName());
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
            addItem(itemToAdd.getOtherCategoryName(), itemToAdd.getParentCategoryName(), itemToAdd.getGivenCategoryName(),
                    itemToAdd.getAmount(), itemToAdd.getDescription());
        }
    }

    public void addItemFromText(String category, double amount, String description) {
        ItemToAdd itemToAdd = analyzer.getItemFromEdit(category, amount, description, listOfCategories);
        addItem(itemToAdd.getOtherCategoryName(), itemToAdd.getParentCategoryName(), itemToAdd.getGivenCategoryName(),
                itemToAdd.getAmount(), itemToAdd.getDescription());
    }

    /**
     * Handling the add item from Edit form and through Voice
     *
     * Inserting the item and if 'parentName + (other)' exists:
     *  1. Creating 'parentName + (other)' (if not exists) and copying items from parent to it
     *  2. Updating otherName column with 'parentName + (other)' of Category table, inserting new category and updating statistics
     *  3. Updating the list with updated data
     * */
    public void addItem(String otherCategoryName, String categoryName, String givenCategoryName, double amount,
                        String description) {
        boolean otherExists = dataHelper.getListOfCategories().stream()
                .anyMatch(cat -> cat.getName().equals(otherCategoryName));
        if (!otherCategoryName.equals("") && !otherExists)
        {
            Category newCat = new Category(otherCategoryName, categoryName, Color.WHITE);
            dataBase.insertCategory(newCat);
            dataBase.updateItemsCategory(otherCategoryName, categoryName);
            dataBase.updateOtherCategoryName(otherCategoryName, categoryName);
            dataBase.fetchCategories();
        }

        String payDate = Utils.getCurrentDate(PAY);
        categoryName = otherExists && givenCategoryName.equals(categoryName) ? otherCategoryName : givenCategoryName;
        Item item = new Item.Builder(categoryName, Utils.getCurrentDate(TIMESTAMP), payDate)
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
        Map<String, Statistics> stats = statistics.stream()
                .collect(toMap(TableStatistics::getCategory,
                        s -> new Statistics(s.getMin(), s.getMax(), Utils.round(s.getAvg()), s.getSum(), s.getCnt())));

        //Add total for all categories to stats map
        int count = (int) getSum(stats, Statistics::getCnt);
        double sum = getSum(stats, Statistics::getSum);
        stats.put(Utils.TOTAL, new Statistics(min, max, Utils.round(sum/count), sum, count));

        //Add categories that have children to stats map
        addParentCategoriesToStatisticsMap(stats);

        monthlyStatisticsMap.put(date ,new MonthlyStatistics(stats, minCategories, maxCategories));
    }

    private void addParentCategoriesToStatisticsMap(Map<String, Statistics> stats) {
        Map<String, Map<String, Statistics>> parentsAndStats = new HashMap<>();
        for (Map.Entry<String, Statistics> stat: stats.entrySet()){
            String parent = Utils.getParentCategoryName(stat.getKey());
            if (!parent.equals(""))
            {
                List<String> children = Utils.getCategoriesNames(cat -> cat.getParent().equals(parent));
                Map<String, Statistics> statistic = Objects.requireNonNull(parentsAndStats.getOrDefault(parent, new HashMap<>()));
                statistic.putAll(stats.entrySet().stream()
                        .filter(s-> children.contains(s.getKey()))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
                parentsAndStats.put(parent, statistic);
            }
        }

        parentsAndStats.entrySet().forEach(s -> {
            int count = (int) getSum(s.getValue(), Statistics::getCnt);
            double sum = getSum(s.getValue(), Statistics::getSum);
            stats.put(s.getKey(), new Statistics(getMin(s.getValue()), getMax(s.getValue()), Utils.round(count / sum), sum,
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
}
