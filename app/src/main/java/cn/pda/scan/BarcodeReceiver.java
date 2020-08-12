package cn.pda.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

public class BarcodeReceiver extends BroadcastReceiver {
    public Handler handler;

    public BarcodeReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        byte[] data = intent.getByteArrayExtra("data");
        if (data != null) {
//                String barcode = Tools.Bytes2HexString(data, data.length);
            String barcode = new String(data);
            Message message = new Message();
            message.what = 888;
            message.obj = barcode;
            handler.sendMessage(message);
        }

    }
}
