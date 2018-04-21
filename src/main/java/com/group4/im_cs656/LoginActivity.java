package com.group4.im_cs656;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import net.qiujuer.genius.ui.widget.Loading;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    TextView register;
    EditText username, password;
    Loading loading;
    Button loginButton;
    String user, pass;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        register = findViewById(R.id.txt_go_register);
        username = findViewById(R.id.edit_name);
        password = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.btn_submit);
        loading = findViewById(R.id.loading);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                loading.start();
                if (user.equals("")) {
                    username.setError("can't be blank");
                } else if (pass.equals("")) {
                    password.setError("can't be blank");
                } else {
                    String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/login";
                    final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
//                    pd.setMessage("Loading...");
//                    pd.show();

                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    System.out.println(response);
                                    try {
                                        JSONObject obj = new JSONObject(response);
                                        if (obj.getBoolean("auth") == true) {
//                                            UserDetails.username = user;
                                            loading.stop();
                                            SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = userInfo.edit();
                                            editor.putString("username", user);
                                            editor.commit();
                                            String test = userInfo.getString("username", "");
                                            Log.i("lx", "onResponse: " + test.toString());
                                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(LoginActivity.this, UsersActivity.class));
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    pd.dismiss();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                                    pd.dismiss();
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
                            params.put("un", user);
                            params.put("pw", pass);

                            return params;
                        }
                    };

                    RequestQueue rQueue = Volley.newRequestQueue(LoginActivity.this);
                    rQueue.add(request);
                }

            }
        });
    }
}

