package com.example.mybudget.components.item;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.mybudget.R;
import com.example.mybudget.databinding.DescriptionDialogBinding;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.utils.Utils;

import static com.example.mybudget.utils.Enums.Id;


/**
 * Class creates dialog of category delete confirmation.
 * Gives the option to move items to existing category or to move them to new one.
 */
public abstract class DescriptionDialog extends Dialog implements View.OnClickListener {
    private DescriptionDialogBinding bind;
    private final Activity activity;
    private final Item item;

    public DescriptionDialog(Activity activity, Item item) {
        super(activity);
        this.activity = activity;
        this.item = item;
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DescriptionDialogBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(bind.getRoot());

        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Utils.getScreenHeight(activity) / 2.3));
        window.setGravity(Gravity.CENTER);

        TextView title = findViewById(R.id.header_txt);
        title.setText(activity.getString(R.string.description_dialog_title));
        bind.descriptionEdit.setText(item.getDescription());
        bind.descriptionEdit.requestFocus();

        Utils.openKeyboard(activity);

        bind.amountVal.setText(String.valueOf(item.getAmount()));
        bind.categoryVal.setText(item.getCategory());
        bind.dateVal.setText(item.getDate());
        bind.dialogOkButton.setOnClickListener(this);
        bind.dialogCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Utils.closeKeyboard(bind.descriptionEdit, activity);
        switch (Id.getId(v.getId()))
        {
            case DIALOG_OK_BUTTON:
                okButtonPressed(bind.descriptionEdit.getText().toString());
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
