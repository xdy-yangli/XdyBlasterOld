package com.example.xdyblaster.util;


import static com.example.xdyblaster.util.AppConstants.BLASTER_TIMER_DELAY;
import static com.example.xdyblaster.util.AppConstants.DEV_CALC_VOLTAGE;
import static com.example.xdyblaster.util.AppConstants.DEV_CHECK_NET;
//import static com.example.xdyblaster.util.AppConstants.DEV_GET_ALL_DATA;
import static com.example.xdyblaster.util.AppConstants.DEV_GET_ID;
import static com.example.xdyblaster.util.AppConstants.DEV_GET_UUID;
import static com.example.xdyblaster.util.AppConstants.DEV_POWER_12V;
import static com.example.xdyblaster.util.AppConstants.DEV_POWER_24V;
import static com.example.xdyblaster.util.AppConstants.DEV_POWER_9V;
import static com.example.xdyblaster.util.AppConstants.DEV_PUT_AREA;
import static com.example.xdyblaster.util.AppConstants.DEV_PUT_ID;
import static com.example.xdyblaster.util.AppConstants.DEV_PUT_UUID;
import static com.example.xdyblaster.util.AppConstants.DEV_READ_EE;
import static com.example.xdyblaster.util.AppConstants.DEV_RW_ID;
import static com.example.xdyblaster.util.AppConstants.DEV_RW_VER;
import static com.example.xdyblaster.util.AppConstants.DEV_SET_TEST_DELAY;
import static com.example.xdyblaster.util.AppConstants.DEV_SET_TIMER;
import static com.example.xdyblaster.util.AppConstants.DEV_TEST_MODE;
import static com.example.xdyblaster.util.AppConstants.DEV_TURN_ON_POWER;
import static com.example.xdyblaster.util.AppConstants.DEV_WRITE_EE;
import static com.example.xdyblaster.util.AppConstants.MODBUS_CMD;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA00;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA01;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA02;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA03;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA10;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA11;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA12;
import static com.example.xdyblaster.util.AppConstants.MODBUS_DATA13;
import static com.example.xdyblaster.util.AppConstants.MODBUS_ID;
import static com.example.xdyblaster.util.AppConstants.MODBUS_MAC0;
import static com.example.xdyblaster.util.AppConstants.MODBUS_MAC1;
import static com.example.xdyblaster.util.AppConstants.MODBUS_MAC2;
import static com.example.xdyblaster.util.AppConstants.MODBUS_MAC3;
import static com.example.xdyblaster.util.AppConstants.MODBUS_STATUS;
import static com.example.xdyblaster.util.AppConstants.MODBUS_SUMH;
import static com.example.xdyblaster.util.AppConstants.MODBUS_SUML;
import static com.example.xdyblaster.util.AppConstants._STATUS_NONE;

public class UartData {
    public byte m_devStatus, m_uartCmd, m_uartReadWrite, m_uartId;
    public byte[] m_uartCmdBuffer = new byte[19];
    public byte[] m_mac = new byte[4];
    public byte[] m_timer = new byte[4];
    public byte[] m_freq = new byte[4];
    public byte[] uuid = new byte[16];
    public int blasterTimer, count, total, hole, area;
    public long macid;
    public int length;
    public int volt;
    public byte eeAddress;
    public int eeData0, eeData1;
    public int dataMac, data0, data1;

    public void initData(byte cmd, byte rw, byte id) {
        short crc;
        m_devStatus = _STATUS_NONE;
        m_uartCmd = cmd;
        m_uartReadWrite = rw;
        m_uartId = id;
        int tmp;
//        for (int i = 0; i < 22; i++)
//            m_uartCmdBuffer[0] = 0x00;
        m_uartCmdBuffer[MODBUS_ID] = id;
        m_uartCmdBuffer[AppConstants.MODBUS_MODE] = rw;
        m_uartCmdBuffer[MODBUS_CMD] = cmd;
        m_uartCmdBuffer[MODBUS_MAC0] = m_mac[0];
        m_uartCmdBuffer[MODBUS_MAC1] = m_mac[1];
        m_uartCmdBuffer[MODBUS_MAC2] = m_mac[2];
        m_uartCmdBuffer[MODBUS_MAC3] = m_mac[3];

        if (cmd == DEV_SET_TIMER) {
            m_uartCmdBuffer[MODBUS_DATA00] = m_timer[0];
            m_uartCmdBuffer[MODBUS_DATA01] = m_timer[1];
            m_uartCmdBuffer[MODBUS_DATA02] = m_timer[2];
            m_uartCmdBuffer[MODBUS_DATA03] = m_timer[3];

            m_uartCmdBuffer[MODBUS_DATA10] = m_freq[0];
            m_uartCmdBuffer[MODBUS_DATA11] = m_freq[1];
            m_uartCmdBuffer[MODBUS_DATA12] = m_freq[2];
            m_uartCmdBuffer[MODBUS_DATA13] = m_freq[3];
        }
        if (cmd == DEV_TURN_ON_POWER || cmd == DEV_CALC_VOLTAGE) {
            m_uartCmdBuffer[MODBUS_DATA00] = (byte) (volt & 0xff);
            m_uartCmdBuffer[MODBUS_DATA01] = (byte) ((volt >> 8) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA02] = (byte) ((volt >> 16) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA03] = (byte) ((volt >> 24) & 0xff);
        }
        if (cmd == DEV_POWER_12V) {
            int v = 1200;
            m_uartCmdBuffer[MODBUS_CMD] = DEV_TURN_ON_POWER;
            m_uartCmdBuffer[MODBUS_DATA00] = (byte) (v & 0xff);
            m_uartCmdBuffer[MODBUS_DATA01] = (byte) ((v >> 8) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA02] = (byte) ((v >> 16) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA03] = (byte) ((v >> 24) & 0xff);
        }
        if (cmd == DEV_POWER_9V) {
            int v = 900;
            m_uartCmdBuffer[MODBUS_CMD] = DEV_TURN_ON_POWER;
            m_uartCmdBuffer[MODBUS_DATA00] = (byte) (v & 0xff);
            m_uartCmdBuffer[MODBUS_DATA01] = (byte) ((v >> 8) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA02] = (byte) ((v >> 16) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA03] = (byte) ((v >> 24) & 0xff);
        }
        if (cmd == DEV_POWER_24V) {
            int v = 2400;
            m_uartCmdBuffer[MODBUS_DATA00] = (byte) (v & 0xff);
            m_uartCmdBuffer[MODBUS_DATA01] = (byte) ((v >> 8) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA02] = (byte) ((v >> 16) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA03] = (byte) ((v >> 24) & 0xff);
        }
        if (cmd == DEV_READ_EE || cmd == DEV_WRITE_EE) {
            m_uartCmdBuffer[MODBUS_DATA00] = eeAddress;
            m_uartCmdBuffer[MODBUS_DATA01] = 0;
            m_uartCmdBuffer[MODBUS_DATA02] = 0;
            m_uartCmdBuffer[MODBUS_DATA03] = 0;
        }
        if (cmd == DEV_WRITE_EE) {
            m_uartCmdBuffer[MODBUS_DATA10] = (byte) (eeData0 & 0xff);
            m_uartCmdBuffer[MODBUS_DATA11] = (byte) ((eeData0 >> 8) & 0xff);
            m_uartCmdBuffer[MODBUS_DATA12] = (byte) (eeData1 & 0xff);
            m_uartCmdBuffer[MODBUS_DATA13] = (byte) ((eeData1 >> 8) & 0xff);
        }
        if (cmd == DEV_PUT_ID) {
            FileFunc.SetModbusData32(m_uartCmdBuffer, (int) macid, MODBUS_MAC0);
            FileFunc.SetModbusData32(m_uartCmdBuffer, blasterTimer, MODBUS_DATA00);
            FileFunc.SetModbusData16(m_uartCmdBuffer, count, MODBUS_DATA10);
            FileFunc.SetModbusData16(m_uartCmdBuffer, total, MODBUS_DATA12);
        }
        if (cmd == DEV_PUT_AREA) {
            FileFunc.SetModbusData16(m_uartCmdBuffer, blasterTimer + BLASTER_TIMER_DELAY, MODBUS_DATA02);
            FileFunc.SetModbusData16(m_uartCmdBuffer, area * 1000 + hole, MODBUS_DATA00);
            FileFunc.SetModbusData32(m_uartCmdBuffer, (int) macid, MODBUS_MAC0);
            FileFunc.SetModbusData16(m_uartCmdBuffer, count, MODBUS_DATA10);
            FileFunc.SetModbusData16(m_uartCmdBuffer, total, MODBUS_DATA12);
        }
        if (cmd == DEV_GET_ID) {
            FileFunc.SetModbusData32(m_uartCmdBuffer, count, MODBUS_MAC0);
        }
//        if (cmd == DEV_GET_ALL_DATA) {
//            FileFunc.SetModbusData32(m_uartCmdBuffer, total, MODBUS_DATA12);
//        }
        if (cmd == DEV_PUT_UUID || cmd == DEV_TEST_MODE || cmd == DEV_RW_ID || cmd == DEV_RW_VER || cmd == DEV_SET_TEST_DELAY) {
            FileFunc.SetModbusData16(m_uartCmdBuffer, count, MODBUS_STATUS);
            FileFunc.SetModbusData32(m_uartCmdBuffer, dataMac, MODBUS_MAC0);
            FileFunc.SetModbusData32(m_uartCmdBuffer, data0, MODBUS_DATA00);
            FileFunc.SetModbusData32(m_uartCmdBuffer, data1, MODBUS_DATA10);

        }
        if (cmd == DEV_GET_UUID) {
            FileFunc.SetModbusData16(m_uartCmdBuffer, count, MODBUS_STATUS);
        }
        if (cmd == DEV_CHECK_NET) {
            FileFunc.SetModbusData16(m_uartCmdBuffer, total, MODBUS_DATA12);
        }
        crc = CRC16(m_uartCmdBuffer, 17);
        m_uartCmdBuffer[MODBUS_SUML] = (byte) (crc >> 8);
        m_uartCmdBuffer[MODBUS_SUMH] = (byte) (crc & 0x00ff);
        length = 19;

    }

    public static short CRC16(byte[] arr_buff, int len) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < len; i++) {
            CRC ^= ((int) arr_buff[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return (short) (CRC & 0x0000ffff);
    }

    public static boolean CheckCrc(byte[] data, int length) {
        boolean ack = false;
        short crc, tmp;
        if (length == 19) {
            crc = data[length - 2];
            crc &= 0x00ff;
            tmp = data[length - 1];
            tmp &= 0x00ff;
            crc = (short) ((crc << 8) + tmp);
            if (crc == CRC16(data, length - 2))
                ack = true;
            return ack;
        }
        return false;
    }

}

