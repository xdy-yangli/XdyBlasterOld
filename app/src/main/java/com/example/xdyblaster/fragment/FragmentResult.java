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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;
import utils.SerialPortUtils;

public class FragmentResult extends DialogFragment {
    DataViewModel dataViewModel;
    SerialPortUtils serialPortUtils;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.cb_correct)
    CheckBox cbCorrect;
    @BindView(R.id.tv_correct)
    TextView tvCorrect;
    @BindView(R.id.cb_error)
    CheckBox cbError;
    @BindView(R.id.tv_error)
    TextView tvError;
    @BindView(R.id.cb_time)
    CheckBox cbTime;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.cb_row)
    CheckBox cbRow;
    @BindView(R.id.tv_row)
    TextView tvRow;
    @BindView(R.id.cb_hole)
    CheckBox cbHole;
    @BindView(R.id.tv_hole)
    TextView tvHole;
    @BindView(R.id.cb_outline)
    CheckBox cbOutline;
    @BindView(R.id.tv_outline)
    TextView tvOutline;
    @BindView(R.id.bt_cancel)
    Button btCancel;
    @BindView(R.id.bt_confirm)
    Button btConfirm;
    @BindView(R.id.lt_button)
    LinearLayout ltButton;
    Unbinder unbinder;
    int correct, error, timeErr, rowErr, holeErr, outline, count;
    int viewFlag, total;
    public OnButtonClickListener onButtonClickListener;
    Realm mRealm = Realm.getDefaultInstance();
    @BindView(R.id.tv_total)
    TextView tvTotal;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.cb_correct, R.id.cb_error, R.id.cb_time, R.id.cb_row, R.id.cb_hole, R.id.cb_outline, R.id.bt_cancel, R.id.bt_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cb_correct:
            case R.id.cb_error:
            case R.id.cb_time:
            case R.id.cb_row:
            case R.id.cb_hole:
            case R.id.cb_outline:
                break;
            case R.id.bt_cancel:
                dismiss();
                break;
            case R.id.bt_confirm:
                viewFlag = 0;
                if (cbCorrect.isChecked())
                    viewFlag |= 0x01;
                if (cbError.isChecked())
                    viewFlag |= 0x02;
                if (cbTime.isChecked())
                    viewFlag |= 0x04;
                if (cbRow.isChecked())
                    viewFlag |= 0x08;
                if (cbHole.isChecked())
                    viewFlag |= 0x10;
                if (cbOutline.isChecked())
                    viewFlag |= 0x20;
                if (onButtonClickListener != null)
                    onButtonClickListener.onButtonClick(1, viewFlag, total);
                dismiss();
                break;
        }
    }

    public interface OnButtonClickListener {
        void onButtonClick(int index, int flag, int total);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container);
        unbinder = ButterKnife.bind(this, view);
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        Bundle bundle = getArguments();
        viewFlag = bundle.getInt("view");
        if ((viewFlag & 0x01) != 0)
            cbCorrect.setChecked(true);
        else
            cbCorrect.setChecked(false);
        if ((viewFlag & 0x02) != 0)
            cbError.setChecked(true);
        else
            cbError.setChecked(false);
        if ((viewFlag & 0x04) != 0)
            cbTime.setChecked(true);
        else
            cbTime.setChecked(false);
        if ((viewFlag & 0x08) != 0)
            cbRow.setChecked(true);
        else
            cbRow.setChecked(false);
        if ((viewFlag & 0x10) != 0)
            cbHole.setChecked(true);
        else
            cbHole.setChecked(false);
        if ((viewFlag & 0x20) != 0)
            cbOutline.setChecked(true);
        else
            cbOutline.setChecked(false);
        timeErr = 0;
        correct = 0;
        error = 0;
        holeErr = 0;
        rowErr = 0;
        outline = 0;
        count = 0;
        int color;
        for (DetonatorData d : dataViewModel.detonatorDatas) {
            color = d.getColor();
            if (color != 0x1f)
                count++;
            if (color != 0)
                total++;
            switch (color) {
                case 0x1f:
                    error++;
                    break;
                case 0x80:
                    correct++;
                    break;
                case 0x40:
                    outline++;
                default:
                    if ((color & 0x01) != 0)
                        holeErr++;
                    if ((color & 0x02) != 0)
                        rowErr++;
                    if ((color & 0x04) != 0)
                        timeErr++;
                    break;
            }
        }
        if (total != 0)
            tvTitle.setText("检测结果");
        else
            tvTitle.setText("未检测");
        tvCorrect.setText(String.valueOf(correct));
        tvError.setText(String.valueOf(error));
        tvRow.setText(String.valueOf(rowErr));
        tvHole.setText(String.valueOf(holeErr));
        tvTime.setText(String.valueOf(timeErr));
        tvOutline.setText(String.valueOf(outline));
        tvTotal.setText(String.valueOf(count));

//        if(!mRealm.isInTransaction()) {
//            mRealm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    DailyData dailyData = realm.createObject(DailyData.class, FileFunc.getDate());
//                    dailyData.setAct("检测结果");
//                    dailyData.setMemo("正常-" + correct + " 错误-" + error + " 不在线-" + outline);
//
//                }
//            });
//        }

//
//        try {
//            mRealm.beginTransaction();//开启事务
//            DailyData dailyData = mRealm.createObject(DailyData.class, FileFunc.getDate());
//            dailyData.setAct("检测结果");
//            dailyData.setMemo("正常-" + correct + " 错误-" + error + " 不在线-" + outline);
//            mRealm.commitTransaction();//提交事务
//        }catch (Exception e)
//        {
//            e.printStackTrace();
//        }


//        sortType = bundle.getInt("sort type", 0);
//        sortUpDown = bundle.getInt("sort up down", 0);
//        if (sortType == 0)
//            rbDelay.setChecked(true);
//        if (sortType == 1)
//            rbCode.setChecked(true);
//        if (sortType == 2)
//            rbPos.setChecked(true);
//        if (sortType == 3)
//            rbHole.setChecked(true);
//        if (sortUpDown == 0)
//            cbUp.setChecked(true);
//        else
//            cbDown.setChecked(true);

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

}
