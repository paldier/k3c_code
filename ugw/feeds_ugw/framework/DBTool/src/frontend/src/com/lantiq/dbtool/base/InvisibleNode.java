/******************************************************************************
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software Foundation,
   Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 
*******************************************************************************/
package com.lantiq.dbtool.base;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class InvisibleNode extends DefaultMutableTreeNode {

	protected boolean isVisible;

	public InvisibleNode() {
		this(null);
	}

	public InvisibleNode(Object userObject) {
		this(userObject, true, true);
	}

	public InvisibleNode(Object userObject, boolean allowsChildren,
			boolean isVisible) {
		super(userObject, allowsChildren);
		this.isVisible = isVisible;
	}

	public TreeNode getChildAt(int index, boolean filterIsActive) {
		if (!filterIsActive) {
			return super.getChildAt(index);
		}
		if (children == null) {
			throw new ArrayIndexOutOfBoundsException("node has no children");
		}

		int realIndex = -1;
		int visibleIndex = -1;
		Enumeration e = children.elements();
		while (e.hasMoreElements()) {
			InvisibleNode node = (InvisibleNode) e.nextElement();
			if (node.isVisible()) {
				visibleIndex++;
			}
			realIndex++;
			if (visibleIndex == index) {
				return (TreeNode) children.elementAt(realIndex);
			}
		}

		throw new ArrayIndexOutOfBoundsException("index unmatched");
		// return (TreeNode)children.elementAt(index);
	}
	
	public int getIndex(InvisibleNode node, boolean filterIsActive) {
		if (!filterIsActive) {
			return super.getIndex(node);
		}
		
		if (!super.isNodeChild(node)) {
            return -1;
        }

		int realIndex = -1;
		int invisiblecnt = 0;
		Enumeration e = children.elements();
		while (e.hasMoreElements()) {
			InvisibleNode inode = (InvisibleNode) e.nextElement();
			if (!inode.isVisible()) {
				invisiblecnt++;
			}
			realIndex++;
			if(inode.equals(node)){
				return realIndex - invisiblecnt;
			}
		}

		throw new ArrayIndexOutOfBoundsException("index unmatched");
		// return (TreeNode)children.elementAt(index);
	}

	public int getChildCount(boolean filterIsActive) {
		if (!filterIsActive) {
			return super.getChildCount();
		}
		if (children == null) {
			return 0;
		}

		int count = 0;
		Enumeration e = children.elements();
		while (e.hasMoreElements()) {
			InvisibleNode node = (InvisibleNode) e.nextElement();
			if (node.isVisible()) {
				count++;
			}
		}

		return count;
	}
	
	public void setVisible(boolean visible) {
		this.isVisible = visible;
	}

	public boolean isVisible() {
		return isVisible;
	}
	
	public boolean isLeaf(boolean isFilterActive) {
		if(!isFilterActive)
			return (getChildCount() == 0);
		else
			return (getChildCount(isFilterActive) == 0);
    }

}
