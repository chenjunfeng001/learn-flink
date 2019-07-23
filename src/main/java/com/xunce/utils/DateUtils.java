package com.xunce.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 * 
 * @author guanbo
 */
public class DateUtils {
	private static DateUtils dateForamtUtils;
	
	private DateUtils(){
		
	}

	/**
	 * 使用 synchronized 修饰，临界资源的同步互斥访问
	 * 
	 * @return
	 */
	public static synchronized DateUtils getInstance() {
		if (dateForamtUtils == null) {
			dateForamtUtils = new DateUtils();
		}
		return dateForamtUtils;
	}

	/**
	 * 8位数字转时间
	 * 
	 * @param in
	 * @return yyyy-MM-dd
	 */
	public  synchronized String getDateIntegerToString(Integer in) {
		if (in == null)
			return null;

		String res = String.valueOf(in);
		if (res.length() != 8)
			return null;

		res = res.substring(0, 4) + "-" + res.substring(4, 6) + "-" + res.substring(6, 8);
		return res;
	}

	/**
	 * 8位数字转时间
	 * 
	 * @param in
	 * @return yyyy-MM-dd
	 * @throws ParseException
	 */
	public  synchronized Date getStringToDate(String in) throws ParseException {
		if (in == null)
			return null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.parse(in);
	}

	/**
	 * 获取当前时间
	 *
	 * @param
	 * @return yyyy-MM-dd
	 */
	public  synchronized String getCurrentDateToString() {
		return getDateIntegerToString(20090109);
	}

	public  synchronized String getCurrentDateToString2() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
//	/**
//	 * 获取当前时间
//	 *
//	 * @param
//	 * @return yyyy-MM-dd
//	 */
//	public static synchronized String getCurrentDateToString() {
//		Date date = new Date();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		try {
//			return sdf.format(date);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	/**
	 * 两个日期差多少天
	 * 
	 * @param anotherDate1
	 * @param anotherDate2
	 * @return
	 * @throws ParseException
	 */
	public  synchronized Long getCurrentDateDiffToAnotherDate(String anotherDate1, String anotherDate2)
			throws ParseException {
		if (anotherDate1 == null || anotherDate2 == null)
			return null;

		long nd = 1000 * 24 * 60 * 60;

		long diff = getStringToDate(anotherDate1).getTime() - getStringToDate(anotherDate2).getTime();

		// 注意结束时间 - 开始时间，不包括开始时间，即 2009-01-09 - 2009-01-09,结果为0
		return diff / nd ;
	}

	/**
	 * 给某个时间加几年
	 * 
	 * @param anotherDate
	 * @param addyear
	 * @return yyyy-MM-dd
	 * @throws ParseException
	 */
	public  synchronized String getDateAddYear(String anotherDate, int addyear) throws ParseException {
		if (anotherDate == null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar c = Calendar.getInstance();
		c.setTime(getStringToDate(anotherDate));
		c.add(Calendar.YEAR, addyear);

		return sdf.format(c.getTime());
	}

	/**
	 * 给某个时间加几个月
	 *
	 * @param anotherDate
	 * @param addMonth
	 * @return yyyy-MM-dd
	 * @throws ParseException
	 */
	public  synchronized String getDateAddMonth(String anotherDate, int addMonth) throws ParseException {
		if (anotherDate == null)
			return null;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar c = Calendar.getInstance();
		c.setTime(getStringToDate(anotherDate));
		c.add(Calendar.MONTH, addMonth);

		return sdf.format(c.getTime());
	}

	/**
	 * 比较两个时间大小
	 *
	 * @param anotherDate1
	 * @param anotherDate2
	 * @return yyyy-MM-dd
	 * @throws ParseException
	 */
	public  synchronized Integer getCompareToDate(String anotherDate1, String anotherDate2)
			throws ParseException {

		if (anotherDate1 == null || anotherDate2 == null)
			return null;

		Calendar c1 = Calendar.getInstance();
		c1.setTime(getStringToDate(anotherDate1));
		Calendar c2 = Calendar.getInstance();
		c2.setTime(getStringToDate(anotherDate2));

		return c1.compareTo(c2);

	}

	public static Integer getRemainSecondsOneDay(Date currentDate) {

		LocalDateTime midnight = LocalDateTime.ofInstant(currentDate.toInstant(),
				ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0)
				.withSecond(0).withNano(0);
		LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentDate.toInstant(),
				ZoneId.systemDefault());
		long seconds = ChronoUnit.SECONDS.between(currentDateTime, midnight);
		return (int) seconds;
	}

	public static void main(String[] args) throws ParseException {
		// DateUtils aa = DateUtils.getDateForamtUtils();
		// System.out.println(aa.DateUtils("2019-05-25")+"");
		//

		// Date date=new Date();
		// SimpleDateFormat s=new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
		// System.out.println(s.format(date));
		//
		// Calendar c=Calendar.getInstance();
		// c.setTime(date);
		// c.add(Calendar.MONTH, -6);
		//
		// System.out.println(s.format(c.getTime()));

		//
		// Calendar c1=Calendar.getInstance();
		// c1.setTime(getStringToDate("2019-05-22"));
		// Calendar c2=Calendar.getInstance();
		// c2.setTime(getStringToDate("2019-05-23"));
//		System.out.println(DateUtils.getCurrentDateDiffToAnotherDate(null, null));
//
//		System.out.println(Math.ceil(1.2));
//		 System.out.println(DateUtils.getInstance().getDateAddMonth("2006-05-11", 90));
//
//		String s = "3";
//
//		System.out.println(DateUtils.getInstance().getCurrentDateToString());
//		Integer remainSecondsOneDay = DateUtils.getRemainSecondsOneDay(new Date());
//		System.out.println(remainSecondsOneDay);

		//不把 2009-01-9 算在内,从1月10号开始计算
//		2012-06-14  2011-12-14		183
//		2011-12-14 2011-6-14		183
//		2011-6-14  2010-12-14 		182
//		2010-12-14 2010-6-14		183
//		2010-6-14  2009-12-14		182
//		2009-12-14 2009-6-14		183
//		2009-6-14  2008-12-14   2009-01-09  156
		System.out.println(DateUtils.getInstance().getCurrentDateDiffToAnotherDate("2012-06-14", "2011-12-14"));
		System.out.println(DateUtils.getInstance().getCurrentDateDiffToAnotherDate("2011-12-14", "2011-6-14"));
		System.out.println(DateUtils.getInstance().getCurrentDateDiffToAnotherDate("2011-6-14", "2010-12-14"));
		System.out.println(DateUtils.getInstance().getCurrentDateDiffToAnotherDate("2010-12-14", "2010-6-14"));
		System.out.println(DateUtils.getInstance().getCurrentDateDiffToAnotherDate("2010-6-14", "2009-12-14"));
		System.out.println(DateUtils.getInstance().getCurrentDateDiffToAnotherDate("2009-12-14", "2009-6-14"));
		System.out.println(DateUtils.getInstance().getCurrentDateDiffToAnotherDate("2009-6-14", "2009-01-09"));
	}
}
