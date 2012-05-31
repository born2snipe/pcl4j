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

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PclCommandFactoryTest {
    private PclCommandFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new PclCommandFactory();
    }

    @Test
    public void shouldBuildAParameterizedCommandWhenGivenMoreThan2Bytes() {
        byte[] data = {PclUtil.ESCAPE, 1, 2, 3, 4, 5};

        PclCommand command = factory.buildParameterizedCommand(
                3L,
                (byte) 1,
                (byte) 2,
                new byte[]{3},
                (byte) 4,
                new byte[]{5}
        );

        assertTrue(command instanceof ParameterizedCommand);
        assertTrue(Arrays.equals(data, command.getBytes()));
        assertEquals(3L, command.getPosition());
    }

    @Test
    public void shouldBuildA2ByteCommandWhenGivenOnly2Bytes() {
        byte[] data = {PclUtil.ESCAPE, 1};

        PclCommand command = factory.buildTwoByteCommand(2L, (byte) 1);

        assertTrue(command instanceof TwoByteCommand);
        assertTrue(Arrays.equals(data, command.getBytes()));
        assertEquals(2L, command.getPosition());
    }

    @Test
    public void shouldBuildATextCommand() {
        byte[] data = {1};

        PclCommand command = factory.buildTextCommand(1L, data);

        assertTrue(command instanceof TextCommand);
        assertTrue(Arrays.equals(data, command.getBytes()));
        assertEquals(1L, command.getPosition());
    }

}
