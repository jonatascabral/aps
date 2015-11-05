package br.com.unip.aps;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
    private Intent intent;
    private JsonReadTask task;
    private boolean debug = false;
    private int action;
    public static final int ACTION_ADD_NOTICE = 1;
    public static final int ACTION_GET_NOTICES = 2;

    public WebService(AppCompatActivity parentActivity, Intent intent) {
        this.parentActivity = parentActivity;
        this.intent = intent;
        this.task = new JsonReadTask();
        this.parentActivity.setIntent(new Intent(parentActivity, parentActivity.getClass()));
    }
    private class JsonReadTask extends AsyncTask<String, Void, String> {
        private ProgressDialog progressDialog = new ProgressDialog(parentActivity);
        private URL url;
        private HttpURLConnection connection;

        protected void onPreExecute() {
            String message = parentActivity.getResources().getString(R.string.wait);
            serverAddress = parseServerAddress();
            progressDialog.setMessage(message);
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    JsonReadTask.this.cancel(true);
                }
            });
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                String postParams = this.buildQueryFromIntent(intent);
                url = new URL(serverAddress);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.connect();
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postParams);
                writer.flush();
                writer.close();
                os.close();
                json = this.inputStreamToString(connection.getInputStream()).toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
//                Log.i("")
            }  catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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
                // e.printStackTrace();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            intent.putExtra("json", result);
            progressDialog.dismiss();
            parentActivity.startActivityForResult(intent, 100);
        }

        private String buildQueryFromIntent(Intent intent) throws UnsupportedEncodingException {
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

                    result.append(URLEncoder.encode(key, "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(value, "UTF-8"));
                }
            }
            return result.toString();
        }
    }// end async task

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

    public void execute() {
        this.task.execute();
    }

    public int getAction() {
        return this.action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
