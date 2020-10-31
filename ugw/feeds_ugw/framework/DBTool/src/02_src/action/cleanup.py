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
The main objective of this script is to clean up the un-necessary contents from the raw xml provided (from broadband forum)
-------Usage-------:
python cleanup.py <raw-xml-file>
'''

## All needed modules to be imported here
## Modules can be imported at any point in the code. Since python is an interpreter we should import the module before we use them

from xml.dom.minidom import parse
import xml.dom.minidom
import sys
from xml.dom.minidom import Document
import os

sys.path.append(r'../common')
from common import *
from pathCorrect import pathCorrect

'''
## minidom is a module, which is already imported in the beginning.
## mindom.parse to parse the xml file and construct a DOM tree in memory.
## we use this DOM tree to traverse through the tree.
## 'try' and 'except' is more or less like a 'try-catch' in java to handle the exception cases
'''

class cleanup():
	def __init__(self):
		pass


	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()


	def main(self,_tr_xml_path):
		tr_xml_path = pathCorrect(_tr_xml_path)
		print "tr_xml_path=",tr_xml_path
		XML_NAME = tr_xml_path.split('/')[-1]

		DOMTree = xml.dom.minidom.parse(tr_xml_path)

		'''<COMMENT node> nodes are being removed in this section '''
		commentNode=[]
		for child in DOMTree.childNodes:		## get the childNodes of Document
			if child.nodeType == xml.dom.Node.COMMENT_NODE:
				commentNode.append(child)

		for i in commentNode:
			i.parentNode.removeChild(i)

		'''description node is kept back in the XML file'''
		'''
		####### In this section, remove all <description> nodes recursively
		description =  DOMTree.getElementsByTagName('description')
		for list in description:
			list.parentNode.removeChild(list)
		'''

		document = DOMTree.childNodes[0]
		documentList = document.childNodes

		dataTypeList=[]

		for child in documentList:
			if child.nodeType == xml.dom.Node.ELEMENT_NODE and child.nodeName != 'model':
				if child.nodeName == 'dataType':
					dataTypeList.append(child)
				child.parentNode.removeChild(child)

		''' In this section we remove attributes (drm:version) ''' 
		dmrList = ['object', 'parameter', 'profile']

		for list in dmrList:
			element = DOMTree.getElementsByTagName(list)
			for child in element:
				pass
				try:
					child.removeAttribute("dmr:version")
				except xml.dom.NotFoundErr:
					pass

		####### Now we have a DOM tree which is stripped off with some attributes and un-necessary elements
		####### Write the DOM tree to a file 
		####### A particular file name and path is chosen

		if not os.path.exists(cleaned_xml_path):
			os.makedirs(cleaned_xml_path)

		name = tr_xml_path
		fName= name.split("/")[-1]
		name = fName.split(".xml")[0]

		completeName = os.path.join(cleaned_xml_path, name + "Stripped.xml")


		textData = DOMTree.toxml().encode('utf-8','replace')
		self.writeNSafeClose(completeName,textData)

		print "\n","Cleanup successful for XML",XML_NAME,"\n"
		print "Cleaned up XML created in the path: ",completeName
		
		nameData = XML_NAME[:12] + "_dataType.xml"
		completeNameD = cleaned_xml_path + "dataTypes/" + nameData
		
		f = open(completeNameD,"w")
		f.close()

		dataTypeDoc = xml.dom.minidom.Document()
		dataTyperoot = dataTypeDoc.createElement('root')

		for list in dataTypeList:
			#print list
			dataTyperoot.appendChild(list)

		textData = dataTyperoot.toxml()
		self.writeNSafeClose(completeNameD,textData)

		print "dataType created in the path",completeNameD,"\n"
		###----------------------------------------------------------------------------

		## This section will remove profile nodes--------------------------------------
		#print "PATH: ",completeNameD,"\n"

		DOMProfile = xml.dom.minidom.parse(completeName)
		#print "DOMProfile=",DOMProfile
		profileDoc = DOMProfile.firstChild
		#print "profileDoc=", profileDoc
		model = profileDoc.childNodes[1]

		modelChilds = model.childNodes
		profileList = []
		for child in modelChilds:
			if child.nodeType == xml.dom.Node.ELEMENT_NODE and child.tagName == 'profile':
				#print child.tagName
				profileList.append(child)
				child.parentNode.removeChild(child)

		textData = DOMProfile.toxml().encode('utf-8','replace')
		self.writeNSafeClose(completeName,textData)

		################################################################
		###### TO create a seperate directory and xml files for PROFILES

		if not os.path.exists(profile_path):
			os.makedirs(profile_path)
		name = XML_NAME[:12] + "_fullProfile.xml"

		fullProfile = os.path.join(profile_path, name)
		#print "fullProfile=",fullProfile

		doc = Document()
		root = doc.createElement("rootProfile")

		for list in profileList:
			root.appendChild(list)

		textData = root.toprettyxml()
		self.writeNSafeClose(fullProfile,textData)


		for list in profileList:
			fName=list.getAttribute("name")
			fName=fName.replace(":","_")
			fullName = os.path.join(profile_path,fName+".xml")
			textData = list.toprettyxml()
			self.writeNSafeClose(fullName,textData)


		print "Profile separated successfully\n"
		return 0
