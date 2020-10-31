#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

#
# A helper class that does a lot of XML grunt work across two / three XMLs
#
#
import sys,os
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from lxml import etree
from copy import deepcopy



class chromia():
	def __init__(self):
		pass
		
		
	# Pass me any root and I will convert all the elements having
	# instance keywords, to _<number>
	# Eg:  If    <Radio instance=2> is found,
	# I will convert this to <Radio_2 instance=2>
	# Same root is returned
	#	
	def instanceToUnderScores(self, anyRoot):
	
		for element in anyRoot.iter():
			try:
				instanceNum = element.attrib['Instance']
				nameStr = element.tag + "--" + instanceNum
				element.tag = nameStr
			except KeyError:
				pass
			
		return anyRoot
	############################################################		
		
	
	
	# Pass me any root and I will convert all the elements_<number>
	# back to elements
	# I will convert this  <Radio_2 instance=2>
	# to <Radio instance=2> 
	# Same root is returned
	#			
	def UnderScorestoInstance(self, anyRoot):
	
		for element in anyRoot.iter():
			try:
				instanceNum = element.attrib['Instance']
				
				nameStr = element.tag
				nameStr = nameStr.split('--')[0]
				element.tag = nameStr
			except KeyError:
				pass
			
		return anyRoot			
	############################################################	



	# Pass me a path to an element in a List(of strings) format 	
	# I will return the correct handled to the final element
	# Eg:  Passing
	#    ['Device', 'WiFi', 'Radio', 'Stats']
	# shall return HANDLE TO
	#  <Stats> tag
	#
	def getElementHandle(self, elemList, anyTree):
		firstNibble = elemList[0]
		remainList = elemList[1:]
		
		if remainList != []:
			if(firstNibble == anyTree.tag):
				for child in anyTree:
					if(remainList[0] == child.tag):  #nibble A match with tag A
						return self.getElementHandle(remainList, child)
				return None
		else:
			return anyTree ##At last its the end node that's being returned
	############################################################
	
	
	
	
	
	#When given any element in a tree, return me the complete 
	# path in string to reach it.
	# Eg. passing Stats might return me:
	# "Device.Wifi.Radio.Stats"
	#
	def getElementPathString(self,element,elementRoot,string=''):
		if element.tag == elementRoot.tag:
			string = element.tag + "." + string
			return string
		else:
			string = element.tag + "." + string
			parent = element.getparent()
			return self.getElementPathString(parent,elementRoot,string)
	############################################################
	


	#
	# Passing me this list:
	#  ['Device', 'WiFi', 'Radio', 'Stats']
	# I shall return VALUE OF
	# <Stats> tag
	def getElementValue_passingList(self, elemFullPathList, anyTree):
		elemHandle = self.getElementHandle(elemFullPathList, anyTree)
		
		if elemHandle != None:
			elementValue =  self.getElementValue(elemHandle)
			return elementValue
		else:
			return None
	############################################################		



	# Passing me this string in dotted notation:
	# "Device.WiFi.Radio.Stats."
	# I shall return value of ['Device','WiFi','Radio','Stats','']
	# pop the last item in list - ['Device','WiFi','Radio','Stats']
	# <Stats> tag
	def getElementValue_passingString(self, pathStr, anyTree):
		pathList = self.getPathByList(pathStr)
		pathList.pop()
		return self.getElementValue_passingList(pathList, anyTree)
	############################################################
	
	
	
	#
	# Passing me this list:
	#  ['Device', 'WiFi', 'Radio', 'Stats']
	# I shall return ELEMENT HANDLE to
	# <Stats> tag
	def getElementHandle_passingList(self, elemFullPathList, anyTree):
		elemHandle = self.getElementHandle(elemFullPathList, anyTree)
		return elemHandle
	############################################################
	
	
	
	# Helper routine
	# Passing me this string in dotted notation:
	# "Device.WiFi.Radio.Stats."
	# I shall return ELEMENT HANDLE to
	# 	['Device','WiFi','Radio','Stats','']
	# pop the last item in list - ['Device','WiFi','Radio','Stats']
	# <Stats> tag
	def getElementHandle_passingString(self, pathStr, anyTree):
		pathList = self.getPathByList(pathStr)
		pathList.pop()
		return self.getElementHandle_passingList(pathList, anyTree)
	############################################################
	
	
	
	#Helper routine
	def isEndNode(self, elemHandler):
		flag=False
		for child in elemHandler:
			flag=True
		return flag
	############################################################	


	
	# Helper routine
	# Convert "Device.WiFi.Radio.Stats"
	# to ['Device', 'WiFi', 'Radio', 'Stats']
	# to   
	def getPathByList(self, pathStr):
		pathList = pathStr.split('.')
		return pathList
	############################################################	

	
	# Helper routine
	# Convert  ['Device', 'WiFi', 'Radio', 'Stats']
	# to   "Device.WiFi.Radio.Stats"
	
	def getPathByString(self, pathList):
		pathString = '.'.join(pathList)
		return pathString
	############################################################


	# Helper routine
	# get an Element's Name, when the Hex address is passed to it
	def getElementName(self, elemHandler):
		return elementHex.tag
	############################################################
	
	
	# Helper routine
	# get an Element's Value
	def getElementValue(self, elemHandler):
		return elemHandler.text
	############################################################

	
	
	# Helper routine
	# get an Element's Attribute Dictionary
	def getElementAttributes(self, elemHandler):
		return elemHandler.attrib
	############################################################
	
	
	# Helper routine
	# get the Element's position from it parent
	# Instance is applicable for data file, else it is optional
	def getElementPosition(self,elemHandler,Instance=None):
		parentHandler = elemHandler.getparent()
		position=0
		for childHandle in parentHandler:
			if childHandle.tag == elemHandler.tag:

				if (Instance != None): #Applicable for Data XML files
					rcvInst = self.getAttribValue(childHandle,"Instance")
					if(rcvInst == Instance):
						return position
					
				return position #Hits here for Control files
			else:
				position+=1
	############################################################
	
	
	
	# Helper routine
	# Pass the file name and text to this function
	# Takes the file and write a new file with the text provided
	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		return 0
	############################################################	
	
	# Helper routine
	# Takes the XML file and adds indents and space to
	# the current XML file
	def beautufyXMLFile(self,xmlFile):
		dom = xml.minidom.parse(xmlFile)
		xmlText = dom.toprettyxml()
		self.writeNSafeClose(xmlFile, xmlText)
	############################################################	
	
	# Helper routine
	# get the Element's attribute value for attribute key passed
	def getAttribValue(self,elemHandler,attribKey):	
		try:
			return elemHandler.attrib[attribKey]
		except KeyError:
			return None
	############################################################
	
	
	# Helper routine
	# insert the element to it's parent based on position number passed
	def insertElementAtPosition(self,elemHandler,position):
		parentHandler = elemHandler.getparent()
		parentHandler.insert(position,deepcopy(elemHandler))
	############################################################
	
	
	# Helper routine
	# remove attribute from a sub tree
	def removeAttributeRecursive(self,anyTree,attribKey):
		for child in anyTree.iter():
			try:
				if child.attrib[attribKey]:
					dict = child.attrib
					dict.pop(attribKey,None)
			except KeyError:
				pass
	############################################################