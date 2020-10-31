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
This scripts removes the unwanted attributes(needs to be mentioned in the list) from the element nodes
'''

from xml.dom.minidom import parse
import xml.dom.minidom
import sys,shutil
import os

sys.path.append('../common')
from common import *


attributeList=['description','targetType','targetParent','targetParam','dmr_fixedObject','refType','nullValue']

class removeAttributes():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()

	def populateChildList(self,list):
		childList=[]
		for i in list:
			for childs in i.childNodes:
				if childs.nodeType == xml.dom.Node.ELEMENT_NODE:
					childList.append(childs)

		#print "childList(populateChildList)=",childList

		if childList != []:
			self.removeAttributes(childList)
		else:
			return

	def removeAttributes(self,list):
		#print "list(removeAttributes)=",list
		
		for elements in list:
			for attributes in attributeList:
				try:
					elements.removeAttribute(attributes)
				except:
					continue
			
		return self.populateChildList(list)

	###########################################################################
	##################################################### MAIN CODE STARTS HERE
	###########################################################################

	def main(self,controlXML):

		inputFileName = controlXML

		new_name = inputFileName.split('/')[-1]
		new_name = new_name.split('.xml')[0]
		description_xml_file = FINAL_XML_PATH + new_name + "_description_control.xml"
		shutil.copy2(inputFileName,description_xml_file)

		print "Removing attributes from file: ",inputFileName
		DOM = xml.dom.minidom.parse(inputFileName)

		#print "DOM=",DOM
		root=DOM.firstChild

		list=[]
		list.append(root)
		self.removeAttributes(list)

		completeName = inputFileName
		
		textData = (DOM.toprettyxml())
		self.writeNSafeClose(completeName,textData)

		print 'Unwanted attributes removed\n',attributeList,"\n"
		
		return 0