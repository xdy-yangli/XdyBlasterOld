package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.jessyan.autosize.internal.CustomAdapt;
import utils.SerialPortUtils;

public class ResultDataActivity extends AppCompatActivity implements CustomAdapt {

    List<String> listLg = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    @BindView(R.id.list_result)
    ListView listResult;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tv_jd)
    TextView tvJd;
    @BindView(R.id.tv_wd)
    TextView tvWd;
    @BindView(R.id.tvPeople)
    TextView tvPeople;
    @BindView(R.id.tvTotal)
    TextView tvTotal;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result_data);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        Intent intent = getIntent();
        String str = intent.getExtras().getString("file", "");
        JSONArray jsonArray = FileFunc.loadDetonateResult(str, dataViewModel);

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                listLg.add(jsonArray.getString(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(listLg);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_result, listLg);
        listResult.setAdapter(arrayAdapter);
        tvTime.setText("时间：" + str);
        String tmp;
        if (dataViewModel.jd.length() > 9)
            tmp = dataViewModel.jd.substring(0, 9);
        else
            tmp = dataViewModel.jd;
        tvJd.setText("经度：" + tmp);
        if (dataViewModel.wd.length() > 9)
            tmp = dataViewModel.wd.substring(0, 9);
        else
            tmp = dataViewModel.wd;
        tvWd.setText("纬度：" + tmp);
        tvTotal.setText("数量：" + listLg.size());
        tvPeople.setText("爆破人员：" + dataViewModel.userId);

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
