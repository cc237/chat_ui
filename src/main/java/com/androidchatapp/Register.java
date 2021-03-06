package com.androidchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    EditText username, password;
    Button registerButton;
    String user, pass;
    TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        registerButton = (Button)findViewById(R.id.registerButton);
        login = (TextView)findViewById(R.id.login);

        Firebase.setAndroidContext(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();

                if(user.equals("")){
                    username.setError("Username Required");
                }
                else if(pass.equals("")){
                    password.setError("Password Required");
                }
                else if(!user.matches("[A-Za-z0-9]+")){
                    username.setError("Alpha-numeric Characters Only");
                }
                else if(user.length()<5){
                    username.setError("5 or More Characters");
                }
                else if(pass.length()<5){
                    password.setError("5 or More Characters");
                }
                else {
                    final ProgressDialog pd = new ProgressDialog(Register.this);
                    pd.setMessage("Loading...");
                    pd.show();

                    String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/register";
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (obj.getBoolean("registered") == true) {
                                            Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(Register.this, Login.class));
                                        } else {
                                            Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    pd.dismiss();
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    NetworkResponse networkResponse = error.networkResponse;
                                    if (networkResponse != null && networkResponse.data != null) {
                                        String errorResp = new String(networkResponse.data);
                                        try {
                                            JSONObject obj = new JSONObject(errorResp);
                                            if (obj.getString("reason").equals("user exists")) {
                                                Toast.makeText(Register.this, "User Already Exists", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_LONG).show();
                                    }
                                    pd.dismiss();
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
                            params.put("un", user);
                            params.put("pw", pass);

                            return params;
                        }
                    };

                    RequestQueue rQueue = Volley.newRequestQueue(Register.this);
                    rQueue.add(request);
                }
            }
        });
    }
}