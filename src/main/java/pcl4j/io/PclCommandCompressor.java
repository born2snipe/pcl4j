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

import java.io.ByteArrayOutputStream;

/**
 * Compresses 2 parameterized commands together based on the following rules:
 * <p/>
 * <ol>
 * <li>The first 2 bytes after "ESC" MUST be the same in all of the commands to be combined.</li>
 * <li>All alphabetic characters within the combined printer command are lower-case, except the final letter which is always upper-case</li>
 * <li>The printer commands are performed in the order that they are combined (from left to right)</li>
 * </ol>
 * <b>Reference:</b>ctechref.pdf
 */
public class PclCommandCompressor {
    private PclUtil pclUtil = new PclUtil();

    /**
     * Compresses the 2 given commands together
     *
     * @param command      - parameterized command
     * @param otherCommand - parameterized command
     * @return the compressed version of the two given commands
     * @throws IllegalArgumentException when given a 2 byte command
     */
    public PclCommand compress(PclCommand command, PclCommand otherCommand) throws IllegalArgumentException {
        if (!canBeCompressed(command, otherCommand)) {
            throw new IllegalArgumentException("These given commands can not be compressed together! Did you forget to check if they could be compressed before trying to compress them?\n" +
                    "\tcommand=[" + command + "]\n" +
                    "\totherCommmand=[" + otherCommand + "]\n");
        }

        byte[] commandToAppendTo = command.getBytes();
        byte[] commandToAppend = otherCommand.getBytes();
        ByteArrayOutputStream compressedBytes = new ByteArrayOutputStream(commandToAppend.length + commandToAppendTo.length);

        for (int i = 0; i < commandToAppendTo.length; i++) {
            byte currentByte = commandToAppendTo[i];
            if (pclUtil.isTermination(currentByte)) {
                currentByte = pclUtil.changeTerminatorToParameter(currentByte);
            }
            compressedBytes.write(currentByte);
        }

        for (int i = PclUtil.VALUE_BYTE_START_POSITION; i < commandToAppend.length; i++) {
            compressedBytes.write(commandToAppend[i]);
        }

        return new ParameterizedCommand(compressedBytes.toByteArray());
    }

    /**
     * Determines if the 2 given commands can be compressed together
     *
     * @param command      - pcl command
     * @param otherCommand - pcl command
     * @return true - they can be compressed together<br/>
     *         false - they can not be compressed together
     */
    public boolean canBeCompressed(PclCommand command, PclCommand otherCommand) {
        if (isEitherCommandNull(command, otherCommand)) {
            return false;
        }

        if (isEitherCommandA2ByteCommand(command, otherCommand)) {
            return false;
        }

        if (parameterizedBytesDoNotMatch(command, otherCommand)
                || groupBytesDoNotMatch(command, otherCommand)
                || pclUtil.hasBinaryData(command)) {
            return false;
        }

        return true;
    }

    private boolean isEitherCommandNull(PclCommand command, PclCommand otherCommand) {
        return command == null || otherCommand == null;
    }

    private boolean groupBytesDoNotMatch(PclCommand command, PclCommand otherCommand) {
        return pclUtil.getGroupByte(command) != pclUtil.getGroupByte(otherCommand);
    }

    private boolean isEitherCommandA2ByteCommand(PclCommand command, PclCommand otherCommand) {
        return is2ByteCommand(command) || is2ByteCommand(otherCommand);
    }

    private boolean parameterizedBytesDoNotMatch(PclCommand command, PclCommand otherCommand) {
        return pclUtil.getParameterizedByte(command) != pclUtil.getParameterizedByte(otherCommand);
    }

    private boolean is2ByteCommand(PclCommand command) {
        return command.getBytes().length == 2;
    }

}
