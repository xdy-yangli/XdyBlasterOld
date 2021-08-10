package com.example.xdyblaster.system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.Adapter.BridgeAdapter;
import com.example.xdyblaster.NetActivity;
import com.example.xdyblaster.R;
import com.example.xdyblaster.fragment.FragmentLoad;
import com.example.xdyblaster.util.Bridge;
import com.example.xdyblaster.util.CommDetonator;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.UuidData;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.AppConstants.BLASTER_TIMER_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_DELAY;
import static com.example.xdyblaster.util.CommDetonator.COMM_DOWNLOAD_DATA;
import static com.example.xdyblaster.util.CommDetonator.COMM_GET_BATT;
import static com.example.xdyblaster.util.CommDetonator.COMM_IDLE;
import static com.example.xdyblaster.util.CommDetonator.COMM_POWER_ON;
import static com.example.xdyblaster.util.CommDetonator.COMM_READ_ALL_BRIDGE;
import static com.example.xdyblaster.util.CommDetonator.COMM_STOP_OUTPUT;
import static com.example.xdyblaster.util.FileFunc.getUuidData;

public class BridgeActivity extends AppCompatActivity {

    @BindView(R.id.lv_bridge)
    ListView lvBridge;
    @BindView(R.id.bt_start)
    Button btStart;
    @BindView(R.id.bt_view)
    Button btView;
    private ArrayList<Bridge> bridgeList = new ArrayList<>();
    private SerialPortUtils serialPortUtils;
    private DataViewModel dataViewModel;
    private CommTask commTask;
    InfoDialog infoDialog;
    BridgeAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bridge);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        initBridge(); //初始化水果数据
        adapter = new BridgeAdapter(BridgeActivity.this, bridgeList);

        // 将适配器上的数据传递给listView
        lvBridge.setAdapter(adapter);
        commTask = new CommTask(this);
        commTask.execute(2, COMM_IDLE, COMM_GET_BATT);
    }

    private void initBridge() {

        for (DetonatorData d : dataViewModel.detonatorDatas) {
            Bridge a = new Bridge(d.getUuid(), d.getId(), 0.0f, 0.0f);
            bridgeList.add(a);
        }
    }

    @OnClick(R.id.bt_start)
    public void onClick() {
        enableButton(false);
        infoDialog = new InfoDialog();
        infoDialog.setTitle("同步数据");
        infoDialog.setMessage("传输数据");
        infoDialog.setProgressEnable(true);
        infoDialog.setCancelable(false);
        infoDialog.setBtn2Enable(true);
        infoDialog.setChronometerEnable(true);
        infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
            @Override
            public void onButtonClick(int index, String str) {
                commTask.cancel(true);
                commTask = new CommTask(BridgeActivity.this);
                commTask.execute(2, COMM_IDLE, COMM_STOP_OUTPUT);
            }
        });
        infoDialog.show(getSupportFragmentManager(), "info");
        commTask.cancel(true);
        commTask = new CommTask(BridgeActivity.this);
        commTask.execute(8, COMM_IDLE, COMM_POWER_ON, COMM_DELAY, COMM_READ_ALL_BRIDGE, COMM_IDLE, COMM_STOP_OUTPUT);
        enableButton(false);
    }

    public void enableButton(boolean t) {

        btStart.setEnabled(t);
        btView.setEnabled(t);
    }

    @SuppressLint("StaticFieldLeak")
    private class CommTask extends CommDetonator {

        public CommTask(Context context) {
            this.serialPortUtils = SerialPortUtils.getInstance(context);
            this.dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
            this.serialPortUtils.sendStop = true;
        }


        @SuppressLint("DefaultLocale")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int i;
            float p;
            switch (values[0]) {

                case COMM_READ_ALL_BRIDGE:
                    if (values[1] == 1) {
                        try {
                            infoDialog.progressBar.setMax(values[2]);
                            infoDialog.progressBar.setProgress(values[3]);
                            infoDialog.setMessageTxt(String.format("传送数据(%d/%d)", values[3], values[2]));
                            Log.e("read ", String.valueOf(values[2]) + " " + String.valueOf(values[3]));
                            if (values[2].equals(values[3])) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        infoDialog.dismissAllowingStateLoss();
                                        for (int i = 0; i < dataViewModel.detonatorDatas.size(); i++) {
                                            bridgeList.get(i).setBridge(dataViewModel.detonatorDatas.get(i).getBridge());
                                            bridgeList.get(i).setCap(dataViewModel.detonatorDatas.get(i).getCap());
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
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
            enableButton(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            enableButton(true);
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
        }

        @Override
        protected void onCancelled(Integer integer) {
            super.onCancelled(integer);
            Log.e("commTask", "close thread");
            serialPortUtils.sendStop = true;
            enableButton(true);
        }

    }

}