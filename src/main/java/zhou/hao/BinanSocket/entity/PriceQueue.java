package zhou.hao.BinanSocket.entity;

public class PriceQueue {
	private int start = 0;
	private int end = 0;
	private double[]data = null;
	private int num = 0;
	
	public PriceQueue(int num) {
		data = new double[num + 1];
	}
	
	public boolean isEmpty() {
		if(num == 0)	return true;
		else return false;
	}
	
	public void add(double x) {
		data[end++] = x;
		if(end == data.length) end = 0;
		if(num == data.length - 1) {
			if(++start == data.length)	start = 0;
		} else num++;
	}
	
	public double remove() {
		if(isEmpty())	return Double.MAX_VALUE;
		double x = data[start++];
		if(start == data.length) start = 0;
		num--;
		return x;
	}
	
	public double oldest() {
		if(isEmpty())	return 0;
		else return data[start];
	}
	
	public double newest() {
		if(isEmpty())	return 0;
		if(end == 0)	return data[data.length - 1];
		return data[end-1];
	}
	
	public double endDivideStart() {
		if(start == end)	return 1;
		if(end == 0)	return data[data.length - 1] / data[start];
		else return data[end-1] / data[start];
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(start == end) return "";
		else if(start < end) {
			for(int i=start; i<end; i++) sb.append(String.valueOf(data[i]) + ",");
		} else {
			for(int i=start; i<data.length; i++)	sb.append(String.valueOf(data[i]) + ",");
			for(int i=0; i<end; i++)	sb.append(String.valueOf(data[i]) + ",");
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		PriceQueue pq = new PriceQueue(7);
		for(int i=1; i<10; i++) {
			pq.add(i);
			System.out.println(pq + "   " + pq.oldest() + " " + pq.endDivideStart());
		}
		
	}
}
