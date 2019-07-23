package com.xunce.utils;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * 处理日期时间工具类
 */
public class JodaUtils {

	public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

	public static final String DATE_TIME_MS_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

	public static final String DATE_FORMAT = "yyyy/MM/dd";

	public static final String DATE_FORMAT2 = "yyyyMMdd";

	public static final String DATE_FORMAT_YYMMDD = "yyMMdd";

	public static final String TIME_FORMAT = "HHmmss";

	public static final String DATE_TIME_FORMAT2 = "yyyyMMddHHmmss";

	public static final String DATE_TIME_FORMAT3 = "yyyyMMddhhmmssSSS";

	public static final String DATE_TIME_FORMAT4 = "yyMMddhhmmssSSS";

	/**
	 * 获取当前系统毫秒数
	 */
	public static long getMillis(){
		return new Instant().getMillis();
	}

	/**
	 * 按照 默认时间格式 获取当前系统时间
	 */
	public static String getCurrentDefaultDateTimeFormat(){
		return DateTimeFormat.forPattern(DEFAULT_DATE_TIME_FORMAT).print(getMillis());

	}

	/**
	 * 按照 传入毫秒数 返回格式化日期时间
	 */
	public static String getDefaultDateTimeFormat(long millis){
		return DateTimeFormat.forPattern(DEFAULT_DATE_TIME_FORMAT).print(millis);
	}


	public static void main(String[] args) throws InterruptedException {
		DateTime dateTime = new DateTime();
		long millis = JodaUtils.getMillis();
		System.out.println(millis);
		Thread.sleep(1000);
		System.out.println(getCurrentDefaultDateTimeFormat());
		System.out.println(getDefaultDateTimeFormat(millis));


	}
}
