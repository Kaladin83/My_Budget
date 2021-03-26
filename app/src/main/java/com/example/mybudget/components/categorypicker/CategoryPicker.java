package com.example.mybudget.components.categorypicker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mybudget.components.Edit;
import com.example.mybudget.databinding.CategoryPickerBinding;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.components.colorpicker.ColorPicker;
import com.example.mybudget.components.colorpicker.FavouriteColors;
import com.example.mybudget.components.item.ItemRecycler;
import com.example.mybudget.helpers.RecyclerTouchHelper;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.mybudget.utils.Enums.*;
import static com.example.mybudget.utils.Enums.Action.*;
import static com.example.mybudget.utils.Enums.Action.ADD_CATEGORY;
import static com.example.mybudget.utils.Enums.Action.DELETE_CATEGORY;
import static com.example.mybudget.utils.Enums.Fragment.EDIT;
import static com.example.mybudget.utils.Enums.Fragment.MAIN_RECYCLER;
import static com.example.mybudget.utils.Enums.Fragment.COLOR_PICKER;
import static com.example.mybudget.utils.Enums.Fragment.FAVOURITE_COLORS;

/**
 * Class that gives to user the tools to add new category, and manage them.
 */
public class CategoryPicker extends Fragment implements
        View.OnClickListener, RecyclerTouchHelper.RecyclerItemTouchHelperListener, View.OnTouchListener, Constants {
    private CategoryPickerBinding bind;
    private int newSelectedColor = 0;
    private DataHelper dataHelper;
    private ViewsHelper viewsHelper;
    private Activity activity;
    private ItemRecycler itemRecycler;
    private CategoryRecyclerAdapter categoryAdapter;
    private CategoryPicker categoryPicker;
    private FavouriteColors favoriteColors;
    private ManageCategoryDialog manageCategoryDialog;
    private int strokeColor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = CategoryPickerBinding.inflate(inflater, container, false);
        View mainView = bind.getRoot();
        activity = requireActivity();
        strokeColor = Utils.getThemeStrokeColor(activity);
        viewsHelper = ViewsHelper.getViewsHelper();
        dataHelper = DataHelper.getDataHelper(getContext());
        itemRecycler = ((ItemRecycler) viewsHelper.getFragment(MAIN_RECYCLER));
        categoryPicker = new CategoryPicker();
        categoryPicker = this;

        assignFields();

        return mainView;
    }

    private void assignFields() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        categoryAdapter = new CategoryRecyclerAdapter(this);
        bind.categoryPickerLayout.setOnTouchListener(this);
        bind.updateLayout.setBackground(Utils.createBorder(10, Color.TRANSPARENT, 2, strokeColor));
        bind.addCategoryBtn.setOnClickListener(this);
        bind.updateBtn.setOnClickListener(this);
        bind.categoryEdit.clearFocus();
        bind.newColorTxt.setBackground(Utils.createBorder(15, Color.TRANSPARENT, 1, strokeColor));
        bind.currentColorTxt.setBackground(Utils.createBorder(15, Color.TRANSPARENT, 1, strokeColor));
        bind.categoryRecyclerView.setAdapter(categoryAdapter);
        bind.categoryRecyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(bind.categoryRecyclerView);

        createTabs();
        refreshCategories();
    }

    private void createTabs() {
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager(), getLifecycle());
        bind.tabLayout.addTab(bind.tabLayout.newTab());
        bind.tabLayout.addTab(bind.tabLayout.newTab());
        bind.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        bind.pager.setAdapter(adapter);

        new TabLayoutMediator(bind.tabLayout, bind.pager, (tab, position) ->
                tab.setText(position == 0 ? "Color Picker" : "Favourites")).attach();
    }

    public void refreshCategories() {
        if (categoryAdapter != null)
        {
            categoryAdapter.notifyDataSetChanged();
            ((Edit) viewsHelper.getFragment(EDIT)).notifyDataChanged();
        }
    }

    private void addCategory() {
        String categoryName = bind.categoryEdit.getText().toString();
        boolean isCategoryExists = dataHelper.getListOfCategories().stream().anyMatch(cat -> cat.getName().equals(categoryName));
        String message = isCategoryExists ? "The category already in the list" : categoryName.length() == 0 ?
                "The category name must not be blank" : "";
        if (!message.equals(""))
        {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            return;
        }

        manageCategoryDialog = new ManageCategoryDialog(activity, ADD_CATEGORY) {
            @Override
            public void okButtonPressed(String categoryName) {
                int color = Utils.findColor(categoryName);
                Category category = new Category(bind.categoryEdit.getText().toString(), categoryName, color);

                dataHelper.addCategory(category);
                bind.categoryEdit.setText("");
                Utils.closeKeyboard(bind.categoryEdit, activity);

                Toast.makeText(categoryPicker.getContext(), "The category: " + category.getName() + " was added",
                        Toast.LENGTH_SHORT).show();
                refreshCategories();
                manageCategoryDialog.dismiss();
            }
        };
        manageCategoryDialog.show();
    }

    private void updateCategoryColor() {
        int selectedPosition = categoryAdapter.getSelectedPosition();
        if (selectedPosition == -1)
        {
            Toast.makeText(getContext(), "A category must be selected", Toast.LENGTH_LONG).show();
            return;
        }

        Category category = dataHelper.getListOfCategories().get(selectedPosition);
        bind.currentColorTxt.setBackgroundColor(newSelectedColor);
        dataHelper.changeCategoryColor(category, newSelectedColor);

        Toast.makeText(getContext(), "The color of " + category.getName() + " was changed", Toast.LENGTH_SHORT).show();
        refreshCategories();
        itemRecycler.refreshItems(UPDATE_CATEGORY);
    }

    @Override
    public void onClick(View view) {
        switch (Id.getId(view.getId()))
        {
            case ADD_CATEGORY_BUTTON:
                addCategory();
                break;
            case UPDATE_CATEGORY_COLOR:
                updateCategoryColor();
                break;
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CategoryRecyclerAdapter.MyViewHolder)
        {
            Category categoryToDelete = dataHelper.getListOfCategories().get(position);
            List<Item> itemsOfCategory = dataHelper.getListOfItems(item -> item.getCategory().equals(categoryToDelete.getName()));
            if (!itemsOfCategory.isEmpty())
            {
                manageCategoryDialog = new ManageCategoryDialog(activity, DELETE_CATEGORY) {
                    @Override
                    public void okButtonPressed(String newCategoryName) {
                        dataHelper.changeCategoryName(categoryToDelete, newCategoryName);
                        Toast.makeText(getContext(), categoryToDelete.getName() + " was renamed to " + newCategoryName,
                                Toast.LENGTH_SHORT).show();
                        manageCategoryDialog.dismiss();
                        refreshCategories();
                        itemRecycler.refreshItems(REMOVE_ITEM);
                    }
                };
                manageCategoryDialog.show();
            }
            else
            {
                restore(null, categoryToDelete, position, "The category was removed from list");
                categoryAdapter.removeItem(position);
                dataHelper.removeCategory(categoryToDelete);
                refreshCategories();
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Utils.closeKeyboard(bind.categoryEdit, activity);
        return false;
    }

    public void updateColorField(int color) {
        newSelectedColor = color;
        bind.newColorTxt.setBackground(Utils.createBorder(15, color, 1, strokeColor));
    }

    public void restoreCategory(Category deleteCategory, int deletedIndex) {
        categoryAdapter.restoreItem(deleteCategory, deletedIndex);
        dataHelper.addCategory(deleteCategory);
        refreshCategories();
    }

    public void restore(Integer deleteColor, Category deleteCategory, int deletedIndex, String message) {
        Snackbar snackbar = Snackbar.make(bind.categoryPickerLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", view -> {
            if (deleteColor != null)
            {
                favoriteColors.restoreColor(deleteColor, deletedIndex);
            }
            else
            {
                restoreCategory(deleteCategory, deletedIndex);
            }
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }


    public void updateColor(String category) {
        bind.currentColorTxt.setBackground(Utils.createBorder(15, Utils.findColor(category), 1, strokeColor));
        categoryAdapter.notifyDataSetChanged();
    }

    public void refreshFavouriteColors() {
        bind.pager.setCurrentItem(1);
    }

    private class PagerAdapter extends FragmentStateAdapter {
        private final Map<Integer, Fragment> fragments;

        private PagerAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
            Fragment colorPicker = new ColorPicker();
            favoriteColors = new FavouriteColors();
            ViewsHelper.getViewsHelper().registerFragment(COLOR_PICKER, colorPicker)
                    .registerFragment(FAVOURITE_COLORS, favoriteColors);
            fragments = JavaUtils.mapOf(colorPicker, favoriteColors);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return Objects.requireNonNull(fragments.get(position));
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
