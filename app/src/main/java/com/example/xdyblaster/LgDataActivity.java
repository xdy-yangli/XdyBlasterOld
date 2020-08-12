package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xdyblaster.util.AuthData;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.LgData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LgDataActivity extends AppCompatActivity {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.list_lg)
    ListView listLg;
    List<String> lgList = new ArrayList<>();
    @BindView(R.id.tvTotal)
    TextView tvTotal;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_lg_data);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String str = intent.getExtras().getString("file");
        tvTitle.setText(str);
        AuthData authData = new AuthData(FileFunc.loadAuthFile(str));
        for (LgData d : authData.lgDatas)
            lgList.add(d.fbh + " (" + d.yxq + ")");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.memu_item, lgList);
        listLg.setAdapter(arrayAdapter);
        listLg.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        tvTotal.setText("总数：" + lgList.size());

    }
}
