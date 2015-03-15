/**
 * 
 */
package com.seightday.vpn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author heantai
 *
 */
public class BatUtil {
	
	
	private final static Logger log = LoggerFactory.getLogger(BatUtil.class);
	
	public static List<String> run(String cmd) throws Exception{
		log.info("cmd:"+cmd);
		
		Process process = Runtime.getRuntime().exec(cmd);
		
		BufferedReader   bufferedReader   =   new   BufferedReader( 
				new   InputStreamReader(process.getInputStream(),PropertyUtil.getSunJnuEncoding()));   
		String str = null;
		List<String> list=new ArrayList<String>();
		while ((str = bufferedReader.readLine()) != null) {
			//不输出，cmd未执行
			log.info(str);
			list.add(str);
		}
		
		if(!list.isEmpty()){
			return list;
		}
		
		bufferedReader   =   new   BufferedReader( 
				new   InputStreamReader(process.getErrorStream(),PropertyUtil.getSunJnuEncoding()));   
		str = null;
		while ((str = bufferedReader.readLine()) != null) {
			log.info(str);
			list.add(str);
		}
		
		return list;
		
	}
	

}
