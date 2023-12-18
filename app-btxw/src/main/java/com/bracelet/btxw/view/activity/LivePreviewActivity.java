package com.bracelet.btxw.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.bracelet.btxw.R;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.mlkit.barcode.CameraSourcePreview;
import com.mlkit.barcode.GraphicOverlay;
import com.mlkit.barcode.MLKit;
import com.mlkit.barcode.PermissionUtil;
import com.mlkit.barcode.UriUtils;
import com.mlkit.barcode.ViewfinderView;

import java.util.List;

public class LivePreviewActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        View.OnClickListener {

    public static final int RESULT_CODE_SCAN = 0xA01;
    public static final String INTENT_EXTRA_KEY_SCAN_RESULT = "code_scan_result";

    public static final int REQUEST_CODE_PHOTO = 0x102;

    private static final String TAG = "LivePreviewActivity";
    private Context context;

    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private MLKit mlKit;

    private TextView imgGallery;
    private TextView imgLight;
    private ImageView imgSwitchCamera;
    private CameraSourcePreview previewView;
    private ViewfinderView viewfinderView;
    private ConstraintLayout previewBox;
    private TextView scanHint;
    private RelativeLayout bottomMask;

    public static void actionStartForResult(Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(activity, LivePreviewActivity.class), requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_preview);
        initViews();
        context = this;

        preview = findViewById(R.id.preview_view);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        imgGallery.setOnClickListener(this);
        imgLight.setOnClickListener(this);
        imgSwitchCamera.setOnClickListener(this);

        //构造出扫描管理器
        mlKit = new MLKit(this, preview, graphicOverlay);
        //是否扫描成功后播放提示音和震动
        mlKit.setPlayBeepAndVibrate(true, false);
        //仅识别二维码
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_AZTEC)
                        .build();
        mlKit.setBarcodeFormats(null);
        mlKit.setOnScanListener(new MLKit.OnScanListener() {
            @Override
            public void onSuccess(List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay, InputImage image) {
                showScanResult(barcodes, graphicOverlay, image);
            }

            @Override
            public void onFail(int code, Exception e) {

            }
        });
    }

    private void showScanResult(List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay, InputImage image) {
        if (barcodes.isEmpty()) {
            return;
        }
        imgSwitchCamera.setVisibility(View.INVISIBLE);
        bottomMask.setVisibility(View.GONE);
        mlKit.stopProcessor();

        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_KEY_SCAN_RESULT, barcodes.get(0).getRawValue());
        setResult(RESULT_CODE_SCAN, intent);
        finish();
    }

    private void requirePermission() {
        PermissionUtil.getInstance().with(this).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionUtil.PermissionListener() {
            @Override
            public void onGranted() {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
            }

            @Override
            public void onDenied(List<String> deniedPermission) {
                PermissionUtil.getInstance().showDialogTips(getBaseContext(), deniedPermission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }

            @Override
            public void onShouldShowRationale(List<String> deniedPermission) {
                requirePermission();
            }
        });
    }

    public void showPictures() {
        requirePermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String photo_path;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_PHOTO:
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = this.getContentResolver().query(data.getData(), proj, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int colum_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        photo_path = cursor.getString(colum_index);
                        if (photo_path == null) {
                            photo_path = UriUtils.getPath(getApplicationContext(), data.getData());
                        }
                        cursor.close();
                        mlKit.scanningImage(photo_path);
                        mlKit.setOnScanListener(new MLKit.OnScanListener() {
                            @Override
                            public void onSuccess(List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay, InputImage image) {
                                showScanResult(barcodes, graphicOverlay, image);
                            }

                            @Override
                            public void onFail(int code, Exception e) {

                            }
                        });
                    }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_gallery:
                showPictures();
                break;
            case R.id.img_light:
                mlKit.switchLight();
                break;
            case R.id.img_switch_camera:
                mlKit.switchCamera();
                break;
            default:
                break;
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        viewfinderView = findViewById(R.id.viewfinderView);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        previewBox = findViewById(R.id.preview_box);
        imgSwitchCamera = findViewById(R.id.img_switch_camera);
        scanHint = findViewById(R.id.scan_hint);
        imgLight = findViewById(R.id.img_light);
        imgGallery = findViewById(R.id.img_gallery);
        bottomMask = findViewById(R.id.bottom_mask);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
