package com.example.xdyblaster.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import androidx.core.graphics.drawable.DrawableCompat;

import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.example.xdyblaster.util.CommDetonator.fbh_month_high;
import static com.example.xdyblaster.util.CommDetonator.fbh_month_low;
import static com.example.xdyblaster.util.CommDetonator.fbh_year_low;
import static com.example.xdyblaster.util.UartData.CRC16;

public class FileFunc {

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }
        return sdDir.toString();
    }

    public static String stringToJSON(String strJson) {
        // 计数tab的个数
        int tabNum = 0;
        StringBuffer jsonFormat = new StringBuffer();
        int length = strJson.length();

        char last = 0;
        for (int i = 0; i < length; i++) {
            char c = strJson.charAt(i);
            if (c == '{') {
                tabNum++;
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else if (c == '}') {
                tabNum--;
                jsonFormat.append("\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
                jsonFormat.append(c);
            } else if (c == ',') {
                jsonFormat.append(c + "\n");
                jsonFormat.append(getSpaceOrTab(tabNum));
            } else if (c == ':') {
                jsonFormat.append(c + " ");
            } else if (c == '[') {
                tabNum++;
                char next = strJson.charAt(i + 1);
                if (next == ']') {
                    jsonFormat.append(c);
                } else {
                    jsonFormat.append(c + "\n");
                    jsonFormat.append(getSpaceOrTab(tabNum));
                }
            } else if (c == ']') {
                tabNum--;
                if (last == '[') {
                    jsonFormat.append(c);
                } else {
                    jsonFormat.append("\n" + getSpaceOrTab(tabNum) + c);
                }
            } else {
                jsonFormat.append(c);
            }
            last = c;
        }
        return jsonFormat.toString();
    }

    private static String getSpaceOrTab(int tabNum) {
        StringBuffer sbTab = new StringBuffer();
        for (int i = 0; i < tabNum; i++) {
            sbTab.append('\t');
        }
        return sbTab.toString();
    }


    public static boolean checkFileExists(String str) {
        File file = new File(getSDPath() + "//xdyBlaster//" + str);
        return file.exists();
    }

    @SuppressLint("DefaultLocale")
    public static void makeDetonatorFile(String fileName, DetonatorSetting setting) {
        File file = new File(getSDPath() + "//xdyBlaster");
        if (!file.exists())
            file.mkdirs();
        file = new File(getSDPath() + "//xdyBlaster//" + fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            byte[] b;
            String str;
            int i = 0, j;
            if (setting.isRowSequence())
                i = 1;
            str = String.format("%d,%d,%d,%d,%d\n", setting.getRow(), setting.getHole(), setting.getRowDelay(), setting.getHoleDelay(), i);
            b = str.getBytes();
            dos.write(b, 0, b.length);
            int delay = 0;
            for (i = 0; i < setting.getRow(); i++) {
                if (!setting.isRowSequence())
                    delay = 0;
                for (j = 0; j < setting.getHole(); j++) {
                    if (j != 0) {
                        delay += setting.getHoleDelay();
                        str = String.format("%03d,%04d,0,%d,%d,0,0,0\n", i, j, setting.getHoleDelay(), delay);
                    } else {
                        if (i == 0) {
                            str = String.format("%03d,%04d,0,%d,%d,0,0,0\n", i, j, 0, delay);
                        } else {
                            delay += setting.getRowDelay();
                            str = String.format("%03d,%04d,0,%d,%d,0,0,0\n", i, j, setting.getRowDelay(), delay);
                        }
                    }
                    b = str.getBytes();
                    dos.write(b, 0, b.length);
                }
            }
//
//            dos.writeInt(setting.getRow());
//            dos.writeInt(setting.getHole());
//            dos.writeInt(setting.getCnt());
//            dos.writeInt(setting.getRowDelay());
//            dos.writeInt(setting.getHoleDelay());
//            int time = 0;
//            for (int i = 0; i < setting.getRow(); i++) {
//                if (i != 0)
//                    time += setting.getRowDelay();
//                for (int j = 0; j < setting.getHole(); j++) {
//                    time += setting.getHoleDelay();
//                    for (int k = 0; k < setting.getCnt(); k++) {
//                        dos.writeInt(i);
//                        dos.writeInt(j);
//                        dos.writeInt(k);
//                        dos.writeInt(0);
//                        dos.writeInt(time);
//                        dos.writeInt(setting.getHoleDelay());
//                        dos.writeInt(0);
//                        dos.writeInt(0);
//                    }
//                }
//            }
            dos.flush();
            dos.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void loadDetonatorFile(String fileName, DetonatorSetting setting, List<DetonatorData> detonatorDatas) {
        File file = new File(getSDPath() + "//xdyBlaster");
        byte b;
        String tmp;
        if (!file.exists())
            file.mkdirs();
        file = new File(getSDPath() + "//xdyBlaster//" + fileName);
        if (file.exists()) {
            try {
                String str;
                int index;
                FileInputStream fis = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(fis);
                detonatorDatas.clear();
                str = readOneLine(dis);

                index = str.indexOf(',');
                setting.setRow(str.substring(0, index));
                if (setting.getRow() == 0)
                    setting.setRow(1);
                str = str.substring(index + 1);

                index = str.indexOf(',');
                setting.setHole(str.substring(0, index));
                str = str.substring(index + 1);

                index = str.indexOf(',');
                setting.setRowDelay(str.substring(0, index));
                str = str.substring(index + 1);

                index = str.indexOf(',');
                setting.setHoleDelay(str.substring(0, index));
                str = str.substring(index + 1);

                if (Integer.parseInt(str) == 1)
                    setting.setRowSequence(true);
                else
                    setting.setRowSequence(false);

                while (true) {
                    DetonatorData data = new DetonatorData();
                    str = readOneLine(dis);
                    if (str.isEmpty())
                        break;
                    try {
                        index = str.indexOf(',');
                        data.setRowNum(Integer.parseInt(str.substring(0, index)));
                        str = str.substring(index + 1);
                        index = str.indexOf(',');
                        data.setHoleNum(Integer.parseInt(str.substring(0, index)));
                        str = str.substring(index + 1);
                        index = str.indexOf(',');
                        tmp = str.substring(0, index).toLowerCase();
                        long d = Long.valueOf(tmp, 16);
                        data.setId(d);
                        str = str.substring(index + 1);
                        index = str.indexOf(',');
                        data.setDelay(Integer.parseInt(str.substring(0, index)));
                        str = str.substring(index + 1);
                        index = str.indexOf(',');
                        data.setBlasterTime(Integer.parseInt(str.substring(0, index)));
                        str = str.substring(index + 1);
                        index = str.indexOf(',');
                        data.setMainFrequency(Integer.parseInt(str.substring(0, index)));
                        str = str.substring(index + 1);
                        index = str.indexOf(',');
                        data.setSubFrequency(Integer.parseInt(str.substring(0, index)));
                        str = str.substring(index + 1);
                        data.setUuid(str);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    detonatorDatas.add(data);
                }


//                setting.setRow(String.valueOf(dis.readInt()));
//                setting.setHole(String.valueOf(dis.readInt()));
//                setting.setCnt(String.valueOf(dis.readInt()));
//                setting.setRowDelay(String.valueOf(dis.readInt()));
//                setting.setHole(String.valueOf(dis.readInt()));
//                while (dis.available() != 0) {
//                    DetonatorData data = new DetonatorData();
//                    data.setRowNum(dis.readInt());
//                    data.setHoleNum(dis.readInt());
//                    data.setCntNum(dis.readInt());
//                    data.setId(dis.readInt());
//                    data.setBlasterTime(dis.readInt());
//                    data.setDelay(dis.readInt());
//                    data.setMainFrequency(dis.readInt());
//                    data.setSubFrequency(dis.readInt());
//                    detonatorDatas.add(data);
//                }
                dis.close();
                fis.close();

            } catch (IOException e) {
                Log.e("open file", "打开文件失败");
                e.printStackTrace();
            }
        }
    }

    public static boolean loadDetonatorSetting(String fileName, DetonatorSetting setting) {
        File file = new File(getSDPath() + "//xdyBlaster");
        byte b;
        String tmp;
        if (!file.exists())
            file.mkdirs();
        file = new File(getSDPath() + "//xdyBlaster//" + fileName);
        if (file.exists()) {
            try {
                String str;
                int index;
                FileInputStream fis = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(fis);
                str = readOneLine(dis);

                index = str.indexOf(',');
                if (index == -1)
                    return false;
                setting.setRow(str.substring(0, index));
                if (setting.getRow() == 0)
                    setting.setRow(1);
                str = str.substring(index + 1);

                index = str.indexOf(',');
                if (index == -1)
                    return false;
                setting.setHole(str.substring(0, index));
                str = str.substring(index + 1);

                index = str.indexOf(',');
                if (index == -1)
                    return false;
                setting.setRowDelay(str.substring(0, index));
                str = str.substring(index + 1);

                index = str.indexOf(',');
                if (index == -1)
                    return false;
                setting.setHoleDelay(str.substring(0, index));
                str = str.substring(index + 1);

                if (Integer.parseInt(str) == 1)
                    setting.setRowSequence(true);
                else
                    setting.setRowSequence(false);
                dis.close();
                fis.close();
                return true;

            } catch (IOException e) {
                Log.e("open file", "打开文件失败");
                e.printStackTrace();
            }
        }
        return false;
    }

    @SuppressLint("DefaultLocale")
    public static void saveDetonatorFile(String fileName, DetonatorSetting setting, List<DetonatorData> detonatorDatas) {
        File file = new File(getSDPath() + "//xdyBlaster");
        if (!file.exists())
            file.mkdirs();
        file = new File(getSDPath() + "//xdyBlaster//" + fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            byte[] b;
            String str;
            int i = 0, j;
            if (setting.isRowSequence())
                i = 1;
            str = String.format("%d,%d,%d,%d,%d\n", setting.getRow(), setting.getHole(), setting.getRowDelay(), setting.getHoleDelay(), i);
            b = str.getBytes();
            dos.write(b, 0, b.length);
            for (DetonatorData data : detonatorDatas) {
                str = String.format("%03d,%04d,%x,%d,%d,%d,%d,%s\n", data.getRowNum(), data.getHoleNum(), data.getId(), data.getDelay(), data.getBlasterTime(), data.getMainFrequency(), data.getSubFrequency(), data.getUuid());
                b = str.getBytes();
                dos.write(b, 0, b.length);
            }
            dos.flush();
            dos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readOneLine(DataInputStream dis) {
        StringBuilder str = new StringBuilder();
        byte[] b = new byte[256];
        int i;
        try {
            while (dis.available() != 0) {
                b[0] = dis.readByte();
                if ((b[0] != 0x0d) && (b[0] != 0x0a)) {
                    i = 1;
                    while (dis.available() != 0) {
                        b[i] = dis.readByte();
                        if ((b[i] == 0x0d) || (b[i] == 0x0a)) {
                            str.append(new String(b, 0, i));
                            break;
                        }
                        i++;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    public static Drawable tintDrawable(Drawable drawable, int colors) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable).mutate();
        DrawableCompat.setTint(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    public static int GetModbusData16(byte[] modbusData, int n) {
        int d;
        d = modbusData[n + 1];
        d = (d << 8) | modbusData[n];
        return d;
    }

    public static int GetModbusData32(byte[] modbusData, int n) {
        int d2, d = 0;
        int i, j;
        j = n + 3;
        for (i = 0; i < 4; i++) {
            d2 = modbusData[j];
            d2 = d2 & 0x000000ff;
            d = (d << 8) + d2;
            j--;
        }
        return d;
    }

    public static void SetModbusData16(byte[] modbusData, int d, int n) {
        modbusData[n] = (byte) (d & 0xff);
        modbusData[n + 1] = (byte) ((d >> 8) & 0xff);
    }

    public static void SetModbusData32(byte[] modbusData, int d, int n) {
        modbusData[n] = (byte) (d & 0xff);
        modbusData[n + 1] = (byte) ((d >> 8) & 0xff);
        modbusData[n + 2] = (byte) ((d >> 16) & 0xff);
        modbusData[n + 3] = (byte) ((d >> 24) & 0xff);
    }


    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
            r.append(" ");
        }
        return r.toString();
    }

    public static class ScreenUtils {

        /**
         * 获取屏幕高度(px)
         */
        public static int getScreenHeight(Context context) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }

        /**
         * 获取屏幕宽度(px)
         */
        public static int getScreenWidth(Context context) {
            return context.getResources().getDisplayMetrics().widthPixels;
        }

    }

    public static int getIntFromBytes(byte[] b, int n) {
        int i;
        i = b[n + 3] & 0x00ff;
        i = i << 8 | (b[n + 2] & 0x0ff);
        i = i << 8 | (b[n + 1] & 0x00ff);
        i = i << 8 | (b[n] & 0x00ff);
        return i;
    }

    public static int getShortFromBytes(byte[] b, int n) {
        int i;
        i = (b[n + 1]) & 0x00ff;
        i = ((i << 8) | (b[n] & 0x0ff)) & 0x00ffff;
        return i;
    }

    private final static char[] monthChar = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'C', 'D'};
    private final static char[] monthCharH = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1'};
    private final static char[] monthCharL = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3'};

    public static String convertUuidString(byte[] b, int ofs) {
        char[] uuid = new char[14];
        int date, year, day;
        int i;
        uuid[0] = 0;
        uuid[1] = (char) (b[ofs + 0] & 0x7f);
        uuid[2] = (char) (b[ofs + 1] & 0x7f);
        uuid[5] = (char) (b[ofs + 2] & 0x7f);
        uuid[8] = (char) (b[ofs + 3] & 0x7f);

        uuid[9] = (char) ((b[ofs + 5] & 0x0f) + 0x30);
        uuid[10] = (char) (((b[ofs + 6] >>> 4) & 0x0f) + 0x30);
        uuid[11] = (char) ((b[ofs + 6] & 0x0f) + 0x30);
        uuid[12] = (char) (((b[ofs + 7] >>> 4) & 0x0f) + 0x30);
        uuid[13] = (char) ((b[ofs + 7] & 0x0f) + 0x30);
        date = (b[ofs + 4] & 0x00ff);
        date = (date << 4) & 0x00ffff;
        date += ((b[ofs + 5] >> 4) & 0x0f);
        year = date / 40;
        day = date - year * 40;
        uuid[3] = (char) ((year % 10) + 0x30);
        for (i = 0; i < 12; i++)
            if (uuid[5] == monthChar[i])
                break;
        uuid[4] = monthCharH[i];
        uuid[5] = monthCharL[i];
        uuid[6] = (char) ((day / 10) + 0x30);
        uuid[7] = (char) ((day % 10) + 0x30);

        StringBuilder stringBuilder = new StringBuilder();
        for (i = 1; i < 14; i++) {
            if (isChar(uuid[i]))
                stringBuilder.append(uuid[i]);
            else
                stringBuilder.append('?');
        }
        return stringBuilder.toString();
    }

    public static boolean isChar(char c) {
        if ((c >= '0') && (c <= '9'))
            return true;
        if ((c >= 'A') && (c <= 'Z'))
            return true;
        return false;
    }

    public static void getUuidData(byte[] b, int ofs, UuidData uuidData) {
        int area;
        uuidData.setUuid(convertUuidString(b, ofs));
        area = b[ofs + 9] & 0x00ff;
        area = (area << 8) + (b[ofs + 8] & 0x00ff);
        uuidData.setArea(area / 1000);
        uuidData.setNum(area - area / 1000 * 1000);
        area = b[ofs + 11] & 0x00ff;
        area = (area << 8) + (b[ofs + 10] & 0x00ff);
        uuidData.setDelay(area);
        uuidData.setId(getIntFromBytes(b, ofs + 12) & 0x00ffffffffL);
    }

    public static boolean checkUuidString(String str) {
        byte[] b = str.getBytes();
        char c;
        int i;
        if (b.length != 13)
            return false;
        for (i = 0; i < 13; i++) {
            c = (char) (b[i] & 0x0ff);
            if (c < '0' || c > '9') {
                if (!((i == 0) || (i == 1) || (i == 7)))
                    break;
            }
        }
        if (i != 13)
            return false;
        else
            return true;
    }

    public static String getRealUuid(String str) {
        byte[] b = str.getBytes();
        byte yl;
        int m;
        yl = b[fbh_year_low];
        m = (b[fbh_month_high] - 0x30) & 0x0ff;
        m = m * 10 + ((b[fbh_month_low] - 0x30) & 0x0ff);
        b[fbh_year_low] = 0x32;
        b[fbh_month_high] = yl;
        b[fbh_month_low] = (byte) monthChar[m - 1];
        return new String(b);
    }

    public static int getErrorCount(List<DetonatorData> datas) {
        int color, n = 0;
        for (DetonatorData d : datas) {
            color = d.getColor();
            switch (color) {
                case 0x80:
                case 0x00:
                case 0x1f:
                case 0x40:
                    break;
                default:
                    n++;
                    break;
            }
        }
        return n;
    }

    public static void saveAuthFile(String fileName, String str, String htid, String xmbh, String dwdm) {
        byte[] b;
        File file = new File(getSDPath() + "//xdyBlaster//auth");
        if (!file.exists())
            file.mkdirs();
        file = new File(getSDPath() + "//xdyBlaster//auth//" + fileName + ".json");
        try {
            JSONObject lgxx = new JSONObject(str);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("htid", htid);
            jsonObject.put("xmbh", xmbh);
            jsonObject.put("dwdm", dwdm);
            jsonObject.put("lgxx", lgxx);
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

    public static String loadAuthFile(String fileName) {
        File file = new File(getSDPath() + "//xdyBlaster//auth");
        String tmp;
        byte[] b;
        if (!file.exists())
            file.mkdirs();
        file = new File(getSDPath() + "//xdyBlaster//auth//" + fileName + ".json");
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(fis);
                b = new byte[dis.available()];
                dis.readFully(b);
                dis.close();
                fis.close();
                return new String(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static Double Distance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6370996.81;  //地球的半径
        /*
         * 获取两点间x,y轴之间的距离
         */
        double x = (lng2 - lng1) * Math.PI * R * Math.cos(((lat1 + lat2) / 2) * Math.PI / 180) / 180;
        double y = (lat2 - lat1) * Math.PI * R / 180;
        return Math.hypot(x, y);
    }

    //
    public static JSONArray loadDetonateResult(String fileName, DataViewModel dataViewModel) {
        File file = new File(getSDPath() + "//xdyBlaster//result//" + fileName + ".json");
        byte[] b;
        if (file.exists())
            try {
                FileInputStream fis = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(fis);
                b = new byte[dis.available()];
                dis.readFully(b);
                dis.close();
                fis.close();
                String str = new String(b);
                JSONObject jsonObject = new JSONObject(str);
                dataViewModel.htid = getJsonString(jsonObject, "htid");
                dataViewModel.xmbh = getJsonString(jsonObject, "xmbh");
                dataViewModel.dwdm = getJsonString(jsonObject, "dwdm");
                dataViewModel.jd = getJsonString(jsonObject, "jd");
                dataViewModel.wd = getJsonString(jsonObject, "wd");
                dataViewModel.bpsj = getJsonString(jsonObject, "bpsj");
                dataViewModel.userId = getJsonString(jsonObject, "bprysfz");
                dataViewModel.devId = getJsonString(jsonObject, "sbbh");
                return jsonObject.getJSONArray("uuids");
            } catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }

    public static String getJsonString(JSONObject obj, String str) {
        String s;
        try {
            s = obj.getString(str);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return s;
    }

    public static String getDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    public static void saveDetonateResult(DataViewModel dataViewModel) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        File file = new File(getSDPath() + "//xdyBlaster//result");
        byte[] b;
        if (!file.exists())
            file.mkdirs();
        file = new File(getSDPath() + "//xdyBlaster//result//" + simpleDateFormat.format(date) + ".json");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            DataOutputStream dos = new DataOutputStream(fos);
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (DetonatorData d : dataViewModel.detonatorDatas) {
                //      jsonArray.put(getRealUuid(d.getUuid()));
                jsonArray.put(d.getUuid() + " <" + d.getBlasterTime() + "> " + (d.getRowNum() + 1) + '区' + (d.getHoleNum() + 1) + "孔");
            }
            jsonObject.put("sbbh", dataViewModel.devId);
            jsonObject.put("jd", dataViewModel.jd);
            jsonObject.put("wd", dataViewModel.wd);
            jsonObject.put("bpsj", simpleDateFormat.format(date));
            jsonObject.put("bprysfz", dataViewModel.userId);
            jsonObject.put("htid", dataViewModel.htid);
            if (!dataViewModel.xmbh.isEmpty())
                jsonObject.put("xmbh", dataViewModel.xmbh);
            jsonObject.put("dwdm", dataViewModel.dwdm);
            jsonObject.put("uuids", jsonArray);
            String str = jsonObject.toString();
            b = str.getBytes();
            dos.write(b, 0, b.length);
            dos.flush();
            dos.close();
            fos.close();
            addReultToIndex(simpleDateFormat.format(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addReultToIndex(String name) {
        byte[] b;
        File file = new File(getSDPath() + "//xdyBlaster//result//index.jason");
        List<ResultData> resultDataList = new ArrayList<>();
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
                    resultDataList.add(resultData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resultData = new ResultData();
        resultData.name = name;
        resultData.upD = "0";
        resultData.upG = "0";
        resultDataList.add(resultData);
        Collections.sort(resultDataList, new ResultDataComparator());
        try {
            jsonArray = new JSONArray();
            for (int i = 0; i < resultDataList.size(); i++) {
                jsonObject = new JSONObject();
                jsonObject.put("name", resultDataList.get(i).name);
                jsonObject.put("upD", resultDataList.get(i).upD);
                jsonObject.put("upG", resultDataList.get(i).upG);
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

    public static int checkDetonatorAuth(String fileName, DataViewModel dataViewModel, double lat, double lng) {
        int flag;
        AuthData authData = new AuthData(FileFunc.loadAuthFile(fileName));
        if (authData.dataErr)
            return 2;
        double lt1, lng1, bj, r;
        dataViewModel.htid = authData.htid;
        dataViewModel.xmbh = authData.xmbh;
        dataViewModel.dwdm = authData.dwdm;
        dataViewModel.wd = String.valueOf(lat);
        dataViewModel.jd = String.valueOf(lng);
        flag = 1;
        for (int i = 0; i < authData.zbqyList.size(); i++) {
            lt1 = Double.parseDouble(authData.zbqyList.get(i).zbqywd);
            lng1 = Double.parseDouble(authData.zbqyList.get(i).zbqyjd);
            bj = Double.parseDouble(authData.zbqyList.get(i).zbqybj);
            r = Distance(lt1, lng1, lat, lng);
            if (r < bj) {
                flag = 0;
                break;
            }
        }
        if (flag != 0)
            return flag;
        boolean b;
        for (DetonatorData d : dataViewModel.detonatorDatas) {
            b = false;
            for (LgData s : authData.lgDatas)
                if (s.fbh.equals(d.getUuid())) {
                    b = true;
                    break;
                }
            if (!b)
                return 2;
        }
        return 0;
    }


    private static int[] crc16tb =
            {
                    0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
                    0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
                    0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
                    0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
                    0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
                    0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
                    0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
                    0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
                    0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
                    0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
                    0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
                    0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
                    0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
                    0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
                    0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
                    0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
                    0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
                    0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
                    0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
                    0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
                    0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
                    0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
                    0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
                    0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
                    0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
                    0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
                    0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
                    0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
                    0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
                    0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
                    0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
                    0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
            };

    public static short uuidCrc16(byte[] buf, int len) {
        short crc = (short) 0x0FFFF;
        for (int i = 0; i < len; i++) {
            crc = (short) (crc16tb[(buf[i] ^ (crc >>> 8)) & 0x0ff] ^ ((crc & 0x0ff) << 8));
        }
        return crc;
    }

    public static String incUuidString(String str) {
        byte[] b = str.getBytes();
        for (int i = 12; i > 7; i--) {
            b[i] = (byte) (b[i] + 1);
            if (b[i] < '9' + 1)
                break;
            b[i] = '0';
        }
        return new String(b);
    }

    @SuppressLint("DefaultLocale")
    public static String makeUuidString(String str, int n1, int n2) {
        String box,f;
        String head = str.substring(0, 8);

        int b0, b1;
        try {
            box = str.substring(8, 11);
            f = str.substring(11, 13);
            b0 = Integer.parseInt(box);
            b1 = Integer.parseInt(f);
        } catch (Exception e) {
            b0 = 0;
            b1 = 0;
        }
        b0 = n1 + b0;
        b1 = n2 + b1;
        box = String.format("%03d", b0);
        f = String.format("%02d", b1);
        return head + box + f;
    }

    public static String UidToFbh(String str) {
        int i;
        byte[] b = str.getBytes();
        b[2] = b[3];
        for (i = 0; i < 12; i++) {
            if (b[4] == monthChar[i])
                break;
        }
        b[3] = (byte) (((i + 1) / 10) + '0');
        b[4] = (byte) (((i + 1) % 10) + '0');
        return new String(b);
    }

    public static String FbhToUid(String str) {
        int i;
        boolean t = true;
        byte[] b = str.getBytes();
        String s = str.substring(2, 7);
        try {
            i = Integer.parseInt(s);
            if (i > 721)
                t = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (t) {
            int crc = CRC16(str.getBytes(), 13) & 0x0ffff;
            crc = ((crc << 8) & 0x0ff00) + ((crc >> 8) & 0x0ff);
            return str + String.format("%04X", crc & 0x0ffff);
        } else {
            int m = b[3] - '0';
            m = m * 10 + (b[4] - '0');
            m = m - 1;
            if (m < 0 || m > 11)
                m = 0;
            b[4] = (byte) monthChar[m];
            b[3] = b[2];
            b[2] = '2';
        }

        return new String(b);


    }
}
