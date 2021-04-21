package com.example.mybudget.helpers;

import androidx.fragment.app.Fragment;

import com.example.mybudget.R;
import com.example.mybudget.utils.Enums;
import com.example.mybudget.utils.JavaUtils;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewsHelper {
    private final Map<Enums.Fragment, Fragment> fragments = new HashMap<>();
    //private Map<String, Integer> icons = new HashMap<>();

    private static ViewsHelper ViewsHelper;

    public static ViewsHelper getViewsHelper() {
        if (ViewsHelper == null) {
            synchronized (ViewsHelper.class) {
                if (ViewsHelper == null) {
                    ViewsHelper = new ViewsHelper();
                }
            }
        }
        return ViewsHelper;
    }

    public ViewsHelper registerFragment(Enums.Fragment key, Fragment value) {
        fragments.put(key, value);
        return this;
    }

    public Fragment getFragment(Enums.Fragment key) {
        return fragments.get(key);
    }
//
//    public void registerIcons() {
//        List<String> keys = ImmutableList.of("Cafes", "Home", "Car", "Food", "Fuel", "Clothes", "Real Estate", "Pets", "Shopping",
//                "Kids", "Business", "Business Trip", "Vacation", "Transportation", "Fast Food");
//        List<Integer> values = ImmutableList.of(R.drawable.icon_cat_restaurant, R.drawable.icon_cat_home, R.drawable.icon_cat_car,
//                R.drawable.icon_cat_food, R.drawable.icon_cat_fuel, R.drawable.icon_cat_clothes, R.drawable.icon_cat_real,
//                R.drawable.icon_cat_pets, R.drawable.icon_cat_shopping, R.drawable.icon_cat_kids, R.drawable.icon_cat_business,
//                R.drawable.icon_cat_business_trip, R.drawable.icon_cat_vacation, R.drawable.icon_cat_transportation,
//                R.drawable.icon_cat_fast_food);
//        icons = JavaUtils.mapOf(keys, values);
//    }

//    public Integer getImageByCategory(String categoryName) {
//        return icons.get(categoryName);
//    }
}
