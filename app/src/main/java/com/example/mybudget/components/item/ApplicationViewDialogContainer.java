package com.example.mybudget.components.item;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.mybudget.MainActivity;
import com.example.mybudget.R;
import com.example.mybudget.databinding.ApplicationViewDialogContainerBinding;
import com.example.mybudget.utils.Utils;

import java.util.Objects;


/**
 * Class creates dialog of category delete confirmation.
 * Gives the option to move items to existing category or to move them to new one.
 */
public class ApplicationViewDialogContainer extends Dialog {
    private final MainActivity mainActivity;
    private final String categoryName;

    public ApplicationViewDialogContainer(MainActivity mainActivity, String categoryName) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.categoryName = categoryName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationViewDialogContainerBinding bind = ApplicationViewDialogContainerBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(bind.getRoot());

        int backgroundColor = mainActivity.getColor(R.color.white_transparent);
        Drawable drawable = AppCompatResources.getDrawable(mainActivity, R.drawable.custom_background_border_none_ellipse_inset);
        Objects.requireNonNull(drawable).setColorFilter(backgroundColor, PorterDuff.Mode.SRC);
        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Utils.getScreenHeight(mainActivity) * 0.53));
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(drawable);

        new ApplicationViewBuilder(mainActivity, bind.getRoot(), window, Utils.getStatsForCategory(categoryName),
                categoryName, cat -> cat.getParent().equals(categoryName));
    }
}
