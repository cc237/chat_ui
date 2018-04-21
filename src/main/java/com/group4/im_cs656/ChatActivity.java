package com.group4.im_cs656;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends Activity {

    ImageView imageButton;
    ImageView voiceButton;
    ImageView sendButton;
    EditText msgContent;
    TextView chatWIth;
    LinearLayout linearLayout;
    NestedScrollView nestedScrollView;
    int count = 0;

    String username;
    String fileType;
    String fileData;
    String fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        linearLayout = findViewById(R.id.layout1);
        nestedScrollView = findViewById(R.id.nestscrollView);
        imageButton = findViewById(R.id.btn_image);
        voiceButton = findViewById(R.id.btn_voice);
        sendButton = findViewById(R.id.btn_send);
        msgContent = findViewById(R.id.edit_msg);
        chatWIth = findViewById(R.id.txt_chatwith);
        Intent intent = getIntent();
        if (intent != null) {
            chatWIth.setText(intent.getStringExtra("friendname"));
            username = intent.getStringExtra("username");
        }

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
                                                addTextMessageBox(messages.getString(i), 2);
                                            }
                                        }
                                        JSONArray files = obj.getJSONArray("files");
                                        if (files.length() > 0) {
                                            for (int i = 0; i < files.length(); i++) {
                                                JSONObject fileObj = files.getJSONObject(i);
                                                String file_data = fileObj.getString("file_data");
                                                String file_type = fileObj.getString("file_type");
                                                byte[] b = Base64.decode(file_data, Base64.URL_SAFE | Base64.NO_WRAP);
                                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), fileObj.getString("file_name")+".MP3");
//                                                if (!file.exists()) {
//                                                    file.createNewFile();
//                                                    Log.i("lx", "文件创建完毕 ");
//                                                }
                                                FileOutputStream fop = new FileOutputStream(file);
                                                fop.write(b);
                                                fop.flush();
                                                fop.close();
                                                if (file_type.equals("IMAGE")) {
                                                    //Log.i("lx", "文件大小: " + file.getAbsolutePath().toString());
                                                    addImageMessageBox(file, 2);
                                                } else if(file_type.equals("AUDIO")) {
                                                    //Log.i("lx", "文件大小: " + file.getAbsolutePath().toString());
                                                    addAudioMessageBox(file,2);
                                                }
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
                        params.put("to_un", username);
                        params.put("from_un", chatWIth.getText().toString());

                        return params;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(ChatActivity.this);
                rQueue.add(request);
                handler.postDelayed(this, 2000);//delay

            }
        };
        handler.postDelayed(runnable, 2000);


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, 2);
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = msgContent.getText().toString();

                if (!messageText.equals("")) {
                    String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/send_msg";
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (obj.getString("status").equals("success")) {
                                            addTextMessageBox(obj.getString("msg"), 1);
                                            Toast.makeText(ChatActivity.this, "Message success", Toast.LENGTH_SHORT).show();
                                            msgContent.setText("");
                                        } else {
                                            Toast.makeText(ChatActivity.this, "Message Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(ChatActivity.this, "Message Failed", Toast.LENGTH_SHORT).show();
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

                            params.put("to_un", chatWIth.getText().toString());
                            params.put("from_un", username);
                            params.put("msg", msgContent.getText().toString());

                            return params;
                        }
                    };
                    RequestQueue rQueue = Volley.newRequestQueue(ChatActivity.this);
                    rQueue.add(request);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/send_file";

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    fileType = "IMAGE";
                    Uri selectedImageURI = data.getData();
                    fileName = getFileName(selectedImageURI);

                    System.out.println(selectedImageURI.getPath());

                    //File file = new File(selectedImageURI.getPath());
                    //myFile.getAbsolutePath();
                    File file = getFileByUri(selectedImageURI, this, fileName);
                    //File file = new File("/storage/emulated/0/Download/SampleJPGImage_50kbmb.jpg");
                    addImageMessageBox(file, 1);

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
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (obj.getString("status").equals("success")) {
                                            Toast.makeText(ChatActivity.this, "Send Image Success", Toast.LENGTH_LONG).show();
                                            msgContent.setText("");
                                        } else {
                                            Toast.makeText(ChatActivity.this, "Send Image Failed", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(ChatActivity.this, "Message Failed", Toast.LENGTH_LONG).show();
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

                            params.put("to_un", chatWIth.getText().toString());
                            params.put("from_un", username);
                            params.put("file_type", fileType);
                            params.put("file_name", fileName);
                            params.put("file_data", fileData);

                            return params;
                        }
                    };
                    RequestQueue rQueue = Volley.newRequestQueue(ChatActivity.this);
                    rQueue.add(request);
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    fileType = "AUDIO";
                    Uri selectedAudioURI = data.getData();
                    fileName = getFileName(selectedAudioURI);

                    System.out.println(selectedAudioURI.getPath());

                    //File file = new File(selectedAudioURI.getPath());
                    File file = getFileByUri(selectedAudioURI, this, fileName);
                    //File file = new File("/storage/emulated/0/Download/SampleAudio_0.4mb.mp3");
                    addAudioMessageBox(file, 1);

                    //Log.i("lx", "音频文件地址 "+file.getAbsolutePath());
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
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (obj.getString("status").equals("success")) {
                                            Toast.makeText(ChatActivity.this, "Send Audio Success", Toast.LENGTH_LONG).show();
                                            msgContent.setText("");
                                        } else {
                                            Toast.makeText(ChatActivity.this, "Send Audio Failed", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(ChatActivity.this, "Message Failed", Toast.LENGTH_LONG).show();
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

                            params.put("to_un", chatWIth.getText().toString());
                            params.put("from_un", username);
                            params.put("file_type", fileType);
                            params.put("file_name", fileName);
                            params.put("file_data", fileData);

                            return params;
                        }
                    };
                    RequestQueue rQueue = Volley.newRequestQueue(ChatActivity.this);
                    rQueue.add(request);
                }
                break;
        }


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

    public void addTextMessageBox(String message, int type) {
        TextView textView = new TextView(ChatActivity.this);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == 1) {
            lp.gravity = Gravity.RIGHT;
            if (count == 0) {
                lp.setMargins(0, 200, 20, 10);
                count++;
            } else {
                lp.setMargins(0, 0, 20, 10);
            }

        }

        if (type == 2) {
            lp.gravity = Gravity.LEFT;
            if (count == 0) {
                lp.setMargins(20, 200, 20, 10);
                count++;
            } else {
                lp.setMargins(20, 0, 20, 10);
            }
        }
        textView.setLayoutParams(lp);
        textView.setBackgroundResource(R.drawable.rounded_corner1);
        linearLayout.addView(textView);
        nestedScrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void addImageMessageBox(final File file, int type) {
        Bitmap bm = getBitmapByWidth(file.getAbsolutePath(), 300, 1);
        ImageView imageView = new ImageView(ChatActivity.this);
        imageView.setImageBitmap(bm);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == 1) {
            lp.gravity = Gravity.RIGHT;
//            final Uri uri =FileProvider.getUriForFile(this,"com.group4.im_cs656.fileprovider",file.getAbsoluteFile());
            if (count == 0) {
                lp.setMargins(0, 200, 20, 10);
                count++;
            } else {
                lp.setMargins(0, 0, 20, 10);
            }

        }

        if (type == 2) {

            lp.gravity = Gravity.LEFT;
            if (count == 0) {
                lp.setMargins(20, 200, 20, 10);
                count++;
            } else {
                lp.setMargins(20, 0, 20, 10);
            }
        }
        imageView.setLayoutParams(lp);
        linearLayout.addView(imageView);
        nestedScrollView.fullScroll(View.FOCUS_DOWN);
        final Uri uri =FileProvider.getUriForFile(this,"com.group4.im_cs656.fileprovider",file.getAbsoluteFile());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
            }
        });
    }

    public void addAudioMessageBox(final File file,int type) {
        ImageView imageView = new ImageView(ChatActivity.this);
        imageView.setImageResource(R.drawable.ic_record);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == 1) {
            lp.gravity = Gravity.RIGHT;
            if (count == 0) {
                lp.setMargins(0, 200, 20, 10);
                count++;
            } else {
                lp.setMargins(0, 0, 20, 10);
            }

        }

        if (type == 2) {
            lp.gravity = Gravity.LEFT;
            if (count == 0) {
                lp.setMargins(20, 200, 20, 10);
                count++;
            } else {
                lp.setMargins(20, 0, 20, 10);
            }
        }
        imageView.setLayoutParams(lp);
        linearLayout.addView(imageView);
        nestedScrollView.fullScroll(View.FOCUS_DOWN);
        final Uri uri =FileProvider.getUriForFile(this,"com.group4.im_cs656.fileprovider",file.getAbsoluteFile());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "audio/*");
                startActivity(intent);
            }
        });
    }

    public Bitmap getBitmapByWidth(String localImagePath, int width, int addedScaling) {
        if (TextUtils.isEmpty(localImagePath)) {
            return null;
        }

        Bitmap temBitmap = null;

        try {
            BitmapFactory.Options outOptions = new BitmapFactory.Options();

            outOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(localImagePath, outOptions);

            int height = outOptions.outHeight;

            if (outOptions.outWidth > width) {

                outOptions.inSampleSize = outOptions.outWidth / width + 1 + addedScaling;
                outOptions.outWidth = width;

                height = outOptions.outHeight / outOptions.inSampleSize;
                outOptions.outHeight = height;
            }


            outOptions.inJustDecodeBounds = false;
            temBitmap = BitmapFactory.decodeFile(localImagePath, outOptions);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return temBitmap;
    }

    public static File getFileByUri(Uri uri, Context context, String fileName) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA}, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();
            if (path != null) {
                return new File(path);}
        } else {
            return new File("/storage/emulated/0/Download/" + fileName);
        }
        //return null;
        return new File("/storage/emulated/0/Download/" + fileName);
    }

}
