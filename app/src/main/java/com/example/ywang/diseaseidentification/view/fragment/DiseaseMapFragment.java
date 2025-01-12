package com.example.ywang.diseaseidentification.view.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cocosw.bottomsheet.BottomSheet;
import com.example.ywang.diseaseidentification.R;
import com.example.ywang.diseaseidentification.bean.MapImg;
import com.example.ywang.diseaseidentification.bean.OverLay;
import com.example.ywang.diseaseidentification.utils.SnackBarUtil;
import com.example.ywang.diseaseidentification.utils.baidumap.MyOrientationListener;
import com.example.ywang.diseaseidentification.utils.baidumap.WalkingRouteOverlay;
import com.example.ywang.diseaseidentification.view.activity.NearByActivity;
import com.example.ywang.diseaseidentification.view.activity.WNavigationGuideActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * 地图fragment
 */
public class DiseaseMapFragment extends Fragment implements BaiduMap.OnMarkerClickListener, OnGetPoiSearchResultListener {

    private static final int SELECT_PICTURE = 300;
    private static final String getOverlayUrls = "http://121.199.19.77:8080/test/GetAreaServlet"; //查询围栏
    private static final String postOverlayUrl = "http://121.199.19.77:8080/test/AddAreaServlet"; //添加围栏
    private static final String getMapImgs = "http://121.199.19.77:8080/test/GetPhotoServlet"; //查询地图相册
    private static final String postMapImg = "http://121.199.19.77:8080/test/AddPhotoServlet"; //添加地图相册
    private static final String uploadFileUrl = "http://121.199.19.77:8080/show/ImageUploadServlet";
    private boolean isFirstLoc = true;
    private float mCurrentX;

    /*经度纬度*/
    private double mLatitude;
    private double mLongitude;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private LocationClient mLocationClient = null;
    private MyLocationConfiguration.LocationMode mLocationMode;

    private FloatingActionsMenu mFloatingActionsMenu;
    private FloatingActionButton mapTypeBtn, locModeBtn, posQuickBtn, panoramaBtn;

    /*自定义图标*/
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener mMyOrientationListener;
    private PoiSearch mPoiSearch = null;

    private WalkingRouteOverlay overlay;
    private RoutePlanSearch routePlanSearch;
    private WalkNaviLaunchParam walkParam;
    private List<OverLay> overlayList = new ArrayList<>();

    public static DiseaseMapFragment newInstance() {
        Bundle bundle = new Bundle();
        DiseaseMapFragment fragment = new DiseaseMapFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_disease, container, false);
        //获取地图控件引用
        mMapView = view.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setIndoorEnable(true);
        initMap();
        initFloatButton(view);
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        //覆盖物对象
        overlay = new WalkingRouteOverlay(mBaiduMap);
        return view;
    }

    private void initMap() {
        //不显示地图上的比例尺
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);

        //放大到标尺50m
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.zoomTo(21.0f);
        mBaiduMap.setMapStatus(mapStatusUpdate);

        //定位初始化
        mLocationMode = MyLocationConfiguration.LocationMode.NORMAL; //定位模式
        mLocationClient = new LocationClient(getContext());
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);  //打开Gps
        option.setCoorType("bd09ll"); //设置坐标类型
        option.setScanSpan(1000);

        //设置LocationClientOptions
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();

        //初始化图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.navi_map_gps_locked);
        mMyOrientationListener = new MyOrientationListener(getContext());
        mMyOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
        mMyOrientationListener.start();
        mBaiduMap.setOnMarkerClickListener(this); //设置Marker点击事件
        new overLayDataAsync().execute(getOverlayUrls); /* 同步病害地理围栏 */
        new mapImgDataAsync().execute(getMapImgs); /* 同步病害地图marker */
    }

    private void initFloatButton(View view) {
        mFloatingActionsMenu = view.findViewById(R.id.map_actions_menu);
        mapTypeBtn = view.findViewById(R.id.change_map_type);
        mapTypeBtn.setIcon(R.drawable.ic_map_normal);
        mapTypeBtn.setTitle("卫星地图");
        mapTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBaiduMap.getMapType() == BaiduMap.MAP_TYPE_NORMAL) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    mapTypeBtn.setIcon(R.drawable.ic_map_statellite);
                    mapTypeBtn.setTitle("普通地图");
                } else {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    mapTypeBtn.setIcon(R.drawable.ic_map_normal);
                    mapTypeBtn.setTitle("卫星地图");
                }
                mFloatingActionsMenu.toggle();
            }
        });

        locModeBtn = view.findViewById(R.id.change_map_model);
        locModeBtn.setIcon(R.drawable.map_mode_compass);
        locModeBtn.setTitle("罗盘模式");
        locModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationMode == MyLocationConfiguration.LocationMode.NORMAL) {
                    mLocationMode = MyLocationConfiguration.LocationMode.COMPASS;
                    locModeBtn.setIcon(R.drawable.map_mode_normal);
                    locModeBtn.setTitle("定位模式");
                } else {
                    mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
                    locModeBtn.setIcon(R.drawable.map_mode_compass);
                    locModeBtn.setTitle("罗盘模式");
                }
                mFloatingActionsMenu.toggle();
            }
        });

        posQuickBtn = view.findViewById(R.id.position_quick);
        posQuickBtn.setIcon(R.drawable.ic_map_position);
        posQuickBtn.setTitle("快速定位");
        posQuickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = new LatLng(mLatitude, mLongitude);
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(latLng);
                Log.e("latitude", String.valueOf(mLatitude));
                Log.e("longitude", String.valueOf(mLongitude));
                MapStatusUpdate zoomStatus = MapStatusUpdateFactory.zoomTo(21.0f);
                mBaiduMap.animateMapStatus(status);
                mBaiduMap.animateMapStatus(zoomStatus);
                mFloatingActionsMenu.toggle();
            }
        });

        panoramaBtn = view.findViewById(R.id.position_panorama);
        panoramaBtn.setIcon(R.drawable.ic_add);
        panoramaBtn.setTitle("其他操作");
        panoramaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new BottomSheet.Builder(getActivity())
                        .title("请选择")
                        .sheet(R.menu.choose_item)
                        .listener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case R.id.add_pic: {  //添加地图相册
                                        if (ContextCompat.checkSelfPermission(getActivity(),
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(getActivity(), new String[]{
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            }, SELECT_PICTURE);
                                        } else {
                                            openAlbum();
                                        }
                                    }
                                    break;
                                    case R.id.fence:   //地理围栏
                                        addGeoFence();
                                        break;
                                    case R.id.panorama:  //附近农资店
                                        Intent nearByIntent = new Intent(getContext(), NearByActivity.class);
                                        nearByIntent.putExtra("latitude", mLatitude);
                                        nearByIntent.putExtra("longitude", mLongitude);
                                        startActivity(nearByIntent);
                                        break;
                                    case R.id.cancel_chose:  //取消
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();
                mFloatingActionsMenu.toggle();
            }
        });
    }


    /**
     * 添加地理围栏
     */
    private void addGeoFence() {
        final int[] result = new int[1];
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = View.inflate(getContext(), R.layout.dialog_select_fence, null);
        final EditText width = view.findViewById(R.id.fence_width);
        final EditText length = view.findViewById(R.id.fence_length);
        final TextView title = view.findViewById(R.id.fence_title);
        final RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = radioGroup.findViewById(i);
                result[0] = i;
            }
        });
        builder.setView(view);
        builder.setTitle("设置围栏信息");
        builder.setPositiveButton("创建", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (title.getText().equals("") || length.getText().toString().equals("") || width.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "请完善区域信息！", Toast.LENGTH_SHORT).show();
                } else {
                    OverLay overLay = new OverLay(String.valueOf(overlayList.size() + 1), "123456", title.getText().toString(),
                            length.getText().toString(), width.getText().toString(), mLatitude, mLongitude);
                    if (result[0] == R.id.radio_blue) {
                        addOverlaysToMap(Integer.parseInt(width.getText().toString()), Integer.parseInt(length.getText().toString()),
                                R.drawable.ground_overlay, title.getText().toString(), mLatitude, mLongitude);
                        overLay.setAreaType(0);
                    } else if (result[0] == R.id.radio_green) {
                        addOverlaysToMap(Integer.parseInt(width.getText().toString()), Integer.parseInt(length.getText().toString()),
                                R.drawable.ground_overlay_green, title.getText().toString(), mLatitude, mLongitude);
                        overLay.setAreaType(1);
                    } else if (result[0] == R.id.radio_red) {
                        addOverlaysToMap(Integer.parseInt(width.getText().toString()), Integer.parseInt(length.getText().toString()),
                                R.drawable.ground_overlay_red, title.getText().toString(), mLatitude, mLongitude);
                        overLay.setAreaType(2);
                    } else {
                        Toast.makeText(getContext(), "未选择区域警示颜色！请完善信息！", Toast.LENGTH_SHORT).show();
                    }
                    new addOverlayAsync().execute(overLay);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    class addOverlayAsync extends AsyncTask<OverLay, String, String> {

        @Override
        protected String doInBackground(OverLay... strings) {
            OverLay overLay = strings[0];
            StringBuilder response = new StringBuilder();
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(postOverlayUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("contentType", "GBK");
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes("AreaId=" + overLay.getAreaId() + "&UserId=" + overLay.getUserId() +
                        "&AreaName=" + URLEncoder.encode(overLay.getAreaName(), "utf-8") + "&AreaLength=" +
                        overLay.getAreaLength() + "&AreaWidth=" + overLay.getAreaWidth() + "&AreaLon=" +
                        overLay.getAreaLon() + "&AreaLat=" + overLay.getAreaLat() + "&AreaType=" + overLay.getAreaType());
                out.flush();
                out.close();
                //设置连接超时和读取超时的毫秒数
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getContext(), "添加成功！", Toast.LENGTH_SHORT).show();
        }
    }

    class mapImgDataAsync extends AsyncTask<String,String,List<MapImg>>{
        List<MapImg> imageList = new ArrayList<>();
        @Override
        protected List<MapImg> doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                //设置连接超时和读取超时的毫秒数
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    MapImg image = new MapImg();
                    image.setPhotoId(Integer.parseInt(line));
                    image.setUserId(reader.readLine());
                    image.setDiseaseName(reader.readLine());
                    image.setLat(Double.parseDouble(reader.readLine()));
                    image.setLog(Double.parseDouble(reader.readLine()));
                    image.setPhotoSrc(reader.readLine());
                    imageList.add(image);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return imageList;
        }

        @Override
        protected void onPostExecute(List<MapImg> mapImgs) {
            super.onPostExecute(mapImgs);
            for(MapImg mapImg : mapImgs){
                customizeMarkerIcon(mapImg);
            }
        }
    }

    /**
     * by moos on 2017/11/13
     * func:定制化marker的图标
     */
    private void customizeMarkerIcon(final MapImg mapImg){
        final View markerView = LayoutInflater.from(getContext()).inflate(R.layout.marker_bg,null);
        final ImageView icon = markerView.findViewById(R.id.marker_item_icon);
        Log.e("marker",mapImg.toString());
        Glide.with(getContext()).load(mapImg.getPhotoSrc())
                .apply(RequestOptions.bitmapTransform(new CenterCrop()))
                .into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                icon.setImageDrawable(resource);
                Bitmap bitmap = convertViewToBitmap(markerView);
                LatLng point = new LatLng(mapImg.getLat(), mapImg.getLog());
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                OverlayOptions options = new MarkerOptions()
                        .icon(bitmapDescriptor)
                        .position(point);
                Marker marker = (Marker) mBaiduMap.addOverlay(options);
                Bundle bundle = new Bundle();
                bundle.putString("pic", mapImg.getPhotoSrc());
                bundle.putDouble("latitude", mapImg.getLat());
                bundle.putDouble("longitude", mapImg.getLog());
                bundle.putString("disease_name",mapImg.getDiseaseName());
                marker.setExtraInfo(bundle);
            }
        });
    }

    /**
     * func:view转bitmap
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    class overLayDataAsync extends AsyncTask<String, String, List<OverLay>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<OverLay> doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                //设置连接超时和读取超时的毫秒数
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    OverLay overLay = new OverLay();
                    overLay.setAreaId(line);
                    overLay.setUserId(reader.readLine());
                    overLay.setAreaName(reader.readLine());
                    overLay.setAreaLength(reader.readLine());
                    overLay.setAreaWidth(reader.readLine());
                    overLay.setAreaLon(Double.parseDouble(reader.readLine()));
                    overLay.setAreaLat(Double.parseDouble(reader.readLine()));
                    overLay.setAreaType(Integer.parseInt(reader.readLine()));
                    overlayList.add(overLay);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return overlayList;
        }

        @Override
        protected void onPostExecute(List<OverLay> overlayList) {
            super.onPostExecute(overlayList);
            for (OverLay overLay : overlayList) {
                int resource = R.drawable.ground_overlay;
                if (overLay.getAreaType() == 1) {
                    resource = R.drawable.ground_overlay_green;
                } else if (overLay.getAreaType() == 2) {
                    resource = R.drawable.ground_overlay_red;
                }
                addOverlaysToMap(Integer.parseInt(overLay.getAreaWidth()), Integer.parseInt(overLay.getAreaWidth()),
                        resource, overLay.getAreaName(), overLay.getAreaLat(),
                        overLay.getAreaLon());
                Log.e("area", overLay.getAreaName());
            }
        }
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(getContext(), "未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {

            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            StringBuilder strInfo = new StringBuilder("在");
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo.append(cityInfo.city);
                strInfo.append(",");
            }
            strInfo.append("找到结果");
            Toast.makeText(getContext(), strInfo.toString(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        } else {

        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //mapView 销毁后不再处理新接受的位置
            if (bdLocation == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    //开始获取方向信息，顺时针0-360
                    .direction(mCurrentX)
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            MyLocationConfiguration configuration = new MyLocationConfiguration(mLocationMode, true, mIconLocation);
            mBaiduMap.setMyLocationConfiguration(configuration);

            /*更新经纬度*/
            mLatitude = bdLocation.getLatitude();
            mLongitude = bdLocation.getLongitude();

            if (isFirstLoc) {  //第一次定位
                LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(status);
                isFirstLoc = false;
            }
        }
    }

    //添加区域
    public void addOverlaysToMap(int width, int length, int backId, String title, double mLatitude, double mLongitude) {
        double latitude_1dp = 0.000008983152841195214;
        double longitude_1dp = 0.000009405717451407729;

        //定义Ground的显示地理范围
        LatLng southwest = new LatLng(mLatitude - width * latitude_1dp, mLongitude - length * longitude_1dp);
        LatLng northeast = new LatLng(mLatitude + width * latitude_1dp, mLongitude + length * longitude_1dp);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(northeast)
                .include(southwest)
                .build();

        //定义Ground显示的图片
        BitmapDescriptor bdGround = BitmapDescriptorFactory.fromResource(backId);
        //定义GroundOverlayOptions对象
        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds)
                .image(bdGround)
                .transparency(0.8f); //覆盖物透明度
        //在地图中添加Ground覆盖物
        mBaiduMap.addOverlay(ooGround);

        LatLng point = new LatLng(mLatitude, mLongitude);
        OverlayOptions textOptions = new TextOptions()
                .text(title)
                .bgColor(0xFFFFFFFF)
                .fontSize(65)
                .fontColor(0xFF1b4560)
                .position(point);
        mBaiduMap.addOverlay(textOptions);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    private void openAlbum() {
        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, SELECT_PICTURE);
        } else {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PICTURE);  //打开相册
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(getContext(), "必须同意所有权限才能使用该功能", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //选择相册
            case SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    //判断手机版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4版本以上的使用下面的方法进行处理
                        handleImageOnKitKat(data);
                    } else {
                        //4.4版本以下的使用下面的方法进行处理
                        handleImageBeforeKitkat(data);
                    }
                }
        }
    }

    //4.4版本以下的，选择相册的图片返回真实的Uri
    private void handleImageBeforeKitkat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        addMapPic(imagePath);
    }

    //4.4版本以上,选择相册中的图片不在返回图片真是的Uri了
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            //如果是Document类型的Uri,则通过document id 进行处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的Uri,则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri,直接获取图片路径即可
            imagePath = uri.getPath();
        }
        addMapPic(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            //如果是从第一个开始查起的
            if (cursor.moveToFirst()) {
                //获取储存下的所有图片
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            //关闭查找
            cursor.close();
        }
        //返回路径
        return path;
    }


    /*
    地图上添加图片
    */
    private void addMapPic(String imagePath) {
        if (imagePath != null) {
            new uploadFileAsync().execute(new File(imagePath));
            Bitmap newBitmap = decodeSampledBitmapFromFile(imagePath, 150, 180);
            LatLng point = new LatLng(mLatitude, mLongitude);
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(newBitmap);
            OverlayOptions options = new MarkerOptions()
                    .icon(bitmapDescriptor)
                    .position(point);
            Marker marker = (Marker) mBaiduMap.addOverlay(options);
            Bundle bundle = new Bundle();
            bundle.putString("pic", imagePath);
            bundle.putDouble("latitude", mLatitude);
            bundle.putDouble("longitude", mLongitude);
            marker.setExtraInfo(bundle);
        } else {
            Toast.makeText(getContext(), "failed to find imagePath", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Bundle bundle = marker.getExtraInfo();
        if (bundle != null) {
            String imagePath = bundle.getString("pic");
            Double latitude = bundle.getDouble("latitude", 0);
            Double longitude = bundle.getDouble("longitude", 0);
            String diseaseName = bundle.getString("disease_name");
            if (latitude == mLatitude && longitude == mLongitude) {
                Toast.makeText(getContext(), "始末位置不能相同！", Toast.LENGTH_SHORT).show();
            } else if (imagePath == null) {
                Toast.makeText(getContext(), "加载失败，请重新添加", Toast.LENGTH_SHORT).show();
            } else {
                showTheWay(new LatLng(latitude, longitude),diseaseName);
            }
        }
        return true;
    }

    private void showTheWay(final LatLng end,String diseaseName) {
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.removeFromMap();
                List<WalkingRouteLine> routeLines = walkingRouteResult.getRouteLines();
                overlay.setData(routeLines.get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });
        routePlanSearch.walkingSearch(getSearchWayParams(end));
        String title = "选择该" + diseaseName + "导航类型";
        new BottomSheet.Builder(getActivity())
                .title(title)
                .sheet(R.menu.choose_navigation)
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case R.id.navigation_ar:
                                startWalkNavigation(end, 1);
                                break;
                            case R.id.navigation_walk:
                                startWalkNavigation(end, 0);
                                break;
                            case R.id.navigation_bike:
                                Toast.makeText(getContext(), "该功能正在开发中....", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }).show();
    }

    private void startWalkNavigation(final LatLng end, final int code) {
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(getActivity(), new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d("Navigation", "WalkNavi engineInitSuccess");
                    routePlanWithWalkParam(end, code);
                }

                @Override
                public void engineInitFail() {
                    Log.d("Navigation", "WalkNavi engineInitFail");
                    WalkNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WalkingRoutePlanOption getSearchWayParams(final LatLng resultLoc) {
        WalkingRoutePlanOption params = new WalkingRoutePlanOption();
        LatLng nowLoc = new LatLng(mLatitude, mLongitude);
        params.from(PlanNode.withLocation(nowLoc));  //设置起点
        params.to(PlanNode.withLocation(resultLoc));  //设置终点
        return params;
    }

    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam(LatLng end, int code) {
        LatLng nowLoc = new LatLng(mLatitude, mLongitude);
        WalkNaviLaunchParam mParam = new WalkNaviLaunchParam().stPt(nowLoc).endPt(end);
        WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
        walkStartNode.setLocation(nowLoc);
        WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
        walkEndNode.setLocation(end);

        walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
        walkParam.extraNaviMode(code);

        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d("Navigation", "WalkNavigation onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                //算路成功
                //跳转至诱导页面
                Intent intent = new Intent(getActivity(), WNavigationGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError walkRoutePlanError) {
                Log.d("Navigation", "WalkNavigation onRoutePlanFail");
            }
        });
    }

    class uploadFileAsync extends AsyncTask<File, String, String> {

        @Override
        protected String doInBackground(File... strings) {
            String BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
            String PREFIX = "--", LINE_END = "\r\n";
            String CONTENT_TYPE = "multipart/form-data";   //内容类型
            try {
                URL url = new URL(uploadFileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10 * 1000000);
                conn.setConnectTimeout(10 * 1000000);
                conn.setDoInput(true);  //允许输入流
                conn.setDoOutput(true); //允许输出流
                conn.setUseCaches(false);  //不允许使用缓存
                conn.setRequestMethod("POST");  //请求方式
                conn.setRequestProperty("Charset", "utf-8");  //设置编码
                conn.setRequestProperty("connection", "keep-alive");
                conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
                if (strings[0] != null) {
                    /*
                     * 当文件不为空，把文件包装并且上传
                     */
                    OutputStream outputSteam = conn.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(outputSteam);
                    StringBuffer sb = new StringBuffer();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    /*
                     * 这里重点注意：
                     * name里面的值为服务器端需要key   只有这个key 才可以得到对应的文件
                     * filename是文件的名字，包含后缀名的   比如:abc.png
                     */

                    sb.append("Content-Disposition: form-data; name=\"img\"; filename=\"" + strings[0].getName() + "\"" + LINE_END);
                    sb.append("Content-Type: application/octet-stream; charset=" + "utf-8" + LINE_END);
                    sb.append(LINE_END);
                    dos.write(sb.toString().getBytes());

                    InputStream is = new FileInputStream(strings[0]);
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = is.read(bytes)) != -1) {
                        dos.write(bytes, 0, len);
                    }
                    is.close();
                    dos.write(LINE_END.getBytes());
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    dos.write(end_data);
                    dos.flush();

                    /*
                     * 获取响应码  200 = 成功
                     * 当响应成功，获取响应的流
                     */

                    int res = conn.getResponseCode();
                    if (res == 200) {
                        InputStream in = conn.getInputStream();
                        BufferedReader reader;
                        reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        return response.toString();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = View.inflate(getContext(), R.layout.dialog_select_pic, null);
            final TextView title = view.findViewById(R.id.pic_title);
            ImageView picImg = view.findViewById(R.id.pic_img);
            Glide.with(getContext()).load(s).into(picImg);
            builder.setView(view);
            builder.setTitle("设置病害信息");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MapImg img = new MapImg();
                    img.setPhotoSrc(s);
                    img.setUserId("123456");
                    img.setDiseaseName(title.getText().toString());
                    img.setLat(mLatitude);
                    img.setLog(mLongitude);
                    new addMapImgAsync().execute(img);
                }
            }).show();
        }
    }

    class addMapImgAsync extends AsyncTask<MapImg,String,String>{

        @Override
        protected String doInBackground(MapImg... mapImgs) {
            MapImg img = mapImgs[0];
            StringBuilder response = new StringBuilder();
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(postMapImg);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("contentType", "GBK");
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes("id=" + img.getPhotoId() + "&userid=" + img.getUserId() +
                        "&DiseaseName=" + URLEncoder.encode(img.getDiseaseName(), "utf-8") +
                        "&Lat=" + img.getLat() + "&Log=" + img.getLog() + "&PhotoSrc=" + img.getPhotoSrc());
                out.flush();
                out.close();
                //设置连接超时和读取超时的毫秒数
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                InputStream in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("test",s);
            if (s.equals("true")){
                Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getContext(), "添加失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mMyOrientationListener.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationClient.stop();
//        mMyOrientationListener.stop();
    }

    @Override
    public void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        mLocationClient.restart();
        super.onResume();
    }

    @Override
    public void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}