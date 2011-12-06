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


public class ByteArrayUtil {
    public static byte[] concat(byte[]... bytes) {
        int count = 0;
        for (byte[] data : bytes) {
            count += data.length;
        }
        byte[] temp = new byte[count];
        int offset = 0;
        for (byte[] data : bytes) {
            System.arraycopy(data, 0, temp, offset, data.length);
            offset += data.length;
        }
        return temp;
    }
}
