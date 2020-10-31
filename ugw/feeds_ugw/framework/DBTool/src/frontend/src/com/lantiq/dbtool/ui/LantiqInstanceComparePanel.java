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
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTabbedPaneUI.TabbedPaneLayout;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.ElementNameAndTextQualifier;
import org.custommonkey.xmlunit.NodeDetail;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.lantiq.dbtool.base.AccessType;
import com.lantiq.dbtool.base.DiffType;
import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.base.InvisibleNode;
import com.lantiq.dbtool.base.InvisibleTreeModel;
import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.LantiqTreeSearchable;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.base.TreeBuilder;
import com.lantiq.dbtool.utils.FileUtils;
import com.lantiq.dbtool.utils.LantiqFileFilter;
import com.lantiq.dbtool.utils.PythonExecutor;
import com.lantiq.dbtool.utils.SpringUtilities;

public class LantiqInstanceComparePanel extends JPanel implements
		ActionListener, TreeExpansionListener {

	private static final Logger logger = LogManager
			.getLogger(LantiqInstanceComparePanel.class);

	private LantiqMutableTreeNode baseleft, baseright, controlbase;
	private DefaultTreeModel controlTreeModel;
	private InvisibleTreeModel leftTreeModel, rightTreeModel;
	private JXTree jTreeLeft, jTreeRight, controlTree;
	private JLabel pathLeftTxt, pathRightTxt, lockLbl;
	private JPopupMenu popupMenuLeft, popupMenuRight;
	// Left
	private JMenuItem itemCopy;
	// Right
	private JMenuItem itemEditRight, itemAddSrvc, itemAddObj, itemAddParam,
			itemAddAttr, itemDel, itemMoveUp, itemMoveDwn;
	private JSplitPane jSplitPane;
	private JTextField leftFilePathText, rightFilePathText;
	private JButton leftFileBrwsBtn, rightFileBrwsBtn, rightFileNewBtn,
			rightFileSaveBtn, rightFileSaveAsBtn;
	private JScrollPane leftJSP, rightJSP;
	private JFrame parent;
	private File selectedfileLeft, selectedfileRight;
	private DetailedDiff difference;
	private List<Integer> diffrowsfound = new ArrayList<>();
	private Icon saveIcon, saveAsIcon, newIcon, lockIcon;
	private LantiqChangeListener listener;
	private LantiqTreeSearchable leftSearchable, rightSearchable;
	private boolean linked = true;
	private int selectedLeftRow, selectedRightRow;
	private TreePath selectedLeftPath, selectedRightPath;
	private StringBuffer xmlFiles[];
	private FileType primaryFileType, secFileType;
	private AccessType accessType;
	private boolean dirty = false;

	public LantiqInstanceComparePanel(JFrame parent) {
		this.parent = parent;
		initComponents();
		addComponents();
		setScrollable(false);
		setVisible(true);
	}

	public void changeRootNode() {
		LantiqMutableTreeNode leftRoot = (LantiqMutableTreeNode) jTreeLeft
				.getModel().getRoot();

		LantiqMutableTreeNode newleftRoot = (LantiqMutableTreeNode) leftRoot
				.getFirstChild();

		((DefaultTreeModel) jTreeLeft.getModel()).setRoot(newleftRoot);
		newleftRoot.setParent(null);
		baseleft = newleftRoot;

		LantiqMutableTreeNode rightRoot = (LantiqMutableTreeNode) jTreeRight
				.getModel().getRoot();

		LantiqMutableTreeNode newrightRoot = (LantiqMutableTreeNode) rightRoot
				.getFirstChild();

		((DefaultTreeModel) jTreeRight.getModel()).setRoot(newrightRoot);
		newrightRoot.setParent(null);
		baseright = newrightRoot;

	}

	public void initComponents() {

		baseleft = new LantiqMutableTreeNode("Left");
		leftTreeModel = new InvisibleTreeModel(baseleft);
		jTreeLeft = new JXTree(leftTreeModel);
		jTreeLeft.setName("Left");
		leftSearchable = new LantiqTreeSearchable(jTreeLeft);
		jTreeLeft.setSearchable(leftSearchable);

		baseright = new LantiqMutableTreeNode("Right");
		rightTreeModel = new InvisibleTreeModel(baseright);
		jTreeRight = new JXTree(rightTreeModel);
		jTreeRight.setName("Right");
		rightSearchable = new LantiqTreeSearchable(jTreeRight);
		jTreeRight.setSearchable(rightSearchable);

		// Drag Drop disabled.
		/*
		 * LantiqTransferHandler lthLeft = new LantiqTransferHandler(this,
		 * jTreeLeft, jTreeLeft.getModel()); jTreeLeft.setDragEnabled(true);
		 * jTreeLeft.setDropMode(DropMode.ON);
		 * jTreeLeft.setTransferHandler(lthLeft);
		 * 
		 * LantiqTransferHandler lthRight = new LantiqTransferHandler(this,
		 * jTreeRight, jTreeRight.getModel()); jTreeRight.setDragEnabled(true);
		 * jTreeRight.setDropMode(DropMode.ON);
		 * jTreeRight.setTransferHandler(lthRight);
		 */

		jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		jSplitPane.setPreferredSize(new Dimension(this.getWidth(), this
				.getHeight()));

		pathLeftTxt = new JLabel("File Path");
		leftFilePathText = new JTextField();
		leftFilePathText.setActionCommand("LeftTxt");
		leftFilePathText.addActionListener(this);

		pathRightTxt = new JLabel("File Path");
		rightFilePathText = new JTextField();
		rightFilePathText.setActionCommand("RightTxt");
		rightFilePathText.addActionListener(this);

		leftFileBrwsBtn = new JButton("...");
		leftFileBrwsBtn.setActionCommand("LeftBrowse");
		leftFileBrwsBtn.addActionListener(this);

		rightFileBrwsBtn = new JButton("...");
		rightFileBrwsBtn.setActionCommand("RightBrowse");
		rightFileBrwsBtn.addActionListener(this);

		URL deviceImg = this.getClass().getResource("/SaveIcon16.png");
		saveIcon = new ImageIcon(deviceImg);
		rightFileSaveBtn = new JButton(saveIcon);
		rightFileSaveBtn.setActionCommand("RightSave");
		rightFileSaveBtn.addActionListener(this);

		URL deviceImg1 = this.getClass().getResource("/SaveAs16.png");
		saveAsIcon = new ImageIcon(deviceImg1);
		rightFileSaveAsBtn = new JButton(saveAsIcon);
		rightFileSaveAsBtn.setActionCommand("RightSaveAs");
		rightFileSaveAsBtn.addActionListener(this);

		URL deviceNewImg = this.getClass().getResource("/NewIcon16.png");
		newIcon = new ImageIcon(deviceNewImg);
		rightFileNewBtn = new JButton(newIcon);
		rightFileNewBtn.setActionCommand("RightNew");
		rightFileNewBtn.addActionListener(this);

		URL lockImg = this.getClass().getResource("/Lock16.png");
		lockIcon = new ImageIcon(lockImg);
		lockLbl = new JLabel(lockIcon);

		leftJSP = new JScrollPane(jTreeLeft,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		rightJSP = new JScrollPane(jTreeRight,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	}

	public void addComponents() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		System.out.println(screenSize);

		setLayout(new BorderLayout());
		add(jSplitPane, BorderLayout.CENTER);
		jSplitPane.setResizeWeight(0.5);

		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel rightPanel = new JPanel(new BorderLayout());

		SpringLayout leftSpring = new SpringLayout();
		SpringLayout rightSpring = new SpringLayout();
		JPanel leftInnerPanel = new JPanel(leftSpring);
		JPanel rightInnerPanel = new JPanel(rightSpring);

		jSplitPane.setLeftComponent(leftPanel);
		jSplitPane.setRightComponent(rightPanel);

		leftPanel.add(leftInnerPanel, BorderLayout.NORTH);
		leftPanel.add(leftJSP, BorderLayout.CENTER);

		leftInnerPanel.add(pathLeftTxt);
		leftInnerPanel.add(leftFilePathText);
		leftInnerPanel.add(leftFileBrwsBtn);
		leftInnerPanel.add(lockLbl);

		pathLeftTxt.setPreferredSize(new Dimension(50, 25));
		leftFilePathText.setPreferredSize(new Dimension(400, 25));
		leftFileBrwsBtn.setPreferredSize(new Dimension(25, 25));
		lockLbl.setPreferredSize(new Dimension(25, 25));

		SpringUtilities.makeCompactGrid(leftInnerPanel, 1, 4, 5, 5, 5, 5);

		rightPanel.add(rightInnerPanel, BorderLayout.NORTH);
		rightPanel.add(rightJSP, BorderLayout.CENTER);

		rightInnerPanel.add(pathRightTxt);
		rightInnerPanel.add(rightFilePathText);
		rightInnerPanel.add(rightFileBrwsBtn);
		rightInnerPanel.add(rightFileSaveBtn);
		rightInnerPanel.add(rightFileSaveAsBtn);
		rightInnerPanel.add(rightFileNewBtn);

		pathRightTxt.setPreferredSize(new Dimension(50, 25));
		rightFilePathText.setPreferredSize(new Dimension(400, 25));
		rightFileBrwsBtn.setPreferredSize(new Dimension(25, 25));
		rightFileSaveBtn.setPreferredSize(new Dimension(25, 25));
		rightFileSaveAsBtn.setPreferredSize(new Dimension(25, 25));
		rightFileNewBtn.setPreferredSize(new Dimension(25, 25));

		SpringUtilities.makeCompactGrid(rightInnerPanel, 1, 6, 5, 5, 5, 5);

		listener = new LantiqChangeListener(leftJSP.getVerticalScrollBar()
				.getModel());
		linkScrollBars(true);

	}

	public void linkScrollBars(boolean link) {
		if (link) {
			rightJSP.getVerticalScrollBar().getModel()
					.addChangeListener(listener);
			rightJSP.getViewport().setViewPosition(new Point(0, 0));
			leftJSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			linked = true;
		} else {
			rightJSP.getVerticalScrollBar().getModel()
					.removeChangeListener(listener);
			leftJSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			linked = false;
		}
	}

	private boolean checkAllChildOrphanDiffs(LantiqMutableTreeNode node) {
		int childcnt = node.getChildCount();
		int diffcnt = 0;
		for (int i = 0; i < childcnt; i++) {
			if (((LantiqMutableTreeNode) node.getChildAt(i)).isDiff()
					&& ((LantiqMutableTreeNode) node.getChildAt(i))
							.getDifftype() == DiffType.ORPHAN) {
				diffcnt++;
			}
		}

		if (childcnt == diffcnt)
			return true;
		else
			return false;
	}

	public void setTreeProperties() {
		jTreeLeft.setCellRenderer(new LantiqRenderer());
		ToolTipManager.sharedInstance().registerComponent(jTreeLeft);
		jTreeLeft.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int selRow = jTreeLeft.getRowForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					setSelectedLeftRow(selRow);
					setSelectedLeftPath(jTreeLeft.getPathForLocation(e.getX(),
							e.getY()));
					if (e.getClickCount() == 1) {
						if (SwingUtilities.isRightMouseButton(e)) {
							LantiqMutableTreeNode node = (LantiqMutableTreeNode) jTreeLeft
									.getPathForRow(selRow)
									.getLastPathComponent();
							if (getAccessType() == AccessType.READWRITE
									&& (node.getDifftype() == DiffType.TEXT
											|| node.getDifftype() == DiffType.ORPHAN
											|| checkAllChildOrphanDiffs(node) || node
											.getDifftype() == DiffType.VALUE)) {
								itemCopy.setEnabled(true);
							} else {
								itemCopy.setEnabled(false);
							}
							popupMenuLeft.show(e.getComponent(), e.getX(),
									e.getY());
						}
					}
				}
			}
		});
		jTreeLeft.setRolloverEnabled(true);
		jTreeLeft.addHighlighter(new ColorHighlighter(
				HighlightPredicate.ROLLOVER_ROW, null, Color.RED));

		jTreeLeft.expandAll();
		leftFilePathText.setText(selectedfileLeft.getAbsolutePath());

		jTreeRight.setCellRenderer(new LantiqRenderer());
		ToolTipManager.sharedInstance().registerComponent(jTreeRight);
		jTreeRight.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int selRow = jTreeRight.getRowForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					setSelectedRightRow(selRow);
					setSelectedRightPath(jTreeRight.getPathForLocation(
							e.getX(), e.getY()));
					if (e.getClickCount() == 1) {
						if (SwingUtilities.isRightMouseButton(e)) {
							jTreeRight.setSelectionPath(getSelectedRightPath());
							LantiqMutableTreeNode node = (LantiqMutableTreeNode) jTreeRight
									.getLastSelectedPathComponent();
							setEnabledMenuItem(node);
							popupMenuRight.show(e.getComponent(), e.getX(),
									e.getY());
						}
					}
				}
			}
		});
		jTreeRight.setRolloverEnabled(true);
		jTreeRight.addHighlighter(new ColorHighlighter(
				HighlightPredicate.ROLLOVER_ROW, null, Color.RED));
		jTreeRight.expandAll();
		rightFilePathText.setText(selectedfileRight.getAbsolutePath());

		setTreeExpansionListeners(true);
	}

	public void setTreeExpansionListeners(boolean set) {
		if (set) {
			jTreeLeft.addTreeExpansionListener(this);
			jTreeRight.addTreeExpansionListener(this);
		} else {
			jTreeLeft.removeTreeExpansionListener(this);
			jTreeRight.removeTreeExpansionListener(this);
		}
	}

	public void addPopupMenus() {
		addPopupMenuLeft();
		addPopupMenuRight();
	}

	public void addPopupMenuLeft() {
		popupMenuLeft = new JPopupMenu();
		itemCopy = new JMenuItem("Copy");

		itemCopy.addActionListener(this);
		popupMenuLeft.add(itemCopy);

	}

	public void copyNode() {
		setTreeExpansionListeners(false);

		TreePath path = getSelectedLeftPath();
		jTreeLeft.setSelectionPath(path);
		LantiqMutableTreeNode node = (LantiqMutableTreeNode) jTreeLeft
				.getLastSelectedPathComponent();
		LantiqMutableTreeNode parent = (LantiqMutableTreeNode) node.getParent();
		int location = parent.getIndex(node);

		jTreeRight.expandPath(path);
		int plocation = TreeBuilder.getLocationOfNode(jTreeRight, new TreePath(
				parent.getPath()), false);
		if (plocation >= 0) {
			copyNodeFromLefttoRight(location, plocation, node);
		}

		jTreeRight.expandAll();
		refreshBufferAfterSync();

		setTreeExpansionListeners(true);
	}

	public void copyNodeFromLefttoRight(int location, int plocation,
			LantiqMutableTreeNode node) {
		LantiqMutableTreeNode parentclone = (LantiqMutableTreeNode) jTreeRight
				.getPathForRow(plocation).getLastPathComponent();

		LantiqMutableTreeNode clone = (LantiqMutableTreeNode) node.clone();

		boolean nodepresent = false;
		LantiqMutableTreeNode child = null;
		int childindex = -1;
		for (int i = 0; i < parentclone.getChildCount(); i++) {
			child = (LantiqMutableTreeNode) parentclone.getChildAt(i);

			if (child.getUserObject().toString()
					.equals(clone.getUserObject().toString())) {
				nodepresent = true;
				childindex = i;
				break;
			}
		}

		setNodesInvisible(rightTreeModel, false);
		if (nodepresent && child != null) {
			int option = JOptionPane
					.showConfirmDialog(
							LantiqInstanceComparePanel.this.parent,
							"This will Overwrite Node on Right. Do you want to continue?",
							"Overwrite", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				child.setValue(node.getValue());
				child.setNodetype(node.getNodetype());
				if (child.getChildCount() > 0) {
					child.removeAllChildren();
					((DefaultTreeModel) rightTreeModel).reload(child);
					TreeBuilder.copyChildNodes(jTreeRight, node, child);
				}
				((DefaultTreeModel) rightTreeModel).nodesChanged(parentclone,
						new int[] { childindex });
			} else {
				return;
			}
		} else {
			clone.setParent(null);
			clone.setValue(node.getValue());
			clone.setNodetype(node.getNodetype());

			if (location > parentclone.getChildCount())
				location = parentclone.getChildCount();

			((DefaultTreeModel) rightTreeModel).insertNodeInto(clone,
					parentclone, location);
			TreeBuilder.copyChildNodes(jTreeRight, node, clone);
		}

		setNodesInvisible(rightTreeModel, true);

		TreePath path = null;
		if (node.isLeaf()) {
			path = new TreePath(parentclone.getPath());
		} else {
			path = new TreePath(node.getPath());
		}

		jTreeRight.expandPath(path);
	}

	public void addPopupMenuRight() {
		popupMenuRight = new JPopupMenu();

		itemEditRight = new JMenuItem("Edit");
		itemAddObj = new JMenuItem("Add");
		itemAddObj.setActionCommand("Add Object");
		itemDel = new JMenuItem("Delete");

		itemEditRight.addActionListener(this);
		itemAddObj.addActionListener(this);
		itemDel.addActionListener(this);

		popupMenuRight.add(itemEditRight);
		popupMenuRight.addSeparator();
		popupMenuRight.add(itemAddObj);
		popupMenuRight.addSeparator();
		popupMenuRight.add(itemDel);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		performAction(evt.getActionCommand());
	}

	private void performAction(String action) {
		boolean refresh = false;
		Boolean rightsave = null;
		boolean rightsaveAs = false;

		FileUtils fu = new FileUtils();
		LantiqFileFilter fnf = new LantiqFileFilter(FileType.INSTANCE,
				"Instance Files Only");

		LantiqInputDialog dialog = null;
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		LantiqMutableTreeNode node = (LantiqMutableTreeNode) jTreeRight
				.getLastSelectedPathComponent();

		switch (action) {
		case "LeftTxt":
			selectedfileLeft = new File(leftFilePathText.getText().trim());

			if (!((LantiqFrame) parent).validateXMLFile(selectedfileLeft, true)) {
				return;
			}

			refresh = true;
			rightsave = false;
			break;
		case "LeftBrowse":
			selectedfileLeft = fu.openFile(this.parent, fnf, true);

			if (selectedfileLeft != null) {
				if (!((LantiqFrame) parent).validateXMLFile(selectedfileLeft,
						true)) {
					return;
				}
				leftFilePathText.setText(selectedfileLeft.getAbsolutePath());

				JOptionPane
						.showMessageDialog(
								this,
								"Please select an instance file to be opened on the RHS window",
								"Instance Compare",
								JOptionPane.INFORMATION_MESSAGE);

				refresh = true;
				rightsave = false;
			}

			break;
		case "RightTxt":
			selectedfileRight = new File(rightFilePathText.getText().trim());

			if (!((LantiqFrame) parent).validateXMLFile(selectedfileLeft, true)) {
				return;
			}

			refresh = true;
			rightsave = false;
			break;
		case "RightBrowse":
			selectedfileRight = fu.openFile(this.parent, fnf, true);

			if (selectedfileRight != null) {
				if (!((LantiqFrame) parent).validateXMLFile(selectedfileLeft,
						true)) {
					return;
				}

				rightFilePathText.setText(selectedfileRight.getAbsolutePath());

				refresh = true;
				rightsave = false;
			}

			break;
		case "RightSave":
			refresh = false;
			rightsave = true;
			break;
		case "RightSaveAs":
			refresh = false;
			rightsave = true;
			rightsaveAs = true;
			break;
		case "RightNew":
			try {
				refreshRightAfterNew();
			} catch (XMLStreamException | SAXException | IOException e1) {
				e1.printStackTrace();
			}
			refresh = true;
			break;
		case "Copy":
			copyNode();
			setDirty(true);
			refresh = false;
			rightsave = false;
			break;
		case "Edit":
			dialog = new LantiqInputDialog(node, false, null, true,
					getPrimaryFileType());
			dialog.setTree(jTreeRight);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setLocation(screensize.width / 2 - 125,
					screensize.height / 2 - 200);
			dialog.pack();
			dialog.setVisible(true);

			if (!dialog.isCancelled()) {
				setDirty(true);
				refresh = false;
				rightsave = false;
			} else {
				refresh = false;
				rightsave = false;
			}
			break;
		case "Add Service":
		case "Add Object":
			if (getPrimaryFileType() == FileType.INSTANCE) {
				rightTreeModel.activateFilter(false);
				rightTreeModel.reload();
				TreePath parentpath = new TreePath(
						((LantiqMutableTreeNode) node.getParent()).getPath());

				HashMap<String, Object> map = TreeBuilder.getValuesFromControl(
						controlTree, node);
				int iMax = 0;
				String maxEntries = (String) map.get("maxEntries");
				if (maxEntries.equals("unbounded")) {
					iMax = Integer.MAX_VALUE;
				} else {
					iMax = Integer.parseInt(maxEntries);
				}
				TreeBuilder.addObjectNode(jTreeRight, node, iMax);
				rightTreeModel.activateFilter(true);
				rightTreeModel.reload();
				jTreeRight.expandPath(parentpath);
				setDirty(true);
				refresh = false;
				rightsave = false;
				break;
			}
		case "Add Parameter":
		case "Add Attribute":
			dialog = new LantiqInputDialog(node, true, action, true,
					getPrimaryFileType());
			dialog.setTree(jTreeRight);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setLocation(screensize.width / 2 - 125,
					screensize.height / 2 - 200);
			dialog.pack();
			dialog.setVisible(true);

			if (!dialog.isCancelled()) {
				setDirty(true);
				refresh = false;
				rightsave = false;
			} else {
				refresh = false;
				rightsave = false;
			}
			break;
		case "Delete":
			if (getPrimaryFileType() == FileType.INSTANCE) {
				rightTreeModel.activateFilter(false);
				rightTreeModel.reload();
				TreePath parentpath = new TreePath(
						((LantiqMutableTreeNode) node.getParent()).getPath());

				HashMap<String, Object> map = TreeBuilder.getValuesFromControl(
						controlTree, node);
				String minEntries = (String) map.get("minEntries");
				int iMin = Integer.parseInt(minEntries);

				String disstr = node.getUserObject().toString();
				if (disstr.indexOf("{") >= 0) {
					String num = disstr.substring(disstr.indexOf("{") + 1,
							disstr.length() - 1);
					int inum = Integer.parseInt(num) - 1;
					if (inum < iMin) {
						JOptionPane
								.showMessageDialog(
										null,
										"Total instances should not be less than minEntries",
										"Instance Error",
										JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						deleteNode(node);
					}
				} else {
					int inum = 0;
					if (inum < iMin) {
						JOptionPane
								.showMessageDialog(
										null,
										"Total instances should not be less than minEntries",
										"Instance Error",
										JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						deleteNode(node);
					}
				}
				rightTreeModel.activateFilter(true);
				rightTreeModel.reload();
				jTreeRight.expandPath(parentpath);
			}
			setDirty(true);
			refresh = false;
			rightsave = false;
			break;
		}

		dialog = null;

		if (rightsave != null) {
			if (rightsave) {
				String filename = rightFilePathText.getText().trim();
				if (filename == null || filename.equals("")
						|| filename.contains("rightNew")
						|| filename.length() == 0 || rightsaveAs) {
					saveFile(fu, fnf);
				} else {
					saveFileFromText(fu);
				}
				setDirty(false);
				refresh = true;
			}
		}		

		if (refresh) {
			try {
				refreshCompareChangeFile();
			} catch (XMLStreamException | SAXException | IOException e) {
				e.printStackTrace();
			}
		}else{
			refreshBufferAfterSync();
		}
	}

	public void saveFile(FileUtils fu, LantiqFileFilter fnf) {
		File file = fu.saveFile(this.parent, fnf, true);
		if (file != null) {
			fu.writeTreeToFile(jTreeRight, file.getAbsolutePath());
			PythonExecutor pe = new PythonExecutor(parent);
			pe.execPythonScriptAddWaterMark(file, getPrimaryFileType(),
					getAccessType());
		}

		selectedfileRight = file;
	}

	public void saveFileFromText(FileUtils fu) {
		fu.writeTreeToFile(jTreeRight, rightFilePathText.getText().trim());
		PythonExecutor pe = new PythonExecutor(parent);
		pe.execPythonScriptAddWaterMark(new File(rightFilePathText.getText()
				.trim()), getPrimaryFileType(), getAccessType());
	}

	private void deleteNode(LantiqMutableTreeNode node) {
		setNodesInvisible(rightTreeModel, false);

		TreePath path = new TreePath(
				((LantiqMutableTreeNode) node.getParent()).getPath());

		if (node.isLeaf())
			TreeBuilder.deleteNode(parent, rightTreeModel, node, false);
		else
			TreeBuilder.deleteNode(parent, rightTreeModel, node, true);

		setNodesInvisible(rightTreeModel, true);

		jTreeRight.expandPath(path);

	}

	public void refreshRightAfterNew() throws XMLStreamException, SAXException,
			IOException {

		LantiqMutableTreeNode devNode = new LantiqMutableTreeNode("Device");
		devNode.setNodetype(NodeType.DEVICE);

		((DefaultTreeModel) rightTreeModel).insertNodeInto(devNode, baseright,
				0);

		LantiqMutableTreeNode rightRoot = (LantiqMutableTreeNode) jTreeRight
				.getModel().getRoot();

		// if (rightRoot.getChildCount() > 0) {
		LantiqMutableTreeNode newrightRoot = (LantiqMutableTreeNode) rightRoot
				.getFirstChild();

		((DefaultTreeModel) jTreeRight.getModel()).setRoot(newrightRoot);
		newrightRoot.setParent(null);
		baseright = newrightRoot;

		rightTreeModel.reload();

		String tempFile = System.getProperty("user.dir") + "/temp/rightNew.xml";
		FileUtils fu = new FileUtils();
		fu.writeTreeToFile(jTreeRight, tempFile);
		PythonExecutor pe = new PythonExecutor(parent);
		pe.execPythonScriptAddWaterMark(new File(tempFile), FileType.INSTANCE,
				AccessType.READWRITE);

		setPrimaryFileType(FileType.INSTANCE);
		setAccessType(AccessType.READWRITE);
		selectedfileRight = new File(tempFile);

	}

	public void setTypes() {
		FileType type = getFileType(selectedfileLeft);
		setSecFileType(type);

		type = getFileType(selectedfileRight);
		setPrimaryFileType(type);

		AccessType atype = getAccessType(selectedfileRight);
		setAccessType(atype);
	}

	public AccessType getAccessType(File file) {
		PythonExecutor exec = new PythonExecutor(parent);
		return exec.execPythonScriptGetAccessType(file);
	}

	public FileType getFileType(File file) {
		PythonExecutor exec = new PythonExecutor(parent);
		return exec.execPythonScriptGetWaterMark(file);
	}

	public void refreshCompareChangeFile() throws XMLStreamException,
			SAXException, IOException {

		setTreeExpansionListeners(false);
		if (selectedfileLeft != null && selectedfileRight != null) {

			baseleft.removeAllChildren();
			baseright.removeAllChildren();

			leftTreeModel.reload();
			rightTreeModel.reload();

			setTypes();

			TreeBuilder.buildTree(jTreeLeft, baseleft, selectedfileLeft,
					primaryFileType);
			TreeBuilder.buildTree(jTreeRight, baseright, selectedfileRight,
					primaryFileType);

			TreeBuilder.setNodesVisible(jTreeLeft, false, primaryFileType,
					secFileType);
			TreeBuilder.setNodesVisible(jTreeRight, false, primaryFileType,
					secFileType);

			setNodesInvisible(leftTreeModel, true);
			setNodesInvisible(rightTreeModel, true);

			leftFilePathText.setText(selectedfileLeft.getAbsolutePath());
			((LantiqFrame) parent).addRecentFile(selectedfileLeft
					.getAbsolutePath());
			rightFilePathText.setText(selectedfileRight.getAbsolutePath());
			((LantiqFrame) parent).addRecentFile(selectedfileRight
					.getAbsolutePath());

			leftTreeModel.reload();
			rightTreeModel.reload();

			changeRootNode();
			addPopupMenus();
			setTreeProperties();

			runCompare(true);

			jTreeLeft.updateUI();
			jTreeRight.updateUI();

			((LantiqFrame) parent).updateParentTabComponent(
					selectedfileLeft.getName(), selectedfileRight.getName());
		}
		setTreeExpansionListeners(true);
	}

	private void loadSchemaFile() {
		File controlfile = null;
		if (getPrimaryFileType() == FileType.INSTANCE) {
			String controlfilename = selectedfileRight.getName().replace(
					"data", "control");

			if (controlfilename.indexOf("new") >= 0) {
				controlfilename = controlfilename.replace("new_", "");
			}

			String controlfilepath = selectedfileRight.getParentFile()
					.getAbsolutePath();
			logger.log(Level.INFO, controlfilepath + File.separator
					+ controlfilename);
			controlfile = new File(controlfilepath + File.separator
					+ controlfilename);

			if (!controlfile.exists()) {
				JOptionPane.showMessageDialog(parent,
						"Control File not found. Please choose Control File.",
						"Instance", JOptionPane.ERROR_MESSAGE);
				FileUtils fu = new FileUtils();
				LantiqFileFilter ff = new LantiqFileFilter(FileType.SCHEMA,
						"All Schema Files");
				controlfile = fu.openFile(parent, ff, true);
			}

			controlbase = new LantiqMutableTreeNode("BulkData");
			controlTreeModel = new DefaultTreeModel(controlbase);
			controlTree = new JXTree(controlTreeModel);

			try {
				TreeBuilder.loadControlFile(controlTree, controlbase,
						controlfile, FileType.SCHEMA);
			} catch (FileNotFoundException | XMLStreamException e) {
				e.printStackTrace();
			}

			LantiqMutableTreeNode root = (LantiqMutableTreeNode) controlTree
					.getModel().getRoot();
			LantiqMutableTreeNode newRoot = (LantiqMutableTreeNode) root
					.getFirstChild();

			((DefaultTreeModel) controlTree.getModel()).setRoot(newRoot);
			newRoot.setParent(null);

			controlTree.expandAll();

		}
	}

	public void setNodesInvisible(InvisibleTreeModel model, boolean visible) {
		model.activateFilter(visible);
		model.reload();
	}

	public void refreshBufferAfterSync() {
		FileUtils fu = new FileUtils();
		xmlFiles[1] = fu.writeTreeToStream(jTreeRight, getPrimaryFileType(),
				getAccessType());

		TreeBuilder.clearAllDiff(jTreeLeft);
		TreeBuilder.clearAllDiff(jTreeRight);
		
		jTreeLeft.expandAll();
		jTreeRight.expandAll();
		
		try {
			runCompare(false);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	public StringBuffer[] readFilesForCompare() {
		FileUtils fu = new FileUtils();
		StringBuffer xmlBuffer[] = new StringBuffer[2];
		xmlBuffer[0] = fu.readFile(selectedfileLeft);
		xmlBuffer[1] = fu.readFile(selectedfileRight);

		return xmlBuffer;
	}

	public void refreshFilesForCompare(boolean left) {
		FileUtils fu = new FileUtils();
		StringBuffer xmlBuffer[] = new StringBuffer[2];
		if (left)
			xmlBuffer[0] = fu.readFile(selectedfileLeft);
		else
			xmlBuffer[1] = fu.readFile(selectedfileRight);

		xmlFiles = xmlBuffer;
	}

	public void runCompare(boolean readfiles) throws SAXException, IOException {
		logger.log(Level.INFO, null, "Run Compare - " + readfiles);

		setScrollable(false);

		diffrowsfound = new ArrayList<Integer>();
		if (readfiles) {
			xmlFiles = readFilesForCompare();
		}

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreAttributeOrder(true);

		DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(
				xmlFiles[0].toString(), xmlFiles[1].toString()));
		diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
		List<?> allDifferences = diff.getAllDifferences();
		Iterator<?> differences = allDifferences.iterator();
		while (differences.hasNext()) {
			Difference difference = (Difference) differences.next();

			if (difference.getId() == DifferenceEngine.CHILD_NODELIST_SEQUENCE_ID
					|| difference.getId() == DifferenceEngine.ATTR_SEQUENCE_ID)
				continue;

			NodeDetail detail1 = difference.getControlNodeDetail();
			NodeDetail detail2 = difference.getTestNodeDetail();

			Node node1 = detail1.getNode();
			Node node2 = detail2.getNode();

			if (node1 == null || node2 == null || node1.equals("null")
					|| node2.equals("null")) {
				if (node1 == null || node1.equals("null")) {
					TreePath path2 = getPathForNode(detail2);
					if (node2.getNodeType() == Node.TEXT_NODE) {
						int alocationleft = TreeBuilder.getLocationOfNode(
								jTreeLeft, path2.getParentPath(), true);
						int alocationright = TreeBuilder.getLocationOfNode(
								jTreeRight, path2.getParentPath(), true);
						if (alocationleft > 0) {
							setDiffInTree(jTreeLeft, alocationleft,
									DiffType.TEXT);
							setDiffInTree(jTreeRight, alocationright,
									DiffType.TEXT);
						} /*
						 * else { setDiffInTree(jTreeRight, alocationright,
						 * DiffType.ORPHAN); }
						 */
					} else {
						/*
						 * int alocationright = TreeBuilder.getLocationOfNode(
						 * jTreeRight, path2); setDiffInTree(jTreeRight,
						 * alocationright, DiffType.ORPHAN);
						 */
					}
				} else if (node2 == null || node2.equals("null")) {
					TreePath path1 = getPathForNode(detail1);
					if (node1.getNodeType() == Node.TEXT_NODE) {
						int alocationleft = TreeBuilder.getLocationOfNode(
								jTreeLeft, path1.getParentPath(), true);
						int alocationright = TreeBuilder.getLocationOfNode(
								jTreeRight, path1.getParentPath(), true);
						if (alocationright > 0) {
							setDiffInTree(jTreeLeft, alocationleft,
									DiffType.TEXT);
							setDiffInTree(jTreeRight, alocationright,
									DiffType.TEXT);
						} /*
						 * else { setDiffInTree(jTreeLeft, alocationleft,
						 * DiffType.ORPHAN); }
						 */
					} else {
						/*
						 * int alocationleft = TreeBuilder.getLocationOfNode(
						 * jTreeLeft, path1); setDiffInTree(jTreeLeft,
						 * alocationleft, DiffType.ORPHAN);
						 */
					}
				}
			} else if (node1.getNodeType() == node2.getNodeType()) {
				if (node1.getNodeType() == Node.ATTRIBUTE_NODE) {

					TreePath path1 = new TreePath(detail1.getXpathLocation()
							.substring(1).replace("[1]", "").replace("@", "")
							.split("/"));
					TreePath path2 = new TreePath(detail2.getXpathLocation()
							.substring(1).replace("[1]", "").replace("@", "")
							.split("/"));

					if (path1.equals(path2)) {
						int locationleft = TreeBuilder.getLocationOfNode(
								jTreeLeft, path1, true);
						setDiffInTree(jTreeLeft, locationleft, DiffType.VALUE);

						int locationright = TreeBuilder.getLocationOfNode(
								jTreeRight, path2, true);
						setDiffInTree(jTreeRight, locationright, DiffType.VALUE);
					}
				} else if (node1.getNodeType() == Node.ELEMENT_NODE) {

					TreePath path1 = getPathForNode(detail1);
					TreePath path2 = getPathForNode(detail2);

					if (path1.equals(path2)) {
						/*
						 * int locationleft = TreeBuilder.getLocationOfNode(
						 * jTreeLeft, path1); setDiffInTree(jTreeLeft,
						 * locationleft, DiffType.VALUE);
						 * 
						 * int locationright = TreeBuilder.getLocationOfNode(
						 * jTreeRight, path2); setDiffInTree(jTreeRight,
						 * locationright, DiffType.VALUE);
						 */

						if (difference.getDescription().startsWith(
								"attribute name")) {
							String attrib1 = detail1.getValue();
							String attrib2 = detail2.getValue();

							if (attrib1 == null || attrib1.equals("null")) {
								/*
								 * TreePath attrpath2 = getPathForNode(detail2);
								 * attrpath2 = attrpath2
								 * .pathByAddingChild(attrib2); int
								 * alocationright = TreeBuilder
								 * .getLocationOfNode(jTreeRight, attrpath2);
								 * setDiffInTree(jTreeRight, alocationright,
								 * DiffType.ORPHAN);
								 */
							} else if (attrib2 == null
									|| attrib2.equals("null")) {
								/*
								 * TreePath attrpath1 = getPathForNode(detail1);
								 * attrpath1 = attrpath1
								 * .pathByAddingChild(attrib1); int
								 * alocationleft = TreeBuilder
								 * .getLocationOfNode(jTreeLeft, attrpath1);
								 * setDiffInTree(jTreeLeft, alocationleft,
								 * DiffType.ORPHAN);
								 */
							}
						}
					} else {
						/*
						 * int locationleft = TreeBuilder.getLocationOfNode(
						 * jTreeLeft, path1); setDiffInTree(jTreeLeft,
						 * locationleft, DiffType.ORPHAN);
						 * 
						 * int locationright = TreeBuilder.getLocationOfNode(
						 * jTreeRight, path2); setDiffInTree(jTreeRight,
						 * locationright, DiffType.ORPHAN);
						 */
					}
				} else if (node1.getNodeType() == Node.TEXT_NODE
						&& node2.getNodeType() == Node.TEXT_NODE) {
					TreePath path1 = getPathForNode(detail1);
					TreePath path2 = getPathForNode(detail2);

					if (path1.equals(path2)) {
						int locationleft = TreeBuilder.getLocationOfNode(
								jTreeLeft, path1.getParentPath(), true);
						setDiffInTree(jTreeLeft, locationleft, DiffType.VALUE);

						int locationright = TreeBuilder.getLocationOfNode(
								jTreeRight, path2.getParentPath(), true);
						setDiffInTree(jTreeRight, locationright, DiffType.VALUE);
					}
				}
			} else if (node1.getNodeType() != node2.getNodeType()) {

				TreePath path1 = getPathForNode(detail1);
				TreePath path2 = getPathForNode(detail2);

				if (path1.getParentPath().equals(path2.getParentPath())) {
					if (node1.getNodeType() == Node.TEXT_NODE) {
						/*
						 * int alocationright = TreeBuilder.getLocationOfNode(
						 * jTreeRight, path2.getParentPath()); alocationright =
						 * jTreeRight.getSearchable().search(
						 * node2.getNodeName(), alocationright);
						 * setDiffInTree(jTreeRight, alocationright,
						 * DiffType.ORPHAN);
						 */

					} else if (node2.getNodeType() == Node.TEXT_NODE) {
						/*
						 * int alocationleft = TreeBuilder.getLocationOfNode(
						 * jTreeLeft, path1.getParentPath()); alocationleft =
						 * jTreeLeft.getSearchable().search(
						 * node1.getNodeName(), alocationleft);
						 * setDiffInTree(jTreeLeft, alocationleft,
						 * DiffType.ORPHAN);
						 */
					}
				}
			}
		}

		setScrollable(true);
	}

	public TreePath getPathForNode(NodeDetail detail) {
		return new TreePath(detail.getXpathLocation().substring(1)
				.replace("[1]", "").replace("@", "").split("/"));
	}

	public void setDiffInTree(JXTree tree, int location, DiffType type) {
		logger.log(Level.INFO, null, "Set Diff In Tree");

		if (location >= 0) {
			diffrowsfound.add(location);
			TreePath path = tree.getPathForRow(location);
			tree.setSelectionPath(path);
			LantiqMutableTreeNode node = (LantiqMutableTreeNode) tree
					.getLastSelectedPathComponent();
			if (node != null) {
				if (node.getNodetype() == NodeType.SERVICE
						&& type == DiffType.ORPHAN)
					return;

				if (!node.isDiff()) {
					node.setDiff(true);
					node.setDifftype(type);
				}

				if (node.isDiff()) {
					LantiqMutableTreeNode parent = (LantiqMutableTreeNode) node
							.getParent();
					if (parent != null) {
						while (!parent.isRoot()) {
							if (parent != null) {
								if (!parent.isDiff()) {
									parent.setDiff(true);
									parent.setDifftype(DiffType.UNKNOWN);
								}
								parent = (LantiqMutableTreeNode) parent
										.getParent();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		TreeBuilder.treeExpanded(event, jTreeLeft, jTreeRight, leftSearchable,
				rightSearchable);
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		TreeBuilder.treeCollapsed(event, jTreeLeft, jTreeRight, leftSearchable,
				rightSearchable);
	}

	public boolean isLinked() {
		return linked;
	}

	public void setLinked(boolean linked) {
		this.linked = linked;
	}

	public int getSelectedLeftRow() {
		return selectedLeftRow;
	}

	public void setSelectedLeftRow(int selectedRow) {
		this.selectedLeftRow = selectedRow;
	}

	public TreePath getSelectedLeftPath() {
		return selectedLeftPath;
	}

	public void setSelectedLeftPath(TreePath selectedPath) {
		this.selectedLeftPath = selectedPath;
	}

	public int getSelectedRightRow() {
		return selectedRightRow;
	}

	public void setSelectedRightRow(int selectedRightRow) {
		this.selectedRightRow = selectedRightRow;
	}

	public TreePath getSelectedRightPath() {
		return selectedRightPath;
	}

	public void setSelectedRightPath(TreePath selectedRightPath) {
		this.selectedRightPath = selectedRightPath;
	}

	public void setScrollable(boolean scroll) {
		rightSearchable.setScroll(scroll);
		leftSearchable.setScroll(scroll);
	}

	public void setEnabledMenuItem(LantiqMutableTreeNode node) {
		if (getAccessType() == AccessType.READONLY) {
			itemEditRight.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			return;
		}

		switch (node.getNodetype()) {
		case DEVICE:
			itemEditRight.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case SERVICE:
			itemEditRight.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case OBJECT:
			itemEditRight.setEnabled(false);
			itemAddObj.setEnabled(true);
			if (TreeBuilder.checkIfLastSibling(node))
				itemDel.setEnabled(true);
			else
				itemDel.setEnabled(false);
			break;
		case PARAM:
			itemEditRight.setEnabled(true);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case ATTRIB:
			itemEditRight.setEnabled(true);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		}
	}

	public FileType getPrimaryFileType() {
		return primaryFileType;
	}

	public void setPrimaryFileType(FileType filetype) {
		this.primaryFileType = filetype;
	}

	public AccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
	}

	public FileType getSecFileType() {
		return secFileType;
	}

	public void setSecFileType(FileType secFileType) {
		this.secFileType = secFileType;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}