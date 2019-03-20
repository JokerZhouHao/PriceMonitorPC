package zhou.hao.BinanSocket.entity;

import com.alibaba.fastjson.JSONObject;

public class QuantityPriceFilter {
	private String symbol = null;
	public JSONObject lotSize = null;
	public JSONObject priceFilter = null;
	
	public QuantityPriceFilter(String symbol, JSONObject lotSize, JSONObject priceFilter) {
		super();
		this.symbol = symbol;
		this.lotSize = lotSize;
		this.priceFilter = priceFilter;
	}

	public String getSymbol() {
		return symbol;
	}
	
	public double getQualifiedQuantity(double quantity) {
		if(quantity < lotSize.getDoubleValue("minQty")) return -1;
		return ((int)((quantity - lotSize.getDoubleValue("minQty")) / lotSize.getDoubleValue("stepSize"))) * lotSize.getDoubleValue("stepSize")
				+ lotSize.getDoubleValue("minQty");
	}
	
	public double getQualifiedPrice(double price) {
		if(price < priceFilter.getDoubleValue("minPrice"))	return -1;
		return ((int)((price - priceFilter.getDoubleValue("minPrice")) / priceFilter.getDoubleValue("tickSize"))) * priceFilter.getDoubleValue("tickSize")
				+ priceFilter.getDoubleValue("minPrice");
	}
	
	public String toString() {
		return "{symbol:" + symbol + ",lotSize:" + lotSize.toString() + ",priceFilter:" + priceFilter + "}";
	}
}
