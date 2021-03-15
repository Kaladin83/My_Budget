package com.example.mybudget.helpers;

import androidx.fragment.app.Fragment;

import com.example.mybudget.enums.Fragments;

import java.util.HashMap;
import java.util.Map;

public class ViewsHelper {
    private final Map<Fragments, Fragment> fragments = new HashMap<>();

    private static ViewsHelper ViewsHelper;

    private ViewsHelper() {
    }

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

    public ViewsHelper registerFragment(Fragments key, Fragment value) {
        fragments.put(key, value);
        return this;
    }

    public Fragment getFragment(Fragments key) {
        return fragments.get(key);
    }
}
