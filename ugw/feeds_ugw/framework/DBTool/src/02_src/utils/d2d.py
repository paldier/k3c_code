
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
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from lxml import etree

sys.path.append(r'../action/')
import addWaterMark
from pathCorrect import pathCorrect
import chromia


class d2d():
		
	def __init__(self):
		pass
		
	def getPeerElementValue(self, newFileFullElemStr):
		return self.instChromia.getElementValue_passingString(newFileFullElemStr, self.rootOld)
		
	def getPeerElementHandle(self, newFileFullElemStr):
		return self.instChromia.getElementHandle_passingString(newFileFullElemStr, self.rootOld)
		
	def doConvert(self,_oldDataXML,_newDataXML):
		print "Received old data XML: ",_oldDataXML
		print "Received new data XML: ",_newDataXML

		self.instChromia = chromia.chromia()
		
		# Fix slash issues if any
		oldDataXML = pathCorrect(_oldDataXML)
		newDataXML = pathCorrect(_newDataXML)
		
		# Check Watermark
		chkWm = addWaterMark.waterMark()
		if (chkWm.isWaterMarked(oldDataXML) != True):
			print "Error: File: ", oldDataXML, " is not watermarked (or) is not an instance file"
			return -1
		
		if (chkWm.isWaterMarked(newDataXML) != True):
			print "Error: File: ", newDataXML, " is not watermarked (or) is not an instance file"
			return -1
		
		
		#Load XMLs as trees to memory
		treeNew = etree.parse(newDataXML)
		rootNew = treeNew.getroot()
		rootNew = self.instChromia.instanceToUnderScores(rootNew)
		
		treeOld = etree.parse(oldDataXML)
		self.rootOld = treeOld.getroot()
		self.rootOld = self.instChromia.instanceToUnderScores(self.rootOld)
		
		for element in rootNew.iter():
			elemFullPath = self.instChromia.getElementPathString(element,rootNew)
			newValue =  self.getPeerElementValue(elemFullPath)
			
			if(newValue != None):
				element.text = newValue  #The core of d2d is simply in this line!
			
			## Handling Special Case
			## End node exists with value as 'None'
			## Eg.   <Enable access="readOnly"/>    this returns None.
			## Handling these cases additionally
			if  ((newValue == None) and (self.getPeerElementHandle(elemFullPath) != None)):
				element.text = None
		
		self.instChromia.UnderScorestoInstance(rootNew)	
		textData = tostring(rootNew)
		self.instChromia.writeNSafeClose(newDataXML,textData)
		chkWm.addWaterMark(newDataXML,"instance","ReadWrite")

		return 0
		
		
	
######### MAIN CODE STARTS FROM HERE ############
#################################################
arguments = sys.argv[1:]
oldDataXML = arguments[0]
newDataXML = arguments[1]

d2dObj = d2d()
d2dObj.doConvert(oldDataXML,newDataXML)
