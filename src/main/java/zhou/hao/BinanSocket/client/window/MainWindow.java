package zhou.hao.BinanSocket.client.window;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.management.loading.MLet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import zhou.hao.BinanSocket.client.processor.PriceMonitor;
import zhou.hao.BinanSocket.client.ui.AllItemPanel;
import zhou.hao.BinanSocket.client.ui.MessageDialog;
import zhou.hao.BinanSocket.entity.Item;
import zhou.hao.BinanSocket.utility.Global;
import zhou.hao.BinanSocket.utility.MLog;


public class MainWindow {
	private static JFrame frame = null;
	private Font globalFont = new Font("楷体", Font.PLAIN, 17);
	
	private TrayIcon trayIcon = null;
	private static Image icon = null;
	
	private JScrollPane centerSPane = null;
	private AllItemPanel allItemPanel = null;
	private TextArea logTA = new TextArea(7, 30);
	
	private PriceMonitor priceMonitor = null;
	
	public MainWindow() throws Exception{
		// 设置全局字体
		initGobalFont(globalFont);
		logTA.setFont(new Font("楷体", Font.PLAIN, 14));
		Global.logTA = logTA;
		
		// 设置设置frame状态
		this.frame = new JFrame("PRICEM");
		this.frame.setLocation(Global.dimScreen.width/2 - Global.dimMainWindow.width/2, Global.dimScreen.height/2 - Global.dimMainWindow.height/2);
		this.frame.setSize(Global.dimMainWindow);
//		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		// 设置图标
		icon = Toolkit.getDefaultToolkit().getImage(Global.pathImgs + "icon1.png");
		this.frame.setIconImage(icon);
		this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		// 为图标添加右键单击事件
		// 创建弹出菜单
		PopupMenu popup = new PopupMenu();
		MenuItem exitItem = new MenuItem("exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		popup.add(exitItem);
		trayIcon = new TrayIcon(icon, "M", popup);
		trayIcon.setImageAutoSize(Boolean.TRUE);
		// 为图标添加点击响应
		trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(true);
			}
		});
		try {
			SystemTray.getSystemTray().add(trayIcon);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// 布局
		BorderLayout bLayout = new BorderLayout();
		this.frame.setLayout(bLayout);
		
		// 价格监视
		priceMonitor = new PriceMonitor(this);
		
		// 中心面板
		allItemPanel = new AllItemPanel(priceMonitor.allItems());
		centerSPane = new JScrollPane(allItemPanel);
		centerSPane.getVerticalScrollBar().setUnitIncrement(7);
		this.frame.add(centerSPane, BorderLayout.CENTER);
		
		// 下方日志框
		this.frame.add(logTA, BorderLayout.SOUTH);
		
		MLog.writeLineCTA("start success");
		
		// 启动价格监视线程
		new Thread(priceMonitor).start();
	}
	
	public JFrame frame() {
		return frame;
	}
	
	
	/**
	 * 设置全局字体
	 * @param font
	 */
	public void initGobalFont(Font font) {  
	    FontUIResource fontResource = new FontUIResource(font);  
	    for(Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {  
	        Object key = keys.nextElement();  
	        Object value = UIManager.get(key);  
	        if(value instanceof FontUIResource) {  
//	            System.out.println(key);  
	            UIManager.put(key, fontResource);  
	        }  
	    }
	} 
	
	public void refreshUI() {
		this.allItemPanel.refreshUI();
	}
	
	/**
	 * show
	 */
	public void show() {
		this.frame.setVisible(true);
	}
	
	public void addItem(Item item) {
		this.allItemPanel.addOneItemPanel(item);
	}
	
	public static void main(String[] args) throws Exception{
//		List<Item> items = new ArrayList<>();
//		Item tIm = new Item("ETH/USDT");
//		tIm.maxPrice = 100;
//		tIm.openPrice = 10;
//		tIm.nowPrice = 50;
//		items.add(tIm);
//		items.add(tIm);
//		items.add(tIm);
//		items.add(tIm);
//		items.add(tIm);
//		items.add(tIm);
		MainWindow mw = new MainWindow();
		mw.show();
		
//		while(true) {
//			Thread.sleep(2000);
//			mw.allItemPanel.addOneItemPanel(null);
//			System.out.println("tick");
//		}
	}
}
