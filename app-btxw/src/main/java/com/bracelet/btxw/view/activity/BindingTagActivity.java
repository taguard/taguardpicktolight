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
import com.bracelet.btxw.view.fragment.BindingListFragment;
import com.bracelet.btxw.view.fragment.BindingTagFragment;
import com.bracelet.btxw.view.weight.FixViewPager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.tab.QMUITab;
import com.qmuiteam.qmui.widget.tab.QMUITabIndicator;
import com.qmuiteam.qmui.widget.tab.QMUITabSegment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BindingTagActivity extends BaseActivity {
    @BindView(R.id.fragmentTab)
    QMUITabSegment fragmentTab;
    @BindView(R.id.fragmentPager)
    FixViewPager fragmentPager;

    public static final int TAB_INDEX_BINDING = 0;
    public static final int TAB_INDEX_BINDING_LIST = 1;

    private TabFragmentPagerAdapter mTabFragmentPagerAdapter;
    private SparseArray<BaseFragment> mFragmentSparseArray;

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, BindingTagActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding_tag);
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

    public BindingTagFragment getBindingTagFragment() {
        return (BindingTagFragment)mTabFragmentPagerAdapter.getItem(TAB_INDEX_BINDING);
    }

    public BindingListFragment getBindingListFragment() {
        return (BindingListFragment)mTabFragmentPagerAdapter.getItem(TAB_INDEX_BINDING_LIST);
    }

    public void updateBindingListTab(int count) {
        QMUITab tab = fragmentTab.getTab(TAB_INDEX_BINDING_LIST);
        tab.setSignCount(count);
        fragmentTab.notifyDataChanged();
    }

    private void initView() {
        setToolbarTitle(getResources().getString(R.string.title_bind));
        mFragmentSparseArray = new SparseArray<>();
        mFragmentSparseArray.append(TAB_INDEX_BINDING, new BindingTagFragment());
        mFragmentSparseArray.append(TAB_INDEX_BINDING_LIST, new BindingListFragment());
        mTabFragmentPagerAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(), mFragmentSparseArray);
        fragmentPager.setAdapter(mTabFragmentPagerAdapter);
        fragmentPager.setCurrentItem(TAB_INDEX_BINDING);
        fragmentTab.setupWithViewPager(fragmentPager);
        fragmentTab.setIndicator(new QMUITabIndicator(
                QMUIDisplayHelper.dp2px(this, 2), false, true));

        QMUITab tab0 = fragmentTab.getTab(TAB_INDEX_BINDING);
        QMUITab tab1 = fragmentTab.getTab(TAB_INDEX_BINDING_LIST);
        tab0.setText(getResources().getString(R.string.title_bind));
        tab1.setText(getResources().getString(R.string.title_bound));
        tab1.setSignCount(4);
        fragmentTab.notifyDataChanged();
    }

    public class TabFragmentPagerAdapter extends FragmentPagerAdapter {
        private SparseArray<BaseFragment> mArray;

        TabFragmentPagerAdapter(FragmentManager fm, SparseArray<BaseFragment> list) {
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