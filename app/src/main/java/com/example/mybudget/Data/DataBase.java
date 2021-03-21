package com.example.mybudget.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles all database queries
 */

public class DataBase extends SQLiteOpenHelper implements Constants {
    public static SQLiteDatabase dbInstance;
    public static final String DATABASE_NAME = "MyDBName.db";
    public final DataHelper dataHelper;

    public DataBase(DataHelper dataHelper, Context context) {
        super(context, DATABASE_NAME , null, 5);
        dbInstance = this.getReadableDatabase();
        this.dataHelper = dataHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        dbInstance = db;
                // ITEM table
        db.execSQL(
                "CREATE TABLE ITEM (CATEGORY TEXT, AMOUNT NUMBER, DATE TEXT, PAY_DATE TEXT, CURRENCY TEXT, DESCRIPTION TEXT, " +
                        "PRIMARY KEY (CATEGORY, AMOUNT, DATE));");
        db.execSQL("CREATE INDEX ITEM_CATEGORY_IND ON ITEM (CATEGORY);");
        db.execSQL("CREATE INDEX ITEM_PAY_DATE_IND ON ITEM (PAY_DATE);");
                // STATISTICS table
        db.execSQL(
                "CREATE TABLE STATISTICS (CATEGORY TEXT, SUM NUMBER, PAY_DATE TEXT, " +
                        "PRIMARY KEY (PAY_DATE, CATEGORY));");
        db.execSQL("CREATE INDEX STAT_CATEGORY_IND ON ITEM (CATEGORY);");
        db.execSQL("CREATE INDEX STAT_PAY_DATE_IND ON ITEM (PAY_DATE);");
               // COLOR table
        db.execSQL(
                "CREATE TABLE COLOR (NAME NUMBER, PRIMARY KEY(NAME));");
               // CATEGORY Table
        db.execSQL(
                "CREATE TABLE CATEGORY (NAME TEXT, PARENT TEXT, OTHER_NAME TEXT DEFAULT \"\" NOT NULL, COLOR NUMBER, PRIMARY KEY (NAME));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ITEM");
        db.execSQL("DROP TABLE IF EXISTS STATISTICS");
        db.execSQL("DROP TABLE IF EXISTS COLOR");
        db.execSQL("DROP TABLE IF EXISTS CATEGORY");
        onCreate(db);
    }

    public void insertItem(Item item)
    {
        Object[] data = new Object[]{item.getCategory(), item.getDate(), item.getPayDate(), item.getAmount(), item.getCurrency()};
        dbInstance.execSQL("INSERT INTO ITEM (CATEGORY, DATE , PAY_DATE, AMOUNT, CURRENCY, DESCRIPTION) VALUES(?,?,?,?,?,?);",  data);
    }

    public void insertStatistics(Statistics stat)
    {
        Object[] data = new Object[]{stat.getCategory(), stat.getPayDate(), stat.getSum()};
        dbInstance.execSQL("INSERT INTO STATISTICS (CATEGORY, PAY_DATE, SUM) VALUES(?,?,?);",  data);
    }

    public void insertColor(Integer color)
    {
        Object[] data = new Object[]{color};
        dbInstance.execSQL("INSERT INTO COLOR (NAME) VALUES(?);",  data);
    }

    public void insertCategory(Category category)
    {
        Object[] data = new Object[]{category.getName(), category.getParent(), category.getColor()};
        dbInstance.execSQL("INSERT INTO CATEGORY (NAME, PARENT, COLOR) VALUES(?,?,?);",  data);
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

    public void deleteStatistics(String payDate, String category)
    {
        Object[] data = new Object[]{payDate, category};
        dbInstance.execSQL("DELETE FROM STATISTICS WHERE PAY_DATE = ? AND CATEGORY = ?",  data);

    }

    public void deleteMonthlyStatistics(int payDate)
    {
        Object[] data = new Object[]{payDate};
        dbInstance.execSQL("DELETE FROM STATISTICS WHERE PAY_DATE = ?",  data);
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

    public void clearAllStatistics()
    {
        dbInstance.execSQL("DELETE FROM STATISTICS");
    }

    public void clearAllColors()
    {
        dbInstance.execSQL("DELETE FROM COLOR");
    }

    public void clearAllCategories()
    {
        dbInstance.execSQL("DELETE FROM CATEGORY");
    }

    public void updateCategoryColor(Category category)
    {
        Object[] data = new Object[]{category.getColor(), category.getName()};
        dbInstance.execSQL("UPDATE CATEGORY SET COLOR = ? WHERE NAME = ?",  data);
    }

    public void updateOtherCategoryName(String otherCategoryName, String categoryName) {
        Object[] data = new Object[]{otherCategoryName, categoryName};
        dbInstance.execSQL("UPDATE CATEGORY SET OTHER_NAME = ? WHERE NAME = ?",  data);
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

    public void updateStatistics(Statistics stats)
    {
        if (stats.getSum() == 0)
        {
            deleteStatistics(stats.getPayDate(), stats.getCategory());
        }
        else
        {
            updateStatistics(stats.getPayDate(), stats.getSum(), stats.getCategory());
        }
    }

    private void updateStatistics(String payDate, double sum, String category) {
        Object[] data = new Object[]{sum, category, payDate};
        dbInstance.execSQL("UPDATE STATISTICS SET SUM = ? WHERE CATEGORY = ? AND PAY_DATE = ?",  data);
    }

    public void fetchCategories()
    {
        List<Category> list = new ArrayList<>();
        Cursor res =  dbInstance.rawQuery( "SELECT NAME, PARENT, OTHER_NAME, COLOR FROM CATEGORY", null);
        res.moveToFirst();

        while(!res.isAfterLast()){
            String name = res.getString(res.getColumnIndex("NAME"));
            String parent = res.getString(res.getColumnIndex("PARENT"));
            String otherName = res.getString(res.getColumnIndex("OTHER_NAME"));
            int color = res.getInt(res.getColumnIndex("COLOR"));
            Category category = new Category(name, parent, color);
            category.setOtherName(otherName);
            list.add(category);
            res.moveToNext();
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

    public void fetchAllMonthsFromStatistics()
    {
        List<String> list = new ArrayList<>();
        Cursor res =  dbInstance.rawQuery( "SELECT DISTINCT PAY_DATE FROM STATISTICS ORDER BY PAY_DATE DESC", null);
        res.moveToFirst();

        while(!res.isAfterLast()){
            list.add(res.getString(res.getColumnIndex("PAY_DATE")));
            res.moveToNext();
        }
        res.close();
        dataHelper.setListOfMonths(list);
    }

    public void fetchAllItems(String payDate)
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
        dataHelper.setListOfItems(list);
    }

    public void fetchMonthlyStatistics(String payDate)
    {
        List<Statistics> list = new ArrayList<>();
        dbInstance = this.getReadableDatabase();
        Cursor res =  dbInstance.rawQuery( "SELECT CATEGORY, SUM FROM STATISTICS WHERE PAY_DATE = ?",
                new String[]{payDate});
        res.moveToFirst();

        while(!res.isAfterLast()){
            String category = res.getString(res.getColumnIndex("CATEGORY"));
            double sum = res.getDouble(res.getColumnIndex("SUM"));
            list.add(new Statistics(payDate, sum, category));
            res.moveToNext();
        }
        res.close();
        dataHelper.populateMonthlyStatistics(list);
    }

}
