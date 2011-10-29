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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * A PclCommandReader implementation backed by an InputStream
 */
public class InputStreamPclCommandReader implements PclCommandReader {
    private static final int EOF = -1;
    private PushbackInputStream inputStream;
    private long currentPosition = -1L;
    private PclUtil pclUtil = new PclUtil();
    private ByteArrayOutputStream commandBytes = new ByteArrayOutputStream();
    private PclCommandFactory pclCommandFactory = new PclCommandFactory();

    public InputStreamPclCommandReader(InputStream inputStream) {
        if (inputStream == null) throw new IllegalArgumentException("A 'null' InputStream was given");
        this.inputStream = new PushbackInputStream(inputStream, 1);
    }

    public PclCommand nextCommand() {
        PclCommand command = null;
        try {
            byte currentByte = -1;
            long commandPosition = 0;
            CommandState commandState = CommandState.LOOK_FOR_COMMAND;
            CommandType commandType = CommandType.UNKNOWN;
            while (command == null) {
                currentByte = (byte) inputStream.read();
                if (currentByte != EOF) {
                    currentPosition++;
                } else {
                    if (commandType == CommandType.UNKNOWN) {
                        break;
                    } else {
                        commandState = CommandState.BUILD;
                    }
                }

                if (commandState == CommandState.LOOK_FOR_COMMAND) {
                    if (pclUtil.isEscape(currentByte)) {
                        commandPosition = currentPosition;
                        commandState = CommandState.CAPTURE;
                    }
                } else if (commandState == CommandState.CAPTURE) {
                    if (commandType == CommandType.UNKNOWN) {
                        if (pclUtil.isParameterizedCharacter(currentByte)) {
                            commandType = CommandType.PARAMETERIZED;
                        } else if (pclUtil.is2ByteCommandOperator(currentByte)) {
                            commandState = CommandState.CAPTURE_AND_BUILD;
                            commandType = CommandType.TWO_BYTE;
                        } else {
                            // by the second read we should know what kind of command we are dealing with if not, this must be a bad command
                            commandState = CommandState.LOOK_FOR_COMMAND;
                            commandBytes.reset();
                            continue;
                        }
                    } else if (commandType == CommandType.PARAMETERIZED) {
                        if (pclUtil.isEscape(currentByte)) {
                            // we have encounter the start of the next command
                            commandState = CommandState.BUILD;
                            currentPosition--;
                            inputStream.unread(currentByte);
                        }
                    } else {
                        if (pclUtil.isEscape(currentByte)) {
                            commandPosition = currentPosition;
                            commandBytes.reset();
                        }
                    }
                }

                if (shouldCaptureTheCurrentByte(commandState)) {
                    commandBytes.write(currentByte);
                }

                if (shouldBuildCommand(commandState)) {
                    command = pclCommandFactory.build(commandPosition, commandBytes.toByteArray());
                }
            }
        } catch (IOException e) {
            throw new PclCommandReaderException("A problem has occurred while trying to parse out pcl command", e);
        } finally {
            commandBytes.reset();
        }

        return command;
    }

    private boolean shouldCaptureTheCurrentByte(CommandState commandState) {
        return commandState == CommandState.CAPTURE || commandState == CommandState.CAPTURE_AND_BUILD;
    }

    private boolean shouldBuildCommand(CommandState commandState) {
        return commandState == CommandState.BUILD || commandState == CommandState.CAPTURE_AND_BUILD;
    }


    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
        }
    }

    public void skip(long numberOfBytesToSkip) {
        try {
            currentPosition += inputStream.skip(numberOfBytesToSkip);
        } catch (IOException e) {
            throw new PclCommandReaderException("A problem occurred while trying to skip " + numberOfBytesToSkip + " byte(s)", e);
        }
    }

    public void setPclCommandFactory(PclCommandFactory pclCommandFactory) {
        this.pclCommandFactory = pclCommandFactory;
    }

    private static enum CommandType {
        TWO_BYTE, PARAMETERIZED, UNKNOWN
    }

    private static enum CommandState {
        LOOK_FOR_COMMAND, CAPTURE, BUILD, CAPTURE_AND_BUILD
    }
}
