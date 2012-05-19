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

        long commandPosition = filePosition;
        byte currentByte = readNextByte();
        if (pclUtil.isEscape(currentByte)) {
            pclCommand(commandPosition);
            return queuedCommands.remove();
        } else {
            return textCommand(commandPosition, currentByte);
        }
    }

    private void pclCommand(long initialCommandPosition) {
        long commandPosition = initialCommandPosition;
        ByteArrayOutputStream commandPrefixBytes = new ByteArrayOutputStream();
        commandPrefixBytes.write(PclUtil.ESCAPE);
        ByteArrayOutputStream commandData = new ByteArrayOutputStream();
        commandData.write(PclUtil.ESCAPE);

        boolean isFirstRead = true;
        boolean captureByte = true;
        boolean escapeSequenceComplete = false;
        boolean groupByteFound = false;
        boolean parameterByteFound = false;
        boolean commandCompleted = false;
        boolean compressedCommand = false;

        while (!escapeSequenceComplete && isNotEOF()) {
            byte currentByte = readNextByte();

            if (!groupByteFound) {
                commandPrefixBytes.write(currentByte);
            }


            if (isTwoByteCommand(isFirstRead, currentByte)) {
                escapeSequenceComplete = true;
            } else if (!parameterByteFound) {
                parameterByteFound = pclUtil.isParameterizedCharacter(currentByte);
            } else if (!groupByteFound) {
                groupByteFound = pclUtil.isGroupCharacter(currentByte);
            } else if (groupByteFound && pclUtil.isParameterCharacter(currentByte) && isNextByteNotAnEscapeByte()) {
                commandCompleted = true;
                compressedCommand = true;
                currentByte = pclUtil.changeParameterToTerminator(currentByte);
            } else if (pclUtil.isTermination(currentByte)) {
                escapeSequenceComplete = true;
            } else if (pclUtil.isEscape(currentByte)) {
                undoRead();
                escapeSequenceComplete = true;
                captureByte = false;
            }

            if (captureByte) {
                commandData.write(currentByte);
            }

            if (escapeSequenceComplete || commandCompleted || isLastByteInFile()) {
                if (!isFirstRead && pclUtil.isCommandExpectingData(commandData.toByteArray())) {
                    captureBinaryData(commandData);
                }

                queuedCommands.add(pclCommandFactory.build(commandPosition, commandData.toByteArray()));
                commandPosition = filePosition;

                if (compressedCommand) {
                    commandData.reset();
                    commandData.write(commandPrefixBytes.toByteArray(), 0, 3);
                    commandCompleted = false;
                }
            }

            isFirstRead = false;
        }
    }

    private boolean isLastByteInFile() {
        if (isEOF()) {
            return true;
        }

        return buffer.position() == buffer.capacity();
    }

    private boolean isNextByteNotAnEscapeByte() {
        return !isNextByteAnEscapeByte();
    }

    private boolean isTwoByteCommand(boolean firstRead, byte currentByte) {
        return firstRead && pclUtil.is2ByteCommandOperator(currentByte);
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

    private void undoRead() {
        buffer.reset();
        filePosition--;
    }

    private PclCommand textCommand(long commandPosition, byte currentByte) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(currentByte);
        while (isNotEOF() && !isNextByteAnEscapeByte()) {
            output.write(readNextByte());
        }
        return pclCommandFactory.build(commandPosition, output.toByteArray());
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
