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

/**
 * A reader that parses PCL commands
 */
public interface PclCommandReader {
    /**
     * Skip a section of the file
     *
     * @param numberOfBytesToSkip - the number of bytes to be skipped
     * @throws PclCommandReaderException when there is a problem skipping the number of bytes
     */
    void skip(long numberOfBytesToSkip) throws PclCommandReaderException;

    /**
     * Blocks until a valid PCL command is found or the EOF is reached
     *
     * @return null if the EOF is reached or a valid PCL command
     * @throws PclCommandReaderException when there is a problem parsing out a PCL command
     */
    PclCommand nextCommand() throws PclCommandReaderException;

    /**
     * Clean up the resources occupied
     */
    void close();

    /**
     * Exception thrown when a problem occurs in the PclCommandReader
     */
    public static class PclCommandReaderException extends RuntimeException {
        public PclCommandReaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
