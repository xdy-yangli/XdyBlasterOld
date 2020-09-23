package com.example.xdyblaster.system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.MainActivity;
import com.example.xdyblaster.R;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.InfoDialog;
import com.xuexiang.xupdate.utils.UpdateUtils;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.CommDetonator.COMM_CALC_VOLTAGE;
import static com.example.xdyblaster.util.CommDetonator.COMM_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_RESET;
import static com.example.xdyblaster.util.CommDetonator.COMM_UPDATE;
import static com.example.xdyblaster.util.FileFunc.tintDrawable;

public class SystemActivity extends AppCompatActivity implements CustomAdapt {

    @BindView(R.id.layout_volt)
    FrameLayout layoutVolt;
    @BindView(R.id.layout_update)
    FrameLayout layoutUpdate;
    @BindView(R.id.layout_encode)
    FrameLayout layoutEncode;
    @BindView(R.id.layout_tester)
    FrameLayout layoutTester;
    @BindView(R.id.layout_ble)
    FrameLayout layoutBle;
    @BindView(R.id.layout_make)
    FrameLayout layoutMake;
    @BindView(R.id.layout_set_id)
    FrameLayout layoutSetId;
    @BindView(R.id.layout_daily_data)
    FrameLayout layoutDailyData;
    @BindView(R.id.layout_test_delay)
    FrameLayout layoutTestDelay;
    @BindView(R.id.layout_selftest)
    FrameLayout layoutSelftest;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;

    private int fileLen, filePtr, commAck, commCmd;
    private byte[] fileData;
    boolean f1, f2;
    InfoDialog infoDialog;
    CommTask commTask;


    //    KeyReceiver myReceiver;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
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
        setContentView(R.layout.activity_system);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.enMonitorVolt = false;
        dataViewModel.keyHandler = handler;
        //myReceiver = new KeyReceiver(this, handler,dataViewModel);
        commTask = new CommTask(this);
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(myReceiver);
        dataViewModel.keyHandler = null;
        super.onDestroy();
    }

    /**
     * 自定义布局的对话框
     */
    private void voltDialog() {

        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("设定电压");
        infoDialog.setEdit1("电压值");
        infoDialog.setBtnEnable(true);
        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
        infoDialog.setEdit1Text(String.valueOf(mShare.getFloat("volt", 24)));
        infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
            @Override
            public void onButtonClick(int index, String str) {
                if (index == 1) {
                    float v;
                    if (!str.isEmpty())
                        v = Float.valueOf(str);
                    else
                        v = 0;
                    if (v > 24.0 || v < 5.0) {
                        infoDialog.setAutoExit(false);
                        InfoDialog dialog = new InfoDialog();
                        dialog.setTitle("电压范围错误");
                        dialog.setMessage("请设定在5v到24v之间");
                        dialog.setCancelable(true);
                        dialog.show(getSupportFragmentManager(), "info");
                    } else {
                        SharedPreferences mShare = getSharedPreferences("setting", Context.MODE_PRIVATE);
                        SharedPreferences.Editor mEdit = mShare.edit();
                        mEdit.putFloat("volt", v);
                        mEdit.apply();
                        dataViewModel.defaultVolt = (int) (v * 100.0);
                        infoDialog.setAutoExit(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                commTask.cancel(true);
                                commTask = new CommTask(SystemActivity.this);
                                commTask.execute(1, COMM_CALC_VOLTAGE);
                            }
                        });
                    }
                }

            }
        });
        infoDialog.show(getSupportFragmentManager(), "info");
    }

    /**
     * 带进度的对话框
     */
    private void updateDialog() {
        infoDialog = new InfoDialog();
        infoDialog.setTitle("请勿关闭电");
        infoDialog.setMessage("重新启动");
        infoDialog.setProgressEnable(true);
        infoDialog.setCancelable(false);
        infoDialog.show(getSupportFragmentManager(), "info");
        commTask.cancel(true);
        commTask = new CommTask(this);
        commTask.setFileData(fileData, fileLen);
        commTask.execute(3, COMM_RESET, COMM_DELAY, COMM_DELAY, COMM_UPDATE);
    }


    private class CommTask extends CommDetonator {

        public CommTask(Context context) {
            this.serialPortUtils = SerialPortUtils.getInstance(context);
            this.dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
            this.serialPortUtils.sendStop = true;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]) {
                case COMM_UPDATE:
                    if (values[1] == 1) {
                        if (values[3] == 0) {
                            runOnUiThread(new Runnable() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void run() {
                                    try {
                                        infoDialog.progressBar.setMax(fileLen);
                                        infoDialog.progressBar.setProgress(values[2]);
//                                        infoDialog.setMessageTxt(String.format("升级中（%d/%d)", values[2], fileLen));
                                        //    infoDialog.setMessageTxt("更新固件");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                        if (values[3] == 2) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    infoDialog.setMessageTxt("升级完成！");
                                    infoDialog.setCancelable(true);
                                }
                            });
                        }
                    }
                    waitPublish = false;
                    break;
                case COMM_RESET:
                    if (values[1] == -1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                infoDialog.setMessageTxt("升级失败！");
                                infoDialog.setCancelable(true);
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            switch (integer) {
                case 0:
                case -1:
                    break;

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }

        @Override
        protected void onCancelled(Integer integer) {
            super.onCancelled(integer);
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }
    }


    private void showError(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SystemActivity.this);
        Drawable drawable1 = ContextCompat.getDrawable(SystemActivity.this, R.mipmap.ic_report_problem_white_48dp);
        Drawable drawableR = tintDrawable(drawable1, ContextCompat.getColor(SystemActivity.this, R.color.colorRed));
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


    @OnClick({R.id.layout_volt, R.id.layout_update, R.id.layout_encode, R.id.layout_tester, R.id.layout_ble, R.id.layout_make, R.id.layout_set_id, R.id.layout_daily_data, R.id.layout_test_delay, R.id.layout_selftest})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_volt:
                voltDialog();
                break;
            case R.id.layout_update:
                try {

                    InputStream is;
                    if (UpdateUtils.getVersionCode(SystemActivity.this) > 100)
                        is = getAssets().open("code.bin");
                    else
                        is = getAssets().open("code2.bin");
                    //InputStream is = getAssets().open("code.bin");
                    fileLen = ((is.available() + 7) / 8) * 8;
                    fileData = new byte[fileLen];
                    if (is.read(fileData) != 0)
                        updateDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }


//                File file;
//                FileInputStream fis;
//                DataInputStream dis;
//                file = new File(getSDPath() + "//xdyBlaster//code.bin");
//                if (!file.exists())
//                    showError("请复制升级文件到文件夹xdyBlaster!");
//                else {
//                    try {
//                        fis = new FileInputStream(file);
//                        dis = new DataInputStream(fis);
//                        fileLen = ((dis.available() + 7) / 8) * 8;
//                        fileData = new byte[fileLen];
//                        if (dis.read(fileData) != 0)
//                            updateDialog();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
                break;
            case R.id.layout_encode:
                Intent intent = new Intent(SystemActivity.this, EncodeActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_tester:
                intent = new Intent(SystemActivity.this, TesterActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_ble:
                serialPortUtils.bleOk = false;
                intent = new Intent(SystemActivity.this, BleActivity.class);
                startActivityForResult(intent, 1001);
                break;
            case R.id.layout_make:
                intent = new Intent(SystemActivity.this, NewAuthActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_set_id:
                intent = new Intent(SystemActivity.this, VerActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_daily_data:
                intent = new Intent(SystemActivity.this, DailyActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_test_delay:
                intent = new Intent(SystemActivity.this, DelayActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_selftest:
                intent = new Intent(SystemActivity.this, SelfTestActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
