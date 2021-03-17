package com.example.mybudget.components.categorypicker;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.mybudget.R;
import com.example.mybudget.adapters.SimpleSpinnerAdapter;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.utils.JavaUtils;
import com.example.mybudget.utils.Utils;
import com.example.mybudget.utils.Enums.Action;

import java.util.List;

import static com.example.mybudget.utils.Enums.Action.ADD_CATEGORY;
import static com.example.mybudget.utils.Enums.Action.DELETE_CATEGORY;


/**
 * Class creates dialog of category delete confirmation.
 * Gives the option to move items to existing category or to move them to new one.
 */
public abstract class ManageCategoryDialog extends Dialog implements View.OnClickListener, Constants {

    private final Action action;
    private final Activity activity;
    private EditText categoryEdit;
    private Spinner categorySpinner;
    private RadioButton firstChoiceRadio, secondChoiceRadio;

    public ManageCategoryDialog(Activity activity, Action action) {
        super(activity);
        this.activity = activity;
        this.action = action;
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.change_category_dialog);

        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(Utils.getScreenHeight(activity)/2.3));
        window.setGravity(Gravity.CENTER);

        List<String> categories = Utils.getCategoriesNames(cat -> cat.getParent().equals(""));
        SimpleSpinnerAdapter adapter = new SimpleSpinnerAdapter(activity, R.layout.spinner_item, categories);

        TextView headerTxt = findViewById(R.id.header_txt);
        View editPlaceHolder = findViewById(R.id.edit_place_holder);
        Button okButton = findViewById(R.id.ok_button);
        Button cancelButton = findViewById(R.id.cancel_button);
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        firstChoiceRadio = findViewById(R.id.new_category_radio);
        firstChoiceRadio.setChecked(true);
        firstChoiceRadio.setOnClickListener(this);
        secondChoiceRadio = findViewById(R.id.existing_category_radio);
        secondChoiceRadio.setOnClickListener(this);
        categoryEdit = findViewById(R.id.category_edit);
        categorySpinner =  findViewById(R.id.category_spinner);
        categorySpinner.setAdapter(adapter);

        if (action == ADD_CATEGORY)
        {
            categoryEdit.setVisibility(View.INVISIBLE);
            editPlaceHolder.setVisibility(View.VISIBLE);
            headerTxt.setText(R.string.sub_category_layout);
            firstChoiceRadio.setText(R.string.standalone_category);
            secondChoiceRadio.setText(R.string.sub_category);
        }
        else
        {
            categoryEdit.setVisibility(View.VISIBLE);
            editPlaceHolder.setVisibility(View.INVISIBLE);
            headerTxt.setText(R.string.new_category_layout);
            firstChoiceRadio.setText(R.string.new_category);
            secondChoiceRadio.setText(R.string.existing_category);
        }
        enableFields(true, true, false, false);
        JavaUtils.addToMapIds(R.id.new_category_radio, 0, R.id.existing_category_radio, 1, R.id.ok_button, 2, R.id.cancel_button, 3);
    }

    @Override
    public void onClick(View v) {
        int id = JavaUtils.getId(v.getId());
        switch (id) {
            case 0:
                enableFields(true, true, false, false);
                break;
            case 1:
                enableFields(false, false, true, true);
                break;
            case 2:
                getCategory(); break;
            case 3:
                cancelButtonPressed();
        }
    }

    private void getCategory() {
        if (firstChoiceRadio.isChecked())
        {
            if (action == DELETE_CATEGORY)
            {
                if (Utils.validateTextInput(categoryEdit.getText().toString()))
                {
                    okButtonPressed(categoryEdit.getText().toString());
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
            okButtonPressed(categorySpinner.getSelectedItem().toString());
        }
    }

    private void enableFields(boolean b0, boolean b1, boolean b2, boolean b3) {
        categoryEdit.setEnabled(b0);
        firstChoiceRadio.setChecked(b1);
        categorySpinner.setEnabled(b2);
        secondChoiceRadio.setChecked(b3);
    }

    public abstract void okButtonPressed(String categoryName);

    public void cancelButtonPressed() {
        this.dismiss();
    }
}
