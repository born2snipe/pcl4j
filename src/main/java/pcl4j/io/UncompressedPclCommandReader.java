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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Uncompresses parameterized commands into individual commands
 */
public class UncompressedPclCommandReader implements PclCommandReader {
    private final PclUtil pclUtil = new PclUtil();
    private final Queue<PclCommand> queuedCommands = new LinkedList<PclCommand>();
    private final byte[] commandLeadingBytes = new byte[3];
    private final ByteArrayOutputStream uncompressedCommandData = new ByteArrayOutputStream();
    private final PclCommandReader pclCommandReader;
    private final PclCommandFactory pclCommandFactory = new PclCommandFactory();
    private long commandPosition = -1L;
    private CommandState state;

    public UncompressedPclCommandReader(PclCommandReader pclCommandReader) {
        if (pclCommandReader == null) {
            throw new IllegalArgumentException("The given pcl command reader is null");
        }
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
        if (command != null && isCompressed(command)) {
            uncompressCommand(command);
            command = queuedCommands.remove();
        }
        return command;
    }

    private boolean isCompressed(PclCommand command) {
        return command instanceof ParameterizedCommand;
    }

    private void uncompressCommand(PclCommand compressedCommand) {
        ByteBuffer compressedData = ByteBuffer.wrap(compressedCommand.getBytes());
        compressedData.get(commandLeadingBytes);

        byte currentByte = -1;
        long position = compressedCommand.getPosition() - 1;
        commandPosition = -1L;
        try {
            state = CommandState.INIT;
            while (compressedData.position() < compressedData.capacity()) {
                currentByte = compressedData.get();
                position++;
                if (state == CommandState.INIT) {
                    commandPosition = position;
                    uncompressedCommandData.write(commandLeadingBytes);
                    state = CommandState.CAPTURE;
                }

                if (isTerminator(currentByte)) {
                    state = CommandState.BUILD_AND_CAPTURE_REMAINING;
                } else if (isStartOfNextCommand(currentByte)) {
                    state = CommandState.BUILD;
                } else {
                    uncompressedCommandData.write(currentByte);
                }

                if (shouldBuildCommand()) {
                    updateCommandPosition();
                    uncompressedCommandData.write(toTerminator(currentByte));

                    if (shouldCaptureRemainingBytes()) {
                        uncompressedCommandData.write(captureRemainingBytes(compressedData));
                    }

                    queueUpCommand();
                    resetForNextCommand();
                }
            }
        } catch (IOException e) {
        } finally {
            resetForNextCommand();
        }
    }

    private boolean shouldBuildCommand() {
        return state == CommandState.BUILD || state == CommandState.BUILD_AND_CAPTURE_REMAINING;
    }

    private boolean isTerminator(byte currentByte) {
        return pclUtil.isTermination(currentByte);
    }

    private boolean shouldCaptureRemainingBytes() {
        return state == CommandState.BUILD_AND_CAPTURE_REMAINING;
    }

    private void updateCommandPosition() {
        if (commandsAreQueuedUp()) {
            commandPosition += commandLeadingBytes.length;
        }
    }

    private void queueUpCommand() {
        queuedCommands.add(pclCommandFactory.build(commandPosition, uncompressedCommandData.toByteArray()));
    }

    private void resetForNextCommand() {
        uncompressedCommandData.reset();
        state = CommandState.INIT;
    }

    private byte[] captureRemainingBytes(ByteBuffer buffer) throws IOException {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }

    private byte toTerminator(byte currentByte) {
        return pclUtil.isTermination(currentByte) ? currentByte : pclUtil.changeParameterToTerminator(currentByte);
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

    private static enum CommandState {
        INIT, CAPTURE, BUILD, BUILD_AND_CAPTURE_REMAINING
    }
}
