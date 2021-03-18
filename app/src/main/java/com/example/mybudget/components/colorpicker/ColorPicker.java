package com.example.mybudget.components.colorpicker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.R;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.utils.Enums;

import static com.example.mybudget.utils.Enums.*;
import static com.example.mybudget.utils.Enums.Fragment.CATEGORY_PICKER;

/**
 * Class that allows the user to create its own color
 */

public class ColorPicker extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private int red = 0, green = 0, blue = 0, selectedColor;
    private DataHelper dataHelper;
    private CategoryPicker categoryPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.color_picker, container, false);

        dataHelper = DataHelper.getDataHelper(getContext());
        categoryPicker = ((CategoryPicker) ViewsHelper.getViewsHelper().getFragment(CATEGORY_PICKER));

        SeekBar redSeekBar = mainView.findViewById(R.id.red_bar);
        SeekBar greenSeekBar = mainView.findViewById(R.id.green_bar);
        SeekBar blueSeekBar = mainView.findViewById(R.id.blue_bar);
        redSeekBar.setOnSeekBarChangeListener(this);
        greenSeekBar.setOnSeekBarChangeListener(this);
        blueSeekBar.setOnSeekBarChangeListener(this);

        Button updateColorBtn = mainView.findViewById(R.id.update_color_btn);
        updateColorBtn.setOnClickListener(this);

        return mainView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (Id.getId(seekBar.getId()))
        {
            case SEEK_BAR_RED:
                red = i;
                break;
            case SEEK_BAR_GREEN:
                green = i;
                break;
            default:
                blue = i;
        }
        selectedColor = Color.rgb(red, green, blue);
        categoryPicker.updateColorField(selectedColor);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.update_color_btn)
        {
            boolean ifColorExists = dataHelper.getListOfColors().stream().anyMatch(color -> color == selectedColor);
            if (ifColorExists)
            {
                Toast.makeText(getActivity(), "A color already in the favourites list", Toast.LENGTH_LONG).show();
                return;
            }
            dataHelper.addColor(selectedColor);
            dataHelper.getListOfColors().add(selectedColor);
            categoryPicker.refreshFavouriteColors();

            Toast.makeText(this.getContext(), "The color was added", Toast.LENGTH_SHORT).show();
        }
    }
}
