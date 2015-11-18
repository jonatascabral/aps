package br.com.unip.aps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Jonatas on 04/11/2015.
 * package br.com.unip.aps
 * project APS
 */
public class WebService {
    private String json;
    private String serverAddress;
    private AppCompatActivity parentActivity;
    private GetJSONListener listener;
    private Intent intent;
    private JsonReadTask task;
    private String message;
    private int action;
    private ProgressDialog progressDialog;
    public static final int ACTION_ADD_NOTICE = 1;

    public static final int ACTION_GET_NOTICES = 2;

    public WebService(AppCompatActivity parentActivity, Intent intent, GetJSONListener listener, String message) {
        this.listener = listener;
        this.parentActivity = parentActivity;
        this.intent = intent;
        this.task = new JsonReadTask();
        this.parentActivity.setIntent(new Intent(parentActivity, parentActivity.getClass()));
        this.message = message;
        progressDialog = new ProgressDialog(parentActivity);
    }

    private class JsonReadTask extends AsyncTask<String, Void, String> {
        private URL url;
        private HttpURLConnection connection;

        protected void onPreExecute() {
            showDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String postParams = this.buildQueryFromIntent(intent);
                serverAddress = parseServerAddress();
                url = new URL(serverAddress);

                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postParams);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    json = this.inputStreamToString(connection.getInputStream()).toString();
                } else {
                    Toast.makeText(parentActivity, R.string.json_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return json;
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine;
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(parentActivity, R.string.json_error, Toast.LENGTH_SHORT).show();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            hideDialog();
            if (getAction() == WebService.ACTION_ADD_NOTICE) {
                listener.onAddNotice(result);
            } else if (getAction() == WebService.ACTION_GET_NOTICES) {
                listener.onGetNotices(result);
            }
        }

        private String buildQueryFromIntent(Intent intent) throws UnsupportedEncodingException {
            if (intent == null) {
                return "";
            }
            Bundle data = intent.getExtras();
            StringBuilder result = new StringBuilder();
            boolean first = true;
            if (data != null) {
                Set<String> keys = data.keySet();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    String value;
                    try {
                        value = data.get(key).toString();
                    } catch (NullPointerException e) {
                        value = "";
                    }
                    if (first) {
                        first = false;
                    } else {
                        result.append("&");
                    }

                    result.append(key);
                    result.append("=");
                    result.append(value);
                }
            }
            return result.toString();
        }
    }

    public void showDialog() {
        String title = parentActivity.getResources().getString(R.string.wait);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
        progressDialog.setCancelable(false);
    }

    public void hideDialog() {
        progressDialog.dismiss();
    }

    private String parseServerAddress() {
        String address = parentActivity.getResources().getString(R.string.server_url);
        if (this.getAction() == 1) {
            address += parentActivity.getResources().getString(R.string.server_add_action);
        } else if(this.getAction() == 2) {
            address += parentActivity.getResources().getString(R.string.server_get_action);
        }
        return address;
    }

    public JsonReadTask getTask(){
        return this.task;
    }

    public String getJson() {
        return this.json;
    }

    public AsyncTask execute() {
        this.task.execute();
        return this.task;
    }

    public int getAction() {
        return this.action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
