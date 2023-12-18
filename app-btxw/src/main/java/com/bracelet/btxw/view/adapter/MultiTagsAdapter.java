package com.bracelet.btxw.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.R;

import java.util.ArrayList;
import java.util.List;

public class MultiTagsAdapter extends RecyclerView.Adapter<MultiTagsAdapter.ViewHolder> {

    private OnAdapterItemClickListener mOnAdapterItemClickListener;
    private List<BTXW_Device> devices;
    private Context mContext;

    public MultiTagsAdapter(Context context) {
        mContext = context;
        devices = new ArrayList<>();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvMac;
        TextView tvRssi;
        ProgressBar progress;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMac = itemView.findViewById(R.id.tvMac);
            tvRssi = itemView.findViewById(R.id.tvRssi);
            progress = itemView.findViewById(R.id.progress);
        }
    }

    @NonNull
    @Override
    public MultiTagsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth2, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnAdapterItemClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    mOnAdapterItemClickListener.onClick(devices.get(position), position);
                }
            }
        });
        return viewHolder;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BTXW_Device device = devices.get(position);
        holder.tvName.setText(device.getName());
        holder.tvMac.setText(device.getAddress());
        if (device.isConnected()) {
            holder.tvRssi.setText((R.string.toast_ble_connect));
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.app_color_theme_10));
            holder.progress.setVisibility(View.INVISIBLE);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.app_color_theme_11));
            if (device.isConnecting()) {
                holder.tvRssi.setText((R.string.toast_connecting));
                holder.progress.setVisibility(View.VISIBLE);
            } else {
                holder.tvRssi.setText(String.format("%ddB", device.getRssi()));
                holder.progress.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BTXW_Device device) {
        for (BTXW_Device d : devices) {
            if (d.equals(device)) {
                devices.get(devices.indexOf(d)).updateRssi(device.getRssi());
                notifyItemChanged(devices.indexOf(d));
                return;
            }
        }
        devices.add(device);
        notifyItemInserted(devices.size() - 1);
    }

    public void removeDevices() {
        for(int i = devices.size() - 1; i >= 0 ;i --){
            if (!devices.get(i).isConnected() && !devices.get(i).isConnecting()) {
                devices.remove(devices.get(i));
            }
        }
        notifyDataSetChanged();
    }

    public List<BTXW_Device> getConnectedDevices() {
        List<BTXW_Device> list = new ArrayList<>();
        for (BTXW_Device device : devices) {
            if (device.isConnected()) {
                list.add(device);
            }
        }
        return list;
    }

    public List<BTXW_Device> getConnectingDevices() {
        List<BTXW_Device> list = new ArrayList<>();
        for (BTXW_Device device : devices) {
            if (device.isConnecting()) {
                list.add(device);
            }
        }
        return list;
    }

    public void updateConnectionState(BTXW_Device device) {
        if (devices.indexOf(device) >= 0) {
            notifyItemChanged(devices.indexOf(device));
        }
    }

    public interface OnAdapterItemClickListener {
        void onClick(BTXW_Device device, int position);
    }

    public void setOnAdapterItemClickListener(OnAdapterItemClickListener listener) {
        mOnAdapterItemClickListener = listener;
    }
}


