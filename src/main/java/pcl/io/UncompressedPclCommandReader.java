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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Uncompresses parameterized commands into individual commands
 */
public class UncompressedPclCommandReader implements PclCommandReader {
    private static final int EOF = -1;
    private static final int PARAMETER_TO_TERMINATOR_OFFSET = 32;
    private final PclUtil pclUtil = new PclUtil();
    private final Queue<PclCommand> queuedCommands = new LinkedList<PclCommand>();
    private final byte[] commandLeadingBytes = new byte[3];
    private final ByteArrayOutputStream uncompressedCommandData = new ByteArrayOutputStream();
    private final PclCommandReader pclCommandReader;
    private PclCommandFactory pclCommandFactory = new PclCommandFactory();

    public UncompressedPclCommandReader(PclCommandReader pclCommandReader) {
        this.pclCommandReader = pclCommandReader;
    }

    public void skip(long numberOfBytesToSkip) throws PclCommandReaderException {
        pclCommandReader.skip(numberOfBytesToSkip);
    }

    public PclCommand nextCommand() throws PclCommandReaderException {
        if (commandsAreQueuedUp()) {
            return queuedCommands.remove();
        }
        PclCommand command = pclCommandReader.nextCommand();
        if (command != null && command.getBytes().length > 2) {
            uncompressCommand(command);
            command = queuedCommands.remove();
        }
        return command;
    }

    private void uncompressCommand(PclCommand compressedCommand) {
        byte[] compressedBytes = compressedCommand.getBytes();
        System.arraycopy(compressedBytes, 0, commandLeadingBytes, 0, commandLeadingBytes.length);

        InputStream inputStream = new ByteArrayInputStream(compressedBytes);

        byte currentByte = -1;
        long position = compressedCommand.getPosition() - 1;
        long commandPosition = -1L;
        try {
            inputStream.skip(commandLeadingBytes.length); // skip the leading bytes
            CommandState state = CommandState.INIT;
            while ((currentByte = (byte) inputStream.read()) != EOF) {
                position++;
                if (state == CommandState.INIT) {
                    commandPosition = position;
                    uncompressedCommandData.write(commandLeadingBytes);
                    state = CommandState.CAPTURE;
                }

                if (isStartOfNextCommand(currentByte)) {
                    state = CommandState.BUILD;
                } else {
                    uncompressedCommandData.write(currentByte);
                }

                if (state == CommandState.BUILD) {
                    if (commandsAreQueuedUp()) {
                        commandPosition += commandLeadingBytes.length;
                    }

                    byte terminator = convertParameterToTerminator(currentByte);
                    uncompressedCommandData.write(terminator);
                    queuedCommands.add(pclCommandFactory.build(commandPosition, uncompressedCommandData.toByteArray()));
                    uncompressedCommandData.reset();
                    state = CommandState.INIT;
                }
            }

            if (uncompressedCommandData.size() > 0) {
                if (commandsAreQueuedUp()) {
                    commandPosition += commandLeadingBytes.length;
                }
                queuedCommands.add(pclCommandFactory.build(commandPosition, uncompressedCommandData.toByteArray()));
            }
        } catch (IOException e) {
        } finally {
            uncompressedCommandData.reset();
        }
    }

    private byte convertParameterToTerminator(byte currentByte) {
        return (byte) (currentByte - PARAMETER_TO_TERMINATOR_OFFSET);
    }

    private boolean isStartOfNextCommand(byte currentByte) {
        return pclUtil.isParameterCharacter(currentByte);
    }

    private boolean commandsAreQueuedUp() {
        return queuedCommands.size() > 0;
    }

    public void close() {
        pclCommandReader.close();
    }

    public void setPclCommandFactory(PclCommandFactory pclCommandFactory) {
        this.pclCommandFactory = pclCommandFactory;
    }

    private static enum CommandState {
        INIT, CAPTURE, BUILD
    }
}