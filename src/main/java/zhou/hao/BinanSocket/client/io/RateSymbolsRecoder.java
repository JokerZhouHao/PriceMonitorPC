package zhou.hao.BinanSocket.client.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.Set;

import zhou.hao.BinanSocket.utility.Global;
import zhou.hao.BinanSocket.utility.IOUtility;

public class RateSymbolsRecoder {
	public static Set<String>  load() throws Exception{
		BufferedReader br = IOUtility.getBR(Global.pathRateSymbols);
		String line = null;
		Set<String> symbols = new HashSet<>();
		while(null != (line = br.readLine()))
			symbols.add(line.trim());
		br.close();
		return symbols;
	}
	
	public static void reset() throws Exception{
		BufferedWriter bw = IOUtility.getBW(Global.pathRateSymbols, Boolean.FALSE);
		bw.close();
	}
	
	public static void addSymbol(String symbol) throws Exception{
		BufferedWriter bw = IOUtility.getBW(Global.pathRateSymbols, Boolean.TRUE);
		bw.write(symbol + "\n");
		bw.close();
	}
	
	public static void main(String[] args) throws Exception{
//		RateSymbolsRecoder.reset();
		RateSymbolsRecoder.addSymbol("zhou");
//		RateSymbolsRecoder.reset();
		System.out.println(RateSymbolsRecoder.load());
	}
}
