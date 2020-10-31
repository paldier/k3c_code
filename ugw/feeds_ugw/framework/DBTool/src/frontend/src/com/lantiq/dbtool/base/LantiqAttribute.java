/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/
package com.lantiq.dbtool.base;

import java.io.Serializable;

/*
 * This class was used for display of multiple attributes in a mutable node.
 * Now now used due to change in requirements 
 * 
 */
public class LantiqAttribute implements Comparable<LantiqAttribute>, Serializable{
	private String label;
	private Object value;
	private String valueType;
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
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

	@Override
	public int compareTo(LantiqAttribute o) {
		if(this.getLabel().equals(o.getLabel()))
            return 0;
        else{
            if(this.getLabel().compareToIgnoreCase(o.getLabel()) < 0 )
            	return -1;
            else
            	return 1;
        }
	}
}
