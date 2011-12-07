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

import java.util.Arrays;

import static junit.framework.Assert.*;

public class AssertPcl {

    public static void assert2ByteCommand(long expectedPosition, byte[] expectedBytes, PclCommand actualCommand) {
        assertEquals("not the right kind of command", TwoByteCommand.class, actualCommand.getClass());
        assertCommand(expectedPosition, expectedBytes, actualCommand);
    }

    public static void assertParameterizedCommand(long expectedPosition, byte[] expectedCommandBytes, PclCommand command) {
        assertEquals("not the right kind of command", ParameterizedCommand.class, command.getClass());
        assertCommand(expectedPosition, expectedCommandBytes, command);
    }

    public static void assertTextCommand(long expectedPosition, byte[] expectedCommandBytes, PclCommand command) {
        assertEquals("not the right kind of command", TextCommand.class, command.getClass());
        assertCommand(expectedPosition, expectedCommandBytes, command);
    }

    public static void assertCommand(long expectedPosition, byte[] expectedBytes, PclCommand actualCommand) {
        assertNotNull("The command should NOT be null", actualCommand);
        byte[] actualBytes = actualCommand.getBytes();
        assertTrue("The bytes in the command are not correct. expect=" + Arrays.toString(expectedBytes) + ", actual=" + Arrays.toString(actualBytes), Arrays.equals(expectedBytes, actualBytes));
        assertEquals("The position of the command in the file is wrong", expectedPosition, actualCommand.getPosition());
    }
}
