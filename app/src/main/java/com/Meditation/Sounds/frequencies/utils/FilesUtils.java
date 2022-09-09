package com.Meditation.Sounds.frequencies.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.Meditation.Sounds.frequencies.QApplication;
import com.Meditation.Sounds.frequencies.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Admin on 3/28/2018.
 */

public class FilesUtils {
    public static File getRemovabeStorageDir(Context context) {
        try {
            List<File> storages = getRemovabeStorages(context);
            if (!storages.isEmpty()) {
                return storages.get(0);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        String anotherWay = getRemovabeStoragesOther();
        if (anotherWay != null) {
            return new File(anotherWay);
        }
        final String SECONDARY_STORAGE = System.getenv("SECONDARY_STORAGE");
        if (SECONDARY_STORAGE != null) {
            return new File(SECONDARY_STORAGE.split(":")[0]);
        }
        return null;
    }

    public static String getRemovabeStoragesOther() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            File storage = new File("/storage");

            if (storage.exists()) {
                File[] files = storage.listFiles();

                for (File file : files) {
                    if (file.exists()) {
                        try {
                            if (Environment.isExternalStorageRemovable(file)) {
                                return file.getPath();
                            }
                        } catch (Exception e) {
                            Log.e("TAG", e.toString());
                        }
                    }
                }
            }
        } else {
            String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
            String s = "";
            try {
                final Process process = new ProcessBuilder().command("mount")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    s = s + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }

            // parse output
            final String[] lines = s.split("\n");
            for (String line : lines) {
                if (!line.toLowerCase().contains("asec")) {
                    if (line.matches(reg)) {
                        String[] parts = line.split(" ");
                        for (String part : parts) {
                            if (part.startsWith("/"))
                                if (!part.toLowerCase().contains("vold"))
                                    return part;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<File> getRemovabeStorages(Context context) throws Exception {
        List<File> storages = new ArrayList<>();

        Method getService = Class.forName("android.os.ServiceManager")
                .getDeclaredMethod("getService", String.class);
        if (!getService.isAccessible()) getService.setAccessible(true);
        IBinder service = (IBinder) getService.invoke(null, "mount");

        Method asInterface = Class.forName("android.os.storage.IMountService$Stub")
                .getDeclaredMethod("asInterface", IBinder.class);
        if (!asInterface.isAccessible()) asInterface.setAccessible(true);
        Object mountService = asInterface.invoke(null, service);

        Object[] storageVolumes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getPackageName();
            int uid = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;
            Method getVolumeList = mountService.getClass().getDeclaredMethod(
                    "getVolumeList", int.class, String.class, int.class);
            if (!getVolumeList.isAccessible()) getVolumeList.setAccessible(true);
            storageVolumes = (Object[]) getVolumeList.invoke(mountService, uid, packageName, 0);
        } else {
            Method getVolumeList = mountService.getClass().getDeclaredMethod("getVolumeList");
            if (!getVolumeList.isAccessible()) getVolumeList.setAccessible(true);
            storageVolumes = (Object[]) getVolumeList.invoke(mountService, (Object[]) null);
        }

        for (Object storageVolume : storageVolumes) {
            Class<?> cls = storageVolume.getClass();
            Method isRemovable = cls.getDeclaredMethod("isRemovable");
            if (!isRemovable.isAccessible()) isRemovable.setAccessible(true);
            if ((boolean) isRemovable.invoke(storageVolume, (Object[]) null)) {
                Method getState = cls.getDeclaredMethod("getState");
                if (!getState.isAccessible()) getState.setAccessible(true);
                String state = (String) getState.invoke(storageVolume, (Object[]) null);
                if (state.equals("mounted")) {
                    Method getPath = cls.getDeclaredMethod("getPath");
                    if (!getPath.isAccessible()) getPath.setAccessible(true);
                    String path = (String) getPath.invoke(storageVolume, (Object[]) null);
                    storages.add(new File(path));
                }
            }
        }

        return storages;
    }

    public static File getSdcardStore(){
        return Environment.getExternalStorageDirectory();
    }

    public static void clearAllImageTmp(Context context) {
        File cacheDir = new File(FilesUtils.getSdcardStore().getAbsolutePath());
        if (cacheDir == null || (cacheDir != null && !cacheDir.exists())) {
            cacheDir = context.getCacheDir();
            String rootDir = cacheDir.getAbsolutePath() + "/TASKSAPP";
            cacheDir = new File(rootDir);
            if (!cacheDir.exists())
                cacheDir.mkdirs();
        } else {
            cacheDir = new File(FilesUtils.getSdcardStore().getAbsolutePath() + "/TASKSAPP");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }
        deleteRecursive(cacheDir);

    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    public static File resizeImages(Context context, String path) throws IOException {
//        SimpleDateFormat SDF = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());
//        File cacheDir = context.getExternalCacheDir();
//        if (cacheDir == null)
//            //fall back
//            cacheDir = context.getCacheDir();
//        String rootDir = cacheDir.getAbsolutePath() + "/ImageCompressor";
//        File root = new File(rootDir);
//        if (!root.exists())
//            root.mkdirs();
        SimpleDateFormat SDF = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());
        File cacheDir = new File(FilesUtils.getSdcardStore().getAbsolutePath());
        if (cacheDir == null || (cacheDir != null && !cacheDir.exists())) {
            cacheDir = context.getCacheDir();
            String rootDir = cacheDir.getAbsolutePath() + "/TASKSAPP";
            cacheDir = new File(rootDir);
            if (!cacheDir.exists())
                cacheDir.mkdirs();
        } else {
            cacheDir = new File(FilesUtils.getSdcardStore().getAbsolutePath() + "/TASKSAPP");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }
        String rootDir = cacheDir.getAbsolutePath();
        File root = new File(rootDir);
        if (!root.exists())
            root.mkdirs();

        Bitmap bitmap = decodeImageFromFiles(path, 1024);
        File compressed = new File(root, SDF.format(new Date()) + ".jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap == null) {
            return new File(path);
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(compressed);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.flush();
        fileOutputStream.close();
        bitmap.recycle();
        bitmap = null;
        return compressed;
    }

    public static Bitmap decodeImageFromFiles(String path, int maxSize) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, scaleOptions);
        int scale = 1;
//        while (scaleOptions.outWidth / scale / 2 >= width
//                || scaleOptions.outHeight / scale / 2 >= height) {
//            scale *= 2;
//        }
        if (scaleOptions.outWidth > scaleOptions.outHeight) {
            scale = scaleOptions.outWidth / maxSize;
        } else {
            scale = scaleOptions.outHeight / maxSize;
        }
        scale++;
        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(path, outOptions);

        ExifInterface ei = null;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    bitmap.recycle();
                    bitmap = null;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    bitmap.recycle();
                    bitmap = null;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    bitmap.recycle();
                    bitmap = null;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            return rotatedBitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static File resizeImages2(final Context context, String path, boolean isReduceQuality) throws IOException {
        SimpleDateFormat SDF = new SimpleDateFormat("yyyymmddhhmmss", Locale.getDefault());
        File cacheDir = new File(FilesUtils.getSdcardStore().getAbsolutePath());
        if (cacheDir == null || (cacheDir != null && !cacheDir.exists())) {
            cacheDir = context.getCacheDir();
            String rootDir = cacheDir.getAbsolutePath() + "/TASKSAPP";
            cacheDir = new File(rootDir);
            if (!cacheDir.exists())
                cacheDir.mkdirs();
        } else {
            cacheDir = new File(FilesUtils.getSdcardStore().getAbsolutePath() + "/TASKSAPP");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }
        String rootDir = cacheDir.getAbsolutePath();
        File root = new File(rootDir);
        if (!root.exists())
            root.mkdirs();

        File orgFile = new File(path);
        if (orgFile.length() < 1 * 1024 * 1024) {
            return orgFile;
        }


        Bitmap bitmap;
        if (isReduceQuality) {
            bitmap = BitmapFactory.decodeFile(path);
            ExifInterface ei = null;
            try {
                ei = new ExifInterface(path);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap rotatedBitmap = null;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                        bitmap.recycle();
                        bitmap = null;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                        bitmap.recycle();
                        bitmap = null;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                        bitmap.recycle();
                        bitmap = null;
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotatedBitmap = bitmap;
                }
                bitmap = rotatedBitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
            scaleOptions.inJustDecodeBounds = false;
            BitmapFactory.decodeFile(path, scaleOptions);
            bitmap = decodeImageFromFiles3(path, orgFile.length(), (int) (scaleOptions.outWidth * 0.7), (int) (scaleOptions.outHeight * 0.7), scaleOptions.outWidth, scaleOptions.outHeight);
        }
        final File compressed = new File(root, SDF.format(new Date()) + ".jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, isReduceQuality ? 60 : 50, byteArrayOutputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(compressed);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.flush();
        fileOutputStream.close();
        bitmap.recycle();
        bitmap = null;
        if (compressed.length() > 1 * 1024 * 1024) {
            return resizeImages2(context, compressed.getPath(), false);
        }
        return compressed;
    }

    public static Bitmap decodeImageFromFiles3(String path, double fileSize, int width, int height, int outWidth, int outHeight) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        double scale = 1;
//        while (outWidth / scale >= width
//                && outHeight / scale >= height) {
//            scale += 0.2;
//        }
        double scale = Math.sqrt(fileSize / 1024 / 1024 * 1.0);
        if (scale < 2 && scale > 1) {
            scale = 2;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (outWidth / scale), (int) (outHeight / scale), false);
        bitmap.recycle();
        bitmap = null;
        return resizedBitmap;
    }

    public static boolean deleteFileInDir(File dir) {
        try {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                if (children != null) {
                    for (String child : children) {
                        boolean success = deleteFileInDir(new File(dir, child));
                        if (!success) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception ignored){ }
        return true;
    }

    public static String getFileJsonDefaultLocal() {
        File CACHE_FOLDER = new File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER);
        if (CACHE_FOLDER.exists()) {
            for (int i = 0; i < CACHE_FOLDER.listFiles().length; i++) {
                String filePath = CACHE_FOLDER.listFiles()[i].getPath();
                if (filePath.endsWith(".json")) {
                    return filePath;
                }
            }
        }
        return null;
    }

    public static String getPlaylistDefaultQuantum(String filePath) {
        String jString = null;
        File playlistPath = new File(filePath);
        if (playlistPath.exists()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(playlistPath);
                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                    /* Instead of using default, pass in a decoder. */
                    jString = Charset.defaultCharset().decode(bb).toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return jString;
    }

    public static String getPlaylistDefaultQuantum() {
        String jString = null;
        File CACHE_FOLDER = new File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER);
        if (CACHE_FOLDER.exists()) {
            File playlistPath = new File(CACHE_FOLDER.getPath(), "quantum_playlists.json");
            if (playlistPath.exists()) {
                FileInputStream stream = null;
                try {
                    stream = new FileInputStream(playlistPath);
                    try {
                        FileChannel fc = stream.getChannel();
                        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                        /* Instead of using default, pass in a decoder. */
                        jString = Charset.defaultCharset().decode(bb).toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return jString;
    }

    public static boolean isExistPlaylistDefaultQuantumFile() {
        File CACHE_FOLDER = new File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER);
        if (CACHE_FOLDER.exists()) {
            File playlistPath = new File(CACHE_FOLDER.getPath(), "quantum_playlists.json");
            return playlistPath.exists();
        }
        return false;
    }

    public static void deletePlaylistDefaultQuantumFile() {
        File CACHE_FOLDER = new File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER);
        if (CACHE_FOLDER.exists()) {
            File playlistPath = new File(CACHE_FOLDER.getPath(), "quantum_playlists.json");
            if (playlistPath.exists()) {
                playlistPath.delete();
            }
        }
    }

    public static void showComingSoon(Context context) {
        new AlertDialog.Builder(context)
                //.setTitle(context.getString(R.string.app_name))
                .setMessage(context.getString(R.string.msg_coming_soon))
                .setPositiveButton(R.string.txt_ok, null).show();
    }
}