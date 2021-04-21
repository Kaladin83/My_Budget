package com.example.mybudget.components.item;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AlignmentSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.IAction;
import com.example.mybudget.MainActivity;
import com.example.mybudget.MessageDialog;
import com.example.mybudget.R;
import com.example.mybudget.components.custom.CategoryIcon;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Enums;
import com.example.mybudget.utils.Utils;
import com.google.common.collect.ImmutableMap;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jp.wasabeef.blurry.Blurry;

import static android.view.View.INVISIBLE;
import static androidx.recyclerview.widget.RecyclerView.ViewHolder;
import static com.example.mybudget.utils.Enums.Action.DELETE_CATEGORY;
import static com.example.mybudget.utils.Enums.Action.DELETE_ITEM;
import static com.example.mybudget.utils.Enums.MessageType.*;

public class ItemsRecyclerAppViewAdapter extends RecyclerView.Adapter<ItemsRecyclerAppViewAdapter.MyViewHolder> {
    private final Map<Category, List<Category>> parentsAndChildren;
    private final List<Category> categoryItems;
    private final MainActivity activity;
    private final ApplicationViewBuilder viewBuilder;
    private final IAction iAction;
    private final Window window;
    private boolean dimScreen = false;
//    private int currentPosition = -1;
//    private int prevPosition = -1;
    //private Mode mode;
    private Map<Integer,String> categoriesToDelete = new HashMap<>();

    public ItemsRecyclerAppViewAdapter(MainActivity mainActivity, ApplicationViewBuilder viewBuilder,
                                       IAction iAction, Window window, List<Category> categoryItems) {
        this.activity = mainActivity;
        this.viewBuilder = viewBuilder;
        this.categoryItems = categoryItems;
        this.parentsAndChildren = getChildrenAndParents(categoryItems);
        this.iAction = iAction;
        this.window = window;
        this.setHasStableIds(true);
        //mode = Mode.VIEW;
    }

    private Map<Category, List<Category>> getChildrenAndParents(List<Category> categoryItems) {
        Function<Category, List<Category>> func = cat1 -> Utils.getCategoryItems(cat2 -> cat2.getParent().equals(cat1.getName()));
        return categoryItems.stream()
                .collect(Collectors.toMap(c -> c, func));
    }

    public void updateData(List<Category> categoryItems) {
        this.categoryItems.clear();
        this.categoryItems.addAll(categoryItems);
        this.parentsAndChildren.clear();
        this.parentsAndChildren.putAll(getChildrenAndParents(categoryItems));
    }

    public static class MyViewHolder extends ViewHolder {
        TextView categoryTxt;
        FrameLayout deleteLayout;

        private MyViewHolder(View v) {
            super(v);
            categoryTxt = v.findViewById(R.id.category_txt);
            deleteLayout = v.findViewById(R.id.delete_icon_layout);
        }
    }

    public static class MyViewHolder1 extends MyViewHolder {
        CategoryIcon categoryIcon;

        private MyViewHolder1(View v) {
            super(v);
            categoryIcon = v.findViewById(R.id.category_icon);
        }
    }

    public static class MyViewHolder2 extends MyViewHolder {
        private final Map<String, CategoryIcon> folderIcons;
        ConstraintLayout folder;

        private MyViewHolder2(View v) {
            super(v);
            folder = v.findViewById(R.id.folder);
            folderIcons = ImmutableMap.of("icon0", v.findViewById(R.id.btn_0), "icon1", v.findViewById(R.id.btn_1),
                    "icon2", v.findViewById(R.id.btn_2), "icon3", v.findViewById(R.id.btn_3));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return Objects.requireNonNull(parentsAndChildren.get(categoryItems.get(position))).isEmpty() ? 1 : 2;
    }

    @Override
    public int getItemCount() {
        return parentsAndChildren.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_view_item, parent, false);
            return new MyViewHolder1(v);
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.app_view_item_folder, parent, false);
            return new MyViewHolder2(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Category parent = categoryItems.get(position);
        String categoryName = parent.getName();
        List<Category> children = Objects.requireNonNull(parentsAndChildren.get(parent));
        int txtColor = dimScreen ? Utils.getTintDimColor(activity) : Utils.getAttrColor(activity, android.R.attr.textColorPrimary);

        holder.categoryTxt.setTextColor(txtColor);
        holder.categoryTxt.setText(categoryName);
        if (children.isEmpty())
        {
            populateIcon(parent, categoryName, ((MyViewHolder1) holder).categoryIcon, holder);
        }
        else
        {
            populateFolder(children, categoryName, (MyViewHolder2) holder);
        }
        if (categoriesToDelete.containsKey(position))
        {
            animateIcon(View.VISIBLE, holder);
        }
        else
        {
            animateIcon(View.INVISIBLE, holder);
        }
//        if (prevPosition  == position)
//        {
//            animateIcon(View.INVISIBLE, holder);
//        }
//        if (currentPosition == position)
//        {
//            animateIcon(View.VISIBLE, holder);
//        }
    }

    public void removeDeletionMarking()
    {
        categoriesToDelete = new HashMap<>();
        notifyDataSetChanged();
//        prevPosition = currentPosition;
//        currentPosition = -1;
//        if (prevPosition > -1)
//        {
//            notifyItemChanged(prevPosition);
//        }
    }
    private void animateIcon(int visibility, MyViewHolder holder) {
        holder.deleteLayout.setVisibility(visibility);
        View view = holder instanceof MyViewHolder1 ? ((MyViewHolder1) holder).categoryIcon : ((MyViewHolder2) holder).folder;
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        if (visibility == INVISIBLE)
        {
            Blurry.delete(viewGroup);
        }
        else
        {
            Blurry.with(activity).radius(16).sampling(1).onto(viewGroup);
        }
    }

    @Override
    public long getItemId(int position) {
        return categoryItems.get(position).hashCode();
    }

    private void populateFolder(List<Category> children, String parentCategoryName, MyViewHolder2 holder) {
        int color = activity.getColor(R.color.white_transparent);
        holder.categoryTxt.setText(parentCategoryName);
        holder.folder.setBackground(Utils.cutCorners(20, dimScreen ? Utils.getBackgroundDimColor(color) : color));
        holder.folder.setOnClickListener(v -> iconClicked(holder, v));
        holder.folder.setOnLongClickListener(view -> markIconForDelete(view, holder));
        holder.folder.setContentDescription(parentCategoryName);
        int i = 0;
        for (Category category : children)
        {
            if (i == holder.folderIcons.size())
            {
                break;
            }
            populateIcon(category, category.getName(), holder.folderIcons.get("icon" + i), holder);
            i++;
        }
    }

    private void populateIcon(Category category, String categoryName, CategoryIcon icon, MyViewHolder holder) {
        int tint = Utils.getContrastTextColor(category.getColor(), activity);
        tint = dimScreen ? Utils.getTintDimColor(activity) : tint;
        int color = dimScreen ? Utils.getBackgroundDimColor(category.getColor()) : category.getColor();
        icon.setParams(category, activity, dimScreen);
        icon.setContentDescription(categoryName);
        icon.setOnClickListener(v -> iconClicked(holder, v));
        icon.setOnLongClickListener(v -> markIconForDelete(v, holder));
        if (category.getIcon() != null)
        {
            icon.setBackground(Utils.cutCorners(icon.getCornerRadius(), color));
            icon.setColorFilter(tint);
            icon.setImageResource(category.getIcon());
        }
    }

    private void iconClicked(MyViewHolder holder, View v) {
        String category = holder.categoryTxt.getText().toString();
        if (categoriesToDelete.containsValue(category))
        {
            deleteItems(holder);
        }
        else if (!categoriesToDelete.isEmpty())
        {
            int position = holder.getAdapterPosition();
            categoriesToDelete.put(position, category);
            notifyItemChanged(position);
        }
        else
        {
            if (holder instanceof MyViewHolder1)
            {
                iconClicked(v);
            }
            else
            {
                folderClicked(v);
            }
        }
    }

    private boolean markIconForDelete(View view, MyViewHolder holder) {
        int position = holder.getAdapterPosition();
        categoriesToDelete.put(position, holder.categoryTxt.getText().toString());
//        if (currentPosition > -1)
//        {
//            prevPosition = currentPosition;
//        }
//        currentPosition = holder.getAdapterPosition();
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.playSequentially(getAnimationSet(view, 1.2f), getAnimationSet(view, 1f));
        scaleSet.start();
        notifyItemChanged(position);
        return true;
    }

    private Animator getAnimationSet(View v, float scale) {
        View p = (View) v.getParent();
        AnimatorSet scaleSet = new AnimatorSet();
        scaleSet.setDuration(200);
        scaleSet.playTogether(getAnimator(v, "scaleX", scale), getAnimator(v, "scaleY", scale),
                getAnimator(p, "scaleX", scale), getAnimator(p, "scaleY", scale));
        return scaleSet;
    }

    private ObjectAnimator getAnimator(View view, String scaleType, float scale){
        return ObjectAnimator.ofFloat(view, scaleType, scale);
    }

    private void iconClicked(View v) {
        dimScreen = true;
        viewBuilder.dimMainScreen(dimScreen);
        activity.dimNavigationView(dimScreen);
        ExpensesListDialog dialog = new ExpensesListDialog(activity, v.getContentDescription().toString());
        dialog.show();
        dialog.setOnDismissListener(d -> {
            dimScreen = false;
            viewBuilder.dimMainScreen(dimScreen);
            activity.dimNavigationView(dimScreen);
        });
    }

    private void folderClicked(View v) {
        dimScreen = true;
        viewBuilder.dimMainScreen(dimScreen);
        ApplicationViewDialogContainer dialog = new ApplicationViewDialogContainer(activity, v.getContentDescription().toString());
        dialog.show();
        dialog.setOnDismissListener(d -> {
            dimScreen = false;
            viewBuilder.dimMainScreen(dimScreen);
        });
    }

    private void deleteItems(MyViewHolder holder)
    {
        DataHelper dataHelper = DataHelper.getDataHelper(activity);
        List<Item> expenses = new ArrayList<>();
        for (String category: categoriesToDelete.values())
        {

            List<String> subcategories = Utils.getCategoriesNames(cat -> cat.getParent().equals(category));
            if (subcategories.isEmpty())
            {
                expenses.addAll(dataHelper.getListOfItems(item -> item.getCategory().equals(category)));
            }
            else
            {
                for (String cat: subcategories)
                {
                    expenses.addAll(dataHelper.getListOfItems(item -> item.getCategory().equals(cat)));
                }
            }
        }
        Html.fromHtml("<h2>Title</h2><br><p>Description here</p>", Html.FROM_HTML_MODE_COMPACT);

        StringBuilder message = new StringBuilder("<p>Are you sure you want to delete the categories below?</p><ul style" +
                "=\"padding-left:40px\">");
        for (String cat : categoriesToDelete.values())
        {
            message.append("<li>&nbsp;&nbsp;").append(cat).append("</li>");
            animateIcon(View.INVISIBLE, holder);
        }
        message.append("</ul>");
        new DeleteMessageDialog(message.toString(), expenses).show();
    }

    private class DeleteMessageDialog extends MessageDialog {
        List<Item> expenses;
        public DeleteMessageDialog(String message, List<Item> expenses) {
            super(DELETE_CATEGORY, WARNING, message, activity);
            this.expenses = expenses;
        }

        @Override
        protected void okPressed() {
            DataHelper.getDataHelper(activity).removeItems(expenses);
            iAction.refreshItems(DELETE_ITEM);
        }
    }
}
