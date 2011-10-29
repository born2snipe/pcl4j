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


import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TwoByteCommandTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfYouGiveItLessThan2Bytes_withoutPosition() {
        new TwoByteCommand(new byte[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfYouGiveItLessThan2Bytes() {
        new TwoByteCommand(0L, new byte[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldBlowUpIfYouGiveItMoreThan2Bytes() {
        new TwoByteCommand(0L, new byte[3]);
    }

    @Test
    public void getOperation() {
        assertEquals(10, new TwoByteCommand(0, new byte[]{0, 10}).getOperation());
    }
}
