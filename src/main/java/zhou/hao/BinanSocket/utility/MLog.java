package zhou.hao.BinanSocket.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class MLog {
    public static void write(String info){
        try {
            BufferedWriter bw = IOUtility.getBW(Global.pathLog, Boolean.TRUE);
            info = StringUtility.getLogDate() + info;
            bw.write(info);
            bw.close();
        }catch (Exception e){}
    }

    public static void writeLine(String info){
        write(info + "\n");
    }

    public static void writeLineCTA(String info) {
    	write(info + "\n");
    	if(null != Global.logTA)	Global.logTA.append(StringUtility.getLogDate() + info + "\n");
    }
    
    public static String loadLog(){
        try {
            BufferedReader br = IOUtility.getBR(Global.pathLog);
            StringBuffer sb = new StringBuffer();
            String line = null;
            while(null != (line = br.readLine())){
                sb.append(line);
                sb.append('\n');
            }
            br.close();
            return sb.toString();
        } catch (Exception e){}
        return "";
    }
}
