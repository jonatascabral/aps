package br.com.unip.aps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

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
}
