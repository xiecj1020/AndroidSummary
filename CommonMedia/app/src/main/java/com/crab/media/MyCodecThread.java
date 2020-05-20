package com.crab.media;


import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 多媒体解码的线程类
 */
public class MyCodecThread extends Thread {
    /**
     * 新建状态
     */
    private static final int NEW = 1;
    /**
     * 正在运行
     */
    private static final int RUNNING = 2;
    /**
     * 暂停状态
     */
    private static final int PAUSED = 3;
    /**
     * 停止状态
     */
    private static final int STOP = 4;
    /**
     * 线程的当前状态
     */
    private volatile int mThreadState;
    /**
     * 多媒体解码对象
     */
    private MyCodecMedia mCodecMedia;
    /**
     * 同步对象，让单个多媒体线程资源都准备完毕才开始播放
     */
    private CyclicBarrier mCyclicBarrier;

    public MyCodecThread(MyCodecMedia codecMedia,CyclicBarrier cyclicBarrier) {
        super("name");
        mThreadState = NEW;
        mCodecMedia = codecMedia;
        mCyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        mThreadState = RUNNING;
        try {
            mCyclicBarrier.await();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            boolean init = mCodecMedia.isInit();
            //1.资源初始化过
            //2.线程不被停止
            //3.没有读取到资源的末尾
            while (init && (mThreadState != STOP)
                    && (!mCodecMedia.isSawInputEOS() || !mCodecMedia.isSawOutEOS())) {
                mCodecMedia.doCodecWork();
            }
        } finally {
            mThreadState = STOP;
            if (mCodecMedia != null) {
                mCodecMedia.release();
            }
        }
    }

    public boolean isRunning() {
        return mThreadState == RUNNING;
    }

    public boolean isNew() {
        return mThreadState == NEW;
    }

    public boolean isStop() {
        return mThreadState == STOP;
    }

    public void stopByHand() {
        mThreadState = STOP;
    }
}
