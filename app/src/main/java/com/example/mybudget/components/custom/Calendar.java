package com.example.mybudget.components.custom;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mybudget.utils.Utils;

public class Calendar extends Drawable {
    private final String month;
    private final String day;
    private final Paint monthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect monthBounds = new Rect();
    private final Rect dayBounds = new Rect();
    private Rect bounds = new Rect();

    public Calendar(Activity activity, String monthName, String day){
        this.day = day;
        this.month = monthName;
        this.monthPaint.setColor(Color.WHITE);
        this.monthPaint.setTextSize(25);
        this.monthPaint.setStyle(Paint.Style.FILL);
        this.dayPaint.setColor(Utils.getAttrColor(activity, android.R.attr.textColorPrimary));
        this.dayPaint.setTextSize(50);
        this.dayPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        this.bounds = bounds;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int radius = bounds.height() / 2;
        monthPaint.getTextBounds(month, 0, month.length(), monthBounds);
        float x = (float) (radius - monthBounds.width() / 2 - 2);
        canvas.drawText(month, x, (float) (bounds.height() * 0.25), monthPaint);

        dayPaint.getTextBounds(day, 0, day.length(), dayBounds);
        x = (float) (radius - dayBounds.width() / 2 - 2);
        canvas.drawText(day, x, (float) (bounds.height() * 0.75), dayPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }


}
