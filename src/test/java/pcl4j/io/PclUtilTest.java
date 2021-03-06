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

import static junit.framework.Assert.*;

public class PclUtilTest {
    private PclUtil util;

    @Before
    public void setUp() throws Exception {
        util = new PclUtil();
    }

    @Test
    public void isCommandExpectingData_commandNotExpectingData() {
        assertFalse(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('c').v("5").t('S').toBytes()));
    }

    @Test
    public void isCommandExpectingData_CreateFontHeaderOfBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p(')').g('s').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_DownloadCharacterOfBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('(').g('s').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_DefineSymbolSetCharacters() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('(').g('f').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_AlphanumericID() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('&').g('n').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_GraphicsDataBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('b').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_ConfigureRasterDataBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('g').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_moveVerticallyRasterLines() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('b').v("4").t('Y').toBytes()));
    }

    @Test
    public void isCommandExpectingData_colourDataBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('b').v("4").t('V').toBytes()));
    }

    @Test
    public void isCommandExpectingData_configureImageDataBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('v').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_ColourLookupTableBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('l').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_ColourTreatment() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('o').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_downloadDitherMatrixBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('*').g('m').v("4").t('W').toBytes()));
    }

    @Test
    public void isCommandExpectingData_TransparentPrintDataBytes() {
        assertTrue(util.isCommandExpectingData(new PclCommandBuilder().p('&').g('p').v("4").t('X').toBytes()));
    }

    @Test
    public void isCommandExpectingData_shouldBePerformant() {
        byte[] commandBytes = new PclCommandBuilder().p('&').g('p').v("4").t('X').toBytes();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++)
            assertTrue(util.isCommandExpectingData(commandBytes));

        long elapsed = System.currentTimeMillis() - start;
        assertTrue("it took longer than expected to look up if a command expect binary data", elapsed < 500);
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

    @Test
    public void isUniversalExit_shouldReturnTrueIfThereIsAnExactMatch() {
        byte[] commandData = new PclCommandBuilder(false).p('%').g('-').v("12345").t('X').toBytes();
        assertTrue(util.isUniversalExit(commandData));
    }

    @Test
    public void isUniversalExit_shouldReturnFalseIfTheCommandGivenIsOnlyPartiallyAnUniversalExitCommand() {
        assertFalse(util.isUniversalExit(new byte[]{0x1b, '%', '-', '1', '2'}));
    }

    @Test
    public void isUniversalExit_shouldReturnFalseIfItDoesNotMatch() {
        byte[] commandData = new PclCommandBuilder(false).p('%').g('-').v("11").t('X').toBytes();
        assertFalse(util.isUniversalExit(commandData));
    }

    @Test
    public void isUniversalExit_shouldBePerformant() {
        byte[] commandData = new PclCommandBuilder(false).p('%').g('-').v("12345").t('X').toBytes();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            util.isUniversalExit(commandData);
        }
        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("PclUtilTest.isUniversalExit_shouldBePerformant -- " + elapsedTime);
        assertTrue("this should be a performant method (elasped: " + elapsedTime + " millis)", elapsedTime < 100);
    }

    @Test
    public void convertValueToInt_shouldSupportWithLeadingPlusSymbol() {
        assertEquals(10, util.convertValueToInt("+10".getBytes()));
    }

    @Test
    public void convertValueToInt_shouldSupportWithLeadingMinusSymbol() {
        assertEquals(-100, util.convertValueToInt("-100".getBytes()));
    }

    @Test
    public void convertValueToInt_shouldSupportWithPaddedSpaces() {
        assertEquals(20, util.convertValueToInt(" 20 ".getBytes()));
    }

    @Test
    public void convertValueToInt_shouldSupportWithDecimalPoints() {
        assertEquals(10, util.convertValueToInt("10.01".getBytes()));
    }

    @Test
    public void convertValueToInt_shouldSupportDecimalPointWithNoLeadingDigits() {
        assertEquals(0, util.convertValueToInt(".01".getBytes()));
    }

    @Test
    public void convertValueToInt_shouldReturnZeroIfNoBytesAreGiven() {
        assertEquals(0, util.convertValueToInt(new byte[0]));
    }

    @Test
    public void convertValueToInt_shouldBePerformant() {
        byte[] commandData = "+10.0001".getBytes();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            util.convertValueToInt(commandData);
        }
        long elapsedTime = System.currentTimeMillis() - start;
        System.out.println("PclUtilTest.convertValueToInt_shouldBePerformant -- " + elapsedTime);
        assertTrue("this should be a performant method (elasped: " + elapsedTime + " millis)", elapsedTime < 100);
    }

    private void assertBytes(byte[] expectedValue, byte[] actualBytes) {
        assertTrue("Byte do not match. expected=[" + new String(expectedValue) + "], actual=[" + new String(actualBytes) + "]",
                Arrays.equals(expectedValue, actualBytes));
    }
}
