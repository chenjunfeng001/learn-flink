package com.xunce.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

/**
 *
 * 处理日期时间工具类
 */
public class JodaUtils {

	private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

	private static final String DATE_TIME_MS_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

	private static final String DATE_FORMAT = "yyyy/MM/dd";

	private static final String DATE_FORMAT2 = "yyyyMMdd";

	private static final Integer DATE_FORMAT2_LENGTH = 8;

	private static final String DATE_FORMAT3 = "yyyy-MM-dd";

	private static final String DATE_FORMAT_YYMMDD = "yyMMdd";

	private static final String TIME_FORMAT = "HHmmss";

	private static final String TIME_FORMAT2 = "HHmm";

	private static final String DATE_TIME_FORMAT2 = "yyyyMMddHHmmss";

	private static final String DATE_TIME_FORMAT3 = "yyyyMMddhhmmssSSS";

	private static final String DATE_TIME_FORMAT4 = "yyMMddhhmmssSSS";

	/**
	 * 获取当前系统毫秒数
	 */
	public static long getMillis() {
		return new Instant().getMillis();
	}

	/**
	 * 按照 默认时间格式 获取当前系统时间 yyyy/MM/dd HH:mm:ss
	 */
	public static String getCurrentDefaultDateTimeFormat() {
		return DateTimeFormat.forPattern(DEFAULT_DATE_TIME_FORMAT).print(getMillis());

	}

	/**
	 * 按照 传入毫秒数 返回格式化日期时间 yyyy/MM/dd HH:mm:ss
	 */
	public static String getDefaultDateTimeFormat(long millis) {
		return DateTimeFormat.forPattern(DEFAULT_DATE_TIME_FORMAT).print(millis);
	}

	/**
	 * 获取当前日期 yyyyMMdd
	 */
	public static String getCurrentDateFormat2() {
		return DateTimeFormat.forPattern(DATE_FORMAT2).print(getMillis());
	}

	/**
	 * 根据字符串日期 yyyy-MM-dd 获取 Date
	 */
	public static Date getDateFormat3(String date) {
		return DateTimeFormat.forPattern(DATE_FORMAT3).parseDateTime(date).toDate();
	}

	/**
	 * 获取两个日期差多少天
	 */
	public static Integer getDaysBetween(String date1, String data2) {
		return Days.daysBetween(DateTime.parse(date1), DateTime.parse(data2)).getDays();
	}

	/**
	 * 根据日期字符串 获取 某个时间加几年 yyyy-MM-dd
	 */
	public static String getDateAddYear(String date, int addYear) {
		return new DateTime(date).plusYears(addYear).toString(DATE_FORMAT3);
	}

	/**
	 * 根据8位 Integer数字 20190912 获取 某个时间加几年后日期字符串 yyyy-MM-dd
	 */

	public static String getDateAddYear(Integer date, int addYear) {
		String dateFormat2 = JodaUtils.getDateFormat2(date);
		return new DateTime(dateFormat2).plusYears(addYear).toString(DATE_FORMAT3);
	}

	/**
	 * 根据日期字符串 获取 某个时间加几个月后日期字符串 yyyy-MM-dd
	 */
	public static String getDateAddMonth(String date, int addMonth) {
		return new DateTime(date).plusMonths(addMonth).toString(DATE_FORMAT3);
	}

	/**
	 * 根据8位 Integer数字 20190912 获取 某个时间加几个月后日期字符串 yyyy-MM-dd
	 */
	public static String getDateAddMonth(Integer date, int addMonth) {
		String dateFormat2 = JodaUtils.getDateFormat2(date);
		return new DateTime(dateFormat2).plusMonths(addMonth).toString(DATE_FORMAT3);
	}

	/**
	 * 根据8位 Integer数字 20190912 获取时间字符串 yyyy-MM-dd
	 */
	public static String getDateFormat2(Integer dateNum) {
		String dateNumStr = String.valueOf(dateNum);
		if (dateNum == null || dateNumStr.length() != DATE_FORMAT2_LENGTH) {
			return "";
		}
		return StringUtils.join(dateNumStr.substring(0, 4), "-", dateNumStr.substring(4, 6), "-", dateNumStr.substring(6, 8));
	}

	/**
	 * 获取当前时间 HHmm
	 */
	public static String getTimeFormat2() {
		return DateTimeFormat.forPattern(TIME_FORMAT2).print(getMillis());
	}

	/**
	 * 比较两个日期大小 yyyy-MM-dd
	 */
	public static Integer compareToDate(String oneDateStr, String otherDateStr) {
		Date oneDate = JodaUtils.getDateFormat3(oneDateStr);
		Date otherDate = JodaUtils.getDateFormat3(otherDateStr);
		return oneDate.compareTo(otherDate);
	}


	public static void main(String[] args) throws InterruptedException {
//        System.out.println(JodaUtils.getDaysBetween("2019-09-12", "2019-09-08"));
//        System.out.println(JodaUtils.getDateFormat2(20181101));
//        System.out.println(JodaUtils.getDateAddYear("2019-09-12", 4));
//        System.out.println(JodaUtils.getDateAddYear(20190912, 4));

		System.out.println(JodaUtils.getDateAddMonth(20190912, 4));
		System.out.println(JodaUtils.compareToDate("2017-09-12", "2012-09-12"));

	}
}
