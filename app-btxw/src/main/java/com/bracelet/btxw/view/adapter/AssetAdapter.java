package com.bracelet.btxw.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bracelet.btxw.R;
import com.bracelet.btxw.entity.Asset;

import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.AssetViewHolder> {
    private Context mContext;
    private List<Asset> mList;
    private OnAdapterItemClickListener mOnAdapterItemClickListener;

    public AssetAdapter(Context context, List<Asset> list) {
        mContext = context;
        mList = list;
    }

    static class AssetViewHolder extends RecyclerView.ViewHolder{
        TextView tvAssetName;
        TextView tvAssetNo;
        TextView tvTagName;
        TextView tvTagNo;
        AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAssetName = itemView.findViewById(R.id.tvAssetName);
            tvAssetNo = itemView.findViewById(R.id.tvAssetNo);
            tvTagName = itemView.findViewById(R.id.tvTagName);
            tvTagNo = itemView.findViewById(R.id.tvTagNo);
        }
    }

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_asset, viewGroup, false);
        final AssetViewHolder viewHolder = new AssetViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnAdapterItemClickListener != null) {
                    int position = viewHolder.getAdapterPosition();
                    mOnAdapterItemClickListener.onClick(position);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder assetViewHolder, int i) {
        Asset asset = mList.get(i);
        assetViewHolder.tvAssetName.setText(String.format(mContext.getResources().getString(R.string.content_good), asset.getAssetName()));
        assetViewHolder.tvAssetNo.setText(String.format(mContext.getResources().getString(R.string.content_number), asset.getAssetNo()));
        assetViewHolder.tvTagName.setText(String.format(mContext.getResources().getString(R.string.content_tag), asset.getTagName()));
        assetViewHolder.tvTagNo.setText(String.format(mContext.getResources().getString(R.string.content_mac), asset.getTagMac()));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setAssets(List<Asset> list) {
        mList.clear();
        if (list != null) {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addAssets(List<Asset> list) {
        if (list != null) {
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addNewAsset(Asset asset) {
        mList.add(0, asset);
        notifyItemInserted(0);
    }

    public void deleteAsset(long keyId) {
        for (int i = 0; i < mList.size(); i ++) {
            if (mList.get(i).getKeyId() == keyId) {
                mList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public interface OnAdapterItemClickListener {
        void onClick(int position);
    }

    public void setOnAdapterItemClickListener(OnAdapterItemClickListener listener) {
        mOnAdapterItemClickListener = listener;
    }
}
