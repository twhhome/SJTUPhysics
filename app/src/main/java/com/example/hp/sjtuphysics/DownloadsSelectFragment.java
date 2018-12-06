package com.example.hp.sjtuphysics;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class DownloadsSelectFragment extends Fragment {

    private Bundle files = new Bundle();

    private int index;

    private boolean checkedPositions[];
    private int checkedItems = 0;

    private RecyclerView DownloadsSelectList;
    private RadioButton chooseAll;
    private LinearLayout chooseAllLL;
    private TextView deleteTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View DownloadsSelectView = inflater.inflate(R.layout.fragment_downloads_select, container, false);
        Bundle bundle = getArguments();
        files = bundle.getBundle("files");
        index = bundle.getInt("index");

        checkedPositions = new boolean[files.size()];

        checkedPositions[index] = true;
        checkedItems++;

        DownloadsSelectList = DownloadsSelectView.findViewById(R.id.DownloadsSelectList);
        chooseAll = DownloadsSelectView.findViewById(R.id.chooseAll);
        chooseAllLL = DownloadsSelectView.findViewById(R.id.chooseAllLL);
        deleteTV = DownloadsSelectView.findViewById(R.id.deleteTV);

        deleteTV.setText("删除(1)");

        DownloadsSelectList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        final DownloadsSelectAdapter downloadsSelectAdapter = new DownloadsSelectAdapter(getActivity(), getActivity(), files, checkedPositions);
        downloadsSelectAdapter.setOnCheckChangeListener(new DownloadsSelectAdapter.OnCheckChangeListener() {
            @Override
            public void onCheckChangeListener(int position, boolean isChecked) {
                if(isChecked) {
                    checkedPositions[position] = true;
                    checkedItems++;
                } else {
                    checkedPositions[position] = false;
                    checkedItems--;
                }

                if(checkedItems == 0) {
                    deleteTV.setText("删除");
                    deleteTV.setEnabled(false);
                    deleteTV.setTextColor(Color.parseColor("#000000"));
                } else {
                    deleteTV.setText("删除(" + String.valueOf(checkedItems) + ")");
                    deleteTV.setTextColor(Color.parseColor("#FF0000"));
                    deleteTV.setEnabled(true);
                }
            }
        });
        DownloadsSelectList.setAdapter(downloadsSelectAdapter);

        chooseAllLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = chooseAll.isChecked();
                chooseAll.setChecked(!state);
            }
        });

        chooseAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    for(int i = 0; i < files.size(); i++) {
                        checkedPositions[i] = true;
                    }
                    checkedItems = files.size();

                    downloadsSelectAdapter.notifyDataSetChanged();
                    deleteTV.setText("删除(" + String.valueOf(checkedItems) + ")");
                    deleteTV.setTextColor(Color.parseColor("#FF0000"));
                    deleteTV.setEnabled(true);
                } else {
                    for(int i = 0; i < files.size(); i++) {
                        checkedPositions[i] = false;
                    }
                    checkedItems = 0;

                    downloadsSelectAdapter.notifyDataSetChanged();
                    deleteTV.setText("删除");
                    deleteTV.setEnabled(false);
                    deleteTV.setTextColor(Color.parseColor("#000000"));
                }
            }
        });

        deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < files.size(); i++) {
                    if(checkedPositions[i]) {
                        DownloadFile downloadFile = files.getParcelable(String.valueOf(i));
                        File file = new File(downloadFile.getFilePath());
                        if(file.exists()) {
                            if(!file.delete()) {
                                Toast.makeText(getActivity(), "删除文件失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "文件不存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                getActivity().onKeyDown(KeyEvent.KEYCODE_BACK, null);
            }
        });

        return DownloadsSelectView;
    }
}
