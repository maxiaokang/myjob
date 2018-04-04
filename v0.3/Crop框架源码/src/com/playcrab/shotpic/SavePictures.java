package com.playcrab.shotpic;

import com.iosdialog.widget.ActionSheetDialog;
import com.iosdialog.widget.AlertDialog;
import com.playcrab.util.PermissionUtil;
import com.soundcloud.android.crop.Crop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SavePictures {
    private static SavePictures savePictures = new SavePictures();
    private Activity context;
    private String savePath;
    private String imageName;
    private SavePicListener savePicListener;
    private static int SAVEPICTURE_SUCCESS = 0;
    private static int SAVEPICTURE_FAIL = -1;
    public static final int RESULT_OK = -1;
    final static String TAG = "playcrab";

    public static SavePictures getInstance() {
        return savePictures;
    }

    public void initData(Activity contexts, SavePicListener savePicListener) {
        this.context = contexts;
        this.savePicListener = savePicListener;
        PermissionUtil.requestNeedPermission(context);
    }

    public void corpImage(String savePaths, String imageNames) {
        myLog("开始裁剪");
        this.savePath = savePaths;
        this.imageName = imageNames;
        isExist();
    }

    /**
     * 
     * @param path
     *            判断文件夹路径是否存在
     */
    private void isExist() {
        File file = new File(savePath);
        // 判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists() || ("").equals(imageName) || null == imageName) {
            new AlertDialog(context).builder().setTitle("错误信息").setMsg("请传入正确的图片路径和图片名称").setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 填写事件
                }
            }).show();
        } else {
            showChoose();
        }
    }

    private void showChoose() {
        new ActionSheetDialog(context).builder().setTitle("获取图片方式").setCancelable(true).setCanceledOnTouchOutside(true)
                .addSheetItem("拍照获取图片", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {

                    @Override
                    public void onClick(int which) {
                        Crop.takePhoto(context);
                    }
                }).addSheetItem("选取相册图片", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        Crop.pickImage(context);// 选取相册图片
                    }
                }).show();

    }

    private void myLog(String info) {
        Log.d(TAG, info);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.RESULT_TAKEPHOTO && resultCode == -1) { // result返回的是-1表示拍照成功，返回的是0表示拍照失败
            Uri uri = Uri.fromFile(Crop.picture);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(uri);
            context.sendBroadcast(intent); // 这里我们发送广播让MediaScanner,扫描我们制定的文件这样在系统的相册中我们就可以找到我们拍摄的照片了
            beginCrop(uri);
        }
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            myLog("。。。。。。。beginCrop:" + result.getData());
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            myLog("。。。。。。。handleCrop");
            handleCrop(resultCode, result, RESULT_OK);
        }
    }

    /*
     * 开始裁剪
     */
    private void beginCrop(Uri source) {// new File(getCacheDir(), "cropped")
        Uri destination = Uri.fromFile(new File(context.getCacheDir(), "cropped"));
        myLog("。。。。。。。asSquare().start");
        Crop.of(source, destination).asSquare().start(context);
    }

    /*
     * 保存裁剪后的图片
     */
    private void handleCrop(int resultCode, final Intent result, int result_ok) {
        if (resultCode == result_ok) {
            myLog("。。。。。。。RESULT_OK");
            try {
                saveFile(getBitmapFromUri(Crop.getOutput(result)));
            } catch (IOException e) {
                savePicListener.status(SAVEPICTURE_FAIL, "图片保存异常，请检查 权限／保存目录");
                e.printStackTrace();
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            myLog("。。。。。。。RESULT_ERROR");
            savePicListener.status(SAVEPICTURE_FAIL, "错误信息" + Crop.getError(result).getMessage());
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            savePicListener.status(SAVEPICTURE_FAIL, "图片URI转换异常：" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void saveFile(Bitmap bm) throws IOException {
        String subForder = savePath;
        File foder = new File(subForder);
        if (!foder.exists()) {
            foder.mkdirs();
        }
        File myCaptureFile = new File(subForder, imageName);// 图片名称
        if (!myCaptureFile.exists()) {
            myCaptureFile.createNewFile();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.PNG, 80, bos);
        bos.flush();
        bos.close();
        // savePicListener.status(SAVEPICTURE_SUCCESS, "图片保存成功");
        myLog("图片保存路径：" + savePath + imageName);
        // 通知系统刷新图库,通知cp图片保存成功
        notifySystemRefresh(myCaptureFile);
        savePicListener.status(SAVEPICTURE_SUCCESS, "图片保存成功");
    }

    private void notifySystemRefresh(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);// 这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
    }

}
