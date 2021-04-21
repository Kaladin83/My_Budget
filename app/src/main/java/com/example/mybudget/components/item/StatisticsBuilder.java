package com.example.mybudget.components.item;

import android.app.Activity;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.mybudget.MainActivity;
import com.example.mybudget.R;
import com.example.mybudget.domain.domain.CategoryTarget;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.Utils;

import java.util.Objects;

import static com.example.mybudget.utils.Enums.DateFormat.DATE;
import static com.example.mybudget.utils.Enums.DateFormat.PAY;

public class StatisticsBuilder {
    private final DataHelper dataHelper;
    private final String category;
    private final Activity activity;
    private final ConstraintLayout statLayout;
    private final ConstraintLayout expandLayout;
    private final ConstraintLayout showMoreLayout;
    private final ConstraintLayout showLessLayout;
    private final TextView statisticsHeader;
    private final TextView expensesVal;
    private final TextView averageVal;
    private final TextView dailyAverageVal;
    private final TextView expensesTxt;
    private final TextView averageTxt;
    private final TextView dailyAverageTxt;
    private final TextView summaryTxt;
    private final TextView minimumVal;
    private final TextView minimumTxt;
    private final TextView maximumVal;
    private final TextView maximumTxt;
    private final TextView targetVal;
    private final TextView targetTxt;
    private final TextView ratioVal;
    private final TextView ratioTxt;
    private final TextView sumVal;
    private final TextView sumTxt;
    private final TextView lessBtn;
    private final TextView lessTxt;
    private final TextView moreBtn;
    private final TextView moreTxt;
    private final ProgressBar spendProgressBar;

    public StatisticsBuilder(MainActivity activity, ConstraintLayout mainLayout, Statistics statistics, String category) {
        this.activity = activity;
        this.dataHelper = DataHelper.getDataHelper(activity);
        this.category = category;

        statLayout = mainLayout.findViewById(R.id.application_view_stats);
        expandLayout = mainLayout.findViewById(R.id.expandedLayout);
        statisticsHeader = statLayout.findViewById(R.id.statistics_header);
        expensesVal = statLayout.findViewById(R.id.expenses_val);
        averageVal = statLayout.findViewById(R.id.average_val);
        dailyAverageVal = statLayout.findViewById(R.id.daily_average_val);
        expensesTxt = statLayout.findViewById(R.id.expenses_txt);
        averageTxt = statLayout.findViewById(R.id.average_txt);
        dailyAverageTxt = statLayout.findViewById(R.id.daily_average_txt);
        summaryTxt = statLayout.findViewById(R.id.summary_txt);
        spendProgressBar = statLayout.findViewById(R.id.spend_progress_bar);

        maximumTxt = statLayout.findViewById(R.id.maximum_sum_txt);
        maximumVal = statLayout.findViewById(R.id.maximum_sum_val);
        minimumTxt = statLayout.findViewById(R.id.minimum_sum_txt);
        minimumVal = statLayout.findViewById(R.id.minimum_sum_val);
        targetTxt = statLayout.findViewById(R.id.target_txt);
        targetVal = statLayout.findViewById(R.id.target_val);
        ratioTxt = statLayout.findViewById(R.id.ratio_txt);
        ratioVal = statLayout.findViewById(R.id.ratio_val);
        sumTxt = statLayout.findViewById(R.id.sum_txt);
        sumVal = statLayout.findViewById(R.id.sum_val);
        lessBtn = statLayout.findViewById(R.id.less_btn);
        lessTxt = statLayout.findViewById(R.id.less_txt);
        moreBtn = statLayout.findViewById(R.id.more_btn);
        moreTxt = statLayout.findViewById(R.id.more_txt);

        showMoreLayout = mainLayout.findViewById(R.id.more_layout);
        showLessLayout = mainLayout.findViewById(R.id.less_layout);

        if (statistics != null)
        {
            populateStatistics(statistics);
        }
        showMoreLayout.setOnClickListener(v -> expandStatistics(View.VISIBLE));
        showLessLayout.setOnClickListener(v -> expandStatistics(View.GONE));
    }

    private void expandStatistics(int visibility) {
        showMoreLayout.setVisibility(visibility == View.VISIBLE? View.GONE: View.VISIBLE);
        showLessLayout.setVisibility(visibility);
        expandLayout.setVisibility(visibility);
    }

    public void populateStatistics(Statistics statistics) {
        statLayout.setVisibility(View.VISIBLE);
        Statistics stats = Objects.requireNonNull(statistics);
        CategoryTarget target = dataHelper.getTargetPerCategory(category);
        int tSum = target.getSum();
        double sSum = stats.getSum();
        int color = getColor(stats, tSum);
        double dailyAvg = Utils.round(sSum / Integer.parseInt(Utils.getCurrentDate(DATE).substring(8)));
        String summary = tSum == 0 ? activity.getString(R.string.statistics_summary_no_target, sSum + "") : tSum < sSum ?
                activity.getString(R.string.statistics_summary_exceed, tSum, (int) (sSum - tSum)) :
                activity.getString(R.string.statistics_summary, sSum + "", tSum);
        summaryTxt.setTextColor(color);

        statisticsHeader.setText(getStatisticsHeaderMessage());
        expensesVal.setText(String.valueOf(stats.getCnt()));
        averageVal.setText(String.valueOf(stats.getAvg()));
        dailyAverageVal.setText(String.valueOf(dailyAvg));
        sumVal.setText(String.valueOf(sSum));
        targetVal.setText(String.valueOf(tSum));
        ratioVal.setText(tSum == 0 ? "N/A" : String.valueOf(Utils.round(sSum / tSum)));
        minimumVal.setText(String.valueOf(stats.getMin()));
        maximumVal.setText(String.valueOf(stats.getMax()));
        summaryTxt.setText(summary);
        spendProgressBar.setMax(tSum);
        spendProgressBar.setProgress((int) sSum);
        spendProgressBar.getProgressDrawable().setColorFilter(new LightingColorFilter(1, color));
//        new MessageDialog(INFO, "Your target plan for " + Utils.TOTAL + " is for " + displayDate +
//                ".\nIt's recommended to update it monthly.", activity).show();
//        new MessageDialog(WARNING, "You don't have a target plan for " + Utils.TOTAL +
//                ".\nIt's recommended to have one for each category.", activity).show();
    }


    private String getStatisticsHeaderMessage() {
        if (category.equals(Utils.TOTAL))
        {
            String date = Utils.getCurrentDate(PAY);
            String month = DataHelper.getDataHelper(activity).getMonthNames(date.substring(5)).getFullName();
            String year = date.substring(0, 4);
            return activity.getString(R.string.statistics, month + " " + year);
        }
        return activity.getString(R.string.statistics, category);
    }

    private int getColor(Statistics stats, Integer target) {
        double sum = stats.getSum();
        int bound = target / 3;
        return sum < bound || target == 0 ? Utils.getAttrColor(activity, R.attr.colorGreen) : sum >= bound && sum < bound * 2 ?
                Utils.getAttrColor(activity, R.attr.colorYellow) : Utils.getAttrColor(activity, R.attr.colorRed);
    }

    public void dimStatisticLayout(boolean dimScreen) {
        MonthlyStatistics statistics = DataHelper.getDataHelper(activity).getMonthlyStatistics(Utils.getCurrentDate(PAY));
        Statistics stats = statistics.getStatistics().get(category);
        int tintTxtColor = Utils.getTintDimColor(activity);
        int headerColor = Utils.getAttrColor(activity, R.attr.colorSecondary);
        int txtColor = Utils.getAttrColor(activity, android.R.attr.textColorPrimary);
        int finalTxtColor = dimScreen? tintTxtColor : txtColor;
        int summaryColorTmp = getColor(stats, dataHelper.getTargetPerCategory(category).getSum());
        int finalHeaderColor = dimScreen? Utils.getBackgroundDimColor(headerColor): headerColor;
        int summaryColor = dimScreen? Utils.getBackgroundDimColor(summaryColorTmp): summaryColorTmp;
        ColorFilter cf = dimScreen? new PorterDuffColorFilter(summaryColor, PorterDuff.Mode.SRC_OUT) :
                new LightingColorFilter(1, summaryColor);
        statisticsHeader.setTextColor(finalHeaderColor);
        lessTxt.setTextColor(finalHeaderColor);
        lessBtn.setBackgroundTintMode(dimScreen? PorterDuff.Mode.DST_OUT: PorterDuff.Mode.SRC_ATOP);
        moreTxt.setTextColor(finalHeaderColor);
        moreBtn.setBackgroundTintMode(dimScreen? PorterDuff.Mode.DST_OUT: PorterDuff.Mode.SRC_ATOP);
        expensesVal.setTextColor(finalTxtColor);
        expensesTxt.setTextColor(finalTxtColor);
        averageTxt.setTextColor(finalTxtColor);
        averageVal.setTextColor(finalTxtColor);
        dailyAverageTxt.setTextColor(finalTxtColor);
        dailyAverageVal.setTextColor(finalTxtColor);
        minimumTxt.setTextColor(finalTxtColor);
        minimumVal.setTextColor(finalTxtColor);
        maximumTxt.setTextColor(finalTxtColor);
        maximumVal.setTextColor(finalTxtColor);
        targetTxt.setTextColor(finalTxtColor);
        targetVal.setTextColor(finalTxtColor);
        sumTxt.setTextColor(finalTxtColor);
        sumVal.setTextColor(finalTxtColor);
        ratioTxt.setTextColor(finalTxtColor);
        ratioVal.setTextColor(finalTxtColor);
        summaryTxt.setTextColor(summaryColor);
        spendProgressBar.getProgressDrawable().setColorFilter(cf);
    }
}
