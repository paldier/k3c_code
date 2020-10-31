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
service splitter takes the transformed XML file, preferably the latest XML file available after transformation.
this script splits up the services into different xml files and stores under the directory path /XML_Files/services/

usage:
	python serviceSplitter.py transformerX.xml

X -> latest transformed file
'''

from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os

sys.path.append('../common')
from common import *


class serviceSplitter():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()

	def main(self,controlXML):
		file=controlXML
		print "Received file: ",file

		DOMTree = xml.dom.minidom.parse(file)
		
		root=DOMTree.firstChild

		savePath = xml_services_path
		if not os.path.exists(savePath):
			os.makedirs(savePath)

		for services in root.childNodes:
			if services.nodeType == xml.dom.Node.ELEMENT_NODE:
				doc = Document()
				rootNode = doc.createElement(root.tagName)
				doc.appendChild(rootNode)
				rootNode.appendChild(services)
				fileName=savePath+services.tagName+"_control.xml"

				textData = (rootNode.toxml())
				self.writeNSafeClose(fileName,textData)

				print "Created: ",fileName
		print "All services are successfully created in the path",savePath,"\n"
		
		return 0