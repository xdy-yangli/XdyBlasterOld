package com.example.xdyblaster.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.xdyblaster.R;

import static com.example.xdyblaster.MainActivity.actionScan;

public class TesterItem extends FrameLayout {
    Context mContext;
    View mView;
    TextView[] textViews = new TextView[8];
    View[] divider = new View[8];

    public TesterItem(Context context) {
        this(context, null);
    }

    public TesterItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TesterItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.layout_tester_item, this, true);
        textViews[0] = mView.findViewById(R.id.tv00);
        textViews[1] = mView.findViewById(R.id.tv01);
        textViews[2] = mView.findViewById(R.id.tv02);
        textViews[3] = mView.findViewById(R.id.tv03);
        textViews[4] = mView.findViewById(R.id.tv04);
        textViews[5] = mView.findViewById(R.id.tv05);
        textViews[6] = mView.findViewById(R.id.tv06);
        textViews[7] = mView.findViewById(R.id.tv07);
        divider[0] = mView.findViewById(R.id.dv01);
        divider[1] = mView.findViewById(R.id.dv01);
        divider[2] = mView.findViewById(R.id.dv02);
        divider[3] = mView.findViewById(R.id.dv03);
        divider[4] = mView.findViewById(R.id.dv04);
        divider[5] = mView.findViewById(R.id.dv05);
        divider[6] = mView.findViewById(R.id.dv06);
        divider[7] = mView.findViewById(R.id.dv07);
    }

    public void setText(String... strings) {
        for (int i = 0; i < strings.length; i++) {
            textViews[i].setText(strings[i]);
        }
    }

    public void setSingleValus(int index, int n) {
        textViews[index].setText(String.valueOf(n));
    }

    public void setSingleValus(int index, String n) {
        textViews[index].setText(n);
    }

    @SuppressLint("DefaultLocale")
    public void setSingleValus(int index, double n) {
        textViews[index].setText(String.format("%.1f", n));
    }

    public void setItemVisible(int v) {
        int b = v;
        for (int i = 1; i < 7; i++) {
            if ((b & 0x01) != 0) {
                textViews[i].setVisibility(VISIBLE);
                divider[i].setVisibility(VISIBLE);
            } else {
                textViews[i].setVisibility(GONE);
                divider[i].setVisibility(GONE);
            }
            b = b >> 1;
        }
    }

    public void setError(int n) {
        if (n == 0) {
            textViews[n].setBackgroundColor(ContextCompat.getColor(mContext,R.color.red));
            textViews[n].setTextColor(ContextCompat.getColor(mContext,R.color.white));
        } else {
            textViews[n].setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
            textViews[n].setTextColor(ContextCompat.getColor(mContext,R.color.red));
        }
    }

    public void setCorrect(int n) {
        textViews[n].setBackgroundColor(ContextCompat.getColor(mContext,R.color.green));
        textViews[n].setTextColor(ContextCompat.getColor(mContext,R.color.white));
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        Intent intent = new Intent(actionScan);
        String str = event.getCharacters();
        intent.putExtra("barcode", str);
        mContext.getApplicationContext().sendBroadcast(intent);
        return super.onKeyMultiple(keyCode, repeatCount, event);


    }

    public void setAllNormal() {
        for (int i = 0; i < 8; i++) {
            textViews[i].setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
            textViews[i].setTextColor(ContextCompat.getColor(mContext,R.color.black));
        }
    }

}
