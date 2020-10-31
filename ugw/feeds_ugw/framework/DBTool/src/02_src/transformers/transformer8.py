
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
Transformer8.py will replace __INSTANCE__ by multiInst="yes" attribute.
Input file is transformer7.xml
output file is transformer8.xml

Remove duplicate tags from the dataType.xml file

usage:
python transformer7.py ..\..\XML_files\transformer_out\transformer6.xml
'''

from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os

sys.path.append('../common')
from common import *


class transformer8():

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
			self.doOperate(childList)
		else:
			return

	def doOperate(self,list):
		#print "list(doOperate)=",list
		for i in list:
			if '__INSTANCE__' in i.tagName:
				i.setAttribute("multiInst","yes")

		self.populateChildList(list)

	##----------- main code starts here------------------
	def main(self,prevXMLFile):
		XML_NAME = prevXMLFile.split("/")[-1]
		print "transformer8 processing..."
		
		## take input file
		inputFileName = prevXMLFile
		#print inputFileName

		DOMTree_trans8 = xml.dom.minidom.parse(inputFileName)
		root = DOMTree_trans8.firstChild
		#print "root=",root

		## send [<ELEMENT: instance root>] in list form
		list=[]
		list.append(root)

		## operate on each item in the list
		self.doOperate(list)

		## write to a file
		savePath = TRANSFORMER_PATH
		if not os.path.exists(savePath):
			os.makedirs(savePath)

		inputFileName = TRANSFORMER_PATH + 'transformer8_1.xml'

		#completeName=os.path.join(savePath,fName+ ".xml" )
		completeName = TRANSFORMER_PATH + XML_NAME[:12] + '_transformer8.xml'
		#print "completeName=",completeName

		textData = (DOMTree_trans8.toprettyxml().encode('utf-8'))
		self.writeNSafeClose(inputFileName,textData)
		
		with open(completeName, "w") as fout:
			with open(inputFileName, "r+") as fin:
				for line in fin:
					fout.write(line.replace('__INSTANCE__', '' ))

		print "transformer8.xml created in the path: ",completeName,"\n"
		os.remove(inputFileName)

		try:
			newDOM = xml.dom.minidom.parse(completeName)
		except:
			print "Error"

		'''
		NOTE : 
		replacing __INSTANCE__ as multiisnt="yes" in file operation is causing problem, since in every node  
		replacing <NODE__INSTANCE__> as <NODE multiInst="yes"> is fine, whereas 
		replacing </NODE__INSTANCE__> as </NODE multiInst="yes"> is not accepatble
		Hence. a separate algorithm will take care of this - First Depth algorithm
		'''
		return 0