
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
This scripts removes all the attributes from the element nodes to create data XML files
'''

import sys
import os.path
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from lxml import etree

sys.path.append('../common')
from common import *


dataAttribList= ['Instance']


###########################################################################
##################################################### MAIN CODE STARTS HERE
###########################################################################
def removeAllAttributes(interimDataFile):
	inputFileName = interimDataFile

	print "Removing attributes from file: ",interimDataFile
	
	tree = etree.parse(inputFileName)
	root = tree.getroot()
	
	for element in root.iter():
		if element != root:
			attributes = element.attrib
			for key in attributes:
				if key not in dataAttribList:
					attributes.pop(key,None)

	fileFd=open(interimDataFile,'w')
	fileFd.write(tostring(root))
	fileFd.flush()
	os.fsync(fileFd.fileno())
	fileFd.close()
	print 'All attributes removed successfully\n'
	return 0
	
if __name__ == '__main__':
	file = sys.argv[1]
	removeAllAttributes(file)