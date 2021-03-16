package com.example.mybudget.helpers;

import android.content.Context;
import android.graphics.Color;

import com.example.mybudget.Data.Analyzer;
import com.example.mybudget.Data.DataBase;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.ItemDrawer;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.domain.dtos.ItemToAdd;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.utils.Enums.Action;
import com.example.mybudget.utils.Utils;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.example.mybudget.utils.Enums.DateFormat.PAY;
import static com.example.mybudget.utils.Enums.DateFormat.TIMESTAMP;
import static com.example.mybudget.utils.Enums.Level.CATEGORY_LVL;
import static java.util.stream.Collectors.toList;


public class DataHelper implements Constants {

    private final DataBase dataBase;
    private final Analyzer analyzer;

    private static DataHelper dataHelper;

    private Item lastAddedItem;

    private List<Item> listOfItems = new ArrayList<>();
    private List<Item> listOfStatisticItems = new ArrayList<>();
    private List<Category> listOfCategories = new ArrayList<>();
    private List<Statistics> listOfStatistics = new ArrayList<>();
    private List<String> listOfMonths = new ArrayList<>();
    private List<Integer> listOfColors = new ArrayList<>();
    private List<ItemDrawer> listOfCombinedItems = new ArrayList<>();

    public List<String> getListOfMonths() {
        return listOfMonths;
    }

    public void setListOfMonths(List<String> list) {
        listOfMonths = list;
    }

    public List<Item> getListOfItems() {
        return listOfItems;
    }

    public List<Item> getListOfStatisticItems() {
        return listOfStatisticItems;
    }

    public void setListOfStatisticItems(List<Item> items) {
        this.listOfStatisticItems = items;
        Utils.sortItemsByDate(items);
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
        listOfCombinedItems = listOfStatisticItems.stream()
                .map(item -> new ItemDrawer(item, Color.WHITE, false, CATEGORY_LVL))
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

    public List<Statistics> getListOfStatistics() {
        return listOfStatistics;
    }

    public void setListOfStatistics(List<Statistics> list) {
        listOfStatistics = new ArrayList<>(list);
    }

    public void populateMonthlyStatistics(List<Statistics> list) {
        setListOfStatistics(list);

        list.forEach(stat -> stat.setMean(Utils.getAverage(stat.getCategory())));
        if (list.size() > 0)
        {
            setListOfStatisticItems(Utils.getParentStatisticsAsItems());
            setInitialListOfCombinedItems();
        }
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
        dataBase.clearAllStatistics();
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
        dataBase.fetchAllItems(payDate);
        dataBase.fetchAllColors();
        dataBase.fetchAllMonthsFromStatistics();
        dataBase.fetchMonthlyStatistics(payDate);
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
        dataBase.fetchAllItems(Utils.getCurrentDate(PAY));
        dataBase.fetchMonthlyStatistics(Utils.getCurrentDate(PAY));
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
            dataBase.fetchAllItems(Utils.getCurrentDate(PAY));
            dataBase.fetchCategories();
            double sum = listOfItems.stream().mapToDouble(Item::getAmount).sum();
            Statistics statistics = new Statistics(Utils.getCurrentDate(PAY), sum, otherCategoryName);
            Map<Statistics, Action> stats = ImmutableMap.of(statistics, Action.INSERT_STATISTICS);
            updateStatistics(stats);
        }

        categoryName = otherExists && givenCategoryName.equals(categoryName) ? otherCategoryName : givenCategoryName;
        Item item = new Item.Builder(categoryName, Utils.getCurrentDate(TIMESTAMP),
                Utils.getCurrentDate(PAY))
                .withAmount(amount).withDescription(description).build();
        setLastAddedItem(item);
        dataBase.insertItem(item);
        dataBase.fetchAllItems(item.getPayDate());
        updateStatistics(analyzer.calculateStatisticSums(item));
    }

    public void restoreItems(List<Item> itemsToRestore) {
        itemsToRestore.forEach(itemToRestore -> {
            dataBase.insertItem(itemToRestore);
            updateStatistics(analyzer.calculateStatisticSums(itemToRestore));
            listOfItems.add(itemToRestore);
        });
    }

    public void removeItems(List<Item> itemsToDelete) {
        itemsToDelete.forEach(item -> {
            Item itemToDelete = Item.copyItemWithAmount(item, item.getAmount() * -1);
            dataBase.deleteItem(item);
            updateStatistics(analyzer.calculateStatisticSums(itemToDelete));
            listOfItems.remove(item);
        });
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

    private void updateStatistics(Map<Statistics, Action> statistics) {
        String date = "";
        for (Map.Entry<Statistics, Action> entry : statistics.entrySet())
        {
            date = entry.getKey().getPayDate();
            if (entry.getValue().equals(Action.INSERT_STATISTICS) && entry.getKey().getSum() > 0)
            {
                dataBase.insertStatistics(entry.getKey());
            }
            else
            {
                dataBase.updateStatistics(entry.getKey());
            }
        }
        dataBase.fetchMonthlyStatistics(date);
    }
}
