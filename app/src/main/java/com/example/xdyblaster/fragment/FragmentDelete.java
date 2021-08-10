package com.example.xdyblaster.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
//import com.example.xdyblaster.util.AllCapTransformationMethod;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.InfoDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.FileFunc.checkUuidString;


public class FragmentDelete extends DialogFragment {
    @BindView(R.id.ivLogo)
    ImageView ivLogo;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.etStart)
    EditText etStart;
    @BindView(R.id.etEnd)
    EditText etEnd;
    @BindView(R.id.etDelay)
    EditText etDelay;
    @BindView(R.id.lt_delay)
    LinearLayout ltDelay;
    @BindView(R.id.btExit)
    Button btExit;
    @BindView(R.id.btConfirm)
    Button btConfirm;
    Unbinder unbinder;


    int type, row;
    DataViewModel dataViewModel;
    SerialPortUtils serialPortUtils;
    @BindView(R.id.lt_hole)
    LinearLayout ltHole;
    @BindView(R.id.lt_count)
    LinearLayout ltCount;
    @BindView(R.id.etId)
    EditText etId;
    @BindView(R.id.lt_id)
    LinearLayout ltId;
    @BindView(R.id.tvStart)
    TextView tvStart;
    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.tv_delay)
    TextView tvDelay;
    @BindView(R.id.tv_id)
    TextView tvId;
    @BindView(R.id.tv_change)
    TextView tvChange;
    @BindView(R.id.etChange)
    EditText etChange;
    @BindView(R.id.lt_change)
    LinearLayout ltChange;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete, container);
        unbinder = ButterKnife.bind(this, view);
        serialPortUtils = SerialPortUtils.getInstance(getActivity());
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        Bundle bundle = getArguments();
        type = bundle.getInt("type", 0);
        row = bundle.getInt("row", 0);
        //etId.setTransformationMethod(new AllCapTransformationMethod());
        etId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //下面这种方法才是真正的将输入的小写字母转换为大写字母
                etId.removeTextChangedListener(this);
                etId.setText(s.toString().toUpperCase());
                //在输入完毕后定位到光标的末尾
                //mEdtCarNo.setSelection(s.length());
                /**在输入完毕后定位到当前修改的末尾 = start + count*/
                etId.setSelection(start+count);
                etId.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        etId.setText("0");
        switch (type) {
            case 0:
                ltDelay.setVisibility(View.GONE);
                ltId.setVisibility(View.GONE);
                ltChange.setVisibility(View.GONE);
                tvTitle.setText("删除雷管");
                break;
            case 1:
            case 2:
                tvCount.setText("雷管发数");
                tvTitle.setText("添加雷管");
                tvStart.setText("起始孔号");
                break;
            case 3:
                ltId.setVisibility(View.GONE);
                tvTitle.setText("修改延时");
                break;
            case 4:
                ltDelay.setVisibility(View.GONE);
                ltId.setVisibility(View.GONE);
                tvTitle.setText("清除编码");
                break;

        }
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
        int start, end, delay, change;
        int i;
        if (!etStart.getText().toString().isEmpty())
            try {
                start = Integer.parseInt(etStart.getText().toString());
            } catch (Exception e) {
                start = 10000;
            }
        else
            start = -1;

        if (!etEnd.getText().toString().isEmpty()) {
            try {
                end = Integer.parseInt(etEnd.getText().toString());
            } catch (Exception e) {
                end = 10000;
            }
        } else
            end = -1;

        if (!etDelay.getText().toString().isEmpty()) {
            try {
                delay = Integer.parseInt(etDelay.getText().toString());
            } catch (Exception e) {
                delay = 1000000;
            }
        } else
            delay = -1;
        if (!etChange.getText().toString().isEmpty()) {
            try {
                change = Integer.parseInt(etChange.getText().toString());
            } catch (Exception e) {
                change = 1000000;
            }
        } else
            change = -1;

        switch (view.getId()) {
            case R.id.btExit:
                dismissAllowingStateLoss();
                break;
            case R.id.btConfirm:
                switch (type) {
                    case 0:
                        if (!checkStartEndDelay(start, end, 0))
                            break;
                        if (!checkStartEndLimit(start, end))
                            break;
                        for (i = start - 1; i < end; i++)
                            dataViewModel.detonatorList.get(0).remove(start - 1);
                        updateData();
                        break;
                    case 1:
                    case 2:
                        if (start == -1) {
                            showError("请输入起始孔号！");
                            break;
                        }
                        if (end == -1) {
                            showError("请输入雷管发数！");
                            break;
                        }
                        if (start > 1000 || start < 1) {
                            showError("起始孔号超范围！");
                            break;
                        }
                        if (end + start > 1000) {
                            showError("孔号超过1000！");
                            break;
                        }
                        if (end + dataViewModel.detonatorDatas.size() > 1000) {
                            showError("总发数超过1000！");
                            break;
                        }
                        if (change == -1) {
                            showError("请输入时差！");
                            break;
                        }
                        if (delay == -1) {
                            showError("请输入起始延时！");
                            break;
                        }
                        if (delay > 60000) {
                            showError("起始延时大于60秒！");
                            break;
                        }
                        if (delay + (end - 1) * change > 60000) {
                            showError("总延时超过60秒！");
                            break;
                        }
                        String uuidStr = etId.getText().toString();
                        if (uuidStr.length() != 13) {
                            showError("编码长度不等于13！");
                            break;
                        }
                        if (!checkUuidString(uuidStr)) {
                            showError("编码格式错误！");
                            break;
                        }

                        DetonatorData detonatorData;
                        long id = Long.parseLong(uuidStr.substring(8));
                        for (i = 0; i < end; i++) {
                            detonatorData = new DetonatorData(row);
                            detonatorData.setBlasterTime(delay);
                            detonatorData.setDelay(change);
                            detonatorData.setHoleNum(start + i);
                            detonatorData.setId(0);
                            detonatorData.setUuid(FileFunc.makeUuidString(uuidStr.substring(0,8), (int) (id / 100L), (int) (id % 100L)));
                            id++;
                            delay += change;
                            dataViewModel.detonatorList.get(0).add(detonatorData);
                        }
                        updateData();
                        break;
                    case 3:
                        if (!checkStartEndDelay(start, end, delay))
                            break;
                        if (!checkStartEndLimit(start, end))
                            break;
                        if (change == -1) {
                            showError("请输入孔间时差！");
                            break;
                        }
                        if (delay > 60000) {
                            showError("孔间时差超过60秒！");
                            break;
                        }
                        for (i = start - 1; i < end; i++) {
                            dataViewModel.detonatorList.get(0).get(i).setBlasterTime(delay);
                            delay += change;
                        }
                        updateData();
                        break;
                    case 4:
                        if (!checkStartEndDelay(start, end, 0))
                            break;
                        if (!checkStartEndLimit(start, end))
                            break;
                        for (i = start - 1; i < end; i++)
                            dataViewModel.detonatorList.get(0).get(i).setId(0);
                        updateData();
                        break;
                }
                break;
        }

    }

    private boolean checkStartEndDelay(int start, int end, int delay) {
        if (start == -1) {
            showError("请输入起始编号！");
            return false;
        }
        if (start == 0) {
            showError("起始编号不能为0！");
            return false;
        }

        if (end == -1 || end == 0) {
            showError("请输结束序号！");
            return false;
        }

        if (end == 0) {
            showError("结束孔序号不能为0！");
            return false;
        }

        if (delay == -1) {
            showError("请输入延时时间！");
            return false;
        }
        if (delay > 60000) {
            showError("延时时间超过60秒！");
            return false;
        }

        return true;
    }

    public boolean checkStartEndLimit(int start, int end) {
        if (start > dataViewModel.detonatorList.get(0).size()) {
            showError("起始序号超范围！");
            return false;
        }
        if (end > dataViewModel.detonatorList.get(0).size()) {
            showError("结束序号超范围！");
            return false;
        }
        if (end < start) {
            showError("结束孔序号不能小于起始序号！");
            return false;
        }

        return true;
    }

    private void updateData() {
        int time = 0;
        int i, j;
        dataViewModel.dataChanged = true;
        dataViewModel.detonatorDatas.clear();
        for (j = 0; j < dataViewModel.detonatorList.get(0).size(); j++) {
            dataViewModel.detonatorDatas.add(dataViewModel.detonatorList.get(0).get(j));
        }
        FileFunc.saveDetonatorFile(dataViewModel.fileName, dataViewModel.detonatorSetting, dataViewModel.detonatorDatas);
        dataViewModel.dataShift=true;
        FragmentLoad fragmentLoad = new FragmentLoad();
        fragmentLoad.setCancelable(false);
        fragmentLoad.show(getActivity().getSupportFragmentManager(), "load");
        dismissAllowingStateLoss();
    }

    private void showError(String str) {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getParentFragmentManager(), "info");
    }
}




