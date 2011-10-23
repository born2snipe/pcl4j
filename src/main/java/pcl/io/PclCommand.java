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

package pcl.io;

import java.util.Arrays;

/**
 * The base class of all PCL Commands
 */
public abstract class PclCommand {
    /**
     * The bytes that represent the given PCL command
     */
    private final byte[] bytes;
    private final long position;

    public PclCommand(long position, byte[] bytes) {
        this.position = position;
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public long getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return super.toString() + " position: " + position + ", length=" + bytes.length + ", contents:{" + Arrays.toString(bytes) + "}";
    }

    public String toAscii() {
        StringBuilder builder = new StringBuilder();
        for (byte data : bytes) {
            builder.append((char) data);
        }
        return builder.toString();
    }
}
