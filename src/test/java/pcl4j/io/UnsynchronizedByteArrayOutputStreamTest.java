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

import static junit.framework.Assert.*;

public class UnsynchronizedByteArrayOutputStreamTest {

    private UnsynchronizedByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception {
        outputStream = new UnsynchronizedByteArrayOutputStream(16);
    }

    @Test
    public void toByteArray_shouldRecreateCacheAfterTheNextWrite() {
        outputStream.write(1);
        byte[] bytes = outputStream.toByteArray();
        outputStream.write(2);

        assertNotSame(bytes, outputStream.toByteArray());
    }

    @Test
    public void toByteArray_shouldCacheTheByteArrayForSpeed() {
        outputStream.write(1);
        byte[] bytes = outputStream.toByteArray();

        assertSame(bytes, outputStream.toByteArray());
    }


    @Test
    public void shouldBeAbleToResetTheStreamBackToTheBeginning() {
        outputStream.write(1);
        outputStream.reset();

        assertEquals(0, outputStream.toByteArray().length);
        assertEquals(0, outputStream.size());
    }


    @Test
    public void shouldGrowIfTheInitialSizeIsExceeded() {
        StringBuilder expectedOutput = new StringBuilder();
        for (int i = 0; i < 17; i++) {
            outputStream.write(i);
            expectedOutput.append((char) i);
        }

        assertEquals(expectedOutput.toString(), new String(outputStream.toByteArray()));
        assertEquals(17, outputStream.size());
    }


    @Test
    public void shouldReturnAnEmptyArrayIfNoBytesWereWritten() {
        byte[] bytes = outputStream.toByteArray();

        assertNotNull(bytes);
        assertEquals(0, bytes.length);
        assertEquals(0, outputStream.size());
    }

}
