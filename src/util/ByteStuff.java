package util;

import java.nio.ByteBuffer;

public class ByteStuff {
    private static ByteBuffer tempBufferPutFloat = ByteBuffer.allocate(4);
    private static ByteBuffer tempBufferReadFloat = ByteBuffer.allocate(4);

    public static void putInt(int value, byte[] array, int offset) {
        array[offset] = (byte) (value >>> 24);
        array[offset + 1] = (byte) (value >>> 16);
        array[offset + 2] = (byte) (value >>> 8);
        array[offset + 3] = (byte) (value & 0xff);
    }

    public static synchronized void putFloat(float value, byte[] array, int offset) {
        tempBufferPutFloat.position(0);
        byte data[] = tempBufferPutFloat.putFloat(value).array();

        array[offset] = data[0];
        array[offset + 1] = data[1];
        array[offset + 2] = data[2];
        array[offset + 3] = data[3];
    }

    public static synchronized float readFloat(byte[] data, int offset) {
        tempBufferReadFloat.position(0);
        tempBufferReadFloat.put(data, offset, 4);
        tempBufferReadFloat.position(0);
        return tempBufferReadFloat.getFloat();
    }

    public static int readInt(byte[] value, int offset) {
        return ((value[offset] << 24))
                | ((value[1 + offset] & 0xff) << 16)
                | ((value[2 + offset] & 0xff) << 8)
                | (value[3 + offset] & 0xff);
    }

    public static byte[] subBytes(byte[] data, int start, int end) {
        int len = end - start;
        byte[] result = new byte[len];
        System.arraycopy(data, start, result, 0, len);
        return result;
    }

    public static void putBytes(byte[] values, byte[] data, int index) {
        for (int i = 0; i < values.length; i++) {
            data[index + i] = values[i];
        }
    }
}
