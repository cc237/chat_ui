package com.androidchatapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// TODO: friend list page

public class Chat2 extends AppCompatActivity {
    LinearLayout layout;
    ImageView sendButton;
    ImageView imgButton;
    ImageView audioButton;
    EditText messageArea;
    ScrollView scrollView;
    String fileType;
    String fileData;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        layout = (LinearLayout) findViewById(R.id.layout1);
        sendButton = (ImageView) findViewById(R.id.sendButton);
        imgButton = (ImageView) findViewById(R.id.imgButton);
        audioButton = (ImageView) findViewById(R.id.audioButton);
        messageArea = (EditText) findViewById(R.id.messageArea);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                }
            }
        );

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("audio/*");
                    startActivityForResult(intent, 2);
                }
            }
        );

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if (!messageText.equals("")) {
                    String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/send_msg";
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (obj.getString("status").equals("success")) {
                                            addMessageBox("You:-\n" + obj.getString("msg"), 1);
                                            messageArea.setText("");
                                        } else {
                                            Toast.makeText(Chat2.this, "Message Failed", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(Chat2.this, "Message Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                    )

                    {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json");
                            return params;
                        }

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("to_un", UserDetails.chatWith);
                            params.put("from_un", UserDetails.username);
                            params.put("msg", messageArea.getText().toString());

                            return params;
                        }
                    };
                    RequestQueue rQueue = Volley.newRequestQueue(Chat2.this);
                    rQueue.add(request);
                }
            }
        });

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/get_msgs";
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (obj.getString("status").equals("success")) {
                                        JSONArray messages = obj.getJSONArray("msgs");
                                        if (messages.length() > 0) {
                                            for (int i = 0; i < messages.length(); i++) {
                                                addMessageBox(UserDetails.chatWith + ":-\n" + messages.getString(i), 1);
                                            }
                                        }
                                        JSONArray files = obj.getJSONArray("files");
                                        if (files.length() > 0) {
                                            for (int i = 0; i < files.length(); i++) {
                                                JSONObject fileObj = files.getJSONObject(i);
                                                String file_data = fileObj.getString("file_data");
                                                byte[] b = Base64.decode(file_data, Base64.URL_SAFE | Base64.NO_WRAP);
                                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileObj.getString("file_name"));
                                                if (!file.exists()){
                                                    file.createNewFile();
                                                }
                                                FileOutputStream fop = new FileOutputStream(file);
                                                fop.write(b);
                                                fop.flush();
                                                fop.close();
                                                addMessageBox(UserDetails.chatWith + ":-\n" + fileObj.getString("file_name"), 1);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println(error);
                            }
                        }
                )

                {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("to_un", UserDetails.username);
                        params.put("from_un", UserDetails.chatWith);

                        return params;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(Chat2.this);
                rQueue.add(request);
                handler.postDelayed(this, 15000);//15 second delay

            }
        };
        handler.postDelayed(runnable, 15000);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/send_file";

        switch(requestCode) {
            case 1:
                if(resultCode==RESULT_OK){

                    fileType = "IMAGE";
                    Uri selectedImageURI = data.getData();
                    fileName = getFileName(selectedImageURI);

                    InputStream is = null;
                    try {
                        is = getContentResolver().openInputStream(selectedImageURI);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap bm = BitmapFactory.decodeStream(is);
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] b = baos.toByteArray();
                    fileData = Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_WRAP);
                }
                break;
            case 2:
                if(resultCode==RESULT_OK){

                    fileType = "AUDIO";
                    Uri selectedAudioURI = data.getData();
                    fileName = getFileName(selectedAudioURI);

                    InputStream is = null;
                    try {
                        is = getContentResolver().openInputStream(selectedAudioURI);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[16384];
                    int read = 0;
                    try {
                        while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                            baos.write(buffer, 0, read);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        baos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] b = baos.toByteArray();

                    fileData = Base64.encodeToString(b, Base64.URL_SAFE | Base64.NO_WRAP);
                }
                break;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("status").equals("success")) {
                                addMessageBox("You:-\n" + fileType, 1);
                            } else {
                                Toast.makeText(Chat2.this, "Send " + fileType + " Failed", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Chat2.this, "Send " + fileType + " Failed", Toast.LENGTH_LONG).show();
                    }
                }
        )

        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("to_un", UserDetails.chatWith);
                params.put("from_un", UserDetails.username);
                params.put("file_type", fileType);
                params.put("file_name", fileName);
                params.put("file_data", fileData);

                return params;
            }
        };
        RequestQueue rQueue = Volley.newRequestQueue(Chat2.this);
        rQueue.add(request);

    }

    public void addMessageBox(String message, int type) {
        TextView textView = new TextView(Chat2.this);
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(lp);

        if (type == 1) {
            textView.setBackgroundResource(R.drawable.rounded_corner1);
        } else if (type == 2) {
            textView.setBackgroundResource(R.drawable.rounded_corner_test);
        }
        else {
            textView.setBackgroundResource(R.drawable.rounded_corner2);
        }

        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}