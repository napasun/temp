package com.estgames.estgames_framework.banner;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.estgames.estgames_framework.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import static android.R.style.Theme_NoTitleBar_Fullscreen;

/**
 * Created by mp on 2018. 1. 24..
 */

public class BannerDialog extends Dialog {
    ImageView imageView;
    ArrayList<Bitmap> bitmap;
    ArrayList<Banner> openBanners;
    BannerJson bannerJson;
    String bannerApiUrl = "https://8726wj937l.execute-api.ap-northeast-2.amazonaws.com/live?region=catcafe.kr.ls&lang=ko&placement=LANDING";
    Dialog dialog;
    Button linkBt;
    Button closeBt;
    int currentIndex = 0;
    CheckBox oneDayCheck;
    Date todayDate;
    String today;
    SharedPreferences pref;
    SharedPreferences.Editor prefEdit;
    Runnable callback = new Runnable() {
        @Override
        public void run() {

        }
    };


    public BannerDialog(Context context, SharedPreferences pref) {
        super(context, Theme_NoTitleBar_Fullscreen);

        bitmap = new ArrayList<Bitmap>();
        openBanners = new ArrayList<Banner>();
        this.pref = pref;
        prefEdit = this.pref.edit();
        todayDate = new Date();

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        today = dt.format(todayDate);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = this;

        setContentView(R.layout.banner);
        imageView = (ImageView)findViewById(R.id.bannerImgView);
        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest stringRequest = new StringRequest(bannerApiUrl
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                bannerJson = new BannerJson(response);
                if (bannerJson.getEntries().size() > 0) {
                    Thread mThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                for (Entry entry : bannerJson.getEntries()) {
                                    if (pref.getString(entry.getBanner().getName(), "0").equals(today)) {
                                        continue;   //오늘은 더 이상 보지 않기
                                    }

                                    if (entry.getBegin() != null && entry.getBegin().after(todayDate)) {
                                        continue;
                                    }

                                    if (entry.getEnd() != null && entry.getEnd().before(todayDate)) {
                                        continue;
                                    }

                                    URL url = new URL(entry.getBanner().getResource());

                                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                                    conn.setDoInput(true);
                                    conn.connect();

                                    InputStream is = conn.getInputStream();

                                    bitmap.add(BitmapFactory.decodeStream(is));
                                    openBanners.add(entry.getBanner());
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                dialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                                dialog.dismiss();
                            }
                        }
                    };

                    mThread.start();

                    try {
                        mThread.join();
                        imageView.setImageBitmap(bitmap.get(currentIndex));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
            }
        });

        queue.add(stringRequest);

        oneDayCheck = (CheckBox) findViewById(R.id.bannerOneDayCheck);

        linkBt = (Button) findViewById(R.id.bannerLinkBt);

        linkBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (openBanners.get(currentIndex).getAction().getType().equals("NONE")) {

                } else {
                    //다이얼로그에서 브라우저 열기
                    Uri uri = Uri.parse(openBanners.get(currentIndex).getAction().getUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                    v.getContext().startActivity(intent);
                }
            }
        });


        closeBt = (Button) findViewById(R.id.bannerCloseBt);

        closeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oneDayCheck.isChecked()) {  //체크 박스가 true 일 때
                    prefEdit.putString(openBanners.get(currentIndex).getName(), today);
                    prefEdit.commit();
                }

                if(++currentIndex >= bitmap.size()) {   //더 이상 배너 그림이 없으면 닫기
                    dialog.dismiss();
                    callback.run();
                } else {
                    oneDayCheck.setChecked(false);  //이미지 변경하면서 체크 해제
                    imageView.setImageBitmap(bitmap.get(currentIndex));
                }
            }
        });
    }
}

