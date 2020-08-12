package com.example.xdyblaster.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.xdyblaster.R;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

public class SettingItem extends FrameLayout {
    Context mContext;
    View mView;
    RangeSeekBar rangeSeekBar;
    CheckBox checkBox;
    TextView tvMax, tvMin;
    int max, min;

    public SettingItem(Context context) {
        this(context, null);
    }

    public SettingItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.layout_setting_item, this, true);
        rangeSeekBar = mView.findViewById(R.id.seek_bar);
        checkBox = mView.findViewById(R.id.cb_name);
        tvMax = mView.findViewById(R.id.tv_max);
        tvMin = mView.findViewById(R.id.tv_min);

        rangeSeekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                //leftValue is left seekbar value, rightValue is right seekbar value
                max = (int) rightValue;
                min = (int) leftValue;
                tvMax.setText(String.valueOf(max));
                tvMin.setText(String.valueOf(min));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //start tracking touch
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //stop tracking touch
            }
        });

    }

    public void initValus(String str, int rMin, int rMax, int vLeft, int vRight) {
        setTitle(str);
        setRange(rMin, rMax);
        setValue(vLeft, vRight);
    }

    public void setRange(int min, int max) {
        rangeSeekBar.setRange(min, max, 1);
    }

    public void setValue(int min, int max) {
        this.max = max;
        this.min = min;
        rangeSeekBar.setProgress(min, max);
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public void setTitle(String string) {
        checkBox.setText(string);
    }

    public boolean getSelection() {
        return checkBox.isChecked();
    }

    public void setSelection(boolean b) {
        checkBox.setChecked(b);
    }


}
