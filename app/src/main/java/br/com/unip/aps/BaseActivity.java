package br.com.unip.aps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected final static int NEW_NOTICE_RESULT_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    protected void showError(Exception e) {
        e.printStackTrace();
        Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show();
    }


    protected Bitmap getBitmapFromView(ImageView view) {
        return ((BitmapDrawable) view.getDrawable()).getBitmap();
    }

    protected Bitmap getImageFromString(String image) {
        byte[] imageBytes = Base64.decode(image, Base64.URL_SAFE);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    protected String getStringImage(Bitmap bmp){
        if (bmp == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.URL_SAFE);
    }
}
