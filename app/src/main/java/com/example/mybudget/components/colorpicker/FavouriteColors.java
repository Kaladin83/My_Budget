package com.example.mybudget.components.colorpicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.R;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.helpers.RecyclerTouchHelper;
import com.example.mybudget.helpers.ViewsHelper;

import static com.example.mybudget.utils.Enums.Fragment.CATEGORY_PICKER;

/**
 * Displays a list of favourite colors.
 * The user can choose a color, add a color from color picker or remove an existing color.
 */

public class FavouriteColors extends Fragment implements RecyclerTouchHelper.RecyclerItemTouchHelperListener,
        View.OnClickListener {
    private ColorRecyclerAdapter colorAdapter;

    private DataHelper dataHelper;
    private CategoryPicker categoryPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.favourite_colors, container, false);



        dataHelper = DataHelper.getDataHelper(getContext());
        categoryPicker = ((CategoryPicker) ViewsHelper.getViewsHelper().getFragment(CATEGORY_PICKER));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        colorAdapter = new ColorRecyclerAdapter(this);
        RecyclerView colorRecyclerView = mainView.findViewById(R.id.colors_recycler_view);
        colorRecyclerView.setAdapter(colorAdapter);
        colorRecyclerView.setLayoutManager(layoutManager);
        colorRecyclerView.setClickable(true);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(colorRecyclerView);
        return mainView;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ColorRecyclerAdapter.MyViewHolder)
        {
            // final Colors deletedItem = MainActivity.getListOfColors().get(position);
            final Integer deletedItem = dataHelper.getListOfColors().get(position);

            colorAdapter.removeItem(position);
            categoryPicker.restoreColorOption(deletedItem, position);
            //   colorAdapter.notifyDataSetChanged();
            //DbHandler.removeColor(MainActivity.getDbInstance(), deletedItem);
            dataHelper.removeColor(deletedItem);
            showData();
        }
    }

    private void showData() {
        colorAdapter.notifyDataSetChanged();
    }

    public void restoreDeletedColor(Integer deletedItem, int deletedIndex) {
        colorAdapter.restoreItem(deletedItem, deletedIndex);
        // DbHandler.populateColor(MainActivity.getDbInstance(), deletedItem);
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
