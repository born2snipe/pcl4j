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
 * Utility class for checking for the magic PCL bytes
 */
public class PclUtil {
    public static final byte ESCAPE = 27;
    public static final byte LOWEST_2BYTE_COMMAND_OPERATOR = 48;
    public static final byte HIGHEST_2BYTE_COMMAND_OPERATOR = 126;
    public static final byte LOWEST_TERMINATION_BYTE = 64;
    public static final byte HIGHEST_TERMINATION_BYTE = 94;
    public static final byte LOWEST_PARAMETERIZED_BYTE = 33;
    public static final byte HIGHEST_PARAMETERIZED_BYTE = 47;
    public static final byte LOWEST_GROUP_BYTE = 96;
    public static final byte HIGHEST_GROUP_BYTE = 126;
    public static final byte LOWEST_PARAMETER_BYTE = 96;
    public static final byte HIGHEST_PARAMETER_BYTE = 126;

    public boolean is2ByteCommandOperator(byte value) {
        return value >= LOWEST_2BYTE_COMMAND_OPERATOR && value <= HIGHEST_2BYTE_COMMAND_OPERATOR;
    }

    public boolean isEscape(byte value) {
        return value == ESCAPE;
    }

    public boolean isTermination(byte value) {
        return value >= LOWEST_TERMINATION_BYTE && value <= HIGHEST_TERMINATION_BYTE;
    }

    public boolean isParameterizedCharacter(byte value) {
        return value >= LOWEST_PARAMETERIZED_BYTE && value <= HIGHEST_PARAMETERIZED_BYTE;
    }

    public boolean isGroupCharacter(byte value) {
        return value >= LOWEST_GROUP_BYTE && value <= HIGHEST_GROUP_BYTE;
    }

    public boolean isParameterCharacter(byte value) {
        return value >= LOWEST_PARAMETER_BYTE && value <= HIGHEST_PARAMETER_BYTE;
    }
}
