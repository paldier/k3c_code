/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/
package com.lantiq.dbtool.base;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class LantiqTransferable implements Transferable {

	private LantiqMutableTreeNode transnode;
	
	private static DataFlavor customFlavor = new DataFlavor(
			LantiqMutableTreeNode.class, "LantiqMutableTreeNode");
	
	private static DataFlavor[] suppFlavor = {customFlavor};

	public LantiqTransferable(LantiqMutableTreeNode node) {
		this.transnode = node;
	}
	
	public LantiqMutableTreeNode getTransnode() {
		return transnode;
	}

	public void setTransnode(LantiqMutableTreeNode transnode) {
		this.transnode = transnode;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return suppFlavor;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if(flavor.equals(customFlavor)){
			return true;
		}
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException {
		if(flavor.equals(customFlavor)){
			return transnode;
		}else{
			throw new UnsupportedFlavorException(flavor);
		}
	}
	
	public static DataFlavor getCustomFlavor() {
		return customFlavor;
	}

	public static void setCustomFlavor(DataFlavor customFlavor) {
		LantiqTransferable.customFlavor = customFlavor;
	}

	public static DataFlavor[] getSuppFlavor() {
		return suppFlavor;
	}

	public static void setSuppFlavor(DataFlavor[] suppFlavor) {
		LantiqTransferable.suppFlavor = suppFlavor;
	}

}
