package com.androidchatapp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// TODO: friend list with add / remove friends

public class Chat extends AppCompatActivity {
    LinearLayout layout;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = (LinearLayout)findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/send_msg";
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (obj.getString("status").equals("success")) {
                                            addMessageBox("You:-\n" + obj.getString("msg"), 1);
                                            messageArea.setText("");
                                        } else {
                                            Toast.makeText(Chat.this, "Message Failed", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(Chat.this, "Message Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                    )

                    {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("Content-Type","application/json");
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
                    RequestQueue rQueue = Volley.newRequestQueue(Chat.this);
                    rQueue.add(request);
                }
            }
        });

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/get_msg";
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (obj.getString("status").equals("success")) {
                                        JSONArray messages = obj.getJSONArray("msgs");
                                        System.out.println(messages);
                                        if (messages.length() > 0) {
                                            for (int i = 0 ; i < messages.length(); i++) {
                                                addMessageBox( UserDetails.chatWith + ":-\n" + messages.getString(i), 1);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println(error);
                            }
                        }
                )

                {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/json");
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

                RequestQueue rQueue = Volley.newRequestQueue(Chat.this);
                rQueue.add(request);
                handler.postDelayed(this,15000);//15 second delay

            }
        };handler.postDelayed(runnable,15000);

    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(lp);

        if(type == 1) {
            textView.setBackgroundResource(R.drawable.rounded_corner1);
        }
        else{
            textView.setBackgroundResource(R.drawable.rounded_corner2);
        }

        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}