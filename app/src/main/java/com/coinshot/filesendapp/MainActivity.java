package com.coinshot.filesendapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.coinshot.filesendapp.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    ActivityMainBinding binding;

    final static int TAKE_PICTURE = 1;
    final static int PICK_FROM_ALBUM = 2;
    final String TAG = "PICTURE";
    final String[] ITEM = {"사진 촬영", "앨범에서 가져오기"};

    private Uri photoUri;
    private String currentPhotoPath;
    String imageCaptureName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.upload.setOnClickListener(this);
        binding.album.setOnClickListener(this);

        checkPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Permission :" + permissions[0]);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upload:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("업로드 방식 선택");
                builder.setItems(ITEM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            takePicture();
                        }else if(which == 1){
                            Intent pickIntent = new Intent(Intent.ACTION_PICK);
                            pickIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                            pickIntent.setType("image/*");
                            startActivityForResult(pickIntent, PICK_FROM_ALBUM);
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.album:
                Intent albumIntent = new Intent(this, AlbumActivity.class);
                startActivity(albumIntent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case TAKE_PICTURE:
                    Intent takeIntent = new Intent(this, FileSendActivity.class);
                    takeIntent.putExtra("pathTaken", currentPhotoPath);
                    takeIntent.putExtra("name", imageCaptureName);
                    takeIntent.putExtra("flag", "1");
                    startActivity(takeIntent);
                    break;

                case PICK_FROM_ALBUM:
                    Intent galleryIntent = new Intent(this, FileSendActivity.class);
                    Uri uri = data.getData();
                    galleryIntent.putExtra("pathGallery", uri.toString());
                    galleryIntent.putExtra("flag", "2");
                    startActivity(galleryIntent);
                    break;
            }
        }

    }

    private void checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "권한 설정 완료");
            }else{
                // 권한 설정 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }

        }
    }

    private void takePicture(){
        String state = Environment.getExternalStorageState();

        // 외장 메모리 검사
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                if(photoFile != null){
                    photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, TAKE_PICTURE);
                }
            }
        }else{
            Log.d(TAG, "저장공간 접근 불가");
        }
    }

    private File createImageFile() throws IOException{
        // 파일 디렉터리가 없다면 생성
        File dir = new File(Environment.getExternalStorageDirectory() + "/Pictures/FileSend/");
        if(!dir.exists()){
            dir.mkdirs();
        }

        // 이미지 파일 이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageCaptureName = timeStamp + ".jpeg";

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Pictures/FileSend/" + imageCaptureName);
        currentPhotoPath = storageDir.getAbsolutePath();

        return storageDir;
    }

}
