
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
It takes two input arguments
This script will keep stripped-cleanedup xml and transformer1.xml (which is created out of transformer1.py) in two seperate DOM trees.
transformer1.xml is appended with immediate child nodes and transformer2.xml is created.

skiplist.txt is a file which has services names that needs to be skipped while writing to the transformer2.xml file. If file doesn't found, normal process continues

Usage:
python transformer2_1.py <stripped-xml> transformer1.xml
'''

from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os
from getIndexAddInstance import getIndexAddInstance

sys.path.append('../common')
from common import *


class transformer2():
	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		
		
	def main(self,strippedXMLFile,prevXMLFile):
		XML_NAME = strippedXMLFile.split("/")[-1]
		
		print "transformer2 processing for XML: ",XML_NAME
		
		DOMTree_trans1 = xml.dom.minidom.parse(prevXMLFile)
		DOMTree_strippedXML = xml.dom.minidom.parse(strippedXMLFile)

		rootTrans1 = DOMTree_trans1.firstChild
		objects = DOMTree_strippedXML.getElementsByTagName("object")

		childListToAppend = []
		attributeListToAppend=[]

		## Listing out Object Names from stripped XML
		for object in objects[1:]:								## since the first object is the root object, we tend to ignore it
			attr = object.attributes.items()
			name = object.getAttribute("name")
			
			nameSplit = name.split(".")[:-1]
			nameTF = getIndexAddInstance(nameSplit)
			
			if nameTF[0] == rootTrans1.tagName:
				if nameTF[1] not in childListToAppend:
					childListToAppend.append(nameTF[1])
					attributeListToAppend.append(attr)


		for e,elements in enumerate(childListToAppend):
			childu = DOMTree_trans1.createElement(elements)
			rootTrans1.appendChild(childu)
			
			for i in attributeListToAppend[e]:
				if i[0] != 'name':
					childu.attributes[i[0]] = i[1]

		completeName = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer2.xml"			## use this when running in batch mode

		textData = DOMTree_trans1.toxml()
		self.writeNSafeClose(completeName,textData)

		print "transformer2.xml created in the path: ",completeName,"\n"
		return 0