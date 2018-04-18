package com.playcrab.shotpic;

import com.iosdialog.widget.ActionSheetDialog;
import com.iosdialog.widget.AlertDialog;
import com.playcrab.util.BitmapUtil;
import com.playcrab.util.PermissionUtil;
import com.soundcloud.android.crop.Crop;
import com.soundcloud.android.crop.R;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class SavePictures {
    private static SavePictures savePictures = new SavePictures();
    private Activity context;
    private String savePath;// 图片保存路径
    private String imageName;// 图片名称
    private SavePicListener savePicListener;
    private int distinguish;// 分辨率
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

    public void corpImage(String savePaths, String imageNames, String distinguishability) {
        myLog("开始裁剪");
        this.savePath = savePaths;
        this.imageName = imageNames;
        this.distinguish = Integer.parseInt(distinguishability);
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
            new AlertDialog(context).builder().setTitle(context.getResources().getString(R.string.error_info)).setMsg(context.getResources().getString(R.string.error_tip))
                    .setPositiveButton(context.getResources().getString(R.string.sure), new View.OnClickListener() {
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
        new ActionSheetDialog(context).builder().setTitle(context.getResources().getString(R.string.dialog_title)).setCancelable(true).setCanceledOnTouchOutside(true)
                .addSheetItem(context.getResources().getString(R.string.camera), ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {

                    @Override
                    public void onClick(int which) {
                        Crop.takePhoto(context);
                    }
                }).addSheetItem(context.getResources().getString(R.string.photo), ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() {
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

            isRotation(Crop.originPath + "/" + Crop.originName, uri);
        }
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            myLog("。。。。。。。beginCrop:" + result.getData());
            isRotation(BitmapUtil.getPath(context, result.getData()), result.getData());

        } else if (requestCode == Crop.REQUEST_CROP) {
            myLog("。。。。。。。handleCrop");
            handleCrop(resultCode, result, RESULT_OK);
        }
    }

    /*
     * 判断手机系统是否对图片做了旋转
     */
    private void isRotation(String path, Uri originUri) {
        // 判断原生照片是否被手机系统旋转
        if (BitmapUtil.readPictureDegree(path) == 0) {
            myLog("照片正常没有旋转");
            beginCrop(originUri);// 未被旋转，直接开始裁剪
        } else {// 旋转到正常位置
            myLog("照片被旋转");
            Bitmap bitmap = BitmapUtil.amendRotatePhoto(path);
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null));
            beginCrop(uri);
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
                savePicListener.status(SAVEPICTURE_FAIL, context.getResources().getString(R.string.exception));
                e.printStackTrace();
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            myLog("。。。。。。。RESULT_ERROR");
            savePicListener.status(SAVEPICTURE_FAIL, context.getResources().getString(R.string.error_info) + Crop.getError(result).getMessage());
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            savePicListener.status(SAVEPICTURE_FAIL, context.getResources().getString(R.string.url_exception) + e.getMessage());
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
        String originName = sdf.format(new Date());
        File myCaptureFile = new File(subForder, originName);// 图片名称
        if (!myCaptureFile.exists()) {
            myCaptureFile.createNewFile();
        }
        // 保存截取后的图片
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
        bos.flush();
        bos.close();
        // 分辨率设置后重新保存
        BitmapUtil.transImage(savePath + originName, savePath + imageName, distinguish, distinguish);

        myLog("图片保存路径：" + savePath + imageName);
        // 通知系统刷新图库,通知cp图片保存成功
        notifySystemRefresh(myCaptureFile);
        savePicListener.status(SAVEPICTURE_SUCCESS, context.getResources().getString(R.string.save_success));
    }

    /*
     * 更新图库
     */
    private void notifySystemRefresh(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);// 这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
    }

}
