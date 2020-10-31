
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
this script adds all the attributes to the object nodes and their child nodes.
This xml file is more or less a complete XML file extracted from the cleaned up XML
Usage:
python transformer5.py <stripped-xml-file> transformer4.xml
'''
## import modules
from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os
from getIndexAddInstance import getIndexAddInstance

sys.path.append('../common')
from common import *


class transformer5():
	
	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	def addAttributes(self,child,attributes):
		#print "addAttributes(child)=",child,attributes
		for attrib in attributes:							## traverse thru all the names in attributes
			if attrib[0] != 'name':							## skip the name in attributes
				#print attrib[0],attrib[1]
				child.setAttribute(attrib[0],attrib[1])
				#print "WRITE DONE"

	## Traverse the transformer4.xml tree and find the location of the object
	def traverseTree(self,list,attributes,index=0,child='NULL'):
		#print "TraverseTree=",list,attributes
		#print "index=",index
		depth = len(list)
		
		if index == 0:
			elemNode = self.DOMTree_trans5.firstChild
			#print "elemNode=",elemNode
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

	#########################################################################################
	############################################################## MAIN CODE STARTS FROM HERE
	def main(self,strippedXMLFile,prevXMLFile):
		
		XML_NAME = strippedXMLFile.split("/")[-1]
		print "transformer5 processing for XML: ",XML_NAME
		
		self.DOMTree_trans5 = xml.dom.minidom.parse(prevXMLFile)
		DOMTree_strippedXML = xml.dom.minidom.parse(strippedXMLFile)
		
		### Get all the element nodes 'object' from a tree
		objects = DOMTree_strippedXML.getElementsByTagName("object")

		#print objects
		objectName=[]

		for object in objects:
			#print "object=",object							## prints each object at a time
			#print "\n"										## take one object at a time
			name = object.getAttribute("name")				## get the object name (its in attribute list)

			objectName.append(name)							## all object names in a list
			#print "name=",name
			nameSplit=name.split(".")[:-1]					## last [u''] in list is removed
			#print "name(split)=",nameSplit					## split them by dots and create list
			attributes = object.attributes.items()			## get all the attributes of an object
			#print "attributes=",attributes					
			nameSplit= getIndexAddInstance(nameSplit)
			#print "name(INDEXED)=",nameSplit
			
			self.traverseTree(nameSplit,attributes)				## find the location of this object in transformer4.xml file
			
			parameters = object.childNodes
			
			for params in parameters:							## select element nodes and with child nodes only 'parameter'
				if params.nodeType == xml.dom.Node.ELEMENT_NODE and params.tagName == 'parameter':
					leafNodeName = ".".join(nameSplit)+"."+params.getAttribute("name") 
					leafNodeName= leafNodeName.split(".")
					#print "leafNode=",leafNodeName
					attributesLeafNode = params.attributes.items()
					#print "attributesLeafNode=",attributesLeafNode
					self.traverseTree(leafNodeName,attributesLeafNode)		## always send a list to traverseTree

		completeName = TRANSFORMER_PATH + 'transformer5.xml'
		
		textData = (self.DOMTree_trans5.toxml())
		self.writeNSafeClose(completeName,textData)


		## transformer5.xml has come issues in opening the file, since it contained 'dmr:' something which was causing problem.
		## so we are replacing 'dmr:' with 'dmr_'.
		## root cause for this will be found out

		#completeNameNewFile= '../../XML_files/transformer_out/transformer5.xml'		## use this when running in batch mode
		completeNameNewFile = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer5.xml"

		f1 = open(completeName, 'r')
		f2 = open(completeNameNewFile, 'w')
		for line in f1:
			f2.write(line.replace('dmr:', 'dmr_'))
		f1.close()
		f2.close()

		os.remove(completeName)

		try:
			DOM = xml.dom.minidom.parse(completeNameNewFile)
			#print "XML parse Successful"
			ParseSuccessful=True
		except xml.parsers.expat.ExpatError:
			print "XML Parse Error"
		#print "##################TEST PROGRAM####################"

		print "transformer5.xml created in the path: ",completeNameNewFile,"\n"
		return 0