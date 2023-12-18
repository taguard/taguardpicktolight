package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.bracelet.btxw.R;
import com.bracelet.btxw.view.fragment.BaseFragment;
import com.bracelet.btxw.view.fragment.LightingDemoFragment;
import com.bracelet.btxw.view.weight.FixViewPager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabIndicator;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LightingDemoActivity extends BaseActivity{
    @BindView(R.id.fragmentLightingTab)
    QMUITabSegment fragmentLightingTab;
    @BindView(R.id.fragmentLightingPager)
    FixViewPager fragmentLightingPager;

    public static final int TAB_INDEX_LIGHTING_DEMO = 0;

    private LightingDemoActivity.TabFragmentLightingPagerAdapter mTabFragmentLightingPagerAdapter;
    private SparseArray<BaseFragment> mFragmentSparseArray;

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, LightingDemoActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lighting_demo);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFragmentSparseArray != null) {
            for (int i = 0; i < mFragmentSparseArray.size(); i ++) {
                mFragmentSparseArray.valueAt(i).disconnectBTXWDevice();
            }
        }
    }

    @Override
    public void onDeviceConnect() {
        super.onDeviceConnect();
        if (mFragmentSparseArray != null) {
            for (int i = 0; i < mFragmentSparseArray.size(); i ++) {
                mFragmentSparseArray.valueAt(i).onDeviceConnect();
            }
        }
    }

    @Override
    public void onDeviceDisconnect() {
        super.onDeviceDisconnect();
    }

    @Override
    public void onScanCodeResult(String code) {
        super.onScanCodeResult(code);
        if (mFragmentSparseArray != null) {
            for (int i = 0; i < mFragmentSparseArray.size(); i ++) {
                mFragmentSparseArray.valueAt(i).onScanCodeResult(code);
            }
        }
    }

    private void initView() {
        setToolbarTitle(getResources().getString(R.string.title_lighting_demo));
        mFragmentSparseArray = new SparseArray<>();
        mFragmentSparseArray.append(TAB_INDEX_LIGHTING_DEMO, new LightingDemoFragment());
        mTabFragmentLightingPagerAdapter = new TabFragmentLightingPagerAdapter(getSupportFragmentManager(), mFragmentSparseArray);
        fragmentLightingPager.setAdapter(mTabFragmentLightingPagerAdapter);
        fragmentLightingPager.setCurrentItem(TAB_INDEX_LIGHTING_DEMO);
        fragmentLightingTab.setupWithViewPager(fragmentLightingPager);
        fragmentLightingTab.setIndicator(new QMUITabIndicator(
                QMUIDisplayHelper.dp2px(this, 2), false, true));

        QMUITab tab0 = fragmentLightingTab.getTab(TAB_INDEX_LIGHTING_DEMO);
        tab0.setText(getResources().getString(R.string.title_lighting_demo));
        fragmentLightingTab.notifyDataChanged();
    }

    public class TabFragmentLightingPagerAdapter extends FragmentPagerAdapter {
        private SparseArray<BaseFragment> mArray;

        TabFragmentLightingPagerAdapter(FragmentManager fm, SparseArray<BaseFragment> list) {
            super(fm);
            mArray = list;
        }

        @Override
        public BaseFragment getItem(int i) {
            return mArray.valueAt(i);
        }

        @Override
        public int getCount() {
            return mArray.size();
        }
    }
}
