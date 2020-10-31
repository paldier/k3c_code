
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

def splitXML(file):
	try:
		DOMTree = xml.dom.minidom.parse(file)
	except IndexError:
		print "Enter python splitXML lq_control.xml"
		sys.exit(0)

	root=DOMTree.firstChild
	#print "root=",root

	savePath = "../"+xml_services_path
	if not os.path.exists(savePath):
		os.makedirs(savePath)

	for services in root.childNodes:
		if services.nodeType == xml.dom.Node.ELEMENT_NODE:
			#print "services=",services
			doc = Document()
			rootNode = doc.createElement(root.tagName)
			doc.appendChild(rootNode)
			rootNode.appendChild(services)
			#print services.toprettyxml()
			fileName=savePath+services.tagName+".xml"
			#print "fileName=",fileName
			file=open(fileName,'w')
			file.write(rootNode.toprettyxml())
			file.close
			print "Created: ",fileName
	print "\nAll services are successfully created in the path",savePath