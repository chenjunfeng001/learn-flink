package com.xunce.conf;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;

/**
 * 配置管理组件
 * @author lichen
 */
public class ConfigurationManager {

	private static Properties prop = new Properties();

	static {
		try {

			InputStream in = ConfigurationManager.class
					.getClassLoader()
					.getResourceAsStream("my.properties");
			prop.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取指定key对应的value
	 *
	 */
	public static String getProperty(String key) {
		final String property = prop.getProperty(key);
		if(StringUtils.isBlank(property)){
			return "";
		}
		return property;
	}

	/**
	 * 获取整数类型的配置项
	 */
	public static Integer getInteger(String key) {
		String value = getProperty(key);
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取布尔类型的配置项
	 */
	public static Boolean getBoolean(String key) {
		String value = getProperty(key);
		try {
			return Boolean.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取Long类型的配置项
	 */
	public static Long getLong(String key) {
		String value = getProperty(key);
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0L;
	}
}
