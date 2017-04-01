package com.yjcloud.alsa.capture;

import com.noisepages.nettoyeur.jack.JackNativeClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhc on 17/4/1.
 */
public class ALSACapture {

    private static String JACK_CAPTURE_PREFIX = "system:capture_";
    private static int OPEN_CHANNEL_NUMBER = 8;

    public static void main(String[] args) throws Exception{
        final List<ByteArrayOutputStream> byteList = new ArrayList<>();
        for (int i = 0; i < OPEN_CHANNEL_NUMBER; i++) {
            byteList.add(new ByteArrayOutputStream());
        }

        JackNativeClient captureClient = new JackNativeClient("jackClient", OPEN_CHANNEL_NUMBER, 0, false) {

            @Override
            protected void process(FloatBuffer[] inBuffers, FloatBuffer[] outBuffers) {
                for (int i = 0; i < inBuffers.length; i++) {
                    FloatBuffer inBuffer = inBuffers[i];
                    ByteArrayOutputStream bos = byteList.get(i);
                    byte[] bytes = new byte[inBuffer.remaining() * 2];
                    int index = 0;
                    while (inBuffer.hasRemaining()) {
                        float fBuffer = inBuffer.get();
                        // 声音放大
                        int tmp = (int) (fBuffer * Short.MAX_VALUE);
                        bytes[index] = (byte) (tmp & 0x000000FF);
                        bytes[index + 1] = (byte) ((tmp >> 8) & 0x000000FF);
                        index += 2;
                    }
                    try {
                        bos.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };

        for (int i = 0; i < OPEN_CHANNEL_NUMBER; i++) {
            // 打开的jack采集通道
            captureClient.connectInputPorts(i, 1, JACK_CAPTURE_PREFIX + i);
        }

    }

}
