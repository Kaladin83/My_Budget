package com.example.mybudget.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.CategoryTarget;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.dtos.TableStatistics;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.mybudget.utils.Enums.DateFormat.*;

/**
 * Class that handles all database queries
 */

public class DataBase extends SQLiteOpenHelper{
    public static SQLiteDatabase dbInstance;
    public static final String DATABASE_NAME = "MyDBName.db";
    public final DataHelper dataHelper;
    private final Context context;

    public DataBase(DataHelper dataHelper, Context context) {
        super(context, DATABASE_NAME , null, 7);
        this.context = context;
        this.dataHelper = dataHelper;
        dbInstance = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        dbInstance = db;
                // ITEM table
        db.execSQL("CREATE TABLE ITEM (CATEGORY TEXT, AMOUNT NUMBER, DATE TEXT, PAY_DATE TEXT, CURRENCY TEXT, DESCRIPTION TEXT, " +
                        "PRIMARY KEY (CATEGORY, AMOUNT, DATE));");
        db.execSQL("CREATE INDEX ITEM_CATEGORY_IND ON ITEM (CATEGORY);");
        db.execSQL("CREATE INDEX ITEM_PAY_DATE_IND ON ITEM (PAY_DATE);");
               // COLOR table
        db.execSQL("CREATE TABLE COLOR (NAME NUMBER, PRIMARY KEY(NAME));");
               // CATEGORY Table
        db.execSQL("CREATE TABLE CATEGORY (NAME TEXT, PARENT TEXT, OTHER_NAME TEXT DEFAULT \"\"" +
                        " NOT NULL, COLOR NUMBER, ICON TEXT, PRIMARY KEY (NAME));");

        db.execSQL("CREATE TABLE TARGET (PAY_DATE TEXT, CATEGORY TEXT, SUM NUMBER, PRIMARY KEY (PAY_DATE, CATEGORY));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ITEM");
        db.execSQL("DROP TABLE IF EXISTS COLOR");
        db.execSQL("DROP TABLE IF EXISTS CATEGORY");
        db.execSQL("DROP TABLE IF EXISTS TARGET");
        onCreate(db);
    }

    public void insertItem(Item item)
    {
        Object[] data = new Object[]{item.getCategory(), item.getDate(), item.getPayDate(), item.getAmount(),
                item.getCurrency(), item.getDescription()};
        dbInstance.execSQL("INSERT INTO ITEM (CATEGORY, DATE , PAY_DATE, AMOUNT, CURRENCY, DESCRIPTION) VALUES(?,?,?,?,?,?);",  data);
    }

    public void insertColor(Integer color)
    {
        Object[] data = new Object[]{color};
        dbInstance.execSQL("INSERT INTO COLOR (NAME) VALUES(?);",  data);
    }

    public void insertCategory(Category category, String iconName)
    {

        Object[] data = new Object[]{category.getName(), category.getParent(), category.getColor(), iconName};
        dbInstance.execSQL("INSERT INTO CATEGORY (NAME, PARENT, COLOR, ICON) VALUES(?,?,?,?);",  data);
        fetchCategories();
    }

    public void deleteAllMonthlyItems(String month)
    {
        Object[] data = new Object[]{month};
        dbInstance.execSQL("DELETE FROM ITEM WHERE PAY_DATE = ?",  data);
    }

    public void deleteItem(Item item)
    {
        Object[] data = new Object[]{item.getDate(), item.getCategory(), item.getAmount()};
        dbInstance.execSQL("DELETE FROM ITEM WHERE DATE = ? AND CATEGORY = ? AND AMOUNT = ?",  data);
    }

    public void deleteCategory(Category category)
    {
        Object[] data = new Object[]{category.getName()};
        dbInstance.execSQL("DELETE FROM CATEGORY WHERE NAME = ?",  data);
        fetchCategories();
    }

    public void deleteColor(int color)
    {
        Object[] data = new Object[]{color};
        dbInstance.execSQL("DELETE FROM COLOR WHERE NAME = ?",  data);
    }

    public void clearAllColors()
    {
        dbInstance.execSQL("DELETE FROM COLOR");
    }

    public void clearAllCategories()
    {
        dbInstance.execSQL("DELETE FROM CATEGORY");
    }

    public void updateCategoryColor(String categoryName, int color)
    {
        Object[] data = new Object[]{color, categoryName};
        dbInstance.execSQL("UPDATE CATEGORY SET COLOR = ? WHERE NAME = ?",  data);
    }

    public void updateDefaultCategoryName(String defaultCategoryName, String categoryName) {
        Object[] data = new Object[]{defaultCategoryName, categoryName};
        dbInstance.execSQL("UPDATE CATEGORY SET OTHER_NAME = ? WHERE NAME = ?",  data);
    }

    public void updateChildCategoryParentName(String child, String parent) {
        Object[] data = new Object[]{parent, child};
        dbInstance.execSQL("UPDATE CATEGORY SET PARENT = ? WHERE NAME = ?",  data);
    }

    public void updateItemsCategory(String newName, String oldName)
    {
        Object[] data = new Object[]{newName, oldName};
        dbInstance.execSQL("UPDATE ITEM SET CATEGORY = ? WHERE CATEGORY = ?",  data);
    }

    public void updateItemDescription(Item item) {
        Object[] data = new Object[]{item.getDescription(), item.getCategory(), item.getAmount(), item.getDate()};
        dbInstance.execSQL("UPDATE ITEM SET DESCRIPTION = ? WHERE CATEGORY = ? AND AMOUNT = ? AND DATE = ?",  data);
    }

    public void fetchCategories()
    {
        List<Category> list = new ArrayList<>();
        Cursor res =  dbInstance.rawQuery( "SELECT NAME, PARENT, OTHER_NAME, COLOR, ICON FROM CATEGORY", null);
        res.moveToFirst();

        int i =0;
        while(!res.isAfterLast()){
            String name = res.getString(res.getColumnIndex("NAME"));
            String parent = res.getString(res.getColumnIndex("PARENT"));
            String otherName = res.getString(res.getColumnIndex("OTHER_NAME"));
            String iconName = res.getString(res.getColumnIndex("ICON"));
            int color = res.getInt(res.getColumnIndex("COLOR"));

            Category category = new Category(name, parent, color);
            category.setIcon(iconName == null? null: context.getResources().getIdentifier(iconName, "drawable",
                    context.getPackageName()));
            category.setOtherName(otherName);
            category.setId(i);
            list.add(category);
            res.moveToNext();
            i++;
        }
        res.close();
        dataHelper.setListOfCategories(list);
    }

    public void fetchAllColors()
    {
        List<Integer> list = new ArrayList<>();
        Cursor res =  dbInstance.rawQuery( "SELECT NAME FROM COLOR", null);
        res.moveToFirst();

        while(!res.isAfterLast()){
            list.add(res.getInt(res.getColumnIndex("NAME")));
            res.moveToNext();
        }
        res.close();
        dataHelper.setListOfColors(list);
    }

    public List<Item> getAllItems(String payDate)
    {
        List<Item> list = new ArrayList<>();
        Cursor res =  dbInstance.rawQuery( "SELECT CATEGORY, AMOUNT, DATE, DESCRIPTION, CURRENCY FROM ITEM WHERE PAY_DATE = ?",
                new String[]{payDate});
        res.moveToFirst();

        while(!res.isAfterLast()){
            String category = res.getString(res.getColumnIndex("CATEGORY"));
            double amount = res.getDouble(res.getColumnIndex("AMOUNT"));
            String date = res.getString(res.getColumnIndex("DATE"));
            String description = res.getString(res.getColumnIndex("DESCRIPTION"));
            String currency = res.getString(res.getColumnIndex("CURRENCY"));
            Item item = new Item.Builder(category, date, payDate)
                    .withAmount(amount).withDescription(description).withCurrency(currency)
                    .build();
            list.add(item);
            res.moveToNext();
        }
        res.close();

        return list;
    }

    public List<TableStatistics> getAllItemsStatistics()
    {
        List<TableStatistics> list = new ArrayList<>();
        Cursor res = dbInstance.rawQuery("SELECT SUM(AMOUNT) AS SUM, AVG(AMOUNT) AS AVG, MIN(AMOUNT) AS MIN, MAX(AMOUNT) AS " +
                "MAX, COUNT(AMOUNT) AS CNT, PAY_DATE, CATEGORY FROM ITEM GROUP BY PAY_DATE, CATEGORY", null);
        res.moveToFirst();

        while(!res.isAfterLast()){
            double sum = res.getDouble(res.getColumnIndex("SUM"));
            double avg = res.getDouble(res.getColumnIndex("AVG"));
            double min = res.getDouble(res.getColumnIndex("MIN"));
            double max = res.getDouble(res.getColumnIndex("MAX"));
            int cnt = res.getInt(res.getColumnIndex("CNT"));
            String date = res.getString(res.getColumnIndex("PAY_DATE"));
            String category = res.getString(res.getColumnIndex("CATEGORY"));
            list.add(new TableStatistics(date, category, min, max, avg, sum, cnt));
            res.moveToNext();
        }
        res.close();

        return list;
    }

    public List<TableStatistics> getItemsStatistics(String payDate)
    {
        List<TableStatistics> list = new ArrayList<>();
        Cursor res = dbInstance.rawQuery("SELECT SUM(AMOUNT) AS SUM, AVG(AMOUNT) AS AVG, MIN(AMOUNT) AS MIN, MAX(AMOUNT) AS " +
                "MAX, COUNT(AMOUNT) AS CNT, PAY_DATE, CATEGORY FROM ITEM WHERE PAY_DATE = ? GROUP BY CATEGORY", new String[]{payDate});
        res.moveToFirst();

        while(!res.isAfterLast()){
            double sum = res.getDouble(res.getColumnIndex("SUM"));
            double avg = res.getDouble(res.getColumnIndex("AVG"));
            double min = res.getDouble(res.getColumnIndex("MIN"));
            double max = res.getDouble(res.getColumnIndex("MAX"));
            int cnt = res.getInt(res.getColumnIndex("CNT"));
            String date = res.getString(res.getColumnIndex("PAY_DATE"));
            String category = res.getString(res.getColumnIndex("CATEGORY"));
            list.add(new TableStatistics(date, category, min, max, avg, sum, cnt));
            res.moveToNext();
        }
        res.close();

        return list;
    }

    public void fetchAndPopulateAllItems(String payDate)
    {
        dataHelper.setListOfItems(getAllItems(payDate));
    }

    public void insertTarget(String categoryName, Integer target) {
        Object[] data = new Object[]{Utils.getCurrentDate(PAY), categoryName, target};
        dbInstance.execSQL("INSERT INTO TARGET (PAY_DATE, CATEGORY, SUM) VALUES(?,?,?);",  data);
    }

    public void updateTarget(String categoryName, Integer target, String payDay) {
        Object[] data = new Object[]{payDay, categoryName, target};
        dbInstance.execSQL("UPDATE TARGET SET SUM = ? WHERE CATEGORY = ? AND PAY_DATE = ?",  data);
    }

    public void fetchTargets()
    {
        List<String> categories = new ArrayList<>();
        Cursor res = dbInstance.rawQuery("SELECT DISTINCT CATEGORY FROM TARGET", null);
        res.moveToFirst();

        while(!res.isAfterLast()){
            String category = res.getString(res.getColumnIndex("CATEGORY"));
            categories.add(category);
            res.moveToNext();
        }
        res.close();
        Map<String, CategoryTarget> targets = categories.stream()
                .map(this::fetchTargetForCategory)
                .collect(Collectors.toMap(pair -> pair.first, pair -> pair.second));
        dataHelper.setCategoryTargets(targets);
    }

    private Pair<String, CategoryTarget> fetchTargetForCategory(String category) {
        Pair<String, CategoryTarget> pair = new Pair<>(category, null);
        Cursor res = dbInstance.rawQuery("SELECT SUM, MAX(PAY_DATE) AS PAY_DATE FROM TARGET WHERE CATEGORY = ?",
                new String[]{category});
        res.moveToFirst();

        if (!res.isAfterLast())
        {
            int sum = res.getInt(res.getColumnIndex("SUM"));
            String payDate = res.getString(res.getColumnIndex("PAY_DATE"));
            pair = new Pair<>(category, new CategoryTarget(payDate, sum));
            res.close();
        }
        return pair;
    }

    public void deleteTarget() {
        dbInstance.execSQL("DELETE FROM TARGET");
    }
}
