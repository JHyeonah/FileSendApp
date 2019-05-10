package com.coinshot.filesendapp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.coinshot.filesendapp.databinding.ActivityAlbumBinding;

public class AlbumActivity extends AppCompatActivity {
    ActivityAlbumBinding bind;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_album);
    }
}
