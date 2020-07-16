package com.example.ywang.diseaseidentification.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.example.ywang.diseaseidentification.utils.iflytek.RecognitionManager;
import com.example.ywang.diseaseidentification.utils.iflytek.SynthesisManager;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MyApplication extends Application {

    public static String TAG = "Init";
    public BMapManager mBMapManager;
    private static MyApplication mInstance = null;

    //设置APPID/AK/SK
    public static final String APP_ID = "17747286";
    public static final String API_KEY = "cjWrIRKGpcwLM4AUfW8LTopn";
    public static final String SECRET_KEY = "VPeZ3SNXgM2p9NdIgOuFdSUEPQUtzQ9q";

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(this,SpeechConstant.APPID + "=5d7360d3");
        RecognitionManager.getSingleton().init(this,"5d7360d3");
        SynthesisManager.getSingleton().init(this,"5d7360d3");
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(this);
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        mInstance = this;
        initEngineManager(this);
        initImageLoader(this);
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(MyApplication.getInstance().getApplicationContext(), "BMapManager初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "initEngineManager");
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    public static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetPermissionState(int iError) {
            //非零值表示key验证未通过
            if (iError != 0) { // 授权Key错误：
                Log.d(TAG, "请在AndroidManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError);
            } else {
                Log.d(TAG, "key认证成功");
            }
        }
    }

    /**
     * 初始化ImageLoader
     */
    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
                //.memoryCache(new WeakMemoryCache())
                .denyCacheImageMultipleSizesInMemory()
                .tasksProcessingOrder( QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
