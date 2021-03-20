package com.example.mybudget.components.item;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.components.Charts;
import com.example.mybudget.MainActivity;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.ItemDrawer;
import com.example.mybudget.R;
import com.example.mybudget.helpers.RecyclerTouchHelper;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Enums.Action;
import com.example.mybudget.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.example.mybudget.utils.Enums.Action.*;
import static com.example.mybudget.utils.Enums.Fragment.CHARTS;
import static com.example.mybudget.utils.Enums.Level.ITEM_LVL;

public class ItemRecycler extends Fragment implements RecyclerTouchHelper.RecyclerItemTouchHelperListener, Constants {
    private final MainActivity mainActivity;
    private ItemsRecyclerAdapter parentItemAdapter;
    private LinearLayout mainLayout;
    private DataHelper dataHelper;

    public ItemRecycler(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.recycler, container, false);
        dataHelper = DataHelper.getDataHelper(requireContext());
        mainLayout = mainView.findViewById(R.id.main_recycler_layout);

        parentItemAdapter = new ItemsRecyclerAdapter(requireActivity());
        LinearLayoutManager pLinearLayoutManager = new LinearLayoutManager(requireContext());
        RecyclerView recyclerView = mainView.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(parentItemAdapter);
        recyclerView.setLayoutManager(pLinearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper.SimpleCallback parentSwiper = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(parentSwiper).attachToRecyclerView(recyclerView);
        return mainView;
    }

    public void refreshItems(Action action) {
        parentItemAdapter.notifyDataSetChanged();
        ((Charts) ViewsHelper.getViewsHelper().getFragment(CHARTS)).refreshCharts();
        if (action == ADD_ITEM)
        {
            mainActivity.moveToMainPage();
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        List<ItemDrawer> combinedItems = dataHelper.getListOfCombinedItems();
        List<Item> deletedItems = findDeletedItems(combinedItems.get(position));

        Snackbar snackbar = Snackbar.make(mainLayout, "The item was removed", Snackbar.LENGTH_LONG);
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