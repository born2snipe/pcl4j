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
import static pcl4j.io.AssertPcl.*;
import static pcl4j.io.PclUtil.*;

public class ByteBufferPclCommandReaderTest {
    @Test
    @Ignore("debugging purposes only, should go away")
    public void realFile() throws Exception {
        for (int i = 0; i < 20; i++) {
            long start = System.currentTimeMillis();
            PclCommandReader reader = new MappedFilePclCommandReader(new File(Thread.currentThread().getContextClassLoader().getResource("example/sample.pcl").toURI()));
            PclCommand command = null;
            int count = 0;
            while ((command = reader.nextCommand()) != null) {
                count++;
            }
            System.out.println((System.currentTimeMillis() - start) + " millis; " + count + " commands");
        }
    }

    @Test
    @Ignore("need to support")
    public void shouldHandleUniversalExitCommand_noData() {
        byte[] expectedCommand = ByteArrayUtil.concat(
                new byte[]{PclUtil.ESCAPE},
                "%-12345X".getBytes()
        );
        ByteBufferPclCommandReader reader = createReader(expectedCommand);

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
    }

    @Test
    @Ignore("need to support")
    public void shouldHandleUniversalExitCommand_withData() {
        byte[] expectedCommand = ByteArrayUtil.concat(
                new byte[]{PclUtil.ESCAPE},
                "%-12345X@PJL JOB".getBytes()
        );
        ByteBufferPclCommandReader reader = createReader(expectedCommand);

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
    }


    @Test
    public void shouldHandleCommandsWithALowerTerminatorAndLastCommandFollowedByText() {
        PclCommandBuilder cmd1 = new PclCommandBuilder(false).p('*').g('p').v("10").t('x');
        PclCommandBuilder cmd2 = new PclCommandBuilder(false).p('(').g('s').v("0").t('S');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(cmd1.toBytes(), cmd2.toBytes(), "Moved".getBytes()));

        assertParameterizedCommand(0L, cmd1.toBytes(), reader.nextCommand());
        assertParameterizedCommand(6L, cmd2.toBytes(), reader.nextCommand());
        assertTextCommand(11L, "Moved".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldHandle2CommandsFirstEndsWithALowercaseTerminator() {
        PclCommandBuilder cmd1 = new PclCommandBuilder(false).p('*').g('p').v("10").t('x');
        PclCommandBuilder cmd2 = new PclCommandBuilder(false).p('(').g('s').v("0").t('S');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(cmd1.toBytes(), cmd2.toBytes()));

        assertParameterizedCommand(0L, cmd1.toBytes(), reader.nextCommand());
        assertParameterizedCommand(6L, cmd2.toBytes(), reader.nextCommand());
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
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').v(" 4 ").t('E').d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
        assertTextCommand(11L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_PositiveValue() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').v("+4").t('E').d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
        assertTextCommand(10L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_DecimalValue() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').v("4.0").t('E').d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
        assertTextCommand(11L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_IntValue() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').v("4").t('E').d("data");

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "12".getBytes()));

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
        assertTextCommand(9L, "12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldOnlyCaptureTheNumberBytesSpecifiedByTheValueOfTheCommand_NoValue() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').t('E');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), "data12".getBytes()));

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
        assertTextCommand(4L, "data12".getBytes(), reader.nextCommand());
    }

    @Test
    public void shouldCaptureBinaryDataWhenTheCommandIs_DecimalValue() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').v("4.0").t('E').d("data");

        ByteBufferPclCommandReader reader = createReader(builder.toBytes());

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
    }

    @Test
    @Ignore("have seen in the wild...")
    public void shouldCaptureBinaryDataWhenTheCommandIs_LowercaseTerminator() {
        PclCommandBuilder builder = new PclCommandBuilder(false).p('*').g('c').v("4").t('e').d("data");

        ByteBufferPclCommandReader reader = createReader(builder.toBytes());

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
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
        byte[] expectedCommand1 = base.copy().v("1").t(LOWEST_TERMINATION_BYTE).toBytes();
        byte[] expectedCommand2 = base.copy().v("2").t(HIGHEST_TERMINATION_BYTE).toBytes();

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0, expectedCommand1, reader.nextCommand());
        assertParameterizedCommand(5, expectedCommand2, reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingCompressedParameterizedCommandsFollowedByAnUncompressedCommand() {
        byte[] compressedContents = new byte[]{ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_PARAMETER_BYTE, '2', HIGHEST_TERMINATION_BYTE};
        PclCommandBuilder base = new PclCommandBuilder(false).p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE);
        byte[] expectedCommand1 = base.copy().v("1").t(LOWEST_TERMINATION_BYTE).toBytes();
        byte[] expectedCommand2 = base.copy().v("2").t(HIGHEST_TERMINATION_BYTE).toBytes();

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(compressedContents, expectedCommand1));

        assertParameterizedCommand(0, expectedCommand1, reader.nextCommand());
        assertParameterizedCommand(5, expectedCommand2, reader.nextCommand());
        assertParameterizedCommand(7, expectedCommand1, reader.nextCommand());
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
        PclCommandBuilder builder = new PclCommandBuilder(false).p('*').g('c').v("0").t('e');
        PclCommandBuilder otherBuilder = new PclCommandBuilder(false).p('*').g('c').v("0").t('E');

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(builder.toBytes(), otherBuilder.toBytes()));

        assertParameterizedCommand(0L, builder.toBytes(), reader.nextCommand());
    }

    @Test
    public void shouldTreatTheEscapeCharacterAsPartOfTheBinaryDataIfTheFollowingByteIsNotAParameterizedByte() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').v("2").t('E').d(new byte[]{ESCAPE, '0'});
        byte[] fileContents = builder.toBytes();

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0L, fileContents, reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingAParameterizedCommand() {
        byte[] fileContents = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE).v("1").t(LOWEST_TERMINATION_BYTE).toBytes();

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0L, fileContents, reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingConsecutiveParameterizedCommands() {
        PclCommandBuilder builder = new PclCommandBuilder().p(LOWEST_PARAMETERIZED_BYTE).g(LOWEST_GROUP_BYTE);
        byte[] expectedCommand = builder.copy().v("1").t(LOWEST_TERMINATION_BYTE).toBytes();
        byte[] expectedCommand2 = builder.copy().v("2").t(HIGHEST_TERMINATION_BYTE).toBytes();

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(expectedCommand, expectedCommand2));

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
        assertParameterizedCommand(5L, expectedCommand2, reader.nextCommand());
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

    private ByteBufferPclCommandReader createReader(byte[] fileContents) {
        return new ByteBufferPclCommandReader(fileContents);
    }
}
