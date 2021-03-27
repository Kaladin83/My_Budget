package com.example.mybudget.components.item;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class ItemsRecyclerAdapter extends RecyclerView.Adapter<ItemsRecyclerAdapter.MyViewHolder> {
    private final int screenWidth, logicalDensity;
    private final DataHelper dataHelper;
    private final Activity activity;
    private DescriptionDialog descriptionDialog;

    public ItemsRecyclerAdapter(Activity activity) {
        this.activity = activity;
        this.dataHelper = DataHelper.getDataHelper(this.activity);
        this.screenWidth = Utils.getScreenWidth(activity);
        this.logicalDensity = (int) Utils.getLogicalDensity(activity);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mainLayout;
        FrameLayout objectLayout;
        RelativeLayout backgroundLayout, mainRowLayout;
        TextView dateTxt;
        ImageView arrowImage;

        private MyViewHolder(View v) {
            super(v);
            objectLayout = v.findViewById(R.id.object_layout);
            mainLayout = v.findViewById(R.id.main_layout);
            mainRowLayout = v.findViewById(R.id.main_row_layout);
            backgroundLayout = v.findViewById(R.id.background_layout);
            dateTxt = v.findViewById(R.id.date_added_txt);
            arrowImage = v.findViewById(R.id.arrow_image);
        }
    }

    public class MyViewHolder1 extends MyViewHolder {
        RelativeLayout categorySumsLayout;
        TextView categoryTxt, categorySumTxt, categoryAverageTxt;

        private MyViewHolder1(View v) {
            super(v);
            categoryTxt = v.findViewById(R.id.category_txt);
            categorySumsLayout = v.findViewById(R.id.category_sums_layout);
            categorySumTxt = v.findViewById(R.id.category_total_txt);
            categoryAverageTxt = v.findViewById(R.id.category_average_txt);
        }
    }

    public class MyViewHolder2 extends MyViewHolder {
        TextView amountTxt;
        ImageView descriptionImage;

        private MyViewHolder2(View v) {
            super(v);
            descriptionImage = v.findViewById(R.id.description_image);
            amountTxt = v.findViewById(R.id.amount_txt);
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
        int categoryItemWidth = (int) ((screenWidth) * 0.92);
        int subCategoryItemWidth = (int) ((screenWidth) * 0.82);
        int height = 45;
        int basicHeight = 45;
        int subTotalLine = 25;
        int inset = 10 * logicalDensity;
        ItemDrawer combinedItem = dataHelper.getListOfCombinedItems().get(position);
        Item item = combinedItem.getItem();
        String lastAddedItemDate = dataHelper.getLastAddedItem().getDate();
        String date = item.getDate();
        holder.mainLayout.setSelected(combinedItem.isSelected());
        if (holder instanceof MyViewHolder1)
        {
            height = createRow((MyViewHolder1) holder, position, basicHeight, basicHeight + subTotalLine + 10);
            renderCategoryItem(holder, subTotalLine, basicHeight, combinedItem, subCategoryItemWidth, categoryItemWidth, inset);
        }
        else
        {
            renderItem(holder, item, lastAddedItemDate, position, basicHeight, inset, subCategoryItemWidth, categoryItemWidth);
        }
        FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(screenWidth, height * logicalDensity);
        holder.mainLayout.setLayoutParams(fParams);
        holder.objectLayout.setLayoutParams(fParams);
        holder.backgroundLayout.setLayoutParams(fParams);
        holder.dateTxt.setText(date.length() > 3 ? date.substring(0, date.length() - 3) : "No date");
        holder.dateTxt.setText(date);
        holder.dateTxt.setSelected(date.equals(lastAddedItemDate));
        holder.mainRowLayout.setOnClickListener(view -> {
            addToCombinedItems(position);
            notifyDataSetChanged();
        });
    }

    private void renderCategoryItem(MyViewHolder holder, int subTotalLine, int basicHeight, ItemDrawer combinedItem,
                                    int subCategoryItemWidth, int categoryItemWidth, int inset) {
        MyViewHolder1 holder1 = (MyViewHolder1) holder;
        Item item = combinedItem.getItem();
        Level level = combinedItem.getLevel();
        Statistics stats = Utils.getStatsForCategory(item.getCategory());
        int color = Utils.findColor(item.getCategory());
        int strokeColor = Utils.getColor(R.color.light_black, activity);
        int statisticsColor = Utils.getAttrColor(activity, R.attr.colorPrimaryVariant);

        holder1.categoryTxt.setText(item.getCategory());
        holder1.categoryTxt.setBackground(Utils.createBorder(20, Color.TRANSPARENT, 6, color));
        holder1.categorySumsLayout.setPadding(inset, 0, inset, 0);
        holder1.categorySumsLayout.setBackground(Utils.createBorder(15, statisticsColor, 1, strokeColor));
        holder1.categorySumTxt.setText(activity.getString(R.string.category_total, String.valueOf(stats.getSum())));
        holder1.categoryAverageTxt.setText(activity.getString(R.string.category_average, String.valueOf(stats.getAvg())));
        holder1.arrowImage.setVisibility(level == SUB_CATEGORY_LVL ? View.VISIBLE : View.GONE);

        int width = level == SUB_CATEGORY_LVL ? subCategoryItemWidth : categoryItemWidth;
        holder1.categorySumsLayout.setLayoutParams(getRLP(width, subTotalLine, RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.ALIGN_PARENT_END, inset, inset));
        width = level == CATEGORY_LVL ? screenWidth : categoryItemWidth;
        int rule = level == CATEGORY_LVL ? RelativeLayout.CENTER_HORIZONTAL : RelativeLayout.ALIGN_PARENT_END;
        holder1.mainRowLayout.setLayoutParams(getRLP(width, basicHeight, rule, -1, inset, 0));
    }

    private void renderItem(MyViewHolder holder, Item item, String lastAddedItemDate, int position, int basicHeight, int inset,
                            int subCategoryItemWidth, int categoryItemWidth) {

        MyViewHolder2 holder2 = (MyViewHolder2) holder;
        holder2.amountTxt.setText(String.valueOf(item.getAmount()));
        holder2.amountTxt.setSelected(item.getDate().equals(lastAddedItemDate));
        holder2.descriptionImage.setOnClickListener(v -> ShowDescriptionDialog(item, position));

        String parentCategoryName = Utils.getParentCategoryName(item.getCategory());
        int width = !parentCategoryName.equals("") ? subCategoryItemWidth : categoryItemWidth;
        holder2.mainRowLayout.setLayoutParams(getRLP(width, basicHeight, RelativeLayout.ALIGN_PARENT_END, -1, inset, 0));
    }

    private RelativeLayout.LayoutParams getRLP(int width, int height, int rule1, int rule2, int marginH, int marginV) {
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(width, height * logicalDensity);
        rParams.addRule(rule1);
        rParams.addRule(rule2 == -1 ? rule1 : rule2);
        rParams.setMargins(marginH, marginV, marginH, marginV);
        return rParams;
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

    private int createRow(MyViewHolder1 holder, int position, int smallHeight, int largeHeight) {
        if (position == -1)
        {
            return smallHeight;
        }
        ItemDrawer itemDrawer = dataHelper.getListOfCombinedItems().get(position);
        int height = itemDrawer.isExtended() ? largeHeight : smallHeight;

        holder.mainLayout.setSelected(itemDrawer.isSelected());
        holder.categorySumsLayout.setVisibility(itemDrawer.isExtended() ? View.VISIBLE : View.GONE);
        return height;
    }
}
