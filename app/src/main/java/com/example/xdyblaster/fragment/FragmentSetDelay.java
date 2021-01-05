package com.example.xdyblaster.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.InfoDialog;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.SerialPortUtils;

public class FragmentSetDelay extends DialogFragment {
    DataViewModel dataViewModel;
    SerialPortUtils serialPortUtils;
    @BindView(R.id.ivLogo)
    ImageView ivLogo;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvNumber)
    TextView tvNumber;
    @BindView(R.id.etF1Delay)
    EditText etF1Delay;
    @BindView(R.id.etF2Delay)
    EditText etF2Delay;
    @BindView(R.id.cbLine)
    CheckBox cbLine;
    @BindView(R.id.cbCode)
    CheckBox cbCode;
    @BindView(R.id.layout_delay)
    LinearLayout layoutDelay;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.btConfirm)
    Button btConfirm;
    Unbinder unbinder;
    public OnSetDelayData onSetDelayData;
    public int area;
    @BindView(R.id.etArea)
    EditText etArea;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int i;
        String str;
        View view = inflater.inflate(R.layout.fragment_set_delay, container);
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel =new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        unbinder = ButterKnife.bind(this, view);
        SharedPreferences mShare = Objects.requireNonNull(getActivity()).getSharedPreferences("setting", Context.MODE_PRIVATE);
        area = mShare.getInt("area", 0);
        etArea.setText(String.valueOf(area + 1));
        i = mShare.getInt("f1", 100);
        etF1Delay.setText(String.valueOf(i));
        i = mShare.getInt("f2", 100);
        etF2Delay.setText(String.valueOf(i));
        i = mShare.getInt("f1f2mode", 0);
        cbCode.setChecked(false);
        cbLine.setChecked(false);
        if (i == 0)
            cbLine.setChecked(true);
        if (i == 1)
            cbCode.setChecked(true);
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        Window win = getDialog().getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //去除半透明阴影
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        layoutParams.dimAmount = 0.4f;
        win.setAttributes(layoutParams);


        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.CENTER;
//        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        int h = dm.widthPixels / 7 * 6;
        params.width = h;//ViewGroup.LayoutParams.MATCH_PARENT;
//
//        params.height = h;// dm.widthPixels / 3;//ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        //seekbar.getThumb().setColorFilter(Color.parseColor("#ec6a88"), PorterDuff.Mode.SRC_ATOP);

        params.alpha = 1.0f;//0.7f;f
        win.setAttributes(params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.cbLine, R.id.cbCode, R.id.btExit, R.id.btConfirm})
    public void onViewClicked(View view) {
        int f1Delay, f2Delay;
        int i;
        if (!etF1Delay.getText().toString().isEmpty())
            try {
                f1Delay = Integer.parseInt(etF1Delay.getText().toString());
            } catch (Exception e) {
                f1Delay = 900000;
            }
        else
            f1Delay = -1;
        if (!etF2Delay.getText().toString().isEmpty()) {
            try {
                f2Delay = Integer.parseInt(etF2Delay.getText().toString());
            } catch (Exception e) {
                f2Delay = 900000;
            }
        } else
            f2Delay = -1;
        if (!etArea.getText().toString().isEmpty()) {
            try {
                area = Integer.parseInt(etArea.getText().toString());
            } catch (Exception e) {
                area = 100000;
            }
        } else
            area = -1;
        switch (view.getId()) {
            case R.id.cbLine:
                cbCode.setChecked(false);
                cbLine.setChecked(true);
                break;
            case R.id.cbCode:
                cbLine.setChecked(false);
                cbCode.setChecked(true);
                break;
            case R.id.btExit:
                dismissAllowingStateLoss();
                break;
            case R.id.btConfirm:
                if ((f1Delay > 60000) || (f1Delay < 0)) {
                    showError("F1时差超范围!（0至60000毫秒）");
                    break;
                }
                if ((f2Delay > 60000) || (f2Delay < 0)) {
                    showError("F2时差超范围!（0至60000毫秒）");
                    break;
                }
                if ((area > 60) || (area < 1)) {
                    showError("区号超范围!（1至60）");
                    break;
                }
                i = 0;
                if (cbCode.isChecked())
                    i = 1;
                if (onSetDelayData != null)
                    onSetDelayData.onUpdateDelayData(f1Delay, f2Delay, area, i);
                dismissAllowingStateLoss();
                break;
        }
    }

    private void showError(String str) {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getParentFragmentManager(), "info");
    }


    public interface OnSetDelayData {
        void onUpdateDelayData(int f1, int f2, int area, int mode);
    }
}
