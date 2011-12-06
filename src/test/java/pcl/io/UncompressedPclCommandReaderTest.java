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

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfANullReaderIsGiven() {
        new UncompressedPclCommandReader(null);
    }

    @Test
    public void twoCommandsCompressedFollowedByBinaryData() {
        ParameterizedCommand originalCommand = new ParameterizedCommand(2L, ((char) PclUtil.ESCAPE + "*p1711x2204Y2.00% (Adj: 12 Mos/Term: 12 Mos)").getBytes());
        when(delegateReader.nextCommand()).thenReturn(originalCommand, null);

        PclCommandBuilder builder = new PclCommandBuilder().p('*').g('p');

        assertParameterizedCommand(2L, builder.copy().v("1711").t('X').toBytes(), pclCommandReader.nextCommand());
        assertParameterizedCommand(10L, builder.copy().v("2204").t('Y').d("2.00% (Adj: 12 Mos/Term: 12 Mos)").toBytes(), pclCommandReader.nextCommand());
        assertNull(pclCommandReader.nextCommand());
    }


    @Test
    public void moreThanTwoCommandCompressed() {
        PclCommandBuilder builder = new PclCommandBuilder().p('&').g('l').t('O');

        ParameterizedCommand originalCommand = new ParameterizedCommand(2L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'o', '2', 'o', '3', 'O'});
        when(delegateReader.nextCommand()).thenReturn(originalCommand, null);

        assertParameterizedCommand(2L, builder.copy().v("1").toBytes(), pclCommandReader.nextCommand());
        assertParameterizedCommand(7L, builder.copy().v("2").toBytes(), pclCommandReader.nextCommand());
        assertParameterizedCommand(9L, builder.copy().v("3").toBytes(), pclCommandReader.nextCommand());
        assertNull(pclCommandReader.nextCommand());
    }


    @Test
    public void whenACompressedParameterizedCommandIsEncounteredWeShouldCacheTheFollowingCommandForTheNextRead() {
        PclCommandBuilder builder = new PclCommandBuilder().p('&').g('l').t('O');

        ParameterizedCommand originalCommand = new ParameterizedCommand(0L, new byte[]{PclUtil.ESCAPE, '&', 'l', '1', 'o', '2', 'O'});
        when(delegateReader.nextCommand()).thenReturn(originalCommand, null);

        assertParameterizedCommand(0L, builder.copy().v("1").toBytes(), pclCommandReader.nextCommand());
        assertParameterizedCommand(5L, builder.copy().v("2").toBytes(), pclCommandReader.nextCommand());
        assertNull(pclCommandReader.nextCommand());
    }

    @Test
    public void aSingleParameterizedCommandWithBinaryData() {
        byte[] expectedCommand = new PclCommandBuilder().p('&').g('l').v("12").t('O').d("DATA").toBytes();
        ParameterizedCommand originalCommand = new ParameterizedCommand(0L, expectedCommand);
        when(delegateReader.nextCommand()).thenReturn(originalCommand);

        assertParameterizedCommand(0L, expectedCommand, pclCommandReader.nextCommand());
    }

    @Test
    public void multipleUncompressedParameterizedCommands() {
        PclCommandBuilder builder = new PclCommandBuilder().p('&').g('l');
        byte[] expectedCommand = builder.copy().v("12").t('O').toBytes();
        byte[] expectedCommand2 = builder.copy().v("1").t('A').toBytes();
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

}
