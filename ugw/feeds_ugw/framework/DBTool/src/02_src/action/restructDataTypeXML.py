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
This script basically does converts from a <syntax name="Element"/> to <Element/>
This conversion is required for easy access of elements and validation(will be done by other scripts)
'''

from xml.etree.ElementTree import *
from xml.etree import ElementTree
import sys,os
from xml.dom import minidom


class restructDataTypeXML():
	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	def prettify(elem):
		"""Return a pretty-printed XML string for the Element.
		"""
		rough_string = ElementTree.tostring(elem, 'utf-8')
		reparsed = minidom.parseString(rough_string)
		return reparsed.toprettyxml(indent="  ")

	def main(self,dataTypeXML):
		file = dataTypeXML
		print "received file=",file

		with open(file, 'r+') as f:
			tree = ElementTree.parse(f)


		## root to be used to build tree, 'syntax' is the root of this xml
		root = Element('syntax')
		#print tree

		for elem in tree.iter('dataType'):
			subRoot = elem.attrib.get('name')
			rootData=SubElement(root,subRoot)
			childList = list(elem.iter())
			
			children = elem.getchildren()
			for i in children:
				rootData.append(i)
		
		
		textData = tostring(root)
		self.writeNSafeClose(file,textData)
		print "dataXML restructured completed\n"
		return 0