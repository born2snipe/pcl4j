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

/**
 * Represents a Parameterized PCL command
 */
public class ParameterizedCommand extends PclCommand {
    private byte groupByte;
    private byte parameterizedByte;
    private byte[] valueBytes;
    private byte terminatorByte;
    private byte[] dataBytes;

    @Deprecated
    public ParameterizedCommand(byte[] bytes) {
        super(bytes);
    }

    public ParameterizedCommand(long position) {
        super(position);
    }

    public byte getGroupByte() {
        return groupByte;
    }

    public byte getParameterizedByte() {
        return parameterizedByte;
    }

    public byte getTerminatorByte() {
        return terminatorByte;
    }

    public byte[] getValueBytes() {
        return valueBytes;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public void setGroupByte(byte group) {
        this.groupByte = group;
    }

    public void setParameterizedByte(byte parameterizedByte) {
        this.parameterizedByte = parameterizedByte;
    }

    public void setValueBytes(byte[] valueBytes) {
        this.valueBytes = valueBytes;
    }

    public void setTerminatorByte(byte terminatorByte) {
        this.terminatorByte = terminatorByte;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
    }

    public byte[] getBytes() {
        UnsynchronizedByteArrayOutputStream output = new UnsynchronizedByteArrayOutputStream(32);
        output.write(PclUtil.ESCAPE);
        output.write(parameterizedByte);
        output.write(groupByte);
        output.write(valueBytes);
        output.write(terminatorByte);
        output.write(dataBytes);
        return output.toByteArray();
    }
}
