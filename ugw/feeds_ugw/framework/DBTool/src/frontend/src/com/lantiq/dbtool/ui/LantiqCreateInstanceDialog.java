/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTree;

import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.base.TreeBuilder;
import com.lantiq.dbtool.utils.FileUtils;
import com.lantiq.dbtool.utils.LantiqFileFilter;
import com.lantiq.dbtool.utils.SpringUtilities;

public class LantiqCreateInstanceDialog extends JDialog {

	private static final Logger logger = LogManager
			.getLogger(LantiqCreateInstanceDialog.class);

	JButton okButton, cancelButton;
	JPanel panel;
	JTextField controlFileTxtFld;
	JButton browseButton;
	JTextField dataFileTxtFld;
	JCheckBox mergeChkBox;
	LantiqFileFilter lff = new LantiqFileFilter(FileType.SCHEMA,
			"Schema Files Only");
	Boolean cancelled = false;

	boolean merge;
	String controlFilePath, dataFilePath;
	
	boolean dfileexists;

	public LantiqCreateInstanceDialog(boolean modal) {
		super();

		setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setSize(new Dimension(305, 25));

		okButton = new JButton("Ok");
		okButton.setPreferredSize(new Dimension(75, 25));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String control = controlFileTxtFld.getText().trim();
				if (control == null || control.equals("")
						|| control.length() == 0) {
					JOptionPane.showMessageDialog(null,
							"Please Select or Enter valid File", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				setControlFilePath(control);

				String data = dataFileTxtFld.getText().trim();
				setDataFilePath(data);

				if (mergeChkBox.isSelected()) {
					setMerge(true);
				} else {
					setMerge(false);
				}

				dispose();
			}
		});

		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(75, 25));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCancelled(true);
				dispose();
			}
		});

		JPanel btnPanel = new JPanel();
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);

		JLabel controlLabel = new JLabel("Schema");
		controlLabel.setPreferredSize(new Dimension(75, 25));
		controlFileTxtFld = new JTextField();
		controlFileTxtFld.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File instCreateselected = new File(controlFileTxtFld.getText()
						.trim());
				if (instCreateselected != null) {
					setDataFile(instCreateselected);
				}
			}
		});
		controlFileTxtFld.setPreferredSize(new Dimension(200, 25));

		browseButton = new JButton("...");
		browseButton.setPreferredSize(new Dimension(25, 25));
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtils fu = new FileUtils();
				File instCreateselected = fu.openFile(null, lff, true);
				if (instCreateselected != null) {
					setDataFile(instCreateselected);
				}
			}
		});

		JLabel dataLabel = new JLabel("Instance");
		dataLabel.setPreferredSize(new Dimension(75, 25));
		dataFileTxtFld = new JTextField();
		dataFileTxtFld.setPreferredSize(new Dimension(200, 25));
		dataFileTxtFld.setEnabled(true);
		dataFileTxtFld.setEditable(false);

		mergeChkBox = new JCheckBox("Merge");
		mergeChkBox.setPreferredSize(new Dimension(75, 25));
		mergeChkBox.setEnabled(false);

		panel.setLayout(new SpringLayout());

		panel.add(controlLabel);
		panel.add(controlFileTxtFld);
		panel.add(browseButton);

		panel.add(dataLabel);
		panel.add(dataFileTxtFld);
		panel.add(new JLabel(""));

		panel.add(mergeChkBox);
		panel.add(new JLabel(""));
		panel.add(new JLabel(""));

		SpringUtilities.makeCompactGrid(panel, 3, 3, 5, 5, 5, 5);

		add(panel, BorderLayout.PAGE_START);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		add(btnPanel, BorderLayout.PAGE_END);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		getRootPane().setDefaultButton(okButton);
		setModal(modal);
	}

	protected void setDataFile(File instCreateselected) {
		controlFileTxtFld.setText(instCreateselected.getAbsolutePath());
		String parentpath = instCreateselected.getParentFile()
				.getAbsolutePath();
		String ctrlfilename = instCreateselected.getName();
		String datafilename = ctrlfilename.replace("control", "data");
		File datafile = new File(parentpath + File.separator + datafilename);
		setDfileexists(false);
		if (datafile.exists()) {
			setDfileexists(true);
			mergeChkBox.setEnabled(true);
			mergeChkBox.setSelected(true);
			datafile = new File(parentpath + File.separator + "new_"
					+ datafilename);
		}
		dataFileTxtFld.setText(datafile.getAbsolutePath());
	}

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public String getControlFilePath() {
		return controlFilePath;
	}

	public void setControlFilePath(String controlFilePath) {
		this.controlFilePath = controlFilePath;
	}

	public String getDataFilePath() {
		return dataFilePath;
	}

	public void setDataFilePath(String dataFilePath) {
		this.dataFilePath = dataFilePath;
	}

	public ArrayList<Object> getInputs() {
		ArrayList<Object> returnvals = new ArrayList<>();
		returnvals.add(getControlFilePath());
		returnvals.add(getDataFilePath());
		returnvals.add(isMerge());
		returnvals.add(isDfileexists());

		return returnvals;
	}

	public Boolean getCancelled() {
		return cancelled;
	}

	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isDfileexists() {
		return dfileexists;
	}

	public void setDfileexists(boolean dfileexists) {
		this.dfileexists = dfileexists;
	}
}