package com.valenguard.server.network.packet.out;

import java.io.DataOutputStream;
import java.io.IOException;

public class ValenguardOutputStream {

    private DataOutputStream dataOutputStream;

    public ValenguardOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    private int bytePosition = 0;

    private OpcodePacketData opcodePacketData = new OpcodePacketData();

    // The current buffer being worked on.
    private byte[] buffer = new byte[500];

    public void writeByte(byte b) {
        buffer[bytePosition++] = b;
    }

    public void writeBoolean(boolean b) {
        buffer[bytePosition++] = (byte) (b ? 0x01 : 0x00);
    }

    public void writeChar(char c) {
        buffer[bytePosition++] = (byte) ((c >>> 8) & 0xFF);
        buffer[bytePosition++] = (byte) (c & 0xFF);
    }

    public void writeShort(short c) {
        buffer[bytePosition++] = (byte) ((c >>> 8) & 0xFF);
        buffer[bytePosition++] = (byte) (c & 0xFF);
    }

    public void writeInt(int i) {
        buffer[bytePosition++] = (byte) ((i >>> 24) & 0xFF);
        buffer[bytePosition++] = (byte) ((i >>> 16) & 0xFF);
        buffer[bytePosition++] = (byte) ((i >>> 8) & 0xFF);
        buffer[bytePosition++] = (byte) (i & 0xFF);
    }

    public void writeLong(long l) {
        buffer[bytePosition++] = (byte) ((l >>> 56) & 0xFF);
        buffer[bytePosition++] = (byte) ((l >>> 48) & 0xFF);
        buffer[bytePosition++] = (byte) ((l >>> 40) & 0xFF);
        buffer[bytePosition++] = (byte) ((l >>> 32) & 0xFF);
        buffer[bytePosition++] = (byte) ((l >>> 24) & 0xFF);
        buffer[bytePosition++] = (byte) ((l >>> 16) & 0xFF);
        buffer[bytePosition++] = (byte) ((l >>> 8) & 0xFF);
        buffer[bytePosition++] = (byte) (l & 0xFF);
    }

    public void writeFloat(float f) {
        writeInt(Float.floatToIntBits(f));
    }

    public void writeDouble(double d) {
        writeLong(Double.doubleToLongBits(d));
    }

    /**
     * Ascii bits of string only. Lower 8 bits
     */
    public void writeString(String s) {
        if (s.length() > 0x7F)
            throw new RuntimeException("Tried writing a string greater than 0x7F in length.");
        buffer[bytePosition++] = (byte) s.length();
        for (int i = 0 ; i < s.length() ; i++) {
            buffer[bytePosition++] = (byte) s.charAt(i);
        }
    }

    // Fill current buffer up
    // Grab size
    // check if the size causes us to need to flush
    //     if yes
    //        finish writing any left over data (we have not added the current buffer at this point in time)
    //        then flush
    //        if last buffer write and flush
    //        else store buffer for later
    //     if no
    //        then check if opcodes are swapped
    //        if yes then write data

    public boolean currentBuffersInitialized() {
        return opcodePacketData.isInitialized();
    }

    public boolean doOpcodesMatch(ServerAbstractOutPacket serverAbstractOutPacket) {
        return opcodePacketData.getOpcode() == serverAbstractOutPacket.getOpcode();
    }

    public void createNewBuffers(ServerAbstractOutPacket serverAbstractOutPacket) {
        opcodePacketData.setOpcode(serverAbstractOutPacket.getOpcode());
        opcodePacketData.getBuffers().add(createNewBuffer());
        opcodePacketData.setInitialized(true);
        opcodePacketData.setNumberOfRepeats(1);
    }


    public void appendBewBuffer() {
        opcodePacketData.getBuffers().add(createNewBuffer());
        opcodePacketData.setNumberOfRepeats(opcodePacketData.getNumberOfRepeats() + 1);
    }

    public int fillCurrentBuffer(ServerAbstractOutPacket serverAbstractOutPacket) {
        serverAbstractOutPacket.createPacket(this);
        return bytePosition;
    }

    public void writeBuffers() throws IOException {
        boolean repeatsExist = opcodePacketData.getNumberOfRepeats() > 1;
        byte opcode = opcodePacketData.getOpcode();
        if (repeatsExist) opcode |= -0x80; // Special bit to tell the client there exist repeats


        dataOutputStream.writeByte(opcode);

        if (repeatsExist) {
            dataOutputStream.writeByte((byte) opcodePacketData.getNumberOfRepeats());
        }

        for (byte[] buffer : opcodePacketData.getBuffers()) {
            dataOutputStream.write(buffer);
        }
        opcodePacketData.getBuffers().clear();
        opcodePacketData.setNumberOfRepeats(-1);
        opcodePacketData.setOpcode((byte) -0x80);
        opcodePacketData.setInitialized(false);
    }

    private byte[] createNewBuffer() {
        byte[] newBuffer = new byte[bytePosition];
        System.arraycopy(buffer, 0, newBuffer, 0, bytePosition);
        buffer = new byte[500]; // todo zero memory might be faster
        bytePosition = 0;
        return newBuffer;
    }

    public void flush() throws IOException {
        dataOutputStream.flush();
    }

    public void close() throws IOException {
        dataOutputStream.close();
    }
}
