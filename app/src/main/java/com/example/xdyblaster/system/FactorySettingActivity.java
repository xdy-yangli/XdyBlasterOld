package com.example.xdyblaster.system;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FactorySetting;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.SettingItem;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import utils.SerialPortUtils;

public class FactorySettingActivity extends AppCompatActivity {

    @BindView(R.id.lt_setting)
    LinearLayout ltSetting;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.btConfirm)
    Button btConfirm;
    @BindView(R.id.tvCount)
    TextView tvCount;
    @BindView(R.id.seek_bar)
    RangeSeekBar seekBar;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;

    private int fileLen, filePtr, commAck, commCmd;
    private byte[] fileData;
    boolean f1, f2;
    InfoDialog infoDialog;
    SettingItem[] settingItems = new SettingItem[7];
    FactorySetting factorySetting;
    int testCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_setting);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        dataViewModel.enMonitorVolt = false;
        for (int i = 0; i < 7; i++) {
            settingItems[i] = new SettingItem(this);
            ltSetting.addView(settingItems[i]);
        }
        factorySetting = new FactorySetting(this);


        settingItems[0].initValus("电流", 0, 20, factorySetting.getCurrMin(), factorySetting.getCurrMax());
        settingItems[1].initValus("电容", 70, 150, factorySetting.getCapMin(), factorySetting.getCapMax());
        settingItems[2].initValus("桥丝", 0, 10, factorySetting.getBridgeMin(), factorySetting.getBridgeMax());
        settingItems[3].initValus("主频", 450, 550, factorySetting.getFreqMin(), factorySetting.getFreqMax());
        settingItems[4].initValus("副频", 28, 56, factorySetting.getSubMin(), factorySetting.getSubMax());
        settingItems[5].initValus("电压", 16, 30, factorySetting.getVoltMin(), factorySetting.getVoltMax());
        settingItems[6].initValus("倒计时", 1, 40, factorySetting.getCdStep(), factorySetting.getCdTime());
        int tmp = factorySetting.getTestWhich();
        for (int i = 0; i < 7; i++) {
            if ((tmp & 0x01) != 0)
                settingItems[i].setSelection(true);
            else
                settingItems[i].setSelection(false);
            tmp = tmp >> 1;
        }
        testCount = factorySetting.getTestCount();
        tvCount.setText(String.valueOf(testCount));
        seekBar.setProgress(testCount);
        seekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                testCount = (int) leftValue;
                tvCount.setText(String.valueOf(testCount));

            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @OnClick({R.id.btExit, R.id.btConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btExit:
                finish();
                break;
            case R.id.btConfirm:
                int tmp = 0, bit = 0x01;
                for (int i = 0; i < 7; i++) {
                    factorySetting.setData(i, settingItems[i].getMin(), settingItems[i].getMax());
                    if (settingItems[i].getSelection())
                        tmp |= bit;
                    bit = bit << 1;

                }
                factorySetting.setTestCount(testCount);
                factorySetting.setTestWhich(tmp);
                finish();
                break;
        }
    }
}
