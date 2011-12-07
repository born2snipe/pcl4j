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
 * A factory that builds a PclCommand object
 */
public class PclCommandFactory {
    private static final PclUtil UTIL = new PclUtil();

    /**
     * Build a PclCommand from the given bytes and position
     *
     * @param position    - the location of the ESC byte of the command
     * @param commandData - the bytes that make of the entire command
     * @return a PclCommand object
     */
    public PclCommand build(long position, byte[] commandData) {
        if (UTIL.isEscape(commandData[0])) {
            if (commandData.length == 2) {
                return new TwoByteCommand(position, commandData);
            }

            return new ParameterizedCommand(position, commandData);
        }

        return new TextCommand(position, commandData);
    }

}
