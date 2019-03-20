package zhou.hao.BinanSocket.client.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JDialog;

import zhou.hao.BinanSocket.utility.Global;

public class MessageDialog {
	public MessageDialog(Frame owner, String title){
		JDialog dialog = new JDialog(owner, title, Boolean.FALSE);
		dialog.setLocation(Global.dimScreen.width/2 - Global.dimMessageDialog.width/2, Global.dimScreen.height/2 - Global.dimMessageDialog.height/2);
		dialog.setSize(Global.dimMessageDialog);
		dialog.setVisible(true);
	}
}
