package com.example.xdyblaster.util;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.example.xdyblaster.R;

import static com.example.xdyblaster.MainActivity.actionScan;

public class GetKey extends FrameLayout {
    Context mContext;
    View mView;
    public KeyMulti keyMulti=null;

    public interface KeyMulti {
        void onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);
    }

    public GetKey(Context context) {
        this(context, null);
    }

    public GetKey(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GetKey(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.getkey, this, true);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        if(keyMulti!=null)
            keyMulti.onKeyMultiple( keyCode,  repeatCount, event);
        Intent intent = new Intent(actionScan);
        String str = event.getCharacters();
        intent.putExtra("barcode", str);
        mContext.getApplicationContext().sendBroadcast(intent);
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

}
