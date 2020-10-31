#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
from lxml import etree
from copy import deepcopy
import chromia


class neo():
	def __init__(self):
		self.lineageList = []
		self.chromiaInst = chromia.chromia()


	def buildXMLLineage(self,lineageList,parentNode):
		print "lineageList(BXL): ",lineageList
		if lineageList == []:
			return
		
		eNode = lineageList[0]
		parent = parentNode
		print "(parent,child): ",parent," , ",eNode
		
		for child in parent:
			print "child(buildXMLLineage): ",child
			if child.tag == eNode.tag:
				print "!@#$: ",child.tag," :: ",eNode.tag
				print "exists Child: ",child
				lineageList = lineageList[1:]
				return self.buildXMLLineage(lineageList,child)
		
		parent.append(eNode)
		print "child added: ",eNode
		lineageList = lineageList[1:]
		return self.buildXMLLineage(lineageList,eNode)


	def buildNode(self,node):
		_element = etree.Element(node.tag)
		attributes = node.attrib
		
		for key,value in attributes.iteritems():
			if key == 'oper' or key == 'node' or key == 'Instance':
				_element.attrib[key] = value
		
		_element.text = node.text
		print "element(build): ",tostring(_element)
		element = self.chromiaInst.instanceToUnderScores(_element)
		self.lineageList.append(element)
		return


	''' Entry Point in this class '''
	''' Note: 'rootNewModelData' must be of normal xml root (without instanceToUnderScores converted) '''
	def buildIndependentLineage(self,eNode,rootNewModelData):
		print "eNode=",eNode
		currentNode = eNode
		
		if currentNode.tag != 'Device':
			self.buildNode(currentNode)
			parentNode = currentNode.getparent()
			return self.buildIndependentLineage(parentNode,rootNewModelData)
		
		else:
			print "########### BAZINGA ###########"
			self.lineageList.reverse()
			lastElement = self.lineageList[-1]
			
			''' check for the 'oper' attribute in an element '''
			try:
				if lastElement.attrib['oper']:
					pass
			except KeyError:
				print "oper not found in the current element"
				lastElement.attrib['oper'] = 'remove'
			
			''' check for the 'node' attribute in an element '''
			try:
				if lastElement.attrib['node']:
					pass
				
			except KeyError:
				if self.chromiaInst.isEndNode(lastElement) == True:
					lastElement.attrib['node'] = 'object'
				else:
					lastElement.attrib['node'] = 'parameter'
				
			self.buildXMLLineage(self.lineageList,rootNewModelData)