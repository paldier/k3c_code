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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.lantiq.dbtool.base.AccessType;
import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.base.InvisibleTreeModel;
import com.lantiq.dbtool.base.LantiqAttribute;
import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.base.TreeBuilder;
import com.lantiq.dbtool.utils.FileUtils;
import com.lantiq.dbtool.utils.LantiqFileFilter;
import com.lantiq.dbtool.utils.PythonExecutor;

public class LantiqTreeViewerPanel extends JPanel implements ActionListener {

	private static final Logger logger = LogManager
			.getLogger(LantiqTreeViewerPanel.class);

	private LantiqMutableTreeNode base, controlbase;
	private InvisibleTreeModel defaultTreeModel; 
	private DefaultTreeModel controlTreeModel;
	private JXTree jxTree, controlTree;
	private JPopupMenu popupMenu;
	private JMenuItem itemEdit, itemAddSrvc, itemAddObj, itemAddParam,
			itemAddAttr, itemDel, itemMoveUp, itemMoveDwn;
	private File selectedFile;
	protected int selRow;
	protected TreePath selPath;
	private JFrame parent;
	private FileType fileType;
	private AccessType accessType;
	private int depth;
	private boolean dirty = false;

	public LantiqTreeViewerPanel(JFrame parent, File file)
			throws XMLStreamException, FileNotFoundException {
		this.parent = parent;
		selectedFile = file;
		initComponents();
		setTypes();
		TreeBuilder.buildTree(jxTree, base, file, getFileType());
		loadSchemaFile();
		changeRootNode();
		addPopupMenu();
		setTreeProperties();
		TreeBuilder
				.setNodesVisible(jxTree, false, getFileType(), getFileType());
		setNodesInvisible(true);
		setLayout(new BorderLayout());
		add(new JScrollPane(jxTree), BorderLayout.CENTER);
		setVisible(true);
	}

	private void loadSchemaFile() {
		File controlfile = null;
		if (getFileType() == FileType.INSTANCE) {
			String controlfilename = selectedFile.getName().replace("data",
					"control");
			
			if(controlfilename.indexOf("new") >= 0){
				controlfilename = controlfilename.replace("new_", "");
			}
			
			String controlfilepath = selectedFile.getParentFile()
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
			
			LantiqMutableTreeNode root = (LantiqMutableTreeNode) controlTree.getModel()
					.getRoot();
			LantiqMutableTreeNode newRoot = (LantiqMutableTreeNode) root
					.getFirstChild();

			((DefaultTreeModel) controlTree.getModel()).setRoot(newRoot);
			newRoot.setParent(null);
			
			controlTree.expandAll();

		}
	}

	public void setTypes() {
		FileType type = getFileType(selectedFile);
		setFileType(type);
		AccessType atype = getAccessType(selectedFile);
		setAccessType(atype);
	}

	public void setNodesInvisible(boolean visible) {
		defaultTreeModel.activateFilter(visible);
		defaultTreeModel.reload();
	}

	public AccessType getAccessType(File file) {
		PythonExecutor exec = new PythonExecutor(parent);
		return exec.execPythonScriptGetAccessType(file);
	}

	public FileType getFileType(File file) {
		PythonExecutor exec = new PythonExecutor(parent);
		return exec.execPythonScriptGetWaterMark(file);
	}

	public void changeRootNode() {
		LantiqMutableTreeNode root = (LantiqMutableTreeNode) jxTree.getModel()
				.getRoot();
		LantiqMutableTreeNode newRoot = (LantiqMutableTreeNode) root
				.getFirstChild();

		((DefaultTreeModel) jxTree.getModel()).setRoot(newRoot);
		newRoot.setParent(null);
	}

	public void initComponents() {
		base = new LantiqMutableTreeNode("BulkData");
		defaultTreeModel = new InvisibleTreeModel(base);
		jxTree = new JXTree(defaultTreeModel);
	}

	public void setTreeProperties() {
		jxTree.setName("Center");
		jxTree.setCellRenderer(new LantiqRenderer());
		ToolTipManager.sharedInstance().registerComponent(jxTree);
		jxTree.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				selRow = jxTree.getRowForLocation(e.getX(), e.getY());
				selPath = jxTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 1) {
						if (SwingUtilities.isRightMouseButton(e)) {
							jxTree.setSelectionPath(selPath);
							LantiqMutableTreeNode node = (LantiqMutableTreeNode) jxTree
									.getLastSelectedPathComponent();
							setEnabledMenuItem(node);
							popupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
				}
			}
		});
		jxTree.setRolloverEnabled(true);
		jxTree.addHighlighter(new ColorHighlighter(
				HighlightPredicate.ROLLOVER_ROW, null, Color.RED));

		setDepth(((DefaultMutableTreeNode) defaultTreeModel.getRoot())
				.getDepth());
		((LantiqFrame) parent).getSlider().setMaximum(getDepth() - 1);
	}

	public void addPopupMenu() {
		if (getFileType() == FileType.INSTANCE) {
			addInstancePopupMenu();
		} else if (getFileType() == FileType.SCHEMA) {
			addSchemaPopupMenu();
		} else if (getFileType() == FileType.MODEL_SCHEMA
				|| getFileType() == FileType.MODEL_INSTANCE) {
			addModelPopupMenu();
		}
	}

	private void addModelPopupMenu() {
		popupMenu = new JPopupMenu();

		itemEdit = new JMenuItem("Edit");
		itemDel = new JMenuItem("Delete");

		itemEdit.addActionListener(this);
		itemDel.addActionListener(this);

		popupMenu.add(itemEdit);
		popupMenu.addSeparator();
		popupMenu.add(itemDel);

	}

	private void addSchemaPopupMenu() {
		popupMenu = new JPopupMenu();

		itemEdit = new JMenuItem("Edit");
		itemAddSrvc = new JMenuItem("Add Service");
		itemAddObj = new JMenuItem("Add Object");
		itemAddParam = new JMenuItem("Add Parameter");
		itemAddAttr = new JMenuItem("Add Attribute");
		itemDel = new JMenuItem("Delete");
		itemMoveUp = new JMenuItem("Move Up");
		itemMoveDwn = new JMenuItem("Move Down");

		itemEdit.addActionListener(this);
		itemAddSrvc.addActionListener(this);
		itemAddObj.addActionListener(this);
		itemAddParam.addActionListener(this);
		itemAddAttr.addActionListener(this);
		itemDel.addActionListener(this);
		itemMoveUp.addActionListener(this);
		itemMoveDwn.addActionListener(this);

		itemEdit.setEnabled(false);
		itemAddSrvc.setEnabled(false);
		itemAddObj.setEnabled(false);
		itemAddParam.setEnabled(false);
		itemAddAttr.setEnabled(false);
		itemDel.setEnabled(false);
		itemMoveUp.setEnabled(false);
		itemMoveDwn.setEnabled(false);

		popupMenu.add(itemEdit);
		popupMenu.addSeparator();
		popupMenu.add(itemAddSrvc);
		popupMenu.add(itemAddObj);
		popupMenu.add(itemAddParam);
		popupMenu.add(itemAddAttr);
		popupMenu.addSeparator();
		popupMenu.add(itemDel);
		popupMenu.add(itemMoveUp);
		popupMenu.add(itemMoveDwn);
	}

	private void addInstancePopupMenu() {
		popupMenu = new JPopupMenu();

		itemEdit = new JMenuItem("Edit");
		itemAddObj = new JMenuItem("Add");
		itemAddObj.setActionCommand("Add Object");
		itemDel = new JMenuItem("Delete");

		itemEdit.addActionListener(this);
		itemAddObj.addActionListener(this);
		itemDel.addActionListener(this);

		itemEdit.setEnabled(false);
		itemAddObj.setEnabled(false);
		itemDel.setEnabled(false);

		popupMenu.add(itemEdit);
		popupMenu.addSeparator();
		popupMenu.add(itemAddObj);
		popupMenu.addSeparator();
		popupMenu.add(itemDel);
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}

	public JXTree getJTree() {
		return jxTree;
	}

	public void setJTree(JXTree jTree) {
		this.jxTree = jTree;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		LantiqInputDialog dialog = null;
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		LantiqMutableTreeNode node = (LantiqMutableTreeNode) jxTree
				.getLastSelectedPathComponent();
		String cmd = e.getActionCommand();
		switch (cmd) {
		case "Edit":
			dialog = new LantiqInputDialog(node, false, null, true,
					getFileType());
			dialog.setTree(jxTree);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setLocation(screensize.width / 2 - 125,
					screensize.height / 2 - 200);
			dialog.pack();
			dialog.setVisible(true);
			break;
		case "Add Object":
			if (getFileType() == FileType.INSTANCE) {
				defaultTreeModel.activateFilter(false);
				defaultTreeModel.reload();
				TreePath parentpath = new TreePath(((LantiqMutableTreeNode)node.getParent()).getPath());
				
				HashMap<String, Object> map = TreeBuilder.getValuesFromControl(controlTree, node);
				int iMax = 0;
				String maxEntries = (String)map.get("maxEntries");
				if(maxEntries.equals("unbounded")){
					iMax = Integer.MAX_VALUE;
				}else{
					iMax = Integer.parseInt(maxEntries);
				}
				TreeBuilder.addObjectNode(jxTree, node, iMax);
				
				defaultTreeModel.activateFilter(true);
				defaultTreeModel.reload();
				jxTree.expandPath(parentpath);
				break;
			}
		case "Add Service":
		case "Add Parameter":
		case "Add Attribute":
			dialog = new LantiqInputDialog(node, true, cmd, true, getFileType());
			dialog.setTree(jxTree);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setLocation(screensize.width / 2 - 125,
					screensize.height / 2 - 200);
			dialog.pack();
			dialog.setVisible(true);
			break;
		case "Delete":
			if (getFileType() == FileType.INSTANCE) {
				defaultTreeModel.activateFilter(false);
				defaultTreeModel.reload();
				TreePath parentpath = new TreePath(((LantiqMutableTreeNode)node.getParent()).getPath());
				
				HashMap<String, Object> map = TreeBuilder.getValuesFromControl(controlTree, node);
				
				String minEntries = (String)map.get("minEntries");
				int iMin = Integer.parseInt(minEntries);
				
				String disstr = node.getUserObject().toString();
				if (disstr.indexOf("{") >= 0) {
					String num = disstr.substring(disstr.indexOf("{") + 1,
							disstr.length() - 1);
					int inum = Integer.parseInt(num) - 1;
					if(inum < iMin){
						JOptionPane.showMessageDialog(null,
								"Total instances should not be less than minEntries",
								"Instance Error", JOptionPane.ERROR_MESSAGE);
						return;
					}else{					
						deleteNode(node);
					}
				}else{
					int inum = 0;
					if(inum < iMin){
						JOptionPane.showMessageDialog(null,
								"Total instances should not be less than minEntries",
								"Instance Error", JOptionPane.ERROR_MESSAGE);
						return;
					}else{					
						deleteNode(node);
					}
				}
				defaultTreeModel.activateFilter(true);
				defaultTreeModel.reload();
				jxTree.expandPath(parentpath);
			}else{
				deleteNode(node);
			}
			break;
		case "Move Up":
			TreeBuilder.moveUp(defaultTreeModel, node);
			break;
		case "Move Down":
			TreeBuilder.moveDown(defaultTreeModel, node);
			break;
		}

		if(!(cmd.equals("Move Up") || cmd.equals("Move Down"))){
			setDirty(true);
		}
		
		dialog = null;
	}

	private void deleteNode(LantiqMutableTreeNode node) {
		if (getFileType() == FileType.MODEL_SCHEMA
				|| getFileType() == FileType.MODEL_INSTANCE) {
			setNodesInvisible(false);
		}

		TreePath path = new TreePath(
				((LantiqMutableTreeNode) node.getParent()).getPath());

		if (node.isLeaf())
			TreeBuilder.deleteNode(parent, defaultTreeModel, node, false);
		else
			TreeBuilder.deleteNode(parent, defaultTreeModel, node, true);

		if (getFileType() == FileType.MODEL_SCHEMA
				|| getFileType() == FileType.MODEL_INSTANCE) {
			setNodesInvisible(true);

			jxTree.expandPath(path);
		}
	}

	public void setEnabledMenuItem(LantiqMutableTreeNode node) {
		if (getFileType() == FileType.SCHEMA) {
			setEnabledSchemaMenuItem(node);
		} else if (getFileType() == FileType.INSTANCE) {
			setEnabledInstanceMenuItem(node);
		} else if (getFileType() == FileType.MODEL_INSTANCE
				|| getFileType() == FileType.MODEL_SCHEMA) {
			setEnabledModelMenuItem(node);
		}
	}

	private void setEnabledModelMenuItem(LantiqMutableTreeNode node) {
		if (getAccessType() == AccessType.READONLY) {
			itemEdit.setEnabled(false);
			itemDel.setEnabled(false);
			return;
		}

		switch (node.getNodetype()) {
		case DEVICE:
			itemEdit.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case SERVICE:
			itemEdit.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case OBJECT:
			itemEdit.setEnabled(false);
			if (TreeBuilder.checkIfLastSibling(node))
				itemDel.setEnabled(true);
			else
				itemDel.setEnabled(false);
			break;
		case PARAM:
			itemEdit.setEnabled(true);
			itemDel.setEnabled(false);
			break;
		case ATTRIB:
			itemEdit.setEnabled(true);
			itemDel.setEnabled(true);
			break;
		}
	}

	private void setEnabledInstanceMenuItem(LantiqMutableTreeNode node) {
		if (getAccessType() == AccessType.READONLY) {
			itemEdit.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			return;
		}

		switch (node.getNodetype()) {
		case DEVICE:
			itemEdit.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case SERVICE:
			itemEdit.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case OBJECT:
			itemEdit.setEnabled(false);
			itemAddObj.setEnabled(true);
			if (TreeBuilder.checkIfLastSibling(node))
				itemDel.setEnabled(true);
			else
				itemDel.setEnabled(false);
			break;
		case PARAM:
			itemEdit.setEnabled(true);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		case ATTRIB:
			itemEdit.setEnabled(true);
			itemAddObj.setEnabled(false);
			itemDel.setEnabled(false);
			break;
		}
	}

	private void setEnabledSchemaMenuItem(LantiqMutableTreeNode node) {
		if (getAccessType() == AccessType.READONLY) {
			itemEdit.setEnabled(false);
			itemAddSrvc.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemAddParam.setEnabled(false);
			itemAddAttr.setEnabled(false);
			itemDel.setEnabled(false);
			itemMoveUp.setEnabled(false);
			itemMoveDwn.setEnabled(false);
			return;
		}

		switch (node.getNodetype()) {
		case DEVICE:
			itemEdit.setEnabled(false);
			itemAddSrvc.setEnabled(true);
			itemAddObj.setEnabled(false);
			itemAddParam.setEnabled(false);
			itemAddAttr.setEnabled(false);
			itemDel.setEnabled(false);
			itemMoveUp.setEnabled(false);
			itemMoveDwn.setEnabled(false);
			break;
		case SERVICE:
			itemEdit.setEnabled(true);
			itemAddSrvc.setEnabled(false);
			itemAddObj.setEnabled(true);
			itemAddParam.setEnabled(true);
			itemAddAttr.setEnabled(true);
			itemDel.setEnabled(true);
			itemMoveUp.setEnabled(true);
			itemMoveDwn.setEnabled(true);
			break;
		case OBJECT:
			itemEdit.setEnabled(true);
			itemAddSrvc.setEnabled(false);
			itemAddObj.setEnabled(true);
			itemAddParam.setEnabled(true);
			itemAddAttr.setEnabled(true);
			itemDel.setEnabled(true);
			itemMoveUp.setEnabled(true);
			itemMoveDwn.setEnabled(true);
			break;
		case PARAM:
			itemEdit.setEnabled(true);
			itemAddSrvc.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemAddParam.setEnabled(false);
			itemAddAttr.setEnabled(true);
			if (!TreeBuilder.checkIsLastNodeOfType(node)) {
				itemDel.setEnabled(true);
			} else {
				itemDel.setEnabled(false);
			}
			itemMoveUp.setEnabled(true);
			itemMoveDwn.setEnabled(true);
			break;
		case ATTRIB:
			itemEdit.setEnabled(true);
			itemAddSrvc.setEnabled(false);
			itemAddObj.setEnabled(false);
			itemAddParam.setEnabled(false);
			itemAddAttr.setEnabled(false);
			if (!TreeBuilder.checkIsLastNodeOfType(node)) {
				itemDel.setEnabled(true);
			} else {
				itemDel.setEnabled(false);
			}
			itemMoveUp.setEnabled(true);
			itemMoveDwn.setEnabled(true);
			break;
		}
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public AccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(AccessType accessType) {
		this.accessType = accessType;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void expandAllAtLevel(int level) {
		TreeBuilder.expandAllAtLevel(jxTree, level);
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

}