package com.example.xdyblaster.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.GetKey;
import com.example.xdyblaster.util.InfoDialog;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.SerialPortUtils;

import static com.example.xdyblaster.MainActivity.actionScan;
import static com.example.xdyblaster.MainActivity.actionStartScan;

public class FragmentEdit extends DialogFragment {
    DataViewModel dataViewModel;
    SerialPortUtils serialPortUtils;
    @BindView(R.id.ivLogo)
    ImageView ivLogo;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.etTime)
    EditText etTime;
    @BindView(R.id.lt_time)
    LinearLayout ltTime;
    @BindView(R.id.tv_row)
    TextView tvRow;
    @BindView(R.id.etRow)
    EditText etRow;
    @BindView(R.id.lt_row)
    LinearLayout ltRow;
    @BindView(R.id.tv_hole)
    TextView tvHole;
    @BindView(R.id.etHole)
    EditText etHole;
    @BindView(R.id.lt_hole)
    LinearLayout ltHole;
    @BindView(R.id.etId)
    EditText etId;
    @BindView(R.id.lt_id)
    LinearLayout ltId;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.btScan)
    Button btScan;
    @BindView(R.id.btConfirm)
    Button btConfirm;
    Unbinder unbinder;


    public int detonatorRow, detonatorHole, detonatorTime;
    public String detonatorId;

    GetKey getKey;
    InfoDialog infoDialog;

    public interface OnExitListener {
        public void OnExit(int index, int row, int hole, int time, String id);
    }

    public OnExitListener onExitListener = null;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "same.uuid")) {
                dismissAllowingStateLoss();
                return;
            }
            if (Objects.equals(intent.getAction(), "add.uuid")) {
                if (!infoDialog.isVisible()) {
                    etId.setText(Objects.requireNonNull(intent.getExtras()).getString("barcode"));
                    btConfirm.performClick();
                }
                return;
            }

            if (intent.getAction().equals(actionScan)) {
                String tmp = Objects.requireNonNull(intent.getExtras()).getString("barcode");
                assert tmp != null;
                if (FileFunc.checkUuidString(tmp)) {
                    if (!infoDialog.isVisible()) {
                        etId.setText(Objects.requireNonNull(intent.getExtras()).getString("barcode"));
                        btConfirm.performClick();
                    }
                }
            } else if (intent.getAction().equals(actionStartScan)) {
                if (!infoDialog.isVisible()) {
                    btScan.performClick();
                }
            } else {
                getKey.requestFocus();
                getKey.requestFocusFromTouch();
            }
        }
    };


    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit, container);
        unbinder = ButterKnife.bind(this, view);
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        Bundle bundle = getArguments();
        detonatorId = bundle.getString("uuid", "");
        detonatorRow = bundle.getInt("row", 0);
        detonatorHole = bundle.getInt("hole", 0);
        detonatorTime = bundle.getInt("time", 0);
        etTime.setText(String.valueOf(detonatorTime));
        etRow.setText(String.valueOf(detonatorRow + 1));
        etHole.setText(String.valueOf(detonatorHole + 1));
        if (detonatorId.isEmpty()) {
            tvTitle.setText("插入雷管");
            etId.setText("");
        } else {
            etId.setText(detonatorId);
        }
        IntentFilter filter = new IntentFilter(actionScan);
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction(actionStartScan);
        filter.addAction("same.uuid");
        filter.addAction("add.uuid");
        getActivity().getApplicationContext().registerReceiver(receiver, filter);
        getKey = view.findViewById(R.id.tv_id);
        getKey.setFocusable(true);
        getKey.setFocusableInTouchMode(true);

        getKey.requestFocus();
        getKey.requestFocusFromTouch();
        //getKey.setVisibility(View.INVISIBLE);
        infoDialog = new InfoDialog();
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
        getActivity().getApplicationContext().unregisterReceiver(receiver);
        unbinder.unbind();
    }

    @OnClick({R.id.btExit, R.id.btScan, R.id.btConfirm})
    public void onViewClicked(View view) {
        int index = 2;
        int i;
        switch (view.getId()) {
            case R.id.btExit:
                if (onExitListener != null)
                    onExitListener.OnExit(0, 0, 0, 0, "");
                dismissAllowingStateLoss();
                break;
            case R.id.btConfirm:
                index = 1;

                if (etId.getText().toString().isEmpty()) {
                    showError("请输入编码！");
                    break;
                }
                if (etId.getText().toString().length() != 13) {
                    showError("编码长度不是13位！");
                    break;
                }
                detonatorId = etId.getText().toString();
                if (!FileFunc.checkUuidString(detonatorId)) {
                    showError("编码错误！");
                    break;
                }
            case R.id.btScan:
                if (etRow.getText().toString().isEmpty()) {
                    showError("请输入区号！");
                    break;
                }
                try {
                    detonatorRow = Integer.parseInt(etRow.getText().toString());
                } catch (Exception e) {
                    showError("区号错误！(1-60)");
                    break;
                }
                if (detonatorRow > 60 || detonatorRow < 1) {
                    showError("区号错误！(1-60)");
                    break;
                }
                if (etHole.getText().toString().isEmpty()) {
                    showError("请输入孔号！");
                    break;
                }
                try {
                    detonatorHole = Integer.parseInt(etHole.getText().toString());
                } catch (Exception e) {
                    showError("孔号错误！(1-1000)");
                    break;
                }
                if (detonatorHole > 1000 || detonatorHole < 1) {
                    showError("孔号错误！(1-1000)");
                    break;
                }
                if (etTime.getText().toString().isEmpty()) {
                    showError("请输入延时！");
                    break;
                }
                try {
                    detonatorTime = Integer.parseInt(etTime.getText().toString());
                } catch (Exception e) {
                    showError("延时错误！(1-60000)");
                    break;
                }
                if (detonatorTime > 60000 || detonatorTime < 0) {
                    showError("延时错误！(1-60000)");
                    break;
                }
                if (onExitListener != null)
                    onExitListener.OnExit(index, detonatorRow - 1, detonatorHole - 1, detonatorTime, detonatorId);
                if (index == 1)
                    dismissAllowingStateLoss();
                break;
        }
    }

    private void showError(String str) {

        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getParentFragmentManager(), "info");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


}
