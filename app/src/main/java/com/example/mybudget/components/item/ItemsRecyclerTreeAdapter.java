package com.example.mybudget.components.item;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.components.custom.CategoryIcon;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.ItemDrawer;
import com.example.mybudget.R;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.example.mybudget.utils.Enums.Level;
import static com.example.mybudget.utils.Enums.Level.ITEM_LVL;
import static com.example.mybudget.utils.Enums.Level.CATEGORY_LVL;
import static com.example.mybudget.utils.Enums.Level.SUB_CATEGORY_LVL;

public class ItemsRecyclerTreeAdapter extends RecyclerView.Adapter<ItemsRecyclerTreeAdapter.MyViewHolder> {
    private final int screenWidth, logicalDensity;
    private final DataHelper dataHelper;
    private final Activity activity;
    private DescriptionDialog descriptionDialog;

    public ItemsRecyclerTreeAdapter(Activity activity) {
        this.activity = activity;
        this.dataHelper = DataHelper.getDataHelper(this.activity);
        this.screenWidth = Utils.getScreenWidth(activity);
        this.logicalDensity = (int) Utils.getLogicalDensity(activity);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ConstraintLayout mainRowLayout;

        ImageView arrowImage;

        private MyViewHolder(View v) {
            super(v);
            mainRowLayout = v.findViewById(R.id.main_row_layout);
            arrowImage = v.findViewById(R.id.arrow_image);
        }
    }

    public class MyViewHolder1 extends MyViewHolder {
        //RelativeLayout categorySumsLayout;
        TextView categoryTxt, categorySumTxt;
        CategoryIcon categoryIcon;
        View separator;

        private MyViewHolder1(View v) {
            super(v);
            categoryTxt = v.findViewById(R.id.category_txt);
            categorySumTxt = v.findViewById(R.id.category_total_txt);
            separator = v.findViewById(R.id.separator);
            categoryIcon = v.findViewById(R.id.categories_icon);
        }
    }

    public class MyViewHolder2 extends MyViewHolder {
        TextView amountTxt;
        ImageView descriptionImage;
        TextView dateTxt;

        private MyViewHolder2(View v) {
            super(v);
            descriptionImage = v.findViewById(R.id.description_image);
            amountTxt = v.findViewById(R.id.amount_txt);
            dateTxt = v.findViewById(R.id.date_added_txt);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataHelper.getListOfCombinedItems().get(position).getLevel() == ITEM_LVL ? 2 : 1;
    }

    @Override
    public int getItemCount() {
        return dataHelper.getListOfCombinedItems().size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.populator_of_category, parent, false);
            return new MyViewHolder1(v);
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.populator_of_item, parent, false);
            return new MyViewHolder2(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemDrawer combinedItem = dataHelper.getListOfCombinedItems().get(position);
        Item item = combinedItem.getItem();
        String lastAddedItemDate = dataHelper.getLastAddedItem().getDate();
        if (holder instanceof MyViewHolder1)
        {
            renderCategoryItem(holder, combinedItem);
        }
        else
        {
            renderItem(holder, item, lastAddedItemDate, position);
        }
        holder.mainRowLayout.setOnClickListener(view -> {
            addToCombinedItems(position);
            notifyDataSetChanged();
        });
    }

    private void renderCategoryItem(MyViewHolder holder, ItemDrawer combinedItem) {
        MyViewHolder1 holder1 = (MyViewHolder1) holder;
        Item item = combinedItem.getItem();
        Level level = combinedItem.getLevel();
        Statistics stats = Utils.getStatsForCategory(item.getCategory());
        int color = Utils.findColor(item.getCategory());
        int iconSize = 56 * logicalDensity;

        holder1.categoryTxt.setText(item.getCategory());
        holder1.categorySumTxt.setText(activity.getString(R.string.category_total, String.valueOf(stats.getSum())));
        holder1.arrowImage.setVisibility(level == SUB_CATEGORY_LVL ? View.VISIBLE : View.GONE);
        holder1.separator.setVisibility(level == CATEGORY_LVL ? View.VISIBLE : View.GONE);
        int radius = level == CATEGORY_LVL ? iconSize / 2 : (int) (iconSize / 2.4);
        //holder1.categoryIcon.setParams(item.getCategory(), color, activity);

        int leftPadding = level == CATEGORY_LVL ? 25 : 50;
        holder.mainRowLayout.setPadding(leftPadding, 0 ,0,0);
    }

    private void renderItem(MyViewHolder holder, Item item, String lastAddedItemDate, int position) {
        MyViewHolder2 holder2 = (MyViewHolder2) holder;
        holder2.amountTxt.setText(String.valueOf(item.getAmount()));
        holder2.amountTxt.setSelected(item.getDate().equals(lastAddedItemDate));
        holder2.descriptionImage.setOnClickListener(v -> ShowDescriptionDialog(item, position));
        holder2.dateTxt.setText(item.getDate().length() > 3 ? item.getDate().substring(0, item.getDate().length() - 3) : "No date");
        holder2.dateTxt.setSelected(item.getDate().equals(lastAddedItemDate));

        String parentCategoryName = Utils.getParentCategoryName(item.getCategory());
        int paddingLeft = !parentCategoryName.equals("") ? 140 : 50;
        holder.mainRowLayout.setPadding(paddingLeft, 0 ,0,0);
    }

    private void ShowDescriptionDialog(Item selectedItem, int position) {
        descriptionDialog = new DescriptionDialog(activity, selectedItem) {
            @Override
            public void okButtonPressed(String description) {
                Toast.makeText(activity, "The description was updated", Toast.LENGTH_SHORT).show();
                dataHelper.updateDescription(selectedItem, description, position);
                descriptionDialog.dismiss();
            }
        };
        descriptionDialog.show();
    }

    public void addToCombinedItems(int position) {
        String subCategoryName = "", parentName;
        int selectedParent, selectedSub;
        List<Item> listOfItems;
        List<Item> listOfSubs;
        List<ItemDrawer> combinedItems = dataHelper.getListOfCombinedItems();
        boolean isItemChosen = combinedItems.get(position).getLevel() == ITEM_LVL;
        boolean isItemExpanded = combinedItems.get(position).isExtended();
        boolean isCategoryExpanded =
                combinedItems.get(position).getLevel() == CATEGORY_LVL && combinedItems.get(position).isExtended();
        boolean isSubCategoryExpanded =
                combinedItems.get(position).getLevel() == SUB_CATEGORY_LVL && combinedItems.get(position).isExtended();
        //category which has subCategories has been selected
        String selectedCategory = combinedItems.get(position).getItem().getCategory();
        if (Utils.getParentCategoryName(selectedCategory).equals(""))
        {
            parentName = selectedCategory;
        }
        else
        {
            subCategoryName = selectedCategory;
            parentName = Utils.getParentCategoryName(subCategoryName);
        }
        listOfItems = Utils.sortItemsByDate(dataHelper.getListOfItems(item -> item.getCategory().equals(selectedCategory)));
        listOfSubs = Utils.sortItemsByDate(Utils.getItemsFromStatistics(cat -> cat.getParent().equals(parentName)));
        dataHelper.setListOfCombinedItems(new ArrayList<>());

        selectedParent = addToCombinedItems(Utils.getItemsFromStatistics(cat -> cat.getParent().equals("")),
                combinedItems.get(position).getItem(), parentName.equals("") ? subCategoryName : parentName, isCategoryExpanded,
                isItemChosen, -1, Level.CATEGORY_LVL);
        if (!isCategoryExpanded)
        {
            selectedSub = addToCombinedItems(listOfSubs, combinedItems.get(position).getItem(), subCategoryName,
                    isSubCategoryExpanded, isItemChosen,
                    selectedParent, Level.SUB_CATEGORY_LVL);
            if (!isSubCategoryExpanded)
            {
                addToCombinedItems(listOfItems, combinedItems.get(position).getItem(), "", isItemExpanded, isItemChosen,
                        selectedSub == -1 ? selectedParent : selectedSub, Level.ITEM_LVL);
            }
        }
    }

    private int addToCombinedItems(List<Item> items, Item item, String categoryName, boolean isExpanded, boolean isItemChosen,
                                   int selectedParent, Level level) {
        int selectedSub = -1, i = 0;
        int index = level == Level.CATEGORY_LVL ? 0 : selectedParent + 1;
        boolean isSelected = level != Level.CATEGORY_LVL;
        Predicate<Item> predicate = itm -> level == Level.ITEM_LVL ? itm.equals(item) && isItemChosen :
                itm.getCategory().equals(categoryName);
        List<ItemDrawer> combinedItems = dataHelper.getListOfCombinedItems();
        for (Item itm : items)
        {
            if (predicate.test(itm) && !isExpanded)
            {
                combinedItems.add(index + i, new ItemDrawer(itm, true, true, level));
                selectedSub = index + i;
            }
            else
            {
                combinedItems.add(index + i, new ItemDrawer(itm, isSelected, false, level));
            }
            i++;
        }
        return selectedSub;
    }
}
