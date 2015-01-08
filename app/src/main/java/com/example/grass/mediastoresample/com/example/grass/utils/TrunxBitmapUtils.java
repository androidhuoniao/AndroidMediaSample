

package com.example.grass.mediastoresample.com.example.grass.utils;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class TrunxBitmapUtils {

    private static final String TAG = "BitmapUtils";
    public static final int UNCONSTRAINED = -1;
    private static final int COMPRESS_JPEG_QUALITY = 90;

    public static final int COVER_LONGER_SIDE = 150;

    /**
     * get a bitamp can center inside the rect of displayWidth * displayHeight
     *
     * @param path
     * @param displayWidth
     * @param displayHeight
     * @return
     */
    public static Bitmap decodeBitmap(String path, int displayWidth,
                                      int displayHeight) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, op);
        int wRatio = (int) Math.ceil(op.outWidth / (float) displayWidth);
        int hRatio = (int) Math.ceil(op.outHeight / (float) displayHeight);
        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }
        op.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, op);
        return Bitmap
                .createScaledBitmap(bmp, displayWidth, displayHeight, true);
    }


    public static Bitmap decodeBitmap(String path, int maxImageSize) {
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, op);
        int scale = 1;
        if (op.outWidth > maxImageSize || op.outHeight > maxImageSize) {
            scale = (int) Math.pow(
                    2,
                    (int) Math.round(Math.log(maxImageSize
                            / (double) Math.max(op.outWidth, op.outHeight))
                            / Math.log(0.5)));
        }
        op.inJustDecodeBounds = false;
        op.inSampleSize = scale;
        bmp = BitmapFactory.decodeFile(path, op);
        return bmp;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
//        Bitmap map = BitmapFactory.decodeFile(filename, options);
//        int degree = getExifOrientation(filename);
//        Matrix matrix = new Matrix();
//        matrix.preRotate(degree);
//        map = Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight(), matrix, true);
        return BitmapFactory.decodeFile(filename, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        long start = System.currentTimeMillis();
        if (bitmap == null) {
            return null;
        }
        int newWidth = width;
        int newHeight = height;
        if (newHeight == 0) {
            newHeight = (int) (newWidth / (float) bitmap.getWidth() * bitmap
                    .getHeight());
        }
        Bitmap result = Bitmap.createBitmap(newWidth, newHeight,
                Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Matrix matrix = new Matrix();
        float scaleX = 1;
        float scaleY = 1;
        scaleX = newWidth / (float) bitmap.getWidth();
        if (height != 0) {
            scaleY = newHeight / (float) bitmap.getHeight();
        } else {
            scaleY = scaleX;
        }
        matrix.postScale(scaleX, scaleY);
        canvas.drawBitmap(bitmap, matrix, null);
        bitmap.recycle();
        Log.i("map", "map cost=" +
                (System.currentTimeMillis() - start));
        return result;
    }

    public static Bitmap scaleBitmap(String path, int newWidth, int newHeight) {
        Bitmap bm = BitmapFactory.decodeFile(path);
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        bm.recycle();
        return newbm;
    }

    public static Bitmap cropCenterBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (newWidth >= width || newHeight >= height) {
            return bitmap;
        }
        int startX = (width - newWidth) / 2;
        int startY = (height - newHeight) / 2;
        Bitmap result = Bitmap.createBitmap(bitmap, startX, startY, newWidth, newHeight);
        bitmap.recycle();
        return result;
    }

    public static boolean saveBitmapToFile(Bitmap bitmap, String filename) {
        boolean result = false;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(filename));
            fos.write(bitmap2byte(bitmap));
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fos) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }

    public static byte[] bitmap2byte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private static float hRadius = 10;
    private static float vRadius = 10;
    private static int iterations = 7;

    public static Drawable BoxBlurFilter(Bitmap bmp) {
        long start = System.currentTimeMillis();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < iterations; i++) {
            blur(inPixels, outPixels, width, height, hRadius);
            blur(outPixels, inPixels, height, width, vRadius);
        }
        blurFractional(inPixels, outPixels, width, height, hRadius);
        blurFractional(outPixels, inPixels, height, width, vRadius);
        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
        Drawable drawable = new BitmapDrawable(bitmap);
        Log.i("blur", "blur a pic cost " + (System.currentTimeMillis() - start));
        return drawable;
    }

    public static void blur(int[] in, int[] out, int width, int height,
                            float radius) {
        int widthMinus1 = width - 1;
        int r = (int) radius;
        int tableSize = 2 * r + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -r; i <= r; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
                        | (divide[tg] << 8) | divide[tb];

                int i1 = x + r + 1;
                if (i1 > widthMinus1)
                    i1 = widthMinus1;
                int i2 = x - r;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    public static void blurFractional(int[] in, int[] out, int width,
                                      int height, float radius) {
        radius -= (int) radius;
        float f = 1.0f / (1 + 2 * radius);
        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;

            out[outIndex] = in[0];
            outIndex += height;
            for (int x = 1; x < width - 1; x++) {
                int i = inIndex + x;
                int rgb1 = in[i - 1];
                int rgb2 = in[i];
                int rgb3 = in[i + 1];

                int a1 = (rgb1 >> 24) & 0xff;
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                int a2 = (rgb2 >> 24) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                int a3 = (rgb3 >> 24) & 0xff;
                int r3 = (rgb3 >> 16) & 0xff;
                int g3 = (rgb3 >> 8) & 0xff;
                int b3 = rgb3 & 0xff;
                a1 = a2 + (int) ((a1 + a3) * radius);
                r1 = r2 + (int) ((r1 + r3) * radius);
                g1 = g2 + (int) ((g1 + g3) * radius);
                b1 = b2 + (int) ((b1 + b3) * radius);
                a1 *= f;
                r1 *= f;
                g1 *= f;
                b1 *= f;
                out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
                outIndex += height;
            }
            out[outIndex] = in[width - 1];
            inIndex += width;
        }
    }

    public static int clamp(int x, int a, int b) {
        return (x < a) ? a : (x > b) ? b : x;
    }

    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable.
     */
    @TargetApi(12)
    public static int getBitmapSize(Bitmap bitmap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static int computeInSampleSizeForMoment(Bitmap map, int newWidth, int newHeight) {
        int width = map.getWidth();
        int height = map.getHeight();
        int inSampleSize = 1;
        if (width < newWidth && height < newHeight) {

        } else {
            float widthRadio = width / newWidth;
            float heightRadio = height / newHeight;
            inSampleSize = (int) Math.max(widthRadio, heightRadio);
        }
        return inSampleSize;
    }

    /**
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     * <p/>
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = UNCONSTRAINED.
     * <p/>
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(int width, int height,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(
                width, height, minSideLength, maxNumOfPixels);

        return initialSize <= 8
                ? nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    private static int computeInitialSampleSize(int w, int h,
                                                int minSideLength, int maxNumOfPixels) {
        if (maxNumOfPixels == UNCONSTRAINED
                && minSideLength == UNCONSTRAINED) return 1;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt((double) (w * h) / maxNumOfPixels));

        if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            int sampleSize = Math.min(w / minSideLength, h / minSideLength);
            return Math.max(sampleSize, lowerBound);
        }
    }


    // This computes a sample size which makes the longer side at least
    // minSideLength long. If that's not possible, return 1.
    public static int computeSampleSizeLarger(int w, int h,
                                              int minSideLength) {
        int initialSize = Math.max(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Fin the min x that 1 / x <= scale
    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor(1f / scale);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Find the max x that 1 / x >= scale.
    public static int computeSampleSize(float scale) {
        assertTrue(scale > 0);
        int initialSize = Math.max(1, (int) Math.ceil(1 / scale));
        return initialSize <= 8
                ? nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    public static Bitmap resizeDownToPixels(
            Bitmap bitmap, int targetPixels, boolean recycle) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = (float) Math.sqrt(
                (double) targetPixels / (width * height));
        if (scale >= 1.0f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap resizeDownBySideLength(
            Bitmap bitmap, int maxLength, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    // Resize the bitmap if each side is >= targetSize * 2
    public static Bitmap resizeDownIfTooBig(
            Bitmap bitmap, int targetSize, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.max(
                (float) targetSize / srcWidth, (float) targetSize / srcHeight);
        if (scale > 0.5f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    // Crops a square from the center of the original image.
    public static Bitmap cropCenter(Bitmap bitmap, boolean recycle) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width == height) return bitmap;
        int size = Math.min(width, height);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2, (size - height) / 2);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    public static Bitmap resizeDownAndCropCenter(Bitmap bitmap, int size,
                                                 boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int minSide = Math.min(w, h);
        if (w == h && minSide <= size) return Bitmap.createBitmap(bitmap);
        size = Math.min(size, minSide);

        float scale = Math.max((float) size / bitmap.getWidth(),
                (float) size / bitmap.getHeight());
        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale); // TODO: seems scale is a wrong value
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    public static void recycleSilently(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            bitmap.recycle();
        } catch (Throwable t) {
            Log.w(TAG, "unable recycle bitmap", t);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, int rotation, boolean recycle) {
        if (rotation == 0) return source;
        int w = source.getWidth();
        int h = source.getHeight();
        Matrix m = new Matrix();
        m.postRotate(rotation);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
        if (recycle) source.recycle();
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String filePath, int size) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int max = Math.max(width, height);
        if (max > 512) {
            float scale = 512f / max;
            int w = Math.round(scale * width);
            int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }
//    public static Bitmap createCoverVideoThumbnail(String filePath) {
//        Bitmap bitmap = null;
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        try {
//            retriever.setDataSource(filePath);
//            bitmap = retriever.getFrameAtTime(-1);
//        } catch (IllegalArgumentException ex) {
//            // Assume this is a corrupt video file
//        } catch (RuntimeException ex) {
//            // Assume this is a corrupt video file.
//        } finally {
//            try {
//                retriever.release();
//            } catch (RuntimeException ex) {
//                // Ignore failures while cleaning up.
//            }
//        }
//
//        if (bitmap == null) return null;
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int newWidth = 0;
//        int newHeight = 0;
//        if (width <= COVER_LONGER_SIDE && height <= COVER_LONGER_SIDE) {
//
//        } else {
//            if (width > height) {
//                newWidth = COVER_LONGER_SIDE;
//                newHeight = (int) (COVER_LONGER_SIDE * ((float) height / (float) width));
//            } else {
//                newHeight = COVER_LONGER_SIDE;
//                newWidth = (int) (COVER_LONGER_SIDE * ((float) width / (float) height));
//            }
//            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
//        }
//
//        return bitmap;
//    }

    public static Bitmap createCoverVideoThumbnail(String filePath) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
        if (bitmap == null) return null;
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, COVER_LONGER_SIDE, COVER_LONGER_SIDE);
        return bitmap;
    }

    public static byte[] compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,
                COMPRESS_JPEG_QUALITY, os);
        return os.toByteArray();
    }

    public static boolean isSupportedByRegionDecoder(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.startsWith("image/") &&
                (!mimeType.equals("image/gif") && !mimeType.endsWith("bmp"));
    }

    public static boolean isRotationSupported(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.equals("image/jpeg");
    }

    public static byte[] compressToBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    // Returns the next power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0 or
    // the answer overflows.
    private static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30)) throw new IllegalArgumentException();
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    // Returns the previous power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0
    private static int prevPowerOf2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    private static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    /*
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }


    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) &&
                (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }


    /* Maximum pixels size for created bitmap. */
    private static final int MAX_NUM_PIXELS_THUMBNAIL = 512 * 384;
    private static final int MAX_NUM_PIXELS_MICRO_THUMBNAIL = COVER_LONGER_SIDE * COVER_LONGER_SIDE;
    /**
     * Constant used to indicate we should recycle the input in
     */
    public static final int OPTIONS_RECYCLE_INPUT = 0x2;

    /**
     * Constant used to indicate the dimension of mini thumbnail.
     *
     * @hide Only used by media framework and media provider internally.
     */
    public static final int TARGET_SIZE_MINI_THUMBNAIL = 320;

    /**
     * Constant used to indicate the dimension of micro thumbnail.
     *
     * @hide Only used by media framework and media provider internally.
     */
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = COVER_LONGER_SIDE;

    public static final int MINI_KIND = 1;
    public static final int FULL_SCREEN_KIND = 2;
    public static final int MICRO_KIND = 3;

    public static Bitmap createImageThumbnail(String filePath, int kind) {
        long start = System.currentTimeMillis();
        if (TextUtils.isEmpty(filePath)) return null;
        Bitmap bitmap = null;
        if (bitmap == null) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(filePath);
                FileDescriptor fd = stream.getFD();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd, null, options);
                if (options.mCancel || options.outWidth <= 0
                        || options.outHeight <= 0) {
                    return null;
                }
                boolean heightBiggerWidth = options.outHeight > options.outWidth;
                int ratio = heightBiggerWidth ? options.outHeight / options.outWidth : options.outWidth / options.outHeight;
                if (ratio >= COVER_LONGER_SIDE) {
                    int left = 0, right, top = 0, bottom = 0;
                    BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(filePath, false);
                    if (heightBiggerWidth) {
                        right = options.outWidth;
                        top = options.outHeight / 2 - right / 2;
                        bottom = top + right;
                    } else {
                        bottom = options.outHeight;
                        left = options.outWidth / 2 - bottom / 2;
                        right = left + bottom;
                    }
                    Rect rect = new Rect(left, top, right, bottom);
                    bitmap = decoder.decodeRegion(rect, null);
                } else {
                    computeInsampleSize(options);
                    options.inJustDecodeBounds = false;
                    options.inDither = false;
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                }
                if (null != bitmap) {
                    int newWidth = 0;
                    int newHeight = 0;
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    if (width <= 0 || height <= 0) {
                        return null;
                    }
                    if (width <= COVER_LONGER_SIDE && height <= COVER_LONGER_SIDE) {

                    } else {
                        if (width > height) {
                            newWidth = COVER_LONGER_SIDE;
                            newHeight = (int) (COVER_LONGER_SIDE * ((float) height / (float) width));
                            if (newHeight <= 0) {
                                newHeight = 1;
                            }
                        } else {
                            newHeight = COVER_LONGER_SIDE;
                            newWidth = (int) (COVER_LONGER_SIDE * ((float) width / (float) height));
                            if (newWidth <= 0) {
                                newWidth = 1;
                            }
                        }
                        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                    }
                }
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(filePath);
                } catch (IOException ex) {
                    Log.w(TAG, ex);
                }
                if (null != bitmap && exif != null) {
                    if (bitmap.getHeight() <= 0 || bitmap.getHeight() <= 0) return null;
                    int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int rotationInDegrees = 0;
                    if (rotation == ExifInterface.ORIENTATION_ROTATE_90) {
                        rotationInDegrees = 90;
                    } else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {
                        rotationInDegrees = 180;
                    } else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {
                        rotationInDegrees = 270;
                    } else {
                        rotationInDegrees = 0;
                    }
                    Matrix matrix = new Matrix();
                    matrix.preRotate(rotationInDegrees);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
            } catch (IOException ex) {
                Log.e(TAG, "", ex);
            } catch (OutOfMemoryError oom) {
                Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "", ex);
                }
            }
        }
        Log.i("time", "createImageThumbnail: cost "+(System.currentTimeMillis() - start)+" filePath: "+filePath);
        return bitmap;
    }


    public static TrunxSize computeInsampleSize(BitmapFactory.Options exifOptions) {
        int width = exifOptions.outWidth;
        int height = exifOptions.outHeight;
        int longerSide = 0;
        TrunxSize size = new TrunxSize();
        if (width <= COVER_LONGER_SIDE && height <= COVER_LONGER_SIDE) {
            exifOptions.inSampleSize = 1;
            size.width = exifOptions.outWidth;
            size.height = exifOptions.outHeight;
        } else {
            if (width > height) {
                longerSide = width;
                size.width = COVER_LONGER_SIDE;
                size.height = (int) (COVER_LONGER_SIDE * ((float) height / (float) width));
                if (size.height <= 0) {
                    size.height = 1;
                }
            } else {
                longerSide = height;
                size.height = COVER_LONGER_SIDE;
                size.width = (int) (COVER_LONGER_SIDE * ((float) width / (float) height));
                if (size.width <= 0) {
                    size.width = 1;
                }
            }
            exifOptions.inSampleSize = longerSide / COVER_LONGER_SIDE;
        }
        return size;
    }

    /**
     * this only used for square rect
     *
     * @param exifOptions
     * @param side
     * @return
     */
    public static int computeInsampleSize(BitmapFactory.Options exifOptions, int side) {
        int width = exifOptions.outWidth;
        int height = exifOptions.outHeight;
        if (width == 0 || height == 0) {
            return -1;
        }
        int sampeSize = 1;
        int longerSide = 0;
        if (side <= 0) {
            side = COVER_LONGER_SIDE;
        }
        if (width <= side && height <= side) {

        } else {
            longerSide = width > height ? width : height;
            sampeSize = longerSide / side;
        }
        return sampeSize;
    }

    public static TrunxSize getScaleSize(int width, int height, int requestSide) {
        TrunxSize size = new TrunxSize();
        if (width <= requestSide && height <= requestSide) {
            size.width = width;
            size.height = height;
            size.isNeedScale = false;
        } else {
            if (width > height) {
                size.width = requestSide;
                size.height = (int) (requestSide * ((float) height / (float) width));
            } else {
                size.height = requestSide;
                size.width = (int) (requestSide * ((float) width / (float) height));
            }
            size.isNeedScale = true;
        }
        return size;
    }

    public static class TrunxSize {
        public int width;
        public int height;
        float ratio;
        public boolean isNeedScale;

        @Override
        public String toString() {
            return "width: " + width + " height: " + height;
        }
    }


    /**
     * SizedThumbnailBitmap contains the bitmap, which is downsampled either from
     * the thumbnail in exif or the full image.
     * mThumbnailData, mThumbnailWidth and mThumbnailHeight are set together only if mThumbnail
     * is not null.
     * <p/>
     * The width/height of the sized bitmap may be different from mThumbnailWidth/mThumbnailHeight.
     */
    private static class SizedThumbnailBitmap {
        public Bitmap mBitmap;
    }

    public static Bitmap rotateBitmap(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                        b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }

    public static Bitmap createMaskBitmap(Bitmap src, Resources resource, int mask) {
        Bitmap maskMap = BitmapFactory.decodeResource(resource,
                mask).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(maskMap);
        if (null != src) {
            canvas.scale((float) maskMap.getWidth() / (float) src.getWidth(), (float) maskMap.getHeight() / (float) src.getHeight());
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(src, 0, 0, paint);
            return maskMap;
        }
        return null;
    }

    public static Bitmap scaleClipBitmapByCircle(
            Bitmap src,
            int nRadius,
            float fStrokeWidth
    ) {
        if (null != src) {
            Bitmap maskMap = Bitmap.createBitmap(nRadius << 1, nRadius << 1, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(maskMap);
            canvas.drawARGB(0, 0, 0, 0);
            Paint ptDrawCircle = new Paint();
            ptDrawCircle.setColor(Color.BLACK);
            ptDrawCircle.setStyle(Style.FILL);
            canvas.drawCircle(nRadius, nRadius, nRadius, ptDrawCircle);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            Rect rcSrc = new Rect();
            rcSrc.set(0, 0, src.getWidth(), src.getHeight());
            Rect rcDst = new Rect();
            rcDst.set(0, 0, maskMap.getWidth(), maskMap.getHeight());
            canvas.drawBitmap(src, rcSrc, rcDst, paint);
            ptDrawCircle.setStyle(Style.STROKE);
            ptDrawCircle.setStrokeWidth(fStrokeWidth);
            ptDrawCircle.setColor(Color.WHITE);
            ptDrawCircle.setAntiAlias(true);
            canvas.drawCircle(nRadius, nRadius, nRadius - fStrokeWidth / 2, ptDrawCircle);
            return maskMap;
        }
        return null;
    }

}