package owenhsieh.tldr.audiorecorddemo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * MIT License
 * <p>
 * Copyright (c) 2016 Owen Hsieh
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class MiniRecorder implements Runnable {
    private static final int[] SAMPLE_RATES = new int[]{8000, 11025, 16000, 22050, 44100};
    private static final int BUFFER_SIZE = 1024;
    private final int storeBufferSize = BUFFER_SIZE * 10 * 1024;
    private int currentRate = 0;
    private AudioRecord audioSource;
    private AudioTrack audioTrack;

    private Thread threadSelf;
    private boolean enable = false;
    private boolean resume = false;
    private ByteBuffer byteBuffer;

    public MiniRecorder() {
        byteBuffer = ByteBuffer.allocate(storeBufferSize);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void resume() {
        resume = true;
        if (audioSource == null) {
            for (int sampleRate : SAMPLE_RATES) {
                int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                audioSource = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

                if (audioSource.getState() == AudioRecord.STATE_INITIALIZED) {
                    currentRate = sampleRate;
                    break;
                }
            }

            if (audioSource != null && audioSource.getState() == AudioRecord.STATE_INITIALIZED) {
                audioSource.startRecording();
            } else {
                Log.e("!msg", "AudioRecord init everything failed");
            }
        }

        if (audioTrack == null) {
            int minBufferSize = AudioTrack.getMinBufferSize(currentRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, currentRate,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize, AudioTrack.MODE_STREAM);

            audioTrack.play();
        }

        if (threadSelf == null) {
            threadSelf = new Thread(this);
            threadSelf.start();
        }
    }

    public void stop() {
        resume = false;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (resume) {
            if (!enable) continue;
            if (!byteBuffer.hasRemaining()) {
                continue;
            }
            int length = audioSource.read(buffer, 0, BUFFER_SIZE);
            if (length > 0) {
                if (length < byteBuffer.remaining())
                    byteBuffer.put(buffer, 0, length);

                audioTrack.write(buffer, 0, length);
            }
        }
        threadSelf = null;
    }

    public void release() {
        audioSource.stop();
        audioSource.release();

        audioTrack.stop();
        audioTrack.release();
    }

    public void getBuffer(ByteBuffer outBuffer) {
        outBuffer.put(byteBuffer.array(), 0, byteBuffer.position());
    }

    public void clearBuffer() {
        byteBuffer.clear();
    }

    public int getCurrentRate() {
        return currentRate;
    }

    public int getBufferSize() {
        return storeBufferSize;
    }
}
