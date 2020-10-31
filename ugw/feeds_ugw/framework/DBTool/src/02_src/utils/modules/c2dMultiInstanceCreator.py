
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
import os
import chromia

##############################################
##############################################
class multiInstanceCreator():
	def __init__(self):
		self.chromiaInst = chromia.chromia()


	def getInstanceCount(self,min,max):
		min = int(min) if(min.isdigit()) else 0
		max = int(max) if(max.isdigit()) else 256
		count = min if min<max else max
		return count


	def multiInstanceCreation(self):
		multiInstObjList =[]
		for child in self.root.iter():
			try:
				if (child.attrib['multiInst']):
					multiInstValue = (child.attrib['multiInst']).lower()
					if multiInstValue == 'yes':
						multiInstObjList.append(child)
			except KeyError:
				pass
				
		#print "multiInstObjList=",multiInstObjList
		
		if multiInstObjList == []:
			return
		
		for elemNode in multiInstObjList:
			min = elemNode.attrib['minEntries']
			max = elemNode.attrib['maxEntries']
			count = self.getInstanceCount(min,max)

			parentNode = elemNode.getparent()
			position = self.chromiaInst.getElementPosition(elemNode)
			
			if count == 0:
				parentNode.remove(elemNode)
			else:
				for indexCount in range(count):
					elemNode.attrib['Instance'] = str(indexCount+1)
					position+=1
					self.chromiaInst.insertElementAtPosition(elemNode,position)

				parentNode.remove(elemNode)
		
		## removeAttributeRecursive(chrmoia) 
		## TBD
		for child in self.root.iter():
			try:
				if child.attrib['Instance']:
					dict = child.attrib
					dict.pop('multiInst',None)
					#print child.attrib
			except KeyError:
				pass
		
		return self.multiInstanceCreation()
	
##############################################
##############################################

	def main(self,controlXMLFile,dataXMLFile):
		print "Generating multiple instances for file: ",controlXMLFile
		tree = etree.parse(controlXMLFile)
		self.root = tree.getroot()
		self.multiInstanceCreation()
		
		fileFd=open(dataXMLFile,'w')
		fileFd.write(tostring(self.root))
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		return 0