package com.example.youhe.youhecheguanjia.ui.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.youhe.youhecheguanjia.R;
import com.example.youhe.youhecheguanjia.dialog.ImageBigDialog;
import com.example.youhe.youhecheguanjia.utils.FileUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class SelectPicActivity extends Activity implements OnClickListener {

    // 使用照相机拍照获取图片
    public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
    // 使用相册中的图片
    public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
    // 从Intent获取图片路径的KEY
    public static final String KEY_PHOTO_PATH = "photo_path";
    private static final String TAG = "SelectPicActivity";
    private static final int PHOTO_RESOULT = 0x03;
    private LinearLayout dialogLayout;
    private Button takePhotoBtn, pickPhotoBtn, cancelBtn;

    /** 获取到的图片路径 */
    private String picPath;
    private Intent lastIntent;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pic);
        getWindow().setLayout(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);

        dialogLayout = (LinearLayout) findViewById(R.id.dialog_layout);
        dialogLayout.setOnClickListener(this);
        takePhotoBtn = (Button) findViewById(R.id.btn_take_photo);
        takePhotoBtn.setOnClickListener(this);
        pickPhotoBtn = (Button) findViewById(R.id.btn_pick_photo);
        pickPhotoBtn.setOnClickListener(this);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);
        lastIntent = getIntent();
    }

    public void onClick(final View v) {
        RxPermissions rxPermissions=new RxPermissions(this);
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    switch (v.getId()) {
                        case R.id.dialog_layout:
                            finish();
                            break;
                        case R.id.btn_take_photo:
                            takePhoto();
                            break;
                        case R.id.btn_pick_photo:
                            Matisse.from(SelectPicActivity.this)
                                    .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))
                                    .theme(R.style.Matisse_Dracula)
                                    .countable(false)
                                    .maxSelectable(1)
                                    .imageEngine(new PicassoEngine())
                                    .forResult(SELECT_PIC_BY_PICK_PHOTO);
                            break;
                        default:
                            finish();
                            break;
                    }
                }else {
                    Toast.makeText(SelectPicActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 拍照获取图片
     */
    private static final int TAKE_PHOTO_REQUEST_CODE = 1;
    private void takePhoto() {
        if  (ContextCompat.checkSelfPermission(SelectPicActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SelectPicActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, TAKE_PHOTO_REQUEST_CODE);
        } else {
            // 执行拍照前，应该先判断SD卡是否存在
            String SDState = Environment.getExternalStorageState();
            if (SDState.equals(Environment.MEDIA_MOUNTED)) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// "android.media.action.IMAGE_CAPTURE"
                /***
                 * 需要说明一下，以下操作使用照相机拍照，拍照后的图片会存放在相册中的 这里使用的这种方式有一个好处就是获取的图片是拍照后的原图
                 * 如果不实用ContentValues存放照片路径的话，拍照后获取的图片为缩略图不清晰
                 */
                ContentValues values = new ContentValues();
                photoUri = this.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

			/*
			 * //收缩裁剪选择的图片 intent.setDataAndType(photoUri, "image/*");
			 * intent.putExtra("crop", "true"); // aspectX aspectY 是宽高的比例
			 * intent.putExtra("aspectX", 2); intent.putExtra("aspectY", 1); //
			 * outputX outputY 是裁剪图片宽高 intent.putExtra("outputX", 140);
			 * intent.putExtra("outputY", 70); intent.putExtra("return-data",
			 * true);
			 */

                /** ----------------- */
                startActivityForResult(intent, SELECT_PIC_BY_TACK_PHOTO);
            } else {
                Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
            }
        }
    }

    /***
     * 从相册中取图片
     */
    private void pickPhoto() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {//因为Android SDK在4.4版本后图片action变化了 所以在这里先判断一下
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK)
            doPhoto(requestCode, data);
    }

    /**
     * 选择图片后，获取图片的路径
     *
     * @param requestCode
     * @param data
     */
    private void doPhoto(int requestCode, Intent data) {
        if (requestCode == SELECT_PIC_BY_PICK_PHOTO) // 从相册取图片，有些手机有异常情况，请注意
        {
            if (data == null) {
                Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
                return;
            }

            Uri pUri = Matisse.obtainResult(data).get(0);

            if (pUri == null) {
                Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
                return;
            }
            String picturePath= FileUtils.getUriPath(SelectPicActivity.this,pUri);
            if(picturePath != null && ( picturePath.endsWith(".png") ||
                    picturePath.endsWith(".PNG") ||picturePath.endsWith(".jpg")
                    ||picturePath.endsWith(".JPG") ))

                if (picturePath != null) {
                    lastIntent.putExtra(KEY_PHOTO_PATH, picturePath);
                    setResult(Activity.RESULT_OK, lastIntent);
                    finish();
                } else {
                    Toast.makeText(this, "选择文件不正确!", Toast.LENGTH_LONG).show();
                }
        }
        if(requestCode==SELECT_PIC_BY_TACK_PHOTO){
            if (photoUri == null) {
                Toast.makeText(this, "选择图片文件出错", Toast.LENGTH_LONG).show();
                return;
            }
            getImagePath(photoUri);
        }

        if (requestCode == PHOTO_RESOULT) {
//            photoUri = data.getData();
			String[] pojo = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(photoUri, pojo, null, null, null);
			if (cursor != null) {
				int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
				cursor.moveToFirst();
				picPath = cursor.getString(columnIndex);
				cursor.close();
			}
			Log.i(TAG, "imagePath = " + picPath);

			 if(picPath != null && ( picPath.endsWith(".png") ||
			 picPath.endsWith(".PNG") ||picPath.endsWith(".jpg")
			 ||picPath.endsWith(".JPG") ))

			if (picPath != null) {
				lastIntent.putExtra(KEY_PHOTO_PATH, picPath);
				setResult(Activity.RESULT_OK, lastIntent);
				finish();
			} else {
				Toast.makeText(this, "选择文件不正确!", Toast.LENGTH_LONG).show();
			}
        }
    }

    private void getImagePath(Uri uri){
        String[] pojo = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, pojo, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
            cursor.moveToFirst();
            picPath = cursor.getString(columnIndex);
            cursor.close();
        }
        Log.i(TAG, "imagePath = " + picPath);

        if(picPath != null && ( picPath.endsWith(".png") ||
                picPath.endsWith(".PNG") ||picPath.endsWith(".jpg")
                ||picPath.endsWith(".JPG") ))

            if (picPath != null) {
                lastIntent.putExtra(KEY_PHOTO_PATH, picPath);
                setResult(Activity.RESULT_OK, lastIntent);
                finish();
            } else {
                Toast.makeText(this, "选择文件不正确!", Toast.LENGTH_LONG).show();
            }
    }

    private void cropImage(Uri uri) {
        System.out.println("cropImages");
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 4);
        intent.putExtra("aspectY", 3);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX",1600);
        intent.putExtra("outputY",1200);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_RESOULT);
    }

}
