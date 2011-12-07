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

import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class NoSideEffectForReadingToWritingTest {
    private File inputFile;
    private PclCommandReader reader;
    private File outputFile;
    private PclCommandWriter writer;

    @Before
    public void setUp() throws Exception {
        inputFile = new File(Thread.currentThread().getContextClassLoader().getResource("example/sample.pcl").toURI());
        outputFile = File.createTempFile("test", ".pcl");
        outputFile.deleteOnExit();

        reader = new MappedFilePclCommandReader(inputFile);
        writer = new OutputStreamPclCommandWriter(new FileOutputStream(outputFile));

    }

    @Test
    public void shouldBeAbleToReadAndWriteAFileAndTheyShouldStillMatchByteForByte() throws IOException {
        copyCommandsToOtherFile();
        assertFilesMatch();
    }

    private void copyCommandsToOtherFile() throws IOException {
        PclCommand command = null;
        while ((command = reader.nextCommand()) != null) {
            writer.write(command);
        }
        reader.close();
        writer.close();
    }

    private void assertFilesMatch() throws IOException {
        byte[] inputData = toBytes(inputFile);
        byte[] outputData = toBytes(outputFile);

        for (int i = 0; i < inputData.length && i < outputData.length; i++) {
            assertEquals("Byte mismatch [position=" + i + "]", inputData[i], outputData[i]);
        }
        assertEquals("the files lengths do not match", inputData.length, outputData.length);
    }

    private byte[] toBytes(File file) throws IOException {
        return Files.toByteArray(file);
    }
}
