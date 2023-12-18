package com.bracelet.btxw.view.adapter;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bracelet.ble.bt.BleBT;
import com.bracelet.btxw.R;
import com.bracelet.btxw.meta.BleBTShow;
import com.bracelet.btxw.utils.Configs;
import com.bracelet.btxw.utils.TextFormatUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BTBroadcastAdapter extends RecyclerView.Adapter<BTBroadcastAdapter.BTBroadcastViewHolder> {
    private List<BleBTShow> mList;
    private List<BleBTShow> mSaveList;
    private Context mContext;

    public BTBroadcastAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
        mSaveList = new ArrayList<>();
    }

    static class BTBroadcastViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvType;
        TextView tvAddress;
        TextView tvRssi;
        TextView tvInterval;
        TextView tvData;
        TextView tvRawData;

        BTBroadcastViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvType = itemView.findViewById(R.id.tvType);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRssi = itemView.findViewById(R.id.tvRssi);
            tvInterval = itemView.findViewById(R.id.tvInterval);
            tvData = itemView.findViewById(R.id.tvData);
            tvRawData = itemView.findViewById(R.id.tvRawData);
        }
    }

    @NonNull
    @Override
    public BTBroadcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bt_broadcast, parent, false);
        BTBroadcastViewHolder viewHolder = new BTBroadcastViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BTBroadcastViewHolder holder, int position) {
        BleBTShow show = mList.get(position);
        BleBT bleBT = show.getBleBT();
        String name = TextFormatUtils.DEFAULT_TEXT;
        if (!TextUtils.isEmpty(bleBT.getName())) {
            name = bleBT.getName();
        }
        holder.tvName.setText(name);
        holder.tvType.setText(bleBT.getTypeName());
        holder.tvType.setBackgroundColor(ContextCompat.getColor(mContext, show.getColor(bleBT)));
        holder.tvAddress.setText(bleBT.getAddress());
        holder.tvRssi.setText(String.format(Locale.getDefault(), "%ddBm", bleBT.getRssi()));
        if (show.getInterval() == 0) {
            holder.tvInterval.setText(TextFormatUtils.DEFAULT_TEXT);
        } else {
            holder.tvInterval.setText(String.format("%sms", show.getInterval()));
        }
        holder.tvData.setText(show.getDataText());
        holder.tvRawData.setText(bleBT.getRawString());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addDevice(BleBT bleBT) {
        for (int i = 0; i < mList.size(); i ++) {
            if (mList.get(i).getBleBT().equals(bleBT)) {
                mList.get(i).updateBleBt(bleBT);
                notifyItemChanged(i);
                return;
            }
        }
        mList.add(new BleBTShow(bleBT));
        notifyItemInserted(mList.size() - 1);
    }

    public void orderByRssi(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mList.sort(Comparator.comparing(BleBTShow::getRssi).reversed());
        }
        notifyDataSetChanged();
    }

    public void clearDevices() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void filterClearDevices(int filterRssi , String filterAddress , Configs.ConfigItem[] filterBTItems){
        for (int i = 0; i < mList.size(); i ++) {
            boolean filterBT = false;
            for (Configs.ConfigItem item : filterBTItems) {
                if (TextUtils.equals(item.getDescription(), mList.get(i).getBleBT().getTypeName())) {
                    filterBT = true;
                    break;
                }
            }
            if(filterBT){
                if(mList.get(i).getBleBT().getRssi() > filterRssi) {
                    mSaveList.add(mList.get(i));
                }
            }
            else if (!TextUtils.isEmpty(filterAddress)) {
                if (mList.get(i).getBleBT().getAddress().contains(filterAddress)) {
                    mSaveList.add(mList.get(i));
                }
            }
        }
        mList.clear();
        mList.addAll(mSaveList);
        mSaveList.clear();
        notifyDataSetChanged();
    }

}
