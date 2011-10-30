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
 * Uses the PclCommandCompressor when writing commands
 */
public class CompressedPclCommandWriter implements PclCommandWriter {
    private final PclCommandWriter pclCommandWriter;
    private PclCommandCompressor pclCommandCompressor = new PclCommandCompressor();
    private PclCommand lastParameterizedCommand = null;

    public CompressedPclCommandWriter(PclCommandWriter pclCommandWriter) {
        this.pclCommandWriter = pclCommandWriter;
    }

    public void write(PclCommand command) throws PclCommandWriterException {
        if (lastParameterizedCommand == null) {
            lastParameterizedCommand = command;
        } else if (pclCommandCompressor.canBeCompressed(command, lastParameterizedCommand)) {
            lastParameterizedCommand = pclCommandCompressor.compress(lastParameterizedCommand, command);
        } else {
            pclCommandWriter.write(lastParameterizedCommand);
            lastParameterizedCommand = command;
        }
    }

    /**
     * Clean up the resources occupied and write any remaining commands
     */
    public void close() {
        if (lastParameterizedCommand != null) {
            pclCommandWriter.write(lastParameterizedCommand);
        }
        pclCommandWriter.close();
    }

    public void setPclCommandCompressor(PclCommandCompressor pclCommandCompressor) {
        this.pclCommandCompressor = pclCommandCompressor;
    }
}
