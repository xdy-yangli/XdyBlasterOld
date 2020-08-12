package com.example.xdyblaster.system;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Window;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DailyData;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

import static io.realm.Sort.DESCENDING;

public class DailyActivity extends AppCompatActivity {

    @BindView(R.id.etDaily)
    EditText etDaily;
    Realm mRealm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);
        ButterKnife.bind(this);
        RealmResults<DailyData> userList = mRealm.where(DailyData.class)./*equalTo("act","扫描").*/findAll();
        userList = userList.sort("date",DESCENDING);
        etDaily.setKeyListener(null);
//        etDaily.setText("aaa\t\b\r\n aaaaaa\n aaaaaaaaaaa\n aaaaaaa \naaaaaa\nbbbbbb\naaaa\n");
//        SpannableString spannableString = new SpannableString("设置文字的背景色为淡绿色");
//        BackgroundColorSpan colorSpan = new BackgroundColorSpan(Color.parseColor("#AC00FF30"));
//        spannableString.setSpan(colorSpan, 9, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        DailyData d;
        int start=0;
        SpannableStringBuilder sb=new SpannableStringBuilder();
        ForegroundColorSpan foregroundColorSpan;
        for(int i=0;i<userList.size();i++)
        {
            d=userList.get(i);
            assert d != null;
            start=sb.length();
            sb.append(d.getDate()+" "+d.getAct()).append("\n   ");
            foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);
            sb.setSpan(foregroundColorSpan, start, sb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            sb.append(d.getMemo()).append(String.valueOf('\n'));
        }
        etDaily.setText(sb);
        etDaily.setMovementMethod(ScrollingMovementMethod.getInstance());
        etDaily.setSelection(etDaily.getText().length(), etDaily.getText().length());
    }

    @Override
    protected void onDestroy() {
        mRealm.close();
        super.onDestroy();
    }
}