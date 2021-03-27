package com.example.mybudget.components.item;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.components.Charts;
import com.example.mybudget.MainActivity;
import com.example.mybudget.databinding.RecyclerBinding;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.ItemDrawer;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.helpers.RecyclerTouchHelper;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Enums.Action;
import com.example.mybudget.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static com.example.mybudget.utils.Enums.Action.*;
import static com.example.mybudget.utils.Enums.DateFormat.*;
import static com.example.mybudget.utils.Enums.Fragment.CHARTS;
import static com.example.mybudget.utils.Enums.Level.ITEM_LVL;

public class ItemRecycler extends Fragment implements RecyclerTouchHelper.RecyclerItemTouchHelperListener{
    private RecyclerBinding bind;
    private final MainActivity mainActivity;
    private ItemsRecyclerAdapter parentItemAdapter;
    private DataHelper dataHelper;

    public ItemRecycler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = RecyclerBinding.inflate(inflater, container, false);
        View mainView = bind.getRoot();
        dataHelper = DataHelper.getDataHelper(requireContext());
        parentItemAdapter = new ItemsRecyclerAdapter(requireActivity());
        LinearLayoutManager pLinearLayoutManager = new LinearLayoutManager(requireContext());
        bind.recyclerView.setAdapter(parentItemAdapter);
        bind.recyclerView.setLayoutManager(pLinearLayoutManager);
        bind.recyclerView.setItemAnimator(new DefaultItemAnimator());

        populateTotals();

        ItemTouchHelper.SimpleCallback parentSwiper = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(parentSwiper).attachToRecyclerView(bind.recyclerView);
        return mainView;
    }

    public void refreshItems(Action action) {
        dataHelper.setInitialListOfCombinedItems();
        parentItemAdapter.notifyDataSetChanged();
        populateTotals();
        ((Charts) ViewsHelper.getViewsHelper().getFragment(CHARTS)).refreshCharts();
        if (action == ADD_ITEM)
        {
            mainActivity.moveToMainPage();
        }
    }

    private void populateTotals() {
        MonthlyStatistics monthlyStats = dataHelper.getMonthlyStatistics(Utils.getCurrentDate(PAY));
        if (monthlyStats == null)
        {
            populateTotals("", "", "");
        }
        else
        {
            Statistics stats = Objects.requireNonNull(monthlyStats.getStatistics().get(Utils.TOTAL));
            populateTotals(String.valueOf(stats.getAvg()), String.valueOf(stats.getSum()), String.valueOf(stats.getCnt()));
        }
    }

    private void populateTotals(String average, String sum, String expenses) {
        bind.averageVal.setText(average);
        bind.totalSumVal.setText(sum);
        bind.numberExpensesVal.setText(expenses);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        List<ItemDrawer> combinedItems = dataHelper.getListOfCombinedItems();
        List<Item> deletedItems = findDeletedItems(combinedItems.get(position));

        Snackbar snackbar = Snackbar.make(bind.mainRecyclerLayout, "The item was removed", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", view -> {
            dataHelper.restoreItems(deletedItems);
            refreshItems(RESTORE_CATEGORY);
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
        dataHelper.removeItems(deletedItems);
        refreshItems(DELETE_CATEGORY);
    }

    private List<Item> findDeletedItems(ItemDrawer itemToDelete) {
        if (itemToDelete.getLevel() == ITEM_LVL)
        {
            return ImmutableList.of(itemToDelete.getItem());
        }
        String itemCategory = itemToDelete.getItem().getCategory();
        List<String> listOfSubcategories = Utils.getCategoriesNames(cat -> cat.getParent().equals(itemCategory));
        listOfSubcategories.add(itemCategory);

        return dataHelper.getListOfItems(item -> listOfSubcategories.stream().anyMatch(cat -> cat.equals(item.getCategory())));
    }
}