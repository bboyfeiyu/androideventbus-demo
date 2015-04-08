
package com.example.event_bus_extend;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.event_bus_extend.custom.ThreadPoolHandler;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subcriber;
import org.simple.eventbus.ThreadMode;
import org.simple.eventbus.handler.AsyncEventHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 将对象注册到事件总线中, ****** 注意要在onDestory中进行注销 ****
        EventBus.getDefault().register(this);
        // 初始化各个按钮的点击事件
        initClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ****** 不要忘了进行注销 ****
        EventBus.getDefault().unregister(this);
    }

    private void initClickListener() {
        findViewById(R.id.default_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EventBus.getDefault().post("这是一个没有tag的事件");
            }
        });

        findViewById(R.id.async_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EventBus.getDefault().setAsyncEventHandler(new AsyncEventHandler());
                // 参数1为要发布的事件,参数2为tag
                EventBus.getDefault().post("这是一个执行在异步线程的事件", "async");
            }
        });

        findViewById(R.id.custom_async_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 自定义的异步事件处理器,使用线程池
                EventBus.getDefault().setAsyncEventHandler(new ThreadPoolHandler());
                EventBus.getDefault().post(
                        "http://img.my.csdn.net/uploads/201407/26/1406383299_1976.jpg", "download");
            }
        });

        findViewById(R.id.csuicide_event_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(" 拜拜!", "csuicide");
            }
        });
        
    }

    // ************ 使用@Subcriber标识的都是事件接收方法 *************
    @Subcriber(tag = "csuicide")
    private void csuicideMyself(String msg) {
        toastShortMsg("应用退出 " + msg);
        finish();
    }
    
    
    @Subcriber(tag = "post", mode=ThreadMode.POST)
    private void executeInpostThread(String msg) {
        toastShortMsg("接收到 " + Thread.currentThread().getName()  + ", "+ msg);
    }

    @Subcriber(mode = ThreadMode.MAIN)
    private void toastMsgFromEvent(String msg) {
        toastShortMsg("接收到事件 ( 无tag ) : " + msg);
    }

    @Subcriber(tag = "async", mode = ThreadMode.ASYNC)
    private void executeAsync(final String msg) {
        final String threadName = Thread.currentThread().getName();
        toastOnUIThread("接收到事件 ( 执行在异步线程 ) :  线程名 = " + threadName + ",  msg =  " + msg);
        
        EventBus.getDefault().post("From async", "post");
    }

    @Subcriber(tag = "download", mode = ThreadMode.ASYNC)
    private void downloadImage(final String imageUrl) {
        toastOnUIThread("### 下载图片中 : " + Thread.currentThread().getName() + ", url : " + imageUrl);

        HttpURLConnection urlConnection = null;
        try {
            final URL url = new URL(imageUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            final Bitmap bmp = BitmapFactory.decodeStream(urlConnection.getInputStream());
            toastOnUIThread("图片下载完成 : " + bmp);
        } catch (IOException e) {
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    // ****** 工具方法 *******

    private void toastShortMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
                .show();
    }

    // 在UI线程打印一个Toast
    private void toastOnUIThread(final String msg) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        msg, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

}
