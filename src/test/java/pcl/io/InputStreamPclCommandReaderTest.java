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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class InputStreamPclCommandReaderTest {
    private static final byte[] TWO_BYTE_COMMAND = new byte[]{PclUtil.ESCAPE, PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR};

    @Test
    public void shouldHandleAParameterizedCommand_withATerminationCharacterAndAParameterCharacter() {
        byte[] fileBytes = new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'o', '2', 'O'};
        PclCommandReader reader = createPclCommandReader(fileBytes);

        assertParameterizedCommand(0L, fileBytes, reader.nextCommand());
    }

    @Test
    public void shouldHandleMultipleParameterizedCommand_withATerminationCharacterAndNoParameterCharacter() {
        byte[] expectedCommand = {PclUtil.ESCAPE, '&', 'l', '1', PclUtil.LOWEST_TERMINATION_BYTE};
        byte[] expectedCommand2 = {PclUtil.ESCAPE, '&', 'l', '1', PclUtil.HIGHEST_TERMINATION_BYTE};

        byte[] fileBytes = ByteArrayUtil.concat(expectedCommand, expectedCommand2);
        PclCommandReader reader = createPclCommandReader(fileBytes);

        assertParameterizedCommand(0L, expectedCommand, reader.nextCommand());
        assertParameterizedCommand(5L, expectedCommand2, reader.nextCommand());
    }

    @Test
    public void shouldHandleAParameterizedCommand_withATerminationCharacterAndNoParameterCharacter() {
        byte[] fileBytes = new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'O'};
        PclCommandReader reader = createPclCommandReader(fileBytes);

        assertParameterizedCommand(0L, fileBytes, reader.nextCommand());
    }

    @Test
    public void shouldHandleAParameterizedCommand_withATerminationCharacterAndNoParameterCharacterAndHasBinaryDataAtTheEndOfAFile() {
        byte[] fileBytes = new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'O', 1, 2, 3, 4};
        PclCommandReader reader = createPclCommandReader(fileBytes);

        assertParameterizedCommand(0L, fileBytes, reader.nextCommand());
    }

    @Test
    public void shouldHandleAParameterizedCommand_withATerminationCharacterAndNoParameterCharacterAndHasBinaryDataFollowedByA2ByteCommand() {
        byte[] command1 = new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'O', 1, 2, 3, 4};
        byte[] command2 = new byte[]{PclUtil.ESCAPE, PclUtil.HIGHEST_2BYTE_COMMAND_OPERATOR};

        byte[] fileBytes = ByteArrayUtil.concat(command1, command2);
        PclCommandReader reader = createPclCommandReader(fileBytes);

        assertParameterizedCommand(0L, command1, reader.nextCommand());
        assert2ByteCommand(9L, command2, reader.nextCommand());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpOnConstructionIfTheGivenInputStreamIsNull() {
        createPclCommandReader((InputStream) null);
    }

    @Test(expected = PclCommandReader.PclCommandReaderException.class)
    public void shouldPropagateAnExceptionWhenAttemptingToSkip() {
        MockInputStream inputStream = new MockInputStream(badData(10));
        inputStream.setExceptionToBeThrown(new IOException());
        PclCommandReader reader = createPclCommandReader(inputStream);

        reader.skip(5);
    }

    @Test
    public void shouldBeAbleToSkipAndContinueReading() {
        byte[] expectedCommand = TWO_BYTE_COMMAND;
        byte[] fileData = ByteArrayUtil.concat(badData(5), expectedCommand);
        PclCommandReader reader = createPclCommandReader(fileData);

        reader.skip(5);
        assertCommand(5L, expectedCommand, reader.nextCommand());
    }

    @Test
    public void shouldReturnNullIfAllBytesWereSkipped() {
        byte[] fileData = {PclUtil.ESCAPE, 48};
        PclCommandReader reader = createPclCommandReader(fileData);

        reader.skip(3);
        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldCloseTheInputStreamWhenTheReaderIsClosed() {
        MockInputStream inputStream = new MockInputStream(new byte[0]);
        PclCommandReader reader = createPclCommandReader(inputStream);

        reader.close();

        assertTrue(inputStream.isClosed());
    }

    @Test
    public void shouldReturnNullThereAreNoBytesToBeRead() {
        PclCommandReader reader = createPclCommandReader(new byte[0]);

        assertNull(reader.nextCommand());
    }


    @Test(expected = PclCommandReader.PclCommandReaderException.class)
    public void shouldPropagateAnExceptionWhenTheInputStreamHasAProblemReading() {
        MockInputStream inputStream = new MockInputStream(new byte[0]);
        inputStream.setExceptionToBeThrown(new IOException("real error"));

        createPclCommandReader(inputStream).nextCommand();
    }

    @Test
    public void shouldNotCaptureBytesIfTheEscapeByteIsFollowedByAnInvalidByte() {
        byte[] expectedBytes = new byte[]{PclUtil.ESCAPE, 0};
        PclCommandReader reader = createPclCommandReader(expectedBytes);

        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldHandleReadingMultiple2CharacterCommands() {
        byte[] expectedCommand = TWO_BYTE_COMMAND;
        byte[] expectedBytes = ByteArrayUtil.concat(expectedCommand, expectedCommand);
        PclCommandReader reader = createPclCommandReader(expectedBytes);

        assert2ByteCommand(0L, expectedCommand, reader.nextCommand());
        assert2ByteCommand(2L, expectedCommand, reader.nextCommand());
        assertNull(reader.nextCommand());
    }

    @Test
    public void shouldSkipLeadingBytesUntilAValidCommandIsFound() {
        byte[] expectedCommand = TWO_BYTE_COMMAND;
        byte[] expectedBytes = ByteArrayUtil.concat(badData(3), expectedCommand);
        PclCommandReader reader = createPclCommandReader(expectedBytes);

        assert2ByteCommand(3L, expectedCommand, reader.nextCommand());
    }

    @Test
    @Ignore("Not sure if this is really a valid scenario")
    public void shouldSkipAnEscapeByteIfThereAreTwoEscapeBytesThatAreConsecutive() {
        byte[] expectedCommand = TWO_BYTE_COMMAND;
        byte[] expectedBytes = ByteArrayUtil.concat(new byte[]{PclUtil.ESCAPE}, expectedCommand);
        PclCommandReader reader = createPclCommandReader(expectedBytes);

        assert2ByteCommand(1L, expectedCommand, reader.nextCommand());
    }

    @Test
    public void shouldSkipBytesBetweenCommands() {
        byte[] expectedCommand = TWO_BYTE_COMMAND;
        byte[] expectedCommand2 = new byte[]{PclUtil.ESCAPE, PclUtil.HIGHEST_2BYTE_COMMAND_OPERATOR};

        byte[] expectedBytes = ByteArrayUtil.concat(expectedCommand, badData(3), expectedCommand2);
        PclCommandReader reader = createPclCommandReader(expectedBytes);

        assert2ByteCommand(0L, expectedCommand, reader.nextCommand());
        assert2ByteCommand(5L, expectedCommand2, reader.nextCommand());
    }

    @Test
    public void shouldHandleReading2CharacterCommands() {
        byte[] expectedBytes = TWO_BYTE_COMMAND;
        PclCommandReader reader = createPclCommandReader(expectedBytes);

        assert2ByteCommand(0L, expectedBytes, reader.nextCommand());
    }

    @Test
    public void shouldSkipPartialCommandsUntilAValidCommandIsFound() {
        byte[] expectedBytes = ByteArrayUtil.concat(new byte[]{PclUtil.ESCAPE, 0, PclUtil.HIGHEST_2BYTE_COMMAND_OPERATOR}, TWO_BYTE_COMMAND);
        PclCommandReader reader = createPclCommandReader(expectedBytes);

        assertCommand(3L, TWO_BYTE_COMMAND, reader.nextCommand());
    }

    @Test
    @Ignore("debugging purposes only, should go away")
    public void x() throws IOException {
        long start = System.currentTimeMillis();
        PclCommandReader reader = new UncompressedPclCommandReader(new InputStreamPclCommandReader(Thread.currentThread().getContextClassLoader().getResource("example/sample.pcl").openStream()));
        PclCommand command = null;
        int count = 0;
        while ((command = reader.nextCommand()) != null) {
            System.out.println(count + "# @ " + command.getPosition() + " " + command.toAscii());
            count++;
        }
        System.out.println((System.currentTimeMillis() - start) + " millis; " + count + " commands");
    }


    private void assert2ByteCommand(long expectedPosition, byte[] expectedBytes, PclCommand actualCommand) {
        AssertPcl.assert2ByteCommand(expectedPosition, expectedBytes, actualCommand);
    }

    private void assertParameterizedCommand(long expectedPosition, byte[] expectedCommandBytes, PclCommand command) {
        AssertPcl.assertParameterizedCommand(expectedPosition, expectedCommandBytes, command);
    }

    private void assertCommand(long expectedPosition, byte[] expectedBytes, PclCommand actualCommand) {
        AssertPcl.assertCommand(expectedPosition, expectedBytes, actualCommand);
    }

    private PclCommandReader createPclCommandReader(InputStream inputStream) {
        return new InputStreamPclCommandReader(inputStream);
    }

    private PclCommandReader createPclCommandReader(byte[] fileBytes) {
        return createPclCommandReader(new MockInputStream(fileBytes));
    }

    private byte[] badData(int count) {
        byte[] data = new byte[count];
        Arrays.fill(data, (byte) 0);
        return data;
    }

}
