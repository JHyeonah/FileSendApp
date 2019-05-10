package com.coinshot.filesendapp;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coinshot.filesendapp.databinding.ActivityFilesendBinding;
import com.gc.materialdesign.widgets.SnackBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileSendActivity extends AppCompatActivity {
    ActivityFilesendBinding bind;

    String currentPhotoPath = "";
    Uri uri;
    String path = "/Pictures/FileSend/";
    String fileName = "";
    Bitmap sendedBitmap, thumbnail;
    int degree;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_filesend);

        Intent intent = getIntent();
        String flag = intent.getStringExtra("flag");

        if(flag.equals("1")){
            // 사진 찍었을 때
            fileName = intent.getStringExtra("name");
            currentPhotoPath = intent.getStringExtra("pathTaken");
            sendedBitmap = getPicture();
            degree = getDegree();

            thumbnail = ThumbnailUtils.extractThumbnail(sendedBitmap, 200, 200 );
            bind.sampleIv.setImageBitmap(rotate(sendedBitmap, degree));

        }else if(flag.equals("2")){
            // 갤러리에서 선택했을 때
            uri = Uri.parse(intent.getStringExtra("pathGallery"));
            sendedBitmap = getBitmapGallery(uri);
            degree = getDegreeGallery(uri);

            thumbnail = ThumbnailUtils.extractThumbnail(sendedBitmap, 200, 200 );
            bind.sampleIv.setImageBitmap(rotate(sendedBitmap, degree));

        }

        bind.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bind.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveThumbnail(thumbnail, fileName);

               Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();

                finish();
            }
        });
    }


    // 찍은 사진 비트맵 가져오기
    private Bitmap getPicture(){
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

        return bitmap;
    }

    // 찍은 사진 각도 가져오기
    private int getDegree(){
        ExifInterface exif = null;

        try{
            exif = new ExifInterface(currentPhotoPath);
        }catch (IOException e){
            e.printStackTrace();
        }

        int exifOrientation;
        int exifDegree;

        if(exif != null){
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegress(exifOrientation);
        }else{
            exifDegree = 0;
        }

        return exifDegree;
    }

    private int exifOrientationToDegress(int exifOrientation){
        // 사진 회전값 처리

        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90){
            return 90;
        }else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180){
            return 180;
        }else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270){
            return 270;
        }

        return 0;
    }

    // 갤러리 사진 비트맵
    private Bitmap getBitmapGallery(Uri uri){
        String imgPath = getRealPathFromURI(uri);
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);

        return bitmap;
    }

    // 갤러리 사진 각도
    private int getDegreeGallery(Uri uri){
        String imgPath = getRealPathFromURI(uri);
        ExifInterface exif = null;

        try{
            exif = new ExifInterface(imgPath);
        }catch (IOException e){
            e.printStackTrace();
        }

        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegress(exifOrientation);

        return exifDegree;
    }

    // 사진 절대경로 구하기
    private String getRealPathFromURI(Uri uri){
        int index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);

        if(cursor.moveToFirst()){
            index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(index);
    }

    // 사진 회전
    private Bitmap rotate(Bitmap src, float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }


    private void saveThumbnail(Bitmap bitmap, String filename){

        if(filename.equals("")){
            filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".png";
        }

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/FileSend/" + filename);
            OutputStream out = null;
            try{
                file.createNewFile();
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                out.close();

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/Pictures/FileSend/" + filename)));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }
}
