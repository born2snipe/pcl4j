/**
 *
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package pcl4j.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class UnsyncronizedByteArrayOutputStream extends OutputStream {
    private byte[] buffer;
    private int position = 0;
    private byte[] cache;

    public UnsyncronizedByteArrayOutputStream(int initialSize) {
        buffer = new byte[initialSize];
    }

    @Override
    public void write(int value) {
        if (position >= buffer.length) {
            int additionalSpace = buffer.length / 2;
            byte[] temp = new byte[buffer.length + additionalSpace];
            System.arraycopy(buffer, 0, temp, 0, buffer.length);
            buffer = temp;
        }
        buffer[position++] = (byte) value;
        cache = null;
    }

    @Override
    public void write(byte[] bytes) {
        try {
            super.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException("A problem writing to the stream", e);
        }
    }

    public byte[] toByteArray() {
        if (cache == null)
            cache = Arrays.copyOf(buffer, position);
        return cache;
    }

    public void reset() {
        position = 0;
    }

    @Override
    public String toString() {
        return new String(toByteArray());
    }

    public int size() {
        return position;
    }
}
