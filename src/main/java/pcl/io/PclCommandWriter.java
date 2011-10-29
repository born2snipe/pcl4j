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


/**
 * A writer that writes PCL commands
 */
public interface PclCommandWriter {
    /**
     * Attempts to write the given PclCommand
     *
     * @param command - the command to write
     * @throws PclCommandWriterException is thrown when a problem occurs
     */
    void write(PclCommand command) throws PclCommandWriterException;

    /**
     * Clean up the resources occupied
     */
    void close();

    /**
     * Exception thrown when a problem occurs in the PclCommandWriter
     */
    public static class PclCommandWriterException extends RuntimeException {
        public PclCommandWriterException(String s) {
            super(s);
        }

        public PclCommandWriterException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }
}
