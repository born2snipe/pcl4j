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


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * A PclCommandReader implementation meant to be used with large PCL files
 */
public class MappedFilePclCommandReader extends ByteBufferPclCommandReader {
    private final File file;
    private FileChannel channel;

    public MappedFilePclCommandReader(File file) {
        this.file = file;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            channel = randomAccessFile.getChannel();
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        } catch (IOException e) {
            throw new PclCommandReaderException("A problem occurred while trying to initialize file=[" + file.getName() + "]", e);
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                throw new PclCommandReaderException("A problem occurred while trying to close the FileChannel of file=[" + file.getName() + "]", e);
            }
        }
    }
}
