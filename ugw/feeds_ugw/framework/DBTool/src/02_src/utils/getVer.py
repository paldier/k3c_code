#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

PKG_VER = "PKG VER: 1.0.0"
EXE_VER = "EXE VER: 1.0.100"


class getVer():

	def __init__(self):
		pass
		
	def getString(self):
		FULL_VER = PKG_VER + "-" + EXE_VER + "\n"
	
		print FULL_VER
		return FULL_VER

gv = getVer()
str = gv.getString()