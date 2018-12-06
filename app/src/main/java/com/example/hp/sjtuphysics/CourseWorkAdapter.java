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
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
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

public class CourseWorkAdapter extends RecyclerView.Adapter<CourseWorkAdapter.ViewHolder> {

    private static final int NOTIFICATION_ID = 11;
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

    private int splitTVId = 1000;

    private FragmentActivity fragmentActivity;
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<CourseWork> CourseWorks;
    private Bundle cookieBundle;

    private PrefManager prefManager;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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

    public CourseWorkAdapter(Context context, FragmentActivity fragmentActivity, ArrayList<CourseWork> courseWorks, Bundle cookieBundle) {
        prefManager = new PrefManager(context);
        this.context = context;
        this.fragmentActivity = fragmentActivity;
        this.CourseWorks = courseWorks;
        this.cookieBundle = cookieBundle;
        layoutInflater = LayoutInflater.from(this.context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView WeekTV;
        private TextView ContentTV;
        private LinearLayout CourseWorkDetailLL;
        View courseWorkDetailView;

        public ViewHolder(View v) {
            super(v);
            this.courseWorkDetailView = v;
            WeekTV = v.findViewById(R.id.WeekTV);
            ContentTV = v.findViewById(R.id.ContentTV);
            CourseWorkDetailLL = v.findViewById(R.id.CourseWorkDetailLL);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.i(TAG, String.valueOf(this.getItemViewType(position)));
        CourseWork courseWork = CourseWorks.get(position);
        viewHolder = new ViewHolder(viewHolder.courseWorkDetailView);
        viewHolder.WeekTV.setText(courseWork.getWeek());
        viewHolder.ContentTV.setText(Html.fromHtml(courseWork.getContent()));

        if(courseWork.getLinks() != null && viewHolder.courseWorkDetailView.findViewById(splitTVId) == null) {
            TextView splitTV = new TextView(this.context);
            splitTV.setId(splitTVId);
            LinearLayout.LayoutParams splitTVLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            splitTVLayoutParams.topMargin = 3;
            splitTVLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            splitTVLayoutParams.weight = 1;
            splitTV.setBackgroundColor(Color.parseColor("#616161"));
            splitTV.setLayoutParams(splitTVLayoutParams);
            viewHolder.CourseWorkDetailLL.addView(splitTV);

            LinearLayout linearLayout = new LinearLayout(this.context);
            LinearLayout.LayoutParams linearLayoutLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayoutLayoutParams.topMargin = 5;
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setLayoutParams(linearLayoutLayoutParams);

            TextView linkTitleTV = new TextView(this.context);
            LinearLayout.LayoutParams linkTitleTVLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linkTitleTV.setText("链接：");
            linkTitleTV.setTextSize(12);
            linkTitleTV.setLayoutParams(linkTitleTVLayoutParams);
            linearLayout.addView(linkTitleTV);

            LinearLayout linksLayout = new LinearLayout(this.context);
            LinearLayout.LayoutParams linksLayoutLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linksLayout.setOrientation(LinearLayout.VERTICAL);
            linksLayout.setLayoutParams(linksLayoutLayoutParams);

            for(int i = 0; i < courseWork.getLinks().size(); i++) {
                TextView linkTV = new TextView(this.context);
                LinearLayout.LayoutParams linkTVLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if(i != 0) {
                    linkTVLayoutParams.topMargin = 5;
                }
                linkTV.setTextAppearance(this.context, R.style.LinkText);
                Link link = courseWork.getLinks().getParcelable(String.valueOf(i));
                linkTV.setText(link.getName());
                final String linkName = link.getName();
                final String linkUrl = link.getUrl();
                linkTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(fragmentActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            fragmentActivity.requestPermissions(PERMISSIONS_STORAGE, 2);
                        } else {
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
                linkTV.setLayoutParams(linkTVLayoutParams);
                linksLayout.addView(linkTV);
            }

            linearLayout.addView(linksLayout);

            viewHolder.CourseWorkDetailLL.addView(linearLayout);
        }
    }

    @Override
    public int getItemCount() {
        return CourseWorks.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View courseWorkView = layoutInflater.inflate(R.layout.course_work_details, viewGroup, false);
        return new ViewHolder(courseWorkView);
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
                if (response.code() == 200) {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;

                    File dir = new File(FilePath);
                    if (!dir.exists()) {
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
                        while ((len = is.read(buf)) != -1) {
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
                            if (is != null) {
                                is.close();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                        try {
                            if (fos != null) {
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
