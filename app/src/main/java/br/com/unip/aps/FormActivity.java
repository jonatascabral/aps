package br.com.unip.aps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class FormActivity extends BaseActivity implements View.OnClickListener {

    private EditText name, email, description;
    private ImageView image;
    private Bitmap photo;
    private Intent requestIntent;
    private Intent resultIntent;
    private static final int CAMERA_REQUEST = 1555;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        requestIntent = getIntent();
        name = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.useremail);
        description = (EditText) findViewById(R.id.description);
        image = (ImageView) findViewById(R.id.image);
        Button btn_add_notice = (Button) findViewById(R.id.btn_add_notice);
        btn_add_notice.setOnClickListener(this);
        Button btn_add_image = (Button) findViewById(R.id.btn_add_image);
        btn_add_image.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_add_notice:
                resultIntent  = new Intent(this, MainActivity.class);
                if (this.validateFormFields(resultIntent)) {
                    resultIntent = this.putExtrasInIntent(resultIntent);
                    WebService service = new WebService(this, resultIntent, this);
                    service.setAction(WebService.ACTION_ADD_NOTICE);
                    service.execute();
                }
                break;
            case R.id.btn_add_image:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                break;
        }
    }

    private Intent putExtrasInIntent(Intent intent) {
        intent.putExtra("username", name.getText().toString());
        intent.putExtra("useremail", email.getText().toString());
        intent.putExtra("description", description.getText().toString());
        intent.putExtra("debug", true);
        intent.putExtra("lat", requestIntent.getDoubleExtra("lat", 0));
        intent.putExtra("lng", requestIntent.getDoubleExtra("lng", 0));
        intent.putExtra("image", getStringImage(photo));

        return intent;
    }

    private boolean validateFormFields(Intent intent) {
        boolean erro = false;
        String message = "";
        if (name.getText().toString().equalsIgnoreCase("")) {
            erro = true;
            message = getResources().getString(R.string.error_name);
        } else if (email.getText().toString().equalsIgnoreCase("")) {
            erro = true;
            message = getResources().getString(R.string.error_email);
        } else if (description.getText().toString().equalsIgnoreCase("")) {
            erro = true;
            message = getResources().getString(R.string.error_description);
        }/* else if (photo == null) {
            erro = true;
            message = getResources().getString(R.string.error_image);
        }*/
        if (erro) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        return ! erro;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            this.photo = photo;
            image.setImageBitmap(photo);
        }
    }

    @Override
    public void onAddNotice(String jsonNotice) {
        super.onAddNotice(jsonNotice);
        try {
            JSONObject json = new JSONObject(jsonNotice);
            JSONArray errors = json.getJSONArray("errors");
            if (errors != null && errors.length() > 0) {
                for (int i = 0; i < errors.length(); i++) {
                    String error = errors.getString(i);
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }
            } else {
                resultIntent.putExtra("marker", json.getString("marker"));
                setResult(100, resultIntent);
                finish();
            }
        } catch (Exception e) {
            showError(e);
        }
    }
}
