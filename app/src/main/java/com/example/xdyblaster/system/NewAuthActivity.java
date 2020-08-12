package com.example.xdyblaster.system;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ZoomControls;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.example.xdyblaster.R;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.HttpUtil;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.SharedPreferencesUtils;

import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import okhttp3.Response;

public class NewAuthActivity extends AppCompatActivity implements CustomAdapt {
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    @BindView(R.id.etHtid)
    EditText etHtid;
    @BindView(R.id.etXmbh)
    EditText etXmbh;
    @BindView(R.id.etDwdm)
    EditText etDwdm;
    @BindView(R.id.etFbh)
    EditText etFbh;
    @BindView(R.id.etCount)
    EditText etCount;
    @BindView(R.id.etBj)
    EditText etBj;
    @BindView(R.id.etJd)
    EditText etJd;
    @BindView(R.id.etWd)
    EditText etWd;
    @BindView(R.id.bt_delete)
    Button btDelete;
    @BindView(R.id.bt_view)
    Button btView;
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker, ma;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
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
    private LatLng latLng2 = null;
    int which = 0;
    LinkedHashMap<String, String> httpParams;
    String head;
    AlertDialog dialog;
    String htid;
    String xmbh;
    String dwdm;
    String fbh;
    String count;
    String bj;
    String jd;
    String wd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_auth);
        ButterKnife.bind(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 地图初始化
        mMapView = (TextureMapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        ImageView v = new ImageView(this);
        v.setImageDrawable(getDrawable(R.drawable.icon_gcoding));
        ma = BitmapDescriptorFactory.fromViewWithDpi(v, 360);


        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMapClick(LatLng latLng) {
                mBaiduMap.clear();

                MarkerOptions option = new MarkerOptions().icon(ma).position(latLng);

                //生长动画

                option.animateType(MarkerOptions.MarkerAnimateType.none);

                //在地图上添加Marker，并显示

                mBaiduMap.addOverlay(option);

                //设置Marker覆盖物的ZIndex

                option.zIndex(0);
                etJd.setText(String.format("%.5f", latLng.longitude));
                etWd.setText(String.format("%.5f", latLng.latitude));
                latLng2 = latLng;

            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {

            }
        });
        etHtid.setText((String) SharedPreferencesUtils.getParam(this, "new_htid", "HT-XDY2020001"));
        etXmbh.setText((String) SharedPreferencesUtils.getParam(this, "new_xmbh", "XM-XDY2020001"));
        etDwdm.setText((String) SharedPreferencesUtils.getParam(this, "new_dwdm", "XDY0001"));
        etFbh.setText((String) SharedPreferencesUtils.getParam(this, "new_fbh", "5320412A00001"));
        etCount.setText((String) SharedPreferencesUtils.getParam(this, "new_count", "100"));
        etBj.setText((String) SharedPreferencesUtils.getParam(this, "new_bj", "1000"));
        etJd.setText((String) SharedPreferencesUtils.getParam(this, "new_jd", "113"));
        etWd.setText((String) SharedPreferencesUtils.getParam(this, "new_wd", "23"));
//        etHtid.setFilters(new InputFilter[]{etHtid.getFilters()[0], mInputFilter});
//        etXmbh.setFilters(new InputFilter[]{etXmbh.getFilters()[0], mInputFilter});
//        etDwdm.setFilters(new InputFilter[]{etDwdm.getFilters()[0], mInputFilter});
//        etFbh.setFilters(new InputFilter[]{etFbh.getFilters()[0], mInputFilter});
//        etHtid.setOnEditorActionListener(onEditorActionListener);
//        etXmbh.setOnEditorActionListener(onEditorActionListener);
//        etDwdm.setOnEditorActionListener(onEditorActionListener);
//        etFbh.setOnEditorActionListener(onEditorActionListener);
//        etCount.setOnEditorActionListener(onEditorActionListener);
//        etBj.setOnEditorActionListener(onEditorActionListener);

    }


    @OnClick({R.id.bt_delete, R.id.bt_view})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_delete:
                MapStatusUpdate mapStatusUpdate;
                if (which == 1) {
                    if (latLng == null)
                        break;
                    //把定位点再次显现出来
                    mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
                    mBaiduMap.animateMapStatus(mapStatusUpdate);
                    which = 0;
                } else {
                    if (latLng2 == null)
                        break;
                    //把定位点再次显现出来
                    mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng2);
                    mBaiduMap.animateMapStatus(mapStatusUpdate);
                    which = 1;
                }
                mapStatusUpdate = MapStatusUpdateFactory.zoomTo(18.0f);
                mBaiduMap.animateMapStatus(mapStatusUpdate);
                break;
            case R.id.bt_view:
                htid = etHtid.getText().toString();
                xmbh = etXmbh.getText().toString();
                dwdm = etDwdm.getText().toString();
                fbh = etFbh.getText().toString();
                count = etCount.getText().toString();
                bj = etBj.getText().toString();
                jd = etJd.getText().toString();
                wd = etWd.getText().toString();

                if (!FileFunc.checkUuidString(etFbh.getText().toString())) {
                    showError("编码错误！");
                    break;
                }
                if (count.isEmpty()) {
                    showError("发数错误！");
                    break;
                }
                if (Integer.parseInt(count) == 0) {
                    showError("发数错误！");
                    break;
                }
                if (bj.isEmpty()) {
                    showError("半径错误！");
                    break;
                }
                if (Integer.parseInt(bj) == 0) {
                    showError("半径错误！");
                    break;
                }
                SharedPreferencesUtils.setParam(this, "new_htid", htid);
                SharedPreferencesUtils.setParam(this, "new_xmbh", xmbh);
                SharedPreferencesUtils.setParam(this, "new_dwdm", dwdm);
                SharedPreferencesUtils.setParam(this, "new_fbh", fbh);
                SharedPreferencesUtils.setParam(this, "new_count", count);
                SharedPreferencesUtils.setParam(this, "new_bj", bj);
                SharedPreferencesUtils.setParam(this, "new_jd", jd);
                SharedPreferencesUtils.setParam(this, "new_wd", wd);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectDialog();
                        HttpReadWrite httpReadWrite = new HttpReadWrite();
                        httpReadWrite.execute(1);
                    }
                });

                break;
        }
    }

    private void connectDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_connect, null, false);
        dialog = new AlertDialog.Builder(this).setView(view).create();
        Window win = dialog.getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        win.setDimAmount(0.4f);
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        layoutParams.alpha = 0.9f;
        win.setAttributes(layoutParams);
        TextView tvConnect = view.findViewById(R.id.textView_connect_Status);
        tvConnect.setText("连接服务器");
        dialog.show();

        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4  注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
    }

    public class MyLocationListenner extends BDAbstractLocationListener {

        @SuppressLint("DefaultLocale")
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                MarkerOptions option = new MarkerOptions().icon(ma).position(latLng);
                option.animateType(MarkerOptions.MarkerAnimateType.none);
                mBaiduMap.addOverlay(option);
                option.zIndex(0);
                etJd.setText(String.format("%.5f", latLng.longitude));
                etWd.setText(String.format("%.5f", latLng.latitude));
                latLng2 = latLng;

            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 600;
    }

    private void showError(String str) {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getSupportFragmentManager(), "info");
    }

    @SuppressLint("StaticFieldLeak")
    public class HttpReadWrite extends AsyncTask<Integer, Object, Integer> {

        @Override
        protected Integer doInBackground(Integer... integers) {
            while(!dialog.isShowing());
            Response response = null;
            httpParams = new LinkedHashMap<>();
            httpParams.put("htid", htid);
            httpParams.put("xmbh", xmbh);
            httpParams.put("dwdm", dwdm);
            httpParams.put("sbbh", "XDY0001");
            httpParams.put("zbqymc", "001");
            httpParams.put("zbqyjd", jd);
            httpParams.put("zbqywd", wd);
            httpParams.put("zbqybj", bj);
            httpParams.put("zbqssj", "001");
            httpParams.put("zbjzsj", "001");
            httpParams.put("jbqyjd", "001");
            httpParams.put("jbqywd", "001");
            httpParams.put("jbqybj", "001");
            httpParams.put("jbqssj", "001");
            httpParams.put("jbjzsj", "001");
            head = "add=hetong&hetong=";
//            response = HttpUtil.sendPostMessage(1, head);
            response = HttpUtil.sendPostMessage(1, head, httpParams);
            if (response == null) {
                publishProgress(1, -1);
                return null;
            }
            response = null;
            String uid = FileFunc.getRealUuid(fbh);
            httpParams = new LinkedHashMap<>();
            httpParams.put("htid", htid);
            httpParams.put("xmbh", xmbh);
            httpParams.put("dwdm", dwdm);
            httpParams.put("sbbh", "XDY0001");
            httpParams.put("fbh", fbh.substring(0, 8));
            httpParams.put("uidf", uid.substring(0, 8));
            httpParams.put("uidb", fbh.substring(8, 13));
            httpParams.put("uidt", count);
            httpParams.put("gzm", "0");
            httpParams.put("yxq", "200601");
            httpParams.put("gzmcwxx", "0");
            head = "add=lg&lg=";
            response = HttpUtil.sendPostMessage(1, head, httpParams);
            if (response == null) {
                publishProgress(1, -1);
                return null;
            }
            publishProgress(1, 1);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            int step = (int) values[0];
            int flag = (int) values[1];
            String str = "";
            dialog.dismiss();
            if (flag == -1) {
                showError("生成失败！");
            } else {
                InfoDialog infoDialog = new InfoDialog();
                infoDialog.setTitle("正常");
                infoDialog.setMessage("生成成功！");
                infoDialog.show(getSupportFragmentManager(), "info");
            }
        }
    }

    @Override
    protected void onDestroy() {
        mLocClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
