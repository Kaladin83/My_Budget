package com.example.mybudget.components.item;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.MainActivity;
import com.example.mybudget.R;
import com.example.mybudget.domain.domain.DayExpense;
import com.example.mybudget.domain.domain.Item;
import com.example.mybudget.utils.Utils;

import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class ExpensesListAdapter extends RecyclerView.Adapter<ExpensesListAdapter.MyViewHolder> {
    private final List<Object> items;
    private final MainActivity activity;

    public ExpensesListAdapter(MainActivity mainActivity, List<Object> items) {
        this.activity = mainActivity;
        this.items = items;
    }

    public void updateData(List<Object> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public static class MyViewHolder extends ViewHolder {
        private MyViewHolder(View v) {
            super(v);
        }
    }

    public static class MyViewHolder1 extends MyViewHolder {
        ConstraintLayout summaryLayout;
        public ConstraintLayout mainExpenseLayout;
        ImageView calendarImg;
        TextView dayTxt;
        TextView totalTxt;

        private MyViewHolder1(View v) {
            super(v);
            summaryLayout = v.findViewById(R.id.summary_layout);
            mainExpenseLayout = v.findViewById(R.id.main_expense_layout);
            calendarImg = v.findViewById(R.id.calendar_img);
            dayTxt = v.findViewById(R.id.day_txt);
            totalTxt = v.findViewById(R.id.total_sum_txt);
        }
    }

    public static class MyViewHolder2 extends MyViewHolder {
        public ConstraintLayout mainExpenseLayout;
        EditText descriptionEdit;
        TextView timeTxt;
        TextView sumTxt;

        private MyViewHolder2(View v) {
            super(v);
            mainExpenseLayout = v.findViewById(R.id.main_expense_layout);
            descriptionEdit = v.findViewById(R.id.description_edit);
            timeTxt = v.findViewById(R.id.time_txt);
            sumTxt = v.findViewById(R.id.expense_sum_txt);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof DayExpense ? 1 : 2;
    }

    @Override
    public int getItemCount() {
     return items.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.expenses_day_summary, parent, false);
            return new MyViewHolder1(v);
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.expenses, parent, false);
            return new MyViewHolder2(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof MyViewHolder1)
        {
            DayExpense dayExpense = (DayExpense) item;
            MyViewHolder1 holder1 = (MyViewHolder1) holder;
            holder1.calendarImg.setImageDrawable(writeOnDrawable(dayExpense.getMonth(), dayExpense.getDay()));
            holder1.dayTxt.setText(dayExpense.getDayName());
            holder1.totalTxt.setText("Total: " + dayExpense.getSum());
        }
        else
        {
            Item expense = (Item) item;
            MyViewHolder2 holder2 = (MyViewHolder2) holder;
            holder2.sumTxt.setText(expense.getAmount() + "");
            String description = expense.getDescription().equals("") ? "PUT YOUR DESCRIPTION HERE" :
                    expense.getDescription();
            int txtColor = expense.getDescription().equals("") ? activity.getColor(R.color.super_light_gray) :
                    Utils.getAttrColor(activity, R.attr.expenseTextColor);
            holder2.descriptionEdit.setText(description);
            holder2.descriptionEdit.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            holder2.descriptionEdit.setTextColor(txtColor);
            holder2.timeTxt.setText(expense.getDate().substring(10));
        }
    }

    public BitmapDrawable writeOnDrawable(String month, String day){
        int drawableId = R.drawable.icon_calendar;
        Rect monthBounds = new Rect();
        Rect dayBounds = new Rect();
        Paint monthPaint = getPaint(Color.WHITE, 40);
        Paint dayPaint = getPaint(Color.BLACK, 85);
        Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bm);
        int radius = bm.getHeight() / 2;

        drawText(canvas, radius, monthPaint, month, monthBounds, (float) (bm.getHeight() * 0.35));
        drawText(canvas, radius, dayPaint, day, dayBounds, (float) (bm.getHeight() * 0.75));
        return new BitmapDrawable(activity.getResources(), bm);
    }

    private void drawText(Canvas canvas, int radius, Paint paint, String text, Rect bounds, float height) {
        paint.getTextBounds(text, 0, text.length(), bounds);
        float x = (float) (radius - bounds.width() / 2 - 10);
        canvas.drawText(text, x, height, paint);
    }

    private Paint getPaint(int color, int textSize) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }
}
