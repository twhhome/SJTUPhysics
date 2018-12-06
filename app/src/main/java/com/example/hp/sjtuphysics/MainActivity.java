package com.example.hp.sjtuphysics;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private MainPageFragment mainPageFragment;
    private CourseWorkFragment courseWorkFragment;
    private DocumentFragment documentFragment;
    private FragmentManager fragmentManager;

    private String name;
    private String password;

    private static final String[] dataNames = { "__EVENTTARGET", "__EVENTARGUMENT", "__VIEWSTATE", "__VIEWSTATEGENERATOR", "__EVENTVALIDATION"};

    private String html;
    private HashMap<String, String> dataMap = new HashMap<>();
    private Bundle cookieBundle = new Bundle();

    private long exitTime;

    public Handler handler;

    private long countDownTime = 10000;
    private long countDownInterval = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        password = intent.getStringExtra("password");

        fragmentManager = getSupportFragmentManager();

        initViews();

        final LoadingDialog dialog = new LoadingDialog(this, "正在登录");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                html = msg.obj.toString();
            }
        };

        final Handler refreshHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dialog.close();
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View headerView = navigationView.getHeaderView(0);
                TextView UserNameTV = headerView.findViewById(R.id.UserNameTV);
                TextView CourseNumberTV = headerView.findViewById(R.id.CourseNumberTV);
                UserNameTV.setText(Jsoup.parse(html).getElementById("Menu2_Label2").text().split("：", 2)[0]);
                CourseNumberTV.setText("课号：" + Jsoup.parse(html).getElementById("Menu1_lblClass").text());
                setNavigationItemSelection(R.id.MainPage);
            }
        };

        final CountDownTimer countDown = new CountDownTimer(countDownTime,countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                dialog.close();
                Toast.makeText(MainActivity.this,"请求超时，请重试", Toast.LENGTH_SHORT).show();
                refreshHandler.removeCallbacksAndMessages(null);
            }
        };
        countDown.start();

        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(getDatas()) {
                    executeSignIn();
                    refreshHandler.sendEmptyMessage(0);
                    countDown.cancel();
                }
            }
        }).start();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.MainPage);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            logoutApp();
        }
    }

    private void logoutApp() {
        if(System.currentTimeMillis() - exitTime > 2000) {
            FrameLayout frameLayout = findViewById(R.id.Content);
            Snackbar.make(frameLayout, "再按一次退出大物教学系统", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_downloads) {
            startActivity(new Intent(MainActivity.this, DownloadsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        setNavigationItemSelection(id);
        if(id != R.id.Settings) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void setNavigationItemSelection(int id) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(id != R.id.Settings) {
            hideFragments(transaction);
        }
        Bundle bundle = new Bundle();
        switch(id) {
            case R.id.MainPage:
                if(mainPageFragment == null) {
                    mainPageFragment = new MainPageFragment();
                    transaction.add(R.id.Content, mainPageFragment);
                } else {
                    transaction.show(mainPageFragment);
                }
                bundle.putString("html", html);
                bundle.putBundle("cookieBundle", cookieBundle);
                mainPageFragment.setArguments(bundle);
                break;
            case R.id.CourseWork:
                if(courseWorkFragment == null) {
                    courseWorkFragment = new CourseWorkFragment();
                    transaction.add(R.id.Content, courseWorkFragment);
                } else {
                    transaction.show(courseWorkFragment);
                }
                bundle.putBundle("cookieBundle", cookieBundle);
                courseWorkFragment.setArguments(bundle);
                break;
            case R.id.Documents:
                if(documentFragment == null) {
                    documentFragment = new DocumentFragment();
                    transaction.add(R.id.Content, documentFragment);
                } else {
                    transaction.show(documentFragment);
                }
                bundle.putBundle("cookieBundle", cookieBundle);
                documentFragment.setArguments(bundle);
                break;
            case R.id.Settings:
                startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), 1);
                break;
        }
        transaction.commit();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if(mainPageFragment != null) {
            transaction.hide(mainPageFragment);
        }
        if(courseWorkFragment != null) {
            transaction.hide(courseWorkFragment);
        }
        if(documentFragment != null) {
            transaction.hide(documentFragment);
        }
    }

    private boolean getDatas() {
        String path = "http://phycai.sjtu.edu.cn/wis/default.aspx";
        try {
            OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();
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
                String html = response.body().string();
                org.jsoup.nodes.Document doc = Jsoup.parse(html);
                for(int i = 0; i < dataNames.length; i++) {
                    String dataName = dataNames[i];
                    String val = doc.getElementById(dataName).val();
                    if(dataMap.containsKey(dataName))
                    {
                        dataMap.remove(dataName);
                        dataMap.put(dataName, val);
                    } else {
                        dataMap.put(dataName, val);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }

    private boolean executeSignIn() {
        String path = "http://phycai.sjtu.edu.cn/wis/default.aspx";
        try {
            OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();
            FormBody.Builder formBody = new FormBody.Builder();
            //String name = Name.getText().toString();
            //String password = Password.getText().toString();
            for(int i = 0; i < dataMap.size(); i++) {
                formBody.add(dataNames[i], dataMap.get(dataNames[i]));
            }
            formBody.add("login1:usern", name);
            formBody.add("login1:pass", password);
            formBody.add("login1:user", "2");
            formBody.add("login1:btnLogin", "登   录");
            Request request = new Request.Builder()
                    .url(path)
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .addHeader("Cache-Control", "max-age=0")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Content-Length", "772")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Host", "phycai.sjtu.edu.cn")
                    .addHeader("Origin", "http://phycai.sjtu.edu.cn")
                    .addHeader("Referer", "http://phycai.sjtu.edu.cn/wis/default.aspx")
                    .addHeader("Upgrade-Insecure-Requests", "1")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                    .post(formBody.build())
                    .build();
            Response response = client.newCall(request).execute();
            if(response.code() == 302) {
                List<String> rawCookies = response.headers().values("Set-Cookie");
                ArrayList<String> cookies = new ArrayList<>();
                if(rawCookies.size() > 0) {
                    for(String cookie : rawCookies) {
                        cookies.add(cookie.split(";")[0]);
                    }
                    getSingleCookie(cookies);
                }
                String rLocation = response.header("Location");
                String cookieStr = ".WIS=" + cookieBundle.get(".WIS") + "; " +
                        "useridentity=" + cookieBundle.get("useridentity") + "; " +
                        "studentid=" + cookieBundle.get("studentid") + "; " +
                        "studentname=" + cookieBundle.get("studentname") + "; " +
                        "ausername=" + cookieBundle.get("ausername") + "; " +
                        "studentnumber=" + cookieBundle.get("studentnumber") + "; " +
                        "jusername=" + cookieBundle.get("jusername") + "; " +
                        "teacherid=" + cookieBundle.get("teacherid") + "; " +
                        "juserpower=" + cookieBundle.get("juserpower") + "; " +
                        "stucourseid=" + cookieBundle.get("stucourseid");
                request = new Request.Builder()
                        .url(rLocation)
                        .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .addHeader("Accept-Encoding", "gzip, deflate")
                        .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .addHeader("Cache-Control", "max-age=0")
                        .addHeader("Connection", "keep-alive")
                        .addHeader("Host", "phycai.sjtu.edu.cn")
                        .addHeader("Referer", "http://phycai.sjtu.edu.cn/wis/default.aspx")
                        .addHeader("Upgrade-Insecure-Requests", "1")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                        .addHeader("Cookie", cookieStr)
                        .build();
                response = client.newCall(request).execute();
                if(response.code() == 302) {
                    rawCookies = response.headers().values("Set-Cookie");
                    cookies = new ArrayList<>();
                    if(rawCookies.size() > 0) {
                        for(String cookie : rawCookies) {
                            cookies.add(cookie.split(";")[0]);
                        }
                        getSingleCookie(cookies);
                    }
                    rLocation = response.header("Location");
                    cookieStr = ".WIS=" + cookieBundle.get(".WIS") + "; " +
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
                    request = new Request.Builder()
                            .url(rLocation)
                            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                            .addHeader("Accept-Encoding", "gzip, deflate")
                            .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                            .addHeader("Cache-Control", "max-age=0")
                            .addHeader("Connection", "keep-alive")
                            .addHeader("Host", "phycai.sjtu.edu.cn")
                            .addHeader("Referer", "http://phycai.sjtu.edu.cn/wis/default.aspx")
                            .addHeader("Upgrade-Insecure-Requests", "1")
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36")
                            .addHeader("Cookie", cookieStr)
                            .build();
                    response = client.newCall(request).execute();
                    if(response.code() == 200) {
                        Message message = Message.obtain();
                        message.what = 0;
                        message.obj = response.body().string();
                        handler.sendMessage(message);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }

    private void getSingleCookie(ArrayList<String> cookies) {
        if(cookies.size() > 0) {
            for(int i = 0; i < cookies.size(); i++) {
                String cookie = cookies.get(i);
                String name, value;
                name = cookie.split("=",2)[0];
                value = cookie.split("=",2)[1];
                if(cookieBundle.containsKey(name)) {
                    cookieBundle.remove(name);
                    cookieBundle.putString(name, value);
                } else {
                    cookieBundle.putString(name, value);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            if(resultCode == 1) {
                startActivity(new Intent(MainActivity.this, Login.class));
                MainActivity.this.finish();
            }
        }
    }
}
