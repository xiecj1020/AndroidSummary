package com.crab.media;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private MyCodecThread mVideoCodecThread;
    private MyCodecThread mAudioCodecThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        SurfaceView surfaceView= findViewById(R.id.surfaceview1);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if(mVideoCodecThread!=null){
                    //如果当前线程正在运行，则关闭
                    if(mVideoCodecThread.isRunning()){
                       mVideoCodecThread.stopByHand();
                    }
                }
                if(mAudioCodecThread!=null){
                    //如果当前线程正在运行，则关闭
                    if(mAudioCodecThread.isRunning()){
                        mAudioCodecThread.stopByHand();
                    }
                }
                try {
                    //线程同步对象，等待所有资源都初始化完毕完毕
                    CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
                    //注意Video与Audio不要共享一个AssetFileDescriptor对象
                    AssetManager assetManager = getAssets();
                    AssetFileDescriptor assetFileDescriptorVideo = assetManager.openFd("clips/testfile.mp4");
                    MyCodecVideo myCodecVideo = new MyCodecVideo();
                    myCodecVideo.init(assetFileDescriptorVideo,holder.getSurface());


                    AssetFileDescriptor assetFileDescriptorAudio = assetManager.openFd("clips/testfile.mp4");
                    MyCodecAudio myCodecAudio = new MyCodecAudio();
                    myCodecAudio.init(assetFileDescriptorAudio,null);

                    mVideoCodecThread = new MyCodecThread(myCodecVideo,cyclicBarrier);
                    mVideoCodecThread.setName("MyCodecVideoThread");
                    mAudioCodecThread = new MyCodecThread(myCodecAudio,cyclicBarrier);
                    mAudioCodecThread.setName("MyCodecAudioThread");
                    mAudioCodecThread.start();
                    mVideoCodecThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if(mVideoCodecThread!=null){
                    //如果当前线程正在运行，则关闭
                    if(mVideoCodecThread.isRunning()){
                        mVideoCodecThread.stopByHand();
                    }
                }
                if(mAudioCodecThread!=null){
                    //如果当前线程正在运行，则关闭
                    if(mAudioCodecThread.isRunning()){
                        mAudioCodecThread.stopByHand();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
