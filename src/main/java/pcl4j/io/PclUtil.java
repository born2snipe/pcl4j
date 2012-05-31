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

import java.util.Arrays;

/**
 * Utility class for checking for the magic PCL bytes
 */
public class PclUtil {
    private static final String[] BINARY_DATA_COMMANDS = {")s#W", "(s#W", "(f#W", "&n#W", "*b#W", "*g#W", "*b#Y", "*b#V", "*v#W", "*l#W", "*o#W", "*m#W", "&p#X"};
    private static final int[] BINARY_DATA_COMMANDS_HASHES = initializeBinaryDataHashes();
    private static final byte[] UNIVERSAL_EXIT_BYTES = "%-12345X".getBytes();

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
     * Determines if the given command is expecting binary data to follow it
     *
     * @param commandBytes - the bytes that make up the command
     * @return true - expecting binary data
     *         false - is not expecting binary data
     */
    public boolean isCommandExpectingData(byte[] commandBytes) {
        byte[] commandId = {
                commandBytes[PARAMETERIZED_BYTE_POSITION],
                commandBytes[GROUP_BYTE_POSITION],
                '#',
                (byte) Character.toUpperCase((char) getTerminatorByte(commandBytes))
        };
        return Arrays.binarySearch(BINARY_DATA_COMMANDS_HASHES, Arrays.hashCode(commandId)) > -1;
    }

    /**
     * Determines if the given command is a "Universal Exit"
     *
     * @param commandBytes - the bytes that make up the command
     * @return true - is an universal exit command
     *         false - is not an universal exit command
     */
    public boolean isUniversalExit(byte[] commandBytes) {
        if (commandBytes.length < UNIVERSAL_EXIT_BYTES.length) {
            return false;
        }

        for (int i = 0; i < UNIVERSAL_EXIT_BYTES.length; i++) {
            if (UNIVERSAL_EXIT_BYTES[i] != commandBytes[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Converts a value to an integer value
     * <p/>
     * Warning: this truncates decimal point values
     *
     * @param valueBytes - the value bytes of a command
     * @return an integer value
     */
    public int convertValueToInt(byte[] valueBytes) {
        if (valueBytes.length == 0) {
            return 0;
        }

        boolean negativeNumber = false;
        int value = 0;
        for (int i = 0; i < valueBytes.length; i++) {
            byte current = valueBytes[i];
            if (isMinus(current)) {
                negativeNumber = true;
            } else if (isNumeric(current)) {
                value += convertNumberCharacterToNumberValue(current);

                if (i + 1 < valueBytes.length) {
                    byte next = valueBytes[i + 1];
                    if (isNumeric(next)) {
                        value *= 10;
                    }
                }
            } else if (isDecimal(current)) {
                break;
            }
        }

        if (negativeNumber) {
            value *= -1;
        }

        return value;
    }

    private boolean isDecimal(byte valueByte) {
        return valueByte == '.';
    }

    private boolean isMinus(byte valueByte) {
        return valueByte == '-';
    }

    private boolean isNumeric(byte valueByte) {
        return valueByte >= '0' && valueByte <= '9';
    }

    private int convertNumberCharacterToNumberValue(byte byteToFind) {
        return byteToFind - '0';
    }

    private void requiresParameterizedCommand(PclCommand command) {
        if (!(command instanceof ParameterizedCommand)) {
            throw new IllegalArgumentException("Parameterized command required, but received a 2 byte command");
        }
    }

    private boolean is2ByteCommand(PclCommand command) {
        return command instanceof TwoByteCommand;
    }

    private byte getTerminatorByte(byte[] commandBytes) {
        return commandBytes[commandBytes.length - 1];
    }

    private static int[] initializeBinaryDataHashes() {
        int[] hashes = new int[BINARY_DATA_COMMANDS.length];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = Arrays.hashCode(BINARY_DATA_COMMANDS[i].getBytes());
        }
        Arrays.sort(hashes);
        return hashes;
    }
}
