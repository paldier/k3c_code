
#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

'''
This script converts a control-XML to data-XML.
In this process, all the attributes are stripped off except multiInst="yes"
continued... multiInst="yes" is replaced with Instance="1" attribute
'''
import sys,os
import shutil
import subprocess

sys.path.append('modules')

from removeAllAttributes import removeAllAttributes
from addNodeAttribute import addNodeAttribute
import c2dMultiInstanceCreator

sys.path.append(r'../action/')
from pathCorrect import pathCorrect

import addWaterMark


class c2d():

	def __init__(self):
		pass

	def c2dConverter(self,controlXMLFile,dataXMLFile):
		print "Control File: ",controlXMLFile
		print "Data File: ",dataXMLFile
		
		
		if not(os.path.lexists(controlXMLFile)):
			print "Error: ", controlXMLFile, " does not exist!"
			return None
		
		if not(controlXMLFile.lower().endswith('.xml')):
			print "Error: ", controlXMLFile, " is not an XML file!"
			return None
		
		
		chkWm = addWaterMark.waterMark()
		if( False == chkWm.isWaterMarked(controlXMLFile)):
			chkWm.addWaterMark(controlXMLFile,"schema","ReadWrite")
			
		objMI = c2dMultiInstanceCreator.multiInstanceCreator()
		objMI.main(controlXMLFile,dataXMLFile)
		
		removeAllAttributes(dataXMLFile)
		addNodeAttribute(dataXMLFile)
		
		objWM = addWaterMark.waterMark()
		objWM.modifyWaterMark(dataXMLFile,"instance","ReadWrite")
		print "Successfully created data XML: ",dataXMLFile,"\n"
		
		return 0

#### MAIN CODE STARTS FROM HERE
###############################################################
if __name__ == '__main__':
	arguments = sys.argv[1:]
	
	if arguments==[]:
		print "Usage:"
		print "c2d.py <service>_control.xml"
		sys.exit(0)
	
	_controlXMLFileName=arguments[0]
	controlXMLFileName = pathCorrect(_controlXMLFileName)
	
	if len(arguments) == 1:
		DATA_XML_NAME = controlXMLFileName.split("/")[-1]
		basePathList = controlXMLFileName.split("/")[:-1]
		basePath = '/'.join(basePathList)
		
		if '_control.xml' in DATA_XML_NAME:
			DATA_XML_NAME=DATA_XML_NAME.replace('control','data')
		else:
			print "It might be a control XML file. But,\n"
			print "Kindly follow the naming convention <service>_control.xml\n"
			sys.exit(0)
		
		dataXMLFileName = basePath+"/"+DATA_XML_NAME
		
	elif len(arguments) == 2:
		dataXMLFileName = arguments[1]
		if  not dataXMLFileName.endswith('_data.xml'):
			print "Data XML file name is incorrect, Exiting"
			sys.exit(0)
	
	c2dObj = c2d()
	c2dObj.c2dConverter(controlXMLFileName,dataXMLFileName)
