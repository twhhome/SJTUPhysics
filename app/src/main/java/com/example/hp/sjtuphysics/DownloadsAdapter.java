package com.example.hp.sjtuphysics;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

    private Context context;
    private FragmentActivity fragmentActivity;
    private Bundle files;
    private LayoutInflater layoutInflater;

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int index);
    }

    private OnItemLongClickListener onItemLongClickListener;

    public DownloadsAdapter(Context context, FragmentActivity fragmentActivity, Bundle files) {
        this.context = context;
        this.fragmentActivity = fragmentActivity;
        this.files = files;
        layoutInflater = LayoutInflater.from(this.context);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView fileIcon;
        private TextView fileName;
        private TextView fileSize;
        private CardView DownloadsDetailCV;

        public ViewHolder(View v) {
            super(v);
            fileIcon = v.findViewById(R.id.fileIcon);
            fileName = v.findViewById(R.id.fileName);
            fileSize = v.findViewById(R.id.fileSize);
            DownloadsDetailCV = v.findViewById(R.id.DownloadsDetailCV);
        }
    }

    @Override
    public void onBindViewHolder(DownloadsAdapter.ViewHolder viewHolder, final int position) {
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
        viewHolder.DownloadsDetailCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile(file.getFilePath());
            }
        });
        viewHolder.DownloadsDetailCV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(context, "long click", Toast.LENGTH_SHORT).show();
                if(onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(v, position);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    @Override
    public DownloadsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View downloadsView = layoutInflater.inflate(R.layout.downloads_detail, viewGroup, false);
        return new ViewHolder(downloadsView);
    }

    private void openFile(String path) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(path));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = getFileType(path);
        intent.setDataAndType(uri, type);
        this.context.startActivity(intent);
    }

    private String getFileType(String path) {
        String type = "*/*";
        String fName = path;

        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0) {
            return type;
        }

        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if(end.equals("")) {
            return type;
        }

        if(end.equals(".doc")) {
            type = "application/msword";
        } else if(end.equals(".ppt")) {
            type = "application/vnd.ms-powerpoint";
        } else if(end.equals(".xls")) {
            type = "application/vnd.ms-excel";
        } else if(end.equals(".pdf")) {
            type = "application/pdf";
        }

        return type;
    }
}
