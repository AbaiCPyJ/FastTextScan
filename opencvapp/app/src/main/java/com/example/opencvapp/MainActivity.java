package com.example.opencvapp;

import java.io.File;
import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.Videoio;
import org.opencv.imgproc.Imgproc;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;
    private String TAG = "OpenCV";
    // ключ для хранения индекса активной камеры
    private static final String CAMERA_INDEX = "cameraIndex";
    // индекс активной камеры
    private int mCameraIndex;
    // Если активная камера является передней,
    // то изображение следует отобразить зеркально
    private boolean mIsCameraFrontFacing;
    // число камер на устройстве
    private int mNumCameras;
    // Сохранять ли следующий кадр
    private boolean mIsPhotoPending;
    // Матрица для сохранения фото
    private Mat mBgr;
    // Заблокировано ли меню
    private boolean mIsMenuLocked;


    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // TODO Auto-generated method stub
                final Mat rgba = inputFrame.rgba();

        if (mIsPhotoPending) {
            mIsPhotoPending = false;
            // Делаем снимок
            takePhoto(rgba);
        }
                return rgba;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(CAMERA_INDEX, 0);

            } else {
            mCameraIndex = 0;

        }





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(mCameraIndex, cameraInfo);
            mIsCameraFrontFacing = (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT);
            mNumCameras = Camera.getNumberOfCameras();
        } else {

            mIsCameraFrontFacing = false;
            mNumCameras = 1;
        }

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.openCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putInt(CAMERA_INDEX, mCameraIndex);



        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        // Инициализируем библиотеку
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this,
                mLoaderCallback);
        mIsMenuLocked = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mIsMenuLocked) {
            return true;
        }
        switch (item.getItemId()) {
             case R.id.menu_take_photo:
                mIsMenuLocked = true;
                mIsPhotoPending = true;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Инициализируем библиотеку
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV загрузилось успешно");
                    mOpenCvCameraView.enableView();
                    mBgr = new Mat();

                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    // Методы интерфейса CvCameraViewListener2
    @Override
    public void onCameraViewStarted(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCameraViewStopped() {
        // TODO Auto-generated method stub

    }



    // Метод для снимка кадра
    private void takePhoto(final Mat rgba) {
        final long currentTimeMillis = System.currentTimeMillis();
        final String appName = getString(R.string.app_name);
        final String galleryPath = Environment
                .getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = galleryPath + "/" + appName;
        final String photoPath = albumPath + "/" + currentTimeMillis + ".png";
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, photoPath);
        values.put(Images.Media.MIME_TYPE, SecondActivity.PHOTO_MIME_TYPE);
        values.put(Images.Media.MIME_TYPE, Recognizer.PHOTO_MIME_TYPE);
        values.put(Images.Media.TITLE, appName);
        values.put(Images.Media.DESCRIPTION, appName);
        values.put(Images.Media.DATE_TAKEN, currentTimeMillis);
        // Убедитесь, что каталог альбомов существует.
        File album = new File(albumPath);
        if (!album.isDirectory() && !album.mkdirs()) {
            Log.e(TAG, "Не удалось создать каталог альбомов " + albumPath);
            onTakePhotoFailed();
            return;
        }
        // Делаем снимок
        Imgproc.cvtColor(rgba, mBgr, Imgproc.COLOR_RGBA2BGR, 3);
        if (!Imgcodecs.imwrite(photoPath, mBgr)) {
            Log.e(TAG, "Не удалось сохранить фотографию в папку" + photoPath);
            onTakePhotoFailed();
        }
        Log.d(TAG, "Фотография успешно сохранена в папке " + photoPath);
        // Помещаем фото в MediaStore
        Uri uri;
        try {
            uri = getContentResolver().insert(
                    Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (final Exception e) {
            Log.e(TAG, "ошибка при вставке фотографии в MediaStore");
            e.printStackTrace();
            // Если не получилось вставить, то удаляем
            File photo = new File(photoPath);
            if (!photo.delete()) {
                Log.e(TAG, "Ошибка при удалении не вставленной фотографии");
            }
            onTakePhotoFailed();
            return;
        }
        // Открываем фотографию во второй активности
        final Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra(SecondActivity.EXTRA_PHOTO_URI, uri);
        intent.putExtra(SecondActivity.EXTRA_PHOTO_DATA_PATH, photoPath);
        startActivity(intent);
    }



    private void onTakePhotoFailed() {
        mIsMenuLocked = false;
        // Показывать сообщение об ошибке.
        final String errorMessage = "Ошибка при сохранении фото";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}