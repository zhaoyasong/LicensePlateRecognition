package com.aiseminar.platerecognizer.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aiseminar.EasyPR.PlateRecognizer;
import com.aiseminar.platerecognizer.R;
import com.aiseminar.platerecognizer.base.BaseActivity;
import com.aiseminar.util.BitmapUtil;
import com.aiseminar.util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 创建车牌识别的Activity
 * <p>
 * created by song on 2017-06-12.9:45
 */
public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener {

    //Log提示的时候使用
    private static final String TAG = CameraActivity.class.getSimpleName();
    //设置切换摄像头（Demo中暂时没有该功能，后期项目需求应该也不需要添加可以考虑去掉）
    private int cameraPosition = 0; // 0表示后置，1表示前置
    //surface的控制器
    private SurfaceHolder mSvHolder;
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private MediaPlayer mShootMP;
    private PlateRecognizer mPlateRecognizer;
    private SurfaceView mSvCamera;
    private ImageView mIvPlateRect;
    private ImageView mIvCapturePhoto;
    private TextView mTvPlateResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化控件
        initWidget();
        //创建牌照识别的对象
        mPlateRecognizer = new PlateRecognizer(this);
        //初始化数据
        initData();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        mSvCamera = (SurfaceView) findViewById(R.id.svCamera);
        mIvPlateRect = (ImageView) findViewById(R.id.ivPlateRect);
        //牌照点击的按钮
        mIvCapturePhoto = (ImageView) findViewById(R.id.ivCapturePhoto);
        //识别的结果显示
        mTvPlateResult = (TextView) findViewById(R.id.tvPlateResult);
        //照相设置点击事件
        mIvCapturePhoto.setOnClickListener(this);
    }

    /**
     * 在oNCreate方法之后执行
     */
    @Override
    public void onStart() {
        super.onStart();
        if (this.checkCameraHardware(this) && (mCamera == null)) {
            // 打开camera
            mCamera = getCamera();
            // 设置camera方向
            mCameraInfo = getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (null != mCameraInfo) {
                adjustCameraOrientation();
            }

            if (mSvHolder != null) {
                setStartPreview(mCamera, mSvHolder);
            }
        }
    }

    /**
     * 每次切换界面的时候都要释放照相机的资源
     */
    @Override
    public void onPause() {
        super.onPause();
        /**
         * 记得释放camera，方便其他应用调用
         */
        releaseCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 初始化相关data
     */
    private void initData() {
        // 获得句柄  当一个应用程序需要引用其他系统所管理的内存块或者是对象的时候就要使用句柄
        mSvHolder = mSvCamera.getHolder(); // 获得句柄
        // 添加回调
        mSvHolder.addCallback(this);
    }

    /**
     * 获取摄像机
     *
     * @return
     */
    private Camera getCamera() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
            Log.e(TAG, "Camera is not available (in use or does not exist)");
        }
        return camera;
    }

    /**
     * 获取摄像机数据、参数
     *
     * @param facing
     * @return
     */
    private Camera.CameraInfo getCameraInfo(int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return cameraInfo;
            }
        }
        return null;
    }

    /**
     * 调整摄像头的方向
     */
    private void adjustCameraOrientation() {
        if (null == mCameraInfo || null == mCamera) {
            return;
        }

        int orientation = this.getWindowManager().getDefaultDisplay().getOrientation();
        int degrees = 0;

        switch (orientation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            // back-facing
            result = (mCameraInfo.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    /**
     * 释放mCamera
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停掉原来摄像头的预览
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 响应按钮的点击事件 999为前后摄像头切换的事件 暂时可以不做处理
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case 999: // R.id.id_switch_camera_btn:
                // 切换前后摄像头
                int cameraCount = 0;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

                for (int i = 0; i < cameraCount; i++) {
                    Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
                    if (cameraPosition == 1) {
                        // 现在是后置，变更为前置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            /**
                             * 记得释放camera，方便其他应用调用
                             */
                            releaseCamera();
                            // 打开当前选中的摄像头
                            mCamera = Camera.open(i);
                            // 通过surfaceview显示取景画面
                            setStartPreview(mCamera, mSvHolder);
                            cameraPosition = 0;
                            break;
                        }
                    } else {
                        // 现在是前置， 变更为后置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            /**
                             * 记得释放camera，方便其他应用调用
                             */
                            releaseCamera();
                            mCamera = Camera.open(i);
                            setStartPreview(mCamera, mSvHolder);
                            cameraPosition = 1;
                            break;
                        }
                    }

                }
                break;
            case R.id.ivCapturePhoto:
                // 拍照,设置相关参数 暂时没有要求可以不设置 按照默认设置就行
                Camera.Parameters params = mCamera.getParameters();

//                params.setPictureFormat(ImageFormat.JPEG);
//                DisplayMetrics metric = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(metric);
//                int width = metric.widthPixels;  // 屏幕宽度（像素）
//                int height = metric.heightPixels;  // 屏幕高度（像素）
//                params.setPreviewSize(width, height);

                // 自动对焦
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(params);
                try {
                    mCamera.takePicture(shutterCallback, null, jpgPictureCallback);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mSvHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mSvHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            //打印错误日志
            e.printStackTrace();
        }

        setStartPreview(mCamera, mSvHolder);
    }

    /**
     * 当surfaceView关闭时，关闭预览并释放资源
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //记得释放camera，方便其他应用调用
        releaseCamera();
        holder = null;
        mSvCamera = null;
    }

    /**
     * TakePicture牌照的回调
     */
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            shootSound();
            mCamera.setOneShotPreviewCallback(previewCallback);
        }
    };

    Camera.PictureCallback rawPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();
        }
    };

    Camera.PictureCallback jpgPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();

            File pictureFile = FileUtil.getOutputMediaFile(FileUtil.FILE_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                // 照片转方向
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap normalBitmap = BitmapUtil.createRotateBitmap(bitmap);
//                fos.write(data);
                normalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                // 更新图库
                // 把文件插入到系统图库
//                try {
//                    MediaStore.Images.Media.insertImage(CameraActivity.this.getContentResolver(),
//                            pictureFile.getAbsolutePath(), pictureFile.getName(), "Photo taked by RoadParking.");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
                // 最后通知图库更新
                CameraActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + pictureFile.getAbsolutePath())));
                Toast.makeText(CameraActivity.this, "图像已保存。", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };


    /**
     * 检查设备中照相机是否可用
     *
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // 当前设备照相机可用
            return true;
        } else {
            // 当前设备照相机不可用
            return false;
        }
    }

    /**
     * activity返回式返回拍照图片路径
     *
     * @param mediaFile
     */
    private void returnResult(File mediaFile) {
//        Intent intent = new Intent();
//        intent.setData(Uri.fromFile(mediaFile));
//        this.setResult(RESULT_OK, intent);
        this.finish();
    }

    /**
     * 设置camera显示取景画面,并预览
     *
     * @param camera
     */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    /**
     * 播放系统拍照声音
     */
    private void shootSound() {
        AudioManager meng = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        if (volume != 0) {
            if (mShootMP == null)
                mShootMP = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            if (mShootMP != null)
                mShootMP.start();
        }
    }

    /**
     * 获取Preview界面的截图，并存储
     */
    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // 获取Preview图片转为bitmap并旋转
            Camera.Size size = mCamera.getParameters().getPreviewSize(); //获取预览大小
            final int w = size.width;  //宽度
            final int h = size.height;
            final YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h, null);
            // 转Bitmap
            ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
            if (!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)) {
                return;
            }
            byte[] tmp = os.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
            Bitmap rotatedBitmap = BitmapUtil.createRotateBitmap(bitmap);

            cropBitmapAndRecognize(rotatedBitmap);
        }
    };

    /**
     * 裁剪出牌照的区域并识别
     *
     * @param originalBitmap
     */
    public void cropBitmapAndRecognize(Bitmap originalBitmap) {
        // 裁剪出关注区域
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        Bitmap sizeBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

        int rectWidth = (int) (mIvPlateRect.getWidth() * 1.5);
        int rectHight = (int) (mIvPlateRect.getHeight() * 1.5);
        int[] location = new int[2];
        mIvPlateRect.getLocationOnScreen(location);
        location[0] -= mIvPlateRect.getWidth() * 0.5 / 2;
        location[1] -= mIvPlateRect.getHeight() * 0.5 / 2;
        Bitmap normalBitmap = Bitmap.createBitmap(sizeBitmap, location[0], location[1], rectWidth, rectHight);

        // 保存图片并进行车牌识别
        File pictureFile = FileUtil.getOutputMediaFile(FileUtil.FILE_TYPE_PLATE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }

        try {
            mTvPlateResult.setText("正在识别...");
            FileOutputStream fos = new FileOutputStream(pictureFile);
            normalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            // 最后通知图库更新
            CameraActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + pictureFile.getAbsolutePath())));

            // 进行车牌识别
            String plate = mPlateRecognizer.recognize(pictureFile.getAbsolutePath());
            if (null != plate && !plate.equalsIgnoreCase("0")) {
                mTvPlateResult.setText(plate);
            } else {
                mTvPlateResult.setText("请调整角度");
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

}

