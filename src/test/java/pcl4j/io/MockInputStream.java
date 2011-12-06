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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MockInputStream extends InputStream {
    private final InputStream delegate;
    private boolean closed;
    private IOException exceptionToBeThrown;

    public MockInputStream(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    public MockInputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        if (exceptionToBeThrown != null) throw exceptionToBeThrown;
        return delegate.read();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        closed = true;
    }

    @Override
    public long skip(long l) throws IOException {
        if (exceptionToBeThrown != null) throw exceptionToBeThrown;
        return super.skip(l);
    }

    public boolean isClosed() {
        return closed;
    }

    public void setExceptionToBeThrown(IOException exceptionToBeThrown) {
        this.exceptionToBeThrown = exceptionToBeThrown;
    }
}
