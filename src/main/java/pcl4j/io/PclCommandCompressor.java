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
     * @param commandToAppendTo - parameterized command
     * @param commandToAppend   - parameterized command
     * @return the compressed version of the two given commands
     * @throws IllegalArgumentException when given a 2 byte command
     */
    public PclCommand compress(PclCommand commandToAppendTo, PclCommand commandToAppend) throws IllegalArgumentException {
        if (!canBeCompressed(commandToAppendTo, commandToAppend)) {
            throw new IllegalArgumentException("These given commands can not be compressed together! Did you forget to check if they could be compressed before trying to compress them?\n" +
                    "\tcommand=[" + commandToAppendTo + "]\n" +
                    "\totherCommmand=[" + commandToAppend + "]\n");
        }

        byte[] commandToAppendToBytes = commandToAppendTo.getBytes();
        byte[] commandToAppendBytes = commandToAppend.getBytes();
        UnsynchronizedByteArrayOutputStream compressedBytes = new UnsynchronizedByteArrayOutputStream(commandToAppendBytes.length + commandToAppendToBytes.length);

        for (int i = 0; i < commandToAppendToBytes.length; i++) {
            byte currentByte = commandToAppendToBytes[i];
            if (pclUtil.isTermination(currentByte)) {
                currentByte = pclUtil.changeTerminatorToParameter(currentByte);
            }
            compressedBytes.write(currentByte);
        }

        for (int i = PclUtil.VALUE_BYTE_START_POSITION; i < commandToAppendBytes.length; i++) {
            compressedBytes.write(commandToAppendBytes[i]);
        }

        return new ParameterizedCommand(compressedBytes.toByteArray());
    }

    /**
     * Determines if the 2 given commands can be compressed together
     *
     * @param commandToAppendTo - pcl command
     * @param commandToAppend   - pcl command
     * @return true - they can be compressed together<br/>
     *         false - they can not be compressed together
     */
    public boolean canBeCompressed(PclCommand commandToAppendTo, PclCommand commandToAppend) {
        if (isEitherCommandNull(commandToAppendTo, commandToAppend)) {
            return false;
        } else if (isEitherCommandA2ByteCommand(commandToAppendTo, commandToAppend)) {
            return false;
        } else if (isEitherCommandATextCommand(commandToAppendTo, commandToAppend)) {
            return false;
        }

        ParameterizedCommand command1 = (ParameterizedCommand) commandToAppendTo;
        ParameterizedCommand command2 = (ParameterizedCommand) commandToAppend;
        if (parameterizedBytesDoNotMatch(command1, command2)
                || groupBytesDoNotMatch(command1, command2)
                || command1.getDataBytes().length > 0) {
            return false;
        }

        return true;
    }

    private boolean isEitherCommandATextCommand(PclCommand commandToAppendTo, PclCommand commandToAppend) {
        return commandToAppendTo instanceof TextCommand || commandToAppend instanceof TextCommand;
    }

    private boolean isEitherCommandNull(PclCommand command, PclCommand otherCommand) {
        return command == null || otherCommand == null;
    }

    private boolean groupBytesDoNotMatch(ParameterizedCommand command, ParameterizedCommand otherCommand) {
        return command.getGroupByte() != otherCommand.getGroupByte();
    }

    private boolean isEitherCommandA2ByteCommand(PclCommand command, PclCommand otherCommand) {
        return is2ByteCommand(command) || is2ByteCommand(otherCommand);
    }

    private boolean parameterizedBytesDoNotMatch(ParameterizedCommand command, ParameterizedCommand otherCommand) {
        return command.getParameterizedByte() != otherCommand.getParameterizedByte();
    }

    private boolean is2ByteCommand(PclCommand command) {
        return command instanceof TwoByteCommand;
    }

}
