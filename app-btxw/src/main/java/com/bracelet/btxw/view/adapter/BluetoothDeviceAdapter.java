package com.bracelet.btxw.view.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private OnAdapterItemClickListener mOnAdapterItemClickListener;
    private List<BTXW_Device> devices;

    public BluetoothDeviceAdapter() {
        devices = new ArrayList<>();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        TextView tvMac;
        TextView tvRssi;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMac = itemView.findViewById(R.id.tvMac);
            tvRssi = itemView.findViewById(R.id.tvRssi);
        }
    }

    @NonNull
    @Override
    public BluetoothDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnAdapterItemClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    mOnAdapterItemClickListener.onClick(devices.get(position));
                }
            }
        });
        return viewHolder;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull BluetoothDeviceAdapter.ViewHolder holder, int position) {
        BTXW_Device device = devices.get(position);
        holder.tvName.setText(device.getName());
        holder.tvRssi.setText(String.format("%ddB", device.getRssi()));
        holder.tvMac.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BTXW_Device device) {
        for (BTXW_Device d : devices) {
            if (d.equals(device)) {
                devices.set(devices.indexOf(d), device);
                notifyItemChanged(devices.indexOf(d));
                return;
            }
        }
        devices.add(device);
        notifyItemInserted(devices.size() - 1);
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

    public interface OnAdapterItemClickListener {
        void onClick(BTXW_Device device);
    }

    public void setOnAdapterItemClickListener (OnAdapterItemClickListener listener) {
        mOnAdapterItemClickListener = listener;
    }
}
