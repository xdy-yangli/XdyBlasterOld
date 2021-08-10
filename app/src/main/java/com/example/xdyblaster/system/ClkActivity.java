package com.example.xdyblaster.system;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.InfoDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.CommDetonator.COMM_SET_CLK;
import static com.example.xdyblaster.util.CommDetonator.COMM_SET_TEST_DELAY;

public class ClkActivity extends AppCompatActivity {
    @BindView(R.id.etDelay)
    EditText etDelay;

    @BindView(R.id.bt_read)
    Button btRead;
    @BindView(R.id.bt_write)
    Button btWrite;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;
    CommTask commTask;
    byte[] strBytes = new byte[12];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_clk);
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
//                commTask.cancel(true);
//                commTask = new CommTask(this);
//                commTask.execute(1, COMM_READ_DEV_ID, COMM_WAIT_PUBLISH, COMM_READ_DEV_VER);
                break;
            case R.id.bt_write:
                int d;
                str = etDelay.getText().toString();
                if (str.length() < 1 || str.length() > 5) {
                    showError("频率超范围！(19950-20050)");
                    break;
                }
                d = Integer.parseInt(etDelay.getText().toString());
                if ((d > 20050)||(d<19950)) {
                    showError("频率超范围！(19950-20050)");
                    break;
                }
                commTask.cancel(true);
                commTask = new CommTask(this);
                commTask.setData00(d);
                commTask.execute(1, COMM_SET_CLK);
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
                case COMM_SET_CLK:
                    if (values[1] != -1) {
                        InfoDialog infoDialog = new InfoDialog();
                        infoDialog.setTitle("提示");
                        infoDialog.setMessage("写入成功！");
                        infoDialog.setCancelable(true);
                        infoDialog.show(getSupportFragmentManager(), "info");
                    }
                    else
                    {
                        InfoDialog infoDialog = new InfoDialog();
                        infoDialog.setTitle("提示");
                        infoDialog.setMessage("写入失败！");
                        infoDialog.setCancelable(true);
                        infoDialog.show(getSupportFragmentManager(), "info");
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