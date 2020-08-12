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
import androidx.recyclerview.widget.RecyclerView;

import com.example.xdyblaster.R;
import com.example.xdyblaster.util.DataViewModel;
import com.example.xdyblaster.util.DetonatorData;

import java.util.List;

public class DetonatorAdapter extends RecyclerView.Adapter<DetonatorAdapter.RecyclerViewHolder> implements View.OnClickListener {

    private Context mContext;
    private List<DetonatorData> mDatas;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    DataViewModel dataViewModel;
    private final static int[] itemColor = {0xff666666, 0xff4BAE4F, 0xfff34235, 0xfffD9735, 0xff019ae8};
    public int opened = -1;
    public int selection = -1;
    boolean clickEn = true;
    int viewFlag = 0x3f;

    public interface OnItemSelectedListener {
        void OnItemSelected(int item, int n);
    }

    public interface OnScrollListener {
        void OnItemScroll(int item);
    }

    public OnItemSelectedListener onItemSelectedListener = null;
    public OnScrollListener onScrollListener = null;

    public DetonatorAdapter(Context context, List<DetonatorData> datas, DataViewModel dataViewModel) {
        this.mContext = context;
        this.mDatas = datas;
        this.dataViewModel = dataViewModel;
        for (DetonatorData d : mDatas) {
            d.setSelection(false);
        }
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final DetonatorAdapter.RecyclerViewHolder holder, final int position) {
        String str;
        DetonatorData data;
        data = mDatas.get(position);
        //setItemVisibility(!data.getSelection(),holder.itemView);
//        if (!data.getSelection())
        //  holder.item.setVisibility(View.GONE);
        holder.tvIndex.setText(String.valueOf(position + 1));
        holder.tvId.setText(data.getUuid());
        holder.tvTime.setText(String.valueOf(data.getBlasterTime()));
        holder.tvRow.setText(String.valueOf(data.getRowNum() + 1));
        holder.tvHole.setText(String.valueOf(data.getHoleNum() + 1));
        holder.tvTimeErr.setText(String.valueOf(data.getBlasterTimeErr()));
        holder.tvRowErr.setText(String.valueOf(data.getRowNumErr() + 1));
        holder.tvHoleErr.setText(String.valueOf(data.getHoleNumErr() + 1));
        holder.setColor(position, data.getColor());
        if (opened != -1) {
            if (position == opened) {
                holder.item.setBackgroundColor(0x80FEEA3A);
                holder.menu.setVisibility(View.VISIBLE);
            } else {
                holder.item.setBackgroundColor(0xffffffff);
                holder.menu.setVisibility(View.GONE);
            }
        } else {
            holder.menu.setVisibility(View.GONE);
            if (position == selection) {
                holder.item.setBackgroundColor(0x80FEEA3A);
            } else {
                holder.item.setBackgroundColor(0xffffffff);
            }
        }
//        if (data.getId() != 0) {
//            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlack0));
//            holder.tvId.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlue));
//            if (data.getColor() == 0)
//                holder.ivDetonator.setImageDrawable(dataViewModel.drawableB);
//            else if (data.getColor() == 1)
//                holder.ivDetonator.setImageDrawable(dataViewModel.drawableG);
//            else if (data.getColor() == 2)
//                holder.ivDetonator.setImageDrawable(dataViewModel.drawableR);
//
//        } else {
//            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlack3));
//            holder.tvId.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlack3));
//            holder.ivDetonator.setImageDrawable(dataViewModel.drawableGray);
//        }
//        holder.cbSelect.setTag(position);
        dataViewModel.ok = false;

    }


    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerViewHolder holder = new RecyclerViewHolder(View.inflate(mContext, R.layout.detonator_item, null));


        return holder;
    }


    class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//        private TextView tvId, tvStatus;
//        private ImageView ivDetonator;
//        private CheckBox cbSelect;

        private TextView tvIndex, tvId, tvTime, tvRow, tvHole;
        private TextView tvTimeErr, tvRowErr, tvHoleErr;
        private LinearLayout item;
        private RelativeLayout menu;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvId = itemView.findViewById(R.id.tvID);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRow = itemView.findViewById(R.id.tvRow);
            tvHole = itemView.findViewById(R.id.tvHole);
            menu = itemView.findViewById(R.id.rt_menu);
            item = itemView.findViewById(R.id.lt_item);
            item.setOnClickListener(this);
            Button btDelete = itemView.findViewById(R.id.bt_delete);
            Button btEdit = itemView.findViewById(R.id.bt_edit);
            Button btInsert = itemView.findViewById(R.id.bt_insert);
            btDelete.setOnClickListener(this);
            btEdit.setOnClickListener(this);
            btInsert.setOnClickListener(this);
            tvTimeErr = itemView.findViewById(R.id.tvTimeErr);
            tvRowErr = itemView.findViewById(R.id.tvRowErr);
            tvHoleErr = itemView.findViewById(R.id.tvHoleErr);
//            tvStatus = itemView.findViewById(R.id.tvStatus);
//            ivDetonator = itemView.findViewById(R.id.ivDetonator);
//            cbSelect = itemView.findViewById(R.id.cbSelect);

        }

        public void setColor(int c, int s) {
            int color;
//            if ((c & 0x01) == 0)
//                item.setBackgroundColor(0x40FEEA3A);
//            else
//                item.setBackgroundColor(0xffffffff);
            tvHoleErr.setVisibility(View.GONE);
            tvRowErr.setVisibility(View.GONE);
            tvTimeErr.setVisibility(View.GONE);
            if (s == 0) {
                tvIndex.setTextColor(itemColor[0]);
                tvId.setTextColor(itemColor[0]);
                tvTime.setTextColor(itemColor[0]);
                tvHole.setTextColor(itemColor[0]);
                tvRow.setTextColor(itemColor[0]);
            } else if (s == 0x80) {
                tvIndex.setTextColor(itemColor[1]);
                tvId.setTextColor(itemColor[1]);
                tvTime.setTextColor(itemColor[1]);
                tvHole.setTextColor(itemColor[1]);
                tvRow.setTextColor(itemColor[1]);
            } else if (s == 0x1f) {
                tvIndex.setTextColor(itemColor[2]);
                tvId.setTextColor(itemColor[2]);
                tvTime.setTextColor(itemColor[2]);
                tvHole.setTextColor(itemColor[2]);
                tvRow.setTextColor(itemColor[2]);
            } else if (s == 0x40) {
                tvIndex.setTextColor(itemColor[4]);
                tvId.setTextColor(itemColor[4]);
                tvTime.setTextColor(itemColor[4]);
                tvHole.setTextColor(itemColor[4]);
                tvRow.setTextColor(itemColor[4]);
            } else {
                tvIndex.setTextColor(itemColor[0]);
                tvId.setTextColor(itemColor[0]);
                tvTime.setTextColor(itemColor[0]);
                tvHole.setTextColor(itemColor[0]);
                tvRow.setTextColor(itemColor[0]);
                if ((s & 0x01) != 0) {
                    tvHoleErr.setVisibility(View.VISIBLE);
                }
                if ((s & 0x02) != 0)
                    tvRowErr.setVisibility(View.VISIBLE);
                if ((s & 0x04) != 0)
                    tvTimeErr.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onClick(View v) {
            if (!clickEn) {
                if(v.getId()== R.id.lt_item)
                {
                    if (onItemSelectedListener != null)
                        onItemSelectedListener.OnItemSelected(getBindingAdapterPosition(), 4);
                }
                return;
            }
            switch (v.getId()) {
                case R.id.lt_item:
                    if (opened == getBindingAdapterPosition()){//getAdapterPosition()) {
                        //当点击的item已经被展开了, 就关闭.
                        opened = -1;
                        notifyItemChanged(getBindingAdapterPosition());
                    } else {
                        int oldOpened = opened;
                        opened = getBindingAdapterPosition();
                        notifyItemChanged(oldOpened);
                        notifyItemChanged(opened);
                        if (onScrollListener != null)
                            onScrollListener.OnItemScroll(opened);
                        if (selection != -1) {
                            notifyItemChanged(selection);
                            selection = -1;
                        }

                    }
                    if (onItemSelectedListener != null)
                        onItemSelectedListener.OnItemSelected(opened, 0);
                    break;
                case R.id.bt_delete:
                    if (onItemSelectedListener != null)
                        onItemSelectedListener.OnItemSelected(opened, 1);
                    opened = -1;
                    break;
                case R.id.bt_edit:
                    if (onItemSelectedListener != null)
                        onItemSelectedListener.OnItemSelected(opened, 2);
                    selection = opened;
                    opened = -1;
                    break;
                case R.id.bt_insert:
                    if (onItemSelectedListener != null)
                        onItemSelectedListener.OnItemSelected(opened, 3);
                    selection = opened;
                    opened = -1;
                    break;

            }
        }
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();

    }

    public void clickEnable(boolean b) {
        clickEn = b;
    }


    public void clearAllItem() {
        mDatas.clear();
    }

    public void addItem(DetonatorData data) {
        mDatas.add(data);
    }
    public DetonatorData getItem(int n) {
         return mDatas.get(n);
    }
    public void setItemColor(int n,int color) {
        mDatas.get(n).setColor(color);
    }

}

