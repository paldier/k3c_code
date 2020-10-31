/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/
package com.lantiq.dbtool.base;

public enum FileType {
	INSTANCE{
		@Override
		public String toString() {
			return "instance";
		}
	},
	SCHEMA{
		@Override
		public String toString() {
			return "schema";
		}
	},
	MODEL{
		@Override
		public String toString() {
			return "model";
		}
	},
	SCHEMA_ID{
		@Override
		public String toString() {
			return "schema_id";
		}
	},
	INSTANCE_ID{
		@Override
		public String toString() {
			return "instance_id";
		}
	},
	MODEL_SCHEMA{
		@Override
		public String toString() {
			return "model_schema";
		}
	},
	MODEL_INSTANCE{
		@Override
		public String toString() {
			return "model_instance";
		}
	},
	SYNTAX{
		@Override
		public String toString() {
			return "syntax";
		}
	},
	DATATYPE{
		@Override
		public String toString() {
			return "datatype";
		}
	},
	UNKNOWN{
		@Override
		public String toString() {
			return "unknown";
		}
	},
	ALLBUTUNKNOWN{
		@Override
		public String toString() {
			return "all";
		}
	}
}
