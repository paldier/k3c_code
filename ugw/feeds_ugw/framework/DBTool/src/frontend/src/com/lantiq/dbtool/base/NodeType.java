/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.base;

public enum NodeType {
	DEVICE{
		@Override
		public String toString() {
			return "Device";
		}
	},SERVICE{
		@Override
		public String toString() {
			return "Service";
		}
	},OBJECT{
		@Override
		public String toString() {
			return "Object";
		}
	},PARAM{
		@Override
		public String toString() {
			return "Parameter";
		}
	},ATTRIB{
		@Override
		public String toString() {
			return "Attribute";
		}
	}
}
