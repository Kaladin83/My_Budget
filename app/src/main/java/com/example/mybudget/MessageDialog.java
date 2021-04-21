package com.example.mybudget;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.mybudget.databinding.MessageDialogBinding;
import com.example.mybudget.utils.Enums.MessageType;
import com.example.mybudget.utils.Utils;

import java.util.Objects;

import static com.example.mybudget.utils.Enums.*;

/**
 * Class creates a dialog with a message
 * A message can be of ERROR, WARNING or INFO types
 */
public class MessageDialog extends Dialog {
    private final String message;
    private final Action action;
    private final Drawable backgroundDrawable;
    private final int strokeColor;
    private final int backgroundColor;
    private int count = 20;
    private MessageDialogBinding bind;

    public MessageDialog(Action action, MessageType messageType, String message, Activity activity) {
        super(activity);
        this.action = action;
        this.message = message;
        int[] backgroundColors;
        switch(messageType)
        {
            case ERROR:
                backgroundColors = Utils.parseColor(Utils.getAttrColor(activity, R.attr.colorRed));
                break;
            case WARNING:
                backgroundColors = Utils.parseColor(Utils.getAttrColor(activity, R.attr.colorYellow));
                break;
            default:
                backgroundColors = Utils.parseColor(Utils.getAttrColor(activity, R.attr.colorBlue));
        }
        backgroundColor = Color.argb(220, backgroundColors[0], backgroundColors[1], backgroundColors[2]);
        this.strokeColor = Utils.getContrastTextColor(backgroundColor, activity);
        backgroundDrawable = AppCompatResources.getDrawable(activity, R.drawable.custom_background_border_none_ellipse_inset);
        Objects.requireNonNull(backgroundDrawable).setColorFilter(backgroundColor, PorterDuff.Mode.SRC);
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = MessageDialogBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(bind.getRoot());

        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(backgroundDrawable);

        bind.txt.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
        bind.txt.setTextColor(strokeColor);
        bind.closeBtn.setColorFilter(strokeColor);
        bind.closeBtn.setOnClickListener(this::closeDialog);
        bind.disableOption.setTextColor(strokeColor);

        if (action == Action.DELETE_CATEGORY){
            bind.dialogOkButton.setText("DELETE");
            bind.dialogOkButton.setOnClickListener(v -> okPressed());
            bind.dialogOkButton.setTextColor(strokeColor);
        }
    }

    protected void okPressed() {

    }

    private void closeDialog(View v) {
        bind.progressBar.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        new Thread(() -> {
            while (count > 0)
            {
                updateLayoutBackground(handler);
                try
                {
                    Thread.sleep(25);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                count--;
            }
            dismiss();
        }).start();
    }

    private void updateLayoutBackground(Handler handler) {
        final int initCount = 20;
        int[] c = Utils.parseColor(backgroundColor);
        int[] c1 = Utils.parseColor(strokeColor);
        int a = c[3] - ((initCount - count) * 10);

        handler.post(() -> {
            bind.txt.setTextColor(Color.argb(a, c1[0], c1[1], c1[2]));
            bind.closeBtn.setColorFilter(Color.argb(a, c1[0], c1[1], c1[2]), PorterDuff.Mode.DST_IN);
            bind.progressBar.getProgressDrawable().setColorFilter(Color.argb(a, c1[0], c1[1], c1[2]), PorterDuff.Mode.DST_IN);
            bind.progressBar.setProgress(count);
            bind.disableOption.setTextColor(Color.argb(a, c1[0], c1[1], c1[2]));
            backgroundDrawable.setColorFilter(Color.argb(a, c[0], c[1], c[2]),  PorterDuff.Mode.DST_IN);
        });
    }
}
