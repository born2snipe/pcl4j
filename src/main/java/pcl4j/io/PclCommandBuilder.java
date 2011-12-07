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


import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A builder for building pcl commands
 */
public class PclCommandBuilder {
    private static final PclCommandFactory FACTORY = new PclCommandFactory();
    private static final PclUtil UTIL = new PclUtil();
    private boolean validate;
    private byte parameterized, group, terminator;
    private String value;
    private byte[] binaryData;

    /**
     * Constructs a builder with validation turned on
     */
    public PclCommandBuilder() {
        this(true);
    }

    /**
     * Constructs a builder
     *
     * @param validate true - validates the values given
     *                 false - allows bad input values
     */
    public PclCommandBuilder(boolean validate) {
        this.validate = validate;
    }

    /**
     * sets the parameterized byte of the command
     *
     * @param value - the parameterized byte
     * @return this instance of the builder
     */
    public PclCommandBuilder p(int value) {
        if (validate && !UTIL.isParameterizedCharacter((byte) value))
            throw new IllegalArgumentException("Invalid parameterized byte given");
        parameterized = (byte) value;
        return this;
    }

    /**
     * sets the group byte of the command
     *
     * @param value - the group byte
     * @return this instance of the builder
     */
    public PclCommandBuilder g(int value) {
        if (validate && !UTIL.isGroupCharacter((byte) value)) {
            throw new IllegalArgumentException("Invalid group byte given");
        }
        group = (byte) value;
        return this;
    }

    /**
     * sets the terminator byte of the command
     *
     * @param value - the terminator byte
     * @return this instance of the builder
     */
    public PclCommandBuilder t(int value) {
        if (validate && !UTIL.isTermination((byte) value))
            throw new IllegalArgumentException("Invalid terminator byte given");
        terminator = (byte) value;
        return this;
    }

    /**
     * sets the value of the command
     *
     * @param value - the value contents
     * @return this instance of the builder
     */
    public PclCommandBuilder v(String value) {
        if (validate && value == null)
            throw new IllegalArgumentException("Invalid value given");
        this.value = value;
        return this;
    }

    /**
     * sets the binary data of the command
     *
     * @param value - the binary data as a String
     * @return this instance of the builder
     */
    public PclCommandBuilder d(String value) {
        return d(value.getBytes());
    }

    /**
     * sets the binary data of the command
     *
     * @param value - the binary data as a byte array
     * @return this instance of the builder
     */
    public PclCommandBuilder d(byte[] value) {
        this.binaryData = value;
        return this;
    }

    /**
     * creates a copy of this builder and returns a new instance
     *
     * @return a copy of this builder
     */
    public PclCommandBuilder copy() {
        PclCommandBuilder child = new PclCommandBuilder();
        child.parameterized = parameterized;
        child.group = group;
        child.value = value;
        child.terminator = terminator;
        child.binaryData = binaryData;
        return child;
    }

    /**
     * constructs the pcl command as a byte array
     *
     * @return a byte array representing the command
     */
    public byte[] toBytes() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(PclUtil.ESCAPE);
        data.write(parameterized);
        data.write(group);
        if (value != null) {
            try {
                data.write(value.getBytes());
            } catch (IOException e) {
            }
        }
        data.write(terminator);
        if (binaryData != null) {
            try {
                data.write(binaryData);
            } catch (IOException e) {
            }
        }
        return data.toByteArray();
    }

    /**
     * constructs a pcl command with the position defaulted to -1
     *
     * @return a new PclCommand
     */
    public PclCommand toCommand() {
        return toCommand(-1);
    }

    /**
     * constructs a pcl command with the given position
     *
     * @param position - the position of the command
     * @return a new PclCommand
     */
    public PclCommand toCommand(long position) {
        return FACTORY.build(position, toBytes());
    }
}
