package com.bracelet.btxw.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bracelet.btxw.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ReportActivity extends AppCompatActivity {
    @BindView(R.id.tvReport)
    TextView tvReport;

    private static final String ExtraReport = "report";

    public static void actionStart(Context context, String report) {
        Intent intent = new Intent(context, ReportActivity.class);
        intent.putExtra(ExtraReport, report);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        tvReport.setText(getIntent().getStringExtra(ExtraReport));
    }
}
