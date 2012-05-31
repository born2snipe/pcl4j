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
 * A factory that builds a PclCommand object
 */
public class PclCommandFactory {

    /**
     * Builds a parameterized command
     *
     * @param position      - the location the command was found in the file
     * @param parameterized - the parameterized byte
     * @param group         - the group byte
     * @param value         - the bytes that make up the value
     * @param terminator    - the terminator byte
     * @param data          - binary data following the command
     * @return a new instance of a PclCommand
     */
    public PclCommand buildParameterizedCommand(long position, byte parameterized, byte group, byte[] value, byte terminator, byte[] data) {
        ParameterizedCommand command = new ParameterizedCommand(position);
        command.setParameterizedByte(parameterized);
        command.setGroupByte(group);
        command.setValueBytes(value);
        command.setTerminatorByte(terminator);
        command.setDataBytes(data);
        return command;
    }

    /**
     * Builds a text command
     *
     * @param position - the location the command was found in the file
     * @param textData - the data making up the text
     * @return a new instance of a PclCommand
     */
    public PclCommand buildTextCommand(long position, byte[] textData) {
        return new TextCommand(position, textData);
    }

    /**
     * Builds a 2 byte command
     *
     * @param position    - the location the command was found in the file
     * @param commandByte - the 2nd byte of the command
     * @return a new instance of a PclCommand
     */
    public PclCommand buildTwoByteCommand(long position, byte commandByte) {
        return new TwoByteCommand(position, new byte[]{PclUtil.ESCAPE, commandByte});
    }
}
