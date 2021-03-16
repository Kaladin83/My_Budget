package com.example.mybudget.components.item;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.ItemDrawer;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.R;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.example.mybudget.utils.Enums.Level.CATEGORY_LVL;
import static com.example.mybudget.utils.Enums.Level.ITEM_LVL;
import static com.example.mybudget.utils.Enums.Level.SUB_CATEGORY_LVL;

public class ItemsRecyclerAdapter2 extends RecyclerView.Adapter<ItemsRecyclerAdapter2.MyViewHolder> implements Constants{
    private final int screenWidth, logicalDensity;
    private final DataHelper dataHelper;
    private final Context context;

    public ItemsRecyclerAdapter2(Activity activity)
    {
        context = activity.getApplicationContext();
        dataHelper = DataHelper.getDataHelper(context);
        screenWidth = Utils.getScreenWidth(activity);
        logicalDensity = (int) Utils.getLogicalDensity(activity);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mainLayout;
        FrameLayout objectLayout;
        RelativeLayout backgroundLayout, mainRowLayout;
        TextView dateTxt, arrowTxt;

        private MyViewHolder(View v) {
            super(v);
            objectLayout = v.findViewById(R.id.object_layout);
            mainLayout = v.findViewById(R.id.main_layout);
            mainRowLayout = v.findViewById(R.id.main_row_layout);
            backgroundLayout = v.findViewById(R.id.background_layout);
            dateTxt = v.findViewById(R.id.date_added_txt);
            arrowTxt = v.findViewById(R.id.arrow_txt);
        }
    }

    public class MyViewHolder1 extends MyViewHolder {
        RelativeLayout categorySumsLayout;
        TextView categoryTxt, categorySumTxt, categoryAverageTxt;
        View separator;

        private MyViewHolder1(View v) {
            super(v);
            categoryTxt = v.findViewById(R.id.category_txt);
            categorySumsLayout = v.findViewById(R.id.category_sums_layout);
            categorySumTxt = v.findViewById(R.id.category_total_txt);
            categoryAverageTxt = v.findViewById(R.id.category_average_txt);
            separator = v.findViewById(R.id.separator);
        }
    }

    public class MyViewHolder2 extends MyViewHolder {
        RelativeLayout descriptionLayout;
        TextView amountTxt;
        Button okButton, cancelButton;
        EditText descriptionEdit;

        private MyViewHolder2(View v) {
            super(v);
            cancelButton = v.findViewById(R.id.cancel_btn);
            okButton = v.findViewById(R.id.ok_btn);
            descriptionEdit = v.findViewById(R.id.description_edit);
            descriptionLayout = v.findViewById(R.id.description_layout);
            amountTxt = v.findViewById(R.id.amount_txt);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataHelper.getListOfCombinedItems().get(position).getLevel() == ITEM_LVL ? 2 : 1;
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
        int height = 45;
        int margins = 10;
        FrameLayout.LayoutParams fParams;
        int parentLine = 45;
        int subTotalLine = 25;
        int subCategoryWidth = (int) ((screenWidth) * 0.8);
        ItemDrawer combinedItem = dataHelper.getListOfCombinedItems().get(position);
        Item lastAddedItem = dataHelper.getLastAddedItem();
        Statistics stats = Utils.getStatisticsByCategory(combinedItem.getItem().getCategory());
        if (holder instanceof MyViewHolder1)
        {
            MyViewHolder1 holder1 = (MyViewHolder1) holder;
            height = createCategoryRow(holder1, position, height, parentLine + subTotalLine + margins);

            int color = Utils.findColor(combinedItem.getItem().getCategory());
            holder1.categoryTxt.setText(combinedItem.getItem().getCategory());
            //holder1.categoryTxt.setBackground(Utils.createBorder(context, 20, color, 1));
            holder1.categorySumsLayout.setPadding(15, 0, 15, 0);
           // holder1.categorySumsLayout.setBackground(Utils.createBorder(context, 15, GRAY_3, 1));
            holder1.categorySumTxt.setText(context.getString(R.string.category_total, String.valueOf(stats.getSum())));
            holder1.categoryAverageTxt.setText(context.getString(R.string.category_average, String.valueOf(stats.getMean())));

            if (combinedItem.getLevel() == SUB_CATEGORY_LVL)
            {
                holder1.arrowTxt.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(subCategoryWidth,
                        subTotalLine * logicalDensity);
                rParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rParams.setMargins(0, 5 * logicalDensity, 15 * logicalDensity, 5 * logicalDensity);
                holder1.categorySumsLayout.setLayoutParams(rParams);
            }
            else
            {
                holder1.arrowTxt.setVisibility(View.GONE);

                RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(screenWidth,
                        subTotalLine * logicalDensity);
                rParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rParams.setMargins(15 * logicalDensity, 5 * logicalDensity, 15 * logicalDensity, 5 * logicalDensity);
                holder1.categorySumsLayout.setLayoutParams(rParams);
            }
        }
        else
        {
            MyViewHolder2 holder2 = (MyViewHolder2) holder;
            height = createItemRow(holder2, position, height, parentLine + subTotalLine + margins);
            holder2.descriptionEdit.setText(combinedItem.getItem().getDescription());
            //holder2.descriptionEdit.setBackground(Utils.createBorder(context, 10, Color.TRANSPARENT, 1));
            holder2.descriptionEdit.setPadding(10, 5, 10, 5);
            holder2.descriptionEdit.addTextChangedListener(new MyTextWatcher(holder2, logicalDensity, subCategoryWidth));
            holder2.amountTxt.setText(String.valueOf(combinedItem.getItem().getAmount()));
            holder2.amountTxt.setTextColor(combinedItem.getItem().getDate().equals
                    (lastAddedItem.getDate()) ? GREEN_2 : Color.BLACK);
            holder2.okButton.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_check));
            holder2.okButton.setOnClickListener(v -> {
                if (holder2.descriptionEdit.getText().toString().equals(""))
                {
                    // update description  in table and refresh
                }
            });
            holder2.cancelButton.setBackground(ContextCompat.getDrawable(context, R.drawable.icon_close));
            holder2.cancelButton.setOnClickListener(v -> holder2.descriptionEdit.setText(""));

            RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(subCategoryWidth,
                    parentLine * logicalDensity);
            rParams.setMargins(35 * logicalDensity, 0, 10 * logicalDensity, 0);
            holder2.mainRowLayout.setLayoutParams(rParams);
        }

        fParams = new FrameLayout.LayoutParams(screenWidth, (int) Math.ceil(height * logicalDensity));
        fParams.gravity = Gravity.TOP;
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams((int) (screenWidth * 0.9),
                (int) Math.ceil(subTotalLine * logicalDensity));
        lParams.gravity = Gravity.CENTER;
        lParams.setMargins(10, (int) Math.ceil(8 * logicalDensity), 10, (int) Math.ceil(11 * logicalDensity));
        holder.mainLayout.setLayoutParams(fParams);
        holder.mainLayout.setGravity(Gravity.CENTER);
        holder.mainLayout.setVerticalGravity(Gravity.TOP);
        holder.objectLayout.setLayoutParams(fParams);
        holder.backgroundLayout.setLayoutParams(fParams);

        String date = combinedItem.getItem().getDate().substring(0, combinedItem.getItem().getDate().length() - 3);
        holder.dateTxt.setText(date);
        holder.dateTxt.setTextColor(combinedItem.getItem().getDate().equals(lastAddedItem.getDate()) ?
                GREEN_2 : Color.BLACK);

        holder.mainRowLayout.setOnClickListener(view -> {
            dataHelper.setListOfStatisticItems(Utils.getParentStatisticsAsItems());
            addToCombinedItems(position);
            notifyDataSetChanged();
        });
    }

    public void addToCombinedItems(int position) {
        String date = "", subCategoryName = "", parentName;
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
            listOfItems = Utils.sortItemsByDate(dataHelper.getListOfItems(item -> item.getCategory().equals(parentName)));
        }
        else
        {
            subCategoryName = selectedCategory;
            parentName = Utils.getParentCategoryName(subCategoryName);
            listOfItems = Utils.sortItemsByDate(Utils.getItemsOfCategories(subCategoryName));
            date = combinedItems.get(position).getItem().getDate();
        }

        listOfSubs = Utils.sortItemsByDate(Utils.getItemsOfCategories(parentName));
        dataHelper.setListOfCombinedItems(new ArrayList<>());

        selectedParent = addCategoriesToCombinedItems(parentName.equals("") ? subCategoryName : parentName,
                isCategoryExpanded);

        if (!isCategoryExpanded)
        {
            selectedSub = addSubCategoriesToCombinedItems(listOfSubs, subCategoryName, isSubCategoryExpanded, selectedParent);
            if (!isSubCategoryExpanded)
            {
                addItemsToCombinedItems(listOfItems, date, isItemChosen, isItemExpanded, selectedSub == -1 ?
                        selectedParent : selectedSub);
            }
        }
    }

    private int addCategoriesToCombinedItems(String parentName, boolean isCategoryExpanded) {
        int selectedParent = -1;
        List<Item> parentItems = dataHelper.getListOfStatisticItems();
        List<ItemDrawer> combinedItems = dataHelper.getListOfCombinedItems();
        for (int i = 0; i < parentItems.size(); i++)
        {
            if (parentItems.get(i).getCategory().equals(parentName) && !isCategoryExpanded)
            {
                combinedItems.add(new ItemDrawer(parentItems.get(i), BLUE_5, true, CATEGORY_LVL));
                selectedParent = i;
            }
            else
            {
                combinedItems.add(new ItemDrawer(parentItems.get(i), Color.WHITE, false, CATEGORY_LVL));
            }
        }
        return selectedParent;
    }

    private int addSubCategoriesToCombinedItems(List<Item> listOfSubs, String subCategoryName,
                                                boolean isSubCategoryExpanded, int selectedParent) {
        int selectedSub = -1;
        List<ItemDrawer> combinedItems = dataHelper.getListOfCombinedItems();
        for (int i = 0; i < listOfSubs.size(); i++)
        {
            if (subCategoryName.equals(listOfSubs.get(i).getCategory()) && !isSubCategoryExpanded)
            {
                combinedItems.add(selectedParent + 1 + i, new ItemDrawer(listOfSubs.get(i), BLUE_5, true, SUB_CATEGORY_LVL));
                selectedSub = selectedParent + 1 + i;
            }
            else
            {
                combinedItems.add(selectedParent + 1 + i, new ItemDrawer(listOfSubs.get(i), BLUE_5, false, SUB_CATEGORY_LVL));
            }
        }
        return selectedSub;
    }

    private void addItemsToCombinedItems(List<Item> listOfItems, String date, boolean isItemChosen, boolean isItemExpanded,
                                         int selectedSub) {
        List<ItemDrawer> combinedItems = dataHelper.getListOfCombinedItems();
        for (int i = 0; i < listOfItems.size(); i++)
        {
            if (date.equals(listOfItems.get(i).getDate()))
            {
                if (isItemChosen && !isItemExpanded)
                {
                    combinedItems.add(selectedSub + 1 + i, new ItemDrawer(listOfItems.get(i), BLUE_5, true, ITEM_LVL));
                }
                else
                {
                    combinedItems.add(selectedSub + 1 + i, new ItemDrawer(listOfItems.get(i), BLUE_5, false, ITEM_LVL));
                }
            }
            else
            {
                combinedItems.add(selectedSub + 1 + i, new ItemDrawer(listOfItems.get(i), BLUE_5, false, ITEM_LVL));
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataHelper.getListOfCombinedItems().size();
    }

//        private void removeItem(Item item, int position) {
//
//            //listOfParentItems.remove(position);
//            //notifyItemRemoved(position);
//        }
//
//        private void restoreItem(Item item, int position) {
//
//            listOfParentItems.add(position, item);
//            listOfItems.add(position, item);
//            notifyItemInserted(position);
//        }

    private int createCategoryRow(MyViewHolder1 holder, int position, int smallHeight, int largeHeight) {
        if (position == -1)
        {
            return smallHeight;
        }
        ItemDrawer id = dataHelper.getListOfCombinedItems().get(position);
        int height = id.isExtended() ? largeHeight : smallHeight;

        holder.mainLayout.setBackgroundColor(id.getBackground());
        holder.categorySumsLayout.setVisibility(id.isExtended() ? View.VISIBLE : View.GONE);
        //    holder.separator.setVisibility(id.isExtended()? View.VISIBLE: View.GONE);
        return height;
    }

    private int createItemRow(MyViewHolder2 holder, int position, int smallHeight, int largeHeight) {
        if (position == -1)
        {
            return smallHeight;
        }
        ItemDrawer id = dataHelper.getListOfCombinedItems().get(position);
        int height = id.isExtended() ? largeHeight : smallHeight;

        holder.mainLayout.setBackgroundColor(id.getBackground());
        holder.descriptionLayout.setVisibility(id.isExtended() ? View.VISIBLE : View.GONE);
        return height;
    }

    private class MyTextWatcher implements TextWatcher {
        private final ItemsRecyclerAdapter2.MyViewHolder2 holder2;
        private final int logicalDensity;
        private final int subCategoryWidth;

        public MyTextWatcher(ItemsRecyclerAdapter2.MyViewHolder2 holder, int logicalDensity, int subCategoryWidth) {
            this.subCategoryWidth = subCategoryWidth;
            this.logicalDensity = logicalDensity;
            holder2 = holder;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!s.toString().equals("") || count != before)
            {
                RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(screenWidth - (120 * logicalDensity),
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rParams.setMarginStart(20 * logicalDensity);
                holder2.descriptionEdit.setLayoutParams(rParams);
                holder2.okButton.setVisibility(View.VISIBLE);
                holder2.cancelButton.setVisibility(View.VISIBLE);
            }
            else
            {
                RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(subCategoryWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                rParams.setMarginStart(20 * logicalDensity);
                holder2.descriptionEdit.setLayoutParams(rParams);
                holder2.okButton.setVisibility(View.GONE);
                holder2.cancelButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
