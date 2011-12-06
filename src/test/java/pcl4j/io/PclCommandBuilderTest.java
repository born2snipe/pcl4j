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


import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static pcl4j.io.AssertPcl.assertParameterizedCommand;
import static pcl4j.io.PclUtil.*;

public class PclCommandBuilderTest {

    @Test
    public void shouldBeAbleToMakeCopiesOfABuilderAndTheChildShouldNotAffectTheParent() {
        PclCommandBuilder parent = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE)
                .g(LOWEST_GROUP_BYTE).t(LOWEST_TERMINATION_BYTE);

        PclCommand childCommand = parent.copy().v("2").toCommand();

        PclCommand parentCommand = parent.v("1").toCommand();

        byte[] expectedParent = {ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE};
        byte[] expectedChild = {ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '2', LOWEST_TERMINATION_BYTE};

        assertParameterizedCommand(-1, expectedParent, parentCommand);
        assertParameterizedCommand(-1, expectedChild, childCommand);
    }

    @Test
    public void toBytes_shouldBeAbleToBuildAnEntireCommand() {
        byte[] command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE)
                .g(LOWEST_GROUP_BYTE).v("10").t(LOWEST_TERMINATION_BYTE).toBytes();

        byte[] expected = {ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', '0', LOWEST_TERMINATION_BYTE};
        assertTrue(Arrays.equals(expected, command));
    }

    @Test
    public void toCommand_shouldDefaultTheValueToNothingWhenNoValueIsGiven() {
        PclCommand command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE)
                .g(LOWEST_GROUP_BYTE).t(LOWEST_TERMINATION_BYTE).toCommand();

        byte[] expected = {ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, LOWEST_TERMINATION_BYTE};
        assertParameterizedCommand(-1, expected, command);
    }

    @Test
    public void toCommand_shouldBeAbleToBuildAnEntireCommandWithBinaryData() {
        PclCommand command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE)
                .g(LOWEST_GROUP_BYTE).v("10").t(LOWEST_TERMINATION_BYTE).d("10".getBytes()).toCommand();

        byte[] expected = {ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', '0', LOWEST_TERMINATION_BYTE, '1', '0'};
        assertParameterizedCommand(-1, expected, command);
    }

    @Test
    public void toCommand_shouldBeAbleToBuildAnEntireCommand() {
        PclCommand command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE)
                .g(LOWEST_GROUP_BYTE).v("10").t(LOWEST_TERMINATION_BYTE).toCommand();

        byte[] expected = {ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', '0', LOWEST_TERMINATION_BYTE};
        assertParameterizedCommand(-1, expected, command);
    }

    @Test
    public void toCommand_shouldBeAbleToPassInThePosition() {
        PclCommand command = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE)
                .g(LOWEST_GROUP_BYTE).v("10").t(LOWEST_TERMINATION_BYTE).toCommand(10);

        byte[] expected = {ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', '0', LOWEST_TERMINATION_BYTE};
        assertParameterizedCommand(10, expected, command);
    }

    @Test
    public void copy_shouldCopyThe_parameterizedByte() {
        PclCommandBuilder builder = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE);
        assertEquals(builder.toCommand(), builder.copy().toCommand());
    }

    @Test
    public void copy_shouldCopyThe_groupByte() {
        PclCommandBuilder builder = new PclCommandBuilder().g(LOWEST_GROUP_BYTE);
        assertEquals(builder.toCommand(), builder.copy().toCommand());
    }

    @Test
    public void copy_shouldCopyThe_terminatorByte() {
        PclCommandBuilder builder = new PclCommandBuilder().t(LOWEST_TERMINATION_BYTE);
        assertEquals(builder.toCommand(), builder.copy().toCommand());
    }

    @Test
    public void copy_shouldCopyThe_value() {
        PclCommandBuilder builder = new PclCommandBuilder().v("blah");
        assertEquals(builder.toCommand(), builder.copy().toCommand());
    }

    @Test
    public void copy_shouldCopyThe_data() {
        PclCommandBuilder builder = new PclCommandBuilder().d("blah".getBytes());
        assertEquals(builder.toCommand(), builder.copy().toCommand());
    }

    @Test
    public void shouldReturnTheSameInstanceOfTheBuilderWhenAParameterizedByteIsGiven() {
        PclCommandBuilder builder = new PclCommandBuilder();
        assertSame(builder, builder.p(LOWEST_PARAMETERIZED_BYTE));
    }

    @Test
    public void shouldReturnTheSameInstanceOfTheBuilderWhenAGroupByteIsGiven() {
        PclCommandBuilder builder = new PclCommandBuilder();
        assertSame(builder, builder.g(LOWEST_GROUP_BYTE));
    }

    @Test
    public void shouldReturnTheSameInstanceOfTheBuilderWhenATerminatorByteIsGiven() {
        PclCommandBuilder builder = new PclCommandBuilder();
        assertSame(builder, builder.t(LOWEST_TERMINATION_BYTE));
    }

    @Test
    public void shouldReturnTheSameInstanceOfTheBuilderWhenAValueIsGiven() {
        PclCommandBuilder builder = new PclCommandBuilder();
        assertSame(builder, builder.v("10"));
    }

    @Test
    public void shouldNotBlowUpIfTheBuilderIsToldNotToValidate_AnInvalidParameterizedByteIsGiven() {
        new PclCommandBuilder(false).p(-1);
    }

    @Test
    public void shouldNotBlowUpIfTheBuilderIsToldNotToValidate_AnInvalidGroupByteIsGiven() {
        new PclCommandBuilder(false).g(-1);
    }

    @Test
    public void shouldNotBlowUpIfTheBuilderIsToldNotToValidate_AnInvalidTerminatorByteIsGiven() {
        new PclCommandBuilder(false).t(-1);
    }

    @Test
    public void shouldNotBlowUpIfTheBuilderIsToldNotToValidate_AnInvalidValueIsGiven() {
        new PclCommandBuilder(false).v(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfAnInvalidParameterizedByteIsGiven() {
        new PclCommandBuilder().p(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfAnInvalidGroupByteIsGiven() {
        new PclCommandBuilder().g(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfAnInvalidTerminatorByteIsGiven() {
        new PclCommandBuilder().t(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfAnInvalidValueIsGiven() {
        new PclCommandBuilder().v(null);
    }
}
