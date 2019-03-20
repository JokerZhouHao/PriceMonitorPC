package zhou.hao.BinanSocket.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Base64.Decoder;

import org.apache.http.util.ByteArrayBuffer;

public class IOUtility {
	public static BufferedReader getBR(InputStream ist, String charset) throws Exception{
		return new BufferedReader(new InputStreamReader(new BufferedInputStream(ist), charset));
	}
	
	public static BufferedReader getBR(InputStream ist) throws Exception{
		return new BufferedReader(new InputStreamReader(new BufferedInputStream(ist), "utf-8"));
	}
	
	public static BufferedReader getBR(String path) throws Exception{
		return new BufferedReader(new FileReader(path));
	}
	
	public static BufferedWriter getBW(String path, boolean append) throws Exception{
		return new BufferedWriter(new FileWriter(path, append));
	}
	
	public static String readAvailString(InputStream ist) throws Exception {
		byte[] bys = new byte[1024];
		int n = 0, len = 0;
		StringBuffer sb = new StringBuffer();
		while(true) {
			len = ist.read(bys, 0, bys.length);
			sb.append(new String(bys, 0, len, "utf-8"));
			n = ist.available();
			if(0==n)	break;
		}
		return sb.toString();
	}
	
	public static void writeString(OutputStream ost, String str) throws Exception{
		ost.write(str.getBytes("utf-8"));
		ost.flush();
	}
}
