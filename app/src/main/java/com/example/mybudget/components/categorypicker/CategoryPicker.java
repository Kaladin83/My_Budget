package com.example.mybudget.components.categorypicker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mybudget.Edit;
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
    int screenWidth, screenHeight, entireHeight, catRecyclerWidth, tabsWidth, addCatLayoutHeight, addCatLayoutWidth,
            editCatWidth, addCatButtonWidth, compAddCatHeight, entireHeaderHeight,
            colorWidth, arrowWidth, betweenLayoutsMargin, updateLayoutWidth, betweenColorsMargin, bigLayoutMarginWidth;
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
    private LinearLayout mainLayout;
    private LinearLayout.LayoutParams lParams;
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
        screenWidth = Utils.getScreenWidth(activity);
        screenHeight = Utils.getScreenHeight(activity);


        defineDimensions();
        assignFields();
        populateCategories();

        return mainView;
    }

    private void defineDimensions() {
        editCatWidth = screenWidth / 2;
        entireHeight = (int) (screenHeight / 2.2);
        catRecyclerWidth = (int) (screenWidth * 0.41);
        tabsWidth = (int) (screenWidth * 0.52);
        addCatLayoutHeight = (screenHeight - entireHeight) / 5;
        betweenLayoutsMargin = addCatLayoutHeight / 2;

        addCatLayoutWidth = screenWidth - 40;
        addCatButtonWidth = screenWidth / 3;
        compAddCatHeight = screenHeight / 16;
        entireHeaderHeight = screenHeight / 16;
        colorWidth = screenWidth / 8;
        arrowWidth = colorWidth / 2;
        updateLayoutWidth = (int) (addCatLayoutWidth * 0.8);
        betweenColorsMargin = (updateLayoutWidth - (2 * colorWidth + 2 * arrowWidth)) / 10;
        bigLayoutMarginWidth = (screenWidth - addCatLayoutWidth) / 2;
    }

    private void populateCategories() {

    }

    private void assignFields() {
        mainLayout = mainView.findViewById(R.id.category_picker);
        mainLayout.setOnTouchListener(this);

        lParams = new LinearLayout.LayoutParams(addCatLayoutWidth, addCatLayoutHeight);
        lParams.setMargins(bigLayoutMarginWidth, betweenLayoutsMargin, 0, betweenLayoutsMargin);
        LinearLayout addCategoryLayout = mainView.findViewById(R.id.add_category_layout);
        addCategoryLayout.setLayoutParams(lParams);
        //addCategoryLayout.setBackgroundColor(Color.GREEN);
        lParams = new LinearLayout.LayoutParams(addCatLayoutWidth, entireHeight);
        lParams.setMargins((int) (bigLayoutMarginWidth * 1.5), 0, 0, 0);
        LinearLayout entireLayout = mainView.findViewById(R.id.combo_layout);
        entireLayout.setLayoutParams(lParams);
        entireLayout.setOnTouchListener(this);
        lParams = new LinearLayout.LayoutParams(catRecyclerWidth, entireHeight - 5);
        lParams.setMargins(2, 0, 0, 0);
        LinearLayout recyclerLayout = mainView.findViewById(R.id.recycle_layout);
        //recyclerLayout.setBackground(Utils.createBorder(10, Color.TRANSPARENT, 1));
        recyclerLayout.setLayoutParams(lParams);
        lParams = new LinearLayout.LayoutParams(tabsWidth, entireHeight - 5);
        lParams.setMargins(bigLayoutMarginWidth + 5, 0, 0, 0);
        LinearLayout tabsLayout = mainView.findViewById(R.id.tabs_layout);
        tabsLayout.setLayoutParams(lParams);
        //tabsLayout.setBackground(Utils.createBorder(10, Color.TRANSPARENT, 1));
        lParams = new LinearLayout.LayoutParams(updateLayoutWidth, addCatLayoutHeight);
        lParams.setMargins((screenWidth - updateLayoutWidth) / 2, betweenLayoutsMargin / 2, 0, 0);
        LinearLayout updateLayout = mainView.findViewById(R.id.update_layout);
        updateLayout.setLayoutParams(lParams);
        updateLayout.setBackground(Utils.createBorder(10, Color.TRANSPARENT, 2, strokeColor));

        lParams = new LinearLayout.LayoutParams(addCatButtonWidth, compAddCatHeight);
        lParams.gravity = Gravity.CENTER_VERTICAL;
        lParams.setMargins(bigLayoutMarginWidth + 5, 0, 0, 0);
        Button addCategoryBtn = mainView.findViewById(R.id.add_category_btn);
        //addCategoryBtn.setBackground(Utils.createBorder(10, ContextCompat.getColor(getContext(), R.color.toolBar), 1));
        addCategoryBtn.setLayoutParams(lParams);
        addCategoryBtn.setOnClickListener(this);
        lParams = new LinearLayout.LayoutParams((int) (colorWidth * 1.5), compAddCatHeight);
        lParams.gravity = Gravity.CENTER_VERTICAL;
        lParams.setMargins(betweenColorsMargin * 3, 0, 0, 0);
        Button updateBtn = mainView.findViewById(R.id.update_btn);
        updateBtn.setLayoutParams(lParams);
        // updateBtn.setBackground(Utils.createBorder(10, ContextCompat.getColor(getContext(), R.color.toolBar), 1));
        updateBtn.setOnClickListener(this);

        lParams = new LinearLayout.LayoutParams(editCatWidth, compAddCatHeight);
        lParams.gravity = Gravity.CENTER_VERTICAL;
        lParams.setMargins(bigLayoutMarginWidth * 4, 0, 0, 0);
        addCategoryEdit = mainView.findViewById(R.id.category_edit);
        addCategoryEdit.setLayoutParams(lParams);
        //addCategoryEdit.setBackground(Utils.createBorder(10, Color.WHITE, 1, strokeColor));
        addCategoryEdit.clearFocus();

        lParams = new LinearLayout.LayoutParams(catRecyclerWidth, entireHeaderHeight);
        TextView categoryHeaderTxt = mainView.findViewById(R.id.category_header_txt);
        categoryHeaderTxt.setLayoutParams(lParams);
        lParams = new LinearLayout.LayoutParams(colorWidth, compAddCatHeight);
        lParams.gravity = Gravity.CENTER_VERTICAL;
        lParams.setMargins(betweenColorsMargin, 0, 0, 0);
        newColorTxt = mainView.findViewById(R.id.new_color_txt);
        newColorTxt.setLayoutParams(lParams);
        newColorTxt.setBackground(Utils.createBorder(15, Color.TRANSPARENT, 1, strokeColor));
        currentColorTxt = mainView.findViewById(R.id.current_color_txt);
        currentColorTxt.setLayoutParams(lParams);
        currentColorTxt.setBackground(Utils.createBorder(15, Color.TRANSPARENT, 1, strokeColor));
        lParams = new LinearLayout.LayoutParams(arrowWidth, (int) (compAddCatHeight * 0.7));
        lParams.gravity = Gravity.CENTER_VERTICAL;
        lParams.setMargins(betweenColorsMargin, 0, 0, 0);
        TextView arrowTxt = mainView.findViewById(R.id.arrow_image);
        arrowTxt.setLayoutParams(lParams);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        lParams = new LinearLayout.LayoutParams(catRecyclerWidth - 5, entireHeight - entireHeaderHeight - 8);
        lParams.setMargins(3, 0, 3, 0);
        categoryAdapter = new CategoryRecyclerAdapter(this);
        RecyclerView categoryRecyclerView = mainView.findViewById(R.id.category_recycler_view);
        categoryRecyclerView.setLayoutParams(lParams);
        categoryRecyclerView.setAdapter(categoryAdapter);
        categoryRecyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(categoryRecyclerView);

        createTabs();
        refreshCategories();
    }

    private void createTabs() {
        lParams = new LinearLayout.LayoutParams(tabsWidth - 5, entireHeaderHeight);
        lParams.setMargins(2, 2, 0, 0);

        TabLayout tabLayout = mainView.findViewById(R.id.tab_layout);
        tabLayout.setLayoutParams(lParams);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //  tabLayout.setSelectedTabIndicatorColor(BLUE_1);
        //  tabLayout.setTabTextColors(BLACK_2, Color.BLACK);


        lParams = new LinearLayout.LayoutParams(tabsWidth - 5, entireHeight - 130);
        lParams.setMargins(2, 2, 0, 2);
        pager = mainView.findViewById(R.id.pager);
        pager.setLayoutParams(lParams);

        final PagerAdapter adapter = new PagerAdapter(getChildFragmentManager(), getLifecycle());
        pager.setAdapter(adapter);
        //pager.registerOnPageChangeCallback(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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
        if (addCategoryEdit.getText().length() == 0)
        {
            Toast.makeText(getContext(), "The category name must not be blank", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isCategoryExists = dataHelper.getListOfCategories().stream()
                .anyMatch(cat -> cat.getName().equals(addCategoryEdit.getText().toString()));
        if (isCategoryExists)
        {
            Toast.makeText(getContext(), "The category already in the list", Toast.LENGTH_LONG).show();
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
            final Category deletedCategory = dataHelper.getListOfCategories().get(position);
            List<Item> itemsOfCategory = dataHelper.getListOfItems(item -> item.getCategory().equals(deletedCategory.getName()));
            if (!itemsOfCategory.isEmpty())
            {
                manageCategoryDialog = new ManageCategoryDialog(activity, DELETE_CATEGORY) {
                    @Override
                    public void okButtonPressed(String newCategoryName) {

                        //int newColor = Color.WHITE;
                        //category.setColor(newColor);
                        //category.setName(newCategoryName);
                        dataHelper.changeCategoryName(deletedCategory, newCategoryName);
                        itemRecycler.refreshItems(REMOVE_ITEM);
                        Toast.makeText(getContext(), deletedCategory.getName() + " was renamed to " + newCategoryName,
                                Toast.LENGTH_SHORT).show();
                        manageCategoryDialog.dismiss();
                        refreshCategories();
                    }

                    @Override
                    public void cancelButtonPressed() {
                        refreshCategories();
                        manageCategoryDialog.dismiss();
                    }
                };
                manageCategoryDialog.show();
            }
            else
            {
                categoryAdapter.removeItem(position);
                restoreOption(null, deletedCategory, position, "The category was removed from list");
                dataHelper.removeCategory(deletedCategory);
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


    public void restoreColorOption(Integer deleteColor, int deletedIndex) {
        restoreOption(deleteColor, null, deletedIndex, "The color removed from list");
    }


    public void restoreCategory(Category deleteCategory, int deletedIndex) {
        categoryAdapter.restoreItem(deleteCategory, deletedIndex);
        //DbHandler.populateCategory(MainActivity.getDbInstance(), deleteCategory);
        dataHelper.addCategory(deleteCategory);
        refreshCategories();
    }

    private void restoreOption(Integer deleteColor, Category deleteCategory_, int deletedIndex_, String message) {
        // final Integer deleteColor = color;
        final int deletedIndex = deletedIndex_;
        final Category deleteCategory = deleteCategory_;

        Snackbar snackbar = Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", view -> {
            if (deleteColor != null)
            {
                favoriteColors.restoreDeletedColor(deleteColor, deletedIndex);
                //pager.setCurrentItem(1);
                //TabLayout.Tab tab = tabLayout.getTabAt(1);
                // tab.select();
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
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
