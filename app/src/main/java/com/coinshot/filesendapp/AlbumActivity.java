package com.coinshot.filesendapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.coinshot.filesendapp.adapter.AlbumAdapter;
import com.coinshot.filesendapp.model.Album;
import com.coinshot.filesendapp.databinding.ActivityAlbumBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class AlbumActivity extends AppCompatActivity {
    ActivityAlbumBinding bind;
    private SQLiteDatabase db;
    private UpdateDBHelper dbHelper;

    ArrayList<Album> data;
    AlbumAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_album);

        dbHelper = new UpdateDBHelper(this);
        db = dbHelper.getWritableDatabase();

        data = new ArrayList<>();

        String sql = "select * from picture;";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        while(!results.isAfterLast()){
            Log.d("PICTURE", results.getString(3));
            Log.d("PICTURE", "file name : " + results.getString(1));
            String num = String.valueOf(results.getInt(0));
            Album album = new Album(num, results.getString(3), results.getString(1), results.getString(4), results.getString(5), results.getString(2));
            data.add(album);

            results.moveToNext();
        }
        results.close();

        Collections.sort(data);

        adapter = new AlbumAdapter(getApplicationContext(), data);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);

        bind.albumRecycler.setLayoutManager(gridLayoutManager);
        bind.albumRecycler.setAdapter(adapter);

    }

    private RecyclerView.OnScrollListener recyclerOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if(!bind.albumRecycler.canScrollVertically(1)){
                // 스크롤했을때
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };
}
