package com.example.xdyblaster.fragment;

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
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.SerialPortUtils;

public class FragmentSort extends DialogFragment {
    DataViewModel dataViewModel;
    SerialPortUtils serialPortUtils;
    Unbinder unbinder;
    @BindView(R.id.rb_delay)
    RadioButton rbDelay;
    @BindView(R.id.rb_code)
    RadioButton rbCode;
    @BindView(R.id.rb_pos)
    RadioButton rbPos;
    @BindView(R.id.cb_up)
    CheckBox cbUp;
    @BindView(R.id.cb_down)
    CheckBox cbDown;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.btConfirm)
    Button btConfirm;

    public OnButtonClickListener onButtonClickListener = null;
    int sortType, sortUpDown;
    @BindView(R.id.rb_hole)
    RadioButton rbHole;


    public interface OnButtonClickListener {
        void onButtonClick(int index, int sortType, int sortUpDown);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sort, container);
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        sortType = bundle.getInt("sort type", 0);
        sortUpDown = bundle.getInt("sort up down", 0);
        if (sortType == 0)
            rbDelay.setChecked(true);
        if (sortType == 1)
            rbCode.setChecked(true);
        if (sortType == 2)
            rbPos.setChecked(true);
        if (sortType == 3)
            rbHole.setChecked(true);
        if (sortUpDown == 0)
            cbUp.setChecked(true);
        else
            cbDown.setChecked(true);

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

    @OnClick({R.id.rb_delay, R.id.rb_code, R.id.rb_pos, R.id.cb_up, R.id.cb_down, R.id.rb_hole, R.id.btExit, R.id.btConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rb_delay:
                rbCode.setChecked(false);
                rbPos.setChecked(false);
                rbHole.setChecked(false);
                sortType = 0;
                break;
            case R.id.rb_code:
                rbDelay.setChecked(false);
                rbPos.setChecked(false);
                rbHole.setChecked(false);
                sortType = 1;
                break;
            case R.id.rb_pos:
                rbDelay.setChecked(false);
                rbCode.setChecked(false);
                rbHole.setChecked(false);
                sortType = 2;
                break;
            case R.id.rb_hole:
                rbDelay.setChecked(false);
                rbCode.setChecked(false);
                rbPos.setChecked(false);
                sortType = 3;
                break;
            case R.id.cb_up:
                cbDown.setChecked(false);
                cbUp.setChecked(true);
                sortUpDown = 0;
                break;
            case R.id.cb_down:
                cbUp.setChecked(false);
                cbDown.setChecked(true);
                sortUpDown = 1;
                break;
            case R.id.btExit:
                dismissAllowingStateLoss();
                break;
            case R.id.btConfirm:
                if (onButtonClickListener != null) {
                    onButtonClickListener.onButtonClick(1, sortType, sortUpDown);
                    dismissAllowingStateLoss();
                }
                break;
        }
    }
}
