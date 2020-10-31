/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/
package com.lantiq.dbtool.base;

public enum ModelType {
	HE{
		@Override
		public String toString() {
			return "HE";
		}
	},
	EL{
		@Override
		public String toString() {
			return "EL";
		}
	},
	MR{
		@Override
		public String toString() {
			return "MR";
		}
	},
	UK{ //Unknown
		@Override
		public String toString() {
			return "UK";
		}
	}
}