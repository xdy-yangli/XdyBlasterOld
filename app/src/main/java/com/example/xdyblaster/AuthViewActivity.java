package com.example.xdyblaster;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ZoomControls;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.SharedPreferencesUtils;
import com.example.xdyblaster.util.Zbqy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;

import static com.example.xdyblaster.util.FileFunc.getSDPath;

public class AuthViewActivity extends AppCompatActivity implements CustomAdapt {

    @BindView(R.id.list_auth)
    ListView listAuth;

    private static final LatLng GEO_GUANGZHOU = new LatLng(23.155, 113.264);
    private static final LatLng GEO_GUANGXI = new LatLng(23.904456, 106.597999);
    @BindView(R.id.bt_delete)
    Button btDelete;
    @BindView(R.id.bt_view)
    Button btView;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0x40FFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    private SensorManager mSensorManager;
    //private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    TextureMapView mMapView;
    BaiduMap mBaiduMap;
    RadioGroup.OnCheckedChangeListener radioButtonListener;
    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    private float direction;
    private LatLng latLng = null;
    private Marker mMarkerA;
    private int listSelection = 0;
    List<String> listFileName = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    List<Zbqy> listZbqy = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_auth_view);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String str = intent.getExtras().getString("file");
        initFileList(str);


//        str = FileFunc.loadAuthFile(str);
//        try {
//            JSONObject jsonObject = new JSONObject(str);
//            JSONObject jsonLgs = jsonObject.getJSONObject("lgs");
//            JSONObject lg;
//            jsonObject.getString("cwxx");
//            JSONArray lgArray = jsonLgs.getJSONArray("lg");
//            for (int i = 0; i < lgArray.length(); i++) {
//                lg = lgArray.getJSONObject(i);
//                text.add(lg.getString("uid"));
//                Log.e("http", "lg " + lg.getString("uid"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 地图初始化
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //   mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        ImageView v = new ImageView(this);
        v.setImageDrawable(getDrawable(R.drawable.icon_gcoding));
        mCurrentMarker = BitmapDescriptorFactory.fromViewWithDpi(v, 360);
        MyLocationConfiguration locationConfiguration = new MyLocationConfiguration(mCurrentMode,
                true, mCurrentMarker, accuracyCircleFillColor, accuracyCircleStrokeColor);
        mBaiduMap.setMyLocationConfiguration(locationConfiguration);
        mBaiduMap.setMyLocationEnabled(true);
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
    }


    public void initFileList(String str) {
        int i, j, k, len;
        String tmp0, tmp1;
        File file = new File(getSDPath() + "//xdyBlaster//auth");
        if (!file.exists()) {
            file.mkdir();
        }
        File[] files = file.listFiles();
        listFileName.clear();
        j = 0;
        k = 0;
        if (files.length != 0) {
            for (i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    tmp0 = files[i].getName().toString();
                    len = tmp0.lastIndexOf(".");
                    if (len != -1) {
                        tmp1 = tmp0.substring(len + 1, tmp0.length());
                        if (tmp1.equals("json")) {
                            tmp1 = tmp0.substring(0, len);
                            listFileName.add(tmp1);
                            if (tmp1.equals(str))
                                k = j;
                            j++;
                        }
                    }
                }
            }
        }
        Collections.sort(listFileName);
        Collections.reverse(listFileName);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_file, listFileName);
        listAuth.setAdapter(arrayAdapter);
        listAuth.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (listAuth.getCount() != 0) {
            {
                int finalK = k;
                listAuth.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listAuth.getCount() != 0) {
                            listAuth.setItemChecked(finalK, true);
                            listAuth.setSelection(finalK);
                            setMapPosition(finalK);
                        }
                    }
                });
            }
        }
        listAuth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setMapPosition(position);
            }
        });
    }

    public void setMapPosition(int n) {
        String str = FileFunc.loadAuthFile(listFileName.get(n));
        SharedPreferencesUtils.setParam(AuthViewActivity.this, "htid file", listFileName.get(n));
        try {
            JSONObject all = new JSONObject(str);
            JSONObject jsonObject = all.getJSONObject("lgxx");
            JSONObject zbqys = jsonObject.getJSONObject("zbqys");
            JSONArray zbqy = zbqys.getJSONArray("zbqy");
            listZbqy.clear();
            JSONObject zb;
            Zbqy z;
            mBaiduMap.clear();
            for (int i = 0; i < zbqy.length(); i++) {
                zb = zbqy.getJSONObject(i);
                mCurrentAccracy = Float.parseFloat(zb.getString("zbqybj"));
                mCurrentLat = Double.parseDouble(zb.getString("zbqywd"));
                mCurrentLon = Double.parseDouble(zb.getString("zbqyjd"));
                z = new Zbqy();
                z.setmCurrentAccracy(mCurrentAccracy);
                z.setmCurrentLat(mCurrentLat);
                z.setmCurrentLon(mCurrentLon);
                listZbqy.add(z);
                MarkerOptions option = new MarkerOptions().icon(mCurrentMarker).position(new LatLng(mCurrentLat, mCurrentLon));
                option.animateType(MarkerOptions.MarkerAnimateType.none);
                option.zIndex(0);
                mBaiduMap.addOverlay(option);
                LatLng llCircle = new LatLng(mCurrentLat, mCurrentLon);
                OverlayOptions ooCircle = new CircleOptions()
                        .fillColor(0x30689F38)
                        .center(llCircle)
                        .stroke(new Stroke(2, 0x30689F38))
                        .radius((int) mCurrentAccracy);
                mBaiduMap.addOverlay(ooCircle);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        MapStatusUpdate mapStatusUpdate;
        //把定位点再次显现出来
        mapStatusUpdate = MapStatusUpdateFactory.newLatLng(new LatLng(mCurrentLat, mCurrentLon));
        mBaiduMap.animateMapStatus(mapStatusUpdate);


//
//        latLng = new LatLng(mCurrentLat, mCurrentLon);
//        locData = new MyLocationData.Builder()
//                .accuracy(mCurrentAccracy)
//                // 此处设置开发者获取到的方向信息，顺时针0-360
//                .direction(0).latitude(latLng.latitude)
//                .longitude(latLng.longitude).build();
//        mBaiduMap.setMyLocationData(locData);
//        mBaiduMap.setMyLocationEnabled(true);
//
////        mBaiduMap.addOverlay(new MarkerOptions()
////                .position(latLng)
////                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)));
//        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(13.0f);
        mBaiduMap.setMapStatus(msu);
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 500;
    }

    @Override
    protected void onDestroy() {
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @OnClick({R.id.bt_delete, R.id.bt_view})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_delete:
                if (listFileName.size() == 0)
                    break;
                int i = listAuth.getCheckedItemPosition();
                if (i == -1)
                    break;
                File file = new File(getSDPath() + "//xdyBlaster//auth//" + listFileName.get(i) + ".json");
                if (file.exists() && file.isFile())
                    file.delete();
                listFileName.remove(i);
                arrayAdapter.notifyDataSetChanged();
                if (listAuth.getCount() > 0) {
                    if (listAuth.getCount() == i)
                        i--;
                    listAuth.setItemChecked(i, true);
                    listAuth.setSelection(i);
                    setMapPosition(i);
                }
                break;
            case R.id.bt_view:
                if ((listAuth.getCount() != 0) && listAuth.getCheckedItemPosition() != -1) {
                    Intent intent = new Intent(AuthViewActivity.this, LgDataActivity.class);
                    intent.putExtra("file", listFileName.get(listAuth.getCheckedItemPosition()));
                    startActivity(intent);
                }

                break;
        }
    }

}
