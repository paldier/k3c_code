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
This script adds dynamic="true" attribute elements wherever 'stats' node is present and its child nodes as well
'''

from xml.dom.minidom import parse
import xml.dom.minidom
import sys,os

sys.path.append('../common')
from common import *

statElemList=[]

class addDynamicAttrib():

	def __init__(self):
		pass
	
	
	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	
	def insertDynamicAttribute(self,list):
		for items in list:
			items.setAttribute("dynamic","true")
		
		childList=[]
		for items in list:
			for childs in items.childNodes:
				if childs.nodeType == xml.dom.Node.ELEMENT_NODE:
					childList.append(childs)
					
		if childList == []:
			return
		
		else:
			return self.insertDynamicAttribute(childList)

	def populateChildList(self,list):
		childList=[]
		for i in list:
			for childs in i.childNodes:
				if childs.nodeType == xml.dom.Node.ELEMENT_NODE:
					childList.append(childs)
					
		#print "childList(populateChildList)=",childList
		
		if childList != []:
			self.getStatsNode(childList)
		else:
			return

	def getStatsNode(self,list):
		#print "list(doOperate)=",list
		for i in list:
			if 'Stats' == i.tagName:
				statElemList.append(i)
				
		self.populateChildList(list)

	def main(self,controlXML):

		inputFileName = controlXML
		print "Adding dynamic='yes' attribute to file: ",inputFileName

		os.chmod(inputFileName,0o777)

		try:
			DOM = xml.dom.minidom.parse(inputFileName)
		except IndexError:
			print "Enter python addDynamicAttrib.py lq_control.xml"

		#print "DOM=",DOM
		root=DOM.firstChild

		list=[]
		list.append(root)
		self.getStatsNode(list)

		#print "## statElemList=", statElemList

		self.insertDynamicAttribute(statElemList)

		completeName = inputFileName
		
		textData = (DOM.toxml().encode('utf-8'))
		self.writeNSafeClose(completeName,textData)

		print "dynamic attribute successfully inserted to stats\n"
		return 0