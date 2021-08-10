package com.example.xdyblaster.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.Bridge;

import java.util.List;

public class BridgeAdapter extends BaseAdapter {
    private List<Bridge> mDatas = null;
    private Context mContext = null;

    public BridgeAdapter(Context context, List<Bridge> datas) {
        mDatas = datas;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {


        ViewHolder vh;
        Bridge bridge = mDatas.get(position);
        if (view == null) {
            vh = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.bridge_item, null);
            vh.tvIndex = (TextView) view.findViewById(R.id.tvBridgeIndex);
            vh.tvUid = (TextView) view.findViewById(R.id.tvBridgeUid);
            vh.tvCap = (TextView) view.findViewById(R.id.tvBridgeCap);
            vh.tvBridge = (TextView) view.findViewById(R.id.tvBridgeBridge);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        vh.tvIndex.setText(String.valueOf(position + 1));
        vh.tvUid.setText(bridge.getUid());
        vh.tvCap.setText(String.valueOf(bridge.getCap()));
        vh.tvBridge.setText(String.valueOf(bridge.getBridge()));
        if (bridge.getCap() > 150 || bridge.getCap() < 60)
            vh.tvCap.setTextColor(ContextCompat.getColor(mContext, R.color.red));
        else
            vh.tvCap.setTextColor(ContextCompat.getColor(mContext, R.color.green));
        if (bridge.getBridge() > 2.8 || bridge.getBridge() < 1.3)
            vh.tvBridge.setTextColor(ContextCompat.getColor(mContext, R.color.red));
        else
            vh.tvBridge.setTextColor(ContextCompat.getColor(mContext, R.color.green));

        return view;

    }

    class ViewHolder {
        TextView tvIndex;
        TextView tvUid;
        TextView tvBridge;
        TextView tvCap;
    }

}
