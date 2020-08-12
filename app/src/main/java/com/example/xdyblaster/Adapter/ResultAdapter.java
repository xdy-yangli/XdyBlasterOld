package com.example.xdyblaster.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;
import com.example.xdyblaster.util.ResultData;

import java.util.List;
import java.util.Objects;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.RecyclerViewHolder> {

    private Context mContext;
    private List<ResultData> mDatas;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    DataViewModel dataViewModel;
    public int opened = -1;

    public ResultAdapter(Context context, List<ResultData> datas, DataViewModel dataViewModel) {
        this.mContext = context;
        this.mDatas = datas;
        this.dataViewModel = dataViewModel;

    }


    @NonNull
    @Override
    public ResultAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ResultAdapter.RecyclerViewHolder holder = new RecyclerViewHolder(View.inflate(mContext, R.layout.result_item, null));

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.RecyclerViewHolder holder, int position) {
        ResultData data;
        data = mDatas.get(position);
        holder.tvName.setText(data.name);
        if (data.upD.equals("1"))
            holder.tvUpD.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(), R.color.green));
        else
            holder.tvUpD.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(), R.color.red));
        if (data.upG.equals("1"))
            holder.tvUpG.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(), R.color.green));
        else
            holder.tvUpG.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(), R.color.red));

        if (opened == position)
            holder.ltItem.setBackgroundColor(0x80FEEA3A);
        else
            holder.ltItem.setBackgroundColor(0xffffffff);

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private TextView tvName, tvUpD, tvUpG;
        private LinearLayout ltItem;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvUpD = itemView.findViewById(R.id.tvUpD);
            tvUpG = itemView.findViewById(R.id.tvUpG);
            ltItem = itemView.findViewById(R.id.lt_item);
            ltItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.lt_item:
                    if (opened == getBindingAdapterPosition()) {//getAdapterPosition()) {
                        //当点击的item已经被展开了, 就关闭.
                        opened = -1;
                        notifyItemChanged(getBindingAdapterPosition());
                    } else {
                        int oldOpened = opened;
                        opened = getBindingAdapterPosition();
                        notifyItemChanged(oldOpened);
                        notifyItemChanged(opened);
                    }
                    break;

            }
        }
    }

}
