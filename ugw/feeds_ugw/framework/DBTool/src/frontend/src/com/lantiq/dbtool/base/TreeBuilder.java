/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
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

import com.lantiq.dbtool.utils.FileUtils;

public class TreeBuilder {

	private static final Logger logger = LogManager
			.getLogger(TreeBuilder.class);

	public static void buildTree(JXTree tree, LantiqMutableTreeNode current,
			File file, FileType filetype) throws XMLStreamException,
			FileNotFoundException {

		logger.log(Level.INFO, null, "Build Tree");

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = inputFactory
				.createXMLEventReader(new FileInputStream(file));
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLStreamConstants.START_DOCUMENT:
				StartDocument startDocument = (StartDocument) event;
				current.setNodetype(null);
				break;
			case XMLStreamConstants.START_ELEMENT:
				StartElement startElement = (StartElement) event;
				QName elementName = startElement.getName();

				LantiqMutableTreeNode element = new LantiqMutableTreeNode(
						elementName.getLocalPart());
				current.add(element);
				current = element;

				switch (element.getLevel()) {
				case 1:
					element.setNodetype(NodeType.DEVICE);
					break;
				case 2:
					element.setNodetype(NodeType.SERVICE);
					break;
				}

				if (!elementName.getNamespaceURI().equals("")) {
					String prefix = elementName.getPrefix();
					if (prefix.equals("")) {
						prefix = "[None]";
					}

				}

				for (Iterator it = startElement.getAttributes(); it.hasNext();) {
					Attribute attr = (Attribute) it.next();
					setNodeType(element, attr, filetype);
					addAttribute(element, attr);
				}

				break;
			case XMLStreamConstants.END_ELEMENT:
				current = (LantiqMutableTreeNode) current.getParent();
				break;
			case XMLStreamConstants.CHARACTERS:
				Characters characters = (Characters) event;
				if (!characters.isIgnorableWhiteSpace()
						&& !characters.isWhiteSpace()) {
					String data = characters.getData();
					if (data.length() != 0) {
						current.setValue(characters.getData());
					}
				}
				break;
			case XMLStreamConstants.DTD:
				DTD dtde = (DTD) event;
				current.add(new LantiqMutableTreeNode(dtde
						.getDocumentTypeDeclaration()));
			default:
				System.out.println(event.getClass().getName());
			}
		}

		if (filetype == FileType.SCHEMA || filetype == FileType.SCHEMA_ID) {
			setNodeTypesInTree(tree);
		}
	}

	public static void loadControlFile(JXTree tree,
			LantiqMutableTreeNode current, File file, FileType filetype)
			throws XMLStreamException, FileNotFoundException {

		logger.log(Level.INFO, null, "Build Tree");

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader reader = inputFactory
				.createXMLEventReader(new FileInputStream(file));
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			switch (event.getEventType()) {
			case XMLStreamConstants.START_DOCUMENT:
				StartDocument startDocument = (StartDocument) event;
				current.setNodetype(null);
				break;
			case XMLStreamConstants.START_ELEMENT:
				StartElement startElement = (StartElement) event;
				QName elementName = startElement.getName();

				LantiqMutableTreeNode element = new LantiqMutableTreeNode(
						elementName.getLocalPart());
				current.add(element);
				current = element;

				switch (element.getLevel()) {
				case 1:
					element.setNodetype(NodeType.DEVICE);
					break;
				case 2:
					element.setNodetype(NodeType.SERVICE);
					break;
				}

				if (!elementName.getNamespaceURI().equals("")) {
					String prefix = elementName.getPrefix();
					if (prefix.equals("")) {
						prefix = "[None]";
					}

				}

				for (Iterator it = startElement.getAttributes(); it.hasNext();) {
					Attribute attr = (Attribute) it.next();
					setNodeType(element, attr, filetype);
					addAttribute(element, attr);
				}

				break;
			case XMLStreamConstants.END_ELEMENT:
				current = (LantiqMutableTreeNode) current.getParent();
				break;
			case XMLStreamConstants.CHARACTERS:
				Characters characters = (Characters) event;
				if (!characters.isIgnorableWhiteSpace()
						&& !characters.isWhiteSpace()) {
					String data = characters.getData();
					if (data.length() != 0) {
						current.setValue(characters.getData());
					}
				}
				break;
			case XMLStreamConstants.DTD:
				DTD dtde = (DTD) event;
				current.add(new LantiqMutableTreeNode(dtde
						.getDocumentTypeDeclaration()));
			default:
				System.out.println(event.getClass().getName());
			}
		}

		if (filetype == FileType.SCHEMA || filetype == FileType.SCHEMA_ID) {
			setNodeTypesInTree(tree);
		}
	}

	public static void setNodeType(LantiqMutableTreeNode element,
			Attribute attr, FileType filetype) {
		String attrName = attr.getName().toString();

		if (filetype == FileType.INSTANCE || filetype == FileType.INSTANCE_ID
				|| filetype == FileType.MODEL_SCHEMA
				|| filetype == FileType.MODEL_INSTANCE
				&& (attrName.equals("Instance") || attrName.equals("node"))) {
			if (attrName.equals("Instance")) {
				StringBuffer userObject = new StringBuffer(
						(String) element.getUserObject());
				userObject.append(".{").append(attr.getValue()).append("}");
				element.setUserObject(userObject);
			} else if (attrName.equals("node")) {
				if (attr.getValue().equalsIgnoreCase("parameter")) {
					element.setNodetype(NodeType.PARAM);
				} else if (attr.getValue().equalsIgnoreCase("object")) {
					element.setNodetype(NodeType.OBJECT);
				}
				if (attr.getValue().equalsIgnoreCase("service")) {
					element.setNodetype(NodeType.SERVICE);
				}
			}
		}
	}

	public static void addAttribute(LantiqMutableTreeNode element,
			Attribute attr) {
		String attrName = attr.getName().toString();
		LantiqMutableTreeNode attribNode = new LantiqMutableTreeNode(attrName);
		attribNode.setNodetype(NodeType.ATTRIB);
		attribNode.setValue(attr.getValue());
		String attURI = attr.getName().getNamespaceURI();
		if (!attURI.equals("")) {
			String attPrefix = attr.getName().getPrefix();
			if (attPrefix.equals("")) {
				attPrefix = "[None]";
			}
		}
		element.add(attribNode);
	}

	public static void setNodesVisible(JXTree tree, boolean isVisible,
			FileType filetype, FileType secFileType) {
		List<LantiqMutableTreeNode> lst = Collections
				.list(((LantiqMutableTreeNode) tree.getModel().getRoot())
						.breadthFirstEnumeration());

		InvisibleNode inode = null;
		for (LantiqMutableTreeNode node : lst) {
			inode = (InvisibleNode) node;

			if (inode.isRoot())
				continue;

			if (inode.getUserObject().toString().equals("Instance")
					|| inode.getUserObject().toString().equals("node")
					|| inode.getUserObject().toString().equals("oper")) {
				inode.setVisible(isVisible);
			}

			if (filetype == FileType.MODEL_SCHEMA
					|| (filetype == FileType.MODEL_SCHEMA && secFileType == FileType.SCHEMA)) {
				if (node.getNodetype() == NodeType.PARAM) {
					inode.setVisible(isVisible);
					List<LantiqMutableTreeNode> children = Collections
							.list(node.children());
					for (LantiqMutableTreeNode child : children) {
						inode = (InvisibleNode) child;
						inode.setVisible(isVisible);
					}
				} else if (!node.isLeaf()) {
					continue;
				}

				String userObj = inode.getUserObject().toString();
				if (!(userObj.equals("maxEntries")
						|| userObj.equals("minEntries") || userObj
							.equals("multiInst"))) {
					inode.setVisible(isVisible);
				}
			}

			if (!node.isLeaf())
				continue;

			try {
				((DefaultTreeModel) tree.getModel()).reload(inode);
			} catch (ArrayIndexOutOfBoundsException ae) {

			}
		}
	}

	public static void restoreTreeWithInstanceNodes(JXTree tree) {
		List<LantiqMutableTreeNode> lst = Collections
				.list(((LantiqMutableTreeNode) tree.getModel().getRoot())
						.breadthFirstEnumeration());

		for (LantiqMutableTreeNode node : lst) {
			String disstr = node.getUserObject().toString();
			if (disstr.indexOf(".{") > 0) {
				String num = disstr.substring(disstr.indexOf("{") + 1,
						disstr.length() - 1);
				LantiqMutableTreeNode newAttr = new LantiqMutableTreeNode(
						"Instance");
				newAttr.setValue(num);
				newAttr.setNodetype(NodeType.ATTRIB);

				((DefaultTreeModel) tree.getModel()).insertNodeInto(newAttr,
						node, 0);
				node.setUserObject(disstr.substring(0, disstr.indexOf(".{")));
				((DefaultTreeModel) tree.getModel()).nodesChanged(
						node.getParent(), new int[] { 0 });
			}
		}
	}

	public static void setNodeTypesInTree(JXTree tree) {
		List<LantiqMutableTreeNode> lst = Collections
				.list(((LantiqMutableTreeNode) tree.getModel().getRoot())
						.breadthFirstEnumeration());
		for (LantiqMutableTreeNode object : lst) {
			List<LantiqMutableTreeNode> children = Collections.list(object
					.children());
			if (object.getNodetype() == null) {
				boolean nonleafpresent = false;
				for (LantiqMutableTreeNode child : children) {
					if (child.isLeaf()) {
						continue;
					} else {
						nonleafpresent = true;
						break;
					}
				}

				if (nonleafpresent == true) {
					object.setNodetype(NodeType.OBJECT);
				} else {
					object.setNodetype(NodeType.PARAM);
				}

			}
		}
	}

	public static int getLocationOfNode(JXTree tree, TreePath path, boolean diff) {
		int location = -1;
		List<LantiqMutableTreeNode> lst = Collections
				.list(((LantiqMutableTreeNode) tree.getModel().getRoot())
						.breadthFirstEnumeration());
		String pathstr = path.toString();
		for (LantiqMutableTreeNode object : lst) {
			TreePath objpath = new TreePath(object.getPath());
			String objpathstr = objpath.toString();

			if (diff) {
				if (objpathstr.indexOf("1") > 0) {
					objpathstr = objpathstr.replace(".{1}", "");
				}

				if (objpathstr.indexOf("{") > 0) {
					objpathstr = objpathstr.replace("{", "[").replace("}", "]")
							.replace(".", "");
				}
			}

			if (objpathstr.equals(pathstr)) {
				location = tree.getRowForPath(objpath);
				break;
			}
		}
		return location;
	}
	
	public static boolean isOperNodePresent(LantiqMutableTreeNode parentclone){
		List<LantiqMutableTreeNode> children = Collections
				.list(parentclone.children());
		for (LantiqMutableTreeNode child : children) {
			if(child.getNodetype() == NodeType.ATTRIB){
				if(child.getUserObject().toString().equals("oper")){
					return true;
				}
			}
		}
		
		return false;
	}

	public static void clearAllDiff(JXTree tree) {
		List<LantiqMutableTreeNode> lst = Collections
				.list(((LantiqMutableTreeNode) tree.getModel().getRoot())
						.breadthFirstEnumeration());
		for (LantiqMutableTreeNode object : lst) {
			object.setDiff(false);
			object.setDifftype(null);
		}
	}

	public static void copyChildNodes(JXTree tree, LantiqMutableTreeNode node,
			LantiqMutableTreeNode clone) {
		if (!node.isLeaf()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				LantiqMutableTreeNode child = (LantiqMutableTreeNode) node
						.getChildAt(i);
				LantiqMutableTreeNode childclone = (LantiqMutableTreeNode) child
						.clone();
				childclone.setParent(null);
				childclone.setNodetype(child.getNodetype());
				childclone.setDiff(false);
				((DefaultTreeModel) tree.getModel()).insertNodeInto(childclone,
						clone, i);
				copyChildNodes(tree, child, childclone);
			}
		}
	}

	public static void copyChildNodesWONodeType(JXTree tree,
			LantiqMutableTreeNode node, LantiqMutableTreeNode clone) {
		if (!node.isLeaf()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				LantiqMutableTreeNode child = (LantiqMutableTreeNode) node
						.getChildAt(i);
				if (!(child.getUserObject().toString().equals("node")
						|| child.getUserObject().toString().equals("Instance"))) {
					LantiqMutableTreeNode childclone = (LantiqMutableTreeNode) child
							.clone();
					childclone.setParent(null);
					childclone.setNodetype(child.getNodetype());
					((DefaultTreeModel) tree.getModel()).insertNodeInto(
							childclone, clone, i);
					copyChildNodes(tree, child, childclone);
				}
			}
		}
	}

	public static void copyNodeTypeChildNodes(JXTree tree,
			LantiqMutableTreeNode node, LantiqMutableTreeNode clone) {
		if (!node.isLeaf()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				LantiqMutableTreeNode child = (LantiqMutableTreeNode) node
						.getChildAt(i);
				if (child.getUserObject().toString().equals("node")
						|| child.getUserObject().toString().equals("Instance")) {
					LantiqMutableTreeNode childclone = (LantiqMutableTreeNode) child
							.clone();
					childclone.setParent(null);
					childclone.setNodetype(child.getNodetype());
					((DefaultTreeModel) tree.getModel()).insertNodeInto(
							childclone, clone, child.getChildCount());
					copyNodeTypeChildNodes(tree, child, childclone);
				}
			}
		}
	}

	public static int getTypeIndexInParent(LantiqMutableTreeNode node, NodeType type){
		LantiqMutableTreeNode parent = (LantiqMutableTreeNode)node.getParent();
		if(type == NodeType.ATTRIB){
			return parent.getIndex(node);			
		}else if(type == NodeType.PARAM){
			int attribcnt = countNodeTypes(parent, NodeType.ATTRIB);
			return parent.getIndex(node) - attribcnt;
		}else if(type == NodeType.OBJECT){
			int attribcnt = countNodeTypes(parent, NodeType.ATTRIB);
			int paramcnt = countNodeTypes(parent, NodeType.PARAM);
			return parent.getIndex(node) - attribcnt - paramcnt;
		}
		
		return -1;
	}
	
	public static int getIndexOfLast(LantiqMutableTreeNode node, NodeType type) {
		if (!node.isLeaf()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				LantiqMutableTreeNode child = (LantiqMutableTreeNode) node
						.getChildAt(i);
				if (child.getNodetype() != type)
					continue;
				else {
					if (i + 1 < node.getChildCount()) {
						LantiqMutableTreeNode nextchild = (LantiqMutableTreeNode) node
								.getChildAt(i + 1);
						if (child.getNodetype() != nextchild.getNodetype()) {
							return i;
						}
					} else if (i + 1 == node.getChildCount()) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	public static void moveUp(DefaultTreeModel model, LantiqMutableTreeNode node) {
		LantiqMutableTreeNode parent = (LantiqMutableTreeNode) node.getParent();
		LantiqMutableTreeNode prev = (LantiqMutableTreeNode) node
				.getPreviousSibling();
		if (prev != null) {
			if (prev.getNodetype() == node.getNodetype()) {
				int previndex = model.getIndexOfChild(parent, prev);
				int nodeindex = model.getIndexOfChild(parent, node);
				model.removeNodeFromParent(node);
				model.insertNodeInto(node, parent, previndex);
			}
		}
	}

	public static void moveDown(DefaultTreeModel model,
			LantiqMutableTreeNode node) {
		LantiqMutableTreeNode parent = (LantiqMutableTreeNode) node.getParent();
		LantiqMutableTreeNode next = (LantiqMutableTreeNode) node
				.getNextSibling();
		if (next != null) {
			if (next.getNodetype() == node.getNodetype()) {
				int nodeindex = model.getIndexOfChild(parent, node);
				int nextindex = model.getIndexOfChild(parent, next);
				model.removeNodeFromParent(node);
				model.insertNodeInto(node, parent, nextindex);
			}
		}
	}

	public static void deleteNode(JFrame parent, DefaultTreeModel model,
			LantiqMutableTreeNode node, boolean message) {
		if (message) {
			if (!node.isLeaf()) {
				int i = JOptionPane
						.showConfirmDialog(
								parent,
								"This will delete the node and all of its children. Do you want to continue?",
								"Delete Node", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);

				if (i == JOptionPane.YES_OPTION) {
					model.removeNodeFromParent(node);
				}
			} else {
				model.removeNodeFromParent(node);
			}
		} else {
			model.removeNodeFromParent(node);
		}
	}

	public static boolean performValidationForDrop(LantiqMutableTreeNode node,
			LantiqMutableTreeNode newparent) {
		switch (node.getNodetype()) {
		case ATTRIB:
			if (newparent.getNodetype() == NodeType.ATTRIB
					|| newparent.getNodetype() == NodeType.DEVICE) {
				JOptionPane
						.showMessageDialog(null,
								"Cannot Drop Attribute Node to "
										+ newparent.getNodetype().toString()
										+ " Node.", "Error",
								JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				return true;
			}
		case PARAM:
			if (newparent.getNodetype() == NodeType.ATTRIB
					|| newparent.getNodetype() == NodeType.PARAM
					|| newparent.getNodetype() == NodeType.DEVICE) {
				JOptionPane
						.showMessageDialog(null,
								"Cannot Drop Param Node to "
										+ newparent.getNodetype().toString()
										+ " Node.", "Error",
								JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				return true;
			}
		case OBJECT:
			if (newparent.getNodetype() == NodeType.ATTRIB
					|| newparent.getNodetype() == NodeType.PARAM
					|| newparent.getNodetype() == NodeType.DEVICE) {
				JOptionPane
						.showMessageDialog(null,
								"Cannot Drop Object Node to "
										+ newparent.getNodetype().toString()
										+ " Node.", "Error",
								JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				return true;
			}
		case SERVICE:
			if (newparent.getNodetype() == NodeType.ATTRIB
					|| newparent.getNodetype() == NodeType.PARAM
					|| newparent.getNodetype() == NodeType.OBJECT
					|| newparent.getNodetype() == NodeType.SERVICE) {
				JOptionPane
						.showMessageDialog(null,
								"Cannot Drop Service Node to "
										+ newparent.getNodetype().toString()
										+ " Node.", "Error",
								JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				return true;
			}
		case DEVICE:
			if (newparent.getNodetype() == NodeType.ATTRIB
					|| newparent.getNodetype() == NodeType.PARAM
					|| newparent.getNodetype() == NodeType.OBJECT
					|| newparent.getNodetype() == NodeType.SERVICE
					|| newparent.getNodetype() == NodeType.DEVICE) {
				JOptionPane
						.showMessageDialog(null,
								"Cannot Drop Device Node to "
										+ newparent.getNodetype().toString()
										+ " Node.", "Error",
								JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				return true;
			}
		default:
			return true;
		}
	}

	public static HashMap<String, Object> getValuesFromControl(JXTree jTree,
			LantiqMutableTreeNode node) {
		HashMap<String, Object> values = new HashMap<String, Object>();
		LantiqMutableTreeNode clone = (LantiqMutableTreeNode) node.clone();

		clone.setParent((LantiqMutableTreeNode) node.getParent());

		TreeNode[] path = clone.getPath();
		TreeNode[] clonepath = new TreeNode[path.length];

		for (int i = 0; i < path.length; i++) {
			LantiqMutableTreeNode lantiqpathnode = (LantiqMutableTreeNode) path[i];
			LantiqMutableTreeNode clonepathnode = (LantiqMutableTreeNode) lantiqpathnode
					.clone();
			String disstr = clonepathnode.getUserObject().toString();
			if (disstr.indexOf("{") >= 0) {
				disstr = disstr.substring(0, disstr.indexOf("{") - 1);
				clonepathnode.setUserObject(disstr);
			}

			clonepath[i] = clonepathnode;
		}

		TreePath treepath = new TreePath(clonepath);

		int location = TreeBuilder.getLocationOfNode(jTree, treepath, false);
		if (location >= 0) {
			LantiqMutableTreeNode foundnode = (LantiqMutableTreeNode) jTree
					.getPathForRow(location).getLastPathComponent();
			List<LantiqMutableTreeNode> list = Collections.list(foundnode
					.children());
			for (LantiqMutableTreeNode LMTN : list) {
				if (LMTN.getUserObject().equals("maxEntries")
						|| LMTN.getUserObject().equals("minEntries")
						|| LMTN.getUserObject().equals("multiInst")) {
					values.put(LMTN.getUserObject().toString(), LMTN.getValue());

				}
			}
			return values;
		} else {
			return null;
		}
	}

	public static void addObjectNode(JXTree jTree, LantiqMutableTreeNode node,
			int maxEntries) {
		int inum = -1;
		LantiqMutableTreeNode clone = (LantiqMutableTreeNode) node.clone();

		String disstr = clone.getUserObject().toString();
		if (disstr.indexOf("{") >= 0) {
			if (checkIfLastSibling(node)) {
				String num = disstr.substring(disstr.indexOf("{") + 1,
						disstr.length() - 1);
				inum = Integer.parseInt(num) + 1;

				if (inum > maxEntries) {
					JOptionPane.showMessageDialog(null,
							"Total instances should not exceed the maxEntries",
							"Instance Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				disstr = disstr.replace("{" + num + "}", "{" + inum + "}");
				clone.setUserObject(disstr);
			} else {
				addObjectNode(jTree,
						(LantiqMutableTreeNode) node.getNextSibling(),
						maxEntries);
				return;
			}
		} else {
			int num = 1;
			inum = num + 1;

			if (inum > maxEntries) {
				JOptionPane.showMessageDialog(null,
						"Total instances should not exceed the maxEntries",
						"Instance Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			disstr += ".{" + inum + "}";
			clone.setUserObject(disstr);
			String nodestr = node.getUserObject().toString() + ".{" + num + "}";
			node.setUserObject(nodestr);
			((DefaultTreeModel) jTree.getModel()).nodesChanged(
					node.getParent(),
					new int[] { node.getParent().getIndex(node) });
		}

		((DefaultTreeModel) jTree.getModel()).insertNodeInto(clone,
				(LantiqMutableTreeNode) node.getParent(), node.getParent()
						.getIndex(node) + 1);
		((DefaultTreeModel) jTree.getModel()).nodesChanged(node.getParent(),
				new int[] { node.getParent().getIndex(node) + 1 });
		copyChildNodes(jTree, node, clone);

		List<LantiqMutableTreeNode> list = Collections.list(clone.children());
		for (LantiqMutableTreeNode child : list) {
			if (child.getUserObject().equals("Instance")) {
				child.setValue(inum);
			}
		}

	}

	public static boolean checkIfLastSibling(LantiqMutableTreeNode node) {
		boolean prevsib, nextsib;

		LantiqMutableTreeNode prevsibling = (LantiqMutableTreeNode) node
				.getPreviousSibling();
		LantiqMutableTreeNode nextsibling = (LantiqMutableTreeNode) node
				.getNextSibling();

		String disstr = node.getUserObject().toString();

		String prevsibdisstr = "";
		if (prevsibling != null)
			prevsibdisstr = prevsibling.getUserObject().toString();

		String nextsibdisstr = "";
		if (nextsibling != null)
			nextsibdisstr = nextsibling.getUserObject().toString();

		if (disstr.indexOf("{") >= 0) {
			disstr = disstr.substring(0, disstr.indexOf(".") - 1);
			if (prevsibdisstr.indexOf("{") >= 0) {
				prevsibdisstr = prevsibdisstr.substring(0,
						prevsibdisstr.indexOf(".") - 1);
				if (prevsibdisstr.equals(disstr)) {
					prevsib = true;
				} else {
					prevsib = false;
				}
			} else {
				prevsib = false;
			}

			if (nextsibdisstr.indexOf("{") >= 0) {
				nextsibdisstr = nextsibdisstr.substring(0,
						nextsibdisstr.indexOf(".") - 1);
				if (nextsibdisstr.equals(disstr)) {
					nextsib = true;
				} else {
					nextsib = false;
				}
			} else {
				nextsib = false;
			}
		} else {
			// Actually to be chacked for minEntries
			prevsib = false;
			nextsib = false;
		}

		if (prevsib == true && nextsib == true) {
			return false;
		}
		if (prevsib == false && nextsib == true) {
			return false;
		}
		if (prevsib == true && nextsib == false) {
			return true;
		}
		if (prevsib == false && nextsib == false) {
			return true;
		}
		return false;
	}

	public static boolean checkIsLastNodeOfType(LantiqMutableTreeNode node) {
		LantiqMutableTreeNode parent = (LantiqMutableTreeNode) node.getParent();
		int paramcnt = 0, attribcnt = 0;
		if (node.getNodetype() == NodeType.PARAM) {
			paramcnt = countNodeTypes(parent, NodeType.PARAM);
		} else if (node.getNodetype() == NodeType.ATTRIB) {
			attribcnt = countNodeTypes(parent, NodeType.ATTRIB);
		}

		if (paramcnt > 1 || attribcnt > 1) {
			return false;
		} else {
			return true;
		}
	}

	public static int countNodeTypes(LantiqMutableTreeNode node, NodeType type) {
		int count = 0;
		List<LantiqMutableTreeNode> lst = Collections
				.list(((DefaultMutableTreeNode) node).children());

		for (LantiqMutableTreeNode child : lst) {
			if (child.getNodetype() == type) {
				count++;
			}
		}

		return count;
	}

	public static void expandAllAtLevel(JXTree jTree, int level) {
		List<LantiqMutableTreeNode> lst = Collections
				.list(((LantiqMutableTreeNode) jTree.getModel().getRoot())
						.breadthFirstEnumeration());
		for (LantiqMutableTreeNode object : lst) {
			if (object.getLevel() == level) {
				TreePath path = new TreePath(object.getPath());
				jTree.expandPath(path);
			}
		}

	}

	public static void treeCollapsed(TreeExpansionEvent event,
			JXTree jTreeLeft, JXTree jTreeRight,
			LantiqTreeSearchable leftSearchable,
			LantiqTreeSearchable rightSearchable) {
		logger.log(Level.INFO, null, "Tree Collapsed");
		if (event.getSource() == jTreeLeft) {
			TreePath path = event.getPath();
			LantiqMutableTreeNode nodeleft = (LantiqMutableTreeNode) path
					.getLastPathComponent();

			rightSearchable.setScroll(false);
			int row = TreeBuilder.getLocationOfNode(jTreeRight,
					event.getPath(), false);
			if (row > 0) {
				TreePath parentpath = jTreeRight.getPathForRow(row)
						.getParentPath();
				LantiqMutableTreeNode noderight = (LantiqMutableTreeNode) jTreeRight
						.getPathForRow(row).getLastPathComponent();
				if (noderight.getParent().toString()
						.equals(nodeleft.getParent().toString())
						&& path.getParentPath().toString()
								.equals(parentpath.toString())) {
					rightSearchable.setScroll(true);
					TreeBuilder.getLocationOfNode(jTreeRight, event.getPath(),
							false);
					jTreeRight.collapseRow(row);
				} else {
					jTreeRight.collapseRow(row);
					rightSearchable.setScroll(true);
				}
			}
		} else if (event.getSource() == jTreeRight) {
			TreePath path = event.getPath();
			LantiqMutableTreeNode noderight = (LantiqMutableTreeNode) path
					.getLastPathComponent();

			leftSearchable.setScroll(false);
			int row = TreeBuilder.getLocationOfNode(jTreeLeft, event.getPath(),
					false);
			if (row > 0) {
				TreePath parentpath = jTreeLeft.getPathForRow(row)
						.getParentPath();
				LantiqMutableTreeNode nodeleft = (LantiqMutableTreeNode) jTreeLeft
						.getPathForRow(row).getLastPathComponent();
				if (noderight.getParent().toString()
						.equals(nodeleft.getParent().toString())
						&& path.getParentPath().toString()
								.equals(parentpath.toString())) {
					leftSearchable.setScroll(true);
					TreeBuilder.getLocationOfNode(jTreeLeft, event.getPath(),
							false);
					jTreeLeft.collapseRow(row);
				} else {
					jTreeLeft.collapseRow(row);
					leftSearchable.setScroll(true);
				}
			}
		}
	}

	public static void treeExpanded(TreeExpansionEvent event, JXTree jTreeLeft,
			JXTree jTreeRight, LantiqTreeSearchable leftSearchable,
			LantiqTreeSearchable rightSearchable) {
		logger.log(Level.INFO, null, "Tree Expanded");
		if (event.getSource() == jTreeLeft) {
			TreePath path = event.getPath();
			LantiqMutableTreeNode nodeleft = (LantiqMutableTreeNode) path
					.getLastPathComponent();

			rightSearchable.setScroll(false);
			int row = TreeBuilder.getLocationOfNode(jTreeRight,
					event.getPath(), false);
			if (row > 0) {
				TreePath parentpath = jTreeRight.getPathForRow(row)
						.getParentPath();
				LantiqMutableTreeNode noderight = (LantiqMutableTreeNode) jTreeRight
						.getPathForRow(row).getLastPathComponent();
				if (noderight.getParent().toString()
						.equals(nodeleft.getParent().toString())
						&& path.getParentPath().toString()
								.equals(parentpath.toString())) {
					rightSearchable.setScroll(true);
					TreeBuilder.getLocationOfNode(jTreeRight, event.getPath(),
							false);
					jTreeRight.expandRow(row);
				} else {
					jTreeRight.expandRow(row);
					rightSearchable.setScroll(true);
				}
			}
		} else if (event.getSource() == jTreeRight) {
			TreePath path = event.getPath();
			LantiqMutableTreeNode noderight = (LantiqMutableTreeNode) path
					.getLastPathComponent();

			leftSearchable.setScroll(false);
			int row = TreeBuilder.getLocationOfNode(jTreeLeft, event.getPath(),
					false);
			if (row > 0) {
				TreePath parentpath = jTreeLeft.getPathForRow(row)
						.getParentPath();
				LantiqMutableTreeNode nodeleft = (LantiqMutableTreeNode) jTreeLeft
						.getPathForRow(row).getLastPathComponent();
				if (noderight.getParent().toString()
						.equals(nodeleft.getParent().toString())
						&& path.getParentPath().toString()
								.equals(parentpath.toString())) {
					leftSearchable.setScroll(true);
					TreeBuilder.getLocationOfNode(jTreeLeft, event.getPath(),
							false);
					jTreeLeft.expandRow(row);
				} else {
					jTreeLeft.expandRow(row);
					leftSearchable.setScroll(true);
				}
			}
		}
	}
}
