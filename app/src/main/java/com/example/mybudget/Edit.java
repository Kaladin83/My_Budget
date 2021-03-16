package com.example.mybudget;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.example.mybudget.adapters.SimpleSpinnerAdapter;
import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.components.item.ItemRecycler;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.utils.Utils;

import java.util.List;

import static com.example.mybudget.utils.Enums.Fragment.MAIN_RECYCLER;
import static com.example.mybudget.utils.Enums.Fragment.CATEGORY_PICKER;

/**
 * Class that allows user to add expense by writing, and edit existed expenses
 */
public class Edit extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private EditText amountEdit, descriptionEdit;
    private Activity activity;
    private Spinner categorySpinner;
    private SimpleSpinnerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.edit, container, false);
        mainView.setOnTouchListener(this);

        activity = requireActivity();

        amountEdit = mainView.findViewById(R.id.amount_edit);
        descriptionEdit = mainView.findViewById(R.id.description_edit);

        adapter = new SimpleSpinnerAdapter(getActivity(), R.layout.spinner_item, getCategoriesNames());
        categorySpinner = mainView.findViewById(R.id.category_spinner);
        categorySpinner.setAdapter(adapter);

        Button addButton = mainView.findViewById(R.id.add_button);
        addButton.setOnClickListener(this);
        return mainView;
    }

    private List<String> getCategoriesNames() {
        return Utils.getCategoriesNames(Utils.getCategoryNoOtherNamePredicate());
    }

    public void notifyDataChanged(){
        if (adapter != null)
        {
            adapter.clear();
            adapter.addAll(getCategoriesNames());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.add_button)
        {
            DataHelper.getDataHelper(getContext()).addItemFromText(categorySpinner.getSelectedItem().toString(),
                    Double.parseDouble(amountEdit.getText().toString()), descriptionEdit.getText().toString());
            ((ItemRecycler) ViewsHelper.getViewsHelper().getFragment(MAIN_RECYCLER)).refreshItems();
            ((CategoryPicker) ViewsHelper.getViewsHelper().getFragment(CATEGORY_PICKER)).refreshCategories();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            Utils.closeKeyboard(amountEdit, activity);
            Utils.closeKeyboard(descriptionEdit, activity);
            view.performClick();
            return false;
        }
        else
        {
            return false;
        }
    }
}
