package com.example.xdyblaster.util;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

import utils.SerialPortUtils;

import static com.example.xdyblaster.util.AppConstants.*;
import static com.example.xdyblaster.util.FileFunc.getErrorCount;
import static com.example.xdyblaster.util.FileFunc.getIntFromBytes;
import static com.example.xdyblaster.util.FileFunc.getShortFromBytes;
import static com.example.xdyblaster.util.FileFunc.getUuidData;

public class CommDetonator extends AsyncTask<Integer, Integer, Integer> {
    final float[] battPercentTab = {
//            12.6f,
//            12.39f,
            12.18f,
            12.06f,
            11.94f,
            11.85f,
            11.76f,
            11.685f,
            11.61f,
            11.535f,
            11.46f,
            11.415f,
            11.37f,
            11.34f,
            11.31f,
            11.265f,
            11.22f,
            11.13f,
            11.04f,
            10.70f,
            10.35f,
            9.5f,
            9.0f,
    };
    public static final int COMM_RETRY = 6;
    public static final int COMM_CALIBRATE = 0;
    public static final int COMM_SCAN = 1;
    public static final int COMM_GET_TIMER = 2;
    public static final int COMM_GET_CURRENT = 3;
    public static final int COMM_TEST_BRIDGE = 4;
    public static final int COMM_TEST_FREQ = 5;
    public static final int COMM_GET_BATT = 6;
    public static final int COMM_GET_STATUS = 7;
    public static final int COMM_IDLE = 8;
    public static final int COMM_PUT_ID_BUFFER = 9;
    public static final int COMM_CHECK_NET = 10;
    public static final int COMM_DETONATE_PROGRESS = 11;
    public static final int COMM_GET_ID_BUFFER = 12;
    public static final int COMM_DETONATE = 13;
    public static final int COMM_COUNT_DOWN = 14;
    public static final int COMM_STOP_OUTPUT = 15;
    public static final int COMM_POWER_ON = 16;
    public static final int COMM_DELAY = 17;
    public static final int COMM_CHECK_ONLINE = 18;
    public static final int COMM_READ_AREA = 19;
    public static final int COMM_WRITE_AREA = 20;
    public static final int COMM_WRITE_TIME = 21;
    public static final int COMM_OFFLINE_SCAN = 22;
    public static final int COMM_OFFLINE_SCAN_STATUS = 23;
    public static final int COMM_RESET_DETONATOR = 24;
    public static final int COMM_BLIND_SCAN = 25;
    public static final int COMM_BLIND_SCAN_PROGRESS = 26;
    public static final int COMM_RESET = 27;
    public static final int COMM_UPDATE = 28;
    public static final int COMM_READ_UID = 29;
    //public static final int COMM_GET_ALL_DATA = 30;
    public static final int COMM_GET_UUID_BUFFER = 31;
    //public static final int COMM_PUT_UUID_BUFFER = 32;
    public static final int COMM_WAIT_PUBLISH = 33;
    public static final int COMM_DOWNLOAD_DATA = 34;
    public static final int COMM_WRITE_PASSWORD = 35;
    public static final int COMM_WRITE_UUID = 36;
    public static final int COMM_ENTER_TEST_MODE = 37;
    public static final int COMM_GET_TEST_STATUS = 38;
    public static final int COMM_TEST_CONTINUE = 39;
    public static final int COMM_STOP_TEST_MODE = 40;
    public static final int COMM_WRITE_DEV_ID = 41;
    public static final int COMM_WRITE_DEV_VER = 42;
    public static final int COMM_READ_DEV_ID = 43;
    public static final int COMM_READ_DEV_VER = 44;
    public static final int COMM_NONE = 45;
    public static final int COMM_ALL_DISCHARGE = 46;
    public static final int COMM_USER_DEFINE = 47;
    public static final int COMM_CALC_VOLTAGE = 48;
    public static final int COMM_SET_TEST_DELAY = 49;
    public static final int COMM_POWER_9V = 50;
    public static final int COMM_POWER_12V = 51;
    public static final int COMM_POWER_24V = 52;
    public static final int COMM_PUT_AREA_BUFFER = 53;
    public static final int COMM_READ_ALL_BRIDGE = 54;
    public static final int COMM_SET_CLK = 55;
    public SerialPortUtils serialPortUtils;
    public DataViewModel dataViewModel;
    private int ack, devStatus, index, detonatorStatus;
    //protected HandlerVolt handlerVolt;
    protected ArrayList<Boolean> job;

    private byte[] mac = new byte[4];
    byte[] timer = new byte[4];
    byte[] mainfreq = new byte[4];
    boolean threadStop = false;
    public boolean running, breaking;
    // private int errCnt;
    private int macid;
    private int data00;

    public void setData00(int data00) {
        this.data00 = data00;
    }

    public void setData10(int data10) {
        this.data10 = data10;
    }

    private int data10;
    private int eeData0, eeData1;
    public int id;
    public int step, type;
    private int data0, data1, data2;
    private int password;


    private int detonatorArea, detonatorHole, detonatorDelay, detonatorTime;
    private int total, count;
    private byte[] fileData;
    public int fileLen;
    protected boolean notSendErr = false;
    protected int cmdType;
    private long timeOut;
    //protected byte[] uuid = new byte[16];
    protected String stringUuid;
    private boolean publishSuccess;
    public boolean waitPublish;
    public int stepCount;


    public CommDetonator() {

    }

    @Override
    protected Integer doInBackground(Integer... value) {
        while (serialPortUtils.busy) {
            serialPortUtils.sendStop = true;
            dataViewModel.volt.postValue(-2);
            delayMs(10);
        }
        Log.e("commTask org", "start thread");
//        if(dataViewModel.reset) {
//            serialPortUtils.ResetBlaster();
//            dataViewModel.reset=false;
//        }

        int i, max;
        max = value.length;
        running = true;
        job = new ArrayList<>();
        for (i = 0; i < max; i++)
            job.add(true);
        for (i = 1; i < max; i++) {
            if (isCancelled() || !running) {
                running = false;
//                Log.e("commTask org", "thread end");
                return -1;
            } else {
                if (!job.get(i))
                    continue;
                stepCount = i;
                runInBackground(value[i]);
            }
        }
        Log.e("commTask org", "thread end");
        running = false;
        return 0;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Integer integer) {
        Log.e("commTask org", "close thread");
        serialPortUtils.sendStop = true;
        super.onCancelled(integer);
    }

    @Override
    protected void onCancelled() {
        serialPortUtils.sendStop = true;
        Log.e("commTask org", "close thread");
        super.onCancelled();
    }

    public void runInBackground(Integer s) {
        step = s;
        breaking = false;
        while (serialPortUtils.busy) {
            serialPortUtils.sendStop = true;
        }
        if (!(s == COMM_GET_BATT || s == COMM_STOP_OUTPUT)) {
            dataViewModel.commErr = 0;
        }
        Log.e("commTask org", "do job " + String.valueOf(s));
        cmdType = s;
        publishSuccess = true;
        switch (s) {
            case COMM_CALIBRATE:
                sendSingleCommand(DEV_CLIBRATE_CURRENT, _MODBUS_WRITE);
                break;
            case COMM_SCAN:
                onProgressUpdate(step, 2);
                timeOut = System.currentTimeMillis() + 2000;
                sendReadCommand(DEV_FAST_SCAN);
                if (timeOut != 0)
                    setMac(data0);
                else {
                    sendSingleCommand(DEV_IDLE, _MODBUS_WRITE);
                    running = false;
                }
                break;
            case COMM_GET_TIMER:
                setEEAddress(EE_COUNT_DOWN);
                sendReadCommand(DEV_READ_EE);
                break;
            case COMM_GET_CURRENT:
                waitPublish = true;
                sendSingleCommand(DEV_SINGLE_CURRENT, _MODBUS_READ);
                break;
            case COMM_TEST_BRIDGE:
                sendReadCommand(DEV_TEST_BRIDGE, 1500);
                break;
            case COMM_TEST_FREQ:
                sendReadCommand(DEV_TEST_MAIN_FREQ, 1000);
                break;
            case COMM_GET_BATT:
                getBattery(0);
                break;
            case COMM_GET_STATUS:
                getDetonatorStatus();
                break;
            case COMM_IDLE:
                sendSingleCommand(DEV_IDLE, _MODBUS_WRITE);
                break;
            case COMM_PUT_ID_BUFFER:
                putDetonatorID(0);
                break;
            case COMM_PUT_AREA_BUFFER:
                putDetonatorID(1);
                break;
            case COMM_CHECK_NET:
                sendSingleCommand(DEV_CHECK_NET, _MODBUS_WRITE);
                break;
            case COMM_DETONATE_PROGRESS:
            case COMM_BLIND_SCAN_PROGRESS:
                while (!isCancelled() && running && !breaking) {
                    sendSingleCommand(DEV_DETONATE_STEP, _MODBUS_READ);
                    delayMs(100);
                    if (!isCancelled() && running && !breaking) {
                        publishSuccess = false;
                        getBattery(0);
                        publishSuccess = true;
                    }
                    delayMs(100);
                }
                break;
            case COMM_GET_ID_BUFFER:
                waitPublish = true;
                publishSuccess = false;
                getDetonatorID();
                break;
            case COMM_DETONATE:
                sendSingleCommand(DEV_DETONATE, _MODBUS_WRITE);
                break;
            case COMM_COUNT_DOWN:
                sendSingleCommand(DEV_COUNT_DOWN, _MODBUS_WRITE);
                break;
            case COMM_STOP_OUTPUT:
                getBattery(1);
                break;
            case COMM_POWER_ON:
                serialPortUtils.uartData.volt = dataViewModel.defaultVolt;
                sendSingleCommand(DEV_TURN_ON_POWER, _MODBUS_WRITE);
                break;
            case COMM_POWER_12V:
                serialPortUtils.uartData.volt = 1200;
                sendSingleCommand(DEV_TURN_ON_POWER, _MODBUS_WRITE);
                break;
            case COMM_POWER_9V:
                serialPortUtils.uartData.volt = 900;
                sendSingleCommand(DEV_TURN_ON_POWER, _MODBUS_WRITE);
                break;
            case COMM_DELAY:
                serialPortUtils.DelayMs(2000);
                break;
            case COMM_CHECK_ONLINE:
                while (!isCancelled() && running && !breaking) {
                    getDetonatorStatus();
                    //sendReadCommand(DEV_SCAN_MAC);
                    delayMs(200);
                }
                break;
            case COMM_READ_AREA:
                serialPortUtils.uartData.eeAddress = EE_AREA;
                sendReadCommand(DEV_READ_EE);
                break;
            case COMM_WRITE_AREA:
                serialPortUtils.uartData.eeData1 = detonatorTime + BLASTER_TIMER_DELAY;
                serialPortUtils.uartData.eeData0 = (detonatorArea * 1000) + detonatorHole;
                serialPortUtils.uartData.eeAddress = EE_AREA;
                sendReadCommand(DEV_WRITE_EE);
                break;
            case COMM_WRITE_UUID:
                publishSuccess = false;
                if (writeDetonatorUuid())
                    publishProgress(step, 1);
                break;
            case COMM_WRITE_PASSWORD:
                serialPortUtils.uartData.eeData1 = password >>> 16;
                serialPortUtils.uartData.eeData0 = password & 0x00ffff;
                serialPortUtils.uartData.eeAddress = EE_PSW;
                sendReadCommand(DEV_WRITE_EE);
                break;
            case COMM_WRITE_TIME:
                serialPortUtils.uartData.eeData0 = detonatorTime + BLASTER_TIMER_DELAY;
                serialPortUtils.uartData.eeData1 = 0;
                serialPortUtils.uartData.eeAddress = EE_COUNT_DOWN;
                sendReadCommand(DEV_WRITE_EE);
                break;
            case COMM_OFFLINE_SCAN:
                sendSingleCommand(DEV_OFFLINE_SCAN, _MODBUS_WRITE);
                break;
            case COMM_OFFLINE_SCAN_STATUS:
                while (!isCancelled() && running && !breaking) {
                    sendSingleCommand(DEV_OFFLINE_SCAN, _MODBUS_READ);
                    delayMs(200);
                }
                break;
            case COMM_RESET_DETONATOR:
                sendSingleCommand(DEV_DET_RESET, _MODBUS_WRITE);
                delayMs(200);
                break;
            case COMM_BLIND_SCAN:
                sendSingleCommand(DEV_BLIND_SCAN, _MODBUS_WRITE);
                break;
            case COMM_RESET:
                serialPortUtils.ResetBlaster();
                delayMs(1000);
                sendSingleCommand(DEV_RESET, _MODBUS_WRITE);
                break;
            case COMM_UPDATE:
                publishSuccess = false;
                deviceUpdate();
                break;
            case COMM_READ_UID:
                publishSuccess = false;
                if (getDetonatorUuid()) {
                    publishProgress(COMM_READ_UID, 1);
                }
                break;
//            case COMM_GET_ALL_DATA:
//                sendSingleCommand(DEV_GET_ALL_DATA, _MODBUS_WRITE);
//                break;
            case COMM_GET_UUID_BUFFER:
                waitPublish = true;
                publishSuccess = false;
                getDetonatorUuidBuffer();
                break;
//            case COMM_PUT_UUID_BUFFER:
//                putDetonatorUuidBuffer();
//                break;
            case COMM_NONE:
                waitPublish = true;
                publishProgress(COMM_NONE, 1);
            case COMM_WAIT_PUBLISH:
                while (waitPublish && !isCancelled() && running) {
                    delayMs(5);
                }
                break;
            case COMM_DOWNLOAD_DATA:
                downloadNotSameData();
                break;
            case COMM_ENTER_TEST_MODE:
                serialPortUtils.uartData.dataMac = macid;
                serialPortUtils.uartData.data0 = data00;
                serialPortUtils.uartData.data1 = data10;
                sendSingleCommand(DEV_TEST_MODE, _MODBUS_WRITE);
                delayMs(1000);
                break;
            case COMM_GET_TEST_STATUS:
                publishSuccess = false;
                breaking = false;
                int count = 0;
                int max;
                while (!isCancelled() && running) {
                    serialPortUtils.uartData.dataMac = 0;
                    sendSingleCommand(DEV_TEST_MODE, _MODBUS_READ);
                    if (!isCancelled() && running && !breaking) {
                        max = data1;
                        while (count < max && !isCancelled() && running && !breaking) {
                            serialPortUtils.uartData.dataMac = 1;
                            serialPortUtils.uartData.data0 = count;
                            sendSingleCommand(DEV_TEST_MODE, _MODBUS_READ);
                            if (!isCancelled() && running && !breaking) {
                                waitPublish = true;
                                publishProgress(step, 1, (int) id, devStatus, detonatorStatus, data0, data1, data2);
                                while (waitPublish && !isCancelled() && running && !breaking) {
                                    delayMs(5);
                                }
                                count++;
                            }
                            delayMs(100);
                        }

                    }
                    if (breaking && running) {
                        serialPortUtils.uartData.dataMac = 2;
                        sendSingleCommand(DEV_TEST_MODE, _MODBUS_READ);
                        if (!isCancelled() && running) {
                            delayMs(2000);
                        }
                        breaking = false;
                    }
                }
                delayMs(200);
                break;
            case COMM_TEST_CONTINUE:
                serialPortUtils.uartData.dataMac = 2;
                sendSingleCommand(DEV_TEST_MODE, _MODBUS_READ);
                if (!isCancelled() && running) {
                    delayMs(2000);
                }
                break;
            case COMM_STOP_TEST_MODE:
                serialPortUtils.uartData.dataMac = 3;
                sendSingleCommand(DEV_TEST_MODE, _MODBUS_READ);
                delayMs(1000);
                break;
            case COMM_WRITE_DEV_ID:
                waitPublish = true;
                serialPortUtils.uartData.dataMac = macid;
                serialPortUtils.uartData.data0 = data00;
                serialPortUtils.uartData.data1 = data10;
                sendSingleCommand(DEV_RW_ID, _MODBUS_WRITE);
                break;
            case COMM_WRITE_DEV_VER:
                waitPublish = true;
                serialPortUtils.uartData.dataMac = macid;
                serialPortUtils.uartData.data0 = data00;
                serialPortUtils.uartData.data1 = data10;
                sendSingleCommand(DEV_RW_VER, _MODBUS_WRITE);
                break;
            case COMM_READ_DEV_ID:
                waitPublish = true;
                sendSingleCommand(DEV_RW_ID, _MODBUS_READ);
                break;
            case COMM_READ_DEV_VER:
                waitPublish = true;
                sendSingleCommand(DEV_RW_VER, _MODBUS_READ);
                break;
            case COMM_ALL_DISCHARGE:
                sendSingleCommand(DEV_ALL_DISCHARGE, _MODBUS_WRITE);
                break;
            case COMM_CALC_VOLTAGE:
                sendSingleCommand(DEV_CALC_VOLTAGE, _MODBUS_WRITE);
                break;

            case COMM_SET_TEST_DELAY:
                waitPublish = true;
                serialPortUtils.uartData.data0 = data00;
                sendSingleCommand(DEV_SET_TEST_DELAY, _MODBUS_WRITE);
                break;
            case COMM_SET_CLK:
                waitPublish = true;
                serialPortUtils.uartData.data0 = data00;
                sendSingleCommand(DEV_SET_CLK, _MODBUS_WRITE);
                break;
            case COMM_READ_ALL_BRIDGE:
                readAllBridge();
                break;

            default:
                break;
        }
    }


    public void setMac(int m) {
        mac[0] = (byte) (m & 0xff);
        mac[1] = (byte) ((m >> 8) & 0xff);
        mac[2] = (byte) ((m >> 16) & 0xff);
        mac[3] = (byte) ((m >> 24) & 0xff);
        macid = m;
        System.arraycopy(mac, 0, serialPortUtils.uartData.m_mac, 0, 4);
    }

    public void setEEAddress(byte addr) {
        serialPortUtils.uartData.eeAddress = addr;
    }

    public void setEEData(int d0, int d1) {
        serialPortUtils.uartData.eeData0 = d0;
        serialPortUtils.uartData.eeData1 = d1;
        eeData0 = d0;
        eeData1 = d1;
    }

    public void setUuid(String uuidStr) {
        stringUuid = uuidStr;
    }


    public void setPassword(int password) {
        this.password = password;
    }

    public void setTotalCount(int d0, int d1) {
        serialPortUtils.uartData.total = d0;
        serialPortUtils.uartData.count = d1;
        total = d0;
        count = d1;
    }

    public void setDetonatorValues(int detonatorArea, int detonatorHole, int detonatorDelay, int detonatorTime) {
        this.detonatorArea = detonatorArea;
        this.detonatorHole = detonatorHole;
        this.detonatorDelay = detonatorDelay;
        this.detonatorTime = detonatorTime;
    }

    public void setFileData(byte[] fileData, int fileLen) {
        this.fileLen = fileLen;
        this.fileData = new byte[fileLen];
        System.arraycopy(fileData, 0, this.fileData, 0, fileLen);
    }

    private boolean getDetonatorUuid() {
        int date, year;
        byte[] b = new byte[8];
        serialPortUtils.uartData.eeAddress = EE_UUID;
        sendReadCommand(DEV_READ_EE);
        if (!running) return false;
        b[0] = (byte) (data1 & 0x0ff);
        b[1] = (byte) ((data1 >> 8) & 0x0ff);
        b[2] = (byte) ((data1 >> 16) & 0x0ff);
        b[3] = (byte) ((data1 >> 24) & 0x0ff);
        serialPortUtils.uartData.eeAddress = EE_UUID + 4;
        sendReadCommand(DEV_READ_EE);
        if (!running) return false;
        b[4] = (byte) (data1 & 0x0ff);
        b[5] = (byte) ((data1 >> 8) & 0x0ff);
        b[6] = (byte) ((data1 >> 16) & 0x0ff);
        b[7] = (byte) ((data1 >> 24) & 0x0ff);

        date = (b[4] & 0x00ff);
        date = (date << 8) & 0x00ffff;
        date += (b[5] & 0x0ff);
        year = date / 16;
        year = year / 40;
        if (year == 10) {
            date = date + 40 * 11 * 16;
            b[4] = (byte) ((date >> 8) & 0x00ff);
            b[5] = (byte) (date & 0x00ff);
        }
        if (year == 1) {
            date = date + 40 * 19 * 16;
            b[4] = (byte) ((date >> 8) & 0x00ff);
            b[5] = (byte) (date & 0x00ff);
            if (b[2] == '2')
                b[2] = 'D';
            if (b[2] == '1')
                b[2] = 'C';
            if (b[2] == '0')
                b[2] = 'A';

        }

        stringUuid = FileFunc.convertUuidString(b, 0);
        return true;
    }

    public final static int fbh_factory0 = 0;
    public final static int fbh_factory1 = 1;
    public final static int fbh_year_low = 2;
    public final static int fbh_month_high = 3;
    public final static int fbh_month_low = 4;
    public final static int fbh_day_high = 5;
    public final static int fbh_day_low = 6;
    public final static int fbh_flag = 7;
    public final static int fbh_num0 = 8;
    public final static int fbh_num1 = 9;
    public final static int fbh_num2 = 10;
    public final static int fbh_num3 = 11;
    public final static int fbh_num4 = 12;
    public final static char[] monthChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'C', 'D'};

    private boolean writeDetonatorUuid() {
        byte[] u = stringUuid.getBytes();
        byte[] uuidHex = new byte[8];
        int date;
        date = 2 * 10;
        date += (u[fbh_year_low] - 0x30) & 0x0f;
        date *= 40;
        date += ((u[fbh_day_high] - 0x30) & 0x0f) * 10 + ((u[fbh_day_low] - 0x30) & 0x0f);
        date = date << 4;
        date += (u[fbh_num0] - 0x30) & 0x0f;
        uuidHex[0] = u[fbh_factory0];
        uuidHex[1] = u[fbh_factory1];
        uuidHex[3] = u[fbh_flag];
        uuidHex[4] = (byte) ((date >> 8) & 0x0ff);
        uuidHex[5] = (byte) (date & 0x0ff);
        uuidHex[6] = (byte) ((u[fbh_num1] - 0x30) << 4);
        uuidHex[6] += (byte) (u[fbh_num2] - 0x30) & 0x0f;
        uuidHex[7] = (byte) ((u[fbh_num3] - 0x30) << 4);
        uuidHex[7] += (byte) (u[fbh_num4] - 0x30) & 0x0f;
        date = ((u[fbh_month_high] - 0x30) & 0x00ff) * 10;
        date += ((u[fbh_month_low] - 0x30) & 0x00ff);
        uuidHex[2] = (byte) monthChar[date];
        serialPortUtils.uartData.eeData0 = getShortFromBytes(uuidHex, 0);
        serialPortUtils.uartData.eeData1 = getShortFromBytes(uuidHex, 2);
        serialPortUtils.uartData.eeAddress = EE_UUID;
        sendReadCommand(DEV_WRITE_EE);
        if (!running) return false;
        serialPortUtils.uartData.eeData0 = getShortFromBytes(uuidHex, 4);
        serialPortUtils.uartData.eeData1 = getShortFromBytes(uuidHex, 6);
        serialPortUtils.uartData.eeAddress = EE_UUID + 4;
        sendReadCommand(DEV_WRITE_EE);
        return running;
    }


    public void getDetonatorStatus() {
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);
                if (data[MODBUS_MODE] == _MODBUS_WRITE) {
                    convertBattery(data);
                }
                if (data[MODBUS_MODE] == _MODBUS_READ) {
                    devStatus = data[MODBUS_STATUS];
                    detonatorStatus = data[MODBUS_DET_STATUS];
                }
                ack = 1;
            }
        });
        int i;
        int errCnt = 0;
        System.arraycopy(mac, 0, serialPortUtils.uartData.m_mac, 0, 4);
        while (!isCancelled()) {
            ack = 0;
            sendCommand(DEV_SCAN_MAC, _MODBUS_WRITE, (byte) 1);
            i = waitDeviceAck();
            if (i == -1) {
                publishProgress(step, -1);
                continue;
            }
            devStatus = _STATUS_WORKING;
            while ((devStatus == _STATUS_WORKING) && !isCancelled()) {
                ack = 0;
                delayMs(10);
                sendCommand(DEV_SCAN_MAC, _MODBUS_READ, (byte) 1);
                i = waitDeviceAck();
                if (i == -1) {
                    publishProgress(step, -1);
                    break;
                }
            }
            if (devStatus == _STATUS_SUCCESS) {
                publishProgress(step, 1, macid, detonatorStatus);
                return;
            }
            if (devStatus == _STATUS_FAIL) {
                publishProgress(step, -1, macid, 0);
                running = false;
                return;
            }

            errCnt++;
            if (errCnt > COMM_RETRY) {
                publishProgress(step, -1);
                running = false;
                return;
            }
        }
    }


    public void getBattery(int t) {
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);
                devStatus = data[MODBUS_STATUS];
                convertBattery(data);
                ack = 1;
            }
        });
        int i;
        while (!isCancelled()) {
            ack = 0;
            if (t == 0)
                sendCommand(DEV_CURRENT, _MODBUS_WRITE, (byte) 1);
            else
                sendCommand(DEV_BATT, _MODBUS_WRITE, (byte) 1);
            i = waitDeviceAck();
            if (i == 1) {
                if (publishSuccess)
                    publishProgress(step, 1);
                return;
            }
            if (i == -1) {
                if (publishSuccess)
                    publishProgress(step, -1);
                return;
            }
        }
    }

    public void putDetonatorID(int t) {
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);
                devStatus = data[MODBUS_STATUS];
                ack = 1;
            }
        });
        int i, time, hole, area;
        serialPortUtils.uartData.count = 0;
        serialPortUtils.uartData.total = dataViewModel.detonatorDatas.size();
        if (serialPortUtils.uartData.total == 0)
            return;
        int errCnt = 0;
        while (!isCancelled()) {
            delayMs(1);
            ack = 0;
            serialPortUtils.uartData.macid = dataViewModel.detonatorDatas.get(serialPortUtils.uartData.count).getId();
            serialPortUtils.uartData.blasterTimer = dataViewModel.detonatorDatas.get(serialPortUtils.uartData.count).getBlasterTime();
            serialPortUtils.uartData.area = dataViewModel.detonatorDatas.get(serialPortUtils.uartData.count).getRowNum();
            serialPortUtils.uartData.hole = dataViewModel.detonatorDatas.get(serialPortUtils.uartData.count).getHoleNum();
            if (t == 0)
                sendCommand(DEV_PUT_ID, _MODBUS_WRITE, (byte) 1);
            else
                sendCommand(DEV_PUT_AREA, _MODBUS_WRITE, (byte) 1);
            i = waitDeviceAck();
            if (i == 1) {
                serialPortUtils.uartData.count++;
                publishProgress(step, 1, serialPortUtils.uartData.count, serialPortUtils.uartData.total);
                errCnt = 0;
                if (serialPortUtils.uartData.count >= serialPortUtils.uartData.total)
                    return;
                else
                    continue;
            }
            if (i == -1) {
                errCnt++;
                if (errCnt > COMM_RETRY) {
                    publishProgress(step, -1);
                    running = false;
                    return;
                }
            }
        }
    }


    public void getDetonatorID() {
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);

                devStatus = data[MODBUS_STATUS];
                detonatorStatus = data[MODBUS_DET_STATUS];
                data0 = FileFunc.GetModbusData32(data, MODBUS_MAC0);
                data1 = FileFunc.GetModbusData32(data, MODBUS_DATA00);
                data2 = FileFunc.GetModbusData32(data, MODBUS_DATA10);
                ack = 1;
            }
        });
        int i;
        serialPortUtils.uartData.count = 0;
        serialPortUtils.uartData.total = total;
        if (total == 0) {
            publishProgress(step, 1, 0, 0);
            return;
        }
        int errCnt = 0;
        while (!isCancelled()) {
            ack = 0;
            sendCommand(DEV_GET_ID, _MODBUS_READ, (byte) 1);
            i = waitDeviceAck();
            if (i == 1) {
                dataViewModel.idBuffer[serialPortUtils.uartData.count] = data0;
                dataViewModel.timerBuffer[serialPortUtils.uartData.count] = data1;
                dataViewModel.areaBuffer[serialPortUtils.uartData.count] = data2;
                dataViewModel.statusBuffer[serialPortUtils.uartData.count] = detonatorStatus;
                serialPortUtils.uartData.count++;
                publishProgress(step, 1, serialPortUtils.uartData.count, serialPortUtils.uartData.total);
                errCnt = 0;
                if (serialPortUtils.uartData.count >= serialPortUtils.uartData.total)
                    return;
                else
                    continue;
            }
            if (i == -1) {
                errCnt++;
                if (errCnt > COMM_RETRY) {
                    publishProgress(step, -1);
                    running = false;
                    return;
                }
            }
            delayMs(5);
        }
    }

    public void putDetonatorUuidBuffer() {
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);
                devStatus = data[MODBUS_STATUS];
                ack = 1;
            }
        });
        int i;
        serialPortUtils.uartData.count = 0;
        serialPortUtils.uartData.total = dataViewModel.detonatorDatas.size();
        int errCnt = 0;
        while (!isCancelled()) {
            delayMs(1);
            ack = 0;
            i = serialPortUtils.uartData.count * 16;
            dataViewModel.detonatorDatas.get(serialPortUtils.uartData.count).convertDataToBuffer(dataViewModel.uuidBuffer, i);
            serialPortUtils.uartData.dataMac = getIntFromBytes(dataViewModel.uuidBuffer, i);
            serialPortUtils.uartData.data0 = getIntFromBytes(dataViewModel.uuidBuffer, i + 4);
            serialPortUtils.uartData.data1 = getIntFromBytes(dataViewModel.uuidBuffer, i + 8);
            sendCommand(DEV_PUT_UUID, _MODBUS_WRITE, (byte) 1);
            i = waitDeviceAck();
            if (i == 1) {
                serialPortUtils.uartData.count++;
                publishProgress(step, 1, serialPortUtils.uartData.count, serialPortUtils.uartData.total);
                errCnt = 0;
                if (serialPortUtils.uartData.count >= serialPortUtils.uartData.total)
                    return;
                else
                    continue;
            }
            if (i == -1) {
                errCnt++;
                if (errCnt > COMM_RETRY) {
                    publishProgress(step, -1);
                    running = false;
                    return;
                }
            }
        }
    }

    public void getDetonatorUuidBuffer() {
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);
                devStatus = data[MODBUS_STATUS];
                detonatorStatus = data[MODBUS_DET_STATUS];
                data0 = FileFunc.GetModbusData32(data, MODBUS_MAC0);
                data1 = FileFunc.GetModbusData32(data, MODBUS_DATA00);
                data2 = FileFunc.GetModbusData32(data, MODBUS_DATA10);
                System.arraycopy(data, MODBUS_DATA00, dataViewModel.uuidBuffer, serialPortUtils.uartData.count * 8, 8);
                ack = 1;
            }
        });
        int i;
        serialPortUtils.uartData.count = 0;
        serialPortUtils.uartData.total = total * 2;
        if (total == 0) {
            publishProgress(step, 1, 0, 0);
            return;
        }
        int errCnt = 0;
        while (!isCancelled()) {
            ack = 0;
            sendCommand(DEV_GET_UUID, _MODBUS_READ, (byte) 1);
            i = waitDeviceAck();
            if (i == 1) {
                serialPortUtils.uartData.count++;
                publishProgress(step, 1, serialPortUtils.uartData.count, serialPortUtils.uartData.total);
                errCnt = 0;
                if (serialPortUtils.uartData.count >= serialPortUtils.uartData.total)
                    return;
                else
                    continue;
            }
            if (i == -1) {
                errCnt++;
                if (errCnt > COMM_RETRY) {
                    publishProgress(step, -1);
                    running = false;
                    return;
                }
            }
            delayMs(5);
        }
    }


    public void sendSingleCommand(byte cmd, byte rw) {

        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);
                devStatus = data[MODBUS_STATUS];
                devStatus = devStatus & 0x000000ff;
                detonatorStatus = data[MODBUS_DET_STATUS];
                detonatorStatus &= 0x000000ff;
                id = data[0] & 0x00ff;
                data0 = FileFunc.GetModbusData32(data, MODBUS_MAC0);
                data1 = FileFunc.GetModbusData32(data, MODBUS_DATA00);
                data2 = FileFunc.GetModbusData32(data, MODBUS_DATA10);
                ack = 1;
            }
        });
        int i;
        int errCnt = 0;
        while (!isCancelled() && running) {
            ack = 0;
            sendCommand(cmd, rw, (byte) 1);
            i = waitDeviceAck();
            if (i == 1) {
                if (publishSuccess)
                    publishProgress(step, 1, data0, data1, data2, detonatorStatus, devStatus);
                return;
            }
            errCnt++;
            if (errCnt > COMM_RETRY) {
                publishProgress(step, -1);
                running = false;
                return;
            }
            delayMs(100);
        }
    }

    public int sendReadCommand(byte cmd, int... d) {
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {


            @Override
            public void ReceiveNoAck(boolean sendStop) {
                notSendErr = sendStop;
                ack = -1;
            }

            @Override
            public void ReceiveAck(byte[] data) {
                checkOverCurrent(data);
                devStatus = data[MODBUS_STATUS];
                devStatus = devStatus & 0x000000ff;
                detonatorStatus = data[MODBUS_DET_STATUS];
                detonatorStatus &= 0x000000ff;
                id = data[0];
                if (data[MODBUS_MODE] == _MODBUS_READ) {
                    data0 = FileFunc.GetModbusData32(data, MODBUS_MAC0);
                    data1 = FileFunc.GetModbusData32(data, MODBUS_DATA00);
                    data2 = FileFunc.GetModbusData32(data, MODBUS_DATA10);
//                    Log.e("uart", "update data  " + FileFunc.printHexBinary(data));
//                    Log.e("uart", "data0,1,2  " + String.format("%04x %04x %04x", data0, data1, data2));
                }
                ack = 1;
            }
        });
        int i;
        int delay = 100;
        for (int j : d) {
            delay = j;
        }
        int errCnt = 0;
        while (!isCancelled() && running) {
            ack = 0;
            sendCommand(cmd, _MODBUS_WRITE, (byte) 1);
            i = waitDeviceAck();
            if (i == -1) {
                publishProgress(step, -1);
                continue;
            } else {
                devStatus = _STATUS_WORKING;
                while ((devStatus == _STATUS_WORKING) && !isCancelled()) {
                    ack = 0;
                    delayMs(delay);
                    if (running)
                        sendCommand(cmd, _MODBUS_READ, (byte) 1);
                    i = waitDeviceAck();
                    if (i == -1) {
                        publishProgress(step, -1);
                        timeOut = 0;
                        break;
                    }
                    delay = 100;
                    if (System.currentTimeMillis() > timeOut && cmd == DEV_FAST_SCAN) {
                        publishProgress(step, -1);
                        timeOut = 0;
                        return -1;
                    }
                }
                if (devStatus == _STATUS_SUCCESS) {
                    if (publishSuccess)
                        publishProgress(step, 1, data0, data1, data2, detonatorStatus, devStatus);
                    return 1;
                }
            }
            errCnt++;
            if (errCnt > COMM_RETRY) {
                publishProgress(step, -1);
                running = false;
                return -1;
            }
            delayMs(30);
        }
        return -1;
    }


    public int delayMs(int ms) {

        long end;
//        int s=0;
        end = System.currentTimeMillis() + ms;
        try {
            while (!isCancelled() && running) {
                if (end < System.currentTimeMillis())
                    break;
//
//                try {
//                    Thread.sleep(1);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return 0;
//                }

            }
            if (isCancelled())
                return 0;
            else
                return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int waitDeviceAck() {
        while (!isCancelled() && running) {
            if (ack != 0) {
                if (ack == -1) {
                    if (!isCancelled())
                        if ((!notSendErr) && running) {
                            dataViewModel.volt.postValue(-1);
                            Log.e("COMM ERR", String.valueOf(cmdType) + ' ' + dataViewModel.commErr);
                        }
                } else {
                    dataViewModel.commErr = 0;
                }
                return ack;
            }
        }
        return -1;
    }

    public void convertBattery(byte[] data) {
        int tmp;
        int v;
        float[] vData = new float[3];
        if (data[MODBUS_MODE] != _MODBUS_WRITE)
            return;
        if ((data[MODBUS_STATUS] == _STATUS_OVERCURRENT))
            return;

        Message message = new Message();
        tmp = data[MODBUS_MAC3];
        v = tmp & 0xff;
        tmp = data[MODBUS_MAC2];
        v = (v << 8) + (tmp & 0xff);
        tmp = data[MODBUS_MAC1];
        v = (v << 8) + (tmp & 0xff);
        tmp = data[MODBUS_MAC0];
        v = (v << 8) + (tmp & 0xff);
        float volt = v;
        if (volt >= 30000.0f) {
            dataViewModel.batt.postValue(1000.0f);
            dataViewModel.battStatus = 3;
            vData[0] = 1000.0f;
        } else if (volt > 20000.0f) {
            dataViewModel.batt.postValue(500.0f);
            dataViewModel.battStatus = 2;
            vData[0] = 500.0f;
        } else if (volt > 10000.0f) {
            dataViewModel.batt.postValue(calcBattPercent((volt - 10000.0f) / 100.0f) + 200.0f);
            dataViewModel.battStatus = 1;
            vData[0] = calcBattPercent((volt - 10000.0f) / 100.0f) + 200.0f;
        } else {
            dataViewModel.batt.postValue(calcBattPercent(volt / 100.0f));
            dataViewModel.battStatus = 0;
            vData[0] = calcBattPercent(volt / 100.0f);
        }
        tmp = data[MODBUS_DATA03];
        v = tmp & 0xff;
        tmp = data[MODBUS_DATA02];
        v = (v << 8) + (tmp & 0xff);
        tmp = data[MODBUS_DATA01];
        v = (v << 8) + (tmp & 0xff);
        tmp = data[MODBUS_DATA00];
        v = (v << 8) + (tmp & 0xff);
        volt = v;
        volt /= 10;
        vData[1] = volt;
        tmp = data[MODBUS_DATA13];
        v = tmp & 0xff;
        tmp = data[MODBUS_DATA12];
        v = (v << 8) + (tmp & 0xff);
        tmp = data[MODBUS_DATA11];
        v = (v << 8) + (tmp & 0xff);
        tmp = data[MODBUS_DATA10];
        v = (v << 8) + (tmp & 0xff);
        float current = v;
        current /= 1000;
        vData[2] = current;
        dataViewModel.vData = vData;
        dataViewModel.volt.postValue(0);
//        dataViewModel.current.postValue(current);
//        dataViewModel.batt.postValue(calcBattPercent(volt / 100.0f));
//        dataViewModel.battStatus = 0;
//        message.what = 0;
//        message.obj = vData;
//        if (handlerVolt != null)
//            handlerVolt.sendMessage(message);
    }

    public void checkOverCurrent(byte[] data) {
        if ((data[MODBUS_STATUS] == _STATUS_OVERCURRENT)) {
            dataViewModel.overCurrent.postValue(1);
            running = false;
        }
        if ((data[MODBUS_STATUS] == _STATUS_OFFLINE)) {
            dataViewModel.offline.postValue(-1000);
            dataViewModel.countDown = -1000;
            running = false;
        }
    }

    public float calcBattPercent(float v) {
        float percent;
        int i;
        percent = 100f;
        for (i = 0; i < 21; i++) {
            if (v > battPercentTab[i])
                break;
            percent = percent - 5.0f;
        }
        if (percent >= 100)
            return 100f;
        if (percent <= 0)
            return 0f;
        percent = percent + Math.round((v - battPercentTab[i]) / (battPercentTab[i - 1] - battPercentTab[i]) * 5f);
        percent = Math.round(percent * 10.0) / 10;
        return percent;

    }

    private void deviceUpdate() {
        int filePtr = 0;
        int end = 0;
        int flag = 0;
        fileData[0x600] = (byte) 0xa5;
        fileData[0x601] = (byte) 0x5a;
        while (running) {
            if (filePtr == fileLen) {
                filePtr = 0x600;
                flag = 1;
            }
            if ((filePtr == 0x800) && (end == 1))
                break;
            if (end == 0 && filePtr == 0x600) {
                filePtr = 0x800;
                end = 1;
            }
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_ID] = 1;
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_MODE] = _MODBUS_WRITE;
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_CMD] = DEV_UPDATE;
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_MAC0] = (byte) ((filePtr + 0x2000) & 0xff);
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_MAC1] = (byte) (((filePtr + 0x2000) >> 8) & 0xff);
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_MAC2] = (byte) (((filePtr + 0x2000) >> 16) & 0xff);
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_MAC3] = (byte) (((filePtr + 0x2000) >> 24) & 0xff);
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA00] = fileData[filePtr];
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA01] = fileData[filePtr + 1];
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA02] = fileData[filePtr + 2];
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA03] = fileData[filePtr + 3];
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA10] = fileData[filePtr + 4];
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA11] = fileData[filePtr + 5];
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA12] = fileData[filePtr + 6];
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_DATA13] = fileData[filePtr + 7];
            int crc = UartData.CRC16(serialPortUtils.uartData.m_uartCmdBuffer, 17);
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_SUML] = (byte) (crc >> 8);
            serialPortUtils.uartData.m_uartCmdBuffer[MODBUS_SUMH] = (byte) (crc & 0x00ff);

            sendSingleCommand(DEV_UPDATE, _MODBUS_WRITE);
            filePtr += 8;
            waitPublish = true;
            if (running)
                onProgressUpdate(COMM_UPDATE, 1, filePtr, flag);
            while (waitPublish && !isCancelled() && running) {
                delayMs(5);
            }
            //  Log.e("update", String.valueOf(filePtr));
        }
        if (running)
            onProgressUpdate(COMM_UPDATE, 1, filePtr, 2);
        else
            onProgressUpdate(COMM_UPDATE, -1, filePtr, end);

    }

    public void downloadNotSameData() {
        int n, p = 0, success = 0, fail = 0;
        publishSuccess = false;
        n = getErrorCount(dataViewModel.detonatorDatas);
        if (n == 0) {
            publishProgress(step, 1, 0, 0, 0, 0);
            return;
        }
        for (DetonatorData d : dataViewModel.detonatorDatas) {
            int color = d.getColor();
            switch (color) {
                case 0x80:
                case 0x00:
                case 0x1f:
                case 0x40:
                    break;
                default:
                    setMac((int) d.getId());
                    sendReadCommand(DEV_SCAN_MAC);
                    if (running & !isCancelled()) {
                        serialPortUtils.uartData.eeData1 = d.getBlasterTime() + BLASTER_TIMER_DELAY;
                        serialPortUtils.uartData.eeData0 = (d.getRowNum() * 1000) + d.getHoleNum();
                        serialPortUtils.uartData.eeAddress = EE_AREA;
                        sendReadCommand(DEV_WRITE_EE);
                    }
                    p++;
                    if (running) {
                        success++;
                        d.setColor(0x80);
                    } else {
                        running = true;
                        fail++;
                    }
                    publishProgress(step, 1, n, p, success, fail);
                    break;
            }
            if (isCancelled())
                break;
        }

    }

    public void readAllBridge() {
        int n, p = 0, success = 0, fail = 0;
        publishSuccess = false;
        n = dataViewModel.detonatorDatas.size();
        int i = 0;
        for (DetonatorData d : dataViewModel.detonatorDatas) {
            setMac((int) d.getId());
            sendReadCommand(DEV_SCAN_MAC);
            if (running & !isCancelled()) {
                if (sendReadCommand(DEV_TEST_BRIDGE, 1500) == 1) {
                    float r;
                    int d0, d1, u;
                    d0 = data1 & 0x00ffff;
                    d1 = (data1 >>> 16);
                    u = d0 * 100 / 280;
                    //if (u > 200 || u < 60)
                    dataViewModel.detonatorDatas.get(i).setCap(u);
                    r = d1;
                    r = r / d0 * 100.0f;
                    r = Math.round(r * 10) / 10.0f;
                    dataViewModel.detonatorDatas.get(i).setBridge(r);
                } else {
                    dataViewModel.detonatorDatas.get(i).setCap(0);
                    dataViewModel.detonatorDatas.get(i).setBridge(0);
                }
            }
            p++;
            if (running) {
                success++;
            } else {
                running = true;
                fail++;
            }
            publishProgress(step, 1, n, p, success, fail);
            if (isCancelled())
                break;
            i++;
        }

    }


    public void sendCommand(final byte cmd, byte rw, byte id) {
        if (cmd != DEV_UPDATE)
            serialPortUtils.uartData.initData(cmd, rw, id);
        if (serialPortUtils.bleOk) {
            byte[] tmp;
            int length = 0;
            long time;
            serialPortUtils.bleReceiveByte = 0;
            serialPortUtils.bleManager.onBleListener = serialPortUtils.onBleListener;
            serialPortUtils.bleManager.SendData(serialPortUtils.uartData.m_uartCmdBuffer);
            if ((cmd == DEV_UPDATE) || (cmd == DEV_TEST_MODE))
                time = System.currentTimeMillis() + 5000;
            else
                time = System.currentTimeMillis() + 500;
            while (!isCancelled() && running) {
                if (serialPortUtils.bleReceiveByte == 19) {
                    System.arraycopy(serialPortUtils.bleReceiveBuffer, 0, serialPortUtils.mReceiveBuffer, 0, 19);
                    break;
                }
                if (serialPortUtils.bleReceiveByte > 19) {
                    break;
                }
                delayMs(2);
                if (time < System.currentTimeMillis()) {
                    serialPortUtils.bleReceiveByte = 0;
                    break;
                }
            }
            if ((serialPortUtils.bleReceiveByte == 19) && UartData.CheckCrc(serialPortUtils.mReceiveBuffer, serialPortUtils.bleReceiveByte) && serialPortUtils.mReceiveBuffer[MODBUS_CMD] == cmd) {
//                   Log.e("uart", "rece data "+ FileFunc.printHexBinary(mReceiveBuffer));
                serialPortUtils.onDataReceiveListener.ReceiveAck(serialPortUtils.mReceiveBuffer);
            } else {
                serialPortUtils.onDataReceiveListener.ReceiveNoAck(isCancelled());
            }

        } else if (serialPortUtils.serialPortOk) {
            byte[] tmp;
            int length = 0;
            long time;
            try {
                while (serialPortUtils.inputStream.available() != 0) {
                    length = serialPortUtils.inputStream.available();
                    if (length != 0) {
                        serialPortUtils.inputStream.skip(length);
//                            tmp = new byte[length];
//                            inputStream.read(tmp);
                    }
                }
//                    Log.e("uart", "send cmd " + String.valueOf(uartData.m_uartCmdBuffer[MODBUS_CMD]));
//                    Log.e("uart", "send data "+ FileFunc.printHexBinary(uartData.m_uartCmdBuffer));
                serialPortUtils.outputStream.write(serialPortUtils.uartData.m_uartCmdBuffer, 0, serialPortUtils.uartData.length);
                serialPortUtils.outputStream.flush();
                if (cmd != DEV_UPDATE)
                    time = System.currentTimeMillis() + 500;
                else
                    time = System.currentTimeMillis() + 2000;
                if (cmd == DEV_COUNT_DOWN)
                    time = System.currentTimeMillis() + 200;
                while (!isCancelled() && running) {
                    length = serialPortUtils.inputStream.available();
                    if (length == 19) {
                        serialPortUtils.inputStream.read(serialPortUtils.mReceiveBuffer);
                        break;
                    }
                    if (length > 19) {
//                        serialPortUtils.inputStream.skip(length);
                        tmp = new byte[length];
                        length = 0;
                        serialPortUtils.inputStream.read(tmp);
                        break;
                    }
                    delayMs(2);
                    if (time < System.currentTimeMillis()) {
                        length = 0;
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
//                    tcpManagerListener.ConnectSuccess(AppConstants.TCP_CONNECT_FAIL);
            }
            if (length > 19)
                length = 19;
            if ((length > 0) && UartData.CheckCrc(serialPortUtils.mReceiveBuffer, length) && serialPortUtils.mReceiveBuffer[MODBUS_CMD] == cmd) {
//                   Log.e("uart", "rece data "+ FileFunc.printHexBinary(mReceiveBuffer));
                serialPortUtils.onDataReceiveListener.ReceiveAck(serialPortUtils.mReceiveBuffer);
            } else {
                serialPortUtils.onDataReceiveListener.ReceiveNoAck(isCancelled());
            }
        }
    }
}


