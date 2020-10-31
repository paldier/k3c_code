
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
Refer README.doc file for minidom usage in python
All series of transformer script files will build the new xml step-by-step from the raw-xml.
Each script function is explained in their respective files
This script mainly makes use of stripped xml file as an input file, and the outcome of cleanup.py
transformer1.py will build a basic xml file with a root node along with its attributes

Usage:
python transformer1.py <stripped xml-file>
'''

## All modules to be imported here
import xml.dom.minidom
import sys,os
from xml.dom.minidom import Document,parse
from getIndexAddInstance import getIndexAddInstance

sys.path.append('../common')
from common import *


class transformer1():
	
	def __init_(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		
		
	def main(self,strippedXMLFile):
		print "transformer1 processing for XML: ",strippedXMLFile
		
		## Parse the xml file from the input argument given

		DOMTree = xml.dom.minidom.parse(strippedXMLFile) 

		name= DOMTree.getElementsByTagName("object")[0].getAttribute("name")
		nameList=name.split(".")[:-1]
		name=getIndexAddInstance(nameList)[0]

		_access = DOMTree.getElementsByTagName("object")[0].getAttribute("access")
		_maxEntries = DOMTree.getElementsByTagName("object")[0].getAttribute("maxEntries")
		_minEntries = DOMTree.getElementsByTagName("object")[0].getAttribute("minEntries")

		## Document will create a new xml file
		## Document is the master node xml file
		## there is only a single element for this Document and ie root node of XML
		## an element is created for a document
		doc = Document()
		device = doc.createElement(name)
		doc.appendChild(device)

		device.attributes['access'] = _access
		device.attributes['maxEntries'] = _maxEntries
		device.attributes['minEntries'] = _minEntries

		## Once the DOM tree is constructed, we write it to a file
		## print doc.toprettyxml(indent='\t')
		## savePath variable contains where files are to be stored

		XML_NAME = strippedXMLFile.split("/")[-1]
		completeName = TRANSFORMER_PATH + XML_NAME[:12] + "_transformer1.xml"
		#print "completeName=",completeName

		## File operation, where a file is created, DOM tree contents are dumped and closed
		
		textData = (doc.toprettyxml(indent='\t'))
		self.writeNSafeClose(completeName,textData)
		

		print "transformer1.xml created in the path: ",completeName,"\n"
		return 0