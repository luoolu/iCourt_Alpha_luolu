package com.icourt.alpha.utils;

import android.os.SystemClock;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static cn.finalteam.toolsfinal.DateUtils.date;

public class DateUtils {

    /**
     * 获取聊天的时间格式化 简写版
     *
     * @param milliseconds
     * @return
     */
    public static String getFormatChatTimeSimple(long milliseconds) {
        String dataString;
        String timeStringBy24;

        Date currentTime = new Date(milliseconds);
        Date today = new Date();
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        Date todaybegin = todayStart.getTime();
        Date yesterdaybegin = new Date(todaybegin.getTime() - 3600 * 24 * 1000);
        Date preyesterday = new Date(yesterdaybegin.getTime() - 3600 * 24 * 1000);

        SimpleDateFormat timeformatter24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeStringBy24 = timeformatter24.format(currentTime);
        if (!currentTime.before(todaybegin)) {
            dataString = timeStringBy24;
        } else if (!currentTime.before(yesterdaybegin)) {
            dataString = "昨天";
        } else if (!currentTime.before(preyesterday)) {
            dataString = "前天";
        } else if (isSameWeekDates(currentTime, today)) {
            dataString = getWeekOfDate(currentTime);
        } else {
            SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dataString = dateformatter.format(currentTime);
        }
        return dataString;
    }

    /**
     * 获取聊天的时间格式化
     *
     * @param milliseconds
     * @return
     */
    public static String getFormatChatTime(long milliseconds) {
        String dataString;
        String timeStringBy24;

        Date currentTime = new Date(milliseconds);
        Date today = new Date();
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        Date todaybegin = todayStart.getTime();
        Date yesterdaybegin = new Date(todaybegin.getTime() - 3600 * 24 * 1000);
        Date preyesterday = new Date(yesterdaybegin.getTime() - 3600 * 24 * 1000);

        SimpleDateFormat timeformatter24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeStringBy24 = timeformatter24.format(currentTime);
        if (!currentTime.before(todaybegin)) {
            dataString = timeStringBy24;
        } else if (!currentTime.before(yesterdaybegin)) {
            dataString = "昨天 " + timeStringBy24;
        } else if (!currentTime.before(preyesterday)) {
            dataString = "前天 " + timeStringBy24;
        } else if (isSameWeekDates(currentTime, today)) {
            dataString = getWeekOfDate(currentTime) + " " + timeStringBy24;
        } else {
            SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            dataString = dateformatter.format(currentTime);
        }
        return dataString;
    }

    /**
     * 注意:别轻易修改
     * 文档地址:http://wiki.alphalawyer.cn/pages/viewpage.action?pageId=1773098
     * 获取标准的时间格式化:
     * 对近期时间点敏感、显示区域有限
     * 1:  t < 60 分钟：x分钟前（x = 1～59）
     * 2:  1 小时 ≤ t < 24 小时：x小时前（x = 1～23）
     * 3:  24 小时 ≤ t ≤ 前一天零点：昨天
     * 4:  前一天零点 < t ≤ 24*5 小时：x天前（x = 2～5）
     * 5:  t > 24*5 小时：yyyy-mm-dd
     *
     * @param milliseconds
     * @return
     */
    public static final String getStandardSimpleFormatTime(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        if (isOverToday(milliseconds)) {//1.未来
            sdf.applyPattern("yyyy-MM-dd: hh:mm");
            return sdf.format(milliseconds);
        } else if (isToday(milliseconds)) {//2.今天
            long distanceMilliseconds = System.currentTimeMillis() - milliseconds;
            if (distanceMilliseconds < TimeUnit.HOURS.toMillis(1)) {//3.x分钟前
                long distanceSeconds = TimeUnit.MILLISECONDS.toMinutes(distanceMilliseconds);
                if (distanceSeconds == 0) {
                    return "刚刚";
                } else {
                    return String.format("%s分钟前", TimeUnit.MILLISECONDS.toMinutes(distanceMilliseconds));
                }
            } else {//4.x小时前
                return String.format("%s小时前", TimeUnit.MILLISECONDS.toHours(distanceMilliseconds));
            }
        } else if (isYesterday(milliseconds)) {
            return "昨天";//5.昨天
        } else {
            long distanceMilliseconds = System.currentTimeMillis() - milliseconds;
            long distanceDay = TimeUnit.MILLISECONDS.toDays(distanceMilliseconds);
            if (distanceDay <= 5) {//x天前（x = 2～5）
                return String.format("%s天前", distanceDay);
            } else {//yyyy-mm-dd
                sdf.applyPattern("yyyy-MM-dd");
                return sdf.format(milliseconds);
            }
        }
    }

    /**
     * 注意:别轻易修改
     * 文档地址:http://wiki.alphalawyer.cn/pages/viewpage.action?pageId=1773098
     * 获取标准的时间格式化:
     * <p>
     * 对近期时间点敏感、对具体时间点要求高、显示区域充裕
     * 通常不带有社交属性
     * <p>
     * 1. t < 60 分钟：x分钟前（x = 1～59）
     * 2. 1 小时 ≤ t < 24 小时：x小时前（x = 1～23）
     * 3. 24 小时 ≤ t ≤ 前一天零点：昨天 + hh:mm
     * 4. t > 前一天零点：yyyy-mm-dd + hh:mm
     *
     * @param milliseconds
     * @return
     */
    public static final String getStandardFormatTime(long milliseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        if (isOverToday(milliseconds)) {//1.未来
            sdf.applyPattern("yyyy-MM-dd: hh:mm");
            return sdf.format(milliseconds);
        } else if (isToday(milliseconds)) {//2.今天
            long distanceMilliseconds = System.currentTimeMillis() - milliseconds;
            if (distanceMilliseconds < TimeUnit.HOURS.toMillis(1)) {//3.x分钟前
                long distanceSeconds = TimeUnit.MILLISECONDS.toMinutes(distanceMilliseconds);
                if (distanceSeconds == 0) {
                    return "刚刚";
                } else {
                    return String.format("%s分钟前", TimeUnit.MILLISECONDS.toMinutes(distanceMilliseconds));
                }
            } else {//4.x小时前
                return String.format("%s小时前", TimeUnit.MILLISECONDS.toHours(distanceMilliseconds));
            }
        } else if (isYesterday(milliseconds)) {
            sdf.applyPattern("昨天 hh:mm");
            return sdf.format(milliseconds);
        } else {
            sdf.applyPattern("yyyy-MM-dd hh:mm");
            return sdf.format(milliseconds);
        }
    }

    /**
     * 判断两个日期是否在同一周
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeekDates(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (0 == subYear) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        } else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
            // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        } else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        }
        return false;
    }

    /**
     * 根据不同时间段，显示不同时间
     *
     * @param date
     * @return
     */
    @Deprecated
    public static String getTodayTimeBucket(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat timeformatter0to11 = new SimpleDateFormat("KK:mm", Locale.getDefault());
        SimpleDateFormat timeformatter1to12 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 5) {
            return "凌晨 " + timeformatter0to11.format(date);
        } else if (hour >= 5 && hour < 12) {
            return "上午 " + timeformatter0to11.format(date);
        } else if (hour >= 12 && hour < 18) {
            return "下午 " + timeformatter1to12.format(date);
        } else if (hour >= 18 && hour < 24) {
            return "晚上 " + timeformatter1to12.format(date);
        }
        return "";
    }

    /**
     * 根据日期获得星期
     *
     * @param date
     * @return
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDaysName = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        // String[] weekDaysCode = { "0", "1", "2", "3", "4", "5", "6" };
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return weekDaysName[intWeek];
    }

    /**
     * 根据日期获得星期
     *
     * @param millis
     * @return
     */
    public static String getWeekOfDateFromZ(long millis) {
        String[] weekDaysName = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return weekDaysName[intWeek];
    }

    /**
     * 获得当前时间的毫秒数
     * <p>
     * 详见{@link System#currentTimeMillis()}
     *
     * @return
     */
    public static long millis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取时长 00:11
     * bug
     *
     * @param milliseconds
     * @return
     */
    @Deprecated
    public static String getTimeDurationDate(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(milliseconds);
    }


    /**
     * 获取时长 00:11
     *
     * @param milliseconds
     * @return
     */
    public static String getHHmmss(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(milliseconds);
    }

    /**
     * 获取时长 00:11
     *
     * @param milliseconds
     * @return
     */
    public static String getHHmm(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(milliseconds);
    }

    /**
     * 获取日期 yyyy年MM月dd日
     *
     * @param milliseconds
     * @return
     */
    public static String getTimeDateFormatYear(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        return formatter.format(milliseconds);
    }

    /**
     * 获取日期 yyyy.MM.dd
     *
     * @param milliseconds
     * @return
     */
    public static String getTimeDateFormatYearDot(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        return formatter.format(milliseconds);
    }

    /**
     * 获取日期 MM月dd日
     *
     * @param milliseconds
     * @return
     */
    public static String getTimeDate(long milliseconds) {
        String formatStr = null;
        if (isThisYear(milliseconds)) {
            formatStr = "MM月dd日";
        } else {
            formatStr = "yyyy年MM月dd日";
        }
        if (!TextUtils.isEmpty(formatStr)) {
            SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
            return formatter.format(milliseconds);
        }
        return "";
    }

    /**
     * 获取日期 MM月dd日 HH:mm
     *
     * @param milliseconds
     * @return
     */
    public static String getTimeDateFormatMm(long milliseconds) {
        String formatStr = null;
        if (isThisYear(milliseconds)) {
            formatStr = "MM月dd日 HH:mm";
        } else {
            formatStr = "yyyy年MM月dd日 HH:mm";
        }
        if (!TextUtils.isEmpty(formatStr)) {
            SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
            return formatter.format(milliseconds);
        }
        return "";
    }

    /**
     * @param milliseconds
     * @return
     */
    public static String getyyyyMMdd(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        return formatter.format(milliseconds);
    }

    /**
     * yyyy-MM-dd 格式
     *
     * @param milliseconds
     * @return
     */
    public static String getyyyy_MM_dd(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(milliseconds);
    }

    /**
     * @param milliseconds
     * @return
     */
    public static String getyyyyMMddHHmm(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return formatter.format(milliseconds);
    }

    /**
     * yyyy年MM月dd日 HH:mm
     *
     * @param milliseconds
     * @return
     */
    public static String getyyyy_YEAR_MM_MONTH_dd_DAY_HHmm(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        return formatter.format(milliseconds);
    }

    /**
     * 获取日期 MM/dd HH:mm
     *
     * @param milliseconds
     * @return
     */
    public static String getTimeDateFormatXMm(long milliseconds) {
        String formatStr = null;
        if (isThisYear(milliseconds)) {
            formatStr = "MM/dd HH:mm";
        } else {
            formatStr = "yyyy/MM/dd HH:mm";
        }
        if (!TextUtils.isEmpty(formatStr)) {
            SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
            return formatter.format(milliseconds);
        }
        return "";
    }

    /**
     * @param milliseconds
     * @return
     */
    public static String getMMMdd(long milliseconds) {
        String formatStr = null;
        if (isThisYear(milliseconds)) {
            formatStr = "MM月dd日";
        } else {
            formatStr = "yyyy年MM月dd日";
        }
        if (!TextUtils.isEmpty(formatStr)) {
            SimpleDateFormat formatter = new SimpleDateFormat(formatStr, Locale.CHINA);
            try {
                return formatter.format(milliseconds);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return String.valueOf(milliseconds);
    }

    /**
     * @param milliseconds
     * @return
     */
    public static String getMMXdd(long milliseconds) {
        String formatStr = null;
        if (isThisYear(milliseconds)) {
            formatStr = "MM/dd";
        } else {
            formatStr = "yyyy/MM/dd";
        }
        if (!TextUtils.isEmpty(formatStr)) {
            SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
            try {
                return formatter.format(milliseconds);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return String.valueOf(milliseconds);
    }

    /**
     * 获得天数差
     *
     * @param begin
     * @param end
     * @return
     */
    public static long getDayDiff(long begin, long end) {
        long day = 1;
        if (end < begin) {
            day = -1;
        } else if (end == begin) {
            day = 0;
        } else {
            day += (end - begin) / (24 * 60 * 60 * 1000);
        }
        return day;
    }

    /**
     * 获取今天开始时间 毫秒
     *
     * @return
     */
    public static long getTodayStartTime() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime().getTime();
    }

    /**
     * 获取今天开始时间 毫秒
     *
     * @return
     */
    public static long getTodayEndTime() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.set(Calendar.HOUR_OF_DAY, 23);
        currentDate.set(Calendar.MINUTE, 59);
        currentDate.set(Calendar.SECOND, 59);
        return currentDate.getTime().getTime();
    }

    /**
     * 获取本周的开始时间 毫秒
     *
     * @return
     */
    public static long getCurrWeekStartTime() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+:08:00"));
        currentDate.setFirstDayOfWeek(Calendar.MONDAY);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return currentDate.getTimeInMillis();
    }


    /* 获取本周的开始时间 毫秒
     * @return
     */
    public static long getCurrWeekEndTime() {
        Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+:08:00"));
        // currentDate.setFirstDayOfWeek(Calendar.SUNDAY);
        currentDate.setFirstDayOfWeek(Calendar.MONDAY);
        currentDate.set(Calendar.HOUR_OF_DAY, 23);
        currentDate.set(Calendar.MINUTE, 59);
        currentDate.set(Calendar.SECOND, 59);
        currentDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return currentDate.getTime().getTime();
    }


    public static boolean isToday(long millis) {
        Calendar current = Calendar.getInstance();
        Calendar todayStart = Calendar.getInstance();    //今天
        todayStart.set(Calendar.YEAR, current.get(Calendar.YEAR));
        todayStart.set(Calendar.MONTH, current.get(Calendar.MONTH));
        todayStart.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);

        Calendar todayEnd = Calendar.getInstance();    //今天
        todayEnd.set(Calendar.YEAR, current.get(Calendar.YEAR));
        todayEnd.set(Calendar.MONTH, current.get(Calendar.MONTH));
        todayEnd.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);

        return millis / 1000 >= todayStart.getTimeInMillis() / 1000
                && millis / 1000 <= todayEnd.getTimeInMillis() / 1000;
    }

    /**
     * 是明天
     *
     * @param millis
     * @return
     */
    public static boolean isOverToday(long millis) {
        Calendar todayEnd = Calendar.getInstance();    //今天
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        return millis / 1000 >= todayEnd.getTimeInMillis() / 1000;
    }


    /**
     * @param millis 毫秒
     * @return
     */
    public static boolean isYesterday(long millis) {
        Calendar today = Calendar.getInstance();    //今天
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        Calendar yesterday = Calendar.getInstance();    //昨天
        yesterday.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH) - 1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        return millis / 1000 < today.getTimeInMillis() / 1000
                && millis / 1000 >= yesterday.getTimeInMillis() / 1000;
    }

    /**
     * 判断是否是今年
     *
     * @param millis
     * @return
     */
    public static boolean isThisYear(long millis) {
        Calendar otherYear = Calendar.getInstance();
        otherYear.setTimeInMillis(millis);
        Calendar thisYear = Calendar.getInstance();
        thisYear.setTimeInMillis(millis());

        return otherYear.get(Calendar.YEAR) == thisYear.get(Calendar.YEAR);
    }

    /**
     * 23:59:59 不显示  xx月xx日 hh：mm
     *
     * @param millis
     * @return
     */
    public static String get23Hour59Min(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        if ((hour == 23 && minute == 59 && second == 59)) {
            return getMMMdd(millis);
        } else {
            return getTimeDateFormatMm(millis);
        }
    }

    /**
     * 23:59:59 不显示 xx/xx hh：mm
     *
     * @param millis
     * @return
     */
    public static String get23Hour59MinFormat(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        if ((hour == 23 && minute == 59 && second == 59)) {
            return getMMXdd(millis);
        } else {
            return getTimeDateFormatXMm(millis);
        }
    }

    /**
     * 根据小时：分钟 获取时间戳
     *
     * @param hour
     * @param min
     * @return
     */
    public static long getMillByHourmin(int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
