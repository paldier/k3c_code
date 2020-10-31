/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.ui;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LantiqChangeListener implements ChangeListener {
	private BoundedRangeModel bndRangeModel;

	public LantiqChangeListener(BoundedRangeModel model) {
		bndRangeModel = model;
	}

	public void stateChanged(ChangeEvent e) {
		BoundedRangeModel sourceModel = (BoundedRangeModel) e.getSource();

		int sourceDiff = sourceModel.getMaximum() - sourceModel.getMinimum();
		int destDiff = bndRangeModel.getMaximum() - bndRangeModel.getMinimum();
		int destValue = sourceModel.getValue();

		if (sourceDiff != destDiff)
			destValue = (destDiff * sourceModel.getValue()) / sourceDiff;

		bndRangeModel.setValue(bndRangeModel.getMinimum() + destValue);
	}
}