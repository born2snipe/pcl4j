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

/**
 * A PclCommandReader implementation using the java.nio.ByteBuffer
 */
public class ByteBufferPclCommandReader implements PclCommandReader {
    private PclCommandFactory pclCommandFactory = new PclCommandFactory();
    private PclUtil pclUtil = new PclUtil();
    protected ByteBuffer buffer;
    private long filePosition = 0;

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
        if (isFileEmpty() || isEOF()) {
            return null;
        }

        long commandPosition = filePosition;
        byte currentByte = readNextByte();
        if (pclUtil.isEscape(currentByte)) {
            return pclCommand(commandPosition);
        } else {
            return textCommand(commandPosition, currentByte);
        }
    }

    private PclCommand pclCommand(long commandPosition) {
        PclCommand command = null;
        ByteArrayOutputStream commandData = new ByteArrayOutputStream();
        commandData.write(PclUtil.ESCAPE);
        boolean isFirstRead = true;
        boolean commandCompleted = false;

        while (!commandCompleted && isNotEOF()) {
            byte currentByte = readNextByte();
            commandData.write(currentByte);

            if (isFirstRead && pclUtil.is2ByteCommandOperator(currentByte)) {
                commandCompleted = true;
            } else if (pclUtil.isTermination(currentByte)) {
                if (pclUtil.isCommandExpectingData(commandData.toByteArray())) {
                    captureBinaryData(commandData);
                }
                commandCompleted = true;
            }

            isFirstRead = false;
        }

        if (isEOF()) {
            commandCompleted = true;
        }

        if (commandCompleted) {
            command = pclCommandFactory.build(commandPosition, commandData.toByteArray());
        }

        return command;
    }

    private void captureBinaryData(ByteArrayOutputStream commandData) {
        String valueAsString = new String(pclUtil.getValue(commandData.toByteArray())).replaceAll("\\.[0-9]*", "");
        Integer numberOfBytesToRead = Integer.valueOf(valueAsString);
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

    public void close() {

    }
}
