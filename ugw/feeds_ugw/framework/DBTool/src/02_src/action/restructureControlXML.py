#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/
import xml.etree.ElementTree as ET
from xml.etree.ElementTree import ElementTree, dump,tostring
from lxml import etree
import sys,os

from xml.dom.minidom import parse
import xml.dom.minidom
sys.path.append(r'../action/')
from pathCorrect import pathCorrect


class restructureControlXML():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()

	def addIndents(self,xmlFile):
		''' give indents for current xml file '''
		DOM = xml.dom.minidom.parse(xmlFile)
		fileFd=open(xmlFile,'w')
		fileFd.write(DOM.toprettyxml().encode('utf-8'))
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()


	def restructure_xml(self,xml):
		"""
			Returns a restructured xml tree moving parameter nodes above child restructureInst nodes.
		"""
		parser = etree.XMLParser(remove_blank_text=True) # lxml.etree only!
		tree = etree.parse(xml, parser)
		root = tree.getroot()
		for child in root:
			childIterList=[]
			for node in child.iter():
				childIterList.append(node)
			
			childIterList.reverse()
			for node in childIterList:
			#if no. of children is 0, that is a parameter node
				if len(node) ==  0:
					node.getparent().insert(0, node)
		return tree
		
		
		

	def processRestructure(self,controlXML):
		newTree = self.restructure_xml(controlXML)
		new_root = newTree.getroot()
		
		completeName = controlXML
		textData = tostring(new_root)
		self.writeNSafeClose(completeName,textData)

		self.addIndents(completeName)
		return 0


def do_restructureControlXML(_fullPath):
	fullPath = pathCorrect(_fullPath)
	restructureInst = restructureControlXML()

	if fullPath[-1] == '*':
		list_of_files = os.listdir(fullPath[:-1])
		for file in list_of_files:
			if file.endswith("_control.xml"):
				control_xml_file_path = fullPath[:-1]+file
				restructureInst.processRestructure(control_xml_file_path)
		return 0
		
	elif fullPath.endswith("_control.xml"):
		restructureInst.processRestructure(fullPath)
		return 0
		
	print "Error in do_restructureControlXML: error (-1)"	
	return -1
		


fullPath = sys.argv[1]
do_restructureControlXML(fullPath)
