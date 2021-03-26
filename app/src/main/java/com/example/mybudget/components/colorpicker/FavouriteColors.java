package com.example.mybudget.components.colorpicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.databinding.FavouriteColorsBinding;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.helpers.RecyclerTouchHelper;
import com.example.mybudget.helpers.ViewsHelper;

import static com.example.mybudget.utils.Enums.Fragment.CATEGORY_PICKER;

/**
 * Displays a list of favourite colors.
 * The user can choose a color, add a color from color picker or remove an existing color.
 */

public class FavouriteColors extends Fragment implements RecyclerTouchHelper.RecyclerItemTouchHelperListener{
    private ColorRecyclerAdapter colorAdapter;
    private DataHelper dataHelper;
    private CategoryPicker categoryPicker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FavouriteColorsBinding bind = FavouriteColorsBinding.inflate(inflater, container, false);
        View mainView = bind.getRoot();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        dataHelper = DataHelper.getDataHelper(getContext());
        categoryPicker = ((CategoryPicker) ViewsHelper.getViewsHelper().getFragment(CATEGORY_PICKER));
        colorAdapter = new ColorRecyclerAdapter(this);
        bind.colorsRecyclerView.setAdapter(colorAdapter);
        bind.colorsRecyclerView.setLayoutManager(layoutManager);
        bind.colorsRecyclerView.setClickable(true);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(bind.colorsRecyclerView);
        return mainView;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ColorRecyclerAdapter.MyViewHolder)
        {
            final Integer deletedItem = dataHelper.getListOfColors().get(position);

            colorAdapter.removeItem(position);
            categoryPicker.restore(deletedItem, null, position, "The color removed from list");
            dataHelper.removeColor(deletedItem);
            showData();
        }
    }

    private void showData() {
        colorAdapter.notifyDataSetChanged();
    }

    public void restoreColor(Integer deletedItem, int deletedIndex) {
        colorAdapter.restoreItem(deletedItem, deletedIndex);
        dataHelper.addColor(deletedItem);
        showData();
    }

    public void refreshColorRecyclerView() {
        showData();
    }

    public void updateColorField(Integer color) {
        categoryPicker.updateColorField(color);
    }
}
