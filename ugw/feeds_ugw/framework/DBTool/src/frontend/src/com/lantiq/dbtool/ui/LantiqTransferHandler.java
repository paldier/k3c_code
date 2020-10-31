/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.ui;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTree;

import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.LantiqTransferable;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.base.TreeBuilder;
import com.lantiq.dbtool.utils.FileUtils;

public class LantiqTransferHandler extends TransferHandler {

	private static final Logger logger = LogManager
			.getLogger(LantiqTransferHandler.class);
	TreeModel dtm;
	JXTree jxtree;
	LantiqSchemaComparePanel parentpanel;

	public LantiqTransferHandler(LantiqSchemaComparePanel panel, JXTree tree,
			TreeModel treemodel) {
		super();
		jxtree = tree;
		dtm = treemodel;
		parentpanel = panel;
	}

	public boolean canImport(TransferHandler.TransferSupport support) {

		if (!support
				.isDataFlavorSupported(LantiqTransferable.getCustomFlavor())
				|| !support.isDrop()) {
			return false;
		}

		JXTree.DropLocation dropLocation = (JXTree.DropLocation) support
				.getDropLocation();

		return dropLocation.getPath() != null;
	}

	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		if (support.getComponent().getName().equals("Left")) {
			return false;
		}

		JXTree.DropLocation dropLocation = (JXTree.DropLocation) support
				.getDropLocation();

		TreePath path = dropLocation.getPath();

		Transferable transferable = support.getTransferable();

		LantiqMutableTreeNode transferData;
		try {
			transferData = (LantiqMutableTreeNode) transferable
					.getTransferData(LantiqTransferable.getCustomFlavor());
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
			return false;
		}

		// Logic to Add Delete Move copy goes here

		int childIndex = dropLocation.getChildIndex();
		if (childIndex == -1) {
			childIndex = dtm.getChildCount(path.getLastPathComponent());
		}

		LantiqMutableTreeNode node = transferData;

		LantiqMutableTreeNode newparent = (LantiqMutableTreeNode) path
				.getLastPathComponent();

		if (!TreeBuilder.performValidationForDrop(node, newparent))
			return false;

		parentpanel.setTreeExpansionListeners(false);

		// Get Parent
		LantiqMutableTreeNode parent = (LantiqMutableTreeNode) transferData
				.getParent();
		int location = TreeBuilder.getLocationOfNode(jxtree,
				new TreePath(node.getPath()), false);

		LantiqMutableTreeNode clone = (LantiqMutableTreeNode) node.clone();
		
		boolean nodepresent = false;
		LantiqMutableTreeNode child = null;
		int childindex = -1;
		for (int i = 0; i < newparent.getChildCount(); i++) {
			child = (LantiqMutableTreeNode) newparent.getChildAt(i);
			TreePath childpath = new TreePath(child.getPath());
			if (child.getUserObject().toString()
					.equals(clone.getUserObject().toString())) {
				nodepresent = true;
				childindex = i;
				break;
			}
		}

		if (nodepresent && child != null) {
			int option = JOptionPane
					.showConfirmDialog(
							null,
							"This will Overwrite Node on Right. Do you want to continue?",
							"Overwrite", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				child.setValue(node.getValue());
				
				if(child.getChildCount() > 0){
					child.removeAllChildren();
					((DefaultTreeModel) jxtree.getModel()).reload(child);
					TreeBuilder.copyChildNodes(jxtree, node, child);
				}
				
				((DefaultTreeModel) jxtree.getModel()).nodesChanged(newparent,
						new int[] { childindex });
				
				TreePath nodePath = new TreePath(node.getPath());
				TreePath clonePath = new TreePath(child.getPath());
				if (nodePath.toString().equals(clonePath.toString())) {
					node.setDiff(false);
					child.setDiff(false);
				} else {
					node.setDiff(true);
					child.setDiff(true);
				}
			}
		} else {
			clone.setParent(newparent);

			// have to set child index earlier
			TreePath nodePath = new TreePath(node.getPath());
			TreePath clonePath = new TreePath(clone.getPath());
			if (nodePath.toString().equals(clonePath.toString())) {
				childIndex = parent.getIndex(node);
			} else {
				childIndex = newparent.getChildCount();
			}

			clone.setParent(null);
			if (childIndex >= newparent.getChildCount())
				childIndex = newparent.getChildCount();

			((DefaultTreeModel) dtm).insertNodeInto(clone, newparent,
					childIndex);

			TreeBuilder.copyChildNodes(jxtree, node, clone);

			if (nodePath.toString().equals(clonePath.toString())) {
				node.setDiff(false);
				clone.setDiff(false);
			} else {
				node.setDiff(true);
				clone.setDiff(true);
			}

			TreePath newPath = path.pathByAddingChild(clone);
			jxtree.makeVisible(newPath);
			jxtree.scrollRectToVisible(jxtree.getPathBounds(newPath));
		}	

		parentpanel.refreshBufferAfterSync();
		parentpanel.setTreeExpansionListeners(true);

		return true;
	}

	protected Transferable createTransferable(JComponent c) {
		JXTree tree = (JXTree) c;
		LantiqMutableTreeNode node = (LantiqMutableTreeNode) tree
				.getLastSelectedPathComponent();
		logger.log(Level.INFO, null, "Create At" + node.toString());
		return new LantiqTransferable(node);
	}

	public int getSourceActions(JComponent c) {
		return COPY;
	}

	protected void exportDone(JComponent c, Transferable data, int action) {
		if (action != MOVE) {
			return;
		}
		JXTree tree = (JXTree) c;
		LantiqMutableTreeNode node = (LantiqMutableTreeNode) tree
				.getLastSelectedPathComponent();
		logger.log(Level.INFO, null, "Export Done at" + node.toString());
	}
}
