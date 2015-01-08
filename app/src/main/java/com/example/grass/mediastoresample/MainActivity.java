package com.example.grass.mediastoresample;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.grass.mediastoresample.com.example.grass.utils.MediaStoreUtils;
import com.example.grass.mediastoresample.com.example.grass.utils.TrunxBitmapUtils;

import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    private Button mOrigin_btn;
    private Button mThumb_btn;
    private int mScreenWidth;
    private int mScreeheight;
    private String mPath = "/sdcard/DSC_0423.JPG";
    int[][] mResolutions = new int[][]{
            {4128, 3096},
            {4128, 2322},
            {3264, 2488},
            {3264, 1836},
            {2048, 1152},
            {5132, 2988},
            {3984, 2988},
            {2976, 2976},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.origin).setOnClickListener(mClickListener);
        findViewById(R.id.thumbnail).setOnClickListener(mClickListener);
        findViewById(R.id.inSampleSize).setOnClickListener(mClickListener);
        findViewById(R.id.sha).setOnClickListener(mClickListener);
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        mScreeheight = display.getHeight();
        mScreenWidth = display.getWidth();

    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.origin:
                    queryOriginalImages(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    queryOriginalImages(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    break;

                case R.id.thumbnail:
                    queryThumbnailsFromDB(MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI);
                    queryThumbnailsFromDB(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI);
                    break;

                case R.id.inSampleSize:

                    for (int index = 0; index < mResolutions.length; index++) {
                        calculateInSampleSize(mResolutions[index][1], mResolutions[index][0]);
                        computeSampleSize(mResolutions[index][1], mResolutions[index][0], mScreenWidth, mScreeheight * mScreenWidth);
                    }
                    break;

                case R.id.sha://this test may be not right,becasue MessageDigest maybe has cache ,the following one get hashcode will be faster than the previous one
                    long start = System.currentTimeMillis();
                    byte[] hash1 = MediaStoreUtils.getFileHashCode(mPath);
                    long start2 = System.currentTimeMillis();
                    byte[] hash2 = MediaStoreUtils.getFileHashCode2(mPath);
                    long start3 = System.currentTimeMillis();
                    byte[] hash3 = MediaStoreUtils.getFileHashCode3(mPath);
                    long end = System.currentTimeMillis();
                    Log.i("hash", "2: " + (Arrays.equals(hash1, hash2)) + " 3: " + Arrays.equals(hash1, hash3) + " 1 cost: " + (start2 - start) + " 2 cost: " + (start3 - start2) + " 3 cost: " + (end - start3));
                    break;
            }
        }
    };


    private void queryThumbnails(Uri uri) {
        Cursor cursor = null;
        try {
            String[] projection = new String[]{MediaStore.Images.Thumbnails.DATA};
            cursor = MediaStore.Images.Thumbnails.queryMiniThumbnails(
                    getContentResolver(), uri,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    null);
            Log.i("time", "queryThumbnails count: " + (null != cursor ? cursor.getCount() : "null") + " uri: " + uri);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                long originId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));
                Log.i("time", "----queryThumbnails: originId: " + originId + " path: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) cursor.close();
        }
    }


    /**
     * get the thumbnail path by the original picture's id
     * @param origId
     * @return
     */
    private String getThumbnailPath(long origId) {
        long start = System.currentTimeMillis();
        Cursor cursor = null;
        try {
            String[] projection = new String[]{MediaStore.Images.Thumbnails.DATA};
            cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
                    getContentResolver(), origId,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    projection);
//            Log.i("time", "getThumbnailPath count: "+cursor.getCount()+" origid: "+origId);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();//**EDIT**
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
//                Log.i("time", "getThumbnailPath: cost "+(System.currentTimeMillis() - start));
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return "";
    }


    private Cursor queryOriginalImages(Uri uri) {
        String[] proj = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DESCRIPTION,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MINI_THUMB_MAGIC,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_ADDED
        };
        queryThumbnails(uri);
        Cursor cursor = MediaStore.Images.Media.query(
                getContentResolver(),
                uri,
                proj,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
        );

        Log.i("time", "queryImages count: " + (null != cursor ? cursor.getCount() : -1) + " uri: " + uri.toString());
        if (cursor != null && cursor.getCount() > 0) {
            int origidIndex = cursor
                    .getColumnIndex(MediaStore.Images.Media._ID);
            int pathIndex = cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);
            int miniTypeIndex = cursor
                    .getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
            int latitudeIndex = cursor
                    .getColumnIndex(MediaStore.Images.Media.LATITUDE);
            int longitudeIndex = cursor
                    .getColumnIndex(MediaStore.Images.Media.LONGITUDE);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                long origid = cursor.getLong(origidIndex);
                String path = cursor.getString(pathIndex);
                String thumbpath = getThumbnailPath(origid);
//                Log.i("time", "---queryImages :"+"origid:  "+origid+" thumbpath: "+thumbpath);
                TrunxBitmapUtils.createImageThumbnail(path, TrunxBitmapUtils.MINI_KIND);
                TrunxBitmapUtils.createImageThumbnail(thumbpath, TrunxBitmapUtils.MINI_KIND);
                Log.i("time", "------------------------------------------------------------");
            }
        }
        return cursor;
    }


    private int calculateInSampleSize(int sWidth, int sHeight) {

        int reqWidth = mScreenWidth;
        int reqHeight = mScreeheight;
        // Raw height and width of image
        float inSampleSize = 1;
        if (reqWidth == 0 || reqHeight == 0) {
            return 32;
        }

        if (sHeight > reqHeight || sWidth > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final float heightRatio = (float) sHeight / (float) reqHeight;
            final float widthRatio = (float) sWidth / (float) reqWidth;

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            final float totalPixels = sWidth * sHeight;
            final float totalReqPixelsCap = reqWidth * reqHeight;
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
            getBestPowerOf2(inSampleSize);
//            Log.i("sample", "calculateInSampleSize: heightRatio: " + heightRatio + " widthRatio: " + widthRatio
//                    + " inSampleSize: " +inSampleSize+" sWidth: "+sWidth);

        }
        // We want the actual sample size that will be used, so round down to nearest power of 2.
        int power = 1;
        while (power * 2 <= inSampleSize) {
            power = power * 2;
        }
        Log.i("sample", "calculateInSampleSize: power: " + power + " inSampleSize: " + inSampleSize + " sWidth: " + sWidth);
        return power;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static int computeSampleSize(int w, int h,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(w, h, minSideLength,
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
//        Log.i("sample", "computeSampleSize");
        Log.i("sample", "calculateInSampleSize: roundedSize: " + roundedSize + " w: " + w + " h: " + h
                + " newW: " + w / roundedSize + " newH: " + (h / roundedSize));
        return roundedSize;
    }

    private static int computeInitialSampleSize(double w, double h,
                                                int minSideLength, int maxNumOfPixels) {

        int lowerBound =
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound =
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        return upperBound;
    }

    // Returns the next power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0 or
    // the answer overflows.
    public static int nextPowerOf2(int n) {
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
    public static int prevPowerOf2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    private int getBestPowerOf2(float value) {
        int result = 1;
        int n = (int) value;
        int nextPower = nextPowerOf2(n);
        int prePower = prevPowerOf2(n);
        float nextValue = Math.abs(nextPower - n);
        float preValue = Math.abs(value - prePower);
        if (nextValue > preValue) {
            result = prePower;
        } else {
            result = nextPower;
        }
        Log.i("result", "getBestPowerOf2: value: " + value + " result: " + result);
        return result;
    }


    private void queryThumbnailsFromDB(Uri uri) {
        String[] proj = {
                MediaStore.Images.Thumbnails._ID,
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.KIND,
                MediaStore.Images.Thumbnails.HEIGHT,
                MediaStore.Images.Thumbnails.WIDTH,
                MediaStore.Images.Thumbnails.IMAGE_ID
        };

        Cursor cursor = MediaStore.Images.Thumbnails.query(getContentResolver(),uri,proj);
        if(null == cursor) return;
        int idIndex = cursor
                .getColumnIndex(MediaStore.Images.Thumbnails._ID);
        int pathIndex = cursor
                .getColumnIndex(MediaStore.Images.Thumbnails.DATA);
        int kindIndex = cursor
                .getColumnIndex(MediaStore.Images.Thumbnails.KIND);
        int widthIndex = cursor.getColumnIndex(MediaStore.Images.Thumbnails.WIDTH);
        int heightIndex = cursor
                .getColumnIndex(MediaStore.Images.Thumbnails.HEIGHT);
        int imageIndex = cursor
                .getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID);

        Log.i("thumb", "queryThumbnailsFromDB :"+cursor.getCount()+" uri: "+uri);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                .moveToNext()) {
            long id = cursor.getLong(idIndex);
            String path = cursor.getString(pathIndex);
            int kind = cursor.getInt(kindIndex);
            int width = cursor.getInt(widthIndex);
            int height = cursor.getInt(heightIndex);
            int iamgeid = cursor.getInt(imageIndex);
            Log.i("thumb", "id: "+id+" kind: "+kind+" width: "+width+" height: "+height +" iamge: "+iamgeid+" path: "+path);
        }
        cursor.close();
    }

}
