package com.group4.im_cs656;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UsersActivity extends Activity {
    Button addFriend;
    Button removeFriend;
    EditText friend;
    ListView usersList;
    String friendName;
    String userName;
    Button getFriendList;
    Button getUserList;
    TextView noUsersText;
    private boolean quit = false;
    ArrayList<String> friendList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        addFriend = findViewById(R.id.btn_add);
        friend = findViewById(R.id.edit_name);
        usersList = findViewById(R.id.usersList);
        getFriendList = findViewById(R.id.btn_submit);
        getUserList = findViewById(R.id.btn_user);
        noUsersText = findViewById(R.id.noUsersText);
        removeFriend = findViewById(R.id.btn_remove);

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendName = friend.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                userName = sharedPreferences.getString("username", "");
                Log.i("lx", "username: " + userName);
                String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/add_friend";

                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        doOnSuccess(s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.i("lx", volleyError.getMessage(), volleyError);
                        byte[] htmlBodyBytes = volleyError.networkResponse.data;  //回应的报文的包体内容
                        Log.e("lx", new String(htmlBodyBytes), volleyError);
                        Toast.makeText(UsersActivity.this, "User Not Found", Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("un", userName);
                        params.put("friend_un", friendName);

                        return params;
                    }
                };
                RequestQueue rQueue = Volley.newRequestQueue(UsersActivity.this);
                rQueue.add(request);
            }
        });

        removeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                friendName = friend.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                userName = sharedPreferences.getString("username", "");
                Log.i("lx", "username: " + userName);
                String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/remove_friend";

                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        removeFriend(s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.i("lx", volleyError.getMessage(), volleyError);
                        byte[] htmlBodyBytes = volleyError.networkResponse.data;
                        Log.e("lx", new String(htmlBodyBytes), volleyError);
                        Toast.makeText(UsersActivity.this, "User Not Found", Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("un", userName);
                        params.put("friend_un", friendName);

                        return params;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(UsersActivity.this);
                rQueue.add(request);
            }
        });

        getFriendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                userName = sharedPreferences.getString("username", "");
                String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/get_friends";
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        showFriendList(s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("un", userName);
                        return params;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(UsersActivity.this);
                rQueue.add(request);

                usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(UsersActivity.this,ChatActivity.class);
                        intent.putExtra("friendname",friendList.get(position));
                        intent.putExtra("username",userName);
                        startActivity(intent);

//                        startActivity(new Intent(UsersActivity.this, Chat2Activity.class));

                    }
                });

            }
        });

        getUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
                userName = sharedPreferences.getString("username", "");
                String url = "http://chat-server-py-chat-server.1d35.starter-us-east-1.openshiftapps.com/users";
                StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        showFriendList(s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("un", userName);
                        return params;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(UsersActivity.this);
                rQueue.add(request);

                usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(UsersActivity.this,ChatActivity.class);
                        intent.putExtra("friendname",friendList.get(position));
                        intent.putExtra("username",userName);
                        startActivity(intent);

//                        startActivity(new Intent(UsersActivity.this, Chat2Activity.class));

                    }
                });

            }
        });


    }

    public void doOnSuccess(String s) {
        try {
            JSONObject obj = new JSONObject(s);
            if (obj.getBoolean("friend_added") == true) {
                Toast.makeText(UsersActivity.this, "Add Friend Successful", Toast.LENGTH_LONG).show();
            } else if (obj.getString("reason").equals("Friendship Already Exists")) {
                Toast.makeText(UsersActivity.this, "Friendship Already Exists", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void removeFriend(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.getBoolean("friend_removed") == true) {
                Toast.makeText(UsersActivity.this, "Remove Friend Successful", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void showFriendList(String s) {

        try {
            JSONObject obj = new JSONObject(s);
            if (obj.getString("status").equals("success")) {
                JSONArray users = obj.getJSONArray("users");
                friendList.clear();
                for (int i = 0; i < users.length(); i++) {
                    if (!users.getString(i).equals(userName)) {
                        friendList.add(users.getString(i));
                    }
                }

                noUsersText.setVisibility(View.GONE);
                usersList.setVisibility(View.VISIBLE);
                usersList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friendList));
            } else {
                noUsersText.setVisibility(View.VISIBLE);
                usersList.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if (!quit) { //询问退出程序
            Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
            new Timer(true).schedule(new TimerTask() { //启动定时任务
                @Override
                public void run() {
                    quit = false; //重置退出标识
                }
            }, 2000); //2秒后运行run()方法
            quit = true;
        } else { //确认退出程序
            super.onBackPressed();
            finish();
        }
    }
    }

