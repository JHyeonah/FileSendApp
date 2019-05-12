package com.coinshot.filesendapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.coinshot.filesendapp.databinding.ActivityAlbumBinding;

public class AlbumActivity extends AppCompatActivity {
    ActivityAlbumBinding bind;
    private SQLiteDatabase db;
    private UpdateDBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_album);

        dbHelper = new UpdateDBHelper(this);
        db = dbHelper.getWritableDatabase();

        String sql = "select * from picture;";
        Cursor results = db.rawQuery(sql, null);

        results.moveToFirst();
        while(!results.isAfterLast()){
            Log.d("PICTURE", results.getString(4));
            results.moveToNext();
        }
        results.close();
    }
}
