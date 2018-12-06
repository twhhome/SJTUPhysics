package com.example.hp.sjtuphysics;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.text.DecimalFormat;

public class DownloadsActivity extends AppCompatActivity implements DownloadsFragment.FragmentInteraction {

    private Bundle files = new Bundle();

    private PrefManager prefManager;

    private DownloadsFragment downloadsFragment;
    private DownloadsSelectFragment downloadsSelectFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        prefManager = new PrefManager(this);
        String directory = prefManager.getDownloadDirectory();
        files = getFiles(directory);

        fragmentManager = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("已下载文件");
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        downloadsFragment = new DownloadsFragment();
        transaction.add(R.id.downloads, downloadsFragment);
        Bundle bundle = new Bundle();
        bundle.putBundle("files", files);
        downloadsFragment.setArguments(bundle);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onKeyDown(KeyEvent.KEYCODE_BACK, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if (downloadsSelectFragment != null) {
                downloadsSelectFragment = null;
                fragmentManager.popBackStack();

                String directory = prefManager.getDownloadDirectory();
                files = getFiles(directory);
                Bundle bundle = new Bundle();
                bundle.putBundle("files", files);

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.detach(downloadsFragment);
                downloadsFragment.setArguments(bundle);
                transaction.attach(downloadsFragment);
                transaction.commit();
            } else {
                DownloadsActivity.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void longClick(int index) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        if(downloadsSelectFragment == null) {
            downloadsSelectFragment = new DownloadsSelectFragment();
            transaction.add(R.id.downloads, downloadsSelectFragment);
            transaction.addToBackStack(null);
            Bundle bundle = new Bundle();
            bundle.putBundle("files", files);
            bundle.putInt("index", index);
            downloadsSelectFragment.setArguments(bundle);
            transaction.commit();
        }
    }

    private void hideFragments(FragmentTransaction transaction) {
        if(downloadsFragment != null) {
            transaction.hide(downloadsFragment);
        }
        if(downloadsSelectFragment != null) {
            transaction.hide(downloadsSelectFragment);
        }
    }

    private Bundle getFiles(String directory) {
        File file = new File(directory);
        File[] subFile = file.listFiles();
        Bundle files = new Bundle();

        for(int i = 0; i < subFile.length; i++) {
            for(int j = i + 1; j < subFile.length; j++) {
                if(subFile[i].lastModified() < subFile[j].lastModified()) {
                    File temp = subFile[i];
                    subFile[i] = subFile[j];
                    subFile[j] = temp;
                }
            }
        }

        for(int i = 0; i < subFile.length; i++) {
            if(!subFile[i].isDirectory()) {
                String filePath = subFile[i].getAbsolutePath();
                String fileName = subFile[i].getName();
                String fileSize = getFileSize(subFile[i].length());
                String fileType = getFileType(fileName);
                DownloadFile downloadFile = new DownloadFile(filePath, fileName, fileSize, fileType);
                //files.add(downloadFile);
                files.putParcelable(String.valueOf(i), downloadFile);
            }
        }

        return files;
    }

    private String getFileType(String name) {
        String type = "";
        String fName = name;

        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0) {
            return type;
        }

        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if(end.equals("")) {
            return type;
        } else {
            return end;
        }
    }

    private String getFileSize(long bytes) {
        DecimalFormat decimalFormat = new DecimalFormat(".00");
        int count = 0;
        double temp = bytes;
        while(true) {
            if((long)temp / 1024 > 0) {
                temp = temp / 1024.0;
                count++;
            } else {
                break;
            }
        }
        String result = decimalFormat.format(temp);
        switch (count) {
            case 0:
                result += "B";
                break;
            case 1:
                result += "KB";
                break;
            case 2:
                result += "MB";
                break;
            case 3:
                result += "GB";
                break;
            case 4:
                result += "TB";
                break;
        }
        return result;
    }
}
