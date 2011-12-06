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

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.*;

public class EbcdicToAsciiPclCommandReaderTest {
    private PclCommandReader delegate;
    private EbcdicToAsciiPclCommandReader reader;

    @Before
    public void setUp() throws Exception {
        delegate = mock(PclCommandReader.class);

        reader = new EbcdicToAsciiPclCommandReader(delegate);
    }

    @Test
    public void shouldConvertBinaryDataIfTheTerminatorIsNotA_W() {
        ParameterizedCommand originalCommand = new ParameterizedCommand(2L, new byte[]{
                PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '0', 'X', (byte) 241, (byte) 242, (byte) 243
        });

        ParameterizedCommand expectedCommand = new ParameterizedCommand(2L, new byte[]{
                PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '0', 'X', '1', '2', '3'
        });

        when(delegate.nextCommand()).thenReturn(originalCommand);

        assertEquals(expectedCommand, reader.nextCommand());
    }

    @Test
    public void shouldNotConvertAnyBinaryDataIfTheTerminatorIsA_W() {
        ParameterizedCommand originalCommand = new ParameterizedCommand(0L, new byte[]{
                PclUtil.ESCAPE, PclUtil.LOWEST_PARAMETERIZED_BYTE, PclUtil.LOWEST_GROUP_BYTE, '0', 'W', 1, 2, 3
        });
        when(delegate.nextCommand()).thenReturn(originalCommand);

        assertSame(originalCommand, reader.nextCommand());
    }

    @Test
    public void shouldReturnNullIfTheDelegatingReaderReturnsNull() {
        when(delegate.nextCommand()).thenReturn(null);

        assertNull(reader.nextCommand());
    }

    @Test(expected = NullPointerException.class)
    public void shouldBlowUpWhenANullValueIsPassedIntoTheConstructor() {
        new EbcdicToAsciiPclCommandReader(null);
    }

    @Test
    public void shouldCallTheDelegateWhenClosingTheReader() {
        reader.close();

        verify(delegate).close();
    }

    @Test
    public void shouldCallTheDelegateWhenSkippingBytes() {
        reader.skip(10L);

        verify(delegate).skip(10L);
    }
}
