package com.example.xdyblaster.ble;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xdyblaster.R;

import java.util.List;


public class BleAdapter extends RecyclerView.Adapter<BleAdapter.BleHolder> {


    private Context mContext;
    private List<BleData> mDatas;
    public OnItemClickListener onItemClickListener;//第二步：声明自定义的接口

    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemLongClick(View v);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    public BleAdapter(Context context, List<BleData> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final BleHolder bleHolder, final int position) {
        BleData bleData;
        bleData=mDatas.get(position);
        String tmp = "";
        tmp = bleData.getName();
        if (tmp == null)
            tmp = "Unknown Device";
        bleHolder.textViewName.setText(tmp);
        bleHolder.textViewMac.setText(bleData.getMac());
        bleHolder.textViewDB.setText("RSSI:" + bleData.getRssi());
    }


    @Override
    public BleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ble_item, parent, false);
        BleHolder bleHolder = new BleHolder(view);
        return bleHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return position & 0x01;
//        return super.getItemViewType(position);
    }


    class BleHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView textViewName;
        private TextView textViewMac;
        private TextView textViewDB;

        public BleHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.tv_ble_name);
            textViewMac = itemView.findViewById(R.id.tv_ble_mac);
            textViewDB = itemView.findViewById(R.id.tv_rssi);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

    }

}

