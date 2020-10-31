/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.tree.DefaultTreeModel;

import org.jdesktop.swingx.JXTree;

import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.base.TreeBuilder;
import com.lantiq.dbtool.utils.SpringUtilities;

public class LantiqAboutDialog extends JDialog{

	JButton okButton;

	JPanel panel;
	
	public LantiqAboutDialog(String version) {
		super();
		
		setTitle("About DBTool");
		setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setSize(new Dimension(305, 25));

		okButton = new JButton("Ok");
		okButton.setPreferredSize(new Dimension(75, 25));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();				
			}
		});
		
		JPanel btnPanel = new JPanel();
		btnPanel.add(okButton);
		
		panel.setLayout(new SpringLayout());
		
		String[] arrversion = version.split("-");
				
		JLabel pkgversionLabel = new JLabel(arrversion[0].split(":")[0]);
		JLabel pkgversionValueLabel = new JLabel(arrversion[0].split(":")[1]);
		
		JLabel exeversionLabel = new JLabel(arrversion[1].split(":")[0]);
		JLabel exeversionValueLabel = new JLabel(arrversion[1].split(":")[1]);

		panel.add(pkgversionLabel);
		panel.add(pkgversionValueLabel);
		panel.add(exeversionLabel);
		panel.add(exeversionValueLabel);
		
		SpringUtilities.makeGrid(panel, 2, 2, 5, 5, 5, 5);
		
		add(panel, BorderLayout.PAGE_START);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(btnPanel, BorderLayout.PAGE_END);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		getRootPane().setDefaultButton(okButton);
		setModal(true);
	}		
}