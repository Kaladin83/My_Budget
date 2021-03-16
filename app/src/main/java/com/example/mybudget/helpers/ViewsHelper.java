package com.example.mybudget.helpers;

import androidx.fragment.app.Fragment;

import com.example.mybudget.utils.Enums;

import java.util.HashMap;
import java.util.Map;

public class ViewsHelper {
    private final Map<Enums.Fragment, Fragment> fragments = new HashMap<>();

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

    public ViewsHelper registerFragment(Enums.Fragment key, Fragment value) {
        fragments.put(key, value);
        return this;
    }

    public Fragment getFragment(Enums.Fragment key) {
        return fragments.get(key);
    }
}
