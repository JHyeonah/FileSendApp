package com.coinshot.filesendapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class UpdateDBHelper extends SQLiteOpenHelper {
    private static final String NAME = "picture.db";
    private static final int VERSION = 1;

    public UpdateDBHelper(Context context){
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE picture" +
                "(id Integer primary key autoincrement, " +
                "filename text not null," +
                "time datetime not null," +
                "thumbnail bolb not null," +
                "title text," +
                "comment text);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 버전 업그레이드 시
        onCreate(db);
    }
}
