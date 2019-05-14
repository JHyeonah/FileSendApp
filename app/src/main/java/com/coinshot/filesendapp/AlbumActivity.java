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
    int start = 0;
    int max = 0;
    final int PAGE = 4;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_album);

        dbHelper = new UpdateDBHelper(this);
        db = dbHelper.getWritableDatabase();

        data = new ArrayList<>();
        itemList = new ArrayList<>();

        String sql = "select * from picture;";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        while(!results.isAfterLast()){
            String num = String.valueOf(results.getInt(0));
            Album album = new Album(results.getInt(0), results.getString(3), results.getString(1), results.getString(4), results.getString(5), results.getString(2));
            data.add(album);
            Collections.sort(data, myComparator);

            results.moveToNext();
        }
        results.close();
        Collections.sort(data, myComparator);

        max = data.size();

        for(int i =0; i< max; i++){
            Log.d("PICTURE", String.valueOf(data.get(i).getNum()));
        }

        if(max < PAGE){
            for(int i=0; i<max; i++){
                itemList.add(data.get(i));
            }
        }else{
            for(int i=0; i<PAGE; i++){
                itemList.add(data.get(i));
            }
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);


        bind.albumRecycler.addOnScrollListener(recyclerOnScrollListener);
        bind.albumRecycler.setLayoutManager(gridLayoutManager);
        adapter = new AlbumAdapter(getApplicationContext(),itemList);
        bind.albumRecycler.setAdapter(adapter);

    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        Collections.sort(data);
        Collections.sort(itemList);
        adapter.notifyDataSetChanged();
    }
*/
    private RecyclerView.OnScrollListener recyclerOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            ArrayList<Album> tempoList = new ArrayList<>();
            tempoList.clear();
            tempoList.addAll(itemList);

            if(!bind.albumRecycler.canScrollVertically(1)){
                // 스크롤이 바닥까지 내려왔을 때
                int start = adapter.getItemCount();
                Log.d("PICTURE", "start : " + String.valueOf(start));
                Log.d("PICTURE", "max :  " + String.valueOf(max));
                int end = start+PAGE-1;
                Log.d("PICTURE", "end : " + String.valueOf(end));

                if(start >= max-1){
                    Toast.makeText(getApplicationContext(), "목록의 끝입니다", Toast.LENGTH_SHORT).show();
                }else if(end >= max){
                    for(int i = start; i < max; i++){
                        tempoList.add(data.get(i));
                        itemList.clear();
                        itemList.addAll(tempoList);
                        adapter.notifyDataSetChanged();
                    }
                }else{
                    for(int i = start; i <= end; i++) {
                        tempoList.add(data.get(i));
                        itemList.clear();
                        itemList.addAll(tempoList);
                        adapter.notifyDataSetChanged();
                    }
                }


            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    private final static Comparator<Album> myComparator = new Comparator<Album>() {
        @Override
        public int compare(Album o1, Album o2) {
            return o2.getNum()-o1.getNum();
        }
    };
}
