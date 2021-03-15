package com.example.mybudget.components.categorypicker;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mybudget.R;
import com.example.mybudget.interfaces.Constants;
import com.example.mybudget.utils.Utils;



/**
 * Class creates dialog of category delete confirmation.
 * Gives the option to move items to existing category or to move them to new one.
 */
public abstract class ManageCategoryDialog extends Dialog implements View.OnClickListener, Constants {
    private int width, height;
    private String action;
    private Activity activity;
    private EditText categoryEdit;
    private Spinner categorySpinner;
    private TextView firstChoiceTxt, secondChoiceTxt;
    private RadioButton firstChoiceRadio, secondChoiceRadio;

    public ManageCategoryDialog(Activity activity, int width, int height, String action) {
        super(activity);
        this.height = height;
        this.width = width;
        this.activity = activity;
        this.action = action;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.change_category_dialog);

        int layoutHeight = (int)(this.height/4.2);

        FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(this.width, this.height);
        LinearLayout mainLayout = findViewById(R.id.change_category_layout);
        mainLayout.setLayoutParams(fParams);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(this.width, layoutHeight);
        TextView headerTxt = findViewById(R.id.header_txt);
        headerTxt.setLayoutParams(lParams);
        headerTxt.setGravity(Gravity.CENTER);
        headerTxt.setBackgroundColor(BLUE_6);

        lParams = new LinearLayout.LayoutParams(this.width, layoutHeight);
        LinearLayout newCategoryLayout = findViewById(R.id.new_category_layout);
        newCategoryLayout.setLayoutParams(lParams);
        LinearLayout existingCategoryLayout = findViewById(R.id.existing_category_layout);
        existingCategoryLayout.setLayoutParams(lParams);

        lParams = new LinearLayout.LayoutParams((int)(this.width*0.14), this.height/7);
        lParams.gravity = Gravity.CENTER;
        firstChoiceRadio = findViewById(R.id.new_category_radio);
        firstChoiceRadio.setChecked(true);
        firstChoiceRadio.setLayoutParams(lParams);
        firstChoiceRadio.setOnClickListener(this);
        secondChoiceRadio = findViewById(R.id.existing_category_radio);
        secondChoiceRadio.setLayoutParams(lParams);
        secondChoiceRadio.setOnClickListener(this);
        lParams = new LinearLayout.LayoutParams((int)(this.width*0.48), this.height/6);
        lParams.gravity = Gravity.CENTER;
        firstChoiceTxt = findViewById(R.id.new_category_txt);
        firstChoiceTxt.setLayoutParams(lParams);
        firstChoiceTxt.setGravity(Gravity.CENTER);
        secondChoiceTxt = findViewById(R.id.existing_category_txt);
        secondChoiceTxt.setGravity(Gravity.CENTER);
        secondChoiceTxt.setLayoutParams(lParams);
        
        lParams = new LinearLayout.LayoutParams((int)(this.width*0.35), this.height/6);
        lParams.gravity = Gravity.CENTER;
        categoryEdit = findViewById(R.id.category_edit);
        categoryEdit.setLayoutParams(lParams);
        categoryEdit.setBackground(Utils.createBorder(10, Color.WHITE, 1));
        categorySpinner =  findViewById(R.id.category_spinner);
        categorySpinner.setLayoutParams(lParams);
        /*categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
        fillUpSpinner(categorySpinner);

        lParams = new LinearLayout.LayoutParams(this.width, 3);
        lParams.setMargins(0,0,0,15);
        View separator1 = findViewById(R.id.separator_1);
        separator1.setLayoutParams(lParams);
        separator1.setBackgroundColor(BLUE_2);
        lParams = new LinearLayout.LayoutParams(this.width, 3);
        lParams.setMargins(0,15,0,0);
        View separator2 = findViewById(R.id.separator_2);
        separator2.setLayoutParams(lParams);
        separator2.setBackgroundColor(BLUE_2);
        enableFields(true, true, true, false, false, false);

        lParams = new LinearLayout.LayoutParams(this.width/2-1, layoutHeight);
        Button okButton = findViewById(R.id.ok_button);
        okButton.setLayoutParams(lParams);
        okButton.setOnClickListener(this);
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setLayoutParams(lParams);
        cancelButton.setOnClickListener(this);

        lParams = new LinearLayout.LayoutParams(3, layoutHeight);
        View separator3 = findViewById(R.id.separator_3);
        separator3.setLayoutParams(lParams);
        separator3.setBackgroundColor(BLUE_2);

        if (action.equals("subCategory"))
        {
            categoryEdit.setVisibility(View.GONE);
            headerTxt.setText(R.string.sub_category_layout);
            firstChoiceTxt.setText(R.string.standalone_category);
            secondChoiceTxt.setText(R.string.sub_category);
        }
        else
        {
            categoryEdit.setVisibility(View.VISIBLE);
            headerTxt.setText(R.string.new_category_layout);
            firstChoiceTxt.setText(R.string.new_category);
            secondChoiceTxt.setText(R.string.existing_category);
        }
    }

    private void fillUpSpinner(Spinner spinner){
        String[] categories = Utils.getCategoriesNames(cat -> cat.getParent().equals("")).toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setBackground(Utils.createBorder(10, Color.WHITE, 1));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_category_radio:
                enableFields(true, true, true, false, false, false);
                break;
            case R.id.existing_category_radio:
                enableFields(false, false, false, true, true, true);
                break;
            case R.id.ok_button:
                getCategory(); break;
            case R.id.cancel_button:
                cancelButtonPressed();
        }
    }

    private void getCategory() {
        if (firstChoiceRadio.isChecked())
        {
            if (action.equals("delete"))
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

    private void enableFields(boolean b, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5) {
        firstChoiceTxt.setEnabled(b);
        if (action.equals("delete"))
        {
            categoryEdit.setEnabled(b1);
        }
        firstChoiceRadio.setChecked(b2);
        secondChoiceTxt.setEnabled(b3);
        categorySpinner.setEnabled(b4);
        secondChoiceRadio.setChecked(b5);
    }

    public abstract void okButtonPressed(String categoryName);

    public abstract void cancelButtonPressed();
}
