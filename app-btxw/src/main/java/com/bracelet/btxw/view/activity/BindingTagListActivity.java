package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.bracelet.btxw.R;
import com.bracelet.btxw.view.adapter.AssetAdapter;
import com.bracelet.btxw.view.fragment.BindingListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BindingTagListActivity extends BaseActivity {
    @BindView(R.id.flTagList)
    FrameLayout flTagList;

    public static final int EXTRA_FINDING_CONTINUITY = 1;
    public static final int EXTRA_FINDING_ONCE = 2;

    private static final String EXTRA_MODE = "mode";

    private int mode;
    private BindingListFragment mBindingListFragment;

    public static void actionStart(Context context, int mode) {
        Intent intent = new Intent(context, BindingTagListActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding_tag_list);
        ButterKnife.bind(this);
        initView();
        loadData();
    }

    @Override
    public void onScanCodeResult(String code) {
        super.onScanCodeResult(code);
        if (mBindingListFragment != null) {
            mBindingListFragment.onScanCodeResult(code);
        }
    }

    private void initView() {
        setToolbarTitle(getResources().getString(R.string.title_bound_good));
        mBindingListFragment = new BindingListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.flTagList, mBindingListFragment).commitAllowingStateLoss();
    }

    private void loadData() {
        mode = getIntent().getIntExtra(EXTRA_MODE, 0);
        mBindingListFragment.setFindingMode(mode);
        useBle = false;
    }
}
