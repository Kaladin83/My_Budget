package com.example.mybudget.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mybudget.R;

import java.util.List;

public class SimpleSpinnerAdapter extends ArrayAdapter<String> {

    private final List<String> values;

    public SimpleSpinnerAdapter(Context context, int textViewResourceId, List<String> values) {
        super(context, textViewResourceId, values);
        this.values = values;
    }



    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item, parent, false);
        TextView textView = layout.findViewById(R.id.txtView);
        textView.setText(values.get(position));
        return layout;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
}
