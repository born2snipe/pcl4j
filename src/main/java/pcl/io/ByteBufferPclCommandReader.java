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
        int newPosition = (int) (filePosition + numberOfBytesToSkip);
        buffer.position(newPosition);
        filePosition = newPosition;
    }

    public PclCommand nextCommand() throws PclCommandReaderException {
        if (isFileEmpty() || isEOF()) {
            return null;
        }

        if (lookForTheNextEscapeByte()) {
            PclCommand command = null;
            long commandPosition = filePosition;
            ByteArrayOutputStream commandData = new ByteArrayOutputStream();
            commandData.write(readNextByte()); // Escape byte
            boolean isFirstRead = true;
            boolean commandCompleted = false;

            while (!commandCompleted && isNotEOF()) {
                byte currentByte = readNextByte();

                if (pclUtil.is2ByteCommandOperator(currentByte) && isFirstRead) {
                    commandData.write(currentByte);
                    commandCompleted = true;
                } else if (pclUtil.isEscape(currentByte)) {
                    if (isNextByteAParameterizedByte()) {
                        commandCompleted = true;
                        buffer.reset();
                        filePosition--;
                    } else {
                        commandData.write(currentByte);
                    }
                } else if (pclUtil.isParameterCharacter(currentByte)) {
                    if (isNextByteAnEscapeByte()) {
                        currentByte = pclUtil.changeParameterToTerminator(currentByte);
                    }
                    commandData.write(currentByte);
                } else {
                    commandData.write(currentByte);
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

        return null;
    }

    private boolean isNextByteAnEscapeByte() {
        boolean result = true;
        if (isNotEOF()) {
            result = pclUtil.isEscape(peekAtNextByte());
        }
        return result;
    }

    private boolean isNextByteAParameterizedByte() {
        boolean result = false;
        if (isNextByteNotEOF()) {
            result = pclUtil.isParameterizedCharacter(peekAtNextByte());
        }
        return result;
    }

    private byte peekAtNextByte() {
        byte data = buffer.get();
        buffer.position(buffer.position() - 1);
        return data;
    }

    private boolean isNextByteNotEOF() {
        return filePosition + 1 < buffer.capacity();
    }

    private byte readNextByte() {
        buffer.mark();
        byte currentByte = buffer.get();
        filePosition++;
        return currentByte;
    }

    private boolean lookForTheNextEscapeByte() {
        while (readNextByte() != PclUtil.ESCAPE && isNotEOF()) ;

        if (isEOF()) {
            return false;
        }

        buffer.reset();
        filePosition--;
        return true;
    }

    private boolean isNotEOF() {
        return !isEOF();
    }

    private boolean isEOF() {
        return filePosition >= buffer.capacity();
    }

    private boolean isFileEmpty() {
        return buffer.capacity() == 0;
    }

    public void close() {

    }
}
