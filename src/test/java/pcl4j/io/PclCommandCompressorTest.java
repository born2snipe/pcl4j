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


import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;
import static pcl4j.io.PclUtil.*;

public class PclCommandCompressorTest {
    private PclCommandCompressor compressor;

    @Before
    public void setUp() throws Exception {
        compressor = new PclCommandCompressor();
    }

    @Test
    public void canBeCompressed_bothCommandAreParameterizedCommandWithMatchingParameterizedAndGroupBytes() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });
        assertTrue(compressor.canBeCompressed(command, command));
    }

    @Test
    public void canBeCompressed_bothCommandAreParameterizedCommandButWithDifferentParameterized() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });
        ParameterizedCommand otherCommand = new ParameterizedCommand(new byte[]{
                ESCAPE, HIGHEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });
        assertFalse(compressor.canBeCompressed(command, otherCommand));
    }

    @Test
    public void canBeCompressed_bothCommandAreParameterizedCommandButWithDifferentGroups() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });
        ParameterizedCommand otherCommand = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, HIGHEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });
        assertFalse(compressor.canBeCompressed(command, otherCommand));
    }

    @Test
    public void canBeCompressed_bothCommandAreParameterizedButFirstCommandHasBinaryData() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE, '1', '2'
        });
        ParameterizedCommand otherCommand = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });
        assertFalse(compressor.canBeCompressed(command, otherCommand));
    }

    @Test
    public void canBeCompressed_secondCommandIsNullAndCanNotBeCompressed() {
        assertFalse(compressor.canBeCompressed(new ParameterizedCommand(new byte[3]), null));
    }

    @Test
    public void canBeCompressed_firstCommandIsNullAndCanNotBeCompressed() {
        assertFalse(compressor.canBeCompressed(null, new ParameterizedCommand(new byte[3])));
    }

    @Test
    public void canBeCompressed_secondCommandIsATwoByteCommandAndCanNotBeCompressed() {
        assertFalse(compressor.canBeCompressed(new ParameterizedCommand(new byte[3]), new TwoByteCommand(new byte[2])));
    }

    @Test
    public void canBeCompressed_firstCommandIsATwoByteCommandAndCanNotBeCompressed() {
        assertFalse(compressor.canBeCompressed(new TwoByteCommand(new byte[2]), new ParameterizedCommand(new byte[3])));
    }

    @Test
    public void compress_shouldReturnACompressedVersionOfTheTwoCommands() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });

        ParameterizedCommand expectedCompressedCommand = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_PARAMETER_BYTE, '1', LOWEST_TERMINATION_BYTE
        });

        PclCommand compressedCommand = compressor.compress(command, command);

        assertNotNull(compressedCommand);
        assertEquals(expectedCompressedCommand, compressedCommand);
    }

    @Test(expected = IllegalArgumentException.class)
    public void compress_shouldBlowUpIfTheCommandsCanNotBeCompressed() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });
        ParameterizedCommand otherCommand = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, HIGHEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });

        compressor.compress(command, otherCommand);
    }


}
