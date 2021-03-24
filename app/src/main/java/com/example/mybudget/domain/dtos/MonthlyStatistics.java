package com.example.mybudget.domain.dtos;

import java.util.List;
import java.util.Map;

public class MonthlyStatistics {
    private final Map<String ,Statistics> statistics;
    private final List<String> categoriesWithMinValue;
    private final List<String> categoriesWithMaxValue;

    public MonthlyStatistics(Map<String, Statistics> statistics, List<String> categoriesWithMinValue,
                             List<String> categoriesWithMaxValue) {
        this.statistics = statistics;
        this.categoriesWithMinValue = categoriesWithMinValue;
        this.categoriesWithMaxValue = categoriesWithMaxValue;
    }

    public Map<String ,Statistics> getStatistics() {
        return statistics;
    }

    public List<String> getCategoriesWithMinValue() {
        return categoriesWithMinValue;
    }

    public List<String> getCategoriesWithMaxValue() {
        return categoriesWithMaxValue;
    }
}
