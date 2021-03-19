package com.example.mybudget;

import android.app.Activity;
import android.os.Bundle;
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

import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.common.SimpleSpinnerAdapter;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static android.widget.AdapterView.*;
import static com.example.mybudget.utils.Enums.*;
import static java.util.stream.Collectors.toList;

/**
 * Charts class - create charts
 */

public class Charts extends Fragment implements View.OnClickListener, Constants {
    private View mainView;
    private List<Item> parentItems;
    private List<Item> nonParentItems;
    private PieChart pieChart;
    private BarChart barChart;
    private RadioButton categoryRadio, subCategoryRadio;
    private final Map<Float, Double> mapOfPercents = new HashMap<>();
    private final Map<String, String> monthsMap = JavaUtils.mapOf("01", "JEN", "02", "FEB", "03", "MAR", "04", "APR", "05",
            "MAY", "06", "JUN", "07", "JUL", "08", "AUG", "09", "SEP", "10", "OCT", "11", "NOV", "12", "DEC");
    private ConstraintLayout pieLayout, barLayout;
    private TextView barTxt, pieTxt;
    private Spinner monthSpinner, categorySpinner;
    private Activity activity;
    private DataHelper dataHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.charts, container, false);

        activity = requireActivity();
        dataHelper = DataHelper.getDataHelper(getContext());
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

    private List<Item> getItemsOfNonParents() {
        return dataHelper.getListOfStatistics().stream()
                .filter(s -> !Utils.getParentCategoryName(s.getCategory()).equals(""))
                .filter(s -> !s.getCategory().equalsIgnoreCase("total"))
                .map(s -> new Item.Builder(s.getCategory(), Utils.findMaxDate(s.getCategory()), s.getPayDate())
                        .withAmount(s.getSum())
                        .build())
                .collect(toList());
    }

    private List<String> getCategoryValues() {
        return Stream.concat(nonParentItems.stream(), parentItems.stream())
                .map(Item::getCategory)
                .collect(toList());
    }

    private List<String> getMonthValues() {
        return dataHelper.getListOfMonths().stream()
                .map(d -> monthsMap.get(d.substring(5)) + " " + d.substring(0, 4))
                .collect(toList());
    }

    private List<Item> getItems() {
        return categoryRadio.isChecked()? parentItems : nonParentItems;
    }

    public void refreshCharts() {
        if (mainView != null && !dataHelper.getListOfItems().isEmpty())
        {
            populateDataLayout();
        }
    }

    private void populateDataLayout() {
        parentItems = Utils.getParentStatisticsAsItems();
        nonParentItems = getItemsOfNonParents();

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
        SimpleSpinnerAdapter monthsAdapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, getMonthValues());
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
        SimpleSpinnerAdapter categoryAdapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, getCategoryValues());
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
        pieChart.setDrawEntryLabels(true);
        pieChart.setRotationEnabled(false);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                displayPieData(e, getItems());
            }

            private void displayPieData(Entry e, List<Item> list) {
                Item selected = list.stream()
                        .filter(item -> item.getAmount() == mapOfPercents.get(e.getY()))
                        .findFirst().orElse(null);
                if (selected != null)
                {
                    String str = String.join("", selected.getCategory(), ": ", String.valueOf(selected.getAmount()), " " + "NIS");
                    Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void loadPieData(String selectedMonth) {
        List<Item> items = getItems();
        List<PieEntry> entryY = new ArrayList<>();
        LegendEntry[] lEntries = new LegendEntry[items.size()];
        int[] arrayOfColors = new int[items.size()];

        getPieDataSet(entryY, lEntries, arrayOfColors, items);
        PieDataSet dataSet = new PieDataSet(entryY, "");
        dataSet.setColors(arrayOfColors);
        dataSet.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) ->
                value > 0 ? String.join("", String.valueOf(value).substring(0, String.valueOf(value).indexOf(".") + 2), "%") :
                        "");
        dataSet.setSliceSpace(2);
        dataSet.setValueTextSize(13);
        pieChart.getLegend().setCustom(lEntries);
        pieChart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        pieChart.getLegend().setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        pieChart.getLegend().setTextSize(12);
        pieChart.getDescription().setText(String.join("", "Expenses for ", selectedMonth, " (shown in percents)"));
        pieChart.getDescription().setTextSize(12);
        pieChart.getDescription().setYOffset(-7);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void getPieDataSet(List<PieEntry> entryY, LegendEntry[] lEntries, int[] arrayOfColors, List<Item> items) {
        double totalSum = items.stream()
                .mapToDouble(Item::getAmount)
                .sum();

        String[] arrayOfCategories = new String[items.size()];
        int i = 0;
        for (Item item : items)
        {
            double percent = item.getAmount() / totalSum * 100;
            mapOfPercents.put((float) Math.round(percent), item.getAmount());
            entryY.add(new PieEntry(Math.round(percent)));
            arrayOfColors[i] = Utils.findColor(item.getCategory());
            arrayOfCategories[i] = item.getCategory();
            LegendEntry entry = new LegendEntry();
            entry.label = arrayOfCategories[i];
            entry.formColor = arrayOfColors[i];
            lEntries[i] = entry;
            i++;
        }
    }

    private void createBarChart() {
        barChart = mainView.findViewById(R.id.bar_chart);
        barChart.setDrawValueAboveBar(true);
        barChart.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        barChart.setFitBars(true);
    }

    private void loadBarData(String selectedCategory) {
        List<BarEntry> barEntries = ImmutableList.of(new BarEntry(0, sumOfCategory(selectedCategory)), new BarEntry(1, 120f),
                new BarEntry(2, 100f), new BarEntry(3, 35f), new BarEntry(4, 111f), new BarEntry(5, 78f));

        final String[] dates = new String[]{"Jul 2018", "Aug 2018", "Sep 2018", "Oct 2018", "Nov 2018", "Dec 2018"};

        BarDataSet dataSet = new BarDataSet(barEntries, "data set 1");
        dataSet.setColor(BLUE_3);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barData.setValueTextSize(12);

        if (selectedCategory.equals(getString(R.string.total_sum)))
        {
            dataSet.setColors(categoryColors(cat -> cat.getParent().equals("")));
        }
        else
        {
            int color = Utils.findColor(selectedCategory);
            dataSet.setColor(color);
        }

        barChart.setData(barData);
        barChart.getDescription().setYOffset(-55);
        barChart.getDescription().setXOffset(-25);
        barChart.getDescription().setText(String.join("", "Distribution by dates for ", selectedCategory));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelRotationAngle(45f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAxisLineWidth(1f);
        xAxis.setValueFormatter(new MyXAxisValueFormatter(dates));
    }

    public float sumOfCategory(String category) {
        return (float) dataHelper.getListOfStatistics().stream()
                .filter(s -> s.getCategory().equals(category))
                .mapToDouble(Statistics::getSum)
                .findFirst().orElse(0);
    }

    public int[] categoryColors(Predicate<Category> predicate) {
        return dataHelper.getListOfCategories().stream()
                .filter(predicate)
                .mapToInt(Category::getColor)
                .toArray();
    }

    @Override
    public void onClick(View view) {
        switch (Id.getId(view.getId()))
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
                changeSelection(false, true, "Subcategories");
                loadPieData(monthSpinner.getSelectedItem().toString());
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
