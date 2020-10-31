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
this scripts removes the element nodes where its attribute nodes are status="deprecated"
'''

from xml.dom.minidom import parse
import xml.dom.minidom
import sys
import os
sys.path.append('../common')
from common import *



class removeStatusDeprecated():

	def __init__(self):
		self.statusElemList=[]
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
					
		if childList != []:
			self.getStatusNode(childList)
		else:
			return

	def getStatusNode(self,list):
		#print "list(getStatusNode)=",list
		
		for i in list:
			attributes = i.attributes.items()
			for attribs in attributes:
				if attribs[0] == "status" and attribs[1] == "deprecated":
					self.statusElemList.append(i)
		
		self.populateChildList(list)

	def main(self,controlXML):

		inputFileName = controlXML
		print "Removing status='deprecated' from file: ",inputFileName
		DOM = xml.dom.minidom.parse(inputFileName)

		root=DOM.firstChild

		list=[]
		list.append(root)
		self.getStatusNode(list)
		print "statusElemList: ",self.statusElemList
		
		try:
			for elements in self.statusElemList:
				print elements
				#print "Removed: ",elements.tagName
				elements.parentNode.removeChild(elements)
		
		except AttributeError:
			print "Element not found"
		
		completeName = inputFileName
		
		textData = (DOM.toxml().encode('utf-8'))
		self.writeNSafeClose(completeName,textData)

		print 'Elements with attribute status="deprecated" removed\n'
		return 0
