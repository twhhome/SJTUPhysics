package com.example.hp.sjtuphysics;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DownloadsSelectAdapter extends RecyclerView.Adapter<DownloadsSelectAdapter.ViewHolder> {

    private Context context;
    private FragmentActivity fragmentActivity;
    private Bundle files;
    private LayoutInflater layoutInflater;
    private boolean[] checkedPositions;

    public interface OnCheckChangeListener {
        void onCheckChangeListener(int position, boolean isChecked);
    }

    private OnCheckChangeListener onCheckChangeListener;

    public void setOnCheckChangeListener(OnCheckChangeListener onCheckChangeListener) {
        this.onCheckChangeListener = onCheckChangeListener;
    }

    public DownloadsSelectAdapter(Context context, FragmentActivity fragmentActivity, Bundle files, boolean[] checkedPositions) {
        this.context = context;
        this.fragmentActivity = fragmentActivity;
        this.files = files;
        this.checkedPositions = checkedPositions;
        layoutInflater = LayoutInflater.from(this.context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RadioButton radioButton;
        private ImageView fileIcon;
        private TextView fileName;
        private TextView fileSize;
        private RelativeLayout DownloadsSelectRL;

        public ViewHolder(View v) {
            super(v);
            radioButton = v.findViewById(R.id.radioButton);
            fileIcon = v.findViewById(R.id.fileIcon2);
            fileName = v.findViewById(R.id.fileName2);
            fileSize = v.findViewById(R.id.fileSize2);
            DownloadsSelectRL = v.findViewById(R.id.DownloadsSelectRL);
        }
    }

    @Override
    public void onBindViewHolder(final DownloadsSelectAdapter.ViewHolder viewHolder, final int position) {
        final DownloadFile file = files.getParcelable(String.valueOf(position));
        viewHolder.fileName.setText(file.getFileName());
        viewHolder.fileSize.setText(file.getFileSize());
        switch (file.getFileType()) {
            case ".doc":
                viewHolder.fileIcon.setImageResource(R.drawable.word);
                break;
            case ".ppt":
                viewHolder.fileIcon.setImageResource(R.drawable.ppt);
                break;
            case ".xls":
                viewHolder.fileIcon.setImageResource(R.drawable.excel);
                break;
            case ".pdf":
                viewHolder.fileIcon.setImageResource(R.drawable.pdf);
                break;
        }
        if(checkedPositions[position]) {
            viewHolder.radioButton.setChecked(true);
        } else {
            viewHolder.radioButton.setChecked(false);
        }
        viewHolder.DownloadsSelectRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = viewHolder.radioButton.isChecked();
                viewHolder.radioButton.setChecked(!state);
                if(onCheckChangeListener!= null) {
                    onCheckChangeListener.onCheckChangeListener(position, !state);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public DownloadsSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View downloadsSelectView = layoutInflater.inflate(R.layout.downloads_select, viewGroup, false);
        return new ViewHolder(downloadsSelectView);
    }
}
