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
        long commandPosition = filePosition;
        byte currentByte = readNextByte();
        if (pclUtil.isEscape(currentByte)) {
            pclCommand(commandPosition);
            return queuedCommands.remove();
        } else {
            textCommand(commandPosition, currentByte);
            return queuedCommands.remove();
        }
    }

    private void pclCommand(long initialCommandPosition) {
        commandData.write(PclUtil.ESCAPE);
        commandPrefixBytes.reset();
        commandPrefixBytes.write(PclUtil.ESCAPE);

        long commandPosition = initialCommandPosition;

        byte currentByte = readNextByte();
        if (pclUtil.is2ByteCommandOperator(currentByte)) {
            commandData.write(currentByte);
            queueUpCommand(commandPosition);
        } else if (pclUtil.isParameterizedCharacter(currentByte)) {
            commandData.write(currentByte);
            commandPrefixBytes.write(currentByte);
            currentByte = readNextByte();

            if (pclUtil.isGroupCharacter(currentByte)) {
                commandData.write(currentByte);
                commandPrefixBytes.write(currentByte);

                do {
                    currentByte = readNextByte();

                    if (pclUtil.isParameterCharacter(currentByte) && isNextByteNotAnEscapeByte()) {
                        writeValueDataToCommand();
                        currentByte = pclUtil.changeParameterToTerminator(currentByte);
                        commandData.write(currentByte);
                        queueUpCommand(commandPosition);

                        commandPosition = filePosition;
                        valueData.reset();
                        commandData.reset();
                        commandData.write(commandPrefixBytes.toByteArray(), 0, 3);
                    } else if (pclUtil.isTermination((byte) Character.toUpperCase(currentByte))) {
                        writeValueDataToCommand();
                        commandData.write(currentByte);
                        break;
                    } else {
                        valueData.write(currentByte);
                    }
                } while (true);

                if (pclUtil.isCommandExpectingData(commandData.toByteArray())) {
                    captureBinaryData(commandData);
                }

                queueUpCommand(commandPosition);
            } else {
                commandData.write(currentByte);

                do {
                    currentByte = readNextByte();
                    if (pclUtil.isTermination((byte) Character.toUpperCase(currentByte))) {
                        writeValueDataToCommand();
                        commandData.write(currentByte);
                        break;
                    } else {
                        valueData.write(currentByte);
                    }
                } while (true);

                queueUpCommand(commandPosition);
            }
        }
    }

    private void queueUpCommand(long commandPosition) {
        queuedCommands.add(pclCommandFactory.build(commandPosition, commandData.toByteArray()));
    }

    private void writeValueDataToCommand() {
        try {
            valueData.writeTo(commandData);
        } catch (IOException e) {
            throw new PclCommandReaderException("A problem writing the value", e);
        }
    }

    private boolean isNextByteNotAnEscapeByte() {
        return !isNextByteAnEscapeByte();
    }

    private void captureBinaryData(ByteArrayOutputStream commandData) {
        String valueAsString = new String(pclUtil.getValue(commandData.toByteArray())).replaceAll("\\+|\\.[0-9]*|\\s+", "");
        Integer numberOfBytesToRead = valueAsString.length() == 0 ? 0 : Integer.valueOf(valueAsString);
        int count = 0;
        while (isNotEOF() && count < numberOfBytesToRead) {
            commandData.write(readNextByte());
            count++;
        }
    }

    private void textCommand(long commandPosition, byte currentByte) {
        commandData.write(currentByte);

        while (isNotEOF() && !isNextByteAnEscapeByte()) {
            commandData.write(readNextByte());
        }

        queueUpCommand(commandPosition);
    }

    private boolean isNextByteAnEscapeByte() {
        boolean result = true;
        if (isNotEOF()) {
            result = pclUtil.isEscape(peekAtNextByte());
        }
        return result;
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
