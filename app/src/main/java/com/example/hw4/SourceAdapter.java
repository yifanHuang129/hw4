package com.example.hw4;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;


public class SourceAdapter extends ArrayAdapter<Source> {

    public static class ViewHolder{
        private TextView SourceTextView;
    }

    public SourceAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Source> sources) {

        super(context, resource, sources);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null){
            view = LayoutInflater.from(this.getContext()).inflate(R.layout.drawer_list, parent, false );
            viewHolder = new ViewHolder();
            viewHolder.SourceTextView = (TextView) view.findViewById(R.id.SourceTextView);
            view.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String sports="sports";
        String science="science";
        String health="health";
        String business="business";
        String technology="technology";
        String entertainment="entertainment";
        String general="general";

        if(getItem(position).getCategory().equals(sports)){
            viewHolder.SourceTextView.setTextColor(Color.parseColor("#a9a6e0"));
        }else if(getItem(position).getCategory().equals(science)) {
            viewHolder.SourceTextView.setTextColor(Color.parseColor("#0CB1BB"));
        }else if(getItem(position).getCategory().equals(health)) {
            viewHolder.SourceTextView.setTextColor(Color.parseColor("#8B008B"));
        }else if(getItem(position).getCategory().equals(business)) {
            viewHolder.SourceTextView.setTextColor(Color.parseColor("#008000"));
        }else if(getItem(position).getCategory().equals(technology)) {
            viewHolder.SourceTextView.setTextColor(Color.parseColor("#FF1493"));
        }else if(getItem(position).getCategory().equals(entertainment)) {
            viewHolder.SourceTextView.setTextColor(Color.parseColor("#FF0000"));
        }
        else if(getItem(position).getCategory().equals(general)) {
            viewHolder.SourceTextView.setTextColor(Color.parseColor("#f1b541"));
        }
        viewHolder.SourceTextView.setText(Objects.requireNonNull(getItem(position)).getName());

        return view;
    }
}
