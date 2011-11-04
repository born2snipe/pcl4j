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

import java.util.Arrays;

import static junit.framework.Assert.*;
import static pcl.io.PclUtil.*;

public class PclUtilTest {
    private PclUtil util;

    @Before
    public void setUp() throws Exception {
        util = new PclUtil();
    }

    @Test
    public void getParameterizedByte_shouldReturnTheParameterizedByteOfACommand() {
        assertEquals(2, util.getParameterizedByte(new TwoByteCommand(new byte[]{1, 2})));
    }

    @Test
    public void getTerminatorByte_shouldReturnTheTerminatorByteFromAParameterizedCommandWithBinaryData() {
        assertEquals(PclUtil.LOWEST_TERMINATION_BYTE, util.getTerminatorByte(new ParameterizedCommand(new byte[]{
                PclUtil.ESCAPE, PclUtil.PARAMETERIZED_BYTE_POSITION, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_TERMINATION_BYTE, 1, 2, 3
        })));
    }

    @Test
    public void getTerminatorByte_shouldReturnTheTerminatorByteFromAParameterizedCommand() {
        assertEquals(PclUtil.LOWEST_TERMINATION_BYTE, util.getTerminatorByte(new ParameterizedCommand(new byte[]{
                PclUtil.ESCAPE, PclUtil.PARAMETERIZED_BYTE_POSITION, PclUtil.LOWEST_GROUP_BYTE, '1', PclUtil.LOWEST_TERMINATION_BYTE
        })));
    }

    @Test(expected = IllegalStateException.class)
    public void getTerminatorByte_shouldBlowUpIfTerminatorByteCouldNotBeLocated() {
        util.getTerminatorByte(new ParameterizedCommand(new byte[]{
                PclUtil.ESCAPE, PclUtil.PARAMETERIZED_BYTE_POSITION, PclUtil.LOWEST_GROUP_BYTE, 0
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTerminatorByte_shouldBlowUpIfTheCommandGivenIsATwoByteCommand() {
        util.getTerminatorByte(new TwoByteCommand(new byte[2]));
    }

    @Test
    public void getGroupByte_shouldReturnTheGroupByteFromAParameterizedCommand() {
        assertEquals(3, util.getGroupByte(new ParameterizedCommand(new byte[]{1, 2, 3})));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGroupByte_shouldBlowUpIfATwoByteCommandIsGiven() {
        util.getGroupByte(new TwoByteCommand(new byte[2]));
    }

    @Test
    public void changeTerminatorToParameter_allTerminatorsCanBeConverted() {
        int range = PclUtil.HIGHEST_TERMINATION_BYTE - PclUtil.LOWEST_TERMINATION_BYTE;
        byte currentTerminator = PclUtil.LOWEST_TERMINATION_BYTE;
        for (int i = 0; i < range; i++) {
            assertEquals(PclUtil.LOWEST_PARAMETER_BYTE + i, util.changeTerminatorToParameter(currentTerminator));
            currentTerminator++;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void changeTerminatorToParameter_blowUpIfNotTerminationByte() {
        util.changeTerminatorToParameter((byte) -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void changeParameterToTerminator_shouldBlowUpIfANonParameterIsGiven() {
        util.changeParameterToTerminator((byte) -1);
    }

    @Test
    public void changeParameterToTerminator_allParametersCanBeConverted() {
        int range = PclUtil.HIGHEST_TERMINATION_BYTE - PclUtil.LOWEST_TERMINATION_BYTE;
        byte currentParameter = PclUtil.LOWEST_PARAMETER_BYTE;
        for (int i = 0; i < range; i++) {
            assertEquals(PclUtil.LOWEST_TERMINATION_BYTE + i, util.changeParameterToTerminator(currentParameter));
            currentParameter++;
        }
    }

    @Test
    public void getBinaryData_twoByteCommandIsAlwaysFalse() {
        TwoByteCommand command = new TwoByteCommand(new byte[2]);

        assertEquals(0, util.getBinaryData(command).length);
    }

    @Test
    public void getBinaryData_commandHasBinaryData() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE, '1', '2'
        });
        byte[] expectedBinaryData = new byte[]{'1', '2'};

        assertTrue(Arrays.equals(expectedBinaryData, util.getBinaryData(command)));
    }

    @Test
    public void getBinaryData_commandHasNoBinaryData() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });

        assertEquals(0, util.getBinaryData(command).length);
    }

    @Test
    public void getBinaryData_commandHasNoBinaryDataOrValue() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, LOWEST_TERMINATION_BYTE
        });

        assertEquals(0, util.getBinaryData(command).length);
    }

    @Test
    public void hasBinaryData_twoByteCommandIsAlwaysFalse() {
        TwoByteCommand command = new TwoByteCommand(new byte[2]);

        assertFalse(util.hasBinaryData(command));
    }

    @Test
    public void hasBinaryData_commandHasNoBinaryData() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE
        });

        assertFalse(util.hasBinaryData(command));
    }

    @Test
    public void hasBinaryData_commandHasBinaryData() {
        ParameterizedCommand command = new ParameterizedCommand(new byte[]{
                ESCAPE, LOWEST_PARAMETERIZED_BYTE, LOWEST_GROUP_BYTE, '1', LOWEST_TERMINATION_BYTE, '1', '2'
        });

        assertTrue(util.hasBinaryData(command));
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
