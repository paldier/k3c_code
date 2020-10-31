
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
this transformer file will add description nodes from the stripped off xml to the new xml created as an attribute 
description="contents from the description node"

This script refers to both stripped off xml and previous transformed XML, in this case transformer5.xml
'''

from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os
import re
from getIndexAddInstance import getIndexAddInstance

sys.path.append('../common')
from common import *


class transformer6():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	def addAttributes(self,child,attribValue):
		#print "addAttributes(child)=",child
		child.setAttribute('description',attribValue)


	## Traverse the transformer5.xml tree and find the location of the object
	def traverseTree(self,list,attributes,index=0,child='NULL'):
		#print "TraverseTree=",list,attributes
		#print "index=",index
		depth = len(list)
		
		if index == 0:
			elemNode = self.DOMTree_trans6.firstChild
		else:
			elemNode = child
			
		if len(list) == 0:
			print "Yet to Define case: Report this case"
			
		if len(list)==1:
			if list[index] == elemNode.tagName:
				childList = elemNode.childNodes
				index+=1
				
				if depth == index:
					self.addAttributes(elemNode,attributes)
					return True

		if len(list) >1:
			
			if list[index] == elemNode.tagName:
				childList = elemNode.childNodes
				index+=1
				
				if depth == index:
					self.addAttributes(child,attributes)
					return True
					
				else:
					for child in childList:
						if child.nodeType == xml.dom.Node.ELEMENT_NODE:
							if child.tagName == list[index]:
								return self.traverseTree(list,attributes,index,child)

	##########################################################################################
	##########################################################################################
	##############################################################  MAIN CODE STARTS FROM HERE
	def main(self,strippedXMLFile,prevXMLFile):
		XML_NAME = strippedXMLFile.split("/")[-1]
		print "transformer6 processing for XML: ",XML_NAME
		
		self.DOMTree_trans6 = xml.dom.minidom.parse(prevXMLFile)
		DOMTree_strippedXML = xml.dom.minidom.parse(strippedXMLFile)

		### Get all the element nodes 'object' from a tree
		objects = DOMTree_strippedXML.getElementsByTagName("object")

		objectName=[]

		for object in objects:
			name = object.getAttribute("name")				## get the object name (its in attribute list)
			objectName.append(name)							## all object names in a list
			nameSplit=name.split(".")[:-1]					## last [u''] in list is removed
			
			for descriptionNode in object.childNodes:
				if descriptionNode.nodeType == xml.dom.Node.ELEMENT_NODE:
					if descriptionNode.tagName == 'description':
						#descriptionValue = str(descriptionNode.firstChild.data)	## (previously done)
						descriptionValue = descriptionNode.firstChild.data			## (temporary adjustment - handle unicode errors)

			
			nameSplit= getIndexAddInstance(nameSplit)
			#print "name(INDEXED)=",nameSplit
			self.traverseTree(nameSplit,descriptionValue)				## find the location of this object in transformer5.xml file

			parameters = object.childNodes
			
			for params in parameters:							## select element nodes and with child nodes only 'parameter'
				if params.nodeType == xml.dom.Node.ELEMENT_NODE and params.tagName == 'parameter':
					leafNodeName = ".".join(nameSplit)+"."+params.getAttribute("name") 
					leafNodeName= leafNodeName.split(".")
					#print "leafNode=",leafNodeName
					
					for descriptionNode in params.childNodes:
						if descriptionNode.nodeType == xml.dom.Node.ELEMENT_NODE:
							if descriptionNode.tagName == 'description':
								descriptionValue = descriptionNode.toxml()
								try:
									#descriptionValue =stripxmlTag(descriptionValue)
									descriptionValue = descriptionNode.firstChild.data
								except UnicodeDecodeError:
									pass
					
					#print "descriptionValue=",descriptionValue			## DO NOT UNCOMMENT THIS 
					self.traverseTree(leafNodeName,descriptionValue)			
					#print "\n"

		completeName = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer6.xml"

		textData = (self.DOMTree_trans6.toxml().encode('utf-8'))
		self.writeNSafeClose(completeName,textData)

		print "description attribute successfully added in the path: ",completeName,"\n"
		return 0