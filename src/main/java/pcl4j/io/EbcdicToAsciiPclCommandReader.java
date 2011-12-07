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

import java.io.UnsupportedEncodingException;

/**
 * PCLCommandReader that converts data from EBCDIC to ASCII
 */
public class EbcdicToAsciiPclCommandReader implements PclCommandReader {
    private final PclCommandReader pclCommandReader;
    private PclCommandFactory pclCommandFactory = new PclCommandFactory();

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
            if (isTextData(originalCommand)) {
                byte[] asciiBytes = convertToAscii(originalCommand.getBytes());
                originalCommand = pclCommandFactory.build(originalCommand.getPosition(), asciiBytes);
            }
        }
        return originalCommand;
    }

    public void close() {
        pclCommandReader.close();
    }

    private byte[] convertToAscii(byte[] ebcdicBytes) {
        try {
            return new String(ebcdicBytes, "cp037").getBytes();
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }


    private boolean isTextData(PclCommand originalCommand) {
        return originalCommand instanceof TextCommand;
    }
}
