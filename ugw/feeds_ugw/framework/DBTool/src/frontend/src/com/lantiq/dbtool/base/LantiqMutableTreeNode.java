/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/
package com.lantiq.dbtool.base;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

public class LantiqMutableTreeNode extends InvisibleNode implements
		Serializable {
	private NodeType nodetype;
	private Object value;
	private String valueType;
	private boolean diff;
	private DiffType difftype;

	String brStr = new String("<br></br>");
	String eqStr = new String(" = ");

	public LantiqMutableTreeNode() {
		super();
	}

	public LantiqMutableTreeNode(Object userObject, boolean allowsChildren, boolean isVisible) {
		super(userObject, allowsChildren, isVisible);
	}

	public LantiqMutableTreeNode(Object userObject) {
		super(userObject);
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;		
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public NodeType getNodetype() {
		return nodetype;
	}

	public void setNodetype(NodeType nodetype) {
		this.nodetype = nodetype;
	}

	public boolean isDiff() {
		return diff;
	}

	public void setDiff(boolean diff) {
		this.diff = diff;
	}

	public DiffType getDifftype() {
		return difftype;
	}

	public void setDifftype(DiffType difftype) {
		this.difftype = difftype;
	}

	/*@Override
	public String toString() {
		if(value != null)
			return getUserObject()+ "=" + value;
		else
			return getUserObject().toString();
	}*/	
}
