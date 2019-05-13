package com.coinshot.filesendapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.coinshot.filesendapp.databinding.ActivityFilesendBinding;
import com.coinshot.filesendapp.service.SendService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FileSendActivity extends AppCompatActivity {
    ActivityFilesendBinding bind;
    private SQLiteDatabase db;
    SendService service;

    String currentPhotoPath = "";
    String imgTitle = "";
    String imgComment = "";
    Uri uri;
    String path = "/Pictures/FileSend/";
    String fileName = "";
    String serverName = "";
    Bitmap sendedBitmap, thumbnail;
    File sendFile;
    byte[] byteBitmap;
    int degree;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = DataBindingUtil.setContentView(this, R.layout.activity_filesend);

        UpdateDBHelper dbHelper = new UpdateDBHelper(this);
        db = dbHelper.getWritableDatabase();

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(120, TimeUnit.SECONDS);
        client.readTimeout(120, TimeUnit.SECONDS);
        client.writeTimeout(120, TimeUnit.SECONDS);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.100.122")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build();

        service = retrofit.create(SendService.class);

        Intent intent = getIntent();
        String flag = intent.getStringExtra("flag");

        if(flag.equals("1")){
            // 사진 찍었을 때
            fileName = intent.getStringExtra("name");
            currentPhotoPath = intent.getStringExtra("pathTaken");
            uri = Uri.parse(currentPhotoPath);
            sendedBitmap = getPicture();
            degree = getDegree();

            makeFile(fileName);

            thumbnail = ThumbnailUtils.extractThumbnail(rotate(sendedBitmap, degree), 300, 300 );
            bind.sampleIv.setImageBitmap(rotate(sendedBitmap, degree));

        }else if(flag.equals("2")){
            // 갤러리에서 선택했을 때
            uri = Uri.parse(intent.getStringExtra("pathGallery"));
            fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpeg";
            sendedBitmap = getBitmapGallery(uri);
            degree = getDegreeGallery(uri);

            makeFile(fileName);

            thumbnail = ThumbnailUtils.extractThumbnail(rotate(sendedBitmap, degree), 300, 300 );
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
                imgTitle = bind.titleEt.getText().toString();
                imgComment = bind.commentEt.getText().toString();

                if(sendFile.length() > 20000000){
                    Toast.makeText(getApplicationContext(), "전송 불가 : 파일 최대 크기는 20MB 입니다.", Toast.LENGTH_SHORT).show();
                    Log.d("PICTURE", "파일크기 : " + String.valueOf(sendFile.length()));
                }else{
                    Log.d("PICTURE", "sendFile : " + sendFile.getName());

                    saveThumbnail(thumbnail, fileName);

                    sendFile();

                    Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();

                    finish();
                }

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

        Bitmap toByte = bitmap;
        if(filename.equals("")){
            filename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpeg";
        }

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/FileSend/" + filename);
            OutputStream out = null;

            try{
                // 파일을 기기에 저장
                file.createNewFile();
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

                // bitmap을 byteArray로
                final int i = bitmap.getByteCount();
                ByteBuffer byteBuffer = ByteBuffer.allocate(i);
                toByte.copyPixelsToBuffer(byteBuffer);
                byteBitmap = byteBuffer.array();

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/Pictures/FileSend/" + filename)));
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    private void makeFile(String name){
        sendFile = new File(getApplicationContext().getCacheDir(), name);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        sendedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
        byte[] bitmapData = bos.toByteArray();

        try{
            FileOutputStream fos = new FileOutputStream(sendFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void sendFile(){
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), sendFile);
        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("file", sendFile.getName(), requestFile);
        Log.d("PICTURE", "filename : " + sendFile.getName() + ", contentType : " + requestFile.contentType() + ", body : " + uploadFile.body().toString());

        Call<Response> call = service.postFile(uploadFile);
        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                if(response.body() != null){
                    if(response.body().success){
                        String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                        Log.d("PICTURE" , "onResponse called : response: " + response.body() + "call : " + call);
                        serverName = response.body().fileName;
                        Log.d("PICTURE", "file name : " + response.body().fileName);

                        SQLiteStatement st = db.compileStatement("insert into picture(id, filename, time, thumbnail, title, comment) values(?, ?, ?, ?, ?, ?)");

                        st.bindNull(1);
                        st.bindString(2, serverName);
                        st.bindString(3, timeNow);
                        st.bindString(4, Environment.getExternalStorageDirectory().getPath() + "/Pictures/FileSend/" + fileName);
                        st.bindString(5, imgTitle);
                        st.bindString(6, imgComment);
                        st.execute();

                       // Log.d("PICTURE", "uri : " + getRealPathFromURI(uri));
                    }else{
                        Log.d("PICTURE", "response success is false");
                    }

                }else{
                    Log.d("PICTURE", "response body is null");
                }

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.d("PICTURE", "onFailure called : " + t.toString());
            }
        });
    }
}
