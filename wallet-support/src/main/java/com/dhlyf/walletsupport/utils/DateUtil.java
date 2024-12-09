package com.dhlyf.walletsupport.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * 日期工具类
 *
 * @author han
 */
public class DateUtil {
    private static Logger logger = LoggerFactory.getLogger(DateUtil.class);
	/*static {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
	}*/

    public static Date getDate() {
        return Calendar.getInstance().getTime();
    }

    /**
     * 判断thisDate 与 thatDate 大小关系
     *
     * @param thisDate
     * @param thatDate
     * @return
     * @author hanzhonghua
     */
    public static boolean greaterThan(Date thisDate, Date thatDate) {
        return compare(thisDate, thatDate) > 0;
    }

    /**
     * 比较两个日期的大小关系
     *
     * @param thisDate
     * @param thatDate
     * @return
     * @author hanzhonghua
     */
    public static int compare(Date thisDate, Date thatDate) {
        if (thisDate.getTime() > thatDate.getTime()) {
            return 1;
        }

        if (thisDate.getTime() == thatDate.getTime()) {
            return 0;
        }

        return -1;
    }

    /**
     * Date 转 String
     *
     * @param date
     * @return
     * @author hanzhonghua
     */
    public static String formatDate(Date date) {
    	if(date==null) {
    		return "";
    	}
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }


    /**
     * Date 转 String(自定义pattern)
     *
     * @param date
     * @return
     * @author hanzhonghua
     */
    public static String formatDate(Date date, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(date);
    }


    /**
     * String 转  Date
     *
     * @param
     * @return
     * @author hanzhonghua
     */
    public static Date parseDate(String dateTime) {
        try {
            return parseDate(dateTime, new String[]{
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-ddHH:mm:ss",
                    "yyyy/MM/dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "yyyy/MM/dd HH:mm",
                    "yyyy-MM-dd",
                    "yyyyMMdd",
                    "yyyy年MM月dd日",
                    "yyyy/MM/dd"});
        } catch (Exception e1) {
            logger.error("日期参数错误，错误信息为：{}", e1.getMessage());
            return null;
        }
    }

    public static Date parseDate(String dateTime, String[] formats) {
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(dateTime);
            } catch (ParseException e) {
                // Continue to next format
            }
        }
        return null;
    }

    public static String getDateTime() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    /**
     * 获取今天零点
     *
     * @return
     * @author hanzhonghua
     */
    public static Date getFirstTimeOfToday() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return df.parse(df.format(new Date()));
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * 获取昨天零点
     *
     * @return
     * @author hanzhonghua
     */
    public static Date getFirstTimeOfYesterday() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date yestoday = new Date(new Date().getTime() - 24 * 3600000l);
        try {
            return df.parse(df.format(yestoday));
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取昨天23点59分59秒
     *
     * @return 昨天的23点59分59秒的Date对象
     * @author hanzhonghua
     */
    public static Date getLastSecondOfYesterday() {
        Calendar calendar = Calendar.getInstance();
        // 设置时间为昨天
        calendar.add(Calendar.DATE, -1);
        // 设置时间为23点59分59秒
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0); // 设置为0毫秒

        return calendar.getTime();
    }

    /**
     * 获取上周日最后的日期   这里设置周一作为一周的第一天
     *
     * @return
     * @author hanzhonghua
     */
    public static Date getLastWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        String lastWeek = formatDate(cal.getTime(), "yyyy-MM-dd " + "23:59:59");
        return parseDate(lastWeek);
    }

    /**
     * 获得前一天
     *
     * @param
     */
    public static String getBeforeDay() {

        Calendar c = Calendar.getInstance();

        c.add(Calendar.DAY_OF_MONTH, -1);

        return formatDate(c.getTime(), "yyyy-MM-dd");

    }

    /**
     * 获取本周日最后的日期  这里设置周一作为一周的第一天
     *
     * @return
     * @author hanzhonghua
     */
    public static Date getWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.add(Calendar.WEEK_OF_YEAR, 0);
        String week = formatDate(cal.getTime(), "yyyy-MM-dd " + "23:59:59");
        return parseDate(week);
    }

    /**
     * 获取本周开始时间（周一0点0分0秒）
     * @return 本周开始时间
     */
    public static Date getStartOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取本周结束时间（周日23点59分59秒999毫秒）
     * @return 本周结束时间
     */
    public static Date getEndOfCurrentWeek() {
        Calendar cal = Calendar.getInstance();
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if (Calendar.SUNDAY == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);//最后一天周六
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_WEEK, 1);//加一天得到周天
        return cal.getTime();
    }

    /**
     * @return
     * @author hanzhonghua
     * 获取上个自然月的最后日期
     */
    public static Date getLastMonth() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        //得到一个月最后一天日期(31/30/29/28)
        int MaxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), MaxDay, 23, 59, 59);
        String lastMonth = formatDate(c.getTime());
        return parseDate(lastMonth);
    }

    /**
     * 获取上上个自然月的最后日期
     * @return
     */
    public static Date getLast2Month() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -2);
        //得到一个月最后一天日期(31/30/29/28)
        int MaxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), MaxDay, 23, 59, 59);
        String lastMonth = formatDate(c.getTime());
        return parseDate(lastMonth);
    }

    /**
     * @param
     * @author hanzhonghua
     * 获取本月的最后日期
     */
    public static Date getMonth() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        //得到一个月最后一天日期(31/30/29/28)
        int MaxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), MaxDay, 23, 59, 59);
        String lastMonth = formatDate(c.getTime());
        return parseDate(lastMonth);
    }


    /**
     * @param date1
     * @param date2
     * @return
     * @author hanzhonghua
     * date2比date1多的天数
     */
    public static int differentDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2)   //同一年
        {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)    //闰年
                {
                    timeDistance += 366;
                } else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        } else    //不同年
        {
            return day2 - day1;
        }
    }


    public static boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2
                .get(Calendar.DAY_OF_MONTH);

        return isSameDate;
    }

    public static Date getStartDayOfWeekNo(int year, int weekNo) {
        Calendar cal = getCalendarFormYear(year);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);
        return parseDate(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" +
                cal.get(Calendar.DAY_OF_MONTH));

    }

    public static String getEndDayOfWeekNo(int year, int weekNo) {
        Calendar cal = getCalendarFormYear(year);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);
        cal.add(Calendar.DAY_OF_WEEK, 6);
        return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" +
                cal.get(Calendar.DAY_OF_MONTH);
    }

    public static Calendar getCalendarFormYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.YEAR, year);
        return cal;
    }

    public static Date getHourBeginTime(Date time) {
        return parseDate(formatDate(time, "yyyy-MM-dd HH:00:00"));
    }

    public static Date getDayBeginTime(Date time) {
        return parseDate(formatDate(time, "yyyy-MM-dd 00:00:00"));
    }

    public static Date getDayEndTime(Date time) {
        return parseDate(formatDate(time, "yyyy-MM-dd 23:59:59"));
    }

    public static Date getMonthBeginTime(Date time) {
        return parseDate(formatDate(time, "yyyy-MM-01 00:00:00"));
    }

    public static String getMonthBeginTimeStr(Date time) {
        return formatDate(time, "yyyy-MM-01 00:00:00");
    }

    /**
     * 获取某年第一天日期
     * @param year 年份
     * @return Date
     */
    public static Date getYearFirst(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return  parseDate(formatDate(calendar.getTime(), "yyyy-MM-dd 00:00:00"));
    }
    /**
     * 获取某年最后一天日期
     * @param year 年份
     * @return Date
     */
    public static Date getYearLast(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return  parseDate(formatDate(calendar.getTime(), "yyyy-MM-dd 23:59:59"));
    }

    /**
     * 获得该月最后的时间
     *
     * @return
     */
    public static Date getMonthEndTime(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //获取某月最大天数
        return parseDate(formatDate(cal.getTime(), "yyyy-MM-dd 23:59:59"));
    }

    public static String getMonthEndTimeStr(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //获取某月最大天数
        return formatDate(cal.getTime(), "yyyy-MM-dd 23:59:59");
    }

    public static Date getPrevMinTime(Date time) {
        return parseDate(formatDate(time, "yyyy-MM-dd HH:mm:00"));
    }

    public static Date getWeekBeginTime(Date time) {
//        SimpleDateFormat simdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(time);
//        cal.set(cal.DAY_OF_WEEK, cal.MONDAY);
//        String weekhand = simdf.format(cal.getTime());
//        //System.out.println(weekhand);
//        return parseDate(weekhand);

        String date = formatDate(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int d = 0;
        if(cal.get(Calendar.DAY_OF_WEEK)==1){
            d = -6;
        }else{
            d = 2-cal.get(Calendar.DAY_OF_WEEK);
        }
        cal.add(Calendar.DAY_OF_WEEK, d);
        return cal.getTime();

    }

    public static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static Date getPrevHourTime(Date time) {
        return parseDate(formatDate(time, "yyyy-MM-dd HH:00:00"));
    }

    /**
     * 根据传入的日期，获取相隔小时的日期
     *
     * @param date
     * @param anyHour 可正可负
     * @return
     */
    public static Date getAnyHourByNo(Date date, int anyHour) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, anyHour);
        return c.getTime();
    }


    /**
	 * 获取UTC时间，精确到毫秒
	 * @return
	 */
	public static long getUtcTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		//2、取得时间偏移量：
		return cal.getTimeInMillis();
	}

    /**
     * 获取当月指定日期
     * @param day 具体哪天
     * @return Date
     */
    public static Date getMonthDay(int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, day);
        return  parseDate(formatDate(calendar.getTime(), "yyyy-MM-dd 00:00:00"));
    }



    /**
     * 判断时间是否在时间段内
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }


	public static void main(String[] args) {
        Date d1 = DateUtil.getEndOfCurrentWeek();
        System.out.println(formatDate(d1, "yyyy-MM-dd HH:mm:ss"));
        System.out.println("123");
    }


    /**
     * @param date
     * @param day 想要获取的日期与传入日期的差值 比如想要获取传入日期前四天的日期 day=-4即可
     * @return
     */
    public static Date getSomeDay(Date date, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * 获取小时数
     * @param date
     * @param hours
     * @return
     */
    public static Date addHoursToDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    public static Date addMinutesToDate(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
    　* @description: 两个日期之间相差秒数
    　* @param ${tags} 　
    　* @return ${return_type}
    　* @throws
    　* @author sunshaojun
    　* @date 2019/10/24 18:44
    　*/
    public static int calLastedTime(Date startDate) {
        if (null == startDate){
            return 0;
        }
        long a = System.currentTimeMillis();
        long b = startDate.getTime();
        int c = (int)((b - a) / 1000);
        return c;
    }

    public static Date truncateSecondsAndMilliseconds(Date time) {
        // 创建 Calendar 对象，并设置为给定时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);

        // 将秒和毫秒部分设置为零
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 获取调整后的时间
        return calendar.getTime();
    }

    public static Date getStartOfYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1); // 设置为昨天
        calendar.set(Calendar.HOUR_OF_DAY, 0);   // 设置小时为0
        calendar.set(Calendar.MINUTE, 0);        // 设置分钟为0
        calendar.set(Calendar.SECOND, 0);        // 设置秒为0
        calendar.set(Calendar.MILLISECOND, 0);   // 设置毫秒为0
        return calendar.getTime();
    }

    public static Date getEndOfYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1); // 设置为昨天
        calendar.set(Calendar.HOUR_OF_DAY, 23);  // 设置小时为23
        calendar.set(Calendar.MINUTE, 59);       // 设置分钟为59
        calendar.set(Calendar.SECOND, 59);       // 设置秒为59
        calendar.set(Calendar.MILLISECOND, 999); // 设置毫秒为999
        return calendar.getTime();
    }


    public static Date getFirstTimeOfDay(Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);   // 设置小时为0
        calendar.set(Calendar.MINUTE, 0);        // 设置分钟为0
        calendar.set(Calendar.SECOND, 0);        // 设置秒为0
        calendar.set(Calendar.MILLISECOND, 0);   // 设置毫秒为0
        return calendar.getTime();
    }
}

