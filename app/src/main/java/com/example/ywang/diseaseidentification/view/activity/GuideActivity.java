package com.example.ywang.diseaseidentification.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ywang.diseaseidentification.R;
import com.example.ywang.diseaseidentification.adapter.UserBeanAdapter;
import com.example.ywang.diseaseidentification.bean.baseData.UserBean;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mAccountView;
    private EditText mPasswordView;
    private ImageView mClearAccountView;
    private ImageView mClearPasswordView,mBtnQq,mBtnWeChat;
    private CheckBox mEyeView;
    private CheckBox mDropDownView;
    private Button mLoginView,mSkipView;
    private TextView mForgetPsdView;
    private TextView mRegisterView;
    private LinearLayout mTermsLayout;
    private TextView mTermsView;
    private RelativeLayout mPasswordLayout;
    private List<View> mDropDownInvisibleViews;
    private TextView name,title;

    private static String APP_ID = "101801728";
    private static Tencent mTencent;
    public static final int SHOW_RESPONSE = 1;
    private String account,password;
    private static final int PERMISSION_CODE = 100;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        findViewId();

        initDropDownGroup();
        mTencent = Tencent.createInstance(APP_ID, GuideActivity.this);
        mPasswordView.setLetterSpacing(0.2f);
        mClearAccountView.setOnClickListener(this);
        mClearPasswordView.setOnClickListener(this);

        mEyeView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    mPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        mAccountView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //当账号栏正在输入状态时，密码栏的清除按钮和眼睛按钮都隐藏
                if(hasFocus){
                    mClearPasswordView.setVisibility(View.INVISIBLE);
                    mEyeView.setVisibility(View.INVISIBLE);
                }else {
                    mClearPasswordView.setVisibility(View.VISIBLE);
                    mEyeView.setVisibility(View.VISIBLE);
                }
            }
        });

        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //当密码栏为正在输入状态时，账号栏的清除按钮隐藏
                if(hasFocus)
                    mClearAccountView.setVisibility(View.INVISIBLE);
                else mClearAccountView.setVisibility(View.VISIBLE);
            }
        });

        mDropDownView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //下拉按钮点击的时候，密码栏、忘记密码、新用户注册、同意服务条款先全部隐藏
                    setDropDownVisible(View.INVISIBLE);
                    //下拉箭头变为上拉箭头
                    //弹出一个popupWindow
                    showDropDownWindow();
                }else {
                    setDropDownVisible(View.VISIBLE);
                }
            }
        });

        mDropDownView.setOnClickListener(this);
        mForgetPsdView.setOnClickListener(this);
        mTermsView.setOnClickListener(this);
        mLoginView.setOnClickListener(this);
        mRegisterView.setOnClickListener(this);
        mSkipView.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        }
        requestPermission();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        }
    }

    //所有权限统一申请
    private void requestPermission(){
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_PHONE_STATE

        };
        try{
            for (int i = 0; i < permissions.length; i++) {
                if(ContextCompat.checkSelfPermission(this,permissions[i]) != PackageManager.PERMISSION_GRANTED){
                    int permission = ActivityCompat.checkSelfPermission(this,permissions[i]);
                    if (permission != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this,permissions,PERMISSION_CODE);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void findViewId() {
        mAccountView = findViewById(R.id.et_input_account);
        mPasswordView = findViewById(R.id.et_input_password);
        mClearAccountView = findViewById(R.id.iv_clear_account);
        mClearPasswordView = findViewById(R.id.iv_clear_password);
        mEyeView = findViewById(R.id.iv_login_open_eye);
        mDropDownView = findViewById(R.id.cb_login_drop_down);
        mLoginView = findViewById(R.id.btn_login);
        mForgetPsdView = findViewById(R.id.tv_forget_password);
        mRegisterView = findViewById(R.id.tv_register_account);
        mTermsLayout = findViewById(R.id.ll_terms_of_service_layout);
        mTermsView = findViewById(R.id.tv_terms_of_service);
        mPasswordLayout = findViewById(R.id.rl_password_layout);
        mSkipView = findViewById(R.id.btn_skip);
        mBtnQq = findViewById(R.id.login_qq);
        name = findViewById(R.id.main_name);
        title = findViewById(R.id.main_title);
        mBtnWeChat = findViewById(R.id.login_we_chat);
        mBtnQq.setOnClickListener(this);
        mBtnWeChat.setOnClickListener(this);
        Typeface typeface = Typeface.createFromAsset(this.getAssets(),"fly.ttf");
        name.setTypeface(typeface);
        title.setTypeface(typeface);
    }

    private void initDropDownGroup() {
        mDropDownInvisibleViews = new ArrayList<>();
        mDropDownInvisibleViews.add(mPasswordView);
        mDropDownInvisibleViews.add(mForgetPsdView);
        mDropDownInvisibleViews.add(mRegisterView);
        mDropDownInvisibleViews.add(mPasswordLayout);
        mDropDownInvisibleViews.add(mLoginView);
        mDropDownInvisibleViews.add(mTermsLayout);
    }

    private void setDropDownVisible(int visible) {
        for (View view:mDropDownInvisibleViews){
            view.setVisibility(visible);
        }
    }

    private void showDropDownWindow() {
        final PopupWindow window = new PopupWindow(this);
        //下拉菜单里显示上次登录的账号，在这里先模拟获取有记录的用户列表
        List<UserBean> userBeanList = new ArrayList<>();
        //添加测试用账号
        userBeanList.add(new UserBean("123456","123456"));
        userBeanList.add(new UserBean("1234567","1234567"));
        //配置ListView的适配器
        final UserBeanAdapter adapter = new UserBeanAdapter(this);
        adapter.replaceData(userBeanList);
        //初始化ListView
        ListView userListView = (ListView) View.inflate(this,
                R.layout.window_account_drop_down,null);
        userListView.setAdapter(adapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //当下拉列表条目被点击时，显示刚才被隐藏视图,下拉箭头变上拉箭头
                //相当于mDropDownView取消选中
                mDropDownView.setChecked(false);
                //账号栏和密码栏文本更新
                UserBean checkedUser = adapter.getItem(position);
                mAccountView.setText(checkedUser.getUserId());
                mPasswordView.setText(checkedUser.getUserPassword());
                //关闭popupWindow
                window.dismiss();
            }
        });
        //添加一个看不见的FooterView，这样ListView就会自己在倒数第一个（FooterView）上边显示Divider，
        //进而在UI上实现最后一行也显示分割线的效果了
        userListView.addFooterView(new TextView(this));
        //配置popupWindow并显示
        window.setContentView(userListView);
        window.setAnimationStyle(0);
        window.setBackgroundDrawable(null);
        window.setWidth(mPasswordLayout.getWidth());
        window.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setOutsideTouchable(true);
        window.showAsDropDown(mAccountView);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //当点击popupWindow之外区域导致window关闭时，显示刚才被隐藏视图，下拉箭头变上拉箭头
                //相当于mDropDownView取消选中
                mDropDownView.setChecked(false);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_skip:   //跳过
                finish();
                startActivity(new Intent(GuideActivity.this,MainActivity.class));
                break;
            case R.id.tv_register_account:  //注册
                startActivity(new Intent(GuideActivity.this,RegisterActivity.class));
                break;
            case R.id.btn_login:  //登录
                account = mAccountView.getText().toString().trim();
                password = mPasswordView.getText().toString().trim();
                if(account.equals("")){
                    Toast.makeText(this, "账号不能为空！", Toast.LENGTH_SHORT).show();
                }else if(password.equals("")){
                    Toast.makeText(this, "密码不能为空！", Toast.LENGTH_SHORT).show();
                }else{
                    sendRequestWithHttpURLConnection(account,password);
                }
                break;
            case R.id.tv_terms_of_service:  //服务条款
                Toast.makeText(this, "正在开发中...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_forget_password:  //忘记密码
                Toast.makeText(this, "正在开发中...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_clear_account:  //清除账号
                mAccountView.setText("");
                break;
            case R.id.iv_clear_password:  //清楚密码：
                mPasswordView.setText("");
                break;
            case R.id.cb_login_drop_down:  //下拉点击

                break;
            case R.id.login_qq:  //QQ登录
                login_qq();
                break;
            case R.id.login_we_chat:  //微信登录

                break;
            default:
                break;
        }
    }

    /*
     *使用HttpURLConnection访问网络：
     * 1.获取HttpURLConnection实例
     * 2.设置http请求的方法：GRT:获取数据, POST:提交数据
     * 3.DataOutStream输出流提交数据，InputStream输入流读取数据
     * 4.关闭HTTP连接
     * */
    private void sendRequestWithHttpURLConnection(final String id, final String pw) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("http://121.199.19.77:8080/show/SearchServlet");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.writeBytes("ID="+id+"&PW="+pw);
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
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    message.obj = r;
                    handler.sendMessage(message);
                } catch (Exception e) {
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
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_RESPONSE:
                    String response = (String)msg.obj;
                    if(response.equals("true")){
                        UserBean bean = new UserBean(account,password);
                        UserBean.setIsLogin(true);
                        Intent intent = new Intent(GuideActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(GuideActivity.this, response +"登录失败！请检查账号跟密码是否正确", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void login_qq(){
        if(!mTencent.isSessionValid()){
            mTencent.login(GuideActivity.this,"all",loginListener);
        }
    }


    //新建BaseListener实例重写doComplete方法
    IUiListener loginListener = new BaseUiListener(){
        @Override
        protected void doComplete(JSONObject values) {
            initOpenidAndToken(values);
            Log.e("结果updateUserInfo","OnComplete成功");
            JSONObject jsonObject = values;
            Toast.makeText(GuideActivity.this,"授权成功",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(GuideActivity.this,MainActivity.class));
        }
    };

    private class BaseUiListener implements IUiListener {

        protected void doComplete(JSONObject values) {
            Log.e("result",values.toString());
        }

        @Override
        public void onComplete(Object response) {
            if (response == null){
                Log.e("QQ ---> BaseListener","返回为空，登录失败");
                return;
            }
            JSONObject jsonObject = (JSONObject) response;
            if (jsonObject.length() == 0) {
                Log.e("QQ ---> BaseListener","返回为空，登录失败2");
                return;
            }
            Log.i("登陆成功",response.toString());
            doComplete(jsonObject);
        }

        @Override
        public void onError(UiError e) {
            Log.e("onError:", "code:" + e.errorCode + ", msg:"
                    + e.errorMessage + ", detail:" + e.errorDetail);
        }
        @Override
        public void onCancel() {
            Log.e("onCancel", "");
        }
    }

    //QQ获取用户OPENID和TOKEN
    public static void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
                Log.i("OPENID",openId);
            }
        } catch(Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode,resultCode,data,loginListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 100:  //所有权限
                if (grantResults.length > 0){
                    //循环遍历
                    for(int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须同意所有权限才能使用该功能", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;

        }
    }
}