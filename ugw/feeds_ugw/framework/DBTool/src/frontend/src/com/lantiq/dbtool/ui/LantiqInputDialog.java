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
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree;

import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.base.InvisibleNode;
import com.lantiq.dbtool.base.InvisibleTreeModel;
import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.base.TreeBuilder;
import com.lantiq.dbtool.utils.SpringUtilities;

public class LantiqInputDialog extends JDialog implements ActionListener {

	JButton okButton, cancelButton;
	boolean add;
	LantiqMutableTreeNode node;
	String command;

	JPanel panel;
	JTextField nameTextField;
	JTextField valueTextField;
	JTextField minEntriesTextField;
	JTextField maxEntriesTextField;

	JCheckBox multiInsChkBox;
	JRadioButton readOnlyRadio, readWriteRadio;
	LantiqComboBox syntaxCombo;

	FileType fileType;

	JXTree tree;

	boolean cancelled;

	public LantiqInputDialog(LantiqMutableTreeNode node, boolean add,
			String command, boolean modal, FileType type) {
		super();
		this.add = add;
		this.command = command;
		this.fileType = type;
		setNode(node);

		setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setSize(new Dimension(305, 25));

		okButton = new JButton("Ok");
		okButton.setPreferredSize(new Dimension(75, 25));
		okButton.addActionListener(this);

		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.setPreferredSize(new Dimension(75, 25));
		cancelButton.addActionListener(this);

		JPanel btnPanel = new JPanel();
		btnPanel.add(okButton);
		btnPanel.add(cancelButton);

		buildComponents();

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
		setModal(modal);
	}

	public void buildComponents() {
		if (add) {
			switch (command) {
			case "Add Service":
				createAddSrvcDlg();
				break;
			case "Add Object":
				createAddObjectDlg();
				break;
			case "Add Parameter":
				createAddParameterDlg();
				break;
			case "Add Attribute":
				createAddAttributeDlg();
				break;
			}
		} else {
			if (getNode().getNodetype() == NodeType.SERVICE
					|| getNode().getNodetype() == NodeType.OBJECT) {
				createEditObjOrSrvcDlg();
			} else if (getNode().getNodetype() == NodeType.PARAM
					|| getNode().getNodetype() == NodeType.ATTRIB) {
				createEditParamOrAttribDlg();
			}
		}
	}

	private void createAddSrvcDlg() {
		JLabel nameLabel = new JLabel();

		JLabel starLabel = new JLabel("*");
		starLabel.setForeground(Color.RED);

		setTitle("Add Service");
		nameLabel.setText("Name");
		okButton.setActionCommand("AddService");

		nameLabel.setPreferredSize(new Dimension(100, 25));
		starLabel.setPreferredSize(new Dimension(25, 25));

		nameTextField = new JTextField();
		nameTextField.setPreferredSize(new Dimension(150, 25));

		panel.setLayout(new SpringLayout());

		panel.add(nameLabel);
		panel.add(nameTextField);
		panel.add(starLabel);

		SpringUtilities.makeCompactGrid(panel, 1, 3, 5, 5, 5, 5);
	}

	private void createAddObjectDlg() {
		JLabel nameLabel = new JLabel();
		JLabel buffer1 = new JLabel();
		JLabel buffer2 = new JLabel();
		JLabel buffer3 = new JLabel();

		JLabel starLabel = new JLabel("*");
		starLabel.setForeground(Color.RED);

		JLabel starLabel1 = new JLabel("*");
		starLabel.setForeground(Color.RED);

		JLabel starLabel2 = new JLabel("*");
		starLabel.setForeground(Color.RED);

		setTitle("Add Object");
		nameLabel.setText("Name");
		okButton.setActionCommand("AddObject");

		nameLabel.setPreferredSize(new Dimension(125, 25));

		nameTextField = new JTextField();
		nameTextField.setPreferredSize(new Dimension(150, 25));

		multiInsChkBox = new JCheckBox("Multi Instance");
		multiInsChkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (multiInsChkBox.isSelected()) {
					maxEntriesTextField.setEnabled(true);
				} else {
					maxEntriesTextField.setEnabled(false);
				}
			}
		});
		multiInsChkBox.setPreferredSize(new Dimension(125, 25));

		ButtonGroup btnGrp = new ButtonGroup();

		readOnlyRadio = new JRadioButton("Read Only");
		multiInsChkBox.setPreferredSize(new Dimension(125, 25));

		readWriteRadio = new JRadioButton("Read Write");
		multiInsChkBox.setPreferredSize(new Dimension(125, 25));

		JLabel minEntriesLabel = new JLabel("minEntries");
		minEntriesLabel.setPreferredSize(new Dimension(125, 25));

		JLabel maxEntriesLabel = new JLabel("maxEntries");
		maxEntriesLabel.setPreferredSize(new Dimension(125, 25));

		minEntriesTextField = new JTextField("1");
		minEntriesTextField.setPreferredSize(new Dimension(125, 25));
		maxEntriesTextField = new JTextField("1");
		maxEntriesTextField.setPreferredSize(new Dimension(125, 25));
		maxEntriesTextField.setEnabled(false);

		buffer1.setPreferredSize(new Dimension(25, 25));
		buffer2.setPreferredSize(new Dimension(25, 25));
		buffer3.setPreferredSize(new Dimension(25, 25));

		btnGrp.add(readOnlyRadio);
		btnGrp.add(readWriteRadio);
		readWriteRadio.setSelected(true);

		panel.setLayout(new SpringLayout());

		panel.add(nameLabel);
		panel.add(nameTextField);
		panel.add(starLabel);
		panel.add(multiInsChkBox);
		panel.add(buffer1);
		panel.add(buffer2);
		panel.add(readOnlyRadio);
		panel.add(readWriteRadio);
		panel.add(buffer3);
		panel.add(minEntriesLabel);
		panel.add(minEntriesTextField);
		panel.add(starLabel1);
		panel.add(maxEntriesLabel);
		panel.add(maxEntriesTextField);
		panel.add(starLabel2);

		SpringUtilities.makeCompactGrid(panel, 5, 3, 5, 5, 5, 5);
	}

	private void createAddParameterDlg() {
		JLabel nameLabel = new JLabel();
		JLabel syntaxLabel = new JLabel();
		JLabel valueLabel = new JLabel();

		JLabel buffer1 = new JLabel();

		JLabel starLabel = new JLabel("*");
		starLabel.setForeground(Color.RED);

		JLabel starLabel1 = new JLabel("*");
		starLabel1.setForeground(Color.RED);

		setTitle("Add Parameter");
		nameLabel.setText("Name");
		okButton.setActionCommand("AddParameter");

		nameLabel.setPreferredSize(new Dimension(100, 25));

		nameTextField = new JTextField();
		nameTextField.setPreferredSize(new Dimension(150, 25));

		valueLabel.setText("Value");
		valueLabel.setPreferredSize(new Dimension(100, 25));

		valueTextField = new JTextField();
		valueTextField.setPreferredSize(new Dimension(150, 25));

		syntaxLabel.setText("Syntax");
		syntaxLabel.setPreferredSize(new Dimension(100, 25));

		syntaxCombo = new LantiqComboBox(DataTypes.datatypes);
		syntaxCombo.setWide(true);
		syntaxCombo.setPreferredSize(new Dimension(150, 25));

		buffer1.setPreferredSize(new Dimension(25, 25));

		panel.setLayout(new SpringLayout());

		panel.add(nameLabel);
		panel.add(nameTextField);
		panel.add(starLabel);
		panel.add(syntaxLabel);
		panel.add(syntaxCombo);
		panel.add(starLabel1);
		panel.add(valueLabel);
		panel.add(valueTextField);
		panel.add(buffer1);

		SpringUtilities.makeCompactGrid(panel, 3, 3, 5, 5, 5, 5);
	}

	private void createAddAttributeDlg() {
		JLabel nameLabel = new JLabel();
		JLabel valueLabel = new JLabel();

		setTitle("Add Attribute");
		nameLabel.setText("Name");
		okButton.setActionCommand("AddAttribute");

		nameLabel.setPreferredSize(new Dimension(100, 25));

		nameTextField = new JTextField();
		nameTextField.setPreferredSize(new Dimension(150, 25));

		valueLabel.setText("Value");
		valueLabel.setPreferredSize(new Dimension(100, 25));

		valueTextField = new JTextField();
		valueTextField.setPreferredSize(new Dimension(150, 25));

		JLabel starLabel = new JLabel("*");
		starLabel.setForeground(Color.RED);

		JLabel starLabel1 = new JLabel("*");
		starLabel1.setForeground(Color.RED);

		panel.setLayout(new SpringLayout());

		panel.add(nameLabel);
		panel.add(nameTextField);
		panel.add(starLabel);
		panel.add(valueLabel);
		panel.add(valueTextField);
		panel.add(starLabel1);

		SpringUtilities.makeCompactGrid(panel, 2, 3, 5, 5, 5, 5);
	}

	private void createEditObjOrSrvcDlg() {
		JLabel nameLabel = new JLabel();
		JLabel nameValueLabel = new JLabel();

		if (getNode().getNodetype() == NodeType.SERVICE) {
			setTitle("Edit Service");
			nameLabel.setText("Name");
			okButton.setActionCommand("EditService");
		} else if (getNode().getNodetype() == NodeType.OBJECT) {
			setTitle("Edit Object");
			nameLabel.setText("Name");
			okButton.setActionCommand("EditObject");
		}

		nameLabel.setPreferredSize(new Dimension(100, 25));

		String name = (String) getNode().getUserObject().toString();
		if (fileType == FileType.INSTANCE || fileType == FileType.INSTANCE_ID) {
			nameValueLabel.setText(name);
			nameValueLabel.setPreferredSize(new Dimension(150, 25));
		} else {
			nameTextField = new JTextField(name);
			nameTextField.setPreferredSize(new Dimension(150, 25));
		}

		JLabel starLabel = new JLabel("*");
		starLabel.setForeground(Color.RED);

		panel.setLayout(new SpringLayout());

		panel.add(nameLabel);
		if (fileType == FileType.INSTANCE || fileType == FileType.INSTANCE_ID) {
			panel.add(nameValueLabel);
			SpringUtilities.makeCompactGrid(panel, 1, 2, 5, 5, 5, 5);
		} else {
			panel.add(nameTextField);
			panel.add(starLabel);
			SpringUtilities.makeCompactGrid(panel, 1, 3, 5, 5, 5, 5);
		}
	}

	private void createEditParamOrAttribDlg() {
		JLabel nameLabel = new JLabel();
		JLabel nameValueLabel = new JLabel();
		JLabel valueLabel = new JLabel();

		if (getNode().getNodetype() == NodeType.PARAM) {
			setTitle("Edit Parameter");
			nameLabel.setText("Name");
			valueLabel.setText("Value");
			okButton.setActionCommand("EditParameter");
		} else if (getNode().getNodetype() == NodeType.ATTRIB) {
			setTitle("Edit Attribute");
			nameLabel.setText("Name");
			valueLabel.setText("Value");
			okButton.setActionCommand("EditAttribute");
		}

		nameLabel.setPreferredSize(new Dimension(100, 25));

		String name = (String) getNode().getUserObject().toString();
		if (fileType == FileType.INSTANCE || fileType == FileType.INSTANCE_ID) {
			nameValueLabel.setText(name);
			nameValueLabel.setPreferredSize(new Dimension(150, 25));
		} else {
			nameTextField = new JTextField(name);
			nameTextField.setPreferredSize(new Dimension(150, 25));
		}

		String value = "";
		if (getNode().getValue() != null) {
			value = getNode().getValue().toString();
		}
		valueTextField = new JTextField(value);
		valueTextField.setPreferredSize(new Dimension(150, 25));

		JLabel starLabel = new JLabel("*");
		starLabel.setForeground(Color.RED);

		panel.setLayout(new SpringLayout());

		panel.add(nameLabel);
		if (fileType == FileType.INSTANCE || fileType == FileType.INSTANCE_ID) {
			panel.add(nameValueLabel);
			starLabel.setText("");
		} else {
			panel.add(nameTextField);

		}
		panel.add(starLabel);
		panel.add(valueLabel);
		panel.add(valueTextField);
		panel.add(new JLabel(""));

		SpringUtilities.makeCompactGrid(panel, 2, 3, 5, 5, 5, 5);

	}

	public boolean editService(boolean add) {
		String srvcname = nameTextField.getText().trim();
		if (srvcname == null || srvcname.equals("") || srvcname.length() < 0) {
			JOptionPane.showMessageDialog(null,
					"Please Enter Valid Service Name", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (!add)
			node.setUserObject(srvcname);
		else {
			LantiqMutableTreeNode newSrvc = new LantiqMutableTreeNode(srvcname);
			newSrvc.setNodetype(NodeType.SERVICE);

			int index = TreeBuilder.getIndexOfLast(node, newSrvc.getNodetype());

			((DefaultTreeModel) tree.getModel()).insertNodeInto(newSrvc, node,
					index + 1);
			tree.expandRow(index+1);
		}

		return true;
	}

	public boolean editObject() {
		String objname = nameTextField.getText().trim();
		if (objname == null || objname.equals("") || objname.length() < 0) {
			JOptionPane.showMessageDialog(this,
					"Please Enter Valid Object Name", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		node.setUserObject(objname);

		((DefaultTreeModel) tree.getModel()).nodesChanged(node.getParent(),
				new int[] { ((InvisibleNode) node.getParent()).getIndex(node) });
		return true;
	}

	public boolean addObject() {
		String objname = nameTextField.getText().trim();
		if (objname == null || objname.equals("") || objname.length() < 0) {
			JOptionPane.showMessageDialog(this,
					"Please Enter Valid Object Name", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		LantiqMutableTreeNode attrib1 = new LantiqMutableTreeNode("multiInst");
		attrib1.setNodetype(NodeType.ATTRIB);
		if (multiInsChkBox.isSelected())
			attrib1.setValue("yes");
		else
			attrib1.setValue("no");

		LantiqMutableTreeNode attrib2 = new LantiqMutableTreeNode("access");
		attrib2.setNodetype(NodeType.ATTRIB);
		if (readOnlyRadio.isSelected())
			attrib2.setValue("readOnly");
		else if (readWriteRadio.isSelected())
			attrib2.setValue("readWrite");

		String minEntries = minEntriesTextField.getText().trim();
		int iMinEntries = 0;
		if (minEntries == null || minEntries.equals("")
				|| minEntries.length() < 0) {
			JOptionPane
					.showMessageDialog(
							this,
							"Please Enter Valid Minimum Entries. Valid Range for Maximum Entries is [0-256]",
							"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			try {
				iMinEntries = Integer.parseInt(minEntries);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						"Valid Range for Minimum Entries is [0-256].", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			if (iMinEntries < 0 || iMinEntries > 256) {
				JOptionPane.showMessageDialog(this,
						"Valid Range for Minimum Entries is [0-256].", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		String maxEntries = maxEntriesTextField.getText().trim();
		int iMaxEntries = 0;
		boolean unbounded = false;
		if (maxEntries == null || maxEntries.equals("")
				|| maxEntries.length() < 0) {
			JOptionPane
					.showMessageDialog(
							this,
							"Please Enter Valid Maximum Entries. Valid Range for Maximum Entries is [0-256] or unbounded.",
							"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (maxEntries.equals("unbounded")) {
			unbounded = true;
		} else {
			try {
				iMaxEntries = Integer.parseInt(maxEntries);
			} catch (NumberFormatException e) {
				JOptionPane
						.showMessageDialog(
								this,
								"Valid Range for Maximum Entries is [1-256] or unbounded.",
								"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			if (multiInsChkBox.isSelected()) {
				if (iMaxEntries < 1 || iMaxEntries > 256) {
					JOptionPane
							.showMessageDialog(
									this,
									"Valid Range for Maximum Entries is [1-256] or unbounded.",
									"Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} else {
				if (iMaxEntries > 1 && iMaxEntries <= 1) {
					JOptionPane
							.showMessageDialog(
									this,
									"Valid Value for Maximum Entries is [1] if Multi Instance is Not Selected.",
									"Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}

		if (!unbounded) {
			if (iMinEntries > iMaxEntries) {
				JOptionPane
						.showMessageDialog(
								this,
								"Minimum Entries should be lesser than Maximum Entries.",
								"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		LantiqMutableTreeNode attrib3 = new LantiqMutableTreeNode("minEntries");
		attrib3.setNodetype(NodeType.ATTRIB);
		attrib3.setValue(iMinEntries);

		LantiqMutableTreeNode attrib4 = new LantiqMutableTreeNode("maxEntries");
		attrib4.setNodetype(NodeType.ATTRIB);
		if (!unbounded)
			attrib4.setValue(iMaxEntries);
		else
			attrib4.setValue("unbounded");

		LantiqMutableTreeNode newObj = new LantiqMutableTreeNode(objname);
		newObj.setNodetype(NodeType.OBJECT);

		int index = node.getChildCount();

		// Add Default Parameter.
		LantiqMutableTreeNode defParam = new LantiqMutableTreeNode("DefParam");
		defParam.setNodetype(NodeType.PARAM);
		defParam.setValue("0");

		LantiqMutableTreeNode defattrib1 = new LantiqMutableTreeNode("syntax");
		defattrib1.setNodetype(NodeType.ATTRIB);
		defattrib1.setValue("integer");

		((DefaultTreeModel) tree.getModel()).insertNodeInto(attrib2, newObj, 0);
		((DefaultTreeModel) tree.getModel()).insertNodeInto(attrib1, newObj, 1);
		((DefaultTreeModel) tree.getModel()).insertNodeInto(attrib3, newObj, 2);
		((DefaultTreeModel) tree.getModel()).insertNodeInto(attrib4, newObj, 3);
		((DefaultTreeModel) tree.getModel())
				.insertNodeInto(defParam, newObj, 4);

		((DefaultTreeModel) tree.getModel()).insertNodeInto(defattrib1,
				defParam, 0);

		((DefaultTreeModel) tree.getModel())
				.insertNodeInto(newObj, node, index);

		return true;
	}

	public boolean editParameter() {
		if (fileType == FileType.INSTANCE || fileType == FileType.INSTANCE_ID) {

		} else {
			String paramname = nameTextField.getText().trim();
			if (paramname == null || paramname.equals("")
					|| paramname.length() < 0) {
				JOptionPane.showMessageDialog(this,
						"Please Enter Valid Parameter Name", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			node.setUserObject(paramname);
		}

		String paramvalue = valueTextField.getText().trim();
		if (paramvalue == null || paramvalue.equals("")
				|| paramvalue.length() < 0) {
			paramvalue = "";
		}

		node.setValue(paramvalue);
		
		((InvisibleTreeModel) tree.getModel()).activateFilter(false);
		((InvisibleTreeModel) tree.getModel()).reload();

		((DefaultTreeModel) tree.getModel()).nodesChanged(node.getParent(),
				new int[] { ((InvisibleNode) node.getParent()).getIndex(node) });
		
		((InvisibleTreeModel) tree.getModel()).activateFilter(true);
		((InvisibleTreeModel) tree.getModel()).reload();
		
		TreePath nodePath = null;
		nodePath = new TreePath(node.getPath());
		tree.expandPath(nodePath);
		
		return true;
	}

	public boolean addParameter() {
		String paramname = nameTextField.getText().trim();
		if (paramname == null || paramname.equals("") || paramname.length() < 0) {
			JOptionPane.showMessageDialog(this,
					"Please Enter Valid Parameter Name", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		int index = syntaxCombo.getSelectedIndex();
		if (index <= -1) {
			JOptionPane.showMessageDialog(this, "Please Select Valid Syntax",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		String paramvalue = valueTextField.getText().trim();
		if (paramvalue == null || paramvalue.equals("")
				|| paramvalue.length() < 0) {
			paramvalue = "";
		}

		LantiqMutableTreeNode attrib1 = new LantiqMutableTreeNode("syntax");
		attrib1.setNodetype(NodeType.ATTRIB);
		attrib1.setValue(syntaxCombo.getSelectedItem());

		LantiqMutableTreeNode newparam = new LantiqMutableTreeNode(paramname);
		newparam.setNodetype(NodeType.PARAM);
		newparam.setValue(paramvalue);

		int lastindex = TreeBuilder
				.getIndexOfLast(node, newparam.getNodetype());

		((DefaultTreeModel) tree.getModel()).insertNodeInto(newparam, node,
				lastindex + 1);
		((DefaultTreeModel) tree.getModel()).insertNodeInto(attrib1, newparam,
				0);

		return true;
	}

	public boolean editAttribute(boolean add) {
		String attribname = "";
		if (add) {
			attribname = nameTextField.getText().trim();
			if (attribname == null || attribname.equals("")
					|| attribname.length() < 0) {
				JOptionPane.showMessageDialog(this,
						"Please Enter Valid Attribute Name", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		String attribvalue = valueTextField.getText().trim();

		if (attribvalue == null || attribvalue.equals("")
				|| attribvalue.length() < 0) {
			JOptionPane.showMessageDialog(this,
					"Please Enter Valid Attribute Value", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		((InvisibleTreeModel) tree.getModel()).activateFilter(false);
		((InvisibleTreeModel) tree.getModel()).reload();
		if (!add) {
			//node.setUserObject(attribname);
			node.setValue(attribvalue);
			((InvisibleTreeModel) tree.getModel()).nodesChanged(node
					.getParent(),
					new int[] { ((InvisibleNode) node.getParent()).getIndex(
							node) });
		} else {
			LantiqMutableTreeNode newAttr = new LantiqMutableTreeNode(
					attribname);
			newAttr.setValue(attribvalue);
			newAttr.setNodetype(NodeType.ATTRIB);

			int index = TreeBuilder.getIndexOfLast(node, newAttr.getNodetype());

			((DefaultTreeModel) tree.getModel()).insertNodeInto(newAttr, node,
					index + 1);
		}
		((InvisibleTreeModel) tree.getModel()).activateFilter(true);
		((InvisibleTreeModel) tree.getModel()).reload();
		
		TreePath nodePath = null;
		if(!add){
			nodePath = new TreePath(((LantiqMutableTreeNode)node.getParent()).getPath());
		}else{
			nodePath = new TreePath(node.getPath());
		}
		tree.expandPath(nodePath);
		
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean noerror = false;
		switch (e.getActionCommand()) {
		case "AddService":
			noerror = editService(true);
			break;
		case "EditService":
			noerror = editService(false);
			break;
		case "AddObject":
			noerror = addObject();
			break;
		case "EditObject":
			noerror = editObject();
			break;
		case "AddParameter":
			noerror = addParameter();
			break;
		case "EditParameter":
			noerror = editParameter();
			break;
		case "AddAttribute":
			noerror = editAttribute(true);
			break;
		case "EditAttribute":
			noerror = editAttribute(false);
			break;

		case "Cancel":
			setCancelled(true);
			noerror = true;
			break;
		}

		if (noerror) {
			dispose();
		}
	}

	public LantiqMutableTreeNode getNode() {
		return node;
	}

	public void setNode(LantiqMutableTreeNode node) {
		this.node = node;
	}

	public JXTree getTree() {
		return tree;
	}

	public void setTree(JXTree tree) {
		this.tree = tree;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean wasCancelled) {
		this.cancelled = wasCancelled;
	}

}