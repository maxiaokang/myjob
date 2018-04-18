package com.playcrab.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class BitmapUtil {
    /**
     * 读取照片旋转角度
     * 
     * @param path
     *            照片路径
     * @return 角度
     */
    @SuppressLint("NewApi")
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 处理旋转后的图片
     * 
     * @param originpath
     *            原图路径
     * @param context
     *            上下文
     * @return 返回修复完毕后的图片路径
     */
    public static Bitmap amendRotatePhoto(String originpath) {

        // 取得图片旋转角度
        int angle = readPictureDegree(originpath);

        // 把原图压缩后得到Bitmap对象
        Bitmap bmp = getCompressPhoto(originpath);
        ;

        // 修复图片被旋转的角度
        Bitmap bitmap = rotaingImageView(angle, bmp);

        // // 保存修复后的图片并返回保存后的图片路径
        // return savePhotoToSD(bitmap, context);
        // 返回修复后的图片
        return bitmap;
    }

    /**
     * 旋转图片
     * 
     * @param angle
     *            被旋转角度
     * @param bitmap
     *            图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    /**
     * 把原图按1/10的比例压缩
     * 
     * @param path
     *            原图的路径
     * @return 压缩后的图片
     */
    public static Bitmap getCompressPhoto(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 10; // 图片的大小设置为原来的十分之一
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        options = null;
        return bmp;
    }

    // /**
    // * 保存Bitmap图片在SD卡中
    // * 如果没有SD卡则存在手机中
    // *
    // * @param mbitmap 需要保存的Bitmap图片
    // * @return 保存成功时返回图片的路径，失败时返回null
    // */
    // public static String savePhotoToSD(Bitmap mbitmap, Context context) {
    // FileOutputStream outStream = null;
    // String fileName = getPhotoFileName(context);
    // try {
    // outStream = new FileOutputStream(fileName);
    // // 把数据写入文件，100表示不压缩
    // mbitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
    // return fileName;
    // } catch (Exception e) {
    // e.printStackTrace();
    // return null;
    // } finally {
    // try {
    // if (outStream != null) {
    // // 记得要关闭流！
    // outStream.close();
    // }
    // if (mbitmap != null) {
    // mbitmap.recycle();
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // /**
    // * 使用当前系统时间作为上传图片的名称
    // *
    // * @return 存储的根路径+图片名称
    // */
    // public static String getPhotoFileName(Context context) {
    // File file = new File(getPhoneRootPath(context) + FILES_NAME);
    // // 判断文件是否已经存在，不存在则创建
    // if (!file.exists()) {
    // file.mkdirs();
    // }
    // // 设置图片文件名称
    // SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_hhmmss",
    // Locale.getDefault());
    // Date date = new Date(System.currentTimeMillis());
    // String time = format.format(date);
    // String photoName = "/" + time;
    // return file + photoName;
    // }
    // /**
    // * 获取手机可存储路径
    // *
    // * @param context 上下文
    // * @return 手机可存储路径
    // */
    // private static String getPhoneRootPath(Context context) {
    // // 是否有SD卡
    // if
    // (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
    // || !Environment.isExternalStorageRemovable()) {
    // // 获取SD卡根目录
    // return context.getExternalCacheDir().getPath();
    // } else {
    // // 获取apk包下的缓存路径
    // return context.getCacheDir().getPath();
    // }
    // }
    /*
     * 截取后图片的分辨率设置
     */
    public static void transImage(String fromFile, String toFile, int width, int height) {// 分辨率
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(fromFile);
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            // 缩放图片的尺寸
            float scaleWidth = (float) width / bitmapWidth;
            float scaleHeight = (float) height / bitmapHeight;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 产生缩放后的Bitmap对象
            Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
            // save file
            File myCaptureFile = new File(toFile);
            FileOutputStream out = new FileOutputStream(myCaptureFile);
            if (resizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {// 第三个参数为照片品质比例，100为未缩放
                out.flush();
                out.close();
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();// 记得释放资源，否则会内存溢出
            }
            if (!resizeBitmap.isRecycled()) {
                resizeBitmap.recycle();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // 通用的从uri中获取路径的方法, 兼容以上说到的2个shceme
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "";
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
