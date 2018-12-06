package com.example.hp.sjtuphysics;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private static final int NOTIFICATION_ID = 10;
    private static final String CHANNEL_ID = "Download";
    private static final String CHANNEL_NAME = "下载";

    private final static String TAG = "MainActivity";

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private Notification notification;

    private FragmentActivity fragmentActivity;
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Document> Documents;
    private Bundle cookieBundle;

    private PrefManager prefManager;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //dialog.close();
            if(msg.what == 1) {
                notificationManager.cancel(NOTIFICATION_ID);
                Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
                Bundle bundle = msg.getData();
                final String filePath = bundle.getString("filePath");
                String fileName = bundle.getString("fileName");
                showMessageOKCancel(fileName + " 已下载完成。是否打开？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openFile(filePath);
                    }
                }, null);
            }
            else
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
        }
    };

    public DocumentAdapter(Context context, FragmentActivity fragmentActivity, ArrayList<Document> documents, Bundle cookieBundle) {
        prefManager = new PrefManager(context);
        this.context = context;
        this.fragmentActivity = fragmentActivity;
        this.Documents = documents;
        this.cookieBundle = cookieBundle;
        layoutInflater = LayoutInflater.from(this.context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView documentNameTV;
        private TextView documentSizeTV;
        private TextView documentDescriptionTV;
        private TextView documentDateTV;
        private LinearLayout documentDownloadLL;
        private LinearLayout documentDetailLL;
        View documentDetailView;

        public ViewHolder(View v) {
            super(v);
            documentDetailView = v;
            documentNameTV = v.findViewById(R.id.DocumentNameTV);
            documentSizeTV = v.findViewById(R.id.DocumentSizeTV);
            documentDescriptionTV = v.findViewById(R.id.DocumentDescriptionTV);
            documentDateTV = v.findViewById(R.id.DocumentDateTV);
            documentDownloadLL = v.findViewById(R.id.DocumentDownloadLL);
            documentDetailLL = v.findViewById(R.id.DocumentDetailLL);
        }
    }

    @Override
    public void onBindViewHolder(DocumentAdapter.ViewHolder viewHolder, int position) {
        final Document document = Documents.get(position);
        viewHolder.documentNameTV.setText(document.getName());
        viewHolder.documentSizeTV.setText("(" + document.getSize() + ")");
        viewHolder.documentDescriptionTV.setText(document.getDescription());
        viewHolder.documentDateTV.setText(document.getDate());
        viewHolder.documentDownloadLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragmentActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    fragmentActivity.requestPermissions(PERMISSIONS_STORAGE, 2);
                } else {
                    final String linkUrl = document.getUrl();
                    final String linkName = document.getName();
                    Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadFiles("http://phycai.sjtu.edu.cn" + linkUrl, linkName, prefManager.getDownloadDirectory());
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return Documents.size();
    }

    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View documentView = layoutInflater.inflate(R.layout.document_details, viewGroup, false);
        return new ViewHolder(documentView);
    }

    private void downloadFiles(String path, String fileName, String filePath) {
        final String FileName = fileName;
        final String FilePath = filePath;
        OkHttpClient client = new OkHttpClient.Builder().build();
        String cookieStr = ".WIS=" + cookieBundle.get(".WIS") + "; " +
                "useridentity=" + cookieBundle.get("useridentity") + "; " +
                "studentid=" + cookieBundle.get("studentid") + "; " +
                "studentname=" + cookieBundle.get("studentname") + "; " +
                "ausername=" + cookieBundle.get("ausername") + "; " +
                "studentnumber=" + cookieBundle.get("studentnumber") + "; " +
                "jusername=" + cookieBundle.get("jusername") + "; " +
                "teacherid=" + cookieBundle.get("teacherid") + "; " +
                "juserpower=" + cookieBundle.get("juserpower") + "; " +
                "stucourseid=" + cookieBundle.get("stucourseid") + "; " +
                "courteaconnid=" + cookieBundle.get("courteaconnid");
        Request request = new Request.Builder()
                .url(path)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .addHeader("Connection", "keep-alive")
                .addHeader("Host", "phycai.sjtu.edu.cn")
                .addHeader("Referer", "http://phycai.sjtu.edu.cn/wis/student/studentcoursehostnew.aspx")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                .addHeader("Cookie", cookieStr)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.code() == 200) {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;

                    File dir = new File(FilePath);
                    if(!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, FileName);
                    try {
                        is = response.body().byteStream();
                        long total = response.body().contentLength();
                        fos = new FileOutputStream(file);
                        long sum = 0;
                        int lastProgress = 0;
                        showNotification(0, file.getName());
                        while((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            sum += len;
                            int progress = (int) (sum * 1.0f / total * 100);
                            if(progress == lastProgress + 10) {
                                updateProgress(progress);
                                lastProgress = progress;
                            }
                        }
                        fos.flush();
                        //下载成功
                        Bundle bundle = new Bundle();
                        bundle.putString("filePath", file.getAbsolutePath());
                        bundle.putString("fileName", file.getName());
                        Message message = Message.obtain();
                        message.what = 1;
                        message.setData(bundle);
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        //下载失败
                        Message message = Message.obtain();
                        message.what = 0;
                        handler.sendMessage(message);
                    } finally {
                        try {
                            if(is != null) {
                                is.close();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                        try {
                            if(fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            }
        });
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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("确定", okListener)
                .setNegativeButton("取消", cancelListener)
                .setCancelable(true)
                .create()
                .show();
    }

    private void showNotification(int progress, String fileName) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mBuilder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(channel);

            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
            mBuilder.setProgress(100, progress, false);
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setContentText(String.valueOf(progress) + "%");
            mBuilder.setContentTitle(fileName);
            mBuilder.setOngoing(true);
            mBuilder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
            mBuilder.setAutoCancel(true);

            Intent intent = new Intent(context, DocumentAdapter.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

            notification = mBuilder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void updateProgress(int progress) {
        mBuilder.setProgress(100, progress, false);
        mBuilder.setContentText(String.valueOf(progress) + "%");
        notification = mBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
