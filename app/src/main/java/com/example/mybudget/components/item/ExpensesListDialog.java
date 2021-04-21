package com.example.mybudget.components.item;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.IAction;
import com.example.mybudget.MainActivity;
import com.example.mybudget.databinding.ExpensesListDialogBinding;
import com.example.mybudget.domain.domain.DayExpense;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.helpers.RecyclerTouchHelper;
import com.example.mybudget.utils.Enums;
import com.example.mybudget.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static androidx.fragment.app.DialogFragment.STYLE_NO_FRAME;
import static com.example.mybudget.utils.Enums.Action.ADD_ITEM;
import static com.example.mybudget.utils.Enums.Action.DELETE_CATEGORY;
import static com.example.mybudget.utils.Enums.Action.DELETE_ITEM;
import static com.example.mybudget.utils.Enums.Action.RESTORE_CATEGORY;
import static com.example.mybudget.utils.Enums.Action.RESTORE_ITEM;
import static com.example.mybudget.utils.Enums.DateFormat.PAY;


/**
 * Class creates dialog of category delete confirmation.
 * Gives the option to move items to existing category or to move them to new one.
 */
public class ExpensesListDialog extends Dialog implements IAction {
    private final MainActivity activity;
    private ExpensesListDialogBinding bind;
    private final String categoryName;
    private ExpensesListAdapter expensesAdapter;
    private DataHelper dataHelper;
    private List<Object> items;
    private List<Item> deletedItems;
    private StatisticsBuilder statBuilder;
    private IAction iAction;

    public ExpensesListDialog(MainActivity mainActivity, String categoryName) {
        super(mainActivity);
        this.activity = mainActivity;
        this.categoryName = categoryName;
        this.dataHelper = DataHelper.getDataHelper(activity);
        setCanceledOnTouchOutside(false);
        iAction = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ExpensesListDialogBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(bind.getRoot());
        items = getExpensesListWithStatsPerDay(categoryName);

        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Utils.getScreenHeight(activity) * 0.90));
        window.setGravity(Gravity.CENTER);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        expensesAdapter = new ExpensesListAdapter(activity, items);

        bind.expensesRecycler.setAdapter(expensesAdapter);
        bind.expensesRecycler.setLayoutManager(linearLayoutManager);
        bind.expensesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int state) {
                super.onScrollStateChanged(recyclerView, state);

                if (state == RecyclerView.SCROLL_STATE_DRAGGING)
                {
                    Utils.hideKeyboard(activity, recyclerView);
                }
            }
        });
        bind.backButton.setOnClickListener(v -> dismiss());

        new ItemTouchHelper(new RecyclerTouchHelper(0, ItemTouchHelper.LEFT,
                (viewHolder, direction, position) -> onSwipe(position))).attachToRecyclerView(bind.expensesRecycler);

        MonthlyStatistics statistics = DataHelper.getDataHelper(activity).getMonthlyStatistics(Utils.getCurrentDate(PAY));
        statBuilder = new StatisticsBuilder(activity, bind.getRoot(), statistics.getStatistics().get(categoryName), categoryName);
    }

    private void onSwipe(int position) {
        deletedItems = findDeletedItems(items.get(position));
        Utils.deleteItems(deletedItems, getWindow(), activity.getRoot(), iAction);
    }

    private List<Object> getExpensesListWithStatsPerDay(String categoryName) {
        Map<String, List<Item>> itemsPerPayDate = getItemsPerDate(categoryName);

        List<Object> listToReturn = new ArrayList<>();
        itemsPerPayDate.entrySet().stream().sorted((e0, e1) -> e1.getKey().compareTo(e0.getKey())).forEach(e ->{
            listToReturn.add(getDayExpense(e.getKey(), e.getValue()));
            listToReturn.addAll(e.getValue());
        });
        return listToReturn;
    }

    private Map<String, List<Item>> getItemsPerDate(String categoryName){
        List<Item> expenses = dataHelper.getListOfItems(item -> item.getCategory().equals(categoryName));

        return expenses.stream()
                .sorted((v0, v1) -> v1.getDate().compareTo(v0.getDate()))
                .collect(Collectors.groupingBy(item -> item.getDate().substring(0, 10)));
    }

    private Object getDayExpense(String date, List<Item> items) {
        double sum = items.stream().mapToDouble(Item::getAmount).sum();
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8);

        LocalDate dateOf = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        String dayOfWeek = dateOf.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CANADA);
        return new DayExpense(dataHelper.getMonthNames(month).getFullName(), day, dayOfWeek, String.valueOf(sum));
    }

    private List<Item> findDeletedItems(Object item) {
        if (item instanceof Item)
        {
            return ImmutableList.of((Item) item);
        }

        String monthDay = Utils.monthNameToMonth(((DayExpense) item).getMonth()) + "/" + ((DayExpense) item).getDay();
        return getItemsPerDate(categoryName).entrySet().stream()
                .filter(e -> e.getKey().substring(5, 10).equals(monthDay))
                .map(Map.Entry::getValue)
                .findFirst().orElse(ImmutableList.of());
    }

    @Override
    public void refreshItems(Enums.Action action) {
        expensesAdapter.updateData(getExpensesListWithStatsPerDay(categoryName));
        expensesAdapter.notifyDataSetChanged();
        MonthlyStatistics statistics = DataHelper.getDataHelper(activity).getMonthlyStatistics(Utils.getCurrentDate(PAY));
        statBuilder.populateStatistics(statistics.getStatistics().get(categoryName));
        if (action == ADD_ITEM)
        {
            activity.moveToMainPage();
        }
    }
}
