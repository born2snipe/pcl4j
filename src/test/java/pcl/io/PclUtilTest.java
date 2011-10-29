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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class PclUtilTest {
    private PclUtil util;

    @Before
    public void setUp() throws Exception {
        util = new PclUtil();
    }

    @Test
    public void isParameterCharacter_shouldHandleAllBytesBetweenTheHighestAndLowestParameterByte() {
        for (byte value = PclUtil.LOWEST_PARAMETER_BYTE; value <= PclUtil.HIGHEST_PARAMETER_BYTE; value++) {
            assertTrue(util.isParameterCharacter(value));
        }
    }

    @Test
    public void isParameterCharacter_shouldHandleAllBytesLowerThanTheLowestParameterByte() {
        for (byte value = Byte.MIN_VALUE; value < PclUtil.LOWEST_PARAMETER_BYTE; value++) {
            assertFalse(util.isParameterCharacter(value));
        }
    }

    @Test
    public void isParameterCharacter_shouldHandleAllBytesHigherThanTheHighestParameterCharacter() {
        for (byte value = PclUtil.HIGHEST_PARAMETER_BYTE + 1; value <= Byte.MAX_VALUE && value != Byte.MIN_VALUE; value++) {
            assertFalse(util.isParameterCharacter(value));
        }
    }

    @Test
    public void isGroupCharacter_shouldHandleAllBytesHigherThanTheHighestGroupCharacter() {
        for (byte value = PclUtil.HIGHEST_GROUP_BYTE + 1; value <= Byte.MAX_VALUE && value != Byte.MIN_VALUE; value++) {
            assertFalse(util.isGroupCharacter(value));
        }
    }

    @Test
    public void isGroupCharacter_shouldHandleAllBytesLowerThanTheLowestGroupCharacter() {
        for (byte value = Byte.MIN_VALUE; value < PclUtil.LOWEST_GROUP_BYTE; value++) {
            assertFalse(util.isGroupCharacter(value));
        }
    }

    @Test
    public void isGroupCharacter_shouldHandleAllGroupCharacters() {
        for (byte value = PclUtil.LOWEST_GROUP_BYTE; value < PclUtil.HIGHEST_GROUP_BYTE; value++) {
            assertTrue(util.isGroupCharacter(value));
        }
    }

    @Test
    public void isParameterizedCharacter_shouldHandleAllByteAfterTheHighestParameterizedCharacters() {
        for (byte value = PclUtil.HIGHEST_PARAMETERIZED_BYTE + 1; value < Byte.MAX_VALUE; value++) {
            assertFalse(util.isParameterizedCharacter(value));
        }
    }

    @Test
    public void isParameterizedCharacter_shouldHandleAllByteBeforeTheLowestParameterizedCharacters() {
        for (byte value = Byte.MIN_VALUE; value < PclUtil.LOWEST_PARAMETERIZED_BYTE; value++) {
            assertFalse(util.isParameterizedCharacter(value));
        }
    }

    @Test
    public void isParameterizedCharacter_shouldHandleAllParameterizedCharacters() {
        for (byte value = PclUtil.LOWEST_PARAMETERIZED_BYTE; value <= PclUtil.HIGHEST_PARAMETERIZED_BYTE; value++) {
            assertTrue(util.isParameterizedCharacter(value));
        }
    }

    @Test
    public void isTermination_shouldAllBytesAfterTheHighestTerminationCharacter() {
        for (byte value = PclUtil.HIGHEST_TERMINATION_BYTE + 1; value < Byte.MAX_VALUE; value++) {
            assertFalse(util.isTermination(value));
        }
    }

    @Test
    public void isTermination_shouldAllBytesBeforeTheLowestTerminationCharacter() {
        for (byte value = Byte.MIN_VALUE; value < PclUtil.LOWEST_TERMINATION_BYTE; value++) {
            assertFalse(util.isTermination(value));
        }
    }

    @Test
    public void isTermination_shouldHandleAllTerminationCharacters() {
        for (byte value = PclUtil.LOWEST_TERMINATION_BYTE; value <= PclUtil.HIGHEST_TERMINATION_BYTE; value++) {
            assertTrue(util.isTermination(value));
        }
    }

    @Test
    public void isEscape_shouldReturnFalseForAnyByteThatIsNotTheEscapeByte() {
        for (byte value = Byte.MIN_VALUE; value < Byte.MAX_VALUE; value++) {
            if (value == 27) {
                continue;
            }
            assertFalse(util.isEscape(value));
        }
    }

    @Test
    public void isEscape_shouldReturnTrueIfTheByteGivenIsTheEscapeByte() {
        assertTrue(util.isEscape((byte) 27));
    }

    @Test
    public void is2ByteCommandOperator_allValidPossibilities() {
        for (byte value = PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR; value <= PclUtil.HIGHEST_2BYTE_COMMAND_OPERATOR; value++) {
            assertTrue(util.is2ByteCommandOperator(value));
        }
    }

    @Test
    public void is2ByteCommandOperator_belowLowestCommandOperator() {
        for (byte value = Byte.MIN_VALUE; value < PclUtil.LOWEST_2BYTE_COMMAND_OPERATOR; value++) {
            assertFalse(value + " should not be valid operator", util.is2ByteCommandOperator(value));
        }
    }

    @Test
    public void is2ByteCommandOperator_aboveHighestCommandOperator() {
        for (byte value = PclUtil.HIGHEST_2BYTE_COMMAND_OPERATOR + 1; value < Byte.MAX_VALUE; value++) {
            assertFalse(value + " should not be valid operator", util.is2ByteCommandOperator(value));
        }
    }

}
