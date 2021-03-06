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


import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static pcl4j.io.AssertPcl.*;
import static pcl4j.io.PclUtil.*;

public class ByteBufferPclCommandReaderTest {
    @Test
    public void performanceTest() throws Exception {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("example/sample.pcl").toURI());
        long overalStart = System.currentTimeMillis();
        int times = 20;
        for (int i = 0; i < times; i++) {
            PclCommandReader reader = new MappedFilePclCommandReader(file);
            while (reader.nextCommand() != null) ;
        }
        long elapsedTimeAvg = (System.currentTimeMillis() - overalStart) / times;
        System.out.println(elapsedTimeAvg + " millis");
        assertTrue("The average time to parse the sample file is getting bigger (Avg:" + elapsedTimeAvg + " millis)", elapsedTimeAvg < 250);
    }

    @Test
    public void shouldHandleUniversalExitCommand_noData() {
        byte[] expectedCommand = ByteArrayUtil.concat(
                new byte[]{PclUtil.ESCAPE},
                "%-12345X".getBytes()
        );
        ByteBufferPclCommandReader reader = createReader(expectedCommand);

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
    }

    @Test
    public void shouldHandleUniversalExitCommand_withData() {
        byte[] expectedCommand = ByteArrayUtil.concat(
                new byte[]{PclUtil.ESCAPE},
                "%-12345X@PJL JOB".getBytes()
        );
        ByteBufferPclCommandReader reader = createReader(expectedCommand);

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
    }

    @Test
    public void shouldHandleSecondarySymbolSetsWithoutAGroupByte() {
        PclCommandBuilder command = new PclCommandBuilder(false).p(')').g('8').t('U');

        ByteBufferPclCommandReader reader = createReader(command.toBytes());

        assertParameterizedCommand(0L, command.toBytes(), reader.nextCommand());
    }

    @Test
    public void shouldHandleSymbolSetsWithoutAGroupByte() {
        PclCommandBuilder command = new PclCommandBuilder(false).p('(').g('8').t('U');

        ByteBufferPclCommandReader reader = createReader(command.toBytes());

        assertParameterizedCommand(0L, command.toBytes(), reader.nextCommand());
    }

    @Test
    public void shouldHandleCommandsWithALowerTerminatorAndLastCommandFollowedByText() {
        PclCommandBuilder baseCommand = new PclCommandBuilder(false).p('*').g('p').v("10");
        PclCommandBuilder cmd1 = baseCommand.copy().t('x');
        PclCommandBuilder cmd2 = new PclCommandBuilder(false).p('(').g('s').v("0").t('S');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(cmd1.toBytes(), cmd2.toBytes(), "Moved".getBytes()));

        assertParameterizedCommand(0L, cmd1.toCommand(), reader.nextCommand());
        assertParameterizedCommand(6L, cmd2.toCommand(), reader.nextCommand());
        assertTextCommand(11L, "Moved".getBytes(), reader.nextCommand());
    }

    @Test
    @Ignore
    public void shouldMakeAFormFeedItsOwnCommand() {
        ByteBufferPclCommandReader reader = createReader("\f12\f34\f".getBytes());

        assertTextCommand(0L, "\f".getBytes(), reader.nextCommand());
        assertTextCommand(1L, "12".getBytes(), reader.nextCommand());
        assertTextCommand(3L, "\f".getBytes(), reader.nextCommand());
        assertTextCommand(4L, "34".getBytes(), reader.nextCommand());
        assertTextCommand(5L, "\f".getBytes(), reader.nextCommand());
        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldHandle2CommandsFirstEndsWithALowercaseTerminator() {
        PclCommandBuilder baseCommand = new PclCommandBuilder(false).p('*').g('p').v("10");
        PclCommandBuilder cmd1 = baseCommand.copy().t('x');
        PclCommandBuilder cmd2 = new PclCommandBuilder(false).p('(').g('s').v("0").t('S');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(cmd1.toBytes(), cmd2.toBytes()));

        assertParameterizedCommand(0L, cmd1.toCommand(), reader.nextCommand());
        assertParameterizedCommand(6L, cmd2.toCommand(), reader.nextCommand());
    }

    @Test
    @Ignore("have seen in the wild...")
    public void shouldHandleCommandsWithALowerTerminatorAndFollowedByText() {
        PclCommandBuilder cmd1 = new PclCommandBuilder(false).p('*').g('p').v("10").t('x').d("I Am text");

        ByteBufferPclCommandReader reader = createReader(cmd1.toBytes());

        assertParameterizedCommand(0L, cmd1.toBytes(), reader.nextCommand());
    }


    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_PaddedValue() {
        PclCommandBuilder builder = commandExpectingBinaryData(" 4 ").d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
        assertTextCommand(11L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_PositiveValue() {
        PclCommandBuilder builder = commandExpectingBinaryData("+4").d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
        assertTextCommand(10L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_DecimalValue() {
        PclCommandBuilder builder = commandExpectingBinaryData("4.0").d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
        assertTextCommand(11L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_IntValue() {
        PclCommandBuilder builder = commandExpectingBinaryData("4").d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
        assertTextCommand(9L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_NoValue() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').t('E');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "data12".getBytes()));

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
        assertTextCommand(4L, "data12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldCaptureBinaryDataWhenTheCommandIs_DecimalValue() {
        PclCommandBuilder builder = commandExpectingBinaryData("4.0").d("data");

        ByteBufferPclCommandReader reader = createReader(builder.toBytes());

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
    }

    @Test
    @Ignore("have seen in the wild...")
    public void shouldCaptureBinaryDataWhenTheCommandIs_LowercaseTerminator() {
        PclCommandBuilder builder = new PclCommandBuilder(false).p('*').g('c').v("4").t('e').d("data");

        ByteBufferPclCommandReader reader = createReader(builder.toBytes());

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
    }

    @Test
    public void shouldHandleIfTheValueIsNotProvided() {
        PclCommandBuilder builder = new PclCommandBuilder(false).p('*').g('p').t('E');

        ByteBufferPclCommandReader reader = createReader(builder.toBytes());

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
    }


    @Test
    public void shouldReturnNullWhenThereAreNoBytesInTheFile() {
        ByteBufferPclCommandReader reader = createReader(new byte[0]);

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldHandleSkippingTheEntireFile() {
        byte[] fileContents = new byte[]{ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        reader.skip(5L);

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingCompressedParameterizedCommands() {
        byte[] fileContents = new byte[]{ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_PARAMETER_BYTE, '2', HIGHEST_TERMINATION_BYTE};
        PclCommandBuilder base = new PclCommandBuilder(false).p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE);
        PclCommand expectedCommand1 = base.copy().v("1").t(LOWEST_TERMINATION_BYTE).toCommand();
        PclCommand expectedCommand2 = base.copy().v("2").t(HIGHEST_TERMINATION_BYTE).toCommand();

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0, expectedCommand1, reader.nextCommand());
        assertParameterizedCommand(5, expectedCommand2, reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingCompressedParameterizedCommandsFollowedByAnUncompressedCommand() {
        byte[] compressedContents = new byte[]{ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_PARAMETER_BYTE, '2', HIGHEST_TERMINATION_BYTE};
        PclCommandBuilder base = new PclCommandBuilder(false).p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE);
        PclCommandBuilder expectedCommand1 = base.copy().v("1").t(LOWEST_TERMINATION_BYTE);
        PclCommandBuilder expectedCommand2 = base.copy().v("2").t(HIGHEST_TERMINATION_BYTE);

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(compressedContents, expectedCommand1.toBytes()));

        assertParameterizedCommand(0, expectedCommand1.toCommand(), reader.nextCommand());
        assertParameterizedCommand(5, expectedCommand2.toCommand(), reader.nextCommand());
        assertParameterizedCommand(7, expectedCommand1.toCommand(), reader.nextCommand());
        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldBeAbleToSkipLeadingBytesAndContinueParsingACommand() {
        byte[] fileContents = new byte[]{0, 0, ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand = new byte[]{ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        reader.skip(2L);
        assert2ByteCommand(2L, expectCommand, reader.nextCommand());
    }

    @Test
    public void shouldAllowLowercaseTerminationBytes() {
        PclCommandBuilder baseBuilder = new PclCommandBuilder(false).p('*').g('c').v("0");
        PclCommandBuilder builder = baseBuilder.copy().t('e');
        PclCommandBuilder otherBuilder = baseBuilder.t('E');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), otherBuilder.toBytes()));

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
    }

    @Test
    public void shouldTreatTheEscapeCharacterAsPartOfTheBinaryDataIfTheFollowingByteIsNotAParameterizedByte() {
        PclCommandBuilder builder = commandExpectingBinaryData("2").d(new byte[]{ESCAPE, '0'});

        ByteBufferPclCommandReader reader = createReader(builder.toBytes());

        assertParameterizedCommand(0L, builder.toCommand(), reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingAParameterizedCommand() {
        PclCommandBuilder fileContents = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE).v("1").t(LOWEST_TERMINATION_BYTE);

        ByteBufferPclCommandReader reader = createReader(fileContents.toBytes());

        assertParameterizedCommand(0L, fileContents.toCommand(), reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingConsecutiveParameterizedCommands() {
        PclCommandBuilder builder = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE);
        PclCommandBuilder expectedCommand = builder.copy().v("1").t(LOWEST_TERMINATION_BYTE);
        PclCommandBuilder expectedCommand2 = builder.copy().v("2").t(HIGHEST_TERMINATION_BYTE);

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(expectedCommand.toBytes(), expectedCommand2.toBytes()));

        assertParameterizedCommand(0L, expectedCommand.toCommand(), reader.nextCommand());
        assertParameterizedCommand(5L, expectedCommand2.toCommand(), reader.nextCommand());
    }

    @Test
    public void shouldReturnNullWhenTheEndOfFileIsReached() {
        byte[] fileContents = new byte[]{ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        reader.nextCommand();

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldReturnATwoByteCommandWhenEncountered() {
        byte[] fileContents = new byte[]{ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assert2ByteCommand(0L, fileContents, reader.nextCommand());
    }

    @Test
    public void shouldCaptureBytesBeforeACommand() {
        byte[] fileContents = new byte[]{0, 0, ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand = new byte[]{ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertTextCommand(0L, new byte[]{0, 0}, reader.nextCommand());
        assert2ByteCommand(2L, expectCommand, reader.nextCommand());
    }

    @Test
    public void shouldHandleIfNotPclCommandsAreFoundButThereIsTextInTheFile() {
        byte[] fileContents = "hello".getBytes();

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertTextCommand(0L, fileContents, reader.nextCommand());
        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingConsecutiveTwoByteCommands() {
        byte[] fileContents = new byte[]{ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR, ESCAPE, HIGHEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand = new byte[]{ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand2 = new byte[]{ESCAPE, HIGHEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assert2ByteCommand(0L, expectCommand, reader.nextCommand());
        assert2ByteCommand(2L, expectCommand2, reader.nextCommand());
    }

    private PclCommandBuilder commandExpectingBinaryData(String value) {
        return new PclCommandBuilder().p(')').g('s').v(value).t('W');
    }

    private ByteBufferPclCommandReader createReader(byte[] fileContents) {
        return new ByteBufferPclCommandReader(fileContents);
    }
}
