package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.location.LocationClient;
import com.example.xdyblaster.databinding.ActivityAuthorizeBinding;
import com.example.xdyblaster.databinding.ActivityMainBinding;
import com.example.xdyblaster.util.DataViewModel;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
//import butterknife.OnClick;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

public class AuthorizeActivity extends AppCompatActivity implements CustomAdapt , View.OnClickListener {

    private ActivityAuthorizeBinding binding;

    private DataViewModel dataViewModel;
    boolean f1, f2;
    AlertDialog dialog;
    LocationClient mLocClient;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 131:
                    f1 = (msg.arg1 == 1);
                    break;
                case 132:
                    f2 = (msg.arg1 == 1);
                    break;
            }
        }
    };


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NotNull View v) {
        switch (v.getId()) {
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
            case R.id.layout_face:
                intent = new Intent(AuthorizeActivity.this, DownloadAuthActivity.class);
                startActivity(intent);
                break;

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        binding = ActivityAuthorizeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //setContentView(R.layout.activity_authorize);
        //ButterKnife.bind(this);
        SerialPortUtils serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.enMonitorVolt = false;
        dataViewModel.keyHandler = handler;

    }

    @Override
    protected void onDestroy() {
        dataViewModel.keyHandler = null;
        super.onDestroy();
        binding=null;
    }

//    @SuppressLint("NonConstantResourceId")
//    @OnClick({R.id.layout_upload, R.id.layout_download, R.id.layout_list_auth, R.id.layout_face})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.layout_download:
//                Intent intent = new Intent(AuthorizeActivity.this, DownloadAuthActivity.class);
//                startActivity(intent);
//                break;
//            //voltDialog();
////                FragmentAuthInput fragmentAuthInput = new FragmentAuthInput();
////                Bundle bundle = new Bundle();
////                bundle.putString("htid", (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "htid", "2020003"));
////                bundle.putString("xmbh", (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "xmbh", "2020001"));
////                bundle.putString("dwdm", (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "dwdm", "001"));
////                fragmentAuthInput.setArguments(bundle);
////                fragmentAuthInput.onButtonClickListener = new FragmentAuthInput.OnButtonClickListener() {
////                    @SuppressLint("DefaultLocale")
////                    @Override
////                    public void onButtonClick(int index, String htid, String xmbh, String dwdm, long uuid, long count, boolean check) {
////                        SharedPreferencesUtils.setParam(AuthorizeActivity.this, "htid", htid);
////                        SharedPreferencesUtils.setParam(AuthorizeActivity.this, "xmbh", xmbh);
////                        SharedPreferencesUtils.setParam(AuthorizeActivity.this, "dwdm", dwdm);
////                        lgCount = count;
////                        lgUuid = uuid;
////                        strDwdm = dwdm;
////                        strHtid = htid;
////                        strXmbh = xmbh;
////                        httpParams = new LinkedHashMap<>();
////                        try {
////                            StringBuilder stringBuilder = new StringBuilder();
////                            String str;
////                            int crc;
////
////                            if (!check) {
////                                httpParams.put("htid", htid);
////                                httpParams.put("xmbh", xmbh);
////                                httpParams.put("dwdm", dwdm);
////                            } else {
////                                httpParams.put("dwdm", "4526002200001");
////                                strHtid = "厂内试爆";
////                            }
////                            httpParams.put("sbbh", dataViewModel.devId);
//////                            httpParams.put("jd", "106.59774");//String.format("%f", mCurrentLat));
//////                            httpParams.put("wd","23.90510"); //String.format("%f", mCurrentLon));
////                            httpParams.put("wd", String.format("%f", mCurrentLat));
////                            httpParams.put("jd", String.format("%f", mCurrentLon));
////                            for (int i = 0; i < lgCount; i++) {
////                                if (i != 0)
////                                    stringBuilder.append(',');
////                                str = String.format("%13d", uuid);
////                                crc = CRC16(str.getBytes(), 13) & 0x0ffff;
////                                crc = ((crc << 8) & 0x0ff00) + ((crc >> 8) & 0x0ff);
////                                stringBuilder.append(str);
////                                stringBuilder.append(String.format("%04X", crc & 0x0ffff));
////                                uuid++;
////                            }
////                            httpParams.put("uid", stringBuilder.toString());
////
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
////                        head = "param=";
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
////                                connectDialog();
////                                HttpReadWrite httpReadWrite = new HttpReadWrite();
////                                httpReadWrite.execute(1);
////                            }
////                        });
////
////                    }
////                };
////                fragmentAuthInput.show(getSupportFragmentManager(), "input");
////                break;
//            case R.id.layout_upload:
//                intent = new Intent(AuthorizeActivity.this, UploadResultActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.layout_list_auth:
//                intent = new Intent(AuthorizeActivity.this, AuthViewActivity.class);
//                intent.putExtra("file", "");
//                startActivity(intent);
//                break;
//            case R.id.layout_face:
//                intent = new Intent(AuthorizeActivity.this, DownloadAuthActivity.class);
//                startActivity(intent);
//                break;
//        }
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 500;
    }



}
