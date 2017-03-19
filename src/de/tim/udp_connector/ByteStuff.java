package de.tim.udp_connector;

class ByteStuff {
    static void putInt(int value, byte[] array, int offset) {
        array[offset] = (byte) (value >>> 24);
        array[offset + 1] = (byte) (value >>> 16);
        array[offset + 2] = (byte) (value >>> 8);
        array[offset + 3] = (byte) (value & 0xff);
    }

    static int readInt(byte[] value, int offset) {
        return ((value[offset] << 24))
                | ((value[1 + offset] & 0xff) << 16)
                | ((value[2 + offset] & 0xff) << 8)
                | (value[3 + offset] & 0xff);
    }

    static byte[] subBytes(byte[] data, int start, int end) {
        int len = end - start;
        byte[] result = new byte[len];
        System.arraycopy(data, start, result, 0, len);
        return result;
    }

    static void putBytes(byte[] values, byte[] data, int index) {
        System.arraycopy(values, 0, data, index, values.length);
    }
}
