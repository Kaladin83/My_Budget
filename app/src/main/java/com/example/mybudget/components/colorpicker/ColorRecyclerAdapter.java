package com.example.mybudget.components.colorpicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mybudget.R;
import com.example.mybudget.helpers.DataHelper;

public class ColorRecyclerAdapter extends RecyclerView.Adapter<ColorRecyclerAdapter.MyViewHolder> {
    private int selectedPosition = -1;
    private final DataHelper dataHelper;
    private final FavouriteColors favouriteColors;

    public ColorRecyclerAdapter(FavouriteColors favouriteColors)
    {
        this.favouriteColors = favouriteColors;
        dataHelper = DataHelper.getDataHelper(favouriteColors.getContext());
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout objectLayout;
        public RelativeLayout mainLayout;
        public RelativeLayout backgroundLayout;
        private final TextView colorTxt;

        public MyViewHolder(View v) {
            super(v);
            objectLayout = v.findViewById(R.id.color_list_layout);
            mainLayout = v.findViewById(R.id.main_color_layout);
            backgroundLayout = v.findViewById(R.id.background_layout);
            colorTxt = v.findViewById(R.id.color_txt);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_list, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int pos) {
        final int position = pos;

        int color = dataHelper.getListOfColors().get(position);
        holder.colorTxt.setBackgroundColor(color);

        holder.mainLayout.setSelected(selectedPosition == position);
        holder.mainLayout.setId(position);
        holder.mainLayout.setClickable(true);
        holder.mainLayout.setOnClickListener(v -> {
            selectedPosition = position;
            //View view = ((RelativeLayout)v).getChildAt(0);
            favouriteColors.updateColorField(dataHelper.getListOfColors().get(position));
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return dataHelper.getListOfColors().size();
    }

    public void removeItem(int position) {
        dataHelper.getListOfColors().remove(position);
    }

    public void restoreItem(Integer color, int position) {

        dataHelper.getListOfColors().add(position, color);
        notifyItemInserted(position);
    }
}