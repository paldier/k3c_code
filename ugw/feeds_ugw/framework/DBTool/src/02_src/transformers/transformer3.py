
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
Input to this script is transformer2.xml and stripped-cleanedup-raw xml files.
this script builds on top of transformer2.xml
this script generates another xml file transformer3.xml which has all the child nodes of an element (except the leaf nodes)

Usage:
python transformer3.py <stripped-xml-file> transformer2.xml
'''

## Modules to be imported here
from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os
from getIndexAddInstance import getIndexAddInstance

sys.path.append('../common')
from common import *


class transformer3():
	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	def traverseTree(self,index,elemNode,pNode):
		#print "elemNode,pNode,index(TT)=",elemNode,pNode,index
		if index == 0:
			return self.DOMTree_trans3.firstChild
		elif index == 1:
			childList = pNode.childNodes
			#print "childList=",childList
			for child in childList:
				#print "CHILD=",child
				if child.nodeType == xml.dom.Node.ELEMENT_NODE:
					#print "child=",child.tagName
					if child.tagName == elemNode:
						#print "child=",child.tagName
						return child
			return 'NULL1'

		else:
			childList = pNode.childNodes
			for child in childList:
				if child.nodeType == xml.dom.Node.ELEMENT_NODE:
					if child.tagName == elemNode:
						#print "child=",child.tagName
						return child
			#print "Build Node"
			return self.constructTreeNode(elemNode,pNode)

	def constructTreeNode(self,elemNode,pNode):
		#print "elemNode,pNode(CT)",elemNode,pNode
		childu = self.DOMTree_trans3.createElement(elemNode)
		#print "childu::",childu
		pNode.appendChild(childu)
		return childu

		
	def main(self,strippedXMLFile,prevXMLFile):
		XML_NAME = strippedXMLFile.split("/")[-1]
		print "transformer3 processing for XML: ",XML_NAME
		
		self.DOMTree_trans3 = xml.dom.minidom.parse(prevXMLFile)
		DOMTree_strippedXML = xml.dom.minidom.parse(strippedXMLFile)

		objects = DOMTree_strippedXML.getElementsByTagName("object")

		for object in objects:
			name = object.getAttribute("name")
			nameList = name.split(".")[:-1]
			name = getIndexAddInstance(nameList)
			#print "ListFinal=",name
			
			index=0
			pNode = 'NULL'
			for each in name:
				#print "elem,pNode(main)",each,pNode
				pNode=self.traverseTree(index,each,pNode)	
				index+=1
				if pNode == 'NULL1':
					#print "Out of range"
					break

		completeName = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer3_1.xml"			## use this when running in batch mode
		
		textData = (self.DOMTree_trans3.toxml())
		self.writeNSafeClose(completeName,textData)

		try:
			DOM = xml.dom.minidom.parse(completeName)
		except xml.parsers.expat.ExpatError:
			#print "*****XML parser Error"
			XMLError=True

		completeN = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer3.xml"								## use this when running in batch mode

		## some service elements have instances, which are mentioned as {i}, when used is not readable by mindidom parser, hence we replace '{i}' with '__INSTANCE__' and create a new xml file, transformer3_1.xml

		with open(completeN, "w") as fout:
			with open(completeName, "r+") as fin:
				for line in fin:
					fout.write(line.replace('{i}', '__INSTANCE__'))

		## remove the transformer3.xml file
		os.remove(completeName)

		print "transformer3.xml created in the path: ",completeN,"\n"

		try:
			DOMn = xml.dom.minidom.parse(completeN)
			#print "ParseSuccessful"
			ParseSuccessful=True
		except:
			print "Error in File"
			
		return 0