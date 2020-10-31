
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
This script assigns the default value to parameter nodes wherever necessary
and to get this default value, we make use of stripped XML to get default values and build on top of transformer7.xml and create transformer8.xml

Input file is transformer8.xml
output file is transformer9.xml

usage:
python transformer7.py ..\..\XML_files\cleanUp_out\tr-181-2-7-0-fullStripped.xml ..\..\XML_files\transformer_out\transformer7.xml
'''

from xml.etree import ElementTree
from xml.etree.ElementTree import tostring
import sys,os
from xml.dom.minidom import parse
import xml.dom.minidom
import shutil

sys.path.append('../common')
from common import *


class transformer9():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		
	def findPath(self,List,parent,index):
		depth = len(List)

		if index==depth:
			#print List, parent,"\n"
			return parent

		for child in parent:
			if child.tag == List[index]:
				index+=1
				#print child
				parent = child
				return self.findPath(List,parent,index)

	def setDefaultValue(self,path,value):
		pathList = path.split('.')
		pathList=filter(lambda a: a != '{i}', pathList)						## function used to remove {i} occurences in a list (method 1)
		#pathList = list(filter(('{i}').__ne__, pathList))					## function used to remove {i} occurences in a list (method 2)
		
		node = self.findPath(pathList,self.transRoot,index=1)
		
		try:
			node.text = value
		except AttributeError:
			Okie = True
	
	def main(self, strippedXMLFile, prevXMLFile):
		print "transformer9 processing..."
		
		with open(prevXMLFile) as transF:
			self.transTree = ElementTree.parse(transF)

		self.transRoot = self.transTree.getroot()
			
		with open(strippedXMLFile,'r+') as f:
			strippedTree = ElementTree.parse(f)
			
		strippedRoot = strippedTree.getroot()

		for object in strippedRoot.iter('object'):									## prints only object node
			#print "ObjectName=",object.attrib.get('name')					## prints the corresponding objects attribute name
			#print "object(objectInstance)=",object
			
			for param in object.iter('parameter'):
				#print "param(ParameterInstance)",param
				
				for syntax in param.iter('syntax'):
					#print syntax
					
					for default in syntax.iter('default'):
						#print object.attrib.get('name'), param.attrib.get('name'),default.attrib.get('value')
						#print default,default.attrib.get('value')
						path = object.attrib.get('name')+param.attrib.get('name')
						value = default.attrib.get('value')
						
						self.setDefaultValue(path,value)

		XML_NAME = strippedXMLFile.split("/")[-1]
		completeName = TRANSFORMER_PATH + XML_NAME[:12] + "_" + 'transformer9.xml'
		
		textData = (tostring(self.transRoot))
		self.writeNSafeClose(completeName,textData)


		print "transformer9.xml created in the path: ",completeName

		try:
			newDOM = xml.dom.minidom.parse(completeName)
		except:
			print "Error"

		final_xml_path_local = FINAL_XML_PATH + XML_NAME[:12] + "_lq_control.xml"
		shutil.copy2(completeName,final_xml_path_local)									## creates a copy of a file
		print "File Successfully created: ",final_xml_path_local,"\n"
		
		return 0