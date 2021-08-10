package com.example.xdyblaster.fragment;

import android.annotation.SuppressLint;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.databinding.FragmentFixdelayBinding;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.InfoDialog;

import java.util.List;

import utils.SerialPortUtils;


public class FragmentFixdelay extends DialogFragment implements View.OnClickListener {

    private FragmentFixdelayBinding binding;
    DataViewModel dataViewModel;
    SerialPortUtils serialPortUtils;
    CheckBox[] checkBoxes = new CheckBox[20];
    public OnFixdelayExitListener onFixdelayExitListener = null;
    public int fixSelected, defaultCheck = 0;

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        binding = FragmentFixdelayBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        checkBoxes[0] = binding.cb1;
        checkBoxes[1] = binding.cb2;
        checkBoxes[2] = binding.cb3;
        checkBoxes[3] = binding.cb4;
        checkBoxes[4] = binding.cb5;
        checkBoxes[5] = binding.cb6;
        checkBoxes[6] = binding.cb7;
        checkBoxes[7] = binding.cb8;
        checkBoxes[8] = binding.cb9;
        checkBoxes[9] = binding.cb10;
        checkBoxes[10] = binding.cb11;
        checkBoxes[11] = binding.cb12;
        checkBoxes[12] = binding.cb13;
        checkBoxes[13] = binding.cb14;
        checkBoxes[14] = binding.cb15;
        checkBoxes[15] = binding.cb16;
        checkBoxes[16] = binding.cb17;
        checkBoxes[17] = binding.cb18;
        checkBoxes[18] = binding.cb19;
        checkBoxes[19] = binding.cb20;
        List<String> strings = FileFunc.loadFixdelay();
        checkBoxes[defaultCheck].setChecked(true);
        for (int i = 0; i < 20; i++) {
            checkBoxes[i].setOnClickListener(this);
            checkBoxes[i].setText(String.format("%d段 %s", i + 1, strings.get(i)));
        }
        binding.btEdit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {

                fixSelected = getCheckboxSelect();
                if (fixSelected != -1) {
                    InfoDialog infoDialog = new InfoDialog();
                    infoDialog.setTitle(String.format("延时设定(%d段)", fixSelected + 1));
                    infoDialog.setMessage("延时范围(0到40000)");
                    infoDialog.setBtnEnable(true);
                    infoDialog.setEdit1("延时");
                    infoDialog.setAutoExit(false);
                    infoDialog.setOnButtonClickListener(new InfoDialog.OnButtonClickListener() {
                        @Override
                        public void onButtonClick(int index, String str) {
                            if(index==0)
                                infoDialog.dismissAllowingStateLoss();
                            else {
                                int d = Integer.parseInt(str);
                                if (d > 0 && d < 40001) {
                                    strings.remove(fixSelected);
                                    strings.add(fixSelected, str);
                                    checkBoxes[fixSelected].setText(String.format("%d段 %s", fixSelected + 1, strings.get(fixSelected)));
                                    FileFunc.saveFixdelay(strings);
                                    infoDialog.dismissAllowingStateLoss();
                                }
                            }
                        }
                    });
                    infoDialog.show(getParentFragmentManager(), "edit");
                }

            }
        });
        binding.btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFixdelayExitListener != null) {
                    fixSelected=getCheckboxSelect();
                    onFixdelayExitListener.onFixdelayExit(fixSelected, strings.get(fixSelected));
                }
                dismissAllowingStateLoss();
            }
        });
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
        //params.height=dm.heightPixels/7*6;
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
        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb1:
                setCheckboxSelect(0);
                break;
            case R.id.cb2:
                setCheckboxSelect(1);
                break;
            case R.id.cb3:
                setCheckboxSelect(2);
                break;
            case R.id.cb4:
                setCheckboxSelect(3);
                break;
            case R.id.cb5:
                setCheckboxSelect(4);
                break;
            case R.id.cb6:
                setCheckboxSelect(5);
                break;
            case R.id.cb7:
                setCheckboxSelect(6);
                break;
            case R.id.cb8:
                setCheckboxSelect(7);
                break;
            case R.id.cb9:
                setCheckboxSelect(8);
                break;
            case R.id.cb10:
                setCheckboxSelect(9);
                break;
            case R.id.cb11:
                setCheckboxSelect(10);
                break;
            case R.id.cb12:
                setCheckboxSelect(11);
                break;
            case R.id.cb13:
                setCheckboxSelect(12);
                break;
            case R.id.cb14:
                setCheckboxSelect(13);
                break;
            case R.id.cb15:
                setCheckboxSelect(14);
                break;
            case R.id.cb16:
                setCheckboxSelect(15);
                break;
            case R.id.cb17:
                setCheckboxSelect(16);
                break;
            case R.id.cb18:
                setCheckboxSelect(17);
                break;
            case R.id.cb19:
                setCheckboxSelect(18);
                break;
            case R.id.cb20:
                setCheckboxSelect(19);
                break;

        }
    }

    private void setCheckboxSelect(int c) {
        for (int i = 0; i < 20; i++) {
            if (i == c)
                checkBoxes[i].setChecked(true);
            else
                checkBoxes[i].setChecked(false);
        }
    }

    private int getCheckboxSelect() {
        for (int i = 0; i < 20; i++) {
            if (checkBoxes[i].isChecked())
                return i;
        }
        return -1;
    }

    public interface OnFixdelayExitListener {
        void onFixdelayExit(int index, String str);
    }

    public void setDefaultCheck(int i) {
        if (i < 20)
            defaultCheck = i;
    }
}