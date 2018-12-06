package com.example.hp.sjtuphysics;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DocumentFragment extends Fragment {

    private static final String TAG = "MainActivity";

    public ArrayList<Document> documents = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView DocumentList;

    private Bundle cookieBundle = new Bundle();

    private long countDownTime = 10000;
    private long countDownInterval = 1;

    private String time;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Document document = msg.getData().getParcelable("document");
            documents.add(document);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View DocumentView = inflater.inflate(R.layout.fragment_document, container, false);
        Bundle bundle = getArguments();
        if(bundle != null) {
            cookieBundle = bundle.getBundle("cookieBundle");
        }

        swipeRefreshLayout = DocumentView.findViewById(R.id.DocumentSwipeRefresh);

        DocumentList = DocumentView.findViewById(R.id.DocumentList);
        DocumentList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        final DocumentAdapter documentAdapter = new DocumentAdapter(getActivity(), getActivity(), documents, cookieBundle);
        DocumentList.setAdapter(documentAdapter);

        final Handler refreshHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                swipeRefreshLayout.setRefreshing(false);
                //Toast.makeText(getActivity(), time, Toast.LENGTH_SHORT).show();
                documentAdapter.notifyDataSetChanged();
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
                                getDocuments("http://phycai.sjtu.edu.cn/wis/course/stucoursefiledownload.aspx?columnid=M15");
                                refreshHandler.sendEmptyMessage(0);
                                countDown.cancel();
                            }
                        }).start();
                    }
                }, 500);
            }
        });

        swipeRefreshLayout.setRefreshing(true);
        countDown.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDocuments("http://phycai.sjtu.edu.cn/wis/course/stucoursefiledownload.aspx?columnid=M15");
                refreshHandler.sendEmptyMessage(0);
                countDown.cancel();
            }
        }).start();

        return DocumentView;
    }

    private void getDocuments(String path) {
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
            if(response.code() == 200) {
                time = response.header("Date");
                String html = response.body().string();
                org.jsoup.nodes.Document doc = Jsoup.parse(html);
                Elements elements = doc.select("table").get(1).select("tr");
                for(int i = 1; i < elements.size(); i++) {
                    org.jsoup.nodes.Element element = elements.get(i);
                    String name = element.select("a").get(0).text();
                    String url = element.select("a").get(0).attr("href");
                    String description = element.select("td").get(1).text();
                    String size = element.select("div").get(0).text();
                    String date = element.select("td").get(3).text();
                    Document document = new Document(name, description, size, date, url);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("document", document);
                    Message message = Message.obtain();
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
                elements = doc.select("table");
                org.jsoup.nodes.Element element = elements.get(elements.size() - 1);
                int Pages = Integer.parseInt(element.select("td").get(0).select("b").get(1).text());
                int currPage = Integer.parseInt(element.select("td").get(0).select("b").get(2).text());
                if(currPage < Pages) {
                    Elements links = element.select("td").get(1).select("a");
                    String link = "";
                    for(int i = 0; i < links.size(); i++) {
                        if(links.get(i).text().equals("下一页")) {
                            link = "http://phycai.sjtu.edu.cn" + links.get(i).attr("href");
                            break;
                        }
                    }
                    getDocuments(link);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
