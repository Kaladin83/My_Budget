package com.example.mybudget.components.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.mybudget.domain.domain.Category;
import com.example.mybudget.utils.Utils;

public class CategoryIcon extends AppCompatImageButton {

    private Category category;
    private final Paint rectanglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect txtBounds = new Rect();
    private final Rect rectangleRect = new Rect(0, 0, 0, 0);
    private final Path path = new Path();

    public CategoryIcon(Context context) {
        super(context);
    }

    public CategoryIcon(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryIcon(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getCornerRadius() {
        return getHeight() == 0 ? 20 : getHeight() / 8;
    }

    public void setParams(Category category, Activity activity, boolean transparent) {
        this.category = category;
        configurePaintAttributes(category.getColor(), transparent, activity);
    }

    private void configurePaintAttributes(int categoryColor, boolean transparent, Activity activity) {
        int textColor = transparent? Utils.getTintDimColor(activity): Utils.getContrastTextColor(categoryColor, activity);
        int color = transparent? Utils.getBackgroundDimColor(categoryColor): categoryColor;
        rectanglePaint.setColor(color);
        rectanglePaint.setStyle(Paint.Style.FILL);
        txtPaint.setColor(textColor);
        txtPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (category != null && category.getIcon() == null)
        {
            canvas.save();
            canvas.clipPath(path);
            super.onDraw(canvas);
            int radius = getWidth() / 2;
            String text = Utils.getCategoryInitials(category.getName());
            rectangleRect.bottom = getHeight();
            rectangleRect.right = getWidth();
            txtPaint.setTextSize(getWidth() > 100 ? 80 : 27);
            txtPaint.getTextBounds(text, 0, text.length(), txtBounds);
            canvas.drawRect(rectangleRect, rectanglePaint);
            canvas.drawText(text, (float) (radius - txtBounds.width() / 2 - 2), (float) (radius + txtBounds.height() / 2),
                    txtPaint);
            canvas.restore();
        }
        else
        {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        RectF rect = new RectF(0, 0, w, h);
        path.reset();
        path.addRoundRect(rect, getCornerRadius(), getCornerRadius(), Path.Direction.CW);
        path.close();
    }
}
