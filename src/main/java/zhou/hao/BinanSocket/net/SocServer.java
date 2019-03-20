package zhou.hao.BinanSocket.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import zhou.hao.BinanSocket.utility.IOUtility;

public class SocServer{
	public static void main(String[] args) throws IOException {
        int port = 7000;
        if(args.length > 0) port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("> start service <" + serverSocket.getInetAddress() + ", " + serverSocket.getLocalPort() + "> success !");
        
        // 多线程
        ExecutorService exec = Executors.newCachedThreadPool();
        int clientNo = 1;
        
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                exec.execute(new SingleServer(socket, clientNo));
                clientNo++;
            }

        } finally {
            serverSocket.close();
        }

    }
}

class SingleServer implements Runnable {

    private Socket socket;
    private int clientNo;
    private MHttp mhttp = null;
    
    public SingleServer(Socket socket, int clientNo) {
        this.socket = socket;
        this.clientNo = clientNo;
        this.mhttp = new MHttp();
    }

    @Override
    public void run() {
    	InputStream sockIs = null;
    	OutputStream sockOs = null;
    	String request = null;
    	String []tempArr = null;
    	String postParams[] = null;
    	
    	// request format : 8()请求类型()网址()post参数1[格式:name((value]]()post参数对2()post参数3 . . . 
        try {
        	sockIs = socket.getInputStream();
        	sockOs = socket.getOutputStream();
            
            while(Boolean.TRUE) {
            	request = IOUtility.readAvailString(sockIs);
            	tempArr = request.split("\\(\\)");
            	if(!tempArr[0].startsWith("8")) break; 	// 必须要设置的标签才可以访问
            	
//            	System.out.println("> 收到client" + clientNo + " : " + request);
            	
            	if(tempArr[1].compareToIgnoreCase("get")==0) {
            		IOUtility.writeString(sockOs, mhttp.Get(tempArr[2]));
            	} else {	// post
            		Map<String, String> pms = new HashMap<>();
            		for(int i=3; i<tempArr.length; i++) {
            			postParams = tempArr[i].split("\\(\\(");
            			pms.put(postParams[0].trim(), postParams[1].trim());
            		}
            		IOUtility.writeString(sockOs, mhttp.Post(tempArr[2], pms));
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            System.out.println("> client" + clientNo + "关闭");
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
