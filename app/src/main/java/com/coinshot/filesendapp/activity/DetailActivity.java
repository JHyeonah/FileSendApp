package com.coinshot.filesendapp.activity;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coinshot.filesendapp.R;
import com.coinshot.filesendapp.databinding.ActivityDetailBinding;
import com.coinshot.filesendapp.model.Album;

import java.io.File;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    ActivityDetailBinding bind;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    GestureDetector detector;
    ArrayList<Album> list;

    Context mContext;
    String fileName;
    int pos;
    File file = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mContext = this;
        detector = new GestureDetector(new MyGestureDetector());

        Intent intent = getIntent();
        pos = intent.getIntExtra("position", 0);

        list = (ArrayList<Album>)intent.getSerializableExtra("list");

        initView();

        bind.photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                downloadImage();
                return true;
            }
        });

        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        };

       bind.scrollView.setOnTouchListener(gestureListener);
    }

    public void initView(){
        Log.d("PICTURE", list.get(pos).getTitle());

        bind.titleText.setText("제목 : "  + list.get(pos).getTitle());
        bind.timeText.setText("업로드 시각 : " + list.get(pos).getTime());
        bind.commentText.setText("코멘트 : " + list.get(pos).getContent());
        fileName = list.get(pos).getFileName();

        Glide.with(this)
                .load("http://192.168.100.122/api/test/file/" + fileName)
                .into(bind.photoView);
    }

    public void downloadImage(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailActivity.this);
        dialog.setMessage("이미지를 다운로드 받으시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri source = Uri.parse("http://192.168.100.122/api/test/file/" + fileName);
                        DownloadManager.Request request = new DownloadManager.Request(source);
                        request.setDescription("서버 이미지 다운");
                        request.setTitle("이미지 다운");
                        final DownloadManager manager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                        request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory() + "/Pictures/ServerReceive/", fileName);
                        Environment.getExternalStoragePublicDirectory(Environment.getExternalStorageDirectory() + "/Pictures/ServerReceive/").mkdirs();

                        manager.enqueue(request);

                        Toast.makeText(getApplicationContext(), "다운로드 완료", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = dialog.create();
        alert.setTitle("이미지 다운로드");
        alert.show();


    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if(e1 == null || e2 == null) return false;

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if(pos+1 == list.size()){
                    Toast.makeText(getApplicationContext(), "목록의 끝입니다", Toast.LENGTH_SHORT).show();
                }else{
                    pos += 1;
                    initView();
                }

            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if(pos == 0){
                    Toast.makeText(getApplicationContext(), "목록의 시작입니다", Toast.LENGTH_SHORT).show();
                }else{
                    pos -= 1;
                    initView();
                }
            }

            return false;
        }
    }
}
