package com.example.mybudget;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.domain.domain.Statistics;
import com.example.mybudget.adapters.SimpleSpinnerAdapter;
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

import static com.example.mybudget.utils.Enums.DateFormat.PAY;
import static java.util.stream.Collectors.toList;

/**
 * Charts class - create charts
 */

public class Charts extends Fragment implements View.OnClickListener, Constants {
    private int chartHeight, radiosHeight;
    private View mainView;
    private List<Item> listOfNonParents;
    private PieChart pieChart;
    private BarChart barChart;
    private RadioButton categoryRadio, subCategoryRadio;
    private final Map<String, String> monthsMap = JavaUtils.mapOf("01", "JEN", "02", "FEB", "03", "MAR", "04", "APR", "05",
            "MAY", "06", "JUN", "07", "JUL", "08", "AUG", "09", "SEP", "10", "OCT", "11", "NOV", "12", "DEC");
    private String chosenDate, chosenCategory;
    private RelativeLayout pieLayout, barLayout, pieSpinnerLayout, barSpinnerLayout;
    private TextView barTxt, pieTxt;
    private Map<Float, Double> mapOfPercents = new HashMap<>();

    private Activity activity;
    private DataHelper dataHelper;
    private int screenWidth, logicalDensity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.charts, container, false);

        activity = requireActivity();
        dataHelper = DataHelper.getDataHelper(getContext());
        screenWidth = Utils.getScreenWidth(activity);
        logicalDensity = (int) Utils.getLogicalDensity(activity);
        RelativeLayout noDataLayout = mainView.findViewById(R.id.no_data_layout);
        RelativeLayout dataLayout = mainView.findViewById(R.id.data_layout);
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
        JavaUtils.addToMapIds(R.id.pie_txt, 0, R.id.bar_txt, 1, R.id.category_radio, 2, R.id.subcategory_radio, 3);
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
        return Utils.getParentStatisticsAsItems().stream()
                .map(Item::getCategory)
                .collect(toList());
    }

    private List<String> getMonthValues() {
        return dataHelper.getListOfMonths().stream()
                .map(d -> monthsMap.get(d.substring(5)) + " " + d.substring(0, 4))
                .collect(toList());
    }

    public void refreshCharts() {
        if (mainView != null && !dataHelper.getListOfItems().isEmpty())
        {
            populateDataLayout();
        }
    }

    private void populateDataLayout() {
        String currentPayDate = Utils.getCurrentDate(PAY);
        String month = monthsMap.get(currentPayDate.substring(5, 6));
        String year = currentPayDate.substring(0, 4);
        chosenDate = String.join("", month, " ", year);
        listOfNonParents = getItemsOfNonParents();
        pieSpinnerLayout = mainView.findViewById(R.id.pie_spinner_layout);
        barSpinnerLayout = mainView.findViewById(R.id.bar_spinner_layout);

        barTxt = mainView.findViewById(R.id.bar_txt);
        barTxt.setOnClickListener(this);
        pieTxt = mainView.findViewById(R.id.pie_txt);
        pieTxt.setOnClickListener(this);

        categoryRadio = mainView.findViewById(R.id.category_radio);
        categoryRadio.setOnClickListener(this);
        subCategoryRadio = mainView.findViewById(R.id.subcategory_radio);
        subCategoryRadio.setOnClickListener(this);

        calculateDimensions();
        createMonthSpinner();
        createCategorySpinner();
        createPieChart();
        createBarChart();

        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(screenWidth, chartHeight + radiosHeight);
        rParams.addRule(RelativeLayout.BELOW, R.id.header_layout);
        rParams.setMargins(0, 20, 0, 10);

        pieLayout = mainView.findViewById(R.id.pie_layout);
        barLayout = mainView.findViewById(R.id.bar_layout);
        pieLayout.setLayoutParams(new FrameLayout.LayoutParams(screenWidth, chartHeight + radiosHeight));
        barLayout.setLayoutParams(new FrameLayout.LayoutParams(screenWidth, chartHeight + radiosHeight));
        FrameLayout chartsLayout = mainView.findViewById(R.id.charts_layout);
        chartsLayout.setLayoutParams(rParams);
        changeSelection(true, View.VISIBLE, View.VISIBLE, false, View.GONE, View.GONE);
    }

    private void calculateDimensions() {
        int chartsScreenHeight = Utils.getScreenHeight(activity) - 60 * logicalDensity;
        int topLayoutHeight = 153 * logicalDensity;
        radiosHeight = 40 * logicalDensity;
        chartHeight = chartsScreenHeight - topLayoutHeight - radiosHeight;
    }

    private void createMonthSpinner() {
        SimpleSpinnerAdapter monthsAdapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, getMonthValues());
        Spinner monthSpinner = mainView.findViewById(R.id.pieSpinner);
        monthSpinner.setAdapter(monthsAdapter);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //enableFields(false, false, true,true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void createCategorySpinner() {
        SimpleSpinnerAdapter categoryAdapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, getCategoryValues());
        Spinner categorySpinner = mainView.findViewById(R.id.columnSpinner);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenCategory = adapterView.getSelectedItem().toString();
                loadBarData();
                barChart.notifyDataSetChanged();
                barChart.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        chosenCategory = categorySpinner.getAdapter().getItem(0).toString();
    }

    private void createPieChart() {
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(screenWidth, chartHeight);
        rParams.setMargins(10 * logicalDensity, 0, 10 * logicalDensity, 0);
        rParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        pieChart = mainView.findViewById(R.id.pie_chart);
        pieChart.setCenterText("Pie charts");
        pieChart.setLayoutParams(rParams);
        pieChart.setCenterTextSize(12);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(50);
        pieChart.setDrawEntryLabels(true);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (categoryRadio.isChecked())
                {
                    displayPieData(e, dataHelper.getListOfStatisticItems());
                }
                else
                {
                    displayPieData(e, listOfNonParents);
                }
            }

            private void displayPieData(Entry e, List<Item> list) {
                Item selected = list.stream()
                        .filter(item -> mapOfPercents.containsKey(e.getY()) && item.getAmount() == mapOfPercents.get(e.getY()))
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

        loadPieData(dataHelper.getListOfStatisticItems());
        rParams = new RelativeLayout.LayoutParams(screenWidth, radiosHeight);
        rParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rParams.setMargins(0, 30, 0, 0);
        RelativeLayout radioLayout = mainView.findViewById(R.id.radio_group);
        radioLayout.setLayoutParams(rParams);
        radioLayout.setGravity(Gravity.BOTTOM);
    }

    private void loadPieData(List<Item> items) {
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
        pieChart.getDescription().setText(String.join("", "Expenses for ", chosenDate, " (shown in percents)"));
        pieChart.getDescription().setTextSize(12);
        pieChart.getDescription().setYOffset(-7);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void getPieDataSet(List<PieEntry> entryY, LegendEntry[] lEntries, int[] arrayOfColors, List<Item> items) {
        mapOfPercents = new HashMap<>();
        double totalSum = dataHelper.getListOfStatistics().stream()
                .filter(s -> s.getCategory().equals(getString(R.string.total_sum)))
                .mapToDouble(Statistics::getSum)
                .findFirst().orElse(0);

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
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(screenWidth, chartHeight);
        rParams.setMargins(10 * logicalDensity, 0, 10 * logicalDensity, 0);
        rParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        barChart = mainView.findViewById(R.id.bar_chart);
        barChart.setDrawValueAboveBar(true);
        barChart.setLayoutParams(rParams);
        barChart.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        barChart.setFitBars(true);

        loadBarData();
    }

    private void loadBarData() {
        List<BarEntry> barEntries = ImmutableList.of(new BarEntry(0, sumOfCategory(chosenCategory)), new BarEntry(1, 120f),
                new BarEntry(2, 100f), new BarEntry(3, 35f), new BarEntry(4, 111f), new BarEntry(5, 78f));

        final String[] dates = new String[]{"Jul 2018", "Aug 2018", "Sep 2018", "Oct 2018", "Nov 2018", "Dec 2018"};

        BarDataSet dataSet = new BarDataSet(barEntries, "data set 1");
        dataSet.setColor(BLUE_3);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barData.setValueTextSize(12);

        if (chosenCategory.equals(getString(R.string.total_sum)))
        {
            dataSet.setColors(categoryColors(cat -> cat.getParent().equals("")));
        }
        else
        {
            int color = Utils.findColor(chosenCategory);
            dataSet.setColor(color);
        }

        barChart.setData(barData);
        barChart.getDescription().setYOffset(-55);
        barChart.getDescription().setXOffset(-25);
        barChart.getDescription().setText(String.join("", "Distribution by dates for ", chosenCategory));

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
        switch (JavaUtils.getId(view.getId()))
        {
            case 0:
                changeSelection(true, View.VISIBLE, View.VISIBLE, false, View.GONE, View.GONE);
                break;
            case 1:
                changeSelection(false, View.GONE, View.GONE, true, View.VISIBLE, View.VISIBLE);
                break;
            case 2:
                changeSelection(true, false);
                loadPieData(dataHelper.getListOfStatisticItems());
                break;
            case 3:
                changeSelection(false, true);
                loadPieData(listOfNonParents);
                break;
        }
    }

    private void changeSelection(boolean b, boolean b1) {
        categoryRadio.setChecked(b);
        subCategoryRadio.setChecked(b1);
    }

    private void changeSelection(boolean selected1, int visibleChart1, int visibleSpinner1, boolean selected2,
                                 int visibleChart2, int visibleSpinner2) {
        pieLayout.setVisibility(visibleChart1);
        pieSpinnerLayout.setVisibility(visibleSpinner1);
        pieTxt.setSelected(selected1);
        barLayout.setVisibility(visibleChart2);
        barSpinnerLayout.setVisibility(visibleSpinner2);
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
