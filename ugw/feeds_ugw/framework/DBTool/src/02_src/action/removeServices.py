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
This script removes the services that are mentioned in the skipList.txt
Any services that are commented out with #service will be removed and everything else is maintained
'''

import os,sys
from xml.dom.minidom import parse,Document
import xml.dom.minidom

sys.path.append('../common')
from common import *



class removeServices():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		
	def main(self,controlXML):
		print "Received: ", controlXML
		skipList = []
		try:
			with open(skipListFile,'r') as file:
				for line in file:
					if not (line.startswith('#')):
						skipList.append(line.rstrip())

		except IOError:
			print "Warning: No skipList file found"
			return 0

		#print "skipList=",skipList

		inputFile = controlXML

		DOM = xml.dom.minidom.parse(inputFile)

		root = DOM.firstChild

		for services in root.childNodes:
			if services.nodeType == xml.dom.Node.ELEMENT_NODE:
				if services.tagName in skipList:
					print "Service Removed: ", services.tagName
					services.parentNode.removeChild(services)
		
		textData = (DOM.toxml().encode('utf-8'))
		self.writeNSafeClose(inputFile,textData)

		print "Services removed: ",skipList, " from the XML file: ",inputFile,"\n"
		return 0