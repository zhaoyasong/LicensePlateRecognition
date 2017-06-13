package com.aiseminar.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Date;

/**
 * 文件管理的工具类
 * <p>
 * created by song on 2017-06-12.9:45
 */
public class FileUtil {
    public static final int FILE_TYPE_IMAGE = 1;
    public static final int FILE_TYPE_PLATE = 2;
    public static final int FILE_TYPE_SVM_MODEL = 3;
    public static final int FILE_TYPE_ANN_MODEL = 4;

    /**
     * 创建一个文件来保存图片和视频，根据文件样式进行区分保存
     *
     * @param type
     * @return
     */
    public static File getOutputMediaFile(int type) {
        //根据文件类型的不同 创建不同的文件
        File mediaStorageDir = null;
        switch (type) {
            case FILE_TYPE_IMAGE: {
                mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "PlateRcognizer");
                break;
            }
            case FILE_TYPE_PLATE: {
                mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "PlateRcognizer/PlateRect");
                break;
            }
            case FILE_TYPE_ANN_MODEL:
            case FILE_TYPE_SVM_MODEL: {
                mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                        "PlateRcognizer");
                break;
            }
            default:
                return null;
        }
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("PlateRcognizer", "failed to create directory");
                return null;
            }
        }

        String timeStamp = DateUtil.getDateFormatString(new Date());
        File mediaFile;
        switch (type) {
            case FILE_TYPE_IMAGE: {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + "RPK_" + timeStamp + ".jpg");
                break;
            }
            case FILE_TYPE_PLATE: {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + "RP_" + timeStamp + ".jpg");
                break;
            }
            case FILE_TYPE_ANN_MODEL: {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + "ann.xml");
                break;
            }
            case FILE_TYPE_SVM_MODEL: {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + "svm.xml");
                break;
            }
            default:
                return null;
        }

        return mediaFile;
    }


    /**
     * 获取文件的路径
     *
     * @param type
     * @return
     */
    public static String getMediaFilePath(int type) {
        File mediaStorageDir = null;
        File mediaFile;
        switch (type) {
            case FILE_TYPE_ANN_MODEL: {
                mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                        "PlateRcognizer");
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + "ann.xml");
                break;
            }
            case FILE_TYPE_SVM_MODEL: {
                mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                        "PlateRcognizer");
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + "svm.xml");
                break;
            }
            default:
                return null;
        }
        return mediaFile.getAbsolutePath();
    }
}
