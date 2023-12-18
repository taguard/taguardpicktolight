package com.bracelet.btxw.view.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.bracelet.btxw.R;

import org.litepal.LitePal;

import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadData();
    }

    public void startBingingActivity(View view) {
        BindingTagActivity.actionStart(this);
    }

    public void startFindingOnceActivity(View view) {
        BindingTagListActivity.actionStart(mContext, BindingTagListActivity.EXTRA_FINDING_ONCE);
    }

    public void startFindingContinuityActivity(View view) {
        BindingTagListActivity.actionStart(mContext, BindingTagListActivity.EXTRA_FINDING_CONTINUITY);
    }
    public void startDemonstrationActivity(View view) {
        DemonstrationActivity.actionStart(this);
    }

    public void startSDKActivity(View view) {
        SDKMainActivity.actionStart(this);
    }

    public void startBroadcastResultActivity(View view) {
        BroadcastResultActivity.actionStart(this);
    }

    public void startDemoActivity(View view){
        LightingDemoActivity.actionStart(this);
    }

    private void loadData() {
        LitePal.getDatabase();
        useBle = false;
    }
}
