package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.xdyblaster.fragment.FragmentAuthInput;
import com.example.xdyblaster.util.AndroidDes3Util;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.HttpUtil;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.SharedPreferencesUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.internal.CustomAdapt;
import okhttp3.Response;
import utils.SerialPortUtils;

import static com.example.xdyblaster.util.FileFunc.saveAuthFile;

public class DownloadAuthActivity extends AppCompatActivity implements CustomAdapt {

    @BindView(R.id.lt_htid)
    LinearLayout ltHtid;
    @BindView(R.id.etXmbh)
    EditText etXmbh;
    @BindView(R.id.lt_xmbh)
    LinearLayout ltXmbh;
    @BindView(R.id.etDwdm)
    EditText etDwdm;
    @BindView(R.id.lt_dwdm)
    LinearLayout ltDwdm;
    @BindView(R.id.cbTester)
    CheckBox cbTester;
    @BindView(R.id.btAdd)
    Button btAdd;
    @BindView(R.id.btDel)
    Button btDel;
    @BindView(R.id.btEdit)
    Button btEdit;
    List<String> lgData = new ArrayList<String>();
    List<Integer> lgSel = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    @BindView(R.id.listLg)
    ListView listLg;
    @BindView(R.id.tvTotal)
    TextView tvTotal;
    @BindView(R.id.etHtid)
    EditText etHtid;
    LinkedHashMap<String, String> httpParams;
    @BindView(R.id.btImport)
    Button btImport;
    @BindView(R.id.etPeople)
    EditText etPeople;
    @BindView(R.id.lt_people)
    LinearLayout ltPeople;
    @BindView(R.id.etJd)
    EditText etJd;
    @BindView(R.id.etWd)
    EditText etWd;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.lt_position)
    LinearLayout ltPosition;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;
    String htid, xmbh, dwdm;
    String head;
    AlertDialog dialog;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_auth);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_file, lgData);
        listLg.setAdapter(arrayAdapter);
        listLg.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        etJd.setText(String.format("%f", serialPortUtils.lng));
        etWd.setText(String.format("%f", serialPortUtils.lat));
        etHtid.setText((String) SharedPreferencesUtils.getParam(this, "htid", ""));
        etXmbh.setText((String) SharedPreferencesUtils.getParam(this, "xmbh", ""));
        etDwdm.setText((String) SharedPreferencesUtils.getParam(this, "dwdm", ""));
        etPeople.setText((String) SharedPreferencesUtils.getParam(this, "bprysfz", ""));
        cbTester.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ltHtid.setVisibility(View.GONE);
                    ltXmbh.setVisibility(View.GONE);
                    ltPeople.setVisibility(View.GONE);
                } else {
                    ltHtid.setVisibility(View.VISIBLE);
                    ltXmbh.setVisibility(View.VISIBLE);
                    ltPeople.setVisibility(View.VISIBLE);
                }
            }
        });
//        btEdit.setFocusable(true);
//        btEdit.setFocusableInTouchMode(true);
//        hideSoftKeyboard(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 500;
    }


    private void showError(String str) {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getSupportFragmentManager(), "info");
    }
//    private void showError(String str) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        Drawable drawable1 = ContextCompat.getDrawable(this, R.mipmap.ic_report_problem_white_48dp);
//        Drawable drawableR = tintDrawable(drawable1, ContextCompat.getColor(this, R.color.colorRed));
//        builder.setIcon(drawableR);
//        builder.setTitle("数据错误");
//        builder.setMessage(str);
////        final String[] choice=new String[]{"上海","北京","重庆","广州","天津"};
////        //设置单选对话框的监听
////        builder.setSingleChoiceItems(choice, 2, new DialogInterface.OnClickListener() {
////            @Override
////            public void onClick(DialogInterface dialog, int which) {
////                Toast.makeText(NewFileActivity.this,"你选中了"+choice[which], Toast.LENGTH_SHORT).show();
////            }
////        });
//        builder.setCancelable(true);
//        builder.create().show();
//    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @OnClick({R.id.btAdd, R.id.btDel, R.id.btEdit, R.id.btImport})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btAdd:
                FragmentAuthInput fragmentAuthInput = new FragmentAuthInput();
                Bundle bundle = new Bundle();
                bundle.putString("uuid", (String) SharedPreferencesUtils.getParam(DownloadAuthActivity.this, "auth uuid", ""));
                fragmentAuthInput.setArguments(bundle);
                fragmentAuthInput.onButtonClickListener = new FragmentAuthInput.OnButtonClickListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onButtonClick(int index, String uuid, int count, int each) {
                        String str = uuid;
                        SharedPreferencesUtils.setParam(DownloadAuthActivity.this, "auth uuid", uuid);
                        for (int i = 0; i < count; i++) {
                            for (int j = 0; j < each; j++) {
                                str = FileFunc.makeUuidString(uuid, i, j);
                                lgData.add(str);
                                lgSel.add(0);
                            }
                        }
                        tvTotal.setText("总数：" + lgData.size());
                        arrayAdapter.notifyDataSetChanged();
                    }
                };
                fragmentAuthInput.show(getSupportFragmentManager(), "input");
                break;
            case R.id.btDel:
                int i;
                for (i = 0; i < listLg.getCount(); i++) {
                    if (listLg.isItemChecked(i)) {
                        listLg.setItemChecked(i, false);
                        lgSel.set(i, 1);
                    } else
                        lgSel.set(i, 0);
                }
                i = 0;
                while (i < lgSel.size()) {
                    if (lgSel.get(i) == 1) {
                        lgSel.remove(i);
                        lgData.remove(i);

                    } else
                        i++;
                }
                arrayAdapter.notifyDataSetChanged();
                tvTotal.setText("总数：" + lgData.size());
                break;
            case R.id.btImport:
                lgData.clear();
                lgSel.clear();
                for (i = 0; i < dataViewModel.detonatorDatas.size(); i++) {
                    lgData.add(dataViewModel.detonatorDatas.get(i).getUuid());
                    lgSel.add(0);
                }
                Collections.sort(lgData);
                for (i = 0; i < listLg.getCount(); i++) {
                    listLg.setItemChecked(i, false);
                }
                tvTotal.setText("总数：" + lgData.size());
                arrayAdapter.notifyDataSetChanged();
                break;
            case R.id.btEdit:
                if (lgData.size() == 0)
                    break;

                htid = etHtid.getText().toString();
                xmbh = etXmbh.getText().toString();
                dwdm = etDwdm.getText().toString();
                dataViewModel.userId = etPeople.getText().toString();
                if (!cbTester.isChecked()) {
//                    if (htid.isEmpty() && xmbh.isEmpty()) {
//                        showError("请输入合同ID或项目编号");
//                        break;
//                    }
                    if (dataViewModel.userId.isEmpty()) {
                        showError("请输爆破人员身份证");
                        break;
                    }
                    if (!htid.isEmpty())
                        SharedPreferencesUtils.setParam(this, "htid", htid);
                    else
                        SharedPreferencesUtils.setParam(this, "htid", "");
                    if (!xmbh.isEmpty())
                        SharedPreferencesUtils.setParam(this, "xmbh", xmbh);
                    else
                        SharedPreferencesUtils.setParam(this, "xmbh", "");
                    SharedPreferencesUtils.setParam(this, "bprysfz", dataViewModel.userId);
                }
                if (dwdm.isEmpty()) {
                    showError("请输入单位代码");
                    break;
                }
                SharedPreferencesUtils.setParam(this, "dwdm", dwdm);

                httpParams = new LinkedHashMap<>();
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    String str;
                    //                  if (!cbTester.isChecked()) {
                    if (!htid.isEmpty())
                        httpParams.put("htid", htid);
                    if (!xmbh.isEmpty())
                        httpParams.put("xmbh", xmbh);
                    httpParams.put("wd", etWd.getText().toString());
                    httpParams.put("jd", etJd.getText().toString());
                    if (htid.isEmpty() && xmbh.isEmpty())
                        htid = "厂内试爆" + FileFunc.getDate();
//                            htid = FileFunc.getDate();


//                    } else {
//                        htid = "厂内试爆" + FileFunc.getDate();
////                        httpParams.put("wd", String.format("%f", serialPortUtils.lat));
////                        httpParams.put("jd", String.format("%f", serialPortUtils.lng));
//                        httpParams.put("jd", "106.59774");
//                        httpParams.put("wd", "23.90510");
//                }
                    httpParams.put("dwdm", dwdm);
                    httpParams.put("sbbh", dataViewModel.devId);
                    //httpParams.put("sbbh", "F53AC008018");

//                    httpParams.put("jd", "106.59774");//String.format("%f", mCurrentLat));
//                    httpParams.put("wd","23.90510"); //String.format("%f", mCurrentLon));
                    // if (serialPortUtils.latLng != null) {


//                    } else {
//                        httpParams.put("wd", String.format("%f", 0.0));
//                        httpParams.put("jd", String.format("%f", 0.0));
//                    }
                    for (i = 0; i < lgData.size(); i++) {
                        if (i != 0)
                            stringBuilder.append(',');
                        str = lgData.get(i);
//                        crc = CRC16(str.getBytes(), 13) & 0x0ffff;
//                        crc = ((crc << 8) & 0x0ff00) + ((crc >> 8) & 0x0ff);
                        stringBuilder.append(FileFunc.FbhToUid(str));
//                        stringBuilder.append(String.format("%04X", crc & 0x0ffff));
                    }
                    httpParams.put("uid", stringBuilder.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                head = "param=";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HttpReadWrite httpReadWrite = new HttpReadWrite();
                        httpReadWrite.execute(1);
                    }
                });
                break;
        }

    }

    private void connectDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.fragment_connect, null, false);
        dialog = new AlertDialog.Builder(DownloadAuthActivity.this).setView(view).create();
        Window win = dialog.getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        assert win != null;
        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        win.setDimAmount(0.4f);
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        layoutParams.alpha = 0.9f;
        win.setAttributes(layoutParams);
        TextView tvConnect = view.findViewById(R.id.textView_connect_Status);
        tvConnect.setText("连接服务器");
        dialog.show();

        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4  注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
    }


    @SuppressLint("StaticFieldLeak")
    public class HttpReadWrite extends AsyncTask<Integer, Object, Integer> {
        @Override
        protected void onPreExecute() {
            connectDialog();
//            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            Response response = null;

            while (!dialog.isShowing()) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            switch (integers[0]) {
                case 1:
                case 2:
                case 3:
                    if (integers[0] == 1) {
                        response = HttpUtil.sendPostMessage(0, head, httpParams);
                    } else if (integers[0] == 2) {
                        response = HttpUtil.sendPostMessage(1, head, httpParams);
                    } else if (integers[0] == 3) {
                        response = HttpUtil.sendPostMessage(1, head);
                    }


                    if (response != null) {
                        try {
                            publishProgress(integers[0], 1, Objects.requireNonNull(response.body()).string());
                            //Log.e("http", "response: " + Objects.requireNonNull(response.body()).string());
                        } catch (Exception e) {
                            e.printStackTrace();
                            publishProgress(integers[0], -1, "");
                        }
                    } else {
                        publishProgress(integers[0], -1, "");
                    }
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            int step = (int) values[0];
            int flag = (int) values[1];
            String str = "";
            String cwxxms = "";
            switch (step) {
                case 1:
                    boolean b = true;
                    dialog.dismiss();
                    if (flag == 1) {
                        try {
                            str = AndroidDes3Util.decode((String) values[2], "jadl12345678912345678912");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!str.isEmpty()) {
                            try {
                                JSONObject jsonObject = new JSONObject(str);
                                if (jsonObject.getString("cwxx").equals("0"))
                                    b = false;
                                else
                                    cwxxms = jsonObject.getString("cwxxms");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (b) {
                        InfoDialog infoDialog = new InfoDialog();
                        infoDialog.setTitle("故障");
                        infoDialog.setMessage("数据下载失败！" + cwxxms);
                        infoDialog.setBtnEnable(true);
                        infoDialog.show(getSupportFragmentManager(), "info");
                    } else {
                        String fileName = FileFunc.getDate() + " " + htid;// (String) SharedPreferencesUtils.getParam(AuthorizeActivity.this, "htid", "htid");
                        saveAuthFile(fileName, str, htid, xmbh, dwdm);
                        Intent intent = new Intent(DownloadAuthActivity.this, AuthViewActivity.class);
                        intent.putExtra("file", fileName);
                        startActivity(intent);
                    }
                    break;
                case 2:
                case 3:
                    dialog.dismiss();
                    break;
            }

            super.onProgressUpdate(values);
        }
    }

}