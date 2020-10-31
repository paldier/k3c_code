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
we have many duplicate nodes in dataType.xml, which required removal.
This small script performs the removal of those duplicates
'''

from xml.dom.minidom import *
import xml.dom.minidom
import sys
import os

sys.path.append('../common')
from common import *


class removeDuplicates():

	def __init__(self):
		pass

		
	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()

		
	def main(self,dataTypeXML):
		file = dataTypeXML
		dataTypeDOM = xml.dom.minidom.parse(file)
		root = dataTypeDOM.firstChild
		listName=[]
		listNameNode=[]
		for i in root.childNodes:
			if i.nodeType == xml.dom.Node.ELEMENT_NODE:
				if i.tagName not in listName:
					listName.append(i.tagName)
					listNameNode.append(i)

		doc = Document()
		syntax = doc.createElement("syntax")
		doc.appendChild(syntax)

		for i in listNameNode:
			syntax.appendChild(i)
			
			
		textData = (doc.toprettyxml().encode('utf-8'))
		self.writeNSafeClose(file,textData)

		print "Removed duplicate nodes from dataType.xml\n"
		return 0