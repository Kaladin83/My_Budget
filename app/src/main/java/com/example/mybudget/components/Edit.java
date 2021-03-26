package com.example.mybudget.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mybudget.R;
import com.example.mybudget.common.SimpleSpinnerAdapter;
import com.example.mybudget.components.categorypicker.CategoryPicker;
import com.example.mybudget.components.item.ItemRecycler;
import com.example.mybudget.databinding.EditBinding;
import com.example.mybudget.helpers.DataHelper;
import com.example.mybudget.helpers.ViewsHelper;
import com.example.mybudget.utils.Utils;

import java.util.List;

import static com.example.mybudget.utils.Enums.Action.*;
import static com.example.mybudget.utils.Enums.Fragment.MAIN_RECYCLER;
import static com.example.mybudget.utils.Enums.Fragment.CATEGORY_PICKER;

/**
 * Class that allows user to add expense by writing, and edit existed expenses
 */
public class Edit extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private EditBinding bind;
    private Activity activity;
    private SimpleSpinnerAdapter adapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = EditBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        view.setOnTouchListener(this);

        activity = requireActivity();
        adapter = new SimpleSpinnerAdapter(getActivity(), R.layout.spinner_item, getCategoriesNames());
        bind.categorySpinner.setAdapter(adapter);
        bind.addButton.setOnClickListener(this);
        return view;
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
            if (bind.amountEdit.getText().toString().equals(""))
            {
                Toast.makeText(activity, "The Amount must not be empty", Toast.LENGTH_SHORT).show();
            }
            else
            {
                DataHelper.getDataHelper(getContext()).addItemFromText(bind.categorySpinner.getSelectedItem().toString(),
                        Double.parseDouble(bind.amountEdit.getText().toString()), bind.descriptionEdit.getText().toString());
                ((ItemRecycler) ViewsHelper.getViewsHelper().getFragment(MAIN_RECYCLER)).refreshItems(ADD_ITEM);
                ((CategoryPicker) ViewsHelper.getViewsHelper().getFragment(CATEGORY_PICKER)).refreshCategories();
                clearFields();
            }
        }
    }

    private void clearFields() {
        bind.amountEdit.setText("");
        bind.descriptionEdit.setText("");
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            Utils.closeKeyboard(bind.amountEdit, activity);
            Utils.closeKeyboard(bind.descriptionEdit, activity);
            view.performClick();
            return false;
        }
        else
        {
            return false;
        }
    }
}
