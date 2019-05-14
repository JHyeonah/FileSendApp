package com.coinshot.filesendapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.coinshot.filesendapp.adapter.AlbumAdapter;
import com.coinshot.filesendapp.model.Album;
import com.coinshot.filesendapp.databinding.ActivityAlbumBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class AlbumActivity extends AppCompatActivity {
    ActivityAlbumBinding bind;
    private SQLiteDatabase db;
    private UpdateDBHelper dbHelper;

    ArrayList<Album> data;
    ArrayList<Album> itemList;
    AlbumAdapter adapter;
    int count;
    int limit;
    int offset;

    final int ROW = 3;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_album);

        dbHelper = new UpdateDBHelper(this);
        db = dbHelper.getWritableDatabase();

        data = new ArrayList<>();
        itemList = new ArrayList<>();

        adapter = new AlbumAdapter(this,data);
        bind.albumRecycler.setAdapter(adapter);
        bind.albumRecycler.addOnScrollListener(recyclerOnScrollListener);
        bind.albumRecycler.setLayoutManager(new GridLayoutManager(this, ROW));

        // RecyclerView 내의 ImageView 높이
        SharedPreferences sp = getSharedPreferences("sp", MODE_PRIVATE);
        float imgHeight = sp.getFloat("height", 0f);

        // 화면 높이
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        float screenHeight = dm.heightPixels * 1.5f;

        // 화면과 이미지 사이즈로 limit 개수 구하기
        int col = (int)(screenHeight / 355.0);
        Log.d("PICTURE", "col : " + String.valueOf(col));

        limit = col * ROW;
        offset = 0;

        // 모든 데이터 개수
        String sql = "select * from picture";
        Cursor cursor = db.rawQuery(sql, null);
        count = cursor.getCount();
        cursor.close();

        getData(limit, offset);

        bind.albumRecycler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("PICTURE", "height: " + bind.albumRecycler.getHeight());
            }
        });

    }

    private RecyclerView.OnScrollListener recyclerOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if(!bind.albumRecycler.canScrollVertically(1)){
                // 스크롤이 바닥까지 내려왔을 때

                if(offset >= count){
                    Toast.makeText(getApplicationContext(), "목록의 끝입니다", Toast.LENGTH_SHORT).show();
                }else{
                    offset += limit;
                    getData(limit, offset);

                    adapter.notifyItemInserted(offset);
                    Log.d("PICTURE", "offset : " + String.valueOf(offset));
                }
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };


    public void getData(int limit, int offset){
        String sql = "select * from picture order by id desc limit " + limit + " offset " + offset + ";";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            String num = String.valueOf(results.getInt(0));
            Album album = new Album(results.getInt(0), results.getString(3), results.getString(1), results.getString(4), results.getString(5), results.getString(2));
            data.add(album);

            results.moveToNext();
        }
        results.close();

    }
}
