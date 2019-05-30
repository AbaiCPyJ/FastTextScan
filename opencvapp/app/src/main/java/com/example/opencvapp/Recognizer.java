package com.example.opencvapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;



public class Recognizer extends AppCompatActivity {
    public static final String PHOTO_MIME_TYPE = "image/png";
    public static final String EXTRA_PHOTO_URI = "test.SecondActivity.extra.PHOTO_URI";
    public static final String EXTRA_PHOTO_DATA_PATH = "test.SecondActivity.extra.PHOTO_DATA_PATH";
    public Uri mUri;
    public String mDataPath;
    public String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        setContentView(R.layout.activity_recog);


        mUri = intent.getParcelableExtra(EXTRA_PHOTO_URI);

        ImageView Rimg = new ImageView(this);

        Rimg.setImageURI(mUri);




            Bitmap Rbtmp = ((BitmapDrawable)Rimg.getDrawable()).getBitmap();


            TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();

            if (!textRecognizer.isOperational()) {
                new AlertDialog.Builder(this)
                        .setMessage("Невозможно настроить распознаватель текста на устройстве :(").show();
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(Rbtmp).build();
            SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);
            String detectedText = "";
            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.valueAt(i);
                if (textBlock != null && textBlock.getValue() != null) {
                    detectedText += textBlock.getValue();
                }
            }
            TextView textView = findViewById(R.id.textViewContent);
            textView.setText(detectedText);
            textRecognizer.release();
            text= detectedText;

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.third, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shareT:
                shareText();
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void shareText() {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent,"Поделиться текстом"));
    }

}
