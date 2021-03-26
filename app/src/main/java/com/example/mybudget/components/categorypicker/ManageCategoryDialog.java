package com.example.mybudget.components.categorypicker;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;


import com.example.mybudget.R;
import com.example.mybudget.common.SimpleSpinnerAdapter;
import com.example.mybudget.databinding.ChangeCategoryDialogBinding;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.utils.Utils;
import com.example.mybudget.utils.Enums.Action;

import java.util.List;

import static com.example.mybudget.utils.Enums.*;
import static com.example.mybudget.utils.Enums.Action.ADD_CATEGORY;
import static com.example.mybudget.utils.Enums.Action.DELETE_CATEGORY;


/**
 * Class creates dialog of category delete confirmation.
 * Gives the option to move items to existing category or to move them to new one.
 */
public abstract class ManageCategoryDialog extends Dialog implements View.OnClickListener, Constants {
    private ChangeCategoryDialogBinding bind;
    private final Action action;
    private final Activity activity;

    public ManageCategoryDialog(Activity activity, Action action) {
        super(activity);
        this.activity = activity;
        this.action = action;
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ChangeCategoryDialogBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(bind.getRoot());

        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(Utils.getScreenHeight(activity)/2.3));
        window.setGravity(Gravity.CENTER);

        List<String> categories = Utils.getCategoriesNames(cat -> cat.getParent().equals(""));
        SimpleSpinnerAdapter adapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, categories);

        bind.dialogOkButton.setOnClickListener(this);
        bind.dialogCancelButton.setOnClickListener(this);
        bind.newCategoryRadio.setOnClickListener(this);
        bind.existingCategoryRadio.setOnClickListener(this);
        bind.newCategoryRadio.setChecked(true);
        bind.categorySpinner.setAdapter(adapter);

        if (action == ADD_CATEGORY)
        {
            bind.categoryEdit.setVisibility(View.INVISIBLE);
            bind.editPlaceHolder.setVisibility(View.VISIBLE);
            bind.headerTxt.setText(R.string.sub_category_layout);
            bind.newCategoryRadio.setText(R.string.standalone_category);
            bind.existingCategoryRadio.setText(R.string.sub_category);
        }
        else
        {
            bind.categoryEdit.setVisibility(View.VISIBLE);
            bind.editPlaceHolder.setVisibility(View.INVISIBLE);
            bind.headerTxt.setText(R.string.new_category_layout);
            bind.newCategoryRadio.setText(R.string.new_category);
            bind.existingCategoryRadio.setText(R.string.existing_category);
        }
        enableFields(true, true, false, false);
    }

    @Override
    public void onClick(View v) {
        switch (Id.getId(v.getId()))
        {
            case NEW_CATEGORY_RADIO:
                enableFields(true, true, false, false);
                break;
            case EXISTING_CATEGORY_RADIO:
                enableFields(false, false, true, true);
                break;
            case DIALOG_OK_BUTTON:
                getCategory();
                break;
            case DIALOG_CANCEL_BUTTON:
                cancelButtonPressed();
        }
    }

    private void getCategory() {
        if (bind.newCategoryRadio.isChecked())
        {
            if (action == DELETE_CATEGORY)
            {
                if (Utils.validateTextInput(bind.categoryEdit.getText().toString()))
                {
                    okButtonPressed(bind.categoryEdit.getText().toString());
                }
                else
                {
                    Toast.makeText(activity, "The category name must not be empty and must not contain digits", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                okButtonPressed("");
            }
        }
        else
        {
            okButtonPressed(bind.categorySpinner.getSelectedItem().toString());
        }
    }

    private void enableFields(boolean b0, boolean b1, boolean b2, boolean b3) {
        bind.categoryEdit.setEnabled(b0);
        bind.newCategoryRadio.setChecked(b1);
        bind.categorySpinner.setEnabled(b2);
        bind.existingCategoryRadio.setChecked(b3);
    }

    public abstract void okButtonPressed(String categoryName);

    public void cancelButtonPressed() {
        this.dismiss();
    }
}
