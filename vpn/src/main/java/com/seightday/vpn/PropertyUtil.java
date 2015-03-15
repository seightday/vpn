/**
 * 
 */
package com.seightday.vpn;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author heantai
 *
 */
public class PropertyUtil {
	
	
	private final static Logger log = LoggerFactory
			.getLogger(PropertyUtil.class);
	
	private static Properties PROPERTIES;
	
	private static String CLASSPATH;
	
	static{
		//windows
		CLASSPATH = PropertyUtil.class.getClass().getResource("/").getPath().substring(1);
		
		Properties p = new Properties();
		InputStream inputStream =null;
		try {
			inputStream = PropertyUtil.class.getResourceAsStream("/config.properties");
			p.load(new InputStreamReader(inputStream,"UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			 IOUtils.closeQuietly(inputStream);
		 }
		PROPERTIES=p;
		log.info("properties:"+PROPERTIES);
	}
	
	public static String getClassPath(){
		return CLASSPATH;
	}
	
	
	public static String get(String key){
		return PROPERTIES.getProperty(key);
	}
	
	public static Integer getAsInt(String key){
		String property = PROPERTIES.getProperty(key);
		if(property==null){
			return null;
		}
		return new Integer(property);
	}
	
	public static String getSysProp(String key){
		return System.getProperty(key);
	}
	
	public static String getSunJnuEncoding(){
		return getSysProp("sun.jnu.encoding");
	}

}
