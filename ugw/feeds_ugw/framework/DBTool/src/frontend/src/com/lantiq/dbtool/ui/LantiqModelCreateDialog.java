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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
import com.lantiq.dbtool.base.ModelType;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.base.TreeBuilder;
import com.lantiq.dbtool.utils.FileUtils;
import com.lantiq.dbtool.utils.LantiqFileFilter;
import com.lantiq.dbtool.utils.SpringUtilities;

public class LantiqModelCreateDialog extends JDialog {

	private static final Logger logger = LogManager
			.getLogger(LantiqModelCreateDialog.class);

	private static ArrayList<String> modelCategories;
	JButton okButton, cancelButton;
	JPanel panel;
	JTextField fileTxtFld;
	JButton browseButton;
	JLabel modelTypeLbl;
	JComboBox modelTypeCombo;
	//JCheckBox chkautoDelInst;

	LantiqFileFilter lff1 = new LantiqFileFilter(FileType.SCHEMA,
			"Schema Files Only");

	LantiqFileFilter lff2 = new LantiqFileFilter(FileType.MODEL_SCHEMA,
			"Model Schema Files Only");

	Boolean cancelled = false;
	boolean model = false;

	String filePath;
	ModelType modelType;
	//boolean autoDelInst = true;
	
	static{
		loadModelCategories();
	}

	public LantiqModelCreateDialog(boolean modal, boolean model) {
		super();
		setModel(model);

		setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setSize(new Dimension(305, 25));

		okButton = new JButton("Ok");
		okButton.setPreferredSize(new Dimension(75, 25));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String control = fileTxtFld.getText().trim();
				if (control == null || control.equals("")
						|| control.length() == 0) {
					JOptionPane.showMessageDialog(null,
							"Please Select or Enter valid File", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				setFilePath(control);

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

		JLabel fileLabel;
		if (isModel()) {
			fileLabel = new JLabel("Schema File");
			fileLabel.setPreferredSize(new Dimension(75, 25));
		} else {
			fileLabel = new JLabel("Model Schema File");
			fileLabel.setPreferredSize(new Dimension(165, 25));
		}

		fileTxtFld = new JTextField();
		fileTxtFld.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = new File(fileTxtFld.getText().trim());
				setFilePath(file.getAbsolutePath());
			}
		});
		fileTxtFld.setPreferredSize(new Dimension(200, 25));

		browseButton = new JButton("...");
		browseButton.setPreferredSize(new Dimension(25, 25));
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileUtils fu = new FileUtils();
				File file;
				if (isModel())
					file = fu.openFile(null, lff1, true);
				else
					file = fu.openFile(null, lff2, true);

				if (file != null) {
					setFilePath(file.getAbsolutePath());
					fileTxtFld.setText(file.getAbsolutePath());
				}

			}
		});

		if (isModel()) {
			modelTypeLbl = new JLabel("Model Type");
			modelTypeLbl.setPreferredSize(new Dimension(75, 25));

			String[] array = modelCategories.toArray(new String[modelCategories.size()]);
			modelTypeCombo = new JComboBox<String>(array);
			
			modelTypeCombo.setSelectedIndex(0);
			String item = (String)modelTypeCombo.getSelectedItem();
			setModelType(ModelType.valueOf(item));
			
			modelTypeCombo.setPreferredSize(new Dimension(200, 25));
			modelTypeCombo.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						String item = (String)modelTypeCombo.getSelectedItem();
						List<ModelType> lst = Arrays.asList(ModelType.values());
						if(lst.toString().indexOf(item) > -1)
							setModelType(ModelType.valueOf(item));
						else
							setModelType(ModelType.UK);
				    }
				}
			});
		}/* else {
			chkautoDelInst = new JCheckBox("Auto Delete Instance(s)");
			chkautoDelInst.setPreferredSize(new Dimension(135, 25));
			chkautoDelInst.setSelected(true);
			chkautoDelInst.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (chkautoDelInst.isSelected()) {
						setAutoDelInst(true);
					} else {
						setAutoDelInst(false);
					}
				}
			});
		}*/

		panel.setLayout(new SpringLayout());

		panel.add(fileLabel);
		panel.add(fileTxtFld);
		panel.add(browseButton);

		if (isModel()) {
			panel.add(modelTypeLbl);
			panel.add(modelTypeCombo);
			panel.add(new JLabel(""));
		} else {
			panel.add(new JLabel(""));//chkautoDelInst
			panel.add(new JLabel(""));
			panel.add(new JLabel(""));
		}

		SpringUtilities.makeCompactGrid(panel, 2, 3, 5, 5, 5, 5);

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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public ArrayList<Object> getInputs() {
		ArrayList<Object> returnvals = new ArrayList<>();
		returnvals.add(getFilePath());
		if (model) {
			returnvals.add(getModelType());
		} else {
			returnvals.add(true);//getAutoDelInst()
		}
		return returnvals;
	}

	public Boolean getCancelled() {
		return cancelled;
	}

	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
	}

	public void setModelType(ModelType type) {
		this.modelType = type;
	}

	public ModelType getModelType() {
		return this.modelType;
	}

	/*public boolean getAutoDelInst() {
		return this.autoDelInst;
	}

	public void setAutoDelInst(boolean autodel) {
		this.autoDelInst = autodel;
	}*/

	public boolean isModel() {
		return model;
	}

	public void setModel(boolean model) {
		this.model = model;
	}

	public static void loadModelCategories() {
		logger.log(Level.INFO, null, "Loading Model Category");
		File file;
		modelCategories = new ArrayList<>();
		try {
			file = new File(LantiqFrame.configpath
					+ "modelCategory.properties");
			StringBuffer strbuff = new StringBuffer();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (!line.equals("") && !line.startsWith("#"))
					modelCategories.add(line);
			}
			br.close();
		} catch (IOException ex) {
			logger.log(Level.DEBUG, "Error Occured : Load Properties", ex);
		}
	}
}