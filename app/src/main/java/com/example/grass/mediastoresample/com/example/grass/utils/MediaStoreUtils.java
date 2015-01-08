package com.example.grass.mediastoresample.com.example.grass.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by grass on 1/8/15.
 */
public class MediaStoreUtils {

    private static byte[] mBuffer = new byte[1024 * 1024 * 1];
    public static byte[] getFileHashCode(String path) {
        synchronized(mBuffer) {
            File file = new File(path);
            FileInputStream in = null;
            MessageDigest messagedigest;
            try {
                in = new FileInputStream(file);
                messagedigest = MessageDigest.getInstance("SHA-256");
                int len = 0;
                while ((len = in.read(mBuffer)) > 0) {
                    messagedigest.update(mBuffer, 0, len);
                }
                return messagedigest.digest();
            } catch (NoSuchAlgorithmException e) {
//                Log.e("hashcode",
//                        "getFileSha256->NoSuchAlgorithmException###" + e.toString());
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
//                Log.e("hashcode",
//                        "getFileSha256->OutOfMemoryError###" + e.toString());
                e.printStackTrace();
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private static byte[] mBuffer2 = new byte[1024 * 1024 * 1];
    public static byte[] getFileHashCode2(String path) {
        synchronized(mBuffer2) {
            File file = new File(path);
            BufferedInputStream in = null;
            MessageDigest messagedigest;
            try {
                in = new BufferedInputStream(new FileInputStream(file));
                messagedigest = MessageDigest.getInstance("SHA-256");
                int len = 0;
                while ((len = in.read(mBuffer2)) > 0) {
                    messagedigest.update(mBuffer2, 0, len);
                }
                return messagedigest.digest();
            } catch (NoSuchAlgorithmException e) {
//                Log.e("hashcode",
//                        "getFileSha256->NoSuchAlgorithmException###" + e.toString());
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
//                Log.e("hashcode",
//                        "getFileSha256->OutOfMemoryError###" + e.toString());
                e.printStackTrace();
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static byte[] getFileHashCode3(String path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            DigestInputStream shaStream = new DigestInputStream(
                    new BufferedInputStream(new FileInputStream(path)),digest );
            int len = 0;
            while ((len = shaStream.read(mBuffer)) > 0) {

            }
            byte[] shaDigest = shaStream.getMessageDigest().digest();
            return shaDigest;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
