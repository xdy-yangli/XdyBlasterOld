package com.example.xdyblaster.system;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.AppConstants.ID_EXIT_FACTORY;
import static com.example.xdyblaster.util.AppConstants.ID_FACTORY;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_DEV_ID;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_DEV_VER;
import static com.example.xdyblaster.util.CommDetonator.COMM_WAIT_PUBLISH;
import static com.example.xdyblaster.util.CommDetonator.COMM_WRITE_DEV_ID;
import static com.example.xdyblaster.util.CommDetonator.COMM_WRITE_DEV_VER;

public class VerActivity extends AppCompatActivity {
    @BindView(R.id.etHtid)
    EditText etHtid;
    @BindView(R.id.etXmbh)
    EditText etXmbh;
    @BindView(R.id.bt_read)
    Button btRead;
    @BindView(R.id.bt_write)
    Button btWrite;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;
    CommTask commTask;
    byte[] strBytes = new byte[12];
    public String newID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ver);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.enMonitorVolt = false;
        commTask = new CommTask(this);
    }

    @OnClick({R.id.bt_read, R.id.bt_write})
    public void onViewClicked(View view) {
        String str;
        switch (view.getId()) {
            case R.id.bt_read:
                commTask.cancel(true);
                commTask = new CommTask(this);
                commTask.execute(1, COMM_READ_DEV_ID, COMM_WAIT_PUBLISH, COMM_READ_DEV_VER);
                break;
            case R.id.bt_write:
                str = etXmbh.getText().toString();
                if (str.isEmpty()) {
                    showError("请输入版本号！");
                    break;
                }
                str = etHtid.getText().toString();
                if (str.isEmpty()) {
                    showError("请输入编号！");
                    break;
                }
                if (str.length() != 11) {
                    showError("编号长度错误！");
                    break;
                }
                if (dataViewModel.devId.equals(ID_FACTORY)) {
                    if (!str.equals(ID_EXIT_FACTORY))
                        break;
                }


                SharedPreferencesUtils.setParam(VerActivity.this, "devId", str);
                commTask.cancel(true);
                commTask = new CommTask(this);
                newID = str;
                byte[] b = str.getBytes();
                System.arraycopy(b, 0, strBytes, 0, 11);
                strBytes[11] = 0;
                commTask.setMac(FileFunc.getIntFromBytes(strBytes, 0));
                commTask.setData00(FileFunc.getIntFromBytes(strBytes, 4));
                commTask.setData10(FileFunc.getIntFromBytes(strBytes, 8));
                commTask.execute(1, COMM_WRITE_DEV_ID, COMM_WAIT_PUBLISH, COMM_WRITE_DEV_VER);
                break;
        }
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
                case COMM_WRITE_DEV_ID:
                    if (values[1] == 1) {
                        for (int i = 0; i < 12; i++)
                            strBytes[i] = 0;
                        String str = etXmbh.getText().toString();
                        byte[] b = str.getBytes();
                        System.arraycopy(b, 0, strBytes, 0, b.length);
                        commTask.setMac(FileFunc.getIntFromBytes(strBytes, 0));
                        commTask.setData00(FileFunc.getIntFromBytes(strBytes, 4));
                        commTask.setData10(FileFunc.getIntFromBytes(strBytes, 7));
                        waitPublish = false;
                    }
                    break;
                case COMM_WRITE_DEV_VER:
                    if (!dataViewModel.devId.equals(newID)) {
                        if (newID.equals(ID_FACTORY) || newID.equals(ID_EXIT_FACTORY)) {
                            final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            //杀掉以前进程
                            android.os.Process.killProcess(android.os.Process.myPid());
                            break;
                        }
                    }
                    InfoDialog infoDialog = new InfoDialog();
                    infoDialog.setTitle("提示");
                    infoDialog.setMessage("写入成功！");
                    infoDialog.setCancelable(true);
                    infoDialog.show(getSupportFragmentManager(), "info");
                    dataViewModel.devId = "";
                    break;
                case COMM_READ_DEV_ID:
                    if (values[1] == 1) {
                        byte[] b = new byte[11];
                        b[0] = (byte) (values[2] & 0x0ff);
                        b[1] = (byte) ((values[2] >> 8) & 0x0ff);
                        b[2] = (byte) ((values[2] >> 16) & 0x0ff);
                        b[3] = (byte) ((values[2] >> 24) & 0x0ff);
                        b[4] = (byte) (values[3] & 0x0ff);
                        b[5] = (byte) ((values[3] >> 8) & 0x0ff);
                        b[6] = (byte) ((values[3] >> 16) & 0x0ff);
                        b[7] = (byte) ((values[3] >> 24) & 0x0ff);
                        b[8] = (byte) (values[4] & 0x0ff);
                        b[9] = (byte) ((values[4] >> 8) & 0x0ff);
                        b[10] = (byte) ((values[4] >> 16) & 0x0ff);
                        etHtid.setText(new String(b));
                        waitPublish = false;
                    }
                    break;
                case COMM_READ_DEV_VER:
                    if (values[1] == 1) {
                        byte[] b = new byte[12];
                        b[0] = (byte) (values[2] & 0x0ff);
                        b[1] = (byte) ((values[2] >> 8) & 0x0ff);
                        b[2] = (byte) ((values[2] >> 16) & 0x0ff);
                        b[3] = (byte) ((values[2] >> 24) & 0x0ff);
                        b[4] = (byte) (values[3] & 0x0ff);
                        b[5] = (byte) ((values[3] >> 8) & 0x0ff);
                        b[6] = (byte) ((values[3] >> 16) & 0x0ff);
                        b[7] = (byte) ((values[3] >> 24) & 0x0ff);
                        b[8] = (byte) (values[4] & 0x0ff);
                        b[9] = (byte) ((values[4] >> 8) & 0x0ff);
                        b[10] = (byte) ((values[4] >> 16) & 0x0ff);
                        b[11] = (byte) ((values[4] >> 24) & 0x0ff);
                        etXmbh.setText(new String(b));
                        waitPublish = false;
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
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getSupportFragmentManager(), "info");
    }

}
