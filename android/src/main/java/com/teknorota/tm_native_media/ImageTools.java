package com.teknorota.tm_native_media;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class ImageTools {
    public static ByteArrayOutputStream compressImage(Bitmap image, int quality) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, quality, output);

        return output;
    }

    public static Bitmap scaleImage(String inputPath, int height, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(inputPath, options);

        if(width == 0 || height == 0 || (width > options.outWidth && height > options.outHeight)) {
            width = options.outWidth;
            height = options.outHeight;
        }
        int[] retval = calculateAspectRatio(options.outWidth, options.outHeight, width, height);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, width, height);
        Bitmap unscaledBitmap = BitmapFactory.decodeFile(inputPath, options);
        return Bitmap.createScaledBitmap(unscaledBitmap, retval[0], retval[1], true);
    }

    public static Bitmap cropImage(Bitmap inputBitmap, int x, int y, int width, int height) {
        return Bitmap.createBitmap(inputBitmap, x, y, width, height);
    }

    public static Bitmap rotateImage(Bitmap inputBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight(), matrix, true);
        inputBitmap.recycle();
        return rotatedBitmap;
    }

    public static Bitmap flipImageHorizontal(Bitmap inputBitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1, inputBitmap.getWidth() / 2f, inputBitmap.getHeight() / 2f);
        Bitmap rotatedBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight(), matrix, true);
        inputBitmap.recycle();
        return rotatedBitmap;
    }

    public static Bitmap flipImageVertical(Bitmap inputBitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, inputBitmap.getWidth() / 2f, inputBitmap.getHeight() / 2f);
        Bitmap rotatedBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight(), matrix, true);
        inputBitmap.recycle();
        return rotatedBitmap;
    }

    private static int[] calculateAspectRatio(int origWidth, int origHeight, int width, int height) {
        int newWidth = width;
        int newHeight = height;

        if (newWidth <= 0 && newHeight <= 0) {
            newWidth = origWidth;
            newHeight = origHeight;
        } else if (newWidth > 0 && newHeight <= 0) {
            newHeight = (newWidth * origHeight) / origWidth;
        } else if (newWidth <= 0 && newHeight > 0) {
            newWidth = (newHeight * origWidth) / origHeight;
        } else {
            double newRatio = newWidth / (double) newHeight;
            double origRatio = origWidth / (double) origHeight;
            if (origRatio > newRatio) {
                newHeight = (newWidth * origHeight) / origWidth;
            } else if (origRatio < newRatio) {
                newWidth = (newHeight * origWidth) / origHeight;
            }
        }
        int[] retval = new int[2];
        retval[0] = newWidth;
        retval[1] = newHeight;
        return retval;
    }

    private static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        final float srcAspect = (float)srcWidth / (float)srcHeight;
        final float dstAspect = (float)dstWidth / (float)dstHeight;
        if (srcAspect > dstAspect) {
            return srcWidth / dstWidth;
        } else {
            return srcHeight / dstHeight;
        }
    }
}
