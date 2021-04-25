package com.example.mybudget.components.item;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.State;

import com.example.mybudget.MainActivity;
import com.example.mybudget.R;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Enums;
import com.example.mybudget.utils.Utils;

import java.util.List;
import java.util.function.Predicate;

import static com.example.mybudget.utils.Enums.Action.ADD_ITEM;
import static com.example.mybudget.utils.Enums.DateFormat.PAY;

public class ApplicationViewBuilder{
    private final MainActivity activity;
    private final DataHelper dataHelper;
    private final String category;
    private ItemsRecyclerAppViewAdapter categoryItemAdapter;
    private final StatisticsBuilder statBuilder;
    private RecyclerView recyclerView;

    @SuppressLint("ClickableViewAccessibility")
    public ApplicationViewBuilder(MainActivity mainActivity, ConstraintLayout mainLayout, Window window,
                                  Statistics statistics, String category, Predicate<Category> predicate)  {
        this.activity = mainActivity;
        this.category = category;
        this.dataHelper = DataHelper.getDataHelper(activity);

        GridLayoutManager manager = new GridLayoutManager(activity, category.equals(Utils.TOTAL) ? 4 : 3);
        recyclerView = mainLayout.findViewById(R.id.app_view_recycler);
        populateRecyclerView(Utils.getCategoryItems(predicate));
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new SpacesItemDecoration());
        recyclerView.setItemAnimator(new MyItemAnimator());
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                categoryItemAdapter.removeDeletionMarking();
            }
            return true;
        });

        statBuilder = new StatisticsBuilder(activity, mainLayout, statistics, category);
    }

    public void refreshItems(Enums.Action action) {
        dataHelper.setListOfCategoryItems();
        List<Category> categoryItems = Utils.getCategoryItems(Utils.NO_PARENT_PREDICATE);
        populateRecyclerView(categoryItems);
        MonthlyStatistics statistics = DataHelper.getDataHelper(activity).getMonthlyStatistics(Utils.getCurrentDate(PAY));
        statBuilder.populateStatistics(statistics.getStatistics().get(category));
        if (action == ADD_ITEM)
        {
            //categoryItemAdapter.updateData(categoryItems);
            activity.moveToMainPage();
            //categoryItemAdapter.notifyDataSetChanged();
        }
        //categoryItemAdapter.notifyDataSetChanged();
//        if (action == DELETE_ITEM)
//        {
//            categoryItemAdapter.notifyItemRemoved(3);
//            //categoryItemAdapter.notifyItemChanged(4);
//            ///categoryItemAdapter.notifyItemRangeChanged(4,1);
//        }

    }

    private void populateRecyclerView(List<Category> categoryItems) {
        categoryItemAdapter = new ItemsRecyclerAppViewAdapter(activity, this, categoryItems);
        recyclerView.setAdapter(categoryItemAdapter);
    }

    public void dimMainScreen(boolean dimScreen) {
        categoryItemAdapter.notifyDataSetChanged();
        statBuilder.dimStatisticLayout(dimScreen);
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect rect, @NonNull View view, @NonNull RecyclerView parent, @NonNull State state) {
            rect.bottom = 60;
        }
    }

    private static class MyItemAnimator extends RecyclerView.ItemAnimator {
        @Override
        public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
            return false;
        }

        @Override
        public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo,
                                         @NonNull ItemHolderInfo postLayoutInfo) {
            return false;
        }

        @Override
        public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo,
                                          @NonNull ItemHolderInfo postLayoutInfo) {
            return false;
        }

        @Override
        public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder,
                                     @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
            if (newHolder != null)
            {
                newHolder.itemView.setAlpha(0);
            }
            return true;
        }

        @Override
        public void runPendingAnimations() {

        }

        @Override
        public void endAnimation(@NonNull RecyclerView.ViewHolder item) {

        }

        @Override
        public void endAnimations() {

        }

        @Override
        public boolean isRunning() {
            return false;
        }
    }
}
