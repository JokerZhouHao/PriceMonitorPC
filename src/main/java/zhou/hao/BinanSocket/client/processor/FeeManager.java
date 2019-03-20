package zhou.hao.BinanSocket.client.processor;

import java.util.LinkedList;

import zhou.hao.BinanSocket.utility.Global;

public class FeeManager {
	private LinkedList<Double> fees = new LinkedList<>();
	
	public FeeManager(Double total) {
		int n = (int)(total/Global.RATE_MAX_BASE_COIN);
		int i=0;
		for(; i<n && i<Global.RATE_MAX_NUM_BASE_COIN ; i++) {
			fees.add(Global.RATE_MAX_BASE_COIN);
			total -= Global.RATE_MAX_BASE_COIN;
		}
		if(i==Global.RATE_MAX_NUM_BASE_COIN)	return;
		else if(total > Global.RATE_MAX_BASE_COIN/400)	fees.add(total);
	}
	
	public Boolean hasFee() {
		return !fees.isEmpty();
	}
	
	public Double pullFee() {
		return fees.removeFirst();
	}
	
	public Double first() {
		return fees.getFirst();
	}
	
	public void addFee(double fee) {
		fees.add(fee);
	}
}
