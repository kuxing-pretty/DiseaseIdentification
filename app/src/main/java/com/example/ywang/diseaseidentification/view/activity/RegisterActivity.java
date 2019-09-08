package com.example.ywang.diseaseidentification.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ywang.diseaseidentification.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameText,mEmailtText,mPasswordText;
    public static final int SHOW_RESPONSE = 1;
    private String name,password;
    private AppCompatButton registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mNameText = (EditText) findViewById(R.id.input_name);
        mEmailtText = (EditText) findViewById(R.id.input_email);
        mPasswordText = (EditText) findViewById(R.id.input_password);
        registerBtn = (AppCompatButton) findViewById(R.id.btn_signup);

        mPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = mNameText.getText().toString().trim();
                password = mPasswordText.getText().toString().trim();
                sendRequestWithHttpURLConnection(name,password);
            }
        });
    }

    private void sendRequestWithHttpURLConnection(final String id,final String pw){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try{
                    URL url = new URL("http://101.37.79.26:8080/twoweb/SearchServlet");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("id="+id+"&password="+pw);
                    out.flush();
                    out.close();

                    //设置连接超时和读取超时的毫秒数
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    InputStream in = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine())!=null){
                        response.append(line);
                    }
                    //将 StringBuilder转为String
                    String r = response.toString();
                    Message message=new Message();
                    message.what = SHOW_RESPONSE;
                    message.obj = r;
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(reader!=null){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    public Handler handler=new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_RESPONSE:
                    String response=(String)msg.obj;
                    if(response.equals("注册成功！ ")){
                        Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else {
                        Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}