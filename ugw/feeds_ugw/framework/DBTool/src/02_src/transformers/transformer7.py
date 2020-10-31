
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
transformer7.py script adds all the syntaxes to element nodes which wasn't addressed in the previous transformation.
All the data types are not taken care of so far, which will be addressed soon.

Addition of more tags to dataType.xml (There are more duplicates in dataType.xml which is cleaned up in the transformer7.py)

usage:
python transformer6.py <stripped-xml> transformer6.xml
'''

## import modules
from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os
from getIndexAddInstance import getIndexAddInstance

sys.path.append('../common')
from common import *


class transformer7():
	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	def addAttributes(self,syntaxAttributes,elemNode):
		#print "addAttributes=",elemNode,syntaxAttributes
		
		for attrib in syntaxAttributes:
			#print attrib
			try:
				elemNode.setAttribute(attrib[0],attrib[1])
			except AttributeError:
				AttribError=None
				#print "NoneType Doesn't exist"


	def insertValueToNode(self,elementNode,value):
		#print "inserted value"
		try:
			if elementNode.firstChild.nodeType != node.TEXT_NODE:
				raise Exception ("node doesn't contain text")
				
			elementNode.firstChild.replaceWholeText(value)
			#elementNode.firstChild.data = value
			#elementNode.text = value
			#child.firstChild.data = newValue
		except AttributeError:
			unKnownReason=True


	## there are so many 'ref' for <dataType> node
	def extractDataTypeRef(self,syntaxNode,elementNode):
		refName = syntaxNode.getAttribute('ref')
		#print "######",syntaxNode,elementNode
		
		try:
			elementNode.setAttribute("syntax","dataTypeRef")
			elementNode.setAttribute("ref",refName)
		except AttributeError:
			AttribError=None
			#print "NoneType Doesn't exist"

	## This script can't be run many times, since 'dataTypeTransformed.xml' gets appended more than once and lots of un-necessary repetitions.
	def buildDataTypeXML(self,childList,nodeName):
		dataTypeFile = self._dataTypeFile
		dataTypeDOM = xml.dom.minidom.parse(dataTypeFile)
		#print dataTypeDOM
		root = dataTypeDOM.firstChild
		node = dataTypeDOM.createElement(nodeName)
		#print "node=",node
		root.appendChild(node)
		
		for i in childList:
			node.appendChild(i)

		#print dataTypeDOM.toprettyxml()

		fileFd = open(dataTypeFile,'w')
		fileFd.write(dataTypeDOM.toxml().encode('utf-8'))
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		
	def getStringFormat(self,element,elemAppAttribute):
		#print "###",element,elemAppAttribute
		
		try:
			elemAppAttribute.setAttribute("syntax","string")
		except AttributeError:
			AttribError=None
			#print "NoneType Doesn't exist"
		
		enumVal=[]
		
		for child in element.childNodes:
			if child.nodeType == xml.dom.Node.ELEMENT_NODE:
				#print "child=",child										## Check

				if child.tagName == 'size' or child.tagName == 'pattern':
					#print "string1=",child
					attributes=child.attributes.items()
					self.addAttributes(attributes,elemAppAttribute)
					#print "string size manipulation"

				elif child.tagName == 'enumeration':
					parentNode = elemAppAttribute.parentNode					## since there were many enum Names with same Name, so decided to add its parent Name as well
					parentName = parentNode.tagName
					parentName = parentName.split('__INSTANCE__')[0]			## some name had '__INSTANCE__' which needs removal
					#print parentName
					
					refValue=parentName+elemAppAttribute.tagName+"Enum"					## this helps map one-one
					#print child.getAttribute("value")
					elemAppAttribute.setAttribute("syntax","string")
					elemAppAttribute.setAttribute("ref",refValue)
					#print "enumeration manipulation"							## debug
					#exit(0)													## debug
					enumVal.append(child)

				elif child.nodeType == xml.dom.Node.ELEMENT_NODE:
					#print kids
					attributes = child.attributes.items()
					self.addAttributes(attributes,elemAppAttribute)				## SHARATH -- self.addAttributes(attributes,elementNode)
		
		if enumVal != []:
			#print enumVal
			#print refValue
			self.buildDataTypeXML(enumVal,refValue)
			
		
	def extractSyntaxAndAdd(self,syntax,elementNode):
		#print "extractSyntax",syntax,elementNode
		
		for child in syntax.childNodes:
			if child.nodeType == xml.dom.Node.ELEMENT_NODE:
				#print "child=",child.tagName					## check

				## string dataType
				if child.tagName == 'string':
					self.getStringFormat(child,elementNode)
				
				## unsignedInt dataType
				elif child.tagName == 'unsignedInt':
					#print "unsigned manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)
					except AttributeError:
						AttribError=None
					
					## this loop considers the immediate children of 'unsignedInt' tag and 
					## writes to the element
					for kids in child.childNodes:
						if kids.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print kids
							attributes = kids.attributes.items()
							self.addAttributes(attributes,elementNode)
				
				## int dataType
				elif child.tagName == 'int':
					#print "int manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)
					except AttributeError:
						AttribError=None

					## this loop considers the immediate children of 'int' tag and 
					## writes to the element
					for kids in child.childNodes:
						if kids.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print kids
							attributes = kids.attributes.items()
							self.addAttributes(attributes,elementNode)
				
				## boolean dataType
				elif child.tagName == 'boolean':
					#print "bool manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)				## this exception is added since we have removed some objects in
					except AttributeError:												## the beginning scripts. These objects being removed using 
						AttribError=None
						#print "NoneType Doesn	't exist"									## skiplist

					## to get a sibling Node, 'default' node in this case - to be used for other cases as well
					try:
						defaultNode = child.parentNode.getElementsByTagName('default')[0]
						#print "defaultNode=",defaultNode
						defaultValue=defaultNode.getAttribute('value')
						#print "defaultValue=",defaultValue
						#elementNode.insertData(defaultValue)
						#insertValueToNode(elementNode,defaultValue)						## inserting values to the NODE is NOT successful so far 
						#elementNode.text = defaultValue

					except IndexError:
						notInterested=1
					'''
					#print defaultNode.attributes.items()
					try:
						defaultValue = defaultNode.getAttribute('value')
						print "defaultValue=",defaultValue
						elemNode.insertData(defaultValue)
					except AttributeError:
						AttribError=None
					'''
				## unsignedLong dataType
				elif child.tagName == 'unsignedLong':
					#print "unsignedLong manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)
					except AttributeError:
						AttribError=None

					## this loop considers the immediate children of 'unsignedInt' tag and 
					## writes to the element
					for kids in child.childNodes:
						if kids.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print kids
							attributes = kids.attributes.items()
							self.addAttributes(attributes,elementNode)
				
				## dataTypeRef type
				elif child.tagName == 'dataType':
					#print "dataType ref manipulation"
					self.extractDataTypeRef(child,elementNode)
					
				## dateTime dataType
				elif child.tagName == 'dateTime':
					#print "dateTime manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)
					except AttributeError:
						AttribError=None
						#print "NoneType Doesn't exist"

				## list dataType
				elif child.tagName == 'list':
					#print "List Manipulation"
					for ch in child.childNodes:
						if ch.nodeType == xml.dom.Node.ELEMENT_NODE:
							attributes=ch.attributes.items()
							self.addAttributes(attributes,elementNode)

				## hexBinary dataType
				elif child.tagName == 'hexBinary':
					#print "hexBinary manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)
					except AttributeError:
						AttribError=None
						#print "NoneType Doesn't exist"

					for kids in child.childNodes:
						if kids.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print kids
							attributes = kids.attributes.items()
							self.addAttributes(attributes,elementNode)
						
				## base64 dataType
				elif child.tagName == 'base64':
					#print "base64 manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)
					except AttributeError:
						AttribError=None
						#print "NoneType Doesn't exist"
						
					for kids in child.childNodes:
						if kids.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print kids
							attributes = kids.attributes.items()
							self.addAttributes(attributes,elementNode)
							
				## long dataType
				elif child.tagName == 'long':
					#print "long manipulation"
					try:
						elementNode.setAttribute("syntax",child.tagName)
					except AttributeError:
						AttribError=None
						#print "NoneType Doesn't exist"
						
					for kids in child.childNodes:
						if kids.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print kids
							attributes = kids.attributes.items()
							self.addAttributes(attributes,elementNode)

	def traverseTree(self,list,index=0,child='NULL'):
		#print "TraverseTree=",list
		#print "index=",index
		depth = len(list)
		#print "depth=",depth
		#print "child=",child
		
		
		if index == 0:
			elemNode = self.DOMTree_trans7.firstChild
			#print "elemNode=",elemNode
		else:
			elemNode = child

		#print "elemNode=",elemNode
		
		##if len(list) == 0:										## Ideally we can ignore this case.
		##	print "Yet to Define case: Report this case"

		if len(list)==1:
			if list[index] == elemNode.tagName:
				childList = elemNode.childNodes
				index+=1
				
				if depth == index:
					#print "FOUND"
					return elemNode

		if len(list) >1:
			if list[index] == elemNode.tagName:
				childList = elemNode.childNodes
				#print "childList=",childList
				index+=1
				
				if depth == index:
					return elemNode
					
				else:
					for child in childList:
						if child.nodeType == xml.dom.Node.ELEMENT_NODE:
							#print child
							if child.tagName == list[index]:
								return self.traverseTree(list,index,child)
					

	##----------------- Main program starts from here -----------------------
	def main(self,strippedXMLFile,prevXMLFile,dataTypeFile):
		XML_NAME = strippedXMLFile.split("/")[-1]
		print "transformer7 processing...(will take more time)",XML_NAME

		self._dataTypeFile = dataTypeFile
		self.DOMTree_trans7 = xml.dom.minidom.parse(prevXMLFile)
		DOMTree_strippedXML = xml.dom.minidom.parse(strippedXMLFile)

		countObj=0											## debug print

		objects = DOMTree_strippedXML.getElementsByTagName("object")

		## Debug code to get all the syntax types
		## creates file with blank
		#file = open("syntax.txt",'w')
		#file.close()

		for object in objects:
			countObj+=1									## debug print
			#print "countObj-",countObj						## debug print
			
			name = object.getAttribute("name")

			nameSplit=name.split(".")[:-1]					## last [u''] in list is removed
			
			nameSplit= getIndexAddInstance(nameSplit)	
			parameters = object.childNodes
			
			
			paramCount=0		## debug
			for params in parameters:							## select element nodes and with child nodes only 'parameter'
				if params.nodeType == xml.dom.Node.ELEMENT_NODE and params.tagName == 'parameter':
					paramCount+=1							## debug
					#print "paramCount=",paramCount		## debug
					
					leafNodeName = ".".join(nameSplit)+"."+params.getAttribute("name")
					leafNodeName= leafNodeName.split(".")
					#print "leafNodeName=",leafNodeName

					
					## there is a bug at this point, elementNode is suppose to get the instance of last node in a list.
					elementNode = self.traverseTree(leafNodeName)
					#print "elementNode(main)=",elementNode
					
					## debug
					#if elementNode == None:
					#	exit(0)
					
					## we tend to get both 'description' and 'syntax' element nodes under parameter node.
					## filtering out only 'syntax' element node is mandatory
					
					if elementNode != None :									## this case is taken where elementNode is None(this occurs when some elements are skipped in skipList)
					
						for syntax in params.childNodes:
							if syntax.nodeType == xml.dom.Node.ELEMENT_NODE and syntax.tagName == 'syntax':
								#print "syntax=",syntax
								syntaxAttributes = self.extractSyntaxAndAdd(syntax,elementNode)
								
								## Debug code to get all the syntax types
								## writes to the file
								#file = open("syntax.txt",'a')
								#file.write(syntax.toxml())
								#file.write("\n----------------------------------------")
								#file.write("----------------------------------------\n")
								#file.close()
						
						#print "syntaxAttributes(main)=",syntaxAttributes						## not used now
						#print "\n"

		completeName = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer7.xml"
		
		textData = (self.DOMTree_trans7.toxml().encode('utf-8'))
		self.writeNSafeClose(completeName,textData)

		print "transformer7.xml created in the path: ",completeName,"\n"

		try:
			newDOM = xml.dom.minidom.parse(completeName)
		except:
			print "Error"
			
		return 0