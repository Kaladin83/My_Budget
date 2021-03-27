package com.example.mybudget.components.colorpicker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.R;
import com.example.mybudget.databinding.ColorPickerBinding;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.helpers.ViewsHelper;

import static com.example.mybudget.utils.Enums.*;
import static com.example.mybudget.utils.Enums.Fragment.CATEGORY_PICKER;

/**
 * Class that allows the user to create its own color
 */

public class ColorPicker extends Fragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private int red = 0, green = 0, blue = 0, selectedColor;
    private DataHelper dataHelper;
    private CategoryPicker categoryPicker;
    private ColorPickerBinding bind;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = ColorPickerBinding.inflate(inflater, container, false);
        View mainView = bind.getRoot();
        dataHelper = DataHelper.getDataHelper(getContext());
        categoryPicker = ((CategoryPicker) ViewsHelper.getViewsHelper().getFragment(CATEGORY_PICKER));

        bind.redBar.setOnSeekBarChangeListener(this);
        bind.greenBar.setOnSeekBarChangeListener(this);
        bind.blueBar.setOnSeekBarChangeListener(this);
        bind.updateColorBtn.setOnClickListener(this);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }
}
