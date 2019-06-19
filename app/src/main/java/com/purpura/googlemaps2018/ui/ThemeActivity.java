package com.purpura.googlemaps2018.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.adapters.ThemeRecyclerAdapter;
import com.purpura.googlemaps2018.models.Themes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ThemeActivity extends AppCompatActivity implements ThemeRecyclerAdapter.ThemeListRecyclerClickListener {
    private ThemeRecyclerAdapter mThemeRecyclerAdapter;
    private RecyclerView mThemeListRecyclerView;
    private ArrayList<String> mThemes;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        mThemes = (ArrayList<String>) Arrays.stream(Themes.values()).map(Enum::toString).collect(Collectors.toList());
        mThemeListRecyclerView = findViewById(R.id.theme_recycler_view);

        initThemeListRecyclerView();
    }


    private void initThemeListRecyclerView() {

        mThemeRecyclerAdapter = new ThemeRecyclerAdapter(mThemes, this);
        mThemeListRecyclerView.setAdapter(mThemeRecyclerAdapter);
        mThemeListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public void onThemeClicked(int position) {
        String themeName = mThemes.get(position);
        String resourceName = String.format("theme_%s", themeName.toLowerCase());
        int themeID = this.getResources().getIdentifier(resourceName, "style", this.getPackageName());
        setTheme(themeID);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("themeName", mThemes.get(position));
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
