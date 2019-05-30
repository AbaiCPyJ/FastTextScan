package com.example.opencvapp;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class SecondActivity extends AppCompatActivity {

    public static final String PHOTO_MIME_TYPE = "image/png";
    public static final String EXTRA_PHOTO_URI = "test.SecondActivity.extra.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "test.SecondActivity.extra.PHOTO_DATA_PATH";
    private Uri mUri;
    private String mDataPath;
    private String TAG = "OpenCV2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

                final Intent intent = getIntent();
        mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);
        mDataPath = intent.getStringExtra(EXTRA_PHOTO_DATA_PATH);
        final ImageView imageView = new ImageView(this);
        imageView.setImageURI(mUri);
        setContentView(imageView);


    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.second, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                deletePhoto();
                return true;
            case R.id.menu_edit:
                editPhoto();
                return true;
            case R.id.menu_share:
                sharePhoto();
                return true;
            case R.id.menu_recog:
                recognizerPhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void deletePhoto() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(
                SecondActivity.this);
        alert.setTitle("Удалить фото?");
        alert.setMessage("Эта фотография сохранена в галерее. Вы хотите уго удалить?");
        alert.setCancelable(false);
        alert.setPositiveButton("Удалить",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        getContentResolver().delete(
                                Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.MediaColumns.DATA + "=?",
                                new String[]{mDataPath});
                        finish();
                    }
                });
        alert.setNegativeButton(android.R.string.cancel, null);
        alert.show();
    }

    /*
     * Показывать выбранное приложение, чтобы пользователь мог выбрать приложение для редактирования фотографии.
     */
    private void editPhoto() {
        final Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setDataAndType(mUri, PHOTO_MIME_TYPE);
        startActivity(Intent.createChooser(intent, "Редактировать фото"));
    }

    /*
     *  выбранное приложение, чтобы пользователь мог выбрать приложение для отправки фотографии.
     */
    private void sharePhoto() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(PHOTO_MIME_TYPE);
        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Фото");
        intent.putExtra(Intent.EXTRA_TEXT, "Зацени фото");
        startActivity(Intent.createChooser(intent,
                "Поделиться фотографией"));
    }



    private void recognizerPhoto()
    {
        Intent intent = new Intent(SecondActivity.this, Recognizer.class);
        intent.putExtra(SecondActivity.EXTRA_PHOTO_URI, mUri);
        intent.putExtra(SecondActivity.EXTRA_PHOTO_DATA_PATH, mDataPath);
        startActivity(intent);
    }

}


