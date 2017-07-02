package com.zic.lqmb.activator.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final int freeMegabytesToInstallLqmb = 900;

    public static String getFileChecksum(File file) throws IOException {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "getFileChecksum: " + e.toString());
        }

        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            if (digest != null) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        fis.close();

        //Get the hash's bytes
        byte[] bytes = new byte[0];
        if (digest != null) {
            bytes = digest.digest();
        }

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }


    public static void deleteFileOrDir(File fileOrDir) {
        if (fileOrDir.isDirectory())
            for (File child : fileOrDir.listFiles())
                deleteFileOrDir(child);

        fileOrDir.delete();
    }

    public static boolean copyAssetsFile(Context context, String fileOrDir, String desDirPath) {
        AssetManager assetManager = context.getAssets();
        String assets[];
        // $desPath is file or dir
        String desPath = desDirPath + "/" + fileOrDir;
        InputStream in;
        OutputStream out;

        // Copy from assets to $desDirPath
        try {
            assets = assetManager.list(fileOrDir);
            // If $des is not a directory
            if (assets.length == 0) {
                in = assetManager.open(fileOrDir);
                out = new FileOutputStream(desPath);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();

            } else {
                File des = new File(desPath);
                // $des is now a directory
                if (!des.exists())
                    des.mkdir();
                for (String asset : assets) {
                    copyAssetsFile(context, fileOrDir + "/" + asset, desDirPath);
                }
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "copyAssetsFile" + e.toString());
            return false;
        }
    }

    public static String findFile(String dir, String name) {

        File[] children = new File(dir).listFiles();

        for (File child : children) {
            if (child.isDirectory()) {
                String found = findFile(child.getAbsolutePath(), name);
                if (found != null) return found;
            } else {
                if (name.equals(child.getName())) return child.getAbsolutePath();
            }
        }

        return null;
    }

    public static boolean isAvailableToInstallLqmb() {
        return getAvailableInternalMemorySize() > (long) freeMegabytesToInstallLqmb * 1024 * 1024;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
        } else {
            blockSize = stat.getBlockSize();
        }
        long availableBlocks;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            availableBlocks = stat.getAvailableBlocks();
        }

        return (availableBlocks * blockSize);
    }

    public static String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());

            long blockSize;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
            } else {
                blockSize = stat.getBlockSize();
            }
            long availableBlocks;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableBlocks = stat.getAvailableBlocksLong();
            } else {
                availableBlocks = stat.getAvailableBlocks();
            }

            return formatSize(availableBlocks * blockSize);
        } else {
            return "Error!";
        }
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public boolean isExternalStorageWritable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state) || "mounted_ro".equals(state);
    }
}
