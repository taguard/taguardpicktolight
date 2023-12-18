package com.bracelet.btxw.view.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bracelet.ble.btxw.BTXW_Device;
import com.bracelet.btxw.R;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LightingDemoFragment extends BaseFragment {

    @BindView(R.id.btnPurple)
    Button btnPurple;
    @BindView(R.id.btnRedGroup)
    Button btnRedGroup;
    @BindView(R.id.btnWhiteGroup)
    Button btnWhiteGroup;
    @BindView(R.id.btnYellowGroup)
    Button btnYellowGroup;
    @BindView(R.id.btnBlueGroup)
    Button btnBlueGroup;
    @BindView(R.id.btnOrangeGroup)
    Button btnOrangeGroup;
    @BindView(R.id.etLightingTime)
    EditText etLightingTime;

    byte[] chooseItem = { 0 };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_lighting_demo, container, false);
        ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }

    @Override
    public void onDeviceConnect() {
        super.onDeviceConnect();
    }

    @Override
    public void onDeviceDisconnect() {
        super.onDeviceDisconnect();
    }

    private void initView() {
        btnPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim(btnPurple);
                chooseItem[0] = BTXW_Device.LIGHT_PURPLE_GROUP;
                light();
            }
        });
        btnWhiteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim(btnWhiteGroup);
                chooseItem[0] = BTXW_Device.LIGHT_WHITE_GROUP;
                light();
            }
        });
        btnRedGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim(btnRedGroup);
                chooseItem[0] = BTXW_Device.LIGHT_RED_GROUP;
                light();
            }
        });
        btnBlueGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim(btnBlueGroup);
                chooseItem[0] = BTXW_Device.LIGHT_BLUE_GROUP;
                light();
            }
        });
        btnOrangeGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim(btnOrangeGroup);
                chooseItem[0] = BTXW_Device.LIGHT_ORANGE_GROUP;
                light();
            }
        });
        btnYellowGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnim(btnYellowGroup);
                chooseItem[0] = BTXW_Device.LIGHT_YELLOW_GROUP;
                light();
            }
        });
    }

    private void initData(){

    }

    private void startAnim(Button button){
        Animation animation = AnimationUtils.loadAnimation(this.mBaseActivity, R.anim.anim_color_button);
        animation.setRepeatCount(1000);
        animation.setRepeatMode(Animation.REVERSE);
        button.startAnimation(animation);
    }

    private boolean isValidTime() {
        String content = etLightingTime.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            if (content.length() < 4) {
                try {
                    int i = Integer.parseInt(content);
                    if (i >= 1 && i <= 250) {
                        return true;
                    }
                } catch (Exception ex) {
                }
            }
        }
        return false;
    }

    private void light() {
        if (mBaseActivity.isDeviceConnected()) {
            if(isValidTime()) {
                byte effectiveSeconds = (byte) Integer.parseInt(etLightingTime.getText().toString());
                mBTXWDevice.openDeviceMultipleLights(new BTXW_Device.OpenDeviceLightCallback() {
                    @Override
                    public void onResult(int status) {
                        if (status == 0) {
                            showToast(getResources().getString(R.string.toast_command_success));
                        } else {
                            showToast(String.format(getResources().getString(R.string.toast_command_failed), status));
                        }
                    }
                }, chooseItem, effectiveSeconds);
            } else {
                showToast(getResources().getString(R.string.toast_input_lighting_seconds));
            }
        }
    }
}

