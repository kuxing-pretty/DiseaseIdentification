package com.example.ywang.diseaseidentification.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.ywang.diseaseidentification.R;
import com.example.ywang.diseaseidentification.adapter.TitleAdapter;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;
import com.youth.banner.transformer.CubeOutTransformer;
import java.util.ArrayList;
import java.util.List;

public class AgricultureNewsFragment extends Fragment{

    private ViewPager mViewPager;

    private List<Fragment> mFragments = new ArrayList<>(  );

    private String[] mTitles = {"农业头条","疾病防治","种植技巧"};
    private static String agricultureMainUrl = "http://121.199.19.77:8080/show/GetNewsOneServlet";
    private static String agricultureControlUrl = "http://121.199.19.77:8080/show/GetNewsTwoServlet";
    private static String agricultureMethodUrl = "http://121.199.19.77:8080/show/GetNewsThreeServlet";

    private List<String> images = new ArrayList<>();
    private List<String> titles = new ArrayList<>();

    public static AgricultureNewsFragment newInstance(){
        Bundle bundle = new Bundle();
        AgricultureNewsFragment fragment = new AgricultureNewsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragments.add(NewsFragment.newInstance(agricultureMainUrl));
        mFragments.add(NewsFragment.newInstance(agricultureControlUrl));
        mFragments.add(NewsFragment.newInstance(agricultureMethodUrl));
        images.add("http://img8.agronet.com.cn/Users/100/616/407/20199301420538304.jpg");
        titles.add("巴南发展“稻田养蛙” 带领村民走上致富路（图）");
        images.add("http://img8.agronet.com.cn/Users/100/616/407/2019930152841634.jpg");
        titles.add("我国农民合作社和家庭农场持续健康发展（图）");
        images.add("http://img8.agronet.com.cn/Users/100/616/407/20199301524244597.jpg");
        titles.add("商务部：国庆节前再投放储备猪肉1万吨（图）");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_agriculture,container,false);
        mViewPager = view.findViewById( R.id.news_home_viewpager );
        TabLayout mTabLayout = view.findViewById(R.id.tab_layout);
        TitleAdapter mAdapter = new TitleAdapter(getFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mAdapter);
        //CubeInTransformer 内旋
        //FlipHorizontalTransformer 像翻书一样
        //AccordionTransformer  风琴 拉压
        mViewPager.setPageTransformer(true,new CubeOutTransformer());
        mViewPager.setCurrentItem(0);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setupWithViewPager(mViewPager);
        FloatingActionButton mFloatingActionButton = view.findViewById(R.id.fab_news);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mViewPager.getCurrentItem();

            }
        } );
        Banner banner = view.findViewById(R.id.banner);
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        //设置图片集合
        banner.setImages(images);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //banner设置方法全部调用完毕时最后调用
        banner.setBannerAnimation(Transformer.DepthPage);
        //设置标题集合（当banner样式有显示title时）
        banner.setBannerTitles(titles);
        //设置轮播时间
        banner.setDelayTime(1500);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.start();
        return view;
    }

    public static class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {

            /**
             注意：
             1.图片加载器由自己选择，这里不限制，只是提供几种使用方法
             2.返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
             传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
             切记不要胡乱强转！
             **/

            //Glide 加载图片简单用法
            Glide.with(context).load((String)path).into(imageView);

            //用fresco加载图片简单用法，记得要写下面的createImageView方法
            Uri uri = Uri.parse((String) path);
            imageView.setImageURI(uri);
        }

        //提供createImageView 方法，如果不用可以不重写这个方法，主要是方便自定义ImageView的创建
        @Override
        public ImageView createImageView(Context context) {
            //使用fresco，需要创建它提供的ImageView，当然你也可以用自己自定义的具有图片加载功能的ImageView
            return new ImageView(context);
        }
    }
}
