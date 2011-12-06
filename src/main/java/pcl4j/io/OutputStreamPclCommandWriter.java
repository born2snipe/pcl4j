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


import java.io.IOException;
import java.io.OutputStream;

/**
 * A PclCommandWriter implementation backed by an OutputStream
 */
public class OutputStreamPclCommandWriter implements PclCommandWriter {
    private final OutputStream output;

    public OutputStreamPclCommandWriter(OutputStream output) {
        if (output == null) throw new IllegalArgumentException("A 'null' outputStream was given");
        this.output = output;
    }

    public void write(PclCommand command) {
        if (command == null) {
            throw new NullPointerException("Sorry I do not know how to write a null command");
        }
        try {
            output.write(command.getBytes());
        } catch (IOException e) {
            throw new PclCommandWriterException("A problem has occurred while trying to write a pcl command", e);
        }
    }

    public void close() {
        try {
            output.close();
        } catch (IOException e) {
        }
    }
}
