package com.crab.media;

import android.content.res.AssetFileDescriptor;
import android.view.Surface;

/**
 * 自定义多媒体解析类
 */
public abstract class MyCodecMedia {
    /** 日志标签*/
    private static final String TAG = "MyCodecMedia";
    /** 是否初始化过*/
    private volatile boolean mInit = false;
    /** 渲染开始执行时间*/
    protected volatile boolean mSawInputEOS = false;
    protected volatile boolean mSawOutputEOS = false;
    /** 是否正在播放文件*/
    protected volatile  boolean mIsPlaying = false;
    /** 是否已经释放过资源对象*/
    private volatile boolean mReleaseSource = false;
    public MyCodecMedia(){
    }

    /**
     * 初始化各种变量
     * @param afd 多媒体文件路径
     * @param surface 展示画面的表面对象
     */
    public final void init(AssetFileDescriptor afd, Surface surface){
        mInit = doInit(afd,surface);

    }

    /**
     * 初始化各种变量
     * @param afd 多媒体文件路径
     * @param surface 展示画面的表面对象
     * @return true初始化成功
     */
    public abstract boolean doInit(AssetFileDescriptor afd, Surface surface);

    /**
     * 解码工作
     */
    public abstract void doCodecWork();

    /**
     * 返回是否已经初始化过资源
     * @return true资源初始化完成
     */
    public boolean isInit(){
        return mInit;
    }

    /**
     * 返回是否已经读到Input末尾
     * @return true已经读到Input末尾
     */
    public boolean isSawInputEOS(){
        return mSawInputEOS;
    }

    /**
     * 返回是否已经读到Output末尾
     * @return true已经读到Output末尾
     */
    public boolean isSawOutEOS(){
        return mSawOutputEOS;
    }

    /**
     * 释放资源
     */
    public final void release(){
        if(!mReleaseSource){
            mReleaseSource = true;
            doRelease();
        }
    }
    public abstract void doRelease();
}
