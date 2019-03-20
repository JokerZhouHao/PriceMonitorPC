package zhou.hao.BinanSocket.client.processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.loading.MLet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zhou.hao.BinanSocket.client.APIClient;
import zhou.hao.BinanSocket.client.io.RateSymbolsRecoder;
import zhou.hao.BinanSocket.client.ui.MessageDialog;
import zhou.hao.BinanSocket.client.window.MainWindow;
import zhou.hao.BinanSocket.entity.Item;
import zhou.hao.BinanSocket.entity.PriceQueue;
import zhou.hao.BinanSocket.entity.QuantityPriceFilter;
import zhou.hao.BinanSocket.entity.RateItem;
import zhou.hao.BinanSocket.utility.Global;
import zhou.hao.BinanSocket.utility.IOUtility;
import zhou.hao.BinanSocket.utility.MLog;
import zhou.hao.BinanSocket.utility.OutTimeUtility;
import zhou.hao.BinanSocket.utility.StringUtility;
import zhou.hao.email.EmailProcessor;

public class PriceMonitor implements Runnable{
	private APIClient apiClient = null;
	
	private Set<String> symbolsSet = null;
	private List<Item> allItems = new ArrayList<>();
	private Map<String, Double> symbolOpenPrices = new HashMap<>();	// 开盘价
	private Map<String, QuantityPriceFilter> symbolQuaPriceFilters = null;
	private static int numTrans = 0;
	
	private MainWindow mWindow = null;
	
	// status
	private Boolean isStart = Boolean.TRUE;
	private Boolean isNewDay = Boolean.TRUE;
	private Item dealingItem = null;	// 正在处理的订单
	private RateItem dealingRateItem = null;
	
	
	public PriceMonitor(MainWindow mWindow) throws Exception{
		this.mWindow = mWindow;
		apiClient = new APIClient();
		if(Global.isNormalMode()) {
			initSymbols();
			Map<String, Double> symbolNowPrices = apiClient.getPrices(symbolsSet);
			JSONArray jArray = null;
			for(Item im : allItems) {
				jArray = apiClient.get1DayKline(im.symbol);
				im.openPrice = jArray.getDoubleValue(1);
				im.maxPrice = jArray.getDoubleValue(2);
				im.nowPrice = symbolNowPrices.get(im.symbol);
				symbolOpenPrices.put(im.symbol, im.nowPrice);
				Thread.sleep(Global.INTERVAL_MIN);
			}
		}
	}
	
	public PriceMonitor() throws Exception{
		this(null);
	}
	
	public static int numTrans() {
		return numTrans;
	}
	
	public List<Item> allItems(){
		return allItems;
	}
	
	/**
	 * 从文件初始化所有symbols
	 * @throws Exception
	 */
	private void initSymbols() throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(Global.pathSymbols));
		symbolsSet = new HashSet<>();
		String line = null;
		String symbol = null;
		allItems = new ArrayList<>();
		String []arr = null;
		while(null != (line=br.readLine())) {
			if(line.startsWith("#") || line.trim().equals(""))	continue;
			arr = line.split(Global.delimiterSymbol);
			if(arr.length == 1) {
				symbol = arr[0].trim().replace("/", "");
				symbolsSet.add(symbol);
				allItems.add(new Item(arr[0].trim()));
			} else if(arr.length == 3) {
				symbol = arr[0].trim().replace("/", "");
				symbolsSet.add(symbol);
				if(arr[1].trim().equals("")) {
					if(arr[2].trim().equals("")) {
						allItems.add(new Item(arr[0].trim()));
					} else {
						allItems.add(new Item(arr[0].trim(), Global.BUY_MIN_THRESHOLD, 
								Double.parseDouble(arr[2].trim())));
					}
				} else {
					if(arr[2].trim().equals("")) {
						allItems.add(new Item(arr[0].trim(), Double.parseDouble(arr[1].trim()),
								Global.SELL_MAX_THRESHOLD));
					} else {
						allItems.add(new Item(arr[0].trim(), Double.parseDouble(arr[1].trim()), 
								Double.parseDouble(arr[2].trim())));
					}
				}
			}
		}
		br.close();
	}
	
	/**
	 * 设置开盘价
	 * @throws Exception
	 */
	private void setOpenPrices() throws Exception{
		for(Item im : allItems) {
			symbolOpenPrices.put(im.symbol, apiClient.getOpenPrice(im.symbol));
			Thread.sleep(Global.INTERVAL_MIN);
		}
	}
	
	/**
	 * 设置QuantityPriceFilter
	 * @throws Exception
	 */
	private void setQuantityPriceFilter() throws Exception{
		symbolQuaPriceFilters = apiClient.getQuantityPriceFilter(symbolsSet);
	}
	
	/*
	 * 获得baseCoin的所有filter
	 */
	private void setQuantityPriceFilter(String baseCoin) throws Exception{
		symbolQuaPriceFilters = apiClient.getAllQuantityPriceFilter(baseCoin);
	}
	
	
	/**
	 * 重置
	 */
	private void reset() throws Exception{
		// 查看是否还有未完成的订单
		if(dealingItem != null) {
			if(dealingItem.isBuying() || dealingItem.isSelling()) {	// 卡在买入或卖出阶段
				apiClient.deleteOrder(dealingItem.symbol, dealingItem.orderId);	// 删除订单
			}
			// 获得当前价格
			Thread.sleep(Global.INTERVAL_CONTINUOUS_REQUEST);
			Map<String, Double> symbolNowPrices = apiClient.getPrices(symbolsSet);
			sellOrder(dealingItem, symbolNowPrices.get(dealingItem.symbol));
			// 不间断测试是否已卖出
			while(true) {
				Thread.sleep(Global.INTERVAL_REQUEST);
				if(!apiClient.hasOrders()) {
					dealingItem.toOverTrans();
					break;
				}
			}
		}
		
		// 重置item
		for(Item im : allItems)	im.reset();
		resetItemPrices();
		
		numTrans = 0;
		dealingItem = null;
		setQuantityPriceFilter();
	}
	
	public void resetRate() throws Exception{
		// 查看是否还有未完成的订单
		if(dealingRateItem != null) {
			if(dealingRateItem.isBuying() || dealingRateItem.isSelling()) {	// 卡在买入或卖出阶段
				apiClient.deleteOrder(dealingRateItem.symbol, dealingRateItem.orderId);	// 删除订单
			}
			// 获得当前价格
			Thread.sleep(Global.RATE_INTERVAL_REQUEST);
			Set<String> st = new HashSet<>();
			st.add(dealingRateItem.symbol);
			Map<String, Double> symbolNowPrices = apiClient.getPrices(st);
			dealingRateItem.nowPrice = symbolNowPrices.get(dealingRateItem.symbol);
			sellOrder(dealingRateItem);
			// 不间断测试是否已卖出
			while(true) {
				Thread.sleep(Global.RATE_INTERVAL_REQUEST);
				if(!apiClient.hasOrders()) {
					dealingRateItem.toOverTrans();
					break;
				}
			}
			dealingRateItem = null;
		}
	}
	
	/**
	 * 重置所有item中的价格
	 * @throws Exception
	 */
	private void resetItemPrices() throws Exception{
		Map<String, Double> symbolNowPrices = apiClient.getPrices(symbolsSet);
		JSONArray jArray = null;
		for(Item im : allItems) {
			jArray = apiClient.get1DayKline(im.symbol);
			im.openPrice = jArray.getDoubleValue(1);
			im.maxPrice = jArray.getDoubleValue(2);
			im.nowPrice = symbolNowPrices.get(im.symbol);
			symbolOpenPrices.put(im.symbol, im.openPrice);
			Thread.sleep(Global.INTERVAL_MIN);
		}
		if(null != mWindow)	mWindow.refreshUI();
	}
	
	/**
	 * 买单
	 * @param item
	 * @param price
	 * @throws Exception
	 */
	private JSONObject buyOrder(Item item, double price) throws Exception{
		price = symbolQuaPriceFilters.get(item.symbol).getQualifiedPrice(price * Global.BUY_PRICE_PRECENT);
		
		// 获得当前数量
		Thread.sleep(Global.INTERVAL_MIN);
		double quantity = apiClient.getFree(item.baseCoin);
		quantity = symbolQuaPriceFilters.get(item.symbol).getQualifiedQuantity(quantity/price);
		
		if(null != mWindow && Global.SHOW_NOTIFICATION_BUY_WINDOW)	new MessageDialog(mWindow.frame(), "BT");
		
		String message = null;
		
		// 发送买入交易
		if(quantity>0 && price>0) {
			JSONObject jObject = apiClient.buyOrder(item.symbol, quantity, price);
			if(!jObject.containsKey("msg")) {
				item.toBuying();
				message = "success buy : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
				if(Global.EMAIL_SEND_BUY)	EmailProcessor.asynSend(Global.emailNotification, "成功买入", StringUtility.getLogDate() + message);
				MLog.writeLineCTA(message);
				return jObject;
			}
		}
		message = "fail buy : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
		EmailProcessor.asynSend(Global.emailNotification, "fail buy : ", StringUtility.getLogDate() + message);
		MLog.writeLineCTA(message);
		return null;
	}
	
	/**
	 * 买入RateItem
	 * @param item
	 * @return
	 * @throws Exception
	 */
	private JSONObject buyOrder(RateItem item) throws Exception{
		double price = symbolQuaPriceFilters.get(item.symbol).getQualifiedPrice(item.nowPrice * Global.RATE_BUY);
		
		// 获得当前数量
		Thread.sleep(Global.INTERVAL_MIN);
		double quantity = symbolQuaPriceFilters.get(item.symbol).getQualifiedQuantity(item.fee/price);
		
		if(null != mWindow && Global.SHOW_NOTIFICATION_BUY_WINDOW)	new MessageDialog(mWindow.frame(), "BT");
		
		String message = null;
		
		// 发送买入交易
		if(quantity>0 && price>0) {
			JSONObject jObject = apiClient.buyOrder(item.symbol, quantity, price);
			if(!jObject.containsKey("msg")) {
				item.toBuying();
				message = "success buy : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
				if(Global.EMAIL_SEND_BUY)	EmailProcessor.asynSend(Global.emailNotification, "成功买入", StringUtility.getLogDate() + message);
				MLog.writeLineCTA(message);
				return jObject;
			}
		}
		message = "fail buy : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
		EmailProcessor.asynSend(Global.emailNotification, "fail buy : ", StringUtility.getLogDate() + message);
		MLog.writeLineCTA(message);
		return null;
	}
	
	/**
	 * 卖单
	 * @param item
	 * @param price
	 * @throws Exception
	 */
	private JSONObject sellOrder(Item item, double price) throws Exception{
		// 获得当前数量
		Thread.sleep(Global.INTERVAL_MIN);
		double quantity = apiClient.getFree(item.mainCoin);
		quantity = symbolQuaPriceFilters.get(item.symbol).getQualifiedQuantity(quantity);
		
		price = symbolQuaPriceFilters.get(item.symbol).getQualifiedPrice(price * Global.SELL_PRICE_PRECENT);
		
		if(null != mWindow && Global.SHOW_NOTIFICATION_SELL_WINDOW)	new MessageDialog(mWindow.frame(), "SE");
		
		String message = null;
		
		// 发送卖出交易
		if(quantity>0 && price>0) {
			JSONObject jObj = apiClient.sellOrder(item.symbol, quantity, price);
			if(!jObj.containsKey("msg")) {
				item.toSelling();
				message = "success sell : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
				if(Global.EMAIL_SEND_SELL)	EmailProcessor.asynSend(Global.emailNotification, "成功卖出", StringUtility.getLogDate() + message);
				MLog.writeLineCTA(message);
				return jObj;
			}
		}
		message = "fail sell : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
		EmailProcessor.asynSend(Global.emailNotification, "fail sell : ", StringUtility.getLogDate() + message);
		MLog.writeLineCTA(message);
		return null;
	}
	
	/**
	 * 卖出RateItem
	 * @param item
	 * @return
	 * @throws Exception
	 */
	private JSONObject sellOrder(RateItem item) throws Exception{
		// 获得当前数量
		Thread.sleep(Global.INTERVAL_MIN);
		double quantity = apiClient.getFree(item.mainCoin);
		quantity = symbolQuaPriceFilters.get(item.symbol).getQualifiedQuantity(quantity);
		
		double price = symbolQuaPriceFilters.get(item.symbol).getQualifiedPrice(item.nowPrice * Global.RATE_SELL);
		
		if(null != mWindow && Global.SHOW_NOTIFICATION_SELL_WINDOW)	new MessageDialog(mWindow.frame(), "SE");
		
		String message = null;
		
		// 发送卖出交易
		if(quantity>0 && price>0) {
			JSONObject jObj = apiClient.sellOrder(item.symbol, quantity, price);
			if(!jObj.containsKey("msg")) {
				item.toSelling();
				message = "success sell : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
				if(Global.EMAIL_SEND_SELL)	EmailProcessor.asynSend(Global.emailNotification, "成功卖出", StringUtility.getLogDate() + message);
				MLog.writeLineCTA(message);
				return jObj;
			}
		}
		message = "fail sell : " + item.toString() + ", quantity=" + String.valueOf(quantity) + ", price=" + String.valueOf(price);
		EmailProcessor.asynSend(Global.emailNotification, "fail sell : ", StringUtility.getLogDate() + message);
		MLog.writeLineCTA(message);
		return null;
	}
	
	/*
	 * normal mode
	 */
	private void runNormalMode() {
//		Date dt = new Date();
//		System.out.println(dt.getTime() % Global.MILL_1DAY / 1000 / 60);
		OutTimeUtility otu = new OutTimeUtility();
		
		if(new Date().getTime() % Global.MILL_1DAY >= Global.MILL_NEW_DAY)	isNewDay = Boolean.TRUE;
		else isNewDay = Boolean.FALSE;
		while(true) {
			try {
				if(isNewDay) {	// 判断是否是新的一天
					if(new Date().getTime() % Global.MILL_1DAY >= Global.MILL_NEW_DAY) {	// 新的一天
						MLog.writeLineCTA("new day");
						reset();	// 重置
						isNewDay = Boolean.FALSE;
						Thread.sleep(Global.INTERVAL_CONTINUOUS_REQUEST);
					}
				} else if(new Date().getTime() % Global.MILL_1DAY < Global.MILL_NEW_DAY)	isNewDay = Boolean.TRUE;
				
				// 更新价格
				Map<String, Double> symbolNowPrices = apiClient.getPrices(symbolsSet);
				Thread.sleep(Global.INTERVAL_MIN);
				for(Item im : allItems) {
					im.refreshPrice(symbolNowPrices.get(im.symbol));
				}
				if(null != mWindow)	mWindow.refreshUI();
				
				if(isStart) {	// 启动
					MLog.writeLineCTA("PriceMonitor start normal mode success");
					JSONArray jArray = apiClient.getOpenOrders();
					if(jArray.size() != 0) {	// 存在正在交易的订单
						JSONObject jObject = jArray.getJSONObject(0);
						for(Item im : allItems) {
							if(im.symbol.equalsIgnoreCase(jObject.getString("symbol"))) {
								dealingItem = im;
//								if(jObject.getString("side").equalsIgnoreCase("SELL"))	dealingItem.toSelling();
//								else dealingItem.toBuying();
								dealingItem.toBuying();
								break;
							}
						}
					}
					resetItemPrices();
					setQuantityPriceFilter();
					isStart = Boolean.FALSE;
				} else if(dealingItem != null) {	// 存在待处理的symbol
					// 判断
					if(dealingItem.isBuying() && !apiClient.hasOrders()) {
						dealingItem.toHasBuy();
						Thread.sleep(Global.INTERVAL_MIN);
					}
					if(dealingItem.hasBuy()) {
						// 检查是否能够卖出
						double openPrice = symbolOpenPrices.get(dealingItem.symbol);
						double nowPrice = symbolNowPrices.get(dealingItem.symbol);
						JSONObject jObject = null;
						if(dealingItem.canSell(openPrice, nowPrice))
							jObject = sellOrder(dealingItem, nowPrice);
						if(null != jObject)	{
							dealingItem.orderId = jObject.getString("orderId");
							dealingItem.toSelling();
							Thread.sleep(Global.INTERVAL_REQUEST);
						}
					}
					if(dealingItem.isSelling() && !apiClient.hasOrders()) {
						dealingItem.toOverTrans();
						dealingItem = null;
						numTrans++;
					}
				} else {
					// 遍历剩下的item，寻找能够买入的symbol
					JSONObject resObj = null;
					double openPrice, nowPrice;
					for(Item im : allItems) {
						openPrice = symbolOpenPrices.get(im.symbol);
						nowPrice = symbolNowPrices.get(im.symbol);
						if(im.noTrans() && im.canBuy(openPrice, nowPrice)) {
							resObj = buyOrder(im, nowPrice);
							if(null != resObj) {
								dealingItem = im;
								dealingItem.toBuying();
								dealingItem.orderId = resObj.getString("orderId");
								break;
							}
						}
					}
				}
				otu.resetConnectTime();
				Thread.sleep(Global.INTERVAL_REQUEST);
			} catch (Exception e) {
				MLog.writeLineCTA("run exception: " + StringUtility.getExceptionStackInfo(e));
				try {
					if(otu.connectOverTime(new Date().getTime())) {
						EmailProcessor.asynSend(Global.emailNotification, "异常超时10分钟", StringUtility.getLogDate() + "run exception: " + e.getMessage());
						return;
					}
					Thread.sleep(Global.INTERVAL_REQUEST);
					apiClient.resetHttp();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
	}
	
	
	/*
	 * run rate mode
	 */
	public void runRateMode() {
		OutTimeUtility otu = new OutTimeUtility();
		
		if(new Date().getTime() % Global.MILL_1DAY >= Global.MILL_NEW_DAY)	isNewDay = Boolean.TRUE;
		else isNewDay = Boolean.FALSE;
		
		Map<String, PriceQueue> symbol2Price = new HashMap<>();
		Map<String, JSONArray> symbol2K = new HashMap<>();
		PriceQueue tPQueue = null;
		Set<String> availSymbols = new HashSet<>();
		Set<String> hasTransSymbols = new HashSet<>();
		Map<String, Double> volumes = new HashMap<>();
		FeeManager feeM = null;
		long timeSleep = 0;
		long tickRefreshGlobalParams = 0;
		
		while(true) {
			try {
				if(tickRefreshGlobalParams++ % 30 == 0) {
					Global.initGlobalParams();
				}
				timeSleep = Global.RATE_INTERVAL_TICK;
				if(isNewDay) {	// 判断是否是新的一天
					if(new Date().getTime() % Global.MILL_1DAY >= Global.MILL_NEW_DAY) {	// 新的一天
						MLog.writeLineCTA("new day");
						
						symbol2Price.clear();
						symbol2K.clear();
						availSymbols.clear();
						if(isStart)	hasTransSymbols = RateSymbolsRecoder.load();
						else {
							hasTransSymbols.clear();
							RateSymbolsRecoder.reset();
						}
						resetRate();
						dealingRateItem = null;
						
						feeM = new FeeManager(apiClient.getFree(Global.RATE_BASE_COIN));
						MLog.writeLineCTA("new day > fee : " + feeM.first() + Global.RATE_BASE_COIN);
						
						setQuantityPriceFilter(Global.RATE_BASE_COIN);
						
						isNewDay = Boolean.FALSE;
						Thread.sleep(Global.INTERVAL_CONTINUOUS_REQUEST);
					}
				} else if(new Date().getTime() % Global.MILL_1DAY < Global.MILL_NEW_DAY)	isNewDay = Boolean.TRUE;
				
				
				// 获得24hr交易量
				volumes = apiClient.get24HrQuoteVolume(Global.RATE_BASE_COIN);
				Thread.sleep(Global.INTERVAL_MIN);
				
				// 更新价格
				availSymbols.clear();
				JSONArray jArr = apiClient.getJsonAllPrice();
				JSONObject jObj = null;
				String sym = null;
				for(Object obj : jArr) {
					jObj = (JSONObject)obj;
					sym = jObj.getString("symbol");
					if(sym.endsWith(Global.RATE_BASE_COIN) &&
					   jObj.getDouble("price") > Global.RATE_MIN_PRICE &&
					   volumes.containsKey(sym) &&
					   volumes.get(sym) >= Global.RATE_MIN_VOLUME) {
						availSymbols.add(sym);
						if(null == (tPQueue = symbol2Price.get(sym))) {
							tPQueue = new PriceQueue(Global.RATE_NUM_TICK);
							symbol2Price.put(sym, tPQueue);
						}
						tPQueue.add(jObj.getDouble("price"));
					}
				}
				Thread.sleep(Global.INTERVAL_MIN);
				
//				System.out.println(volumes.get("QTUMBTC") + " " + symbol2Price.get("QTUMBTC"));
//				for(Item im : allItems) {
//					im.refreshPrice(symbolNowPrices.get(im.symbol));
//				}
//				if(null != mWindow)	mWindow.refreshUI();
				
				if(isStart) {	// 启动
					feeM = new FeeManager(apiClient.getFree(Global.RATE_BASE_COIN));
					hasTransSymbols = RateSymbolsRecoder.load();
					dealingRateItem = null;
					setQuantityPriceFilter(Global.RATE_BASE_COIN);
					isStart = Boolean.FALSE;
					MLog.writeLineCTA("PriceMonitor start rate mode success");
				} else if(dealingRateItem != null) {	// 存在待处理的symbol
					dealingRateItem.refreshPrice(symbol2Price.get(dealingRateItem.symbol).newest());
					// 判断
					if(dealingRateItem.isBuying() && !apiClient.hasOrder(dealingRateItem.symbol, dealingRateItem.orderId)) {
						dealingRateItem.toHasBuy();
						Thread.sleep(Global.INTERVAL_MIN);
					}
					if(dealingRateItem.hasBuy()) {
						// 检查是否能够卖出
						JSONObject jObject = null;
						if(dealingRateItem.canSell())
							jObject = sellOrder(dealingRateItem);
						if(null != jObject)	{
							dealingRateItem.orderId = jObject.getString("orderId");
							dealingRateItem.toSelling();
							Thread.sleep(Global.INTERVAL_CONTINUOUS_REQUEST);
						}
					}
					if(dealingRateItem.isSelling() && !apiClient.hasOrder(dealingRateItem.symbol, dealingRateItem.orderId)) {
						dealingRateItem.toOverTrans();
						if(Global.RATE_REMOVE_USED) {
							hasTransSymbols.add(dealingRateItem.symbol);
							RateSymbolsRecoder.addSymbol(dealingRateItem.symbol);
						}
						feeM = new FeeManager(apiClient.getFree(Global.RATE_BASE_COIN));
						MLog.writeLineCTA("sell > fee : " + feeM.first() + Global.RATE_BASE_COIN);
						dealingRateItem = null;
						symbol2Price.clear();
						numTrans++;
					}
				} else if(!symbol2Price.isEmpty() && feeM.hasFee()) {
					// 遍历所有symbols，寻找能够买入的symbol
					for(String symbol : availSymbols) {
						tPQueue = symbol2Price.get(symbol);
//						System.out.println(tPQueue.endDivideStart());
						if( feeM.hasFee() &&
							!hasTransSymbols.contains(symbol) &&
							null != tPQueue &&
							(tPQueue.endDivideStart() >= Global.RATE_IN_REQUEST)) {
								RateItem tItem = new RateItem(StringUtility.getMainCoin(symbol, Global.RATE_BASE_COIN),
																Global.RATE_BASE_COIN, tPQueue.newest(), feeM.first());
								
//								if(null == (jArr = symbol2K.get(symbol))) {
//									symbol2K.put(symbol, jArr);
//								}
//								jArr = symbol2K.get(symbol);
//								if(null != jArr) {
//									tItem.openPrice = jArr.getDoubleValue(1);
//									if(tPQueue.oldest() / tItem.openPrice > Global.RATE_BUY_MAX_THRESHOLD)	continue;
//								}
								jArr = apiClient.get1DayKline(tItem.symbol);
//								symbol2K.put(symbol, jArr);
								Thread.sleep(Global.INTERVAL_MIN);
								timeSleep -= Global.INTERVAL_MIN;
								tItem.openPrice = jArr.getDoubleValue(1);
								tItem.maxPrice = jArr.getDoubleValue(2);
								tItem.minPrice = jArr.getDoubleValue(3);
								tItem.buyPrice = tPQueue.newest();
								
								MLog.writeLineCTA("end / start = " + StringUtility.formatRate(tPQueue.endDivideStart()));
								MLog.writeLineCTA(tItem.toString());
								
								if(((tItem.maxPrice <= tItem.openPrice * Global.RATE_BUY_MAX_THRESHOLD ) || 
										(tItem.maxPrice > tItem.openPrice * Global.RATE_BUY_MAX_THRESHOLD && 
										tItem.maxPrice / tPQueue.newest() <= Global.RATE_MAX_RISE_RANGE)) &&
								   tPQueue.oldest() / tItem.openPrice <= Global.RATE_BUY_MAX_THRESHOLD &&
								   tItem.minPrice / tItem.openPrice >= Global.RATE_BUY_MIN_THRESHOLD);
								else{
									hasTransSymbols.add(tItem.symbol);
									RateSymbolsRecoder.addSymbol(tItem.symbol);
									continue;
								}
								
								if(null != mWindow)	mWindow.addItem(tItem);	// 添加条目显示
								
								JSONObject resObj = null;
								resObj = buyOrder(tItem);
								if(null != resObj) {
									MLog.writeLineCTA("buy > fee : " + feeM.first() + Global.RATE_BASE_COIN);
									feeM.pullFee();	// 已消费
									dealingRateItem = tItem;
									dealingRateItem.toBuying();
									dealingRateItem.orderId = resObj.getString("orderId");
									break;
								}
						}
					}
				}
				
				// 刷新UI
				if(null != mWindow)	mWindow.refreshUI();
				
				otu.resetConnectTime();
//				Thread.sleep(Global.RATE_INTERVAL_REQUEST);
//				Thread.sleep(Global.RATE_INTERVAL_TICK - 5 * Global.INTERVAL_MIN);
				Thread.sleep(timeSleep - 5 * Global.INTERVAL_MIN);
			} catch (Exception e) {
				symbol2Price.clear(); // 清空preSymbol2Price
				MLog.writeLineCTA("run exception: " + StringUtility.getExceptionStackInfo(e));
				try {
					if(otu.connectOverTime(new Date().getTime())) {
						EmailProcessor.asynSend(Global.emailNotification, "异常超时10分钟", StringUtility.getLogDate() + "run exception: " + e.getMessage());
						return;
					}
					Thread.sleep(Global.INTERVAL_REQUEST);
					apiClient.resetHttp();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
	}
	
	/**
	 * 线程运行
	 */
	public void run() {
		if(Global.isNormalMode())	runNormalMode();
		else if(Global.isRateMode())	runRateMode();
	}
	
	public static void main(String[] args) throws Exception{
		PriceMonitor pm = new PriceMonitor();
		pm.run();
		
		
//		pm.run();
//		long startT = new Date().getTime();
//		JSONArray jArr = pm.apiClient.getJsonAllPrice();
//		int numBtc = 0;
//		for(Object obj : jArr) {
//			if(((JSONObject)obj).getString("symbol").endsWith("BTC")) {
//				System.out.println(((JSONObject)obj).getString("symbol"));
//				numBtc++;
//			}
//		}
//		System.out.println(new Date().getTime() - startT);
//		System.out.println(numBtc);
	}
}
