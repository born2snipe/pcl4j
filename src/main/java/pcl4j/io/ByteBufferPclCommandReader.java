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
 * A PclCommandReader implementation using the java.nio.ByteBuffer
 */
public class ByteBufferPclCommandReader implements PclCommandReader {
    private PclCommandFactory pclCommandFactory = new PclCommandFactory();
    private PclUtil pclUtil = new PclUtil();
    protected ByteBuffer buffer;
    private long filePosition = 0;
    private Queue<PclCommand> queuedCommands = new LinkedList<PclCommand>();
    private ByteArrayOutputStream commandPrefixBytes = new ByteArrayOutputStream(3);
    private ByteArrayOutputStream commandData = new ByteArrayOutputStream(1024);
    private ByteArrayOutputStream valueData = new ByteArrayOutputStream(16);
    private long commandPosition;

    public ByteBufferPclCommandReader(byte[] entirePclFileContents) {
        this(ByteBuffer.wrap(entirePclFileContents));
    }

    public ByteBufferPclCommandReader(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    protected ByteBufferPclCommandReader() {
    }

    public void skip(long numberOfBytesToSkip) throws PclCommandReaderException {
        int newPosition = (int) (buffer.position() + numberOfBytesToSkip);
        buffer.position(newPosition);
        filePosition = newPosition;
    }

    public PclCommand nextCommand() throws PclCommandReaderException {
        if (commandsAreQueued()) {
            return queuedCommands.remove();
        }

        if (isFileEmpty() || isEOF()) {
            return null;
        }

        valueData.reset();
        commandData.reset();
        commandPrefixBytes.reset();
        commandPosition = filePosition;

        byte currentByte = readNextByte();
        if (pclUtil.isEscape(currentByte)) {
            pclCommand();
        } else {
            undoRead();
            textCommand();
        }
        return queuedCommands.remove();
    }

    private void pclCommand() {
        commandData.write(PclUtil.ESCAPE);
        commandPrefixBytes.write(PclUtil.ESCAPE);

        byte currentByte = readNextByte();
        if (pclUtil.is2ByteCommandOperator(currentByte)) {
            commandData.write(currentByte);
            queueUpCommand();
        } else if (pclUtil.isParameterizedCharacter(currentByte)) {
            commandData.write(currentByte);
            commandPrefixBytes.write(currentByte);
            currentByte = readNextByte();

            if (pclUtil.isGroupCharacter(currentByte)) {
                commandData.write(currentByte);
                commandPrefixBytes.write(currentByte);

                do {
                    currentByte = readNextByte();

                    if (isACompoundCommand(currentByte)) {
                        writeValueDataToCommand();
                        currentByte = pclUtil.changeParameterToTerminator(currentByte);
                        commandData.write(currentByte);
                        queueUpCommand();
                        copyCommandPrefixToCommand();
                    } else if (isTerminationByte(currentByte)) {
                        writeValueDataToCommand();
                        commandData.write(currentByte);
                        captureBinaryDataAsNeeded();
                        queueUpCommand();
                        break;
                    } else {
                        valueData.write(currentByte);
                    }
                } while (true);
            } else {
                commandData.write(currentByte);

                do {
                    currentByte = readNextByte();
                    if (isTerminationByte(currentByte)) {
                        writeValueDataToCommand();
                        commandData.write(currentByte);
                        if (pclUtil.isUniversalExit(commandData.toByteArray())) {
                            while (isNextByteNotAnEscapeByte()) {
                                commandData.write(readNextByte());
                            }
                        }
                        queueUpCommand();
                        break;
                    } else {
                        valueData.write(currentByte);
                    }
                } while (isNotEOF());
            }
        }
    }

    private boolean isTerminationByte(byte currentByte) {
        return pclUtil.isTermination((byte) Character.toUpperCase(currentByte));
    }

    private boolean isACompoundCommand(byte currentByte) {
        return pclUtil.isParameterCharacter(currentByte) && isNextByteNotAnEscapeByte();
    }

    private void copyCommandPrefixToCommand() {
        commandData.write(commandPrefixBytes.toByteArray(), 0, commandPrefixBytes.toByteArray().length);
    }

    private void queueUpCommand() {
        queuedCommands.add(pclCommandFactory.build(commandPosition, commandData.toByteArray()));
        valueData.reset();
        commandData.reset();
        commandPosition = filePosition;
    }

    private void writeValueDataToCommand() {
        try {
            valueData.writeTo(commandData);
        } catch (IOException e) {
            throw new PclCommandReaderException("A problem writing the value to the command", e);
        }
    }

    private boolean isNextByteNotAnEscapeByte() {
        return !isNextByteAnEscapeByte();
    }

    private void captureBinaryDataAsNeeded() {
        if (pclUtil.isCommandExpectingData(commandData.toByteArray())) {
            Integer numberOfBytesToRead = commandValueAsInt();
            int count = 0;
            while (isNotEOF() && count < numberOfBytesToRead) {
                commandData.write(readNextByte());
                count++;
            }
        }
    }

    private Integer commandValueAsInt() {
        String valueAsString = new String(valueData.toByteArray()).replaceAll("\\+|\\.[0-9]*|\\s+", "");
        return valueAsString.length() == 0 ? 0 : Integer.valueOf(valueAsString);
    }

    private void textCommand() {
        while (isNotEOF() && !isNextByteAnEscapeByte()) {
            commandData.write(readNextByte());
        }

        if (commandData.toByteArray().length > 0) {
            queueUpCommand();
        }
    }

    private boolean isNextByteAnEscapeByte() {
        boolean result = true;
        if (isNotEOF()) {
            result = pclUtil.isEscape(peekAtNextByte());
        }
        return result;
    }

    private void undoRead() {
        buffer.reset();
        filePosition--;
    }

    private byte peekAtNextByte() {
        byte data = buffer.get();
        buffer.position(buffer.position() - 1);
        return data;
    }

    private byte readNextByte() {
        buffer.mark();
        byte currentByte = buffer.get();
        filePosition++;
        return currentByte;
    }

    private boolean isNotEOF() {
        return !isEOF();
    }

    private boolean isEOF() {
        return buffer.position() >= buffer.capacity();
    }

    private boolean isFileEmpty() {
        return buffer.capacity() == 0;
    }

    private boolean commandsAreQueued() {
        return queuedCommands.size() > 0;
    }

    public void close() {

    }

}
