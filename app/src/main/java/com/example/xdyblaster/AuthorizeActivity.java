package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.example.xdyblaster.fragment.FragmentAuthInput;
import com.example.xdyblaster.util.AndroidDes3Util;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.HttpUtil;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.KeyReceiver;
import com.example.xdyblaster.util.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import okhttp3.Response;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.FileFunc.saveAuthFile;
import static com.example.xdyblaster.util.FileFunc.tintDrawable;
import static com.example.xdyblaster.util.UartData.CRC16;

public class AuthorizeActivity extends AppCompatActivity implements CustomAdapt {


    @BindView(R.id.layout_download)
    FrameLayout layoutDownload;
    @BindView(R.id.layout_upload)
    FrameLayout layoutUpload;
    @BindView(R.id.layout_list_auth)
    FrameLayout layoutListAuth;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;
    LinkedHashMap<String, String> httpParams;
    String head;
    boolean f1, f2;
    InfoDialog infoDialog;
    AlertDialog dialog;
    //KeyReceiver myReceiver;
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LatLng latLng = null;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    String strXmbh, strHtid, strDwdm;
    long lgUuid, lgCount;
    JSONObject jsonObject;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 131:
                    if (msg.arg1 == 1) {
                        f1 = true;
                    } else {
                        f1 = false;
                    }
                    break;
                case 132:
                    if (msg.arg1 == 1) {
                        f2 = true;
                    } else {
                        f2 = false;
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.enMonitorVolt = false;
        //myReceiver = new KeyReceiver(this, handler, dataViewModel);
        dataViewModel.keyHandler=handler;
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

    }

    @Override
    protected void onDestroy() {
        dataViewModel.keyHandler=null;
        //unregisterReceiver(myReceiver);
        mLocClient.stop();
        super.onDestroy();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner extends BDAbstractLocationListener {

        @SuppressLint("DefaultLocale")
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }


    private void showError(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthorizeActivity.this);
        Drawable drawable1 = ContextCompat.getDrawable(AuthorizeActivity.this, R.mipmap.ic_report_problem_white_48dp);
        Drawable drawableR = tintDrawable(drawable1, ContextCompat.getColor(AuthorizeActivity.this, R.color.colorRed));
        builder.setIcon(drawableR);
        builder.setTitle("数据错误");
        builder.setMessage(str);
//        final String[] choice=new String[]{"上海","北京","重庆","广州","天津"};
//        //设置单选对话框的监听
//        builder.setSingleChoiceItems(choice, 2, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(NewFileActivity.this,"你选中了"+choice[which], Toast.LENGTH_SHORT).show();
//            }
//        });
        builder.setCancelable(true);
        builder.create().show();
    }


    @OnClick({R.id.layout_upload, R.id.layout_download, R.id.layout_list_auth})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_download:
                Intent intent = new Intent(AuthorizeActivity.this, DownloadAuthActivity.class);
                startActivity(intent);
                break;
            //voltDialog();
//                FragmentAuthInput fragmentAuthInput = new FragmentAuthInput();
//                Bundle bundle = new Bundle();
//                bundle.putString("htid", (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "htid", "2020003"));
//                bundle.putString("xmbh", (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "xmbh", "2020001"));
//                bundle.putString("dwdm", (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "dwdm", "001"));
//                fragmentAuthInput.setArguments(bundle);
//                fragmentAuthInput.onButtonClickListener = new FragmentAuthInput.OnButtonClickListener() {
//                    @SuppressLint("DefaultLocale")
//                    @Override
//                    public void onButtonClick(int index, String htid, String xmbh, String dwdm, long uuid, long count, boolean check) {
//                        SharedPreferencesUtils.setParam(AuthorizeActivity.this, "htid", htid);
//                        SharedPreferencesUtils.setParam(AuthorizeActivity.this, "xmbh", xmbh);
//                        SharedPreferencesUtils.setParam(AuthorizeActivity.this, "dwdm", dwdm);
//                        lgCount = count;
//                        lgUuid = uuid;
//                        strDwdm = dwdm;
//                        strHtid = htid;
//                        strXmbh = xmbh;
//                        httpParams = new LinkedHashMap<>();
//                        try {
//                            StringBuilder stringBuilder = new StringBuilder();
//                            String str;
//                            int crc;
//
//                            if (!check) {
//                                httpParams.put("htid", htid);
//                                httpParams.put("xmbh", xmbh);
//                                httpParams.put("dwdm", dwdm);
//                            } else {
//                                httpParams.put("dwdm", "4526002200001");
//                                strHtid = "厂内试爆";
//                            }
//                            httpParams.put("sbbh", dataViewModel.devId);
////                            httpParams.put("jd", "106.59774");//String.format("%f", mCurrentLat));
////                            httpParams.put("wd","23.90510"); //String.format("%f", mCurrentLon));
//                            httpParams.put("wd", String.format("%f", mCurrentLat));
//                            httpParams.put("jd", String.format("%f", mCurrentLon));
//                            for (int i = 0; i < lgCount; i++) {
//                                if (i != 0)
//                                    stringBuilder.append(',');
//                                str = String.format("%13d", uuid);
//                                crc = CRC16(str.getBytes(), 13) & 0x0ffff;
//                                crc = ((crc << 8) & 0x0ff00) + ((crc >> 8) & 0x0ff);
//                                stringBuilder.append(str);
//                                stringBuilder.append(String.format("%04X", crc & 0x0ffff));
//                                uuid++;
//                            }
//                            httpParams.put("uid", stringBuilder.toString());
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        head = "param=";
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                connectDialog();
//                                HttpReadWrite httpReadWrite = new HttpReadWrite();
//                                httpReadWrite.execute(1);
//                            }
//                        });
//
//                    }
//                };
//                fragmentAuthInput.show(getSupportFragmentManager(), "input");
//                break;
            case R.id.layout_upload:
                intent = new Intent(AuthorizeActivity.this, UploadResultActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_list_auth:
                intent = new Intent(AuthorizeActivity.this, AuthViewActivity.class);
                intent.putExtra("file", "");
                startActivity(intent);
                break;

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
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

//    public class HttpReadWrite extends AsyncTask<Integer, Object, Integer> {
//
//        @Override
//        protected Integer doInBackground(Integer... integers) {
//            Response response = null;
//            while (!dialog.isShowing()) ;
//            switch (integers[0]) {
//                case 1:
//                case 2:
//                case 3:
//                    if (integers[0] == 1) {
//                        response = HttpUtil.sendPostMessage(0, head, httpParams);
//                    } else if (integers[0] == 2) {
//                        response = HttpUtil.sendPostMessage(1, head, httpParams);
//                    } else if (integers[0] == 3) {
//                        response = HttpUtil.sendPostMessage(1, head);
//                    }
//
//
//                    if (response != null) {
//                        try {
//                            publishProgress(integers[0], 1, Objects.requireNonNull(response.body()).string());
//                            //Log.e("http", "response: " + Objects.requireNonNull(response.body()).string());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            publishProgress(integers[0], -1, "");
//                        }
//                    }
//                    break;
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Integer integer) {
//            super.onPostExecute(integer);
//        }
//
//        @Override
//        protected void onProgressUpdate(Object... values) {
//            int step = (int) values[0];
//            int flag = (int) values[1];
//            String str = "";
//            switch (step) {
//                case 1:
//                    boolean b = true;
//                    dialog.dismiss();
//                    if (flag == 1) {
//                        try {
//                            str = AndroidDes3Util.decode((String) values[2], "jadl12345678912345678912");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        if (!str.isEmpty()) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(str);
//                                if (jsonObject.getString("cwxx").equals("0"))
//                                    b = false;
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    if (b) {
//                        InfoDialog infoDialog = new InfoDialog();
//                        infoDialog.setTitle("故障");
//                        infoDialog.setMessage("数据下载失败！");
//                        infoDialog.setBtnEnable(true);
//                        infoDialog.show(getSupportFragmentManager(), "info");
//                    } else {
//                        String fileName = strHtid;// (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "htid", "htid");
//                        saveAuthFile(fileName, str, strHtid, strXmbh, strDwdm);
//                        Intent intent = new Intent(AuthorizeActivity.this, AuthViewActivity.class);
//                        intent.putExtra("file", fileName);
//                        startActivity(intent);
//                    }
//                    break;
//                case 2:
//                case 3:
//                    dialog.dismiss();
//                    break;
//            }
//
//            super.onProgressUpdate(values);
//        }
//    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 500;
    }
}
