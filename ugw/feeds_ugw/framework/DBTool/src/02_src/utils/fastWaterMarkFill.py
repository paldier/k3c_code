
#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import os
import addWaterMarkManual

path = r'D:/mercurial/newWaterMarkScript/DBTool/src/01_db_files/xml_files/02_developer/service_level/'
fileList =  os.listdir(path)

for file in fileList:
	fullFilePath =path+file
	
	if fullFilePath.endswith("_data.xml"):
		addWaterMarkManual.WaterMarkManual(fullFilePath,'instance','ReadWrite')
	
	elif fullFilePath.endswith("_control.xml"):
		addWaterMarkManual.WaterMarkManual(fullFilePath,'schema','ReadWrite')
	
	else:
		print "Skipping file: ",fullFilePath