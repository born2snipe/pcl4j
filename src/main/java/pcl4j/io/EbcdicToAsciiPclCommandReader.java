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
import java.io.UnsupportedEncodingException;

/**
 * PCLCommandReader that converts data from EBCDIC to ASCII
 */
public class EbcdicToAsciiPclCommandReader implements PclCommandReader {
    private final PclCommandReader pclCommandReader;
    private PclCommandFactory pclCommandFactory = new PclCommandFactory();
    private PclUtil pclUtil = new PclUtil();

    public EbcdicToAsciiPclCommandReader(PclCommandReader pclCommandReader) {
        this.pclCommandReader = pclCommandReader;
        if (pclCommandReader == null) {
            throw new NullPointerException("Null PclCommandReader given");
        }
    }

    public void skip(long numberOfBytesToSkip) throws PclCommandReaderException {
        pclCommandReader.skip(numberOfBytesToSkip);
    }

    public PclCommand nextCommand() throws PclCommandReaderException {
        PclCommand originalCommand = pclCommandReader.nextCommand();
        if (originalCommand != null) {
            if (pclUtil.hasBinaryData(originalCommand) && isTextData(originalCommand)) {
                byte[] data = originalCommand.getBytes();
                boolean terminationByteFound = false;
                byte[] tempCommand = new byte[data.length];
                ByteArrayOutputStream ebcdicBytes = new ByteArrayOutputStream();
                int binaryDataStartingPosition = -1;

                for (int i = 0; i < data.length; i++) {
                    if (pclUtil.isTermination(data[i])) {
                        terminationByteFound = true;
                        binaryDataStartingPosition = i + 1;
                    }

                    if (!pclUtil.isTermination(data[i]) && terminationByteFound) {
                        ebcdicBytes.write(data[i]);
                    } else {
                        tempCommand[i] = data[i];
                    }
                }

                byte[] asciiBytes = convertToAscii(ebcdicBytes.toByteArray());
                try {
                    System.arraycopy(asciiBytes, 0, tempCommand, binaryDataStartingPosition, asciiBytes.length);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw e;
                }

                originalCommand = pclCommandFactory.build(originalCommand.getPosition(), tempCommand);
            }
        }
        return originalCommand;
    }

    private byte[] convertToAscii(byte[] ebcdicBytes) {
        try {
            return new String(ebcdicBytes, "cp037").getBytes();
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }


    private boolean isTextData(PclCommand originalCommand) {
        return pclUtil.getTerminatorByte(originalCommand) != 'W';
    }

    public void close() {
        pclCommandReader.close();
    }

    public void setPclCommandFactory(PclCommandFactory pclCommandFactory) {
        this.pclCommandFactory = pclCommandFactory;
    }
}
