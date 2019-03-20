package zhou.hao.BinanSocket.entity;

import zhou.hao.BinanSocket.client.processor.PriceMonitor;
import zhou.hao.BinanSocket.utility.Global;
import zhou.hao.BinanSocket.utility.StringUtility;

public class Item {
	public String symbol = null;
	public String mainCoin = null;
	public String baseCoin = null;
	public double openPrice = -1;
	public double nowPrice = -1;
	public double maxPrice = -1;
	public double minPrice = -1;
	public int status = 0; // 0--未交易过，1--正在买入，2--已买入，3--正在卖出，4--已卖出(交易完成)
	public String orderId = null;
	
	public double BUY_MIN_THRESHOLD = -1;
	public double SELL_MAX_THRESHOLD = -1;
	
	public Item() {}
	
	public Item(String symbol) {
		this(symbol, Global.BUY_MIN_THRESHOLD, Global.SELL_MAX_THRESHOLD);
	}
	
	public Item(String symbol, double BUY_MIN_THRESHOLD, double SELL_MAX_THRESHOLD) {
		this.symbol = symbol.replace("/", "");
		mainCoin = symbol.split("/")[0];
		baseCoin = symbol.split("/")[1];
		this.BUY_MIN_THRESHOLD = BUY_MIN_THRESHOLD;
		this.SELL_MAX_THRESHOLD = SELL_MAX_THRESHOLD;
	}
	
	public void reset() {
		openPrice = 0.0;
		nowPrice = 0.0;
		maxPrice = 0.0;
		status = 0;
		orderId = null;
	}
	
	public Boolean canBuy(double openPrice, double nowPrice) {
		if(noTrans()) {
			if((0==PriceMonitor.numTrans() && nowPrice >= openPrice * BUY_MIN_THRESHOLD && nowPrice <= openPrice * Global.BUY_FIRST_MAX_THRESHOLD && maxPrice <= openPrice * Global.BUY_FIRST_MAX_THRESHOLD)
				|| ((PriceMonitor.numTrans() >0 && PriceMonitor.numTrans() <2 && nowPrice >= openPrice * BUY_MIN_THRESHOLD && nowPrice <= openPrice * Global.BUY_NEXT_MAX_THRESHOLD && maxPrice <= openPrice * Global.BUY_NEXT_MAX_THRESHOLD))) {
				return Boolean.TRUE;
			}
			else return Boolean.FALSE;
		} else return Boolean.FALSE;
	}
	
	public Boolean canSell(double openPrice, double nowPrice) {
		if(hasBuy() && (nowPrice >= openPrice * SELL_MAX_THRESHOLD || nowPrice <= openPrice * Global.SELL_MIN_THRESHOLD)) return Boolean.TRUE;
		return Boolean.FALSE;
	}
	
	public void refreshPrice(double price) {
		nowPrice = price;
		if(maxPrice < price) maxPrice = price;
	}
	
	public Boolean noTrans() {
		if(0 == status)	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public Boolean isBuying() {
		if(1 == status)	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public Boolean hasBuy() {
		if(2 == status) return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public Boolean isSelling() {
		if(3 == status)	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public Boolean overTrans() {
		if(4 == status) return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	public double rate() {
		return nowPrice / openPrice;
	}
	
	public void toBuying() {
		status = 1;
	}
	
	public void toHasBuy() {
		status = 2;
	}
	
	public void toSelling() {
		status = 3;
	}
	
	public void toOverTrans() {
		status = 4;
	}
	
	public String status() {
		switch (status) {
			case 0:
				return "NOTRANS";
			case 1:
				return "BUYING";
			case 2:
				return "BUYED";
			case 3:
				return "SELLING";
			case 4:
				return "OVERTRANS";
			default:
				return "NOTRANS";
		}
	}
	
	public String toString() {
		return "symbol=" + String.valueOf(symbol) + ", " +
			"rate=" + String.valueOf(StringUtility.formatRate(nowPrice/openPrice)) + ", " + 
			"mainCoin=" + mainCoin + ", " +
			"baseCoin=" + baseCoin + ", " +
			"openPrice=" + String.valueOf(openPrice) + ", " + 
			"nowPrice=" + String.valueOf(nowPrice) + ", " +
			"maxPrice=" + String.valueOf(maxPrice) + ", " +
			"status=" + status() + ", " +
			"orderId=" + String.valueOf(orderId);
	}
}
