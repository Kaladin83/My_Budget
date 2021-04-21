package com.example.mybudget;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mybudget.components.Charts;
import com.example.mybudget.components.Edit;
import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.components.item.ApplicationViewMainContainer;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.Data.Preferences;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.mybudget.utils.Enums.Mode;
import static com.example.mybudget.utils.Enums.DateFormat.PAY;
import static com.example.mybudget.utils.Enums.Fragment.CATEGORY_PICKER;
import static com.example.mybudget.utils.Enums.Fragment.EDIT;
import static com.example.mybudget.utils.Enums.Fragment.MAIN_RECYCLER;
import static com.example.mybudget.utils.Enums.Fragment.CHARTS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ViewPager2 pager;
    private ActivityResultLauncher<Intent> startActivityForResult;
    private DataHelper dataHelper;
    private BottomNavigationView navigationView;
    private ApplicationViewMainContainer appView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setGlobalVariables();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JavaUtils.addToMapIds(R.id.edit_category, 0, R.id.view_charts, 1, R.id.view_entries, 2, R.id.add_entry, 3,
                R.id.share_entries, 4, R.id.night_theme, 0, R.id.day_theme, 1);
        dataHelper = DataHelper.getDataHelper(this);
       //dataHelper.deleteItems(Utils.getCurrentDate(PAY));
       //dataHelper.populateInitialCategories(this);
        dataHelper.fetchData(Utils.getCurrentDate(PAY));
        //Map<String, Integer> target =
        //        ImmutableMap.of(Utils.TOTAL, 1500, "Cafes", 350, "Home", 200, "Car", 500, "Clothes", 250);
        //dataHelper.updateTarget(target, Utils.getCurrentDate(PAY));
        appView = new ApplicationViewMainContainer(this);
        navigationView = findViewById(R.id.bottom_navigation_view);
        dimNavigationView(false);
        handleVoiceCommands();
        createPager();
        navigationView.setOnNavigationItemSelectedListener(this::onItemSelected);
    }

    public ConstraintLayout getRoot(){
        return findViewById(R.id.main_layout);
    }

    private boolean onItemSelected(MenuItem item) {
        Integer tabNumber = JavaUtils.getId(item.getItemId());
        if (tabNumber != null)
        {
            pager.setCurrentItem(tabNumber);
//            if (tabNumber == 2 && pager.getCurrentItem() == 2)
//            {
//                startVoiceRecognitionActivity();
//            }
//            else
//            {
//                pager.setCurrentItem(tabNumber);
//            }
            return true;
        }
        return false;
    }

    private void setGlobalVariables() {
        Preferences.setPreferences(this);
        Utils.setApplicationContext(this);
        setTheme(Preferences.getValue("Theme", Mode.class) == Mode.DAY_MODE ? R.style.DayTheme : R.style.NightTheme);
    }

    private void handleVoiceCommands() {
        startActivityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK)
                    {
                        Intent data = result.getData();
                        if (data != null)
                        {
                            List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            dataHelper.addItemFromVoice(matches);
                            //itemRecycler.refreshItems(ADD_ITEM);
                        }
                    }
                }
        );
    }

    private void createPager() {
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        changeSelection(2);
        Integer currentPage = Preferences.getValue("CurrentPage", Integer.class);

        pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(currentPage == null ? 2 : currentPage, false);
        setRefreshPage(2);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                changeSelection(position);
            }
        });
    }

    private void setRefreshPage(int pageNumber) {
        Preferences.putValue("CurrentPage", pageNumber);
    }

    private void changeSelection(int position) {
        View v = ((BottomNavigationMenuView) navigationView.getChildAt(0)).getChildAt(position);
        navigationView.setSelectedItemId(v.getId());
    }

    public boolean hasNavBar(Resources resources) {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    public void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech recognition demo");
        startActivityForResult.launch(intent);
    }

    @Override
    public void onClick(View view) {
        Integer tabNumber = JavaUtils.getId(view.getId());
        if (tabNumber != null)
        {
            if (tabNumber == 2 && pager.getCurrentItem() == 2)
            {
                startVoiceRecognitionActivity();
            }
            else
            {
                pager.setCurrentItem(tabNumber);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = JavaUtils.getId(item.getItemId());
        if (id == null)
        {
            return super.onOptionsItemSelected(item);
        }
        setRefreshPage(pager.getCurrentItem());
        Preferences.putValue("Theme", id == 0 ? Mode.NIGHT_MODE : Mode.DAY_MODE);
        startActivity(new Intent(this, getClass()));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        return true;
    }

    public void moveToMainPage() {
        pager.setCurrentItem(2);
    }

    public void dimNavigationView(boolean dimScreen) {
        int selected = Utils.getAttrColor(this, R.attr.colorSecondary);
        int pressed = ContextCompat.getColor(this, R.color.light_gray);
        int notSelected = Utils.getAttrColor(this, R.attr.colorOnSurface);

        int tintSelected = dimScreen ? Utils.getBackgroundDimColor(selected) : selected;
        int tintPressed = dimScreen ? Utils.getBackgroundDimColor(pressed) : pressed;
        int tintNotSelected = dimScreen ? Utils.getBackgroundDimColor(notSelected) : notSelected;

        int[][] states = {{android.R.attr.state_selected}, {android.R.attr.state_pressed}, {-android.R.attr.state_selected}};
        int[] colors = {tintSelected, tintPressed, tintNotSelected};

        navigationView.setItemIconTintList(new ColorStateList(states, colors));
        navigationView.setItemTextColor(new ColorStateList(states, colors));
    }

    private class PagerAdapter extends FragmentStateAdapter {
        private final Map<Integer, Fragment> fragments;

        public PagerAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
            CategoryPicker categoryPicker = new CategoryPicker();
            Charts charts = new Charts();
            Edit edit = new Edit();

            ViewsHelper.getViewsHelper().registerFragment(CATEGORY_PICKER, categoryPicker)
                    .registerFragment(EDIT, edit).registerFragment(MAIN_RECYCLER, appView)
                    .registerFragment(CHARTS, charts);
            fragments = JavaUtils.mapOf(categoryPicker, charts, appView, edit);
//            ViewsHelper.getViewsHelper().registerFragment(CATEGORY_PICKER, categoryPicker)
//                    .registerFragment(EDIT, edit).registerFragment(MAIN_RECYCLER, itemRecycler)
//                    .registerFragment(CHARTS, charts);
//            fragments = JavaUtils.mapOf(categoryPicker, charts, itemRecycler, edit);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return Objects.requireNonNull(fragments.get(position));
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}