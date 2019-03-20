package zhou.hao.BinanSocket.client;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zhou.hao.BinanSocket.entity.QuantityPriceFilter;
import zhou.hao.BinanSocket.utility.Global;
import zhou.hao.BinanSocket.utility.StringUtility;

public class APIClient {
	private static CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(Global.PROXY_CONFIG).build();
	
	private static HttpGet GetGeneral = new HttpGet();
	private static HttpGet GetPrice = new HttpGet(Global.URI_GET_PRICES);
	private static HttpGet GetAccount = new HttpGet();
	private static HttpGet GetOpenOrders = new HttpGet();
	private static HttpDelete DeleteOrders = new HttpDelete();
	private static HttpPost PostSendOrder = new HttpPost();
	private static HttpGet GetExchangeInfo = new HttpGet(Global.URI_EXCHANGE_INFO);
	private static HttpGet Get24hrTicker = new HttpGet(Global.URI_24HR_TICKER);
	
	private static Map<String, String> HEADER = new HashMap<>();
	static {
//		HEADER.put(Global.API_KEY_HEADER, Global.API_KEY);
		GetAccount.addHeader(Global.API_KEY_HEADER, Global.API_KEY);
		GetOpenOrders.addHeader(Global.API_KEY_HEADER, Global.API_KEY);
		DeleteOrders.addHeader(Global.API_KEY_HEADER, Global.API_KEY);
		PostSendOrder.addHeader(Global.API_KEY_HEADER, Global.API_KEY);
	}
	
	public APIClient() {
//		for(Entry<String, String> en : HEADER.entrySet()) {
//			httpGet.addHeader(en.getKey(), en.getValue());
//			httpPost.addHeader(en.getKey(), en.getValue());
//		}
	}
	
	public void resetHttp() throws Exception{
		try {
			client.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		client = HttpClients.custom().setDefaultRequestConfig(Global.PROXY_CONFIG).build();
	}
	
	/**
	 * 获得所有价格的json串
	 * @return
	 * @throws Exception
	 */
	public JSONArray getJsonAllPrice() throws Exception{
		CloseableHttpResponse response = client.execute(GetPrice);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		return JSONArray.parseArray(str);
	}
	
	/**
	 * 获得symbols对应的价格
	 * @param symbols
	 * @return
	 * @throws Exception
	 */
	public Map<String, Double> getPrices(Set<String> symbols) throws Exception{
		int num = symbols.size();
		Map<String, Double> prices = new HashMap<>();
		JSONArray array = getJsonAllPrice();
		for(Object ob : array) {
			JSONObject jo = (JSONObject)ob;
			if(symbols.contains(jo.getString("symbol"))) {
				prices.put(jo.getString("symbol"), jo.getDouble("price"));
				if((--num)==0)	break;
			}
		}
		if(prices.isEmpty())	return null;
		else return prices;
	}
	
	/**
	 * 获得当天的k线数据
	 * @param symbol
	 * @return
	 * @throws Exception
	 */
	public JSONArray get1DayKline(String symbol) throws Exception{
		GetGeneral.setURI(new URI(StringUtility.getUri1DKline(symbol)));
		CloseableHttpResponse response = client.execute(GetGeneral);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
//		System.out.println(JSONArray.parseArray(str).getJSONArray(0));
		return JSONArray.parseArray(str).getJSONArray(0);
	}
	
	/**
	 * 获得开盘价
	 * @param symbol
	 * @return
	 * @throws Exception
	 */
	public double getOpenPrice(String symbol) throws Exception{
		return get1DayKline(symbol).getDoubleValue(1);
	}
	
	/**
	 * 获得当天最高价
	 * @param symbol
	 * @return
	 * @throws Exception
	 */
	public double getMaxPrice(String symbol) throws Exception{
		return get1DayKline(symbol).getDoubleValue(2);
	}
	
	/**
	 * 获得账户信息
	 * @return
	 * @throws Exception
	 */
	private JSONObject getAccount() throws Exception{
		GetAccount.setURI(new URI(StringUtility.getUriAccount()));
		CloseableHttpResponse response = client.execute(GetAccount);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		return JSONObject.parseObject(str);
	}
	
	/**
	 * 获得symbol对应的余额数
	 * @param symbol
	 * @return
	 * @throws Exception
	 */
	public Double getFree(String symbol) throws Exception{
		JSONArray array = getAccount().getJSONArray("balances");
		for(Object ob : array) {
			if(((JSONObject)ob).getString("asset").equalsIgnoreCase(symbol))
				return (((JSONObject)ob).getDouble("free"));
		}
		return -1.0;
	}
	
	/**
	 * 获得symbols对应的free
	 * @param symbols
	 * @return
	 * @throws Exception
	 */
	public Map<String, Double> getFrees(Set<String> symbols) throws Exception{
		JSONArray array = getAccount().getJSONArray("balances");
		Map<String, Double> symbol2Free = new HashMap<>();
		for(Object ob : array) {
			if(symbols.contains(((JSONObject)ob).getString("asset"))) {
				symbol2Free.put(((JSONObject)ob).getString("asset"), ((JSONObject)ob).getDouble("free"));
			}
		}
		if(symbol2Free.isEmpty())	return null;
		else return symbol2Free;
	}
	
	/**
	 * 获得当前所有订单
	 * @return
	 * @throws Exception
	 */
	public JSONArray getOpenOrders() throws Exception{
		GetOpenOrders.setURI(new URI(StringUtility.getUriOpenOrders()));
		CloseableHttpResponse response = client.execute(GetOpenOrders);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		return JSONArray.parseArray(str);
	}
	
	/**
	 * 获得当前所有订单id
	 * @return
	 * @throws Exception
	 */
	public List<BasicNameValuePair> getAllOrderIds() throws Exception{
		JSONArray array = getOpenOrders();
		if(array.isEmpty())	return null;
		List<BasicNameValuePair> orderIds = new ArrayList<>();	// symbol--orderId
		for(Object ob : array) {
			JSONObject jo = (JSONObject)ob;
			orderIds.add(new BasicNameValuePair(jo.getString("symbol"), jo.getString("orderId")));
		}
		return orderIds;
	}
	
	/**
	 * 判断是否还有订单
	 * @return
	 * @throws Exception
	 */
	public Boolean hasOrders() throws Exception{
		if(null == getAllOrderIds())	return Boolean.FALSE;
		else return Boolean.TRUE;
	}
	
	/**
	 * 判断是否还有相应的订单
	 * @param symbol
	 * @param orderId
	 * @return
	 * @throws Exception
	 */
	public Boolean hasOrder(String symbol, String orderId) throws Exception{
		List<BasicNameValuePair> sIds = null;
		if(null == (sIds = getAllOrderIds()))	return Boolean.FALSE;
		for(BasicNameValuePair pair : sIds)
			if(pair.getName().equals(symbol) && pair.getValue().equals(orderId))	return Boolean.TRUE;
		return Boolean.FALSE;
	}
	
	/**
	 * 删除指定订单
	 * @param symbol
	 * @param orderId
	 * @return
	 * @throws Exception
	 */
	public JSONObject deleteOrder(String symbol, String orderId) throws Exception{
		DeleteOrders.setURI(new URI(StringUtility.getUriDeleteOrders(symbol, orderId)));
		CloseableHttpResponse response = client.execute(DeleteOrders);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		return JSONObject.parseObject(str);
	}
	
	/**
	 * 删除所有订单
	 * @return
	 * @throws Exception
	 */
	public Boolean deleteAllOrder() throws Exception{
		List<BasicNameValuePair> allOrderIds = getAllOrderIds();
		if(null == allOrderIds)	return Boolean.TRUE;
		for(BasicNameValuePair pair : allOrderIds) {
			deleteOrder(pair.getName(), pair.getValue());
			Thread.sleep(2000);
		}
		if(null == getAllOrderIds())	return Boolean.TRUE;
		else return Boolean.FALSE;
	}
	
	/**
	 * 获得数量和价格过滤器
	 * @param symbols
	 * @return
	 * @throws Exception
	 */
	public Map<String, QuantityPriceFilter> getQuantityPriceFilter(Set<String> symbols) throws Exception{
		CloseableHttpResponse response = client.execute(GetExchangeInfo);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		JSONArray array = ((JSONObject)JSONObject.parse(str)).getJSONArray("symbols");
		int num = symbols.size();
		JSONObject lotSize = null;
		JSONObject priceFilter = null;
		JSONArray filterArray = null;
		Map<String, QuantityPriceFilter> filters = new HashMap<>();
		for(Object obj : array) {
			JSONObject jo = (JSONObject)obj;
			if(symbols.contains(jo.getString("symbol"))) {
				filterArray = jo.getJSONArray("filters");
				for(Object obj1 : filterArray) {
					if(((JSONObject)obj1).getString("filterType").equals("LOT_SIZE"))	lotSize = (JSONObject)obj1;
					else if(((JSONObject)obj1).getString("filterType").equals("PRICE_FILTER")) priceFilter = (JSONObject)obj1;
				}
				filters.put(jo.getString("symbol"), new QuantityPriceFilter(jo.getString("symbol"), lotSize, priceFilter));
				if((--num)==0)	return filters;
			}
		}
		return filters;
	}
	
	/**
	 * 获得指定baseCoin的所有filter
	 * @param baseCoin
	 * @return
	 * @throws Exception
	 */
	public Map<String, QuantityPriceFilter> getAllQuantityPriceFilter(String baseCoin) throws Exception{
		CloseableHttpResponse response = client.execute(GetExchangeInfo);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		JSONArray array = ((JSONObject)JSONObject.parse(str)).getJSONArray("symbols");
		Map<String, QuantityPriceFilter> allFilters = new HashMap<>();
		JSONObject lotSize = null;
		JSONObject priceFilter = null;
		JSONArray filterArray = null;
		for(Object obj : array) {
			JSONObject jo = (JSONObject)obj;
			filterArray = jo.getJSONArray("filters");
			if(!jo.getString("symbol").endsWith(baseCoin))	continue;
			for(Object obj1 : filterArray) {
				if(((JSONObject)obj1).getString("filterType").equals("LOT_SIZE"))	lotSize = (JSONObject)obj1;
				else if(((JSONObject)obj1).getString("filterType").equals("PRICE_FILTER")) priceFilter = (JSONObject)obj1;
			}
			allFilters.put(jo.getString("symbol"), new QuantityPriceFilter(jo.getString("symbol"), lotSize, priceFilter));
		}
		return allFilters;
	}
	
	/**
	 * 发送订单
	 * @param symbol
	 * @param side
	 * @param quantity
	 * @param price
	 * @return
	 * @throws Exception
	 */
	private JSONObject sendOrder(String symbol, String side, double quantity, double price) throws Exception{
		PostSendOrder.setURI(new URI(StringUtility.getUriSendOrder(symbol, side, quantity, price)));
		CloseableHttpResponse response = client.execute(PostSendOrder);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		return JSONObject.parseObject(str);
	}
	
	/**
	 * 发送购买订单
	 * @param symbol
	 * @param quantity
	 * @param price
	 * @return
	 * @throws Exception
	 */
	public JSONObject buyOrder(String symbol, double quantity, double price) throws Exception{
		return sendOrder(symbol, "BUY", quantity, price);
	}
	
	/**
	 * 发送卖出订单
	 * @param symbol
	 * @param quantity
	 * @param price
	 * @return
	 * @throws Exception
	 */
	public JSONObject sellOrder(String symbol, double quantity, double price) throws Exception{
		return sendOrder(symbol, "SELL", quantity, price);
	}

	
	/**
	 * 获得24hr ticker
	 * @return
	 * @throws Exception
	 */
	public JSONArray getJson24hrTicker() throws Exception{
		CloseableHttpResponse response = client.execute(Get24hrTicker);
		String str = EntityUtils.toString(response.getEntity());
		response.close();
		return JSONArray.parseArray(str);
	}
	
	/**
	 * 获得最近24小时内的交易量
	 * @param symbol
	 * @return
	 * @throws Exception
	 */
	public Map<String, Double> get24HrQuoteVolume(String symbol) throws Exception{
		Map<String, Double> vs = new HashMap<>();
		JSONArray jArr = getJson24hrTicker();
		JSONObject jObj = null;
		for(Object obj : jArr) {
			jObj = (JSONObject)obj;
			if(jObj.getString("symbol").endsWith(symbol)) 
				vs.put(jObj.getString("symbol"), jObj.getDouble("quoteVolume"));
		}
		return vs;
	}
	
	public static void main(String []args) throws Exception{
		APIClient td = new APIClient();
		// 24 hr ticker
		System.out.println(td.get24HrQuoteVolume("BTC").get("ETHBTC"));
		
		// test price
//		Set<String> symbols = new HashSet<>();
//		symbols.add("ONTUSDT");
//		symbols.add("TRXUSDT");
//		symbols.add("ETHUSDT");
//		System.out.println(td.getPrices(symbols));
		
		// test openprice
//		System.out.println(new Date().getTime());
//		System.out.println(new Date().getTime() - Global.MILL_DAY);
//		System.out.println(td.getOpenPrice("TRXUSDT"));
		
//		Set<String> symbols = new HashSet<>();
//		symbols.add("ETH");
//		symbols.add("USDT");
//		symbols.add("ONT");
//		System.out.println(td.getFrees(symbols));
//		symbols = new HashSet<>();
//		symbols.add("ETHBTC");
//		symbols.add("BNTETH");
//		symbols.add("SNTETH");
//		Map<String, QuantityPriceFilter> filter = td.getQuantityPriceFilter(symbols);
		
		
		
		// test get 余额
//		System.out.println(td.getAccount());
//		System.out.println(td.getFree("eth"));
		
		// test 当前订单
//		System.out.println(td.getOpenOrders().toJSONString());
//		System.out.println(td.getAllOrderIds());
		
		// test 删除订单
//		System.out.println(td.deleteOrder("ETHUSDT", "195221654").toString());
//		System.out.println(td.deleteAllOrder());
		
		
		// test getQuantityPriceFilter
//		Set<String> symbols = new HashSet<>();
//		symbols.add("ETHBTC");
//		symbols.add("BNTETH");
//		symbols.add("SNTETH");
//		System.out.println(td.getQuantityPriceFilter(symbols));
		
		// test 发送购买订单
//		System.out.println(td.sellOrder("ETHUSDT", 0.0988, 500));
//		System.out.println(td.getFree("USDT"));
//		Set<String> symbols = new HashSet<>();
//		symbols.add("ETHUSDT");
//		Map<String, QuantityPriceFilter> filters = td.getQuantityPriceFilter(symbols);
//		System.out.println(filters.get("ETHUSDT").lotSize);
//		
//		double qua = 20;
//		double lotsize = 0.00001000;
//		System.out.println((qua - lotsize) / lotsize);
//		
//		
//		System.out.println(filters.get("ETHUSDT").getQualifiedQuantity(20));
		
		
//		System.out.println(td.buyOrder("ETHUSDT", filters.get("ETHUSDT").getQualifiedQuantity(0.65000012), filters.get("ETHUSDT").getQualifiedPrice(20)));
		
//		Set<String> symbols = new HashSet<>();
//		symbols.add("ONTUSDT");
//		Map<String, QuantityPriceFilter> filters = td.getQuantityPriceFilter(symbols);
//		QuantityPriceFilter qpf = filters.get("ONTUSDT");
//		System.out.println(qpf.lotSize);
//		double price = qpf.getQualifiedPrice(20);
//		double quantity = qpf.getQualifiedQuantity(td.getFree("USDT")/price);
//		System.out.println("price=" + price + "\n" + "quantity=" + quantity + "\ntotal=" + price * quantity);
//		System.out.println(qpf.getQualifiedQuantity(337.92));
		
		
//		System.out.println(td.buyOrder("ETHUSDT", quantity, price));
	}
}
