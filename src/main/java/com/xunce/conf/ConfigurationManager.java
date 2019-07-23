package com.xunce.conf;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置管理组件
 * @author lichen
 */
public class ConfigurationManager {

	private static Properties prop = new Properties();

	public static void init(String args){
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream("my.properties");
			prop.load(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(inputStream !=null){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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

	public static void main(String[] args) {
		
	}
}
