package zhou.hao.BinanSocket.client.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

import zhou.hao.BinanSocket.entity.Item;
import zhou.hao.BinanSocket.utility.Global;

public class AllItemPanel extends JPanel {
	private List<Item> allItem = null;
	private List<OneItemPanel> allItemPanel = new ArrayList<>();
	
	public AllItemPanel(List<Item> items) {
		this.allItem = items;
		this.init();
	}
	
	private void init() {
		this.setLayout(new GridBagLayout());
		this.setBackground(Global.colorPanelBackground);
		
		if(this.allItem.size() == 0)	return;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = -1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(1, 1, 2, 1);
		
		for(int i=0; i<allItem.size() - 1; i++) {
			gbc.gridy++;
			allItemPanel.add(new OneItemPanel(allItem.get(i)));
			this.add(allItemPanel.get(i), gbc);
		}
		gbc.gridy++;
		gbc.weighty = 1;
		allItemPanel.add(new OneItemPanel(allItem.get(allItem.size() - 1)));
		this.add(allItemPanel.get(allItemPanel.size() - 1), gbc);
	}
	
	public void addOneItemPanel(Item item) {
//		item = new Item();
		
		GridBagConstraints gb = null;
		GridBagLayout gbl = (GridBagLayout)this.getLayout();
		if(allItemPanel.size() != 0) {
			Component[] comps = this.getComponents();
			gb = gbl.getConstraints(comps[comps.length - 1]);
			gb.weighty = 0;
			gbl.setConstraints(comps[comps.length - 1], gb);
		}
		
		if(null == gb) {
			gb = new GridBagConstraints();
			gb.gridx = 0;
			gb.gridy = -1;
			gb.gridwidth = 1;
			gb.gridheight = 1;
			gb.weightx = 1;
			gb.weighty = 0;
			gb.anchor = GridBagConstraints.NORTH;
			gb.fill = GridBagConstraints.HORIZONTAL;
			gb.insets = new Insets(1, 1, 2, 1);
		}
		gb.gridy++;
		gb.weighty = 1;
		gb.fill = GridBagConstraints.HORIZONTAL;
		allItemPanel.add(new OneItemPanel(item));
		this.add(allItemPanel.get(allItemPanel.size() - 1), gb);
		this.updateUI();
	}
	
	public void refreshUI() {
		for(OneItemPanel oip : allItemPanel)	oip.refreshUI();
	}
}
