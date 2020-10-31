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

from xml.etree.ElementTree import *
from xml.etree import ElementTree

class addBasicDataType():

	def __init__(self):
		pass
		
		
	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		
		
		
	def createBasicTypeNodes(self,basicTypeNode,elemList):
		for element in elemList:
			newNode = SubElement(basicTypeNode,element)
			descNode = SubElement(newNode,'description')
			descNode.text = element
		
		return basicTypeNode


	def getAllSyntaxList(self,controlXML):
		with open(controlXML, 'r+') as f:
			tree = ElementTree.parse(f)
		
		basicDataTypeList=[]
		
		for elem in tree.iter():
			value = elem.attrib.get('syntax')
			if value != None:
				if value not in basicDataTypeList:
					basicDataTypeList.append(value)
		
		return basicDataTypeList


	def main(self,controlXML,dataTypeXML):
		print "Adding basic data Types to dataType XML file: ", dataTypeXML

		''' Collect available basic syntax from big XML '''
		basicDataTypeList = self.getAllSyntaxList(controlXML)
		
		''' Start building new data Type XML file '''
		root = Element('syntax')
		basicTypeNode=SubElement(root,'basic_types')
		refTypeNode=SubElement(root,'ref_types')
		
		''' Get the contents for <ref_types> '''
		with open(dataTypeXML, 'r+') as f:
			treeDataType = ElementTree.parse(f)
		
		rootDataType = treeDataType.getroot()
		childrenDataType = rootDataType.getchildren()
		
		''' Get the contents for <basic_types> and fill contents '''
		modifiedBasicTypeNode = self.createBasicTypeNodes(basicTypeNode,basicDataTypeList)
		
		''' Fill the contents for <ref_types>'''
		for elemNode in childrenDataType:
			refTypeNode.append(elemNode)
		
		''' Write the new tree to a file'''
		textData = (tostring(root))
		self.writeNSafeClose(dataTypeXML,textData)
		
		print "Reformatted dataType XML: ",dataTypeXML,"\n"
		return 0