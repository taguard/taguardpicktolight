package com.bracelet.btxw.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bracelet.btxw.R;
import com.bracelet.btxw.entity.Asset;
import com.bracelet.btxw.view.activity.BindingTagActivity;
import com.bracelet.btxw.view.activity.BindingTagListActivity;
import com.bracelet.btxw.view.activity.FindingTagContinuityActivity;
import com.bracelet.btxw.view.activity.FindingTagOnceActivity;
import com.bracelet.btxw.view.adapter.AssetAdapter;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BindingListFragment extends BaseFragment {
    @BindView(R.id.etSearchContent)
    EditText etSearchContent;
    @BindView(R.id.tvHint)
    TextView tvHint;
    @BindView(R.id.rvAssets)
    RecyclerView rvAssets;

    private List<Asset> mAssets;
    private AssetAdapter mAssetAdapter;
    private int findingMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_binding_list, container, false);
        ButterKnife.bind(this, view);
        initView();
        loadData();
        return view;
    }

    @Override
    public void onScanCodeResult(String code) {
        super.onScanCodeResult(code);
        etSearchContent.setText(code);
        if (!(mBaseActivity instanceof BindingTagActivity)) {
            searchAsset(etSearchContent.getText().toString());
        }
    }

    public void addNewAsset(Asset asset) {
        mAssetAdapter.addNewAsset(asset);
        if (mBaseActivity instanceof BindingTagActivity) {
            BindingTagActivity activity = (BindingTagActivity) mBaseActivity;
            activity.updateBindingListTab(mAssets.size());
        }
    }

    public void setFindingMode(int mode) {
        findingMode = mode;
    }

    private void initView() {
        mAssets = new ArrayList<>();
        mAssetAdapter = new AssetAdapter(mBaseActivity, mAssets);
        mAssetAdapter.setOnAdapterItemClickListener(new AssetAdapter.OnAdapterItemClickListener() {
            @Override
            public void onClick(int position) {
                if (position < mAssets.size()) {
                    if (findingMode == BindingTagListActivity.EXTRA_FINDING_CONTINUITY) {
                        FindingTagContinuityActivity.actionStart(getActivity(), mAssets.get(position));
                    } else if (findingMode == BindingTagListActivity.EXTRA_FINDING_ONCE) {
                        FindingTagOnceActivity.actionStart(getActivity(), mAssets.get(position));
                    } else {
                        if (mBaseActivity instanceof BindingTagActivity) {
                            showDeleteDialog(mAssets.get(position).getKeyId());
                        }
                    }
                }
            }
        });
        rvAssets.setAdapter(mAssetAdapter);
        rvAssets.setLayoutManager(new LinearLayoutManager(mBaseActivity));

        etSearchContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager inputMethodManager = (InputMethodManager) mBaseActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) {
                        inputMethodManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    }
                    searchAsset(etSearchContent.getText().toString());
                    return true;
                }
                return false;
            }
        });
        etSearchContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (etSearchContent.getRight() -
                            etSearchContent.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        onScanIconClick();
                        return true;
                    }
                }
                return false;
            }
        });

        if (mBaseActivity instanceof BindingTagActivity) {
            tvHint.setText(getResources().getString(R.string.content_select_to_delete));
        } else if (mBaseActivity instanceof FindingTagContinuityActivity || mBaseActivity instanceof FindingTagOnceActivity) {
            tvHint.setText(getResources().getString(R.string.content_select_to_find));
        }
    }

    private void loadData() {
        mAssetAdapter.addAssets(LitePal
                .order("id desc")
                .find(Asset.class));
        if (mBaseActivity instanceof BindingTagActivity) {
            BindingTagActivity activity = (BindingTagActivity)mBaseActivity;
            activity.updateBindingListTab(mAssets.size());
        }
    }

    private void onScanIconClick() {
        etSearchContent.setText("");
        mBaseActivity.toScanCode();
    }

    private void searchAsset(String content) {
        String match = "%" + content + "%";
        List<Asset> assets = LitePal
                .where("assetName like ? or assetNo like ? or tagName like ? or tagMac like ?",
                        match, match, match, match)
                .order("id desc")
                .find(Asset.class);
        mAssetAdapter.setAssets(assets);
        showToast(String.format(getResources().getString(R.string.toast_total_result), String.valueOf(assets.size())));
    }

    private void deleteAsset(long keyId) {
        boolean suc = LitePal.delete(Asset.class, keyId) > 0;
        if (suc) {
            mAssetAdapter.deleteAsset(keyId);
            BindingTagActivity activity = (BindingTagActivity) mBaseActivity;
            activity.updateBindingListTab(mAssets.size());
            showToast(getResources().getString(R.string.toast_deleted));
        }
    }

    private void showDeleteDialog(final long keyId) {
        new QMUIDialog.MessageDialogBuilder(mBaseActivity)
                .setTitle(getResources().getString(R.string.title_prompt))
                .setMessage(getResources().getString(R.string.content_confirm_delete))
                .addAction(getResources().getString(R.string.btn_cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(getResources().getString(R.string.btn_confirm), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        deleteAsset(keyId);
                    }
                })
                .create().show();
    }
}
