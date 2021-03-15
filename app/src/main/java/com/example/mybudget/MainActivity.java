package com.example.mybudget;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.components.item.ItemRecycler;
import com.example.mybudget.enums.DateFormat;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.Data.Preferences;
import com.example.mybudget.enums.Fragments;
import com.example.mybudget.enums.Mode;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ItemRecycler itemRecycler;
    private ViewPager2 pager;
    private ImageButton categoriesButton, chartsButton, voiceButton, editButton;
    private ActivityResultLauncher<Intent> startActivityForResult;
    private DataHelper dataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setGlobalVariables();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JavaUtils.addToMapIds(R.id.categories_btn, 0, R.id.charts_btn, 1, R.id.voice_btn, 2, R.id.edit_btn, 3,
                R.id.night_theme, 0, R.id.day_theme, 1);

        itemRecycler = new ItemRecycler(this);
        dataHelper = DataHelper.getDataHelper(this);
        //dataHelper.deleteItems(Utils.getCurrentDate(DateFormat.PAY));
        // dataHelper.populateInitialCategories();
        dataHelper.fetchData(Utils.getCurrentDate(DateFormat.PAY));

        handleVoiceCommands();
        createButtons();
        createPager();
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
                            itemRecycler.refreshItems();
                        }
                    }
                }
        );
    }

    private void createPager() {
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        changeSelection(false, false, true, false);

        pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(2, false);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0:
                        changeSelection(true, false, false, false);
                        break;
                    case 1:
                        changeSelection(false, true, false, false);
                        break;
                    case 2:
                        changeSelection(false, false, true, false);
                        break;
                    case 3:
                        changeSelection(false, false, false, true);
                        break;
                }
            }
        });
    }

    private void changeSelection(boolean b, boolean b1, boolean b2, boolean b3) {
        categoriesButton.setSelected(b);
        chartsButton.setSelected(b1);
        voiceButton.setSelected(b2);
        editButton.setSelected(b3);
    }

    public boolean hasNavBar(Resources resources) {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    private void createButtons() {
        categoriesButton = findViewById(R.id.categories_btn);
        categoriesButton.setOnClickListener(this);
        chartsButton = findViewById(R.id.charts_btn);
        chartsButton.setOnClickListener(this);
        voiceButton = findViewById(R.id.voice_btn);
        voiceButton.setOnClickListener(this);
        editButton = findViewById(R.id.edit_btn);
        editButton.setOnClickListener(this);
        ImageButton syncButton = findViewById(R.id.sync_btn);
        syncButton.setOnClickListener(this);
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
        Preferences.putValue("Theme", id == 0 ? Mode.NIGHT_MODE : Mode.DAY_MODE);
        startActivity(new Intent(this, getClass()));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        return true;
    }

    public void moveToMainPage() {
        pager.setCurrentItem(2);
    }

    private class PagerAdapter extends FragmentStateAdapter {
        private final Map<Integer, Fragment> fragments;

        public PagerAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
            CategoryPicker categoryPicker = new CategoryPicker();
            Charts charts = new Charts();
            Edit edit = new Edit();
            ViewsHelper.getViewsHelper().registerFragment(Fragments.CATEGORY_PICKER, categoryPicker)
                    .registerFragment(Fragments.EDIT, edit).registerFragment(Fragments.MAIN_RECYCLER, itemRecycler)
                    .registerFragment(Fragments.CHARTS, charts);
            fragments = JavaUtils.mapOf(categoryPicker, charts, itemRecycler, edit);
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