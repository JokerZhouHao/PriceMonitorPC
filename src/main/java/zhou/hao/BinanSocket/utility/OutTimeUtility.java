package zhou.hao.BinanSocket.utility;

import java.util.Date;

public class OutTimeUtility {
	private final static long MAX_CONNECT_OUT_TIME = 1000 * 60 * 10;
	
	private long timeStartConnect = 0; 
	
	public void resetConnectTime() {
		timeStartConnect = 0;
	}
	
	public Boolean connectOverTime(long nowTime) {
		if(-1 == timeStartConnect)	return Boolean.FALSE;
		else if(0==timeStartConnect)	timeStartConnect = nowTime;
		else if((nowTime - timeStartConnect) >= MAX_CONNECT_OUT_TIME) {
			timeStartConnect = -1; // 发送一次邮件后就不要再发送了
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
