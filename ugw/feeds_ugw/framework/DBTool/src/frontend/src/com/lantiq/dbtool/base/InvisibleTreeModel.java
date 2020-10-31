/**************************************************************************
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
 
*****************************************************************************/

package com.lantiq.dbtool.base;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class InvisibleTreeModel extends DefaultTreeModel {

	protected boolean filterIsActive;

	public InvisibleTreeModel(TreeNode root) {
		this(root, false);
	}

	public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren) {
		this(root, false, false);
	}

	public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren,
			boolean filterIsActive) {
		super(root, asksAllowsChildren);
		this.filterIsActive = filterIsActive;
	}

	public void activateFilter(boolean newValue) {
		filterIsActive = newValue;
	}

	public boolean isActivatedFilter() {
		return filterIsActive;
	}

	public Object getChild(Object parent, int index) {
		if (filterIsActive) {
			if (parent instanceof InvisibleNode) {
				return ((InvisibleNode) parent).getChildAt(index,
						filterIsActive);
			}
		}
		return ((TreeNode) parent).getChildAt(index);
	}

	public int getChildCount(Object parent) {
		if (filterIsActive) {
			if (parent instanceof InvisibleNode) {
				return ((InvisibleNode) parent).getChildCount(filterIsActive);
			}
		}
		return ((TreeNode) parent).getChildCount();
	}

}
