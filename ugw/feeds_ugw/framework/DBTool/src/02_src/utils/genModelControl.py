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
Script: genModelControl.py
This script takes current <service>_control.xml and oldModelControlXML as input files, and 
creates newModelControl XML file
'''

import os,sys
sys.path.append(r'../action/')
from pathCorrect import pathCorrect
import chromia
import shutil
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from lxml import etree
import addWaterMark


def doCreateControlModel(fileControlXML, fileOldCtrlModel, fileNewCtrlModel):
	#Load XMLs as trees to memory
	# 1.
	treeCtrl = etree.parse(fileControlXML)
	rootCtrl = treeCtrl.getroot()
	#rootCtrl = instChromia.instanceToUnderScores(rootCtrl)
	
	# 2.
	treeOldModel = etree.parse(fileOldCtrlModel)
	rootOldModel = treeOldModel.getroot()
	#rootOldModel = instChromia.instanceToUnderScores(rootModel)
		
	# 3.
	treeNewModel = etree.parse(fileNewCtrlModel)
	rootNewModel = treeNewModel.getroot()
	#rootCtrlModl = instChromia.instanceToUnderScores(rootModel)
	
	instChromia = chromia.chromia()
	
	for e2 in rootOldModel.iter():
		#In this module, we really do not bother with parameter nodes (aka leaf nodes)
		# But we leave this commented till the Front End's handling of ObjectNode with zero parameters get handled.
		##if( instChromia.isEndNode(e2) )
		##	continue

		print "##Processing: ", instChromia.getElementPathString(e2, rootOldModel)
		
		e2FullPath = instChromia.getElementPathString(e2,rootOldModel)
		
		e1Handle =  instChromia.getElementHandle_passingString(e2FullPath, rootCtrl)
		print "e1Handle: ",e1Handle
		e3Handle =  instChromia.getElementHandle_passingString(e2FullPath, rootNewModel)
		print "e3Handle: ",e3Handle
		print "e2Handle: ",e2
		
		if(e1Handle == None):
			#remove the same from e3Handle
			#print "## >> Removing:",  instChromia.getElementPathString(e3Handle, rootNewModel)
			try:
				parentNode = e3Handle.getparent()
				print "parentNode: (e3)",parentNode
				parentNode.remove(e3Handle)
			except AttributeError:
				print "Ignoring non-existent children!"
		
		if ((e1Handle != None) and (e3Handle != None)) :
			
			''' handle multiInst logic case '''
			try:	#1
				oldModelControlInst = e2.attrib['multiInst']
				try:	#2
					controlMultiInst = e1Handle.attrib['multiInst']
					
					
					if ((controlMultiInst == 'yes') and (oldModelControlInst == 'yes')):
						e3Handle.attrib['multiInst'] = 'yes'
					else:
						e3Handle.attrib['multiInst'] = 'no'
				except KeyError:	#2
					pass
			except KeyError:	#1
				print "No multiInst attribute exists in this object"
			
			
			''' handle minEntries logic case '''
			try:	#1
				oldModelMinEntries = e2.attrib['minEntries']
				try:	#2
					controlMinEntries = e1Handle.attrib['minEntries']
					
					if (int(controlMinEntries) >= int(oldModelMinEntries)):
						e3Handle.attrib['minEntries'] = oldModelMinEntries
					
					else:
						e3Handle.attrib['minEntries'] = controlMinEntries
				except KeyError:	#2
					pass
			except KeyError:	#1
				print "No minEntries attribute present in this object"
			
			
			''' handle maxEntries logic case '''
			try:	#1
				oldModelMaxEntries = e2.attrib['maxEntries']
				
				try:	#2
					controlMaxEntries = e1Handle.attrib['maxEntries']
					if ((controlMaxEntries != 'unbounded') and (oldModelMaxEntries != 'unbounded')):
						if (int(controlMaxEntries) >= int(oldModelMaxEntries)):
							e3Handle.attrib['maxEntries'] = oldModelMaxEntries
						else:
							e3Handle.attrib['maxEntries'] = controlMaxEntries
				except KeyError:	#2
					pass
			except KeyError:	#1
				print "No maxEntries attribute found in this object"
			
		print "\n"
			
			
	textData = tostring(rootNewModel)
	instChromia.writeNSafeClose(fileNewCtrlModel,textData)
	waterMarkInst = addWaterMark.waterMark()
	waterMarkInst.addWaterMark(fileNewCtrlModel,'model_schema','ReadWrite')
