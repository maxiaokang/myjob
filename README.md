# Crop截图框架接入说明
***
| 文档版本     | 作者          | 修订时间 | 修订内容 |
| ----------- |:-------------:| -----:|--------:|
|  v0.1   | 马晓康 | 2017年8月29日 | 选取照片并截图功能|    
|  v0.3   | 马晓康 | 2018年4月3日 | 添加相机拍照功能，接口未变更|
***
```
前言：此文档旨在指导游戏开发人员接入该工具库，实现游戏中获取android系统图片的需求，请务必按照以下顺序实现接口。
```
##开发准备：
###添加依赖库：
```
1、将Crop依赖库与主工程关联；
2、在主工程的project.properties文件中加入manifestmerger.enabled=true配置完成manifest与依赖库关联。
```

##第一步：
###获取依赖库基础实例：
```
SavePictures savePictures = SavePictures.getInstance();
```
##第二步：
###初始化基础数据

```
/**
*@param Activity 当前activity
*@param SavePicListener 截图结果监听 
        返回值说明：status＝0 保存成功；status＝－1图片保存失败	describe截图结果描述
*注：status返回0时，可根据自定义图片路径＋名称得到截取成功后的图片，显示在相应位置。
*/
savePictures.initData(Activity activity,SavePicListener savePicListener)；
```
##第三步：
###截图接口调用
```
/**
*@param SAVE_REAL_PATH 自定义的图片保存路径
*@param imageNameString 自定义的图片名称
*/
savePictures.corpImage(String SAVE_REAL_PATH, String imageNameString);
```