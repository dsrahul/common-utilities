package com.sample.examples.swing.qframe;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Example from Chapter 3
 * 
 * Simple object to prompt for user id/password.
 * 
 * @author Jeff Heaton
 * @version 1.0
 */
@Component
public class SecurePrompt extends javax.swing.JDialog {
	@Autowired
	private ResultSetTableModelFactory factory; // A factory to obtain our table data
	
	private static final long serialVersionUID = 1L;
	public SecurePrompt() {
		super();
		setModal(true);
		//{{INIT_CONTROLS
		setTitle("Security");
		getContentPane().setLayout(null);
		setSize(403, 129);
		setVisible(false);
		JLabel1.setText("User ID :");
		getContentPane().add(JLabel1);
		JLabel1.setBounds(12, 12, 48, 24);
		JLabel2.setText("Password :");
		getContentPane().add(JLabel2);
		JLabel2.setBounds(12, 48, 72, 24);
		resetUIDTextField();
		resetPWDTextField();
		getContentPane().add(_uid);
		_uid.setBounds(82, 12, 314, 24);
		_ok.setText("OK");
		getContentPane().add(_ok);
		_ok.setBounds(60, 84, 84, 24);
		getContentPane().add(_pwd);
		_pwd.setBounds(82, 48, 314, 24);
		_cancel.setText("Cancel");
		getContentPane().add(_cancel);
		_cancel.setBounds(264, 84, 84, 24);
		JLabelMSG.setBounds(12, 100, 300, 24);
		JLabelMSG.setText("");
		JLabelMSG.setForeground(Color.RED);
		getContentPane().add(JLabelMSG);
		//}}

		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		_ok.addActionListener(lSymAction);
		_cancel.addActionListener(lSymAction);
		_uid.addActionListener(lSymAction);
		_pwd.addActionListener(lSymAction);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		//}}
	}

	private void resetUIDTextField() {
		_uid.setText(StringUtils.EMPTY);
		_uid.setFont(font);
		_uid.setForeground(Color.BLUE);
	}

	private void resetPWDTextField() {
		_pwd.setText(StringUtils.EMPTY);
		_pwd.setFont(font);
		_pwd.setForeground(Color.BLUE);
	}

	public void setVisible(boolean b) {
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	public void addNotify() {
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();

	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	
	javax.swing.JLabel JLabelMSG = new javax.swing.JLabel();

	/**
	 * The user ID entered.
	 */
	javax.swing.JTextField _uid = new javax.swing.JTextField();

	/**
	 */
	javax.swing.JButton _ok = new javax.swing.JButton();

	/**
	 * The password is entered.
	 */
	javax.swing.JPasswordField _pwd = new javax.swing.JPasswordField();

	javax.swing.JButton _cancel = new javax.swing.JButton();
	private static final Font font = new Font("Verdana", Font.BOLD, 12);

	//}}

	class SymAction implements java.awt.event.ActionListener {

		public void actionPerformed(java.awt.event.ActionEvent event) {
			Object object = event.getSource();
			if (object == _ok || object == _uid || object == _pwd)
				Ok_actionPerformed(event);
			else if (object == _cancel)
				Cancel_actionPerformed(event);
		}
	}

	/**
	 * Called when ok is clicked.
	 * 
	 * @param event
	 */
	void Ok_actionPerformed(java.awt.event.ActionEvent event) {
		
		final String uid = _uid.getText();
		final String pwd = _pwd.getText();
		boolean continuePrompt = false;
		if (StringUtils.isNotEmpty(StringUtils.trimToEmpty(uid)) && StringUtils.isNotEmpty(StringUtils.trimToEmpty(pwd))) {			
			try {
				factory.checkDBCredentials(uid, pwd);
			} catch (SQLException e) {
				continuePrompt = true;
			}
		} else {
			continuePrompt = true;
		}
		setVisible(continuePrompt);
		if (continuePrompt) {
			JLabelMSG.setText("Invalid User id / password");
		} else {
			JLabelMSG.setText(StringUtils.EMPTY);
		}
		resetPWDTextField();
		resetUIDTextField();
	}

	/**
	 * Called when cancel is clicked.
	 * 
	 * @param event
	 */
	void Cancel_actionPerformed(java.awt.event.ActionEvent event) {
		System.exit(0);
	}
	
	
}