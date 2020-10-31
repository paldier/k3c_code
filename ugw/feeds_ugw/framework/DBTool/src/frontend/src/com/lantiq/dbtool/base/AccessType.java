/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/
package com.lantiq.dbtool.base;

public enum AccessType {
	READONLY{
		@Override
		public String toString() {
			return "ReadOnly";
		}
	},
	READWRITE{
		@Override
		public String toString() {
			return "ReadWrite";
		}
	},
	DONOTOPEN{
		@Override
		public String toString() {
			return "DoNotOpen";
		}
	}
}
