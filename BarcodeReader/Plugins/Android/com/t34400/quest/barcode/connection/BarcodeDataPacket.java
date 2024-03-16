package com.t34400.quest.barcode.connection;

import android.graphics.PointF;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BarcodeDataPacket {
    private static final int META_DATA_SIZE = 44; // 4 (totalSize) + 8 (unixTime) + 4 * 2 * 4 (cornerPoints)
    public final long unixTime;
    public final PointF[] cornerPoints;
    public final String rawValue;

    public BarcodeDataPacket(long unixTime, PointF[] cornerPoints, String rawValue) throws IllegalArgumentException {
        if (cornerPoints == null || cornerPoints.length < 4) {
            throw new IllegalArgumentException("Corner points too short.");
        }

        this.unixTime = unixTime;
        this.cornerPoints = cornerPoints;
        this.rawValue = rawValue;
    }

    public void writeToOutputStream(OutputStream outputStream) throws IOException {
        byte[] rawValueBytes = rawValue.getBytes(StandardCharsets.UTF_8);
        int rawValueSize = rawValueBytes.length;

        int totalSize = META_DATA_SIZE + rawValueSize;
        byte[] outputBytes = new byte[totalSize];

        ByteBuffer.wrap(outputBytes)
                .putInt(rawValueSize)
                .putLong(unixTime)
                .putFloat(cornerPoints[0].x).putFloat(cornerPoints[0].y)
                .putFloat(cornerPoints[1].x).putFloat(cornerPoints[1].y)
                .putFloat(cornerPoints[2].x).putFloat(cornerPoints[2].y)
                .putFloat(cornerPoints[3].x).putFloat(cornerPoints[3].y)
                .put(rawValueBytes);

        outputStream.write(outputBytes);
    }

    public static BarcodeDataPacket readFromInputStream(DataInputStream dataInputStream) throws IOException {
        int rawValueSize = dataInputStream.readInt();
        if (rawValueSize <= 0) {
            return null;
        }

        long unitTime = dataInputStream.readLong();

        PointF[] cornerPoints = new PointF[4];
        cornerPoints[0] = readPointF(dataInputStream);
        cornerPoints[1] = readPointF(dataInputStream);
        cornerPoints[2] = readPointF(dataInputStream);
        cornerPoints[3] = readPointF(dataInputStream);

        byte[] rawValueBytes = new byte[rawValueSize];
        int bytesRead = 0;
        while (bytesRead < rawValueSize) {
            int bytesToRead = Math.min(rawValueSize - bytesRead, 1024);
            int result = dataInputStream.read(rawValueBytes, bytesRead, bytesToRead);
            if (result == -1) {
                return null;
            }
            bytesRead += result;
        }

        String rawValue = new String(rawValueBytes, StandardCharsets.UTF_8);
        return new BarcodeDataPacket(unitTime, cornerPoints, rawValue);
    }

    private static PointF readPointF(DataInputStream dataInputStream) throws IOException {
        float x = dataInputStream.readFloat();
        float y = dataInputStream.readFloat();
        return new PointF(x, y);
    }
}
