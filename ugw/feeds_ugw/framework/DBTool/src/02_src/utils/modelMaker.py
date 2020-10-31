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


class modelMaker():
	def __init__(self):
		self.instChromia = chromia.chromia()
		
	
	def getPeerElementHandle(self, rootOld, file2):
		return self.instChromia.getElementHandle_passingString(file2, rootOld)		

	def decideAction(e2, e1Handle):
		
		try:
			if e2.attrib['oper'] == "replace"
				return "attrib_replace"
			
			if	e2.attrib['oper'] == "remove"
				return "object_remove"
				
		except KeyError:
			return None
	
	
	def doRemoveObjects(e3Handle):
		e3Handle.remove #recursive needed

		
		
    def doReplaceAtributes(e2, e3Handle)
		e2AttribList = #collect all attribs of e2 (except oper) to list
		
		#run each attrib if it exits in e3Handle. If it exists replace it with the one that came from e2
		
		
		
		
    def doCreateModel(fileControlXML, fileModelXML, fileControlPerModelXML):
		# Fix slash issues if any
		fileControlXML = pathCorrect(_fileControlXML)
		fileModelXML = pathCorrect(_fileModelXML)
		fileControlPerModelXML = pathCorrect(_fileControlPerModelXML)
		
		
		#Check watermark
		
		#Load XMLs as trees to memory
		# 1.
		treeCtrl = etree.parse(fileControlXML)
		rootCtrl = treeCtrl.getroot()
		rootCtrl = self.instChromia.instanceToUnderScores(rootNew)
		# 2.
		treeModel = etree.parse(fileControlXML)
		rootModel = treeModel.getroot()
		rootModel = self.instChromia.instanceToUnderScores(rootModel)
		# 3.
		treeCtrlModl #How = etree.parse(fileControlXML)
		rootCtrlModl #How = treeCtrlModl.getroot()
		rootCtrlModl # Not needed?= self.instChromia.instanceToUnderScores(rootCtrlModl)
		# also a merge / copy of file on disk is needed - TBD
	
	#1. Read fileControlXML => domControl
	#2. Duplicate read fileControlXML => domControlPerModel
	
	
	for e2 in rootModel.iter():
		#In this module, we really do not bother with parameter nodes (aka leaf nodes)
		# But we leave this commented till the Front End's handling of ObjectNode with zero parameters get handled.
		##if( self.instChromia.isEndNode(e2) )
		##	continue
	
		e2FullPath = self.instChromia.getElementPathString(e2,rootModel)
		
		e1Handle =  self.instChromia.getElementHandle_passingString(e2FullPath, rootCtrl)
		e3Handle =  self.instChromia.getElementHandle_passingString(e2FullPath, rootCtrlModl)
	
	
		if(e1Handle == None):
			#remove the same from e3Handle
			parentNode = e3Handle.getparent()
			parentNode.remove(e3Handle)

	
		action = decideAction(e2, e1Handle)
		
		if( action == "attrib_replace"):
			# remove element e3Handle and its children recursively - have seperate routine for it
			doReplaceAtributes(e1Handle, e2, e3Handle)
		
		elif (action == "object_remove"):
			doRemoveObjects(e3Handle)
		
		else:
			pass
	
