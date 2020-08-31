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
        dataViewModel.keyHandler=handler;

    }

    @Override
    protected void onDestroy() {
        dataViewModel.keyHandler=null;
        super.onDestroy();
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


    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 500;
    }
}
