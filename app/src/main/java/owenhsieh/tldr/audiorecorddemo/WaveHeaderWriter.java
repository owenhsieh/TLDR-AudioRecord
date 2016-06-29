package owenhsieh.tldr.audiorecorddemo;

import java.io.DataOutputStream;
import java.io.IOException;
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
 * <p>
 * Reference: http://stackoverflow.com/a/37436599/5689728
 */
public class WaveHeaderWriter {
    public static void rawToWave(int numberOfChannels, int sampleRate, int bitPerSample,
                                 final ByteBuffer rawBuffer, final DataOutputStream outputStream) throws IOException {

        // WAVE header
        writeString(outputStream, "RIFF"); // chunk id
        writeInt(outputStream, 36 + rawBuffer.position()); // chunk size
        writeString(outputStream, "WAVE"); // format
        writeString(outputStream, "fmt "); // subchunk 1 id
        writeInt(outputStream, 16); // subchunk 1 size
        writeShort(outputStream, (short) 1); // audio format (1 = PCM)
        writeShort(outputStream, (short) numberOfChannels); // number of channels
        writeInt(outputStream, sampleRate); // sample rate
        writeInt(outputStream, numberOfChannels * sampleRate * bitPerSample / 8); // byte rate = {number of channels} * {sample rate} * {bits per sample} / 8
        writeShort(outputStream, (short) 2); // block align
        writeShort(outputStream, (short) bitPerSample); // bits per sample
        writeString(outputStream, "data"); // subchunk 2 id
        writeInt(outputStream, rawBuffer.position()); // subchunk 2 size

        outputStream.write(rawBuffer.array(), 0, rawBuffer.position());

    }

    private static void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private static void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value);
        output.write(value >> 8);
    }

    private static void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}
