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
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class CompressedPclCommandWriterTest {
    private PclCommandWriter delegateWriter;
    private CompressedPclCommandWriter writer;
    private PclCommandCompressor commandCompressor;

    @Before
    public void setUp() throws Exception {
        delegateWriter = mock(PclCommandWriter.class);
        commandCompressor = mock(PclCommandCompressor.class);

        writer = new CompressedPclCommandWriter(delegateWriter);
        writer.setPclCommandCompressor(commandCompressor);
    }

    @Test
    public void whenTwoParameterizedCommandsAreWrittenInSequenceAndCanNotBeCompressedTheFirstCommandWrittenShouldBeWrittenToTheDelegate() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[3]);
        ParameterizedCommand otherCommand = new ParameterizedCommand(new byte[4]);

        when(commandCompressor.canBeCompressed(otherCommand, command)).thenReturn(false);

        writer.write(command);
        writer.write(otherCommand);
        verify(delegateWriter).write(command);

        writer.close();
        verify(delegateWriter).write(otherCommand);
    }

    @Test
    public void whenTwoParameterizedCommandsAreWrittenInSequenceAndCanBeCompressedTheyShouldNotBeWrittenToTheDelegate() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[3]);
        ParameterizedCommand otherCommand = new ParameterizedCommand(new byte[4]);
        ParameterizedCommand compressedCommand = new ParameterizedCommand(new byte[5]);

        when(commandCompressor.canBeCompressed(command, otherCommand)).thenReturn(true);
        when(commandCompressor.compress(otherCommand, command)).thenReturn(compressedCommand);

        writer.write(otherCommand);
        writer.write(command);
        verifyZeroInteractions(delegateWriter);

        writer.close();
        verify(delegateWriter).write(compressedCommand);
    }


    @Test
    public void whenOnlyOneCommandIsWrittenItShouldNotBeWrittenToTheDelegateUntilTheWriterIsClosed() {
        InOrder inOrder = inOrder(delegateWriter);
        ParameterizedCommand command = new ParameterizedCommand(new byte[3]);

        writer.write(command);
        writer.close();

        inOrder.verify(delegateWriter).write(command);
        inOrder.verify(delegateWriter).close();
    }

    @Test
    public void shouldNotWriteAParameterizedCommandRightAway() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[3]);

        writer.write(command);

        verifyZeroInteractions(delegateWriter);
    }


    @Test
    public void shouldCloseTheDelegateWriterWhenTheWriterIsToldToClose() {
        writer.close();
        verify(delegateWriter).close();
        verifyNoMoreInteractions(delegateWriter);
    }
}
