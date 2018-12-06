package com.example.hp.sjtuphysics;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class SplashActivity extends Activity {

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    private final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private int countDownTime = 1000;
    private int countDownInterval = 1;

    private static final int ANIMATION_TIME = 2000;
    private static final float SCALE_END = 1.2F;

    private ImageView mSplashImage;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PrefManager(this);
        if(!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if(intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_splash);
        mSplashImage = findViewById(R.id.iv_entry);
        mSplashImage.setImageResource(R.drawable.sjtulogored);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !prefManager.isFirstTimeLaunch()) {
                    showMessageOKCancel("应用需要储存权限, 前往设置允许权限", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //requestPermissions(PERMISSIONS_STORAGE, 2);
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, 123);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SplashActivity.this, "申请权限失败", Toast.LENGTH_SHORT).show();
                            SplashActivity.this.finish();
                        }
                    });
                } else {
                    prefManager.setFirstTimeLaunch(false);
                    requestPermissions(PERMISSIONS_STORAGE, REQUEST_CODE_ASK_PERMISSIONS);
                }
            } else {
                CountDownTimer countDown = new CountDownTimer(countDownTime, countDownInterval) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        startAnim();
                    }
                };
                countDown.start();
            }
        }

    }

    private void startAnim() {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mSplashImage, "scaleX", 1f, SCALE_END);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mSplashImage, "scaleY", 1f, SCALE_END);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(ANIMATION_TIME).play(animatorX).with(animatorY);
        set.start();

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!prefManager.isAutoLogin()) {
                    startActivity(new Intent(SplashActivity.this, Login.class));
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("name", "517021910320");
                    intent.putExtra("password", "517021910320");
                    startActivity(intent);
                }
                SplashActivity.this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // do nothing
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CountDownTimer countDown = new CountDownTimer(countDownTime, countDownInterval) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            startAnim();
                        }
                    };
                    countDown.start();
                } else {
                    Toast.makeText(this, "申请权限失败", Toast.LENGTH_SHORT).show();
                    SplashActivity.this.finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("确定", okListener)
                .setNegativeButton("取消", cancelListener)
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SplashActivity.this, "申请权限失败", Toast.LENGTH_SHORT).show();
                    showMessageOKCancel("应用需要储存权限, 前往设置允许权限", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, 123);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SplashActivity.this, "申请权限失败", Toast.LENGTH_SHORT).show();
                            SplashActivity.this.finish();
                        }
                    });
                } else {
                    Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
                    CountDownTimer countDown = new CountDownTimer(countDownTime, countDownInterval) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            startAnim();
                        }
                    };
                    countDown.start();
                }
            }
        }
    }
}
