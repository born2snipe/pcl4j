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

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.*;

public class OutputStreamPclCommandWriterTest {
    private MockOutputStream output;
    private OutputStreamPclCommandWriter writer;

    @Before
    public void setUp() throws Exception {
        output = new MockOutputStream();
        writer = new OutputStreamPclCommandWriter(output);
    }

    @Test
    public void shouldThrowAnErrorIfANullCommandIsGiven() {
        try {
            writer.write(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("Sorry I do not know how to write a null command", e.getMessage());
        }
    }

    @Test
    public void shouldPropagateAnExceptionFromTheOutputStream() {
        IOException realError = new IOException();
        output.setExceptionToThrow(realError);

        try {
            writer.write(new TwoByteCommand(1L, new byte[2]));
            fail();
        } catch (PclCommandWriter.PclCommandWriterException e) {
            assertSame(realError, e.getCause());
        }
    }

    @Test
    public void shouldWriteTheBytesOfTheCommandToTheOutputStream() {
        byte[] expectedData = {1, 2};

        writer.write(new TwoByteCommand(0L, expectedData));

        assertTrue(Arrays.equals(expectedData, output.getBytes()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfANullOutputStreamIsGiven() {
        new OutputStreamPclCommandWriter(null);
    }

    @Test
    public void shouldCloseTheOutputStreamWhenTheWriterIsToldToClose() {
        writer.close();

        assertTrue(output.isClosed());
    }
}
