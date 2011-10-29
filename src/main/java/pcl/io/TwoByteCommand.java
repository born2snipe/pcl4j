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

/**
 * Represents a 2 byte PCL command
 * <p/>
 * <p/>
 * <p/>
 * Two-character escape sequences have the following form:
 * <p/>
 * [ESC]X
 * <p/>
 * where "X" is a character that defines the operation
 * to be performed. "X" may be any character from
 * the ASCII table within the range 48-126 decimal
 */
public class TwoByteCommand extends PclCommand {
    public TwoByteCommand(byte[] bytes) {
        super(bytes);
        verifyByteCount(bytes);
    }

    public TwoByteCommand(long position, byte[] bytes) {
        super(position, bytes);
        verifyByteCount(bytes);
    }

    public byte getOperation() {
        return getBytes()[1];
    }

    private void verifyByteCount(byte[] bytes) {
        if (bytes.length != 2)
            throw new IllegalArgumentException("Not a valid number of bytes given (" + bytes.length + " byte(s)  given)");
    }
}
