package com.example.mybudget.components.item;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mybudget.R;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.utils.Utils;

import static com.example.mybudget.utils.Enums.Id;


/**
 * Class creates dialog of category delete confirmation.
 * Gives the option to move items to existing category or to move them to new one.
 */
public abstract class DescriptionDialog extends Dialog implements View.OnClickListener {
    private final Activity activity;
    private final Item item;
    private EditText descriptionEdit;

    public DescriptionDialog(Activity activity, Item item) {
        super(activity);
        this.activity = activity;
        this.item = item;
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.description_dialog);

        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Utils.getScreenHeight(activity) / 2.3));
        window.setGravity(Gravity.CENTER);

        TextView title = findViewById(R.id.header_txt);
        title.setText("Add/Edit description to your expense");
        descriptionEdit = findViewById(R.id.description_edit);
        descriptionEdit.setText(item.getDescription());
        descriptionEdit.requestFocus();

        Utils.openKeyboard(activity);
        TextView amount = findViewById(R.id.amount_column_val);
        TextView category = findViewById(R.id.category_column_val);
        TextView date = findViewById(R.id.date_column_val);
        amount.setText(String.valueOf(item.getAmount()));
        category.setText(item.getCategory());
        date.setText(item.getDate());
        Button okButton = findViewById(R.id.dialog_ok_button);
        Button cancelButton = findViewById(R.id.dialog_cancel_button);
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

//    @Override
//    public boolean onTouch(View view, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN)
//        {
//            return true;
//        }
//        else if (event.getAction() == MotionEvent.ACTION_UP)
//        {
//            Utils.closeKeyboard(descriptionEdit, activity);
//            view.performClick();
//            return false;
//        }
//        else
//        {
//            return false;
//        }
//    }

    @Override
    public void onClick(View v) {
        Utils.closeKeyboard(descriptionEdit, activity);
        switch (Id.getId(v.getId()))
        {
            case DIALOG_OK_BUTTON:
                okButtonPressed(descriptionEdit.getText().toString());
                break;
            case DIALOG_CANCEL_BUTTON:
                cancelButtonPressed();
        }
    }

    public abstract void okButtonPressed(String description);

    public void cancelButtonPressed() {
        this.dismiss();
    }

}
