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


import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.*;
import static pcl.io.AssertPcl.assertParameterizedCommand;

public class UncompressedPclCommandReaderTest {
    private PclCommandReader delegateReader;
    private UncompressedPclCommandReader pclCommandReader;

    @Before
    public void setUp() throws Exception {
        delegateReader = mock(PclCommandReader.class);

        pclCommandReader = new UncompressedPclCommandReader(delegateReader);
    }

    @Test
    public void moreThanTwoCommandCompressed() {
        ParameterizedCommand originalCommand = new ParameterizedCommand(2L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'o', '2', 'o', '3', 'O'});
        when(delegateReader.nextCommand()).thenReturn(originalCommand, null);

        assertParameterizedCommand(2L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'O'}, pclCommandReader.nextCommand());
        assertParameterizedCommand(7L, new byte[]{PclUtil.ESCAPE, '&', 'l', '2', 'O'}, pclCommandReader.nextCommand());
        assertParameterizedCommand(9L, new byte[]{PclUtil.ESCAPE, '&', 'l', '3', 'O'}, pclCommandReader.nextCommand());
        assertNull(pclCommandReader.nextCommand());
    }


    @Test
    public void whenACompressedParameterizedCommandIsEncounteredWeShouldCacheTheFollowingCommandForTheNextRead() {
        ParameterizedCommand originalCommand = new ParameterizedCommand(0L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'o', '2', 'O'});
        when(delegateReader.nextCommand()).thenReturn(originalCommand, null);

        assertParameterizedCommand(0L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'O'}, pclCommandReader.nextCommand());
        assertParameterizedCommand(5L, new byte[]{PclUtil.ESCAPE, '&', 'l', '2', 'O'}, pclCommandReader.nextCommand());
        assertNull(pclCommandReader.nextCommand());
    }

    @Test
    public void aSingleParameterizedCommandWithBinaryData() {
        byte[] expectedCommand = {PclUtil.ESCAPE, '&', 'l', '1', '2', 'O', 'D', 'A', 'T', 'A'};
        ParameterizedCommand originalCommand = new ParameterizedCommand(0L, expectedCommand);
        when(delegateReader.nextCommand()).thenReturn(originalCommand);

        assertParameterizedCommand(0L, expectedCommand, pclCommandReader.nextCommand());
    }

    @Test
    public void multipleUncompressedParameterizedCommands() {
        byte[] expectedCommand = {PclUtil.ESCAPE, '&', 'l', '1', '2', 'O'};
        byte[] expectedCommand2 = {PclUtil.ESCAPE, '&', 'l', '1', 'A'};
        ParameterizedCommand originalCommand = new ParameterizedCommand(0L, expectedCommand);
        ParameterizedCommand originalCommand2 = new ParameterizedCommand(0L, expectedCommand2);
        when(delegateReader.nextCommand()).thenReturn(originalCommand, originalCommand2);

        assertParameterizedCommand(0L, expectedCommand, pclCommandReader.nextCommand());
        assertParameterizedCommand(0L, expectedCommand2, pclCommandReader.nextCommand());
    }

    @Test
    public void whenATwoByteCommandIsReadTheCommandShouldBeReturned() {
        TwoByteCommand command = new TwoByteCommand(0L, new byte[2]);

        when(delegateReader.nextCommand()).thenReturn(command);

        assertSame(command, pclCommandReader.nextCommand());
    }

    @Test
    public void skipShouldSkipOnTheDelegate() {
        pclCommandReader.skip(19L);

        verify(delegateReader).skip(19L);
    }

    @Test
    public void closeShouldCloseTheDelegate() {
        pclCommandReader.close();

        verify(delegateReader).close();
    }

    @Test
    public void shouldBeOverridePclCommandFactoryImplementation() {

        PclCommandFactory pclCommandFactory = mock(PclCommandFactory.class);
        pclCommandReader.setPclCommandFactory(pclCommandFactory);

        ParameterizedCommand originalCommand = new ParameterizedCommand(2L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'o', '2', 'o', '3', 'O'});
        ParameterizedCommand expectedCommand = new ParameterizedCommand(1L, new byte[4]);

        when(delegateReader.nextCommand()).thenReturn(originalCommand);
        when(pclCommandFactory.build(2L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'O'})).thenReturn(expectedCommand);

        assertSame(expectedCommand, pclCommandReader.nextCommand());
    }
}
