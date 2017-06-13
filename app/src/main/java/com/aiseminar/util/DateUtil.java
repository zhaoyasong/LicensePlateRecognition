package com.aiseminar.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期的工具类
 * <p>
 * created by song on 2017-06-12.9:45
 */
public class DateUtil {
    /**
     * 将当前的日期 转换成指定的日期格式
     * @param date
     * @return
     */
    public static String getDateFormatString(Date date) {
        String dateString = null;
        if (null != date) {
            SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd_HHmmss");
            dateString = format.format(date);
        }

        return dateString;
    }
}
