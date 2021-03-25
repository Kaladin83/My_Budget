package com.example.mybudget.components;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.mybudget.R;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.common.SimpleSpinnerAdapter;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static android.widget.AdapterView.*;
import static com.example.mybudget.utils.Enums.*;
import static com.github.mikephil.charting.components.Legend.LegendForm.*;
import static java.util.stream.Collectors.toList;

/**
 * Charts class - create charts
 */

public class Charts extends Fragment implements View.OnClickListener, Constants {
    private View mainView;
    private List<String> months;
    private PieChart pieChart;
    private BarChart barChart;
    private RadioButton categoryRadio, subCategoryRadio;
    private final Map<Float, Double> mapOfPercents = new HashMap<>();
    private final Map<String, String> monthsMap = JavaUtils.mapOf("01", "JEN", "02", "FEB", "03", "MAR", "04", "APR", "05",
            "MAY", "06", "JUN", "07", "JUL", "08", "AUG", "09", "SEP", "10", "OCT", "11", "NOV", "12", "DEC");
    private ConstraintLayout pieLayout, barLayout;
    private TextView barTxt, pieTxt, minimumTxt, maximumTxt, averageTxt, totalTxt;
    private Spinner monthSpinner, categorySpinner;
    private Activity activity;
    private DataHelper dataHelper;
    private int textColor;
    private String selectedDateDBFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.charts, container, false);

        activity = requireActivity();
        dataHelper = DataHelper.getDataHelper(getContext());
        textColor = Utils.getAttrColor(activity, android.R.attr.textColorPrimary);
        FrameLayout noDataLayout = mainView.findViewById(R.id.no_data_layout);
        ConstraintLayout dataLayout = mainView.findViewById(R.id.data_layout);
        if (!dataHelper.getListOfItems().isEmpty())
        {
            refreshCharts();
            dataLayout.setVisibility(View.VISIBLE);
            noDataLayout.setVisibility(View.GONE);
        }
        else
        {
            noDataLayout.setVisibility(View.VISIBLE);
            dataLayout.setVisibility(View.GONE);
        }

        return mainView;
    }

    private List<String> getAllCategoriesNamesWithStatistics() {
        MonthlyStatistics monthlyStatistics = dataHelper.getMonthlyStatistics(selectedDateDBFormat);
        if (monthlyStatistics == null)
        {
            return ImmutableList.of();
        }
        return new ArrayList<>(monthlyStatistics.getStatistics().keySet());
    }

    private List<String> getAllMonthsWithDummies() {
        List<String> tempMonths = dataHelper.getListOfMonths();
        return IntStream.range(0, 6)
                .mapToObj(i -> i < tempMonths.size() ? tempMonths.get(i) : getPrevMonth(i, tempMonths.get(0)))
                .map(d -> monthsMap.get(d.substring(5)) + " " + d.substring(0, 4))
                .collect(toList());
    }

    private List<String> getAllMonthNoDummies() {
        return dataHelper.getListOfMonths().stream()
                .map(d -> monthsMap.get(d.substring(5)) + " " + d.substring(0, 4))
                .collect(toList());
    }

    private String getPrevMonth(int index, String firstMonth) {
        int month = Integer.parseInt(firstMonth.substring(5));
        int year = Integer.parseInt(firstMonth.substring(0, 4));
        LocalDate date = LocalDate.of(year, month, 1);
        return Utils.getDate(DateFormat.PAY, date.minusMonths(index));
    }

    public void refreshCharts() {
        if (mainView != null && !dataHelper.getListOfItems().isEmpty())
        {
            populateDataLayout();
        }
    }

    private void populateDataLayout() {
        months = getAllMonthNoDummies();

        barTxt = mainView.findViewById(R.id.all_expenses_txt);
        pieTxt = mainView.findViewById(R.id.monthly_expenses_txt);
        barTxt.setOnClickListener(this);
        pieTxt.setOnClickListener(this);

        categoryRadio = mainView.findViewById(R.id.category_radio);
        subCategoryRadio = mainView.findViewById(R.id.subcategory_radio);
        categoryRadio.setOnClickListener(this);
        subCategoryRadio.setOnClickListener(this);

        createPieChart();
        createBarChart();
        createMonthSpinner();
        createCategorySpinner();

        pieLayout = mainView.findViewById(R.id.pie_layout);
        barLayout = mainView.findViewById(R.id.bar_layout);
        changeSelection(true, View.VISIBLE, View.VISIBLE, false, View.GONE, View.GONE);
    }

    private void createMonthSpinner() {
        SimpleSpinnerAdapter monthsAdapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, months);
        monthSpinner = mainView.findViewById(R.id.pieSpinner);
        monthSpinner.setAdapter(monthsAdapter);
        monthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = monthSpinner.getItemAtPosition(position).toString();
                loadPieData(selectedItem);
                barChart.notifyDataSetChanged();
                barChart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        loadPieData(monthsAdapter.getItem(0));
    }

    private void createCategorySpinner() {
        SimpleSpinnerAdapter categoryAdapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, getAllCategoriesNamesWithStatistics());
        categorySpinner = mainView.findViewById(R.id.columnSpinner);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadBarData(categorySpinner.getSelectedItem().toString());
                barChart.notifyDataSetChanged();
                barChart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        loadBarData(categoryAdapter.getItem(0));
    }

    private void createPieChart() {
        pieChart = mainView.findViewById(R.id.pie_chart);
        pieChart.setCenterText("Category");
        pieChart.setHoleRadius(32);
        pieChart.setCenterTextSize(12);
        pieChart.setTransparentCircleAlpha(50);
        pieChart.setRotationEnabled(false);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                displayPieData(e, Utils.getCategoriesStats(selectedDateDBFormat,
                        cat -> categoryRadio.isChecked() == cat.getParent().equals("")));
            }

            private void displayPieData(Entry e, Map<String, Statistics> stats) {
                Pair<String, Double> selected = stats.entrySet().stream()
                        .filter(stat -> stat.getValue().getSum() == mapOfPercents.get(e.getY()))
                        .map(stat -> new Pair<>(stat.getKey(), stat.getValue().getSum()))
                        .findFirst().orElse(null);
                if (selected != null)
                {
                    String str = String.join("", selected.first, ": ", String.valueOf(selected.second), " " + "NIS");
                    Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void loadPieData(String selectedMonth) {
        selectedDateDBFormat = Utils.getDBMonthFormat(selectedMonth, monthsMap);
        Map<String ,Statistics> stats = Utils.getCategoriesStats(selectedDateDBFormat,
                cat -> categoryRadio.isChecked() == cat.getParent().equals(""));
        List<PieEntry> entryY = new ArrayList<>();
        LegendEntry[] lEntries = getLegendEntries(entryY, stats);

        PieDataSet dataSet = new PieDataSet(entryY, "");
        dataSet.setColors(getDataSetColors(lEntries));
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> value > 0 ? Utils.round(value) + "%" : "");
        dataSet.setSliceSpace(2);
        dataSet.setValueTextSize(13);
        Legend legend = pieChart.getLegend();
        legend.setCustom(lEntries);
        legend.setTextSize(12);
        legend.setTextColor(textColor);
        setDescription(pieChart.getDescription(), "Expenses for " + selectedMonth + " (shown in percents)", -7, 0);

        pieChart.setData(new PieData(dataSet));
        pieChart.invalidate();
    }

    private int[] getDataSetColors(LegendEntry[] lEntries) {
        return Arrays.stream(lEntries)
                .mapToInt(entry -> entry.formColor)
                .toArray();
    }

    private void setDescription(Description description, String text, int yOffset, int xOffset) {
        description.setText(text);
        description.setTextSize(12);
        description.setXOffset(xOffset);
        description.setYOffset(yOffset);
        description.setTextColor(textColor);
    }

    private LegendEntry[] getLegendEntries(List<PieEntry> entryY, Map<String, Statistics> stats) {
        double totalSum = stats.values().stream()
                .mapToDouble(Statistics::getSum)
                .sum();

        Function<Statistics, Double> calculatePercent = item -> item.getSum() / totalSum * 100;
        return stats.entrySet().stream()
                .peek(e -> mapOfPercents.put((float) Math.round(calculatePercent.apply(e.getValue())), e.getValue().getSum()))
                .peek(e -> entryY.add(new PieEntry(Math.round(calculatePercent.apply(e.getValue())))))
                .map(e -> new LegendEntry(e.getKey(), CIRCLE, Float.NaN, Float.NaN, null,
                        Utils.findColor(e.getKey())))
                .toArray(LegendEntry[]::new);
    }

    private void createBarChart() {
        minimumTxt = mainView.findViewById(R.id.min_val);
        maximumTxt = mainView.findViewById(R.id.max_val);
        averageTxt = mainView.findViewById(R.id.avg_val);
        totalTxt = mainView.findViewById(R.id.total_val);
        barChart = mainView.findViewById(R.id.bar_chart);
        barChart.setFitBars(true);
        barChart.setBorderColor(textColor);
        barChart.setSelected(false);
        barChart.setPinchZoom(false);
        barChart.setVerticalScrollBarEnabled(false);
        populateStatisticsTable("", "", "", "");
    }

    private void loadBarData(String selectedCategory) {
        List<String> monthsWithDummies = getAllMonthsWithDummies();
        List<BarEntry> barEntries = IntStream.range(0, monthsWithDummies.size())
                .mapToObj(i -> new BarEntry(i, (float) Utils.getStatsForCategory(Utils.getDBMonthFormat(monthsWithDummies.get(i),
                        monthsMap), selectedCategory).getSum()))
                .collect(toList());
        BarDataSet dataSet = new BarDataSet(barEntries, "");
        dataSet.setHighLightColor(textColor);
        dataSet.setValueTextColor(textColor);
        BarData barData = new BarData(dataSet);
        barData.setValueTextSize(12);
        if (selectedCategory.equals(Utils.TOTAL))
        {
            dataSet.setColors(categoryColors(cat -> cat.getParent().equals("")));
        }
        else
        {
            int color = Utils.findColor(selectedCategory);
            dataSet.setColor(color);
        }
        barChart.setData(barData);
        dataSet.setFormSize(0);
        setDescription(barChart.getDescription(), "Distribution by dates for " + selectedCategory, -65, -25);
        setAxis(monthsWithDummies);
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String tempDate = barChart.getXAxis().getFormattedLabel((int) e.getX()).toUpperCase();
                String date = Utils.getDBMonthFormat(tempDate, monthsMap);
                MonthlyStatistics monthlyStats = dataHelper.getMonthlyStatistics(date);
                if (monthlyStats == null){
                    populateStatisticsTable("", "", "", "");
                }
                else
                {
                    Statistics stats = Objects.requireNonNull(monthlyStats.getStatistics().get(Utils.TOTAL));
                    populateStatisticsTable(stats.getMin() + " [ " + joinCategories(monthlyStats.getCategoriesWithMinValue()) +
                            " ]", stats.getMax() + " [ " + joinCategories(monthlyStats.getCategoriesWithMaxValue()) +
                            " ]", String.valueOf(Utils.round(stats.getAvg())), String.valueOf(stats.getSum()));
                }
            }

            private String joinCategories(List<String> categories) {
                return String.join(", ", categories);
            }

            @Override
            public void onNothingSelected() {
                populateStatisticsTable("", "", "", "");
            }
        });
    }

    private void populateStatisticsTable(String min, String max, String avg, String total) {
        minimumTxt.setText(min);
        maximumTxt.setText(max);
        averageTxt.setText(avg);
        totalTxt.setText(total);
    }

    private void setAxis(List<String> monthsWithDummies) {
        final String[] dates = monthsWithDummies.toArray(new String[0]);
        barChart.getAxisLeft().setAxisLineColor(textColor);
        barChart.getAxisLeft().setTextColor(textColor);
        barChart.getAxisRight().setAxisLineColor(textColor);
        barChart.getAxisRight().setTextColor(textColor);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setAxisMaximum(dates.length);
        xAxis.setAxisLineColor(textColor);
        xAxis.setTextColor(textColor);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(5.5f);
        xAxis.setValueFormatter(new MyXAxisValueFormatter(dates));
    }

    public int[] categoryColors(Predicate<Category> predicate) {
        return dataHelper.getListOfCategories().stream()
                .filter(predicate)
                .mapToInt(Category::getColor)
                .toArray();
    }

    @Override
    public void onClick(View view) {
        switch(Id.getId(view.getId()))
        {
            case MONTHLY_EXPENSES_TXT:
                changeSelection(true, View.VISIBLE, View.VISIBLE, false, View.GONE, View.GONE);
                break;
            case ALL_EXPENSES_TXT:
                changeSelection(false, View.GONE, View.GONE, true, View.VISIBLE, View.VISIBLE);
                break;
            case CATEGORY_RADIO:
                changeSelection(true, false, "Categories");
                loadPieData(monthSpinner.getSelectedItem().toString());
                break;
            case SUBCATEGORY_RADIO:
                if (!Utils.getCategoriesStats(selectedDateDBFormat, cat-> !cat.getParent().equals("")).isEmpty())
                {
                    changeSelection(false, true, "Subcategories");
                    loadPieData(monthSpinner.getSelectedItem().toString());
                }
                else
                {
                    changeSelection(true, false, "Categories");
                    Toast.makeText(activity, "No subcategories exist", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void changeSelection(boolean b, boolean b1, String pieChartHoleText) {
        categoryRadio.setChecked(b);
        subCategoryRadio.setChecked(b1);
        pieChart.setCenterText(pieChartHoleText);
    }

    private void changeSelection(boolean selected1, int visibleChart1, int visibleSpinner1, boolean selected2,
                                 int visibleChart2, int visibleSpinner2) {
        pieLayout.setVisibility(visibleChart1);
        monthSpinner.setVisibility(visibleSpinner1);
        pieTxt.setSelected(selected1);
        barLayout.setVisibility(visibleChart2);
        categorySpinner.setVisibility(visibleSpinner2);
        barTxt.setSelected(selected2);
    }

    public static class MyXAxisValueFormatter implements IAxisValueFormatter {

        private final String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            String month = "";
            if (value >= 0)
            {
                month = mValues[(int) value % mValues.length];
            }
            return month;
        }
    }
}
