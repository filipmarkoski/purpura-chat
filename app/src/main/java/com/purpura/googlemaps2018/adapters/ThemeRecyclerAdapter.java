package com.purpura.googlemaps2018.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.purpura.googlemaps2018.R;

import java.util.ArrayList;

public class ThemeRecyclerAdapter extends RecyclerView.Adapter<ThemeRecyclerAdapter.ViewHolder> {

    private ArrayList<String> mThemes = new ArrayList<>();
    private ThemeListRecyclerClickListener mClickListener;


    public ThemeRecyclerAdapter(ArrayList<String> mThemes, ThemeListRecyclerClickListener clickListener) {
        this.mThemes = mThemes;
        mClickListener = clickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_theme_list_item, parent, false);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ((ViewHolder) holder).name.setText(mThemes.get(position));

    }

    @Override
    public int getItemCount() {
        return mThemes.size();
    }

    public interface ThemeListRecyclerClickListener {
        void onThemeClicked(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ThemeListRecyclerClickListener mClickListener;

        public ViewHolder(View itemView, ThemeListRecyclerClickListener clickListener) {
            super(itemView);
            name = itemView.findViewById(R.id.theme_name);
            mClickListener = clickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickListener.onThemeClicked(getAdapterPosition());
        }
    }
}

















