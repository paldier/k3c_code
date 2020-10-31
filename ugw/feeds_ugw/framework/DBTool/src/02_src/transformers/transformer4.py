
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
this script adds all the leaf nodes remaining in the stripped-cleaned xml
input files for this script are stripped-cleaned-raw xml and transformer3.xml
output transformer4.xml now has all the child nodes including parameter nodes. we still haven't added attributes yet

Usage:
python transformer4.py <stripped-xml-file> transformer3.xml
'''
## import modules
from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os
from getIndexAddInstance import getIndexAddInstance

sys.path.append('../common')
from common import *


class transformer4():
	
	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	## functions declared
	def getNodeFromTree(self,list,child='NULL',index=0):
		depth = len(list)
		list = [s.encode('ascii') for s in list]
		#print "List,index,child=",list,index,child
		
		if index == 0:
			elemNode = self.DOMTree_trans4.firstChild
			#print "elemNode(NFT)=",elemNode
		else:
			elemNode = child
			#print "elemNode(NFT)",elemNode
		
		if depth == 1:
			if list[index] == ((elemNode.tagName) or (elemNode.tagName + "__INSTANCE__")):
				childList = elemNode.childNodes
				index+=1
				
				if depth == index:
					return elemNode

		if depth > 1:
			if (list[index] == (elemNode.tagName)) or ((list[index]+"__INSTANCE__") == (elemNode.tagName)):
				childList = elemNode.childNodes
				index+=1

				if depth == index:
					#print "END OF SEARCH"
					return elemNode

				else:
					for child in childList:
						if child.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print "child, list[index]",child.tagName,"::",list[index]+"__INSTANCE__"
							if (child.tagName  == (list[index])) or (child.tagName == (list[index]+"__INSTANCE__")):
								#print "child(found)",child.tagName
								return self.getNodeFromTree(list,child,index)
					return "NULL1"


	def addParamNodes(self,elemNode,paramList):
		if elemNode != 'NULL1':
			#print "elemNode,[addParamNodes]",elemNode,paramList
			paramNameList=[]
			for params in paramList:
				paramNameList.append(params.getAttribute("name"))
			
			#print "paramNameList=",paramNameList

			for paramName in paramNameList:
				paramNode=self.DOMTree_trans4.createElement(paramName)
				elemNode.appendChild(paramNode)

				
	def main(self,strippedXMLFile,prevXMLFile):
		XML_NAME = strippedXMLFile.split("/")[-1]
		print "transformer4 processing for XML: ",XML_NAME
		
		self.DOMTree_trans4 = xml.dom.minidom.parse(prevXMLFile)
		DOMTree_strippedXML = xml.dom.minidom.parse(strippedXMLFile)
		
		objects = DOMTree_strippedXML.getElementsByTagName("object")
		#print "DOMTree_trans4(doc)=",DOMTree_trans4

		for object in objects:
			childList = object.childNodes
			#print "childList=",childList
			paramList = []
			for child in childList:
				if child.nodeType == xml.dom.Node.ELEMENT_NODE:
					if child.tagName == 'parameter':
						paramList.append(child)
			#print "paramList=",paramList

			name = object.getAttribute("name")

			nameSplit = name.split(".")[:-1]
			nameTF = getIndexAddInstance(nameSplit)
			#print  "nameTF=",nameTF
			
			if nameTF != []:
				#print "nameTF(main)=",nameTF,"\n"
				elemNode = self.getNodeFromTree(nameTF)				## equivalent to recurse traverse
				#print "elemNode(main)=",elemNode
				self.addParamNodes(elemNode,paramList)

		completeName = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer4.xml"						## use this when running in batch mode
		
		textData = (self.DOMTree_trans4.toxml())
		self.writeNSafeClose(completeName,textData)

		print "transformer4.xml created in the path: ",completeName,"\n"

		try:
			DOM = xml.dom.minidom.parse(completeName)
			#print "XML parse Successful"
			ParseSuccessful=True
		except xml.parsers.expat.ExpatError:
			print "XML Parse Error"
			
		return 0