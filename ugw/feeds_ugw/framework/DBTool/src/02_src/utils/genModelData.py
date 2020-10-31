#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import os,sys
sys.path.append(r'../action/')
from pathCorrect import pathCorrect
import chromia
import shutil
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from lxml import etree
import addWaterMark
import neo
import math


def insertInNewDataModel(e3,rootNewModelData):
	print "e3(attribs): ",e3.attrib
	neoInst = neo.neo()
	neoInst.buildIndependentLineage(e3,rootNewModelData)


#Move to chromia
def getElementNumInstances(elementHandler):
	parentHandler = elementHandler.getparent()
	numInstance = 0
	
	for child in parentHandler:
		if child.tag == elementHandler.tag:
			numInstance+=1

	return numInstance


#Move to chromia
# Helper routine
# Remove Element from position
def removeElementFromPosition(self,elemHandler,relativePosition):
	nthElementHandler = getNthElementHandler(elemHandler,relativePosition)
	parentHandler = nthElementHandler.getparent()
	parentHandler.remove(nthElementHandler)   
############################################################	



#Move to chromia
#getNthElementHandler
def getNthElementHandler(elementHandler, relativePosition):
	parentHandler = elemHandler.getparent()
	nthHandlePosn = 1
	for child in parentHandler:
		if child.tag == elementHandler.tag:
			nthHandlePosn+=1
			if(nthHandlePosn == relativePosition):
				return child

	return None


def deleteInstance(elementHandler,instanceNumber,rootNewModelData):
	chromiaInst = chromia.chromia()
	elementTag = elementHandler.tag
	parentHandler = elementHandler.getparent()
	print "instanceNumber: ",instanceNumber
	
	for child in parentHandler:
		if child.tag == elementTag:
			currInst = child.attrib['Instance']
			child.attrib['node'] = 'object'
			if int(currInst) == instanceNumber:
				insertInNewDataModel(child,rootNewModelData)



'''     numInstance -> Taken from the value of minEntries
        currentInstance -> Number of instances available in the data XML file (in this case e3)
        elementHandler positions at the first instance of e3 '''
def removeInstances(elementHandler, numInstance,currentInstance,rootNewModelData):		## numInstance = minEntries, elemHandler=e3
	if(numInstance >= 0):
		if(numInstance < currentInstance):												## delete the remaining from e3 and make an entry to newDataModel XML
			totalDeletableClones = currentInstance-numInstance 

			for each in reversed(range(totalDeletableClones)):
				deleteInstance(elementHandler,numInstance+each+1,rootNewModelData)
			
			print "Residual Instances deleted"

		else: 
			print "There are less instances than minEntries value, hence to be ignored"



def createNewElement(ElemNameStr, attribValuesDict, elementValue):
	elementHandler = Element(ElemNameStr,)
	elementHandler.text = elementValue
	
	for attrib in attribValuesDict.iterkeys():
		key = attribValuesDict[attrib][0]
		value = attribValuesDict[attrib][1]
		elementHandler.attrib[key] = value
	return elementHandler


def checkParentInstance(e3Handle):
	parentHandle = e3Handle.getparent()
	try:
		print parentHandle.attrib['Instance']
		return True
	except KeyError:
		print "NOT A MULTI-INSTANCE"
		return False


def handleMultiInstanceOp(minEnt,e3Handle,rootNewModelData):
	e3Name = e3Handle.tag
	e3Parent = e3Handle.getparent()
	e3GParent = e3Parent.getparent()
	
	e3ParentList=[]
	for child in e3GParent:
		if child.tag == e3Parent.tag:
			e3ParentList.append(child)
	
	for each in e3ParentList:
		e3List=[]
		for child in each:
			if child.tag == e3Name:
				e3List.append(child)
		
		e3List.reverse()
		for i in e3List:
			currentInstance = getElementNumInstances(child)
			removeInstances(child, minEnt, int(currentInstance),rootNewModelData)



def doCreateDataModel(dataXML, oldModelDataXML, newModelControlXML,    newModelDataXML, autoDelInstParameter):
	instChromia = chromia.chromia()
	
	print "autoDelInstParameter=", autoDelInstParameter
	
	#Load XMLs as trees to memory
	# 1 (e1))
	treeNewModelControl = etree.parse(newModelControlXML)
	rootNewModelControl = treeNewModelControl.getroot()


	# 2 (e2)
	treeData = etree.parse(dataXML)
	rootData = treeData.getroot()


	# 3 (e3) Take a temp copy of e2
	treeDataTemp = etree.parse(dataXML)
	rootDataTemp = treeDataTemp.getroot()


	# 4 (e4)
	treeOldModelData = etree.parse(oldModelDataXML)
	rootOldModelData = treeOldModelData.getroot()


	# 5 (e5)
	treeNewModelData = etree.parse(newModelDataXML) #check if freshly created - TBD - in calling method
	rootNewModelData = treeNewModelData.getroot()


	#####################################################
	# STEP - 1: Instance removal for removed Objects
	# Copy dataXML to temp_dataXML
	# A) For every <oper = remove> in newModelControlXML, remove the object instances from temp_dataXML
	# B)  For each of those removed instances, place an object 
	#	  instance node in the tmp_<serv>_data_model_<category>.xml (newModelDataXML)
	#	  with "oper=remove" attribute.
	#####################################################
	#print "\n###################################### STEP-1 ######################################"
	#
	#'''Original files before STEP-1'''
	#temp_stage0_e3 = tostring(rootDataTemp)
	#instChromia.writeNSafeClose('temp_stage0_e3.xml',temp_stage0_e3)
	#
	#temp_stage0_e5 = tostring(rootNewModelData)
	#instChromia.writeNSafeClose('temp_stage0_e5.xml',temp_stage0_e5)
	#
	#'''Convert the Necessary XML files to underscores before STEP-1'''
	#rootNewModelControl = instChromia.instanceToUnderScores(rootNewModelControl)			#(e1)
	#rootDataTemp = instChromia.instanceToUnderScores(rootDataTemp)							#(e3)
	#rootNewModelData = instChromia.instanceToUnderScores(rootNewModelData)					#(e5)
	#
	#'''Iterate through e1 and look for oper="replace" '''
	#for e1 in rootNewModelControl.iter():
	#	e1FullPath = instChromia.getElementPathString(e1,rootNewModelControl)
	#	e3Handle =  instChromia.getElementHandle_passingString(e1FullPath, rootDataTemp)
	#	
	#	
	#	try:  ###try 1
	#		if (e1.attrib["oper"] == "remove" and e3Handle != None):
	#			try: ###try2
	#				parentNode = e3Handle.getparent()
	#				print "Element exists in Data XML and hence will be removed (e3)"
	#				parentNode.remove(e3Handle)
	#				''' This Entry has to be inserted in newDataModel (e5) '''
	#				insertInNewDataModel(e1,rootNewModelData)
	#				
	#			except AttributeError: #except 2
	#				print "Ignoring non-existent children!"
	#	except KeyError: ###except 1
	#		print "Warning: KeyError for element: ", e1
	#
	#
	#rootDataTemp = instChromia.UnderScorestoInstance(rootDataTemp)
	#temp_stage1_e3 = tostring(rootDataTemp)
	#instChromia.writeNSafeClose('temp_stage1_e3.xml',temp_stage1_e3)
	#
	#rootNewModelData = instChromia.UnderScorestoInstance(rootNewModelData)
	#temp_stage1_e5 = tostring(rootNewModelData)
	#instChromia.writeNSafeClose('temp_stage1_e5.xml',temp_stage1_e5)
	#print "###################################### STEP-1 ######################################\n"
	############################################################
	
	
	
	
	
	
	
	#####################################################
	# STEP - 2: Prune Instances based on minEntries
	# A) For every <oper = replace> (minEntries only) in newModelControlXML, 
	#    prune the excess instances from temp_dataXML
	# B)  For each of those removed instances, place an object 
	#     instance node in the tmp_<serv>_data_model_<category>.xml (newModelDataXML)
	#     with "oper=remove" attribute.
	#####################################################
	if autoDelInstParameter == 'true':
		print "\n###################################### STEP-2 ######################################"
		rootNewModelData = instChromia.instanceToUnderScores(rootNewModelData)


		for e1 in rootNewModelControl.iter():
			e1FullPath = instChromia.getElementPathString(e1,rootNewModelControl)
			e3Handle =  instChromia.getElementHandle_passingString(e1FullPath, rootDataTemp)
			
			try:
				if(e1.attrib["oper"] == "replace" and e3Handle != None):
					minEnt = e1.attrib["minEntries"]

					if checkParentInstance(e3Handle):
						handleMultiInstanceOp(int(minEnt),e3Handle,rootNewModelData)

					else:
						currInstances = getElementNumInstances(e3Handle)
						print "min_schema: ",minEnt,"currInstances: ",currInstances
						removeInstances(e3Handle, int(minEnt), int(currInstances),rootNewModelData)
						
			except KeyError:
				print "Warning: KeyError for element: ", e1


		rootNewModelData = instChromia.UnderScorestoInstance(rootNewModelData)
		#temp_stage2_e5 = tostring(rootNewModelData)
		#instChromia.writeNSafeClose('temp_stage2_e5.xml',temp_stage2_e5)
		
		#temp_stage2_e3 = tostring(rootDataTemp)
		#instChromia.writeNSafeClose('temp_stage2_e3.xml',temp_stage2_e3)
		print "###################################### STEP-2 ######################################\n"
	#####################################################






	#####################################################
	# STEP - 3: Restore customized instance values
	# For every <oper = replace> (Value) in oldModelDataXML:
	# check if it exists in e3
	# if yes... copy Element's Value from old_dataModel XML to newModelDataXML
	#####################################################
	print "\n###################################### STEP-3 ######################################"
	rootDataTemp = instChromia.instanceToUnderScores(rootDataTemp)			#(e3)
	rootNewModelData = instChromia.instanceToUnderScores(rootNewModelData)	#(e5)		# --
	rootOldModelData = instChromia.instanceToUnderScores(rootOldModelData)	#(e4)		# --

	#print "TEST:(oldModelData) ",tostring(rootOldModelData),"\n"
	#print "TEST: (newModelData) ",tostring(rootNewModelData), "\n"
	
	for e4 in rootOldModelData.iter():
		e4FullPath = instChromia.getElementPathString(e4,rootOldModelData)
		e3Handle = instChromia.getElementHandle_passingString(e4FullPath, rootDataTemp)
		e5Handle = instChromia.getElementHandle_passingString(e4FullPath, rootNewModelData)
		
		print "e4FullPath=",e4FullPath
		print "e3Handle=",e3Handle
		print "e5Handle=",e5Handle
		print "\n"
		
		try:
			#if(e4.attrib["oper"] == "replace" and (e4.text != None) and (e3Handle != None)):
			if(e4.attrib["oper"] == "replace" and (e3Handle != None)):
				print "HIT THIS BABY"

				try:
					e5Handle.text = e4.text
				except AttributeError:
					print "Create Node for this"
					#rootNewModelData = instChromia.UnderScorestoInstance(rootNewModelData)	#(e5)		# Normal
					insertInNewDataModel(e4,rootNewModelData)
					#rootNewModelData = instChromia.instanceToUnderScores(rootNewModelData)	#(e5)		# Normal

		except KeyError:
			print "Warning: KeyError for element: ", e4
	############################################################
	rootNewModelData = instChromia.UnderScorestoInstance(rootNewModelData)
	#temp_stage3_e5 = tostring(rootNewModelData)
	#instChromia.writeNSafeClose(newModelDataXML,temp_stage3_e5)
	
	rootDataTemp = instChromia.UnderScorestoInstance(rootDataTemp)
	#temp_stage3_e3 = tostring(rootDataTemp)
	#instChromia.writeNSafeClose('temp_stage3_e3.xml',temp_stage3_e3)
	#instChromia.writeNSafeClose(dataXML,temp_stage3_e3)					## check1
	
	temp_stage3_e5 = tostring(rootNewModelData)
	#instChromia.writeNSafeClose('temp_stage3_e5.xml',temp_stage3_e5)
	#instChromia.writeNSafeClose(newModelDataXML,temp_stage3_e5)
	print "###################################### STEP-3 ######################################\n"

	
	
	
	
	
	#####################################################
	# STEP - 4: Sorting
	#####################################################
	print "\n###################################### STEP-4 ######################################"
	chromiaInstSort = chromia.chromia()
	
	_DataTreeRoot = chromiaInstSort.instanceToUnderScores(rootData)
	_modelDataTreeRoot = chromiaInstSort.instanceToUnderScores(rootNewModelData)
	
	_serviceDataList = []
	for service in _DataTreeRoot:
		_serviceDataList.append(service)
	
	_serviceModelDataList=[]
	for service in _modelDataTreeRoot:
		_serviceModelDataList.append(service.tag)
		
	
	_rootElement = etree.Element('Device')
	for eachService in _serviceDataList:
		if eachService.tag in _serviceModelDataList:
			serviceElement = etree.Element(service.tag)
			_rootElement.append(serviceElement)
		
			for object in eachService:
				elementFullPath = chromiaInstSort.getElementPathString(object,_DataTreeRoot)
				
				modelElementHandle = chromiaInstSort.getElementHandle_passingString(elementFullPath, _modelDataTreeRoot)
				if modelElementHandle != None:
					serviceElement.append(modelElementHandle)
					
	
	rootElement = chromiaInstSort.UnderScorestoInstance(_rootElement)
	#print tostring(rootElement)
	dataText = tostring(rootElement)
	chromiaInstSort.writeNSafeClose(newModelDataXML,dataText)
	print "\n###################################### STEP-4 ######################################"
	
	
	waterMarkInst = addWaterMark.waterMark()
	
	dataText = tostring(rootDataTemp)
	instChromia.writeNSafeClose(dataXML,dataText)
	waterMarkInst.addWaterMark(dataXML,'instance','ReadWrite')
	
	
	#waterMarkInst.addWaterMark(dataXML,'instance','ReadWrite')				## check1
	waterMarkInst.addWaterMark(newModelDataXML,'model_instance','ReadWrite')
	return 0