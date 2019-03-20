package zhou.hao.BinanSocket.entity;

import java.util.Date;

import zhou.hao.BinanSocket.utility.Global;
import zhou.hao.BinanSocket.utility.StringUtility;

public class RateItem extends Item {
	public double buyPrice = -1;
	public double fee = -1;
	public long timeSell = -1;	// 卖出的时间点
	
	public RateItem(String mCoin, String bCoin, double nowPrice, double fee) {
		this.symbol = mCoin + bCoin;
		this.mainCoin = mCoin;
		this.baseCoin = bCoin;
		this.nowPrice = nowPrice;
		this.fee =fee;
		
		long t = new Date().getTime() % Global.RATE_INTERVAL_KEEP_ORDER;
//		if(t >= Global.RATE_INTERVAL_KEEP_ORDER / 3 * 2)
//			timeSell = (new Date().getTime() - t) + Global.RATE_INTERVAL_KEEP_ORDER * 2;
//		else timeSell = (new Date().getTime() - t) + Global.RATE_INTERVAL_KEEP_ORDER;
//		timeSell -= Global.RATE_INTERVAL_SELL_OFFSET;
		
		if(t >= Global.RATE_INTERVAL_KEEP_ORDER / 3 * 2)
			timeSell = (new Date().getTime() - t) + Global.RATE_INTERVAL_KEEP_ORDER;
		else {
			timeSell = (new Date().getTime() - t) + Global.RATE_INTERVAL_KEEP_ORDER;
			timeSell -= Global.RATE_INTERVAL_SELL_OFFSET;
		}
	}
	
	public Boolean canSell() {
		if(((nowPrice / openPrice) >= Global.RATE_THRESHOLD_MAX_SELL) ||
		   ((nowPrice / buyPrice) <= Global.RATE_THRESHOLD_MIN_SELL) ||
		   (new Date().getTime() >= timeSell))
			return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public double rate() {
		return nowPrice / buyPrice;
	}
	
	public String toString() {
		return "symbol=" + String.valueOf(symbol) + ", " +
			"rateNow/Buy=" + String.valueOf(StringUtility.formatRate(nowPrice/buyPrice)) + ", " + 
			"rateMin/Open=" + String.valueOf(StringUtility.formatRate(minPrice/openPrice)) + ", " + 
			"rateMax/Open=" + String.valueOf(StringUtility.formatRate(maxPrice/openPrice)) + ", " + 
			"rateMax/Now=" + String.valueOf(StringUtility.formatRate(maxPrice/nowPrice)) + ", " + 
			"mainCoin=" + mainCoin + ", " +
			"baseCoin=" + baseCoin + ", " +
			"openPrice=" + String.valueOf(openPrice) + ", " + 
			"nowPrice=" + String.valueOf(nowPrice) + ", " +
			"maxPrice=" + String.valueOf(maxPrice) + ", " +
			"status=" + status() + ", " +
			"orderId=" + String.valueOf(orderId);
	}
}
