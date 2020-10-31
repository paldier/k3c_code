/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.ui;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lantiq.dbtool.base.DiffType;
import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.NodeType;

public class LantiqRenderer extends DefaultTreeCellRenderer {

	private static final Logger logger = LogManager
			.getLogger(LantiqRenderer.class);

	Icon deviceIcon, serviceIcon, objectIcon, paramIcon, attribIcon;

	static String configpath = System.getProperty("user.dir") + "/../" + "configs/";
	
	private static Properties colors;

	static {
		loadProperties();
	}

	public static void loadProperties() {
		logger.log(Level.INFO, null, "Loading Properties");
		colors = new Properties();
		InputStream in;
		try {
			in = new FileInputStream(new File(configpath + "colors.properties"));
			colors.load(in);
			in.close();
		} catch (IOException ex) {
			logger.log(Level.DEBUG, "Error Occured : Load Properties", ex);
		}
	}

	public LantiqRenderer() {
		super();
		getImageIcons();
	}

	private void getImageIcons() {
		URL deviceImg = getClass().getResource("/DBlack16.png");
		deviceIcon = new ImageIcon(deviceImg);

		URL srvcImg = this.getClass().getResource("/SGold16.png");
		serviceIcon = new ImageIcon(srvcImg);

		URL objImg = this.getClass().getResource("/OGrey16.png");
		objectIcon = new ImageIcon(objImg);

		URL paramImg = this.getClass().getResource("/PDarkGreen16.png");
		paramIcon = new ImageIcon(paramImg);

		URL attrImg = this.getClass().getResource("/ABlue16.png");
		attribIcon = new ImageIcon(attrImg);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component comp = super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);

		/* For the Icons */
		LantiqMutableTreeNode node = (LantiqMutableTreeNode) value;
		if (node.getNodetype() == NodeType.DEVICE) {
			setIcon(deviceIcon);
		} else if (node.getNodetype() == NodeType.SERVICE) {
			setIcon(serviceIcon);
		} else if (node.getNodetype() == NodeType.OBJECT) {
			setIcon(objectIcon);
		} else if (node.getNodetype() == NodeType.PARAM) {
			setIcon(paramIcon);
		} else if (node.getNodetype() == NodeType.ATTRIB) {
			setIcon(attribIcon);
		}

		/* For the color coding and tooltip */
		JLabel jComp = (JLabel) comp;
		Boolean leftorright = null;

		if (tree.getName().equals("Left"))
			leftorright = true;
		else if (tree.getName().equals("Right"))
			leftorright = false;

		if (node.isDiff()) {
			if (leftorright) {
				jComp.setOpaque(true);
				jComp.setForeground(Color.BLACK);

				if (node.getDifftype() == DiffType.VALUE 
						|| node.getDifftype() == DiffType.TEXT) {
					if (node.isLeaf() || node.getNodetype() == NodeType.PARAM)
						jComp.setBackground(Color.decode(colors
								.getProperty("mismatchcolor")));
					else
						jComp.setBackground(Color.decode(colors
								.getProperty("diffcolor")));
				} else if (node.getDifftype() == DiffType.ORPHAN) {
					jComp.setBackground(Color.decode(colors
							.getProperty("missingcolor")));
				} else if (node.getDifftype() == DiffType.UNKNOWN) {
					jComp.setBackground(Color.decode(colors
							.getProperty("diffcolor")));
				}

			} else {
				jComp.setOpaque(true);
				jComp.setForeground(Color.BLACK);
				if (node.getDifftype() == DiffType.VALUE 
						|| node.getDifftype() == DiffType.TEXT) {
					if (node.isLeaf()  || node.getNodetype() == NodeType.PARAM)
						jComp.setBackground(Color.decode(colors
								.getProperty("mismatchcolor")));
					else
						jComp.setBackground(Color.decode(colors
								.getProperty("diffcolor")));
				} else if (node.getDifftype() == DiffType.ORPHAN) {
					jComp.setBackground(Color.decode(colors
							.getProperty("missingcolor")));
				} else if (node.getDifftype() == DiffType.UNKNOWN) {
					jComp.setBackground(Color.decode(colors
							.getProperty("diffcolor")));
				}
			}
		} else {
			jComp.setOpaque(false);
			jComp.setBackground(null);
		}

		if (node.getValue() != null) {
			String nodevalue = node.getValue().toString();
			if(!nodevalue.equals("") || nodevalue.length() > 0){
				jComp.setText(node.getUserObject().toString() + " = "+ node.getValue().toString());
				jComp.setToolTipText(node.getValue().toString());
			}else{
				jComp.setText(node.getUserObject().toString());
				jComp.setToolTipText(node.getUserObject().toString());
			}
		} else {
			jComp.setText(node.getUserObject().toString());
			jComp.setToolTipText(node.getUserObject().toString());
		}

		return jComp;
	}
}
