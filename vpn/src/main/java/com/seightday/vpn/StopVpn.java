package com.seightday.vpn;


public class StopVpn {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String cmd="rasdial \""+PropertyUtil.get("vpn.name")+"\" /DISCONNECT";
		BatUtil.run(cmd);
		
	}

}
