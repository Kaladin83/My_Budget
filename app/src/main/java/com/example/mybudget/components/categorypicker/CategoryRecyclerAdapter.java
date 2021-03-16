package com.example.mybudget.components.categorypicker;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.R;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Utils;

import java.util.List;

public class CategoryRecyclerAdapter  extends RecyclerView.Adapter<CategoryRecyclerAdapter.MyViewHolder> {

    private int selectedPosition = -1;
    private final DataHelper dataHelper;
    private final CategoryPicker categoryPicker;
    private final float logicalDensity;
    private final Activity activity;

    public CategoryRecyclerAdapter(CategoryPicker categoryPicker){
        this.activity = categoryPicker.requireActivity();
        this.categoryPicker = categoryPicker;
        this.dataHelper = DataHelper.getDataHelper(activity);
        this.logicalDensity = Utils.getLogicalDensity(activity);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public final RelativeLayout mainLayout;
        private final TextView colorTxt, colorSubTxt;
        private final TextView categoryTxt;

        public MyViewHolder(View v) {
            super(v);
            mainLayout = v.findViewById(R.id.main_category_list_layout);
            colorTxt = v.findViewById(R.id.color_category_txt);
            colorSubTxt = v.findViewById(R.id.color_category_sub_txt);
            categoryTxt = v.findViewById(R.id.text_txt);
        }
    }

    @NonNull
    @Override
    public CategoryRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CategoryRecyclerAdapter.MyViewHolder holder, int position_) {
        final int position = position_;
        holder.mainLayout.setId(position);
        holder.mainLayout.setSelected(selectedPosition == position);
        Category category = dataHelper.getListOfCategories().get(position);
        int color = category.getColor();
        holder.categoryTxt.setText(category.getName());

        int width = category.getParent().equals("") ? (int) Math.ceil(30 * logicalDensity) :
                (int) Math.ceil(14 * logicalDensity);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(width, (int) Math.ceil(20 * logicalDensity));
        lParams.setMarginEnd(6);
        holder.colorTxt.setLayoutParams(lParams);
        holder.colorTxt.setBackground(Utils.createBorder(15, category.getParent().equals("") ? color :
                Utils.findColor(category.getParent()), 1, Utils.getThemeStrokeColor(activity)));

        lParams = new LinearLayout.LayoutParams(width, (int) Math.ceil(20 * logicalDensity));
        holder.colorSubTxt.setVisibility(category.getParent().equals("") ? View.GONE : View.VISIBLE);
        holder.colorSubTxt.setLayoutParams(lParams);
        holder.colorSubTxt.setBackground(Utils.createBorder(15, color, 1, Utils.getColor(R.color.light_black, activity)));

        holder.mainLayout.setOnClickListener(view -> {
            selectedPosition = position;
            String cat = ((TextView) ((RelativeLayout) view).getChildAt(0)).getText().toString();
            categoryPicker.updateColor(cat);
        });
    }

    @Override
    public int getItemCount() {
        return dataHelper.getListOfCategories().size();
    }

    public void removeItem(int position) {
        List<Category> combinedCategories = dataHelper.getListOfCategories();
        combinedCategories.remove(position);
    }

    public void restoreItem(Category category, int position) {
        List<Category> combinedCategories = dataHelper.getListOfCategories();
        combinedCategories.add(position, category);
        notifyItemInserted(position);
    }
}