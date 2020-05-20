package com.crab.media;

import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;

/**
 * 自定义音频解码类
 */
public class MyCodecAudio extends MyCodecMedia {
    /**
     * 日志标签
     */
    private static final String TAG = "MyCodecAudio";
    /**
     * 多媒体的MIME类型是否以audio/开始
     */
    private static final String AUDIO_PRE = "audio/";
    /**
     * 多媒体对象信息的提取器
     */
    private MediaExtractor mExtractor;
    /**
     * 多媒体编码/解码对象
     */
    private MediaCodec mMediaCodec;
    /**
     * 音频播放类
     */
    private AudioTrack mAudioTrack;
    /**
     * 渲染开始执行时间
     */
    private long mRenderStart = -1;

    public MyCodecAudio() {
    }

    @Override
    public boolean doInit(AssetFileDescriptor afd, Surface surface) {
        try {
            mExtractor = new MediaExtractor();
            mExtractor.setDataSource(afd);
            int numTracks = mExtractor.getTrackCount();
            for (int i = 0; i < numTracks; i++) {
                MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
                String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (!TextUtils.isEmpty(mimeType) && mimeType.startsWith(AUDIO_PRE)) {
                    mExtractor.selectTrack(i);
                    mMediaCodec = MediaCodec.createDecoderByType(mimeType);
                    mMediaCodec.configure(mediaFormat, null, null, 0);
                    mRenderStart = -1;
                    mSawInputEOS = false;
                    mSawOutputEOS = false;
                    mIsPlaying = false;
                    int sampleRateInHz = 96000;
                    if (mediaFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRateInHz = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    }
                    int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
                    if (mediaFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                        if (channelCount == 2) {
                            channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
                        }
                    }
                    int minBuffer = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig,
                            AudioFormat.ENCODING_PCM_16BIT);
                    mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                            sampleRateInHz,
                            channelConfig,
                            AudioFormat.ENCODING_PCM_16BIT,
                            minBuffer,
                            AudioTrack.MODE_STREAM);
                    mMediaCodec.start();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Video codec Init fail....");
            return false;
        }
        return true;
    }

    /**
     * 解码工作
     */
    @Override
    public void doCodecWork() {
        if (!mSawInputEOS) {
            int bufferIndex = mMediaCodec.dequeueInputBuffer(2000);
            if (bufferIndex >= 0) {
                ByteBuffer buffer = mMediaCodec.getInputBuffer(bufferIndex);
                int sampleSize = mExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    sampleSize = 0;
                    mSawInputEOS = true;
                    Log.e(TAG, "saw input EOS");
                }
                long presentationTimeUs = mExtractor.getSampleTime();
                int flag = mSawInputEOS ? BUFFER_FLAG_END_OF_STREAM : 0;
                mMediaCodec.queueInputBuffer(bufferIndex, 0, sampleSize, presentationTimeUs, flag);
                mExtractor.advance();
            }
        }
        if (!mSawOutputEOS) {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int status = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            if (status >= 0) {
                if (bufferInfo.flags == BUFFER_FLAG_END_OF_STREAM) {
                    Log.e(TAG, "saw output EOS");
                    mSawOutputEOS = true;
                }
                long presentationNano = bufferInfo.presentationTimeUs * 1000;
                if (mRenderStart < 0) {
                    mRenderStart = System.nanoTime() - presentationNano;
                }
                /**
                 * 0 P1(100)                      P2(500)                 PresentationTime
                 * |_|____________________________|_______________________|
                 *
                 *   T1(0)          T2(300)                               ThreadTime
                 *   |______________|_____________|_______________________|
                 *   |              |             |
                 *   mRenderStart       sleepTime
                 *   sleepTime = T1-P1+P2-T2=(0-100+500-300)=100
                 * */
                long delay = (mRenderStart + presentationNano) - System.nanoTime();
                if (delay > 0) {
                    try {
                        Thread.sleep(delay / (1000 * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ByteBuffer byteBuffer = mMediaCodec.getOutputBuffer(status);
                byte[] audioData = new byte[bufferInfo.size];
                byteBuffer.get(audioData);
                mAudioTrack.write(audioData,0,audioData.length);
                mAudioTrack.play();
                mMediaCodec.releaseOutputBuffer(status, false);
            } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.e(TAG, "output buffers changed");
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat format = mMediaCodec.getOutputFormat();
                Log.e(TAG, "format changed to:" + format.toString());
            } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.e(TAG, "no output buffer right now");
            } else {
                Log.e(TAG, "unexpected info code:" + status);
            }

        }
    }

    @Override
    public void doRelease() {
        mSawOutputEOS = true;
        mSawInputEOS = true;
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
        Log.e(TAG, "release source over");
    }
}
