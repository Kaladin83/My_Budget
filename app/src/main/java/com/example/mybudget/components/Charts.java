package com.example.mybudget.components;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mybudget.R;
import com.example.mybudget.databinding.ChartsBinding;
import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.common.SimpleSpinnerAdapter;
import com.example.mybudget.domain.domain.MonthlyStatistics;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
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
    private ChartsBinding bind;
    private List<String> months;
    private final Map<Float, Double> mapOfPercents = new HashMap<>();
    private final Map<String, String> monthsMap = JavaUtils.mapOf("01", "JEN", "02", "FEB", "03", "MAR", "04", "APR", "05",
            "MAY", "06", "JUN", "07", "JUL", "08", "AUG", "09", "SEP", "10", "OCT", "11", "NOV", "12", "DEC");
    private Activity activity;
    private DataHelper dataHelper;
    private int textColor;
    private String selectedDateDBFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = ChartsBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        activity = requireActivity();
        dataHelper = DataHelper.getDataHelper(getContext());
        textColor = Utils.getAttrColor(activity, android.R.attr.textColorPrimary);
        if (!dataHelper.getListOfItems().isEmpty())
        {
            refreshCharts();
            bind.dataLayout.setVisibility(View.VISIBLE);
            bind.noDataLayout.setVisibility(View.GONE);
        }
        else
        {
            bind.noDataLayout.setVisibility(View.VISIBLE);
            bind.dataLayout.setVisibility(View.GONE);
        }

        return view;
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
        if (bind != null && !dataHelper.getListOfItems().isEmpty())
        {
            populateDataLayout();
        }
    }

    private void populateDataLayout() {
        months = getAllMonthNoDummies();

        bind.allExpensesTxt.setOnClickListener(this);
        bind.monthlyExpensesTxt.setOnClickListener(this);
        bind.categoryRadio.setOnClickListener(this);
        bind.subcategoryRadio.setOnClickListener(this);

        createPieChart();
        createBarChart();
        createMonthSpinner();
        createCategorySpinner();
        changeSelection(true, View.VISIBLE, View.VISIBLE, false, View.GONE, View.GONE);
    }

    private void createMonthSpinner() {
        SimpleSpinnerAdapter monthsAdapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, months);
        bind.monthSpinner.setAdapter(monthsAdapter);
        bind.monthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = bind.monthSpinner.getItemAtPosition(position).toString();
                loadPieData(selectedItem);
                bind.barChart.notifyDataSetChanged();
                bind.barChart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        loadPieData(monthsAdapter.getItem(0));
    }

    private void createCategorySpinner() {
        SimpleSpinnerAdapter categoryAdapter = new
                SimpleSpinnerAdapter(activity, R.layout.spinner_item, getAllCategoriesNamesWithStatistics());
        bind.categorySpinner.setAdapter(categoryAdapter);
        bind.categorySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadBarData(bind.categorySpinner.getSelectedItem().toString());
                bind.barChart.notifyDataSetChanged();
                bind.barChart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        loadBarData(categoryAdapter.getItem(0));
    }

    private void createPieChart() {
        bind.pieChart.setHoleRadius(35);
        bind.pieChart.setCenterTextSize(13);
        bind.pieChart.setTransparentCircleAlpha(50);
        bind.pieChart.setRotationEnabled(false);
        bind.pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                displayPieData(e, Utils.getCategoriesStats(selectedDateDBFormat,
                        cat -> bind.categoryRadio.isChecked() == cat.getParent().equals("")));
            }

            private void displayPieData(Entry e, Map<String, Statistics> stats) {
                Pair<String, Double> selected = stats.entrySet().stream()
                        .filter(stat -> stat.getValue().getSum() == Objects.requireNonNull(mapOfPercents.get(e.getY())))
                        .map(stat -> new Pair<>(stat.getKey(), stat.getValue().getSum()))
                        .findFirst().orElse(null);
                if (selected != null)
                {
                    String str = String.join("", selected.first, "\n", String.valueOf(selected.second), " " + "NIS");
                    bind.pieChart.setCenterText(str);
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
                cat -> bind.categoryRadio.isChecked() == cat.getParent().equals(""));
        List<PieEntry> entryY = new ArrayList<>();
        LegendEntry[] lEntries = getLegendEntries(entryY, stats);

        PieDataSet dataSet = new PieDataSet(entryY, "");
        dataSet.setColors(getDataSetColors(lEntries));
        dataSet.setValueFormatter((new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return value > 0 ? Utils.round(value) + "%" : "";
            }
        }));
        dataSet.setSliceSpace(2);
        dataSet.setValueTextSize(13);
        Legend legend = bind.pieChart.getLegend();
        legend.setCustom(lEntries);
        legend.setTextSize(12);
        legend.setTextColor(textColor);
        setDescription(bind.pieChart.getDescription(), "Expenses for " + selectedMonth + " (shown in percents)", -7, 0);

        bind.pieChart.setData(new PieData(dataSet));
        bind.pieChart.invalidate();
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
        bind.barChart.setFitBars(true);
        bind.barChart.setBorderColor(textColor);
        bind.barChart.setSelected(false);
        bind.barChart.setPinchZoom(false);
        bind.barChart.setDoubleTapToZoomEnabled(false);
        bind.barChart.setVerticalScrollBarEnabled(false);
        bind.barChart.disableScroll();
        bind.barChart.setExtraBottomOffset(20);
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
        bind.barChart.setData(barData);
        dataSet.setFormSize(0);
        setDescription(bind.barChart.getDescription(), "Distribution by dates for " + selectedCategory, -65, -5);
        setAxis(monthsWithDummies);
        bind.barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String tempDate = bind.barChart.getXAxis().getFormattedLabel((int) e.getX()).toUpperCase();
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
        bind.minVal.setText(min);
        bind.maxVal.setText(max);
        bind.avgVal.setText(avg);
        bind.totalVal.setText(total);
    }

    private void setAxis(List<String> monthsWithDummies) {
        final String[] dates = monthsWithDummies.toArray(new String[0]);
        bind.barChart.getAxisLeft().setAxisLineColor(textColor);
        bind.barChart.getAxisLeft().setTextColor(textColor);
        bind.barChart.getAxisRight().setAxisLineColor(textColor);
        bind.barChart.getAxisRight().setTextColor(textColor);

        XAxis xAxis = bind.barChart.getXAxis();
        xAxis.setAxisMaximum(dates.length);
        xAxis.setAxisLineColor(textColor);
        xAxis.setTextColor(textColor);
        xAxis.setLabelRotationAngle(45f);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMaximum(5.5f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return dates[(int) value % dates.length];
            }
        });
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
                changeSelection(true, false);
                loadPieData(bind.monthSpinner.getSelectedItem().toString());
                break;
            case SUBCATEGORY_RADIO:
                if (!Utils.getCategoriesStats(selectedDateDBFormat, cat-> !cat.getParent().equals("")).isEmpty())
                {
                    changeSelection(false, true);
                    loadPieData(bind.monthSpinner.getSelectedItem().toString());
                }
                else
                {
                    changeSelection(true, false);
                    Toast.makeText(activity, "No subcategories exist", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void changeSelection(boolean b, boolean b1) {
        bind.categoryRadio.setChecked(b);
        bind.subcategoryRadio.setChecked(b1);
        bind.pieChart.setCenterText("");
    }

    private void changeSelection(boolean selected1, int visibleChart1, int visibleSpinner1, boolean selected2,
                                 int visibleChart2, int visibleSpinner2) {
        bind.pieLayout.setVisibility(visibleChart1);
        bind.monthSpinner.setVisibility(visibleSpinner1);
        bind.monthlyExpensesTxt.setSelected(selected1);
        bind.barLayout.setVisibility(visibleChart2);
        bind.categorySpinner.setVisibility(visibleSpinner2);
        bind.allExpensesTxt.setSelected(selected2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }
}
