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
        PclCommand command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE).v("1").t(LOWEST_TERMINATION_BYTE).toCommand();
        assertTrue(compressor.canBeCompressed(command, command));
    }

    @Test
    public void canBeCompressed_bothCommandAreParameterizedCommandButWithDifferentParameterized() {
        PclCommand command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE).v("1").t(LOWEST_TERMINATION_BYTE).toCommand();
        PclCommand otherCommand = new PclCommandBuilder().p(HIGHEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE).v("1").t(LOWEST_TERMINATION_BYTE).toCommand();
        assertFalse(compressor.canBeCompressed(command, otherCommand));
    }

    @Test
    public void canBeCompressed_bothCommandAreParameterizedCommandButWithDifferentGroups() {
        PclCommandBuilder builder = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).v("1").t(LOWEST_TERMINATION_BYTE);

        PclCommand command = builder.copy().g(LOWEST_GROUP_BYTE).toCommand();
        PclCommand otherCommand = builder.copy().g(HIGHEST_GROUP_BYTE).toCommand();

        assertFalse(compressor.canBeCompressed(command, otherCommand));
    }

    @Test
    public void canBeCompressed_bothCommandAreParameterizedButFirstCommandHasBinaryData() {
        PclCommandBuilder builder = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE).v("1").t(LOWEST_TERMINATION_BYTE);

        PclCommand command = builder.copy().d("12").toCommand();
        PclCommand otherCommand = builder.toCommand();

        assertFalse(compressor.canBeCompressed(command, otherCommand));
    }

    @Test
    public void canBeCompressed_firstCommandIsATextCommand() {
        assertFalse(compressor.canBeCompressed(new TextCommand("X".getBytes()), new ParameterizedCommand(new byte[3])));
    }

    @Test
    public void canBeCompressed_secondCommandIsATextCommand() {
        assertFalse(compressor.canBeCompressed(new ParameterizedCommand(new byte[3]), new TextCommand("X".getBytes())));
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
        PclCommand command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE).v("1").t(LOWEST_TERMINATION_BYTE).toCommand();

        ParameterizedCommand expectedCompressedCommand = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_PARAMETER_BYTE, '1', LOWEST_TERMINATION_BYTE
        });

        PclCommand compressedCommand = compressor.compress(command, command);

        assertNotNull(compressedCommand);
        assertEquals(expectedCompressedCommand, compressedCommand);
    }

    @Test(expected = IllegalArgumentException.class)
    public void compress_shouldBlowUpIfTheCommandsCanNotBeCompressed() {
        PclCommandBuilder builder = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).v("1").t(LOWEST_TERMINATION_BYTE);

        PclCommand command = builder.copy().g(LOWEST_GROUP_BYTE).toCommand();
        PclCommand otherCommand = builder.copy().g(HIGHEST_GROUP_BYTE).toCommand();

        compressor.compress(command, otherCommand);
    }


}
