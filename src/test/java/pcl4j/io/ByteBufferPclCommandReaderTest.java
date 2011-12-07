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
        for (int i = 0; i < 1000; i++) {
            long start = System.currentTimeMillis();
            PclCommandReader reader = new UncompressedPclCommandReader(new MappedFilePclCommandReader(new File(Thread.currentThread().getContextClassLoader().getResource("example/sample.pcl").toURI())));
            PclCommand command = null;
            int count = 0;
            while ((command = reader.nextCommand()) != null) {
                int length = command.getBytes().length;
//            System.out.println(count + "# @ " + command.getPosition() + ", length=" + length + "  " + command.toAscii());
                count++;
            }
            System.out.println((System.currentTimeMillis() - start) + " millis; " + count + " commands");
        }
    }

    @Test
    public void shouldCaptureBinaryDataWhenTheCommandIs_AsciiCodeDecimal() {
        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('c').v("4").t('E').d("data");

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
    public void shouldBeAbleToSkipLeadingBytesAndContinueParsingACommand() {
        byte[] fileContents = new byte[]{0, 0, ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand = new byte[]{ESCAPE, LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        reader.skip(2L);
        assert2ByteCommand(2L, expectCommand, reader.nextCommand());
    }

    @Test
    @Ignore("thinking this was a bug...")
    public void shouldFixLowercaseTerminationBytes() {
        byte[] fileContents = new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_PARAMETER_BYTE
        };

        byte[] expectedCommand = new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        };

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
    }

    @Test
    @Ignore("this should become invalid case...")
    public void shouldTreatTheEscapeCharacterAsPartOfTheBinaryDataIfTheFollowingByteIsNotAParameterizedByte() {
        byte[] fileContents = new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE, ESCAPE, '0'
        };

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
