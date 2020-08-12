package com.example.xdyblaster.fragment;

import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.InfoDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.SerialPortUtils;

public class FragmentAuthInput extends DialogFragment {

    Unbinder unbinder;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.btConfirm)
    Button btConfirm;
    @BindView(R.id.etUuid)
    EditText etUuid;
    @BindView(R.id.etCount)
    EditText etCount;
    @BindView(R.id.etEach)
    EditText etEach;

    private SerialPortUtils serialPortUtils;
    private DataViewModel dataViewModel;
    public OnButtonClickListener onButtonClickListener = null;
    String uuid;
    int count, each;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_input, container);
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        assert bundle != null;
        uuid = bundle.getString("uuid");
        etUuid.setText(uuid);
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


    @OnClick({R.id.btExit, R.id.btConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btExit:
                dismissAllowingStateLoss();
                break;
            case R.id.btConfirm:
                if (etUuid.getText().toString().length() != 13) {
                    showError("编码长度不是13位！");
                    break;
                }
                uuid = etUuid.getText().toString();
                if (!FileFunc.checkUuidString(uuid)) {
                    showError("编码错误！");
                    break;
                }

                if (etCount.getText().toString().length() == 0) {
                    count = 1;
                } else
                    count = Integer.parseInt(etCount.getText().toString());
                if (etEach.getText().toString().length() == 0) {
                    each = 1;
                } else
                    each = Integer.parseInt(etEach.getText().toString());

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //隐藏软键盘 //
                imm.hideSoftInputFromWindow(etUuid.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(etCount.getWindowToken(), 0);
//                //显示软键盘
//                imm.showSoftInputFromInputMethod(tv.getWindowToken(), 0);
//                //切换软键盘的显示与隐藏
//                imm.toggleSoftInputFromWindow(tv.getWindowToken(), 0, InputMethodManager.HIDE_NOT_ALWAYS);


                if (onButtonClickListener != null)
                    onButtonClickListener.onButtonClick(1, uuid, count, each);
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

    public interface OnButtonClickListener {
        void onButtonClick(int index, String uuid, int count, int each);
    }
}
