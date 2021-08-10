package com.example.xdyblaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xdyblaster.Adapter.DetonatorAdapter;
import com.example.xdyblaster.Adapter.ResultAdapter;
import com.example.xdyblaster.mina.client.MinaClient;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.FileFunc;
import com.example.xdyblaster.util.HttpUtil;
import com.example.xdyblaster.util.InfoDialog;
import com.example.xdyblaster.util.ResultData;
import com.example.xdyblaster.util.ResultDataComparator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import static com.example.xdyblaster.util.FileFunc.getSDPath;
import static com.example.xdyblaster.util.FileFunc.loadDetonateResult;
import static com.example.xdyblaster.util.UartData.CRC16;

public class UploadResultActivity extends AppCompatActivity implements CustomAdapt {


    @BindView(R.id.bt_view)
    Button btView;
    List<String> listFileName = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    String sbbh, jd, wd, bpsj, uid, htid, xmbh, dwdm;
    @BindView(R.id.bt_data)
    Button btData;
    @BindView(R.id.bt_upload_gx)
    Button btUploadGx;
    @BindView(R.id.list_auth)
    RecyclerView listAuth;
    private DataViewModel dataViewModel;
    private SerialPortUtils serialPortUtils;
    LinkedHashMap<String, String> httpParams;
    String head;
    public AlertDialog dialog;
    JSONArray lgs;
    InfoDialog info;
    List<ResultData> resultDatas;
    ResultAdapter resultAdapter;
    LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_upload_result);
        ButterKnife.bind(this);
        serialPortUtils = SerialPortUtils.getInstance(this);
        dataViewModel = new ViewModelProvider(serialPortUtils.mActivity).get(DataViewModel.class);
        initFileList();
        resultAdapter = new ResultAdapter(this, resultDatas, dataViewModel);
        layoutManager = new LinearLayoutManager(this);
        listAuth.setLayoutManager(layoutManager);
        listAuth.setAdapter(resultAdapter);


    }

    public void initFileList() {
        byte[] b;
        File file = new File(getSDPath() + "//xdyBlaster//result//index.jason");
        resultDatas = new ArrayList<>();
        ResultData resultData;
        JSONObject jsonObject;
        JSONArray jsonArray;
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(fis);
                b = new byte[dis.available()];
                dis.readFully(b);
                dis.close();
                fis.close();
                String str = new String(b);
                jsonObject = new JSONObject(str);
                jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject result = jsonArray.getJSONObject(i);
                    resultData = new ResultData();
                    resultData.name = result.getString("name");
                    resultData.upD = result.getString("upD");
                    resultData.upG = result.getString("upG");
                    resultDatas.add(resultData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @OnClick({R.id.bt_view, R.id.bt_data, R.id.bt_upload_gx})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_view:
                if (resultAdapter.opened != -1) {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //connectDialog();
                            HttpReadWrite httpReadWrite = new HttpReadWrite();
                            httpReadWrite.execute(1);
                        }
                    });

                }
                break;
            case R.id.bt_data:
                if (resultAdapter.opened != -1) {
                    Intent intent = new Intent(UploadResultActivity.this, ResultDataActivity.class);
                    intent.putExtra("file", resultDatas.get(resultAdapter.opened).name);
                    startActivity(intent);
                }
                break;
            case R.id.bt_upload_gx:
                if (resultAdapter.opened != -1) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //connectDialog();
                            TcpWrite tcpWrite = new TcpWrite();
                            tcpWrite.execute(1);
                        }
                    });

                }

        }
    }

    public void saveReultIndex() {
        byte[] b;
        File file = new File(getSDPath() + "//xdyBlaster//result//index.jason");
        JSONObject jsonObject;
        JSONArray jsonArray;
        if (file.exists()) {
            try {
                jsonArray = new JSONArray();
                for (int i = 0; i < resultDatas.size(); i++) {
                    jsonObject = new JSONObject();
                    jsonObject.put("name", resultDatas.get(i).name);
                    jsonObject.put("upD", resultDatas.get(i).upD);
                    jsonObject.put("upG", resultDatas.get(i).upG);
                    jsonArray.put(jsonObject);
                }
                jsonObject = new JSONObject();
                jsonObject.put("results", jsonArray);
                b = jsonObject.toString().getBytes();
                FileOutputStream fos = new FileOutputStream(file);
                DataOutputStream dos = new DataOutputStream(fos);
                dos.write(b, 0, b.length);
                dos.flush();
                dos.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class HttpReadWrite extends AsyncTask<Integer, Object, Integer> {

        @Override
        protected Integer doInBackground(Integer... integers) {

            //while (!dialog.isShowing()) ;
            List<String> list = new ArrayList<>();


            Response response = null;
            lgs = loadDetonateResult(resultDatas.get(resultAdapter.opened).name, dataViewModel);
            if (lgs == null) {
                publishProgress(1, -1);
                return null;
            }
            head = "param=";
            httpParams = new LinkedHashMap<>();
            String str;
            if (!dataViewModel.htid.isEmpty()) {
                str = dataViewModel.htid.substring(0, 1);
                if (!str.equals("厂"))
                    httpParams.put("htid", dataViewModel.htid);
            }
            if (!dataViewModel.xmbh.isEmpty())
                httpParams.put("xmbh", dataViewModel.xmbh);
            //"4526002200001");
            httpParams.put("dwdm", dataViewModel.dwdm);
            httpParams.put("sbbh", dataViewModel.devId);
            httpParams.put("jd", dataViewModel.jd);
            httpParams.put("wd", dataViewModel.wd);
            httpParams.put("bpsj", dataViewModel.bpsj);
            httpParams.put("bprysfz", dataViewModel.userId);
            StringBuilder stringBuilder = new StringBuilder();
            int crc;

            for (int j = 0; j < lgs.length(); j++) {
                if (j != 0)
                    stringBuilder.append(",");
                try {
                    str = lgs.getString(j).substring(0, 13);
                    list.add(str);
//                    crc = CRC16(str.getBytes(), 13) & 0x0ffff;
//                    crc = ((crc << 8) & 0x0ff00) + ((crc >> 8) & 0x0ff);
                    stringBuilder.append(FileFunc.FbhToUid(str));
//                    stringBuilder.append(str);
//                    stringBuilder.append(String.format("%04X", crc & 0x0ffff));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            MinaClient minaClient=new MinaClient();
//            minaClient.start(list,dataViewModel);
            httpParams.put("uid", stringBuilder.toString());
            response = HttpUtil.sendPostMessage(2, head, httpParams);
            if (response == null) {
                publishProgress(1, -1);
                return null;
            }

            try {
                str = Objects.requireNonNull(response.body()).string();
                JSONObject jsonObject = new JSONObject(str);
                str = jsonObject.getString("success");
                if (str.equals("true"))
                    publishProgress(1, 1);
                else
                    publishProgress(1, -1);
                Log.e("http", "response: " + str);
            } catch (Exception e) {
                e.printStackTrace();
                publishProgress(1, -1);
            }

//            MinaClient minaClient = new MinaClient();
//            minaClient.start(list, dataViewModel);
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

            //   dialog.dismiss();
            if (flag == -1) {
                showError("上传失败！");

            } else {
                InfoDialog infoDialog = new InfoDialog();
                infoDialog.setTitle("正常");
                infoDialog.setMessage("上传成功！");
                infoDialog.show(getSupportFragmentManager(), "info");
                resultDatas.get(resultAdapter.opened).upD = "1";
                saveReultIndex();
                resultAdapter.notifyDataSetChanged();


//                int i=listAuth.getCheckedItemPosition();
//                File file = new File(getSDPath() + "//xdyBlaster//result//" + listFileName.get(i) + ".json");
//                file.delete();
//                listFileName.remove(i);
//                arrayAdapter.notifyDataSetChanged();
//                if (listAuth.getCount() > 0) {
//                    if (listAuth.getCount() == i)
//                        i--;
//                    listAuth.setItemChecked(i, true);
//                    listAuth.setSelection(i);
//                }
//                arrayAdapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class TcpWrite extends AsyncTask<Integer, Object, Integer> {
        public boolean finish;

        @Override
        protected Integer doInBackground(Integer... integers) {

            //while (!dialog.isShowing()) ;
            List<String> list = new ArrayList<>();
            lgs = loadDetonateResult(resultDatas.get(resultAdapter.opened).name, dataViewModel);
            if (lgs == null) {
                publishProgress(1, -1);
                return null;
            }
            String str;
            for (int j = 0; j < lgs.length(); j++) {
                try {
                    str = lgs.getString(j).substring(0, 13);
                    list.add(str);
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress(1, -1);
                    return null;
                }
            }
            finish = false;
            MinaClient minaClient = new MinaClient();
            minaClient.onSendDataListener = new MinaClient.OnSendDataListener() {
                @Override
                public void sendDataResult(int percent) {
                    publishProgress(1, percent);
                }
            };
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    minaClient.start(list, dataViewModel);
                }
            });
            thread.start();
            while (!finish) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
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

            switch (flag) {
                case -1:
                    showError("上传失败！");
                    finish = true;
                    break;
                case 0:
                    info = new InfoDialog();
                    info.setTitle("上传广西中爆网");
                    info.setMessage("开始上传。。。");
                    info.setProgressEnable(true);
                    info.setProgressBarMax(100);
                    info.setCancelable(false);
                    info.show(getSupportFragmentManager(), "info");
                    break;
                case 100:
                    info.setMessageTxt("上传成功!");
                    info.progressBar.setProgress(100);
                    info.setCancelable(true);
                    finish = true;
                    resultDatas.get(resultAdapter.opened).upG = "1";
                    saveReultIndex();
                    resultAdapter.notifyDataSetChanged();
                    break;
                default:
                    info.progressBar.setProgress(flag);
                    break;

            }

        }
    }

    private void showError(String str) {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.setTitle("数据错误");
        infoDialog.setMessage(str);
        infoDialog.show(getSupportFragmentManager(), "info");
    }

//    private void connectDialog() {
//        View view = LayoutInflater.from(this).inflate(R.layout.fragment_connect, null, false);
//        dialog = new AlertDialog.Builder(this).setView(view).create();
//        Window win = dialog.getWindow();
//        // 一定要设置Background，如果不设置，window属性设置无效
//        win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        win.setDimAmount(0.4f);
//        WindowManager.LayoutParams layoutParams = win.getAttributes();
//        layoutParams.alpha = 0.9f;
//        win.setAttributes(layoutParams);
//        TextView tvConnect = view.findViewById(R.id.textView_connect_Status);
//        tvConnect.setText("连接服务器");
//        dialog.show();
//
//        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4  注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
//    }

}
