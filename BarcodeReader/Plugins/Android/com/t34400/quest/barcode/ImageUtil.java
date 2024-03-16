package com.t34400.quest.barcode;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;

import java.nio.ByteBuffer;

public class ImageUtil {
    public static int[] convertRGBA8888toIntArray(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        int offset = 0;

        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                int r = buffer.get(offset) & 0xff;
                int g = buffer.get(offset + 1) & 0xff;
                int b = buffer.get(offset + 2) & 0xff;
                int a = buffer.get(offset + 3) & 0xff;
                int pixel = Color.argb(a, r, g, b);
                pixels[row * image.getWidth() + col] = pixel;
                offset += pixelStride;
            }
            offset += rowPadding;
        }

        return pixels;
    }

    public static Bitmap extractLeftHalfBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer srcBuffer = planes[0].getBuffer();

        int rowStride = planes[0].getRowStride();
        int pixelStride = planes[0].getPixelStride();

        int dstWidth = image.getWidth() / 2;
        int dstHeight = image.getHeight();
        int dstRowStride = dstWidth * pixelStride;
        byte[] dstBytes = new byte[dstRowStride * dstHeight];

        for (int row = 0; row < dstHeight; row++) {
            int srcIndex = row * rowStride;
            int dstIndex = row * dstRowStride;

            srcBuffer.position(srcIndex);
            srcBuffer.get(dstBytes, dstIndex, dstRowStride);
        }

        Bitmap bitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(dstBytes));

        return bitmap;
    }
}
