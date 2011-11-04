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


import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertNull;
import static pcl.io.AssertPcl.assert2ByteCommand;
import static pcl.io.AssertPcl.assertParameterizedCommand;

public class ByteBufferPclCommandReaderTest {
    @Test
    @Ignore("debugging purposes only, should go away")
    public void realFile() throws Exception {
        long start = System.currentTimeMillis();
        PclCommandReader reader = new EbcdicToAsciiPclCommandReader(new MappedFilePclCommandReader(new File(Thread.currentThread().getContextClassLoader().getResource("example/sample.pcl").toURI())));
        PclCommand command = null;
        int count = 0;
        while ((command = reader.nextCommand()) != null) {
            int length = command.getBytes().length;
            System.out.println(count + "# @ " + command.getPosition() + ", length=" + length + "  " + command.toAscii());
            count++;
        }
        System.out.println((System.currentTimeMillis() - start) + " millis; " + count + " commands");
    }

    @Test
    public void shouldReturnNullWhenThereAreNoPclCommands() {
        ByteBufferPclCommandReader reader = createReader(new byte[]{0, 0, 0, 0});

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldReturnNullWhenThereAreNoBytesInTheFile() {
        ByteBufferPclCommandReader reader = createReader(new byte[0]);

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldHandleSkippingTheEntireFile() {
        byte[] fileContents = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_TERMINATION_BYTE};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        reader.skip(5L);

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldBeAbleToSkipLeadingBytesAndContinueParsingACommand() {
        byte[] fileContents = new byte[]{0, 0, PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        reader.skip(2L);
        assert2ByteCommand(2L, expectCommand, reader.nextCommand());
    }

    @Test
    public void shouldFixLowercaseTerminationBytes() {
        byte[] fileContents = new byte[]{
                PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_PARAMETER_BYTE
        };

        byte[] expectedCommand = new byte[]{
                PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_TERMINATION_BYTE
        };

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
    }

    @Test
    public void shouldTreatTheEscapeCharacterAsPartOfTheBinaryDataIfTheFollowingByteIsNotAParameterizedByte() {
        byte[] fileContents = new byte[]{
                PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_TERMINATION_BYTE, PclUtil.ESCAPE, '0'
        };

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0L, fileContents, reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingAParameterizedCommand() {
        byte[] fileContents = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_TERMINATION_BYTE};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assertParameterizedCommand(0L, fileContents, reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingConsecutiveParameterizedCommands() {
        byte[] expectedCommand = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_TERMINATION_BYTE};
        byte[] expectedCommand2 = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '2', PclUtil.HIGHEST_TERMINATION_BYTE};

        ByteBufferPclCommandReader reader = createReader(ByteArrayUtil.concat(expectedCommand, expectedCommand2));

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
        assertParameterizedCommand(5L, expectedCommand2, reader.nextCommand());
    }

    @Test
    public void shouldReturnNullWhenTheEndOfFileIsReached() {
        byte[] fileContents = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        reader.nextCommand();

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldReturnATwoByteCommandWhenEncountered() {
        byte[] fileContents = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assert2ByteCommand(0L, fileContents, reader.nextCommand());
    }

    @Test
    public void shouldSkipLeadingBytesUntilAnEscapeByteIsFound() {
        byte[] fileContents = new byte[]{0, 0, PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assert2ByteCommand(2L, expectCommand, reader.nextCommand());
    }

    @Test
    public void shouldHandleParsingConsecutiveTwoByteCommands() {
        byte[] fileContents = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR, PclUtil.ESCAPE, PclUtil.HIGHEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};
        byte[] expectCommand2 = new byte[]{PclUtil.ESCAPE, PclUtil.HIGHEST_2BYTE_COMMAND_OPERATOR};

        ByteBufferPclCommandReader reader = createReader(fileContents);

        assert2ByteCommand(0L, expectCommand, reader.nextCommand());
        assert2ByteCommand(2L, expectCommand2, reader.nextCommand());
    }

    private ByteBufferPclCommandReader createReader(byte[] fileContents) {
        return new ByteBufferPclCommandReader(fileContents);
    }
}
