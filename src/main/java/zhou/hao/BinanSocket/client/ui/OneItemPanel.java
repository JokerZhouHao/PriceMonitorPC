package zhou.hao.BinanSocket.client.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import zhou.hao.BinanSocket.entity.Item;
import zhou.hao.BinanSocket.utility.StringUtility;

public class OneItemPanel extends JPanel {
	private Item item = null;
	
	// 控件
	private JLabel lblHighPrice = null;
	private JLabel lblOpenPrice = null;
	private JLabel lblRate = null;
	private JLabel lblNowPrice = null;
	private JLabel lblStatus = null;
	
	
	public OneItemPanel(Item item) {
		this.item = item;
		init();
	}
	
	public void init() {
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = gbc.WEST;
		JLabel lblSymbol = new JLabel(item.symbol);
		lblSymbol.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblSymbol, gbc);
		
		// 状态
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 0;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		lblStatus = new JLabel("");
		lblStatus.setText(item.status());
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblStatus, gbc);
		
		// 最高价
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel lblHP = new JLabel("高");
		lblHP.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblHP, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		lblHighPrice = new JLabel("");
		lblHighPrice.setText(StringUtility.formatPrice(item.maxPrice));
		lblHighPrice.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblHighPrice, gbc);
		
		// 开盘价
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel lblOP = new JLabel("开");
		lblOP.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblOP, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		lblOpenPrice = new JLabel("");
		lblOpenPrice.setText(StringUtility.formatPrice(item.openPrice));
		lblOpenPrice.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblOpenPrice, gbc);
		
		// 现价
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel lblNP = new JLabel("现");
		lblNP.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblNP, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		lblNowPrice = new JLabel("");
		lblNowPrice.setText(StringUtility.formatPrice(item.nowPrice));
		lblNowPrice.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblNowPrice, gbc);
		
		// 比率
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel lblR = new JLabel("比率");
		lblR.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblR, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		lblRate = new JLabel("");
		lblRate.setText(StringUtility.formatRate(item.nowPrice / item.openPrice));
		lblRate.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblRate, gbc);
		
	}
	
	public void refreshUI() {
		lblHighPrice.setText(StringUtility.formatPrice(item.maxPrice));
		lblOpenPrice.setText(StringUtility.formatPrice(item.openPrice));
		lblRate.setText(StringUtility.formatRate(item.rate()));
		lblNowPrice.setText(StringUtility.formatPrice(item.nowPrice));
		lblStatus.setText(item.status());
	}
}
