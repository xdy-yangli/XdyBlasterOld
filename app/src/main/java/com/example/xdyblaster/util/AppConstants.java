package com.example.xdyblaster.util;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_NUMPAD_DOT;
import static android.view.KeyEvent.KEYCODE_PAGE_DOWN;
import static android.view.KeyEvent.KEYCODE_PAGE_UP;

public class AppConstants {
    public static final String ACTION_SCAN_INIT = "com.rfid.SCAN_INIT";
    public static final String ACTION_CLOSE_SCAN = "com.rfid.CLOSE_SCAN";
    public static final String ID_FACTORY="F53AC666888";
    public static final String ID_EXIT_FACTORY="F53AC888666";

    public static int BLASTER_TIMER_DELAY = 2100;
    public static final int TCP_NULL = 1;
    public static final int TCP_CONNECT_SUCCESS = 2;
    public static final int TCP_CONNECT_FAIL = 3;
    public static final int TCP_DATA_IN = 4;
    public static final int TCP_SEND_FAIL = 5;


    public static final byte DEV_NULL = 0;
    public static final byte DEV_IDLE = 1;
    public static final byte DEV_SCAN = 2;
    public static final byte DEV_SCAN_MAC = 3;
    public static final byte DEV_CHARGE = 4;
    public static final byte DEV_DISCHARGE = 5;
    public static final byte DEV_TEST_BRIDGE = 6;
    public static final byte DEV_BOOMB_ENABLE = 7;
    public static final byte DEV_SET_TIMER = 8;
    public static final byte DEV_READ_FREQ = 9;
    public static final byte DEV_VOLTAGE = 10;
    public static final byte DEV_CURRENT = 11;
    public static final byte DEV_TEST_MAIN_FREQ = 12;
    public static final byte DEV_TEST_SUB_FREQ = 13;
    public static final byte DEV_COUNT_DOWN = 14;
    public static final byte DEV_WRITE_EE = 15;
    public static final byte DEV_READ_EE = 16;
    public static final byte DEV_TURN_ON_POWER = 17;
    public static final byte DEV_TURN_OFF_POWER = 18;
    public static final byte DEV_RESET = 19;
    public static final byte DEV_UPDATE = 20;
    public static final byte DEV_BLIND_SCAN = 21;
    public static final byte DEV_GET_ID = 22;
    public static final byte DEV_TEST_MODE = 23;
    public static final byte DEV_FAST_SCAN = 24;
    public static final byte DEV_SINGLE_CURRENT = 25;
    public static final byte DEV_CLIBRATE_CURRENT = 26;
    public static final byte DEV_PUT_ID = 27;
    public static final byte DEV_DETONATE = 28;
    public static final byte DEV_DETONATE_STEP = 29;
    public static final byte DEV_FAST_TEST = 30;
    public static final byte DEV_CHECK_NET = 31;
    public static final byte DEV_BATT = 32;
    public static final byte DEV_DET_RESET = 33;
    public static final byte DEV_OFFLINE_SCAN = 34;
    public static final byte DEV_PUT_UUID = 35;
    public static final byte DEV_GET_UUID = 36;
    // public static final byte DEV_GET_ALL_DATA = 37;
    public static final byte DEV_RW_ID = 38;
    public static final byte DEV_RW_VER = 39;
    public static final byte DEV_ALL_DISCHARGE = 40;
    public static final byte DEV_CALC_VOLTAGE = 41;
    public static final byte DEV_SET_TEST_DELAY = 42;
    public static final byte DEV_PUT_AREA = 43;
    public static final byte DEV_SET_CLK = 44;
    public static final byte DEV_POWER_9V = 100;
    public static final byte DEV_POWER_12V = 101;
    public static final byte DEV_POWER_24V = 102;

    public static final int MODBUS_ID = 0;
    public static final int MODBUS_MODE = 1;
    public static final int MODBUS_CMD = 2;
    public static final int MODBUS_MAC0 = 3;
    public static final int MODBUS_MAC1 = 4;
    public static final int MODBUS_MAC2 = 5;
    public static final int MODBUS_MAC3 = 6;
    public static final int MODBUS_STATUS = 7;
    public static final int MODBUS_DET_STATUS = 8;
    public static final int MODBUS_DATA00 = 9;
    public static final int MODBUS_DATA01 = 10;
    public static final int MODBUS_DATA02 = 11;
    public static final int MODBUS_DATA03 = 12;
    public static final int MODBUS_DATA10 = 13;
    public static final int MODBUS_DATA11 = 14;
    public static final int MODBUS_DATA12 = 15;
    public static final int MODBUS_DATA13 = 16;
    public static final int MODBUS_SUML = 17;
    public static final int MODBUS_SUMH = 18;


    public static final byte _MODBUS_WRITE = 0x03;
    public static final byte _MODBUS_READ = 0x06;
    public static final byte _MODBUS_STOP = (byte) 0xc0;

    public static final byte _STATUS_STOP = 0;
    public static final byte _STATUS_WORKING = 1;
    public static final byte _STATUS_FAIL = 2;
    public static final byte _STATUS_SUCCESS = 3;
    public static final byte _STATUS_OVERCURRENT = 4;
    public static final byte _STATUS_ROMERR = 5;
    public static final byte _STATUS_OFFLINE = 6;
    public static final byte _STATUS_EXIT = (byte) 0xfe;
    public static final byte _STATUS_NONE = (byte) 0xff;

    public static final int DET_ID = 1;
    public static final int DET_CURRENT = 2;
    public static final int DET_FREQ = 3;
    public static final int DET_CAP = 4;
    public static final int DET_CAP_VOLT = 5;
    public static final int DET_ERR_STATUS = 6;
    public static final int DET_TEST_FINISH = 7;

    public static final int ERR_OFFLINE = 1;
    public static final int ERR_COMM = 2;
    public static final int ERR_BRIDGE = 3;
    public static final int ERR_CAP = 4;
    public static final int ERR_FREQ = 5;
    public static final int ERR_SET_TIMER = 6;
    public static final int ERR_START_TIMER = 7;
    public static final int ERR_CHARGE = 8;
    public static final int ERR_COUNT_DOWN = 9;
    public static final int ERR_WAIT = 10;

    public static final byte BIT_COMM = 0x01;
    public static final byte BIT_TIMER = 0x02;
    public static final byte BIT_CHARGED = 0x04;
    public static final byte BIT_BRIDGE = 0x08;
    public static final byte BIT_CAP = 0x10;
    public static final byte BIT_BOOMB = (byte) 0x80;

    public static final byte _KEY_KEYDATA = 0x01;
    public static final byte _KEY_BARCODE = 0x02;

    public static final byte EXKEY_MENU = 0;
    public static final byte EXKEY_PAGEUP = 1;
    public static final byte EXKEY_PAGEDOWN = 2;
    public static final byte EXKEY_CANCLE = 3;
    public static final byte EXKEY_CONFIRM = 4;
    public static final byte EXKEY_3 = 5;
    public static final byte EXKEY_6 = 6;
    public static final byte EXKEY_9 = 7;
    public static final byte EXKEY_POINT = 8;
    public static final byte EXKEY_RIGTH = 9;
    public static final byte EXKEY_2 = 10;
    public static final byte EXKEY_5 = 11;
    public static final byte EXKEY_8 = 12;
    public static final byte EXKEY_UP = 13;
    public static final byte EXKEY_DOWN = 14;
    public static final byte EXKEY_1 = 15;
    public static final byte EXKEY_4 = 16;
    public static final byte EXKEY_7 = 17;
    public static final byte EXKEY_0 = 18;
    public static final byte EXKEY_LEFT = 19;


    public static final byte EE_FREQ = 4;
    public static final byte EE_SUB = 8;
    public static final byte EE_COUNT_DOWN = 12;
    public static final byte EE_CAP = 16;
    public static final byte EE_BRIDGE = 18;
    public static final byte EE_UUID = 32;
    public static final byte EE_PSW = 64;
    public static final byte EE_AREA = 40;

    public static final int[] keyTab = {
            KEYCODE_BACK,
            KEYCODE_PAGE_UP,
            KEYCODE_PAGE_DOWN,
            KEYCODE_DEL,
            KEYCODE_ENTER,

            KEYCODE_3,
            KEYCODE_6,
            KEYCODE_9,
            KEYCODE_NUMPAD_DOT,
            KEYCODE_DPAD_RIGHT,

            KEYCODE_2,
            KEYCODE_5,
            KEYCODE_8,
            KEYCODE_DPAD_UP,
            KEYCODE_DPAD_DOWN,

            KEYCODE_1,
            KEYCODE_4,
            KEYCODE_7,
            KEYCODE_0,
            KEYCODE_DPAD_LEFT
    };

    private AppConstants() {
    }
}
