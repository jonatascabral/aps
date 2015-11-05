package br.com.unip.aps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class FormActivity extends BaseActivity implements View.OnClickListener {

    private EditText name, email, description;
    private ImageView image;
    private Intent requestIntent;

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

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
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
                Intent intent = new Intent(this, MainActivity.class);
                if (this.validateFormFields(intent)) {
                    intent = this.putExtrasInIntent(intent);
                    WebService service = new WebService(this, intent);
                    service.setAction(WebService.ACTION_ADD_NOTICE);
                    service.execute();
                }
                break;
        }
    }

    private Intent putExtrasInIntent(Intent intent) {
        intent.putExtra("name", name.getText().toString());
        intent.putExtra("email", email.getText().toString());
        intent.putExtra("description", description.getText().toString());
        intent.putExtra("debug", true);
        intent.putExtra("lat", requestIntent.getDoubleExtra("lat", 0));
        intent.putExtra("lng", requestIntent.getDoubleExtra("lng", 0));

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
        }/* else if (image.getText().toString().equalsIgnoreCase("")) {
            erro = true;
            message = getResources().getString(R.string.error_image);
        }*/
        if (erro) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        return ! erro;
    }
}
