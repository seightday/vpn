package com.seightday.vpn;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author seightday@gmail.com
 *
 */
public class Main {
	
	private final static Logger log = LoggerFactory.getLogger(Main.class);
	
	private final static VPN vpn=new VPN();
	
	private static String gateWay;
	
	public static void main(String[] args) throws Exception {
		
		
		//route -f清空所有路由
		
		
		
		if(!startVpn()){
			return;
		}
		
//		setDns();//手动设置
		
		
		initVpnIpDns();

		gateWay = getGateWay();
		if(gateWay.contains("在链路上")){
			log.error("网关错误，请检查是否使用了默认网关");
			return;
		}
		
		proxyDns();
		
		List<String> domains = getDomains();
		
		for (String d : domains) {
			for (int i = 0; i < 1; i++) {//尽可能多的查找ip进行代理
				proxyDomain(d);
			}
		}
		
	}
	

	private static void setDns() throws Exception {
		//vpn设置dns只有启动后设置才能设置，否则报错：文件名、目录名或卷标语法不正确；命令显示成功，但实际无效，手动进行设置
		BatUtil.run("netsh interface ip set dns \""+PropertyUtil.get("vpn.name")+"\" static "+firstDns);
		BatUtil.run("netsh interface ip add dns \""+PropertyUtil.get("vpn.name")+"\" "+sencondDns);
		
		//本地连接可设置成功，若连接为公司内网，可能影响内网服务的使用
		BatUtil.run("netsh interface ip set dns \""+PropertyUtil.get("local.net.name")+"\" static 8.8.8.8");
		BatUtil.run("netsh interface ip add dns \""+PropertyUtil.get("local.net.name")+"\" 223.5.5.5");
	}
	
	private static String firstDns=PropertyUtil.get("vpn.first.dns");
	private static String sencondDns=PropertyUtil.get("vpn.second.dns");
	
	//设置dns
	private static void proxyDns() throws Exception {
		addRoute(firstDns,true);
		addRoute(sencondDns,true);
	}

	private static void proxyDomain(String d) throws Exception {
		String cmd="nslookup "+d+" "+firstDns;
	
		List<String> list =BatUtil.run(cmd);
		
		if(list==null||list.isEmpty()){
			log.info("查找ip失败");
			return;
		}
		
		boolean containDomain=false;
		for (String s : list) {
			if(s.contains(d)){
				containDomain=true;
				break;
			}
		}
		
		if(!containDomain){
			log.info("查找ip失败");
			return;
		}
		
		
		int idx=0;
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			if(str.contains("名称")){
				idx=i;
				break;
			}
		}
		
		for (int i = idx+1; i < list.size(); i++) {
			String str = list.get(i);
			
			
			if(str.contains("Aliases")){
				continue;
			}
			
			if(str.contains("Addresses:")){
				str=str.replace("Addresses:", "");
			}else if(str.contains("Addresse:")){
				str=str.replace("Addresse:", "");
			}
			str=str.trim();
			
			if(str.isEmpty()){
				continue;
			}
			
			//手动将vpn中ipv6协议的默认网关取消
			if(str.contains(":")){//不处理ipv6 TODO
				continue;
			}
			
			addRoute(str,"true".equals(PropertyUtil.get("route.permanent")));
		}
		
	}

	private static void addRoute(String ip,boolean isPermanent) throws Exception {
		String cmd = "route add "+ip+" mask 255.255.255.255 "+gateWay+(isPermanent?" -p":"");//永久，无-p命令重启电脑后被清空
		BatUtil.run(cmd);
	}

	public static void initVpnIpDns() throws Exception {
		List<String> list=BatUtil.run("ipconfig /all");
		for (int i = 0; i < list.size(); i++) {
			String s=list.get(i);
			if(s==null){
				continue;
			}
			
			if(!s.endsWith(PropertyUtil.get("vpn.name"))){
				continue;
			}
			
			String _ip=list.get(i+4);
			_ip = _ip.split(":")[1].trim();
			if(_ip.contains("(")){
				_ip=_ip.substring(0, _ip.indexOf("("));
			}
			String _dns=list.get(i+4+3);
			_dns=_dns.split(":")[1].trim();
			log.info("_ip:{},_dns:{}",_ip,_dns);
			
			vpn.ip=_ip;
			vpn.dns=_dns;
			return;
		}
		
	}
	
	public static String getGateWay() throws Exception{
		String cmd="route print";
		String gateway=null;
		List<String> list = BatUtil.run(cmd);
		int idx=0;
		for (int i = 0; i < list.size(); i++) {
			String s = list.get(i);
			if(s.contains("IPv4 路由表")){
				idx=i;
				break;
			}
		}
		
		for (int i = idx; i < list.size(); i++) {
			String s = list.get(i);
			if(s.contains(vpn.ip)){
				log.info("s:"+s);
				String[] strings = s.trim().replaceAll(" +"," ").split(" ");
				gateway= strings[2];
				break;
			}
		}
		
		log.info("gateway:{}",gateway);
		
		return gateway;
	}
	
	public final static String domainsFile="domains.txt";
	
	public static List<String> getDomains() {
		try {
			List<String> lines = FileUtils.readLines(new File(PropertyUtil.getClassPath()+domainsFile));
			log.info("domains:"+lines);
			return lines;
		} catch (IOException e) {
			log.error(null, e);
			return null;
		}
	}
	
	public static boolean startVpn() throws Exception {
		log.info("开始连接vpn");
		
		String cmd="rasdial \""+PropertyUtil.get("vpn.name")+"\" "+PropertyUtil.get("vpn.user")+" "+PropertyUtil.get("vpn.pwd");
		
		List<String> list = BatUtil.run(cmd);
		for (String s : list) {
			if (s == null) {
				continue;
			}

			if (s.contains("已连接") || s.contains("已经连接")) {
				log.info("vpn已连接");
				return true;
			}

			if (s.contains("远程访问错误")) {
				log.info("vpn连接失败");
				return false;
			}
		}
		
		return false;
	}
	
}
