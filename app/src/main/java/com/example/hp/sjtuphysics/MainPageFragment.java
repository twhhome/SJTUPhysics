package com.example.hp.sjtuphysics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainPageFragment extends Fragment {

    private static final String TAG = "MainActivity";

    private static final String[] hiddenDataNames = { "__VIEWSTATE", "__VIEWSTATEGENERATOR"};

    private String html;
    private HashMap<String, String> hiddenDataMap = new HashMap<>();
    private HashMap<String, String> cookieMap = new HashMap<>();
    private Bundle cookieBundle = new Bundle();

    private String date;
    private String weekDate;
    private String name;
    private String teacherName;
    private String intro;

    private TextView dateTV;
    private TextView weekDateTV;
    private TextView NameTV;
    private TextView teacherNameTV;
    private TextView IntroTV;
    private ImageView imageView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Handler handler;

    private long countDownTime = 10000;
    private long countDownInterval = 1;

    private String time;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View MainPageView = inflater.inflate(R.layout.fragment_main_page, container, false);
        Bundle bundle = getArguments();
        if(bundle != null) {
            html = bundle.getString("html");
            cookieBundle = bundle.getBundle("cookieBundle");
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] Pic = (byte[])msg.obj;
                Bitmap bitmap = BitmapFactory.decodeByteArray(Pic, 0, Pic.length);
                imageView.setImageBitmap(bitmap);
            }
        };

        swipeRefreshLayout = MainPageView.findViewById(R.id.MainPageSwipeRefresh);
        dateTV = MainPageView.findViewById(R.id.dateTV);
        weekDateTV = MainPageView.findViewById(R.id.weekDateTV);
        NameTV = MainPageView.findViewById(R.id.NameTV);
        teacherNameTV = MainPageView.findViewById(R.id.TeacherNameTV);
        IntroTV = MainPageView.findViewById(R.id.IntroTV);
        imageView = MainPageView.findViewById(R.id.ImageView);

        handleHtml();

        final Handler refreshHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                swipeRefreshLayout.setRefreshing(false);
                //Toast.makeText(getActivity(), time, Toast.LENGTH_SHORT).show();
                handleHtml();
            }
        };

        final CountDownTimer countDown = new CountDownTimer(countDownTime,countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(),"请求超时，请重试",Toast.LENGTH_SHORT).show();
                refreshHandler.removeCallbacksAndMessages(null);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countDown.start();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getMainPage("http://phycai.sjtu.edu.cn/wis/student/studentcoursehostnew.aspx?columnid=M03");
                                refreshHandler.sendEmptyMessage(0);
                                countDown.cancel();
                            }
                        }).start();
                    }
                }, 500);
            }
        });

        return MainPageView;
    }

    private void getMainPage(String path) {
        try {
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
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                html = response.body().string();
                time = response.header("Date");
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void handleHtml() {
        //Toast.makeText(getActivity(), "handle html", Toast.LENGTH_SHORT).show();
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        for(int i = 0; i < hiddenDataNames.length; i++) {
            String dataName = hiddenDataNames[i];
            String val = doc.getElementById(dataName).val();
            if(hiddenDataMap.containsKey(dataName))
            {
                hiddenDataMap.remove(dataName);
                hiddenDataMap.put(dataName, val);
            } else {
                hiddenDataMap.put(dataName, val);
            }
        }
        String str = doc.getElementById("top").getElementById("topwrapper").select("span").get(0).text();
        String[] temp = str.split(" ", 2);
        date = temp[0];
        weekDate = temp[1];
        dateTV.setText(date);
        weekDateTV.setText(weekDate);

        name = doc.getElementById("Menu2_Label2").text();
        teacherName = doc.getElementById("Menu2_Label4").text();
        NameTV.setText(name);
        teacherNameTV.setText(teacherName);

        intro = doc.select("div.listul").get(0).text();
        intro = intro.replaceAll("。 ", "。\n\n");
        IntroTV.setText(intro);

        final String picUrl;
        picUrl = "http://phycai.sjtu.edu.cn/wis/" + doc.getElementById("Menu2_imgCal").attr("src").split("/", 2)[1];

        new Thread(new Runnable() {
            @Override
            public void run() {
                getPicture(picUrl);
            }
        }).start();
    }

    private void getPicture(String path) {
        try {
            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder()
                    .url(path)
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Host", "phycai.sjtu.edu.cn")
                    .addHeader("Upgrade-Insecure-Requests", "1")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                    .build();
            Response response = client.newCall(request).execute();
            if(response.code() == 200) {
                byte[] Picbyte = response.body().bytes();
                Message message = Message.obtain();
                message.obj = Picbyte;
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
