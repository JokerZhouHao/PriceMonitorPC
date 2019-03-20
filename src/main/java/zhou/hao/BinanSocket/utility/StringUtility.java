package zhou.hao.BinanSocket.utility;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import zhou.hao.BinanSocket.client.security.HmacSHA256Signer;

public class StringUtility {
	private static DecimalFormat dfPrice = new DecimalFormat("##.########");
	private static DecimalFormat dfQuantity = new DecimalFormat("##.########");
	private static DecimalFormat dfRate = new DecimalFormat("###.####");
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String formatPrice(double price) {
		return dfPrice.format(price);
	}
	
	public static String formatRate(double rate) {
		return dfRate.format(rate);
	}
	
	public static String formatDate(Date date){
        if(sdf==null)   sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String getLogDate(){
        return "[" + formatDate(new Date()) + "]";
    }
	
	/**
	 * uri：最近一天的k线数据
	 * @param item
	 * @return
	 */
	public static String getUri1DKline(String item) {
		return Global.API_BASE_URL + "/api/v1/klines?symbol=" + item + "&interval=1d&limit=1";
	}
	
	/**
	 * uri：账户信息
	 * @return
	 */
	public static String getUriAccount() {
		String reqStr = "timestamp=" + String.valueOf(new Date().getTime());
		return Global.API_BASE_URL + "/api/v3/account?" + reqStr + "&" + "signature=" + 
				HmacSHA256Signer.sign(reqStr, Global.SECRET_KEY);
	}
	
	/**
	 * uri：当前的所有订单
	 * @return
	 */
	public static String getUriOpenOrders() {
		String reqStr = "timestamp=" + String.valueOf(new Date().getTime());
		return Global.API_BASE_URL + "/api/v3/openOrders?" + reqStr + "&" + "signature=" + 
			HmacSHA256Signer.sign(reqStr, Global.SECRET_KEY);
	}
	
	/**
	 * uri：删除指定订单
	 * @param symbol
	 * @param orderId
	 * @return
	 */
	public static String getUriDeleteOrders(String symbol, String orderId) {
		String reqStr = "symbol=" + symbol + "&orderId=" + orderId + "&timestamp=" + String.valueOf(new Date().getTime());
		return Global.API_BASE_URL + "/api/v3/order?" + reqStr + "&" + "signature=" + 
			HmacSHA256Signer.sign(reqStr, Global.SECRET_KEY);
	}
	
	public static String getUriSendOrder(String symbol, String side, double quantity, double price) {
		String reqStr = "symbol=" + symbol + "&side=" + side + "&type=LIMIT&timeInForce=GTC" +
						"&quantity=" + dfQuantity.format(quantity) + "&price=" + dfPrice.format(price) +
						"&timestamp=" + String.valueOf(new Date().getTime());
		return Global.API_BASE_URL + "/api/v3/order?" + reqStr + "&" + "signature=" + 
			HmacSHA256Signer.sign(reqStr, Global.SECRET_KEY);
	}
	
	public static String getMainCoin(String symbol, String baseCoin) {
		return symbol.substring(0, symbol.indexOf(baseCoin));
	}
	
	public static String getExceptionStackInfo(Exception e) {
		StringBuffer sb = new StringBuffer();
		StackTraceElement[] eles = e.getStackTrace();
		int i = 0;
		if(eles.length > 5) {
			sb.append(eles[0].toString() + "\n");
			for(i=eles.length - 4; i < eles.length; i++)
				sb.append(eles[i].toString() + "\n");
		} else {
			for(StackTraceElement ele : e.getStackTrace()) {
				sb.append(ele.toString() + "\n");
			}
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
//		double d = 0.16020;
//		System.out.println(formatQuantityOrPrice(d));
//		System.out.println(f);
		
//		System.out.println(getUriSendOrder("ETHUSDT", "SELL",  0.12345678, 500.12345678));
//		System.out.println(1.21%1.1);
		
		System.out.println(getMainCoin("ETHBTC", Global.RATE_BASE_COIN));
		
	}
}
