package com.example.mybudget.components.categorypicker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mybudget.components.Edit;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.R;
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
    private int newSelectedColor = 0;
    private DataHelper dataHelper;
    private ViewsHelper viewsHelper;
    private Activity activity;
    private View mainView;
    private ItemRecycler itemRecycler;
    private EditText addCategoryEdit;
    private CategoryRecyclerAdapter categoryAdapter;
    private ViewPager2 pager;
    private TextView newColorTxt, currentColorTxt;
    private ConstraintLayout mainLayout;
    private CategoryPicker categoryPicker;
    private FavouriteColors favoriteColors;
    private ManageCategoryDialog manageCategoryDialog;
    private int strokeColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.category_picker, container, false);

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
        mainLayout = mainView.findViewById(R.id.category_picker);
        mainLayout.setOnTouchListener(this);
        ConstraintLayout updateLayout = mainView.findViewById(R.id.update_layout);
        updateLayout.setBackground(Utils.createBorder(10, Color.TRANSPARENT, 2, strokeColor));
        Button addCategoryBtn = mainView.findViewById(R.id.add_category_btn);
        addCategoryBtn.setOnClickListener(this);
        Button updateBtn = mainView.findViewById(R.id.update_btn);
        updateBtn.setOnClickListener(this);

        addCategoryEdit = mainView.findViewById(R.id.category_edit);
        addCategoryEdit.clearFocus();
        newColorTxt = mainView.findViewById(R.id.new_color_txt);
        newColorTxt.setBackground(Utils.createBorder(15, Color.TRANSPARENT, 1, strokeColor));
        currentColorTxt = mainView.findViewById(R.id.current_color_txt);
        currentColorTxt.setBackground(Utils.createBorder(15, Color.TRANSPARENT, 1, strokeColor));

        categoryAdapter = new CategoryRecyclerAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView categoryRecyclerView = mainView.findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setAdapter(categoryAdapter);
        categoryRecyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(categoryRecyclerView);

        createTabs();
        refreshCategories();
    }

    private void createTabs() {
        TabLayout tabLayout = mainView.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        pager = mainView.findViewById(R.id.pager);
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager(), getLifecycle());
        pager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, pager, (tab, position) ->
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
        String categoryName = addCategoryEdit.getText().toString();
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
                Category category = new Category(addCategoryEdit.getText().toString(), categoryName, color);

                dataHelper.addCategory(category);
                addCategoryEdit.setText("");
                Utils.closeKeyboard(addCategoryEdit, activity);

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
        currentColorTxt.setBackgroundColor(newSelectedColor);
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
        Utils.closeKeyboard(addCategoryEdit, activity);
        return false;
    }

    public void updateColorField(int color) {
        newSelectedColor = color;
        newColorTxt.setBackground(Utils.createBorder(15, color, 1, strokeColor));
    }

    public void restoreCategory(Category deleteCategory, int deletedIndex) {
        categoryAdapter.restoreItem(deleteCategory, deletedIndex);
        dataHelper.addCategory(deleteCategory);
        refreshCategories();
    }

    public void restore(Integer deleteColor, Category deleteCategory, int deletedIndex, String message) {
        Snackbar snackbar = Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG);
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
        currentColorTxt.setBackground(Utils.createBorder(15, Utils.findColor(category), 1, strokeColor));
        categoryAdapter.notifyDataSetChanged();
    }

    public void refreshFavouriteColors() {
        pager.setCurrentItem(1);
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
