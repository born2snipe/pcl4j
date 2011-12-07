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
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for checking for the magic PCL bytes
 */
public class PclUtil {
    private static final List<String> BINARY_DATA_COMMANDS = Arrays.asList(
            "*c#E", ")s#W", "(s#W", "(f#W", "&n#W", "*b#W", "*g#W", "*b#Y", "*b#V", "*v#W", "*l#W", "*o#W", "*m#W", "&p#X"
    );

    public static final int PARAMETERIZED_BYTE_POSITION = 1;
    public static final int GROUP_BYTE_POSITION = 2;
    public static final int VALUE_BYTE_START_POSITION = 3;
    public static final byte ESCAPE = 27;
    public static final byte LOWEST_2BYTE_COMMAND_OPERATOR = 48;
    public static final byte HIGHEST_2BYTE_COMMAND_OPERATOR = 126;
    public static final byte LOWEST_TERMINATION_BYTE = 64;
    public static final byte HIGHEST_TERMINATION_BYTE = 94;
    public static final byte LOWEST_PARAMETERIZED_BYTE = 33;
    public static final byte HIGHEST_PARAMETERIZED_BYTE = 47;
    public static final byte LOWEST_GROUP_BYTE = 96;
    public static final byte HIGHEST_GROUP_BYTE = 126;
    public static final byte LOWEST_PARAMETER_BYTE = 96;
    public static final byte HIGHEST_PARAMETER_BYTE = 126;
    public static final int PARAMETER_TO_TERMINATOR_DIFFERENCE = LOWEST_PARAMETER_BYTE - LOWEST_TERMINATION_BYTE;

    /**
     * Determines if the given byte is a 2 byte command operator
     * <p>
     * 48 >= value <= 126
     * </p>
     *
     * @param value the byte to check
     * @return true - is a 2 byte operator<br/>false - is not a 2 byte operator
     */
    public boolean is2ByteCommandOperator(byte value) {
        return value >= LOWEST_2BYTE_COMMAND_OPERATOR && value <= HIGHEST_2BYTE_COMMAND_OPERATOR;
    }

    /**
     * Determines if the given byte is an ESCAPE byte
     *
     * @param value the byte to check
     * @return true - is an ESCAPE byte<br/>false - is not an ESCAPE byte
     */
    public boolean isEscape(byte value) {
        return value == ESCAPE;
    }

    /**
     * Determines if the given byte is a termination byte
     * <p>
     * 64 >= value <= 94
     * </p>
     *
     * @param value the byte to check
     * @return true - is a termination byte<br/> false - is not a termination byte
     */
    public boolean isTermination(byte value) {
        return value >= LOWEST_TERMINATION_BYTE && value <= HIGHEST_TERMINATION_BYTE;
    }

    /**
     * Determines if the given byte is a parameterized byte
     * <p>
     * 33 >= value <= 47
     * </p>
     *
     * @param value the byte to check
     * @return true - is a parameterized byte<br/>false - is not a parameterized byte
     */
    public boolean isParameterizedCharacter(byte value) {
        return value >= LOWEST_PARAMETERIZED_BYTE && value <= HIGHEST_PARAMETERIZED_BYTE;
    }

    /**
     * Determines if the given byte is a group character
     * <p>
     * 96 >= value <= 126
     * </p>
     *
     * @param value the byte to check
     * @return true - is a group character<br/>false - is not a group character
     */
    public boolean isGroupCharacter(byte value) {
        return value >= LOWEST_GROUP_BYTE && value <= HIGHEST_GROUP_BYTE;
    }

    /**
     * Determines if the given byte is a parameter character
     * <p>
     * 96 >= value <= 126
     * </p>
     *
     * @param value the byte to check
     * @return true - is a parameter character<br/>false - is not a parameter character
     */
    public boolean isParameterCharacter(byte value) {
        return value >= LOWEST_PARAMETER_BYTE && value <= HIGHEST_PARAMETER_BYTE;
    }

    /**
     * Determines if the given command has binary data
     *
     * @param command the command to check
     * @return true - binary data was found<br/>false - no binary data was found
     */
    public boolean hasBinaryData(PclCommand command) {
        return getBinaryData(command).length > 0;
    }

    /**
     * Converts a termination byte into a parameter byte
     *
     * @param terminationByte the termination byte to be converted
     * @return the resulting parameter byte
     * @throws IllegalArgumentException when the given byte is not a termination byte
     */
    public byte changeTerminatorToParameter(byte terminationByte) throws IllegalArgumentException {
        if (!isTermination(terminationByte)) {
            throw new IllegalArgumentException("Not a terminator byte given. byte=[" + terminationByte + "]");
        }
        return (byte) (terminationByte + PARAMETER_TO_TERMINATOR_DIFFERENCE);
    }

    /**
     * Gets the Group byte from the given command
     *
     * @param command the command to get the group byte from
     * @return the group byte of the command
     */
    public byte getGroupByte(PclCommand command) {
        requiresParameterizedCommand(command);
        return command.getBytes()[GROUP_BYTE_POSITION];
    }

    /**
     * Gets the parameterized byte from the given command
     *
     * @param command the command to get the parameterized byte from
     * @return the parameterized byte of the command
     */
    public byte getParameterizedByte(PclCommand command) {
        return command.getBytes()[PARAMETERIZED_BYTE_POSITION];
    }

    /**
     * Gets the terminator byte from the given command
     *
     * @param command the command to get the terminator byte from
     * @return the terminator byte of the command
     */
    public byte getTerminatorByte(PclCommand command) {
        requiresParameterizedCommand(command);
        byte[] data = command.getBytes();
        for (int i = 0; i < data.length; i++) {
            if (isTermination(data[i])) {
                return data[i];
            }
        }
        throw new IllegalStateException("Could not locate terminator byte in command: " + command);
    }

    /**
     * Converts a parameter byte into a terminator byte
     *
     * @param parameterByte the parameter byte to be converted
     * @return terminator byte
     */
    public byte changeParameterToTerminator(byte parameterByte) {
        if (!isParameterCharacter(parameterByte)) {
            throw new IllegalArgumentException("Not a parameter byte given. byte=[" + parameterByte + "]");
        }
        return (byte) (parameterByte - PARAMETER_TO_TERMINATOR_DIFFERENCE);
    }

    /**
     * Captures the binary data of the given command
     *
     * @param command - the command to capture the binary data from
     * @return a byte array containing all the binary data
     */
    public byte[] getBinaryData(PclCommand command) {
        if (is2ByteCommand(command)) {
            return new byte[0];
        }

        byte[] data = command.getBytes();
        int startOfBinaryDataIndex = -1;
        for (int i = GROUP_BYTE_POSITION + 1; i < data.length; i++) {
            if (isTermination(data[i])) {
                startOfBinaryDataIndex = i + 1;
                break;
            }
        }

        byte[] temp = new byte[data.length - startOfBinaryDataIndex];
        System.arraycopy(data, startOfBinaryDataIndex, temp, 0, temp.length);

        return temp;
    }

    /**
     * Captures the value bytes of the given command
     * <p/>
     * Defaults the value to zero if a 'value' is not found in the command
     *
     * @param command - the command to capture the value from
     * @return a byte array containing all the value bytes
     */
    public byte[] getValue(PclCommand command) {
        requiresParameterizedCommand(command);
        return getValue(command.getBytes());
    }

    /**
     * Captures the value bytes of the given command bytes
     * <p/>
     * Defaults the value to zero if a 'value' is not found in the command
     *
     * @param commandBytes - the command to capture the value from
     * @return a byte array containing all the value bytes
     */
    public byte[] getValue(byte[] commandBytes) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (isTermination(commandBytes[VALUE_BYTE_START_POSITION])) {
            output.write('0');
        } else {
            for (int i = VALUE_BYTE_START_POSITION; i < commandBytes.length; i++) {
                byte currentByte = commandBytes[i];
                if (isTermination(currentByte)) {
                    break;
                } else if (isParameterCharacter(currentByte)) {
                    output.reset();
                } else {
                    output.write(currentByte);
                }
            }
        }
        return output.toByteArray();
    }

    /**
     * Determines if the given command is expecting binary data to follow it
     *
     * @param commandBytes - the bytes that make up the command
     * @return true - expecting binary data
     *         false - is not expecting binary data
     */
    public boolean isCommandExpectingData(byte[] commandBytes) {
        StringBuilder pattern = new StringBuilder();
        pattern.append((char) commandBytes[PARAMETERIZED_BYTE_POSITION]);
        pattern.append((char) commandBytes[GROUP_BYTE_POSITION]);
        pattern.append("#");
        pattern.append((char) getTerminatorByte(commandBytes));
        return BINARY_DATA_COMMANDS.contains(pattern.toString());
    }

    private void requiresParameterizedCommand(PclCommand command) {
        if (!(command instanceof ParameterizedCommand)) {
            throw new IllegalArgumentException("Parameterized command required, but received a 2 byte command");
        }
    }

    private boolean is2ByteCommand(PclCommand command) {
        return command.getBytes().length == 2;
    }

    private byte getTerminatorByte(byte[] commandBytes) {
        return commandBytes[commandBytes.length - 1];
    }
}
