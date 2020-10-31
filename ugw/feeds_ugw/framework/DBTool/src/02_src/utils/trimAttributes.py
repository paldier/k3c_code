#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import sys,os
import chromia
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from lxml import etree

sys.path.append('../action/')

from pathCorrect import pathCorrect

def trimAttributes(fileName,attribList):
	if attribList == []:
		print "No attributes passed. Exiting"
		sys.exit(0)
	
	chromiaInst = chromia.chromia()
	tree = etree.parse(fileName)
	root = tree.getroot()
	for attrib in attribList:
		chromiaInst.removeAttributeRecursive(root,attrib)
	
	baseName = os.path.basename(fileName)
	basePath = os.path.dirname(fileName)
	
	newFileName = basePath+ "/" + baseName.split('.xml')[0] + "_trimmed.xml"
	print newFileName
	
	textData = tostring(root)
	chromiaInst.writeNSafeClose(newFileName,textData)

#############################
####### MAIN CODE STARTS HERE
#############################

arguments = sys.argv

try:
	_fileName = arguments[1]
	attributesList = arguments[2:]
	fileName = pathCorrect(_fileName)
	trimAttributes(fileName,attributesList)

except IndexError:
	print "Insufficient arguments passed"
	print "Usage: "
	print "trimAttributes.py <file_name> [attrib1] [attrib2] [attrib3]"