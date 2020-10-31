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
this script adds profile attribute to the parameter nodes defined by the tr-181/tr-104/tr-069 BBF XML.
separated out profile file is kept in Intermediate directory path
'''
from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os

sys.path.append('../common')
from common import *


class addProfileAttrib():

	def __init__(self):
		pass

		
	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()

		
	def getPathElementObject(self,list,parent='NULL',index=0):
		#print parent
		#print index
		#print "list(getPathElementObject)=",list
		depth=len(list)
		
		if index == 0:
			parent = self.DOM_FINAL_XML_ROOT
			
		if depth != 0:
			if list[index] == parent.tagName:
				#print "Matching... continue"
				pass
			else:
				pass
				#print "list mismatch... Terminating"

			
		index+=1
		
		if index == depth:
			return parent
		
		for child in parent.childNodes:
			if child.nodeType == xml.dom.Node.ELEMENT_NODE:
				if child.tagName == list[index]:
					parent = child
					return self.getPathElementObject(list,parent,index)

	def addProfileAttribute(self,profileName,objectPathList,parameterList):
		node = self.getPathElementObject(objectPathList)
		#print "node=",node
		for parameters in node.childNodes:
			if parameters.nodeType == xml.dom.Node.ELEMENT_NODE:
				#print parameters
				
				if parameters.tagName in parameterList:
					#print "Parameter present... append profile attribute"
					parameters.setAttribute("profile",profileName)

	def remove_i_(self,list):
		while '{i}' in list:
			list.remove('{i}')
		
		return list

	###################################################################################
	############################################################# MAIN CODE STARTS HERE
	def main(self,controlXML,profileXML):
		DOM_FINAL_XML = xml.dom.minidom.parse(controlXML)
		DOM_PROFILE_XML = xml.dom.minidom.parse(profileXML)

		self.DOM_FINAL_XML_ROOT = DOM_FINAL_XML.firstChild
		self.DOM_PROFILE_XML_ROOT = DOM_PROFILE_XML.firstChild

		for profiles in self.DOM_PROFILE_XML_ROOT.childNodes:
			if profiles.nodeType == xml.dom.Node.ELEMENT_NODE:
				profileName = profiles.getAttribute("name")    # (get profile name)

     
				for objects in profiles.childNodes:
					if objects.nodeType == xml.dom.Node.ELEMENT_NODE:
						if objects.tagName == 'object':
							objectPath = objects.getAttribute("ref")
							objectPathList =  objectPath.split(".")[:-1]
							objectPathList = self.remove_i_(objectPathList)

							#print profileName, objectPathList
							parameterList=[]
							for parameters in objects.childNodes:
								if parameters.nodeType == xml.dom.Node.ELEMENT_NODE:
									parameterList.append(parameters.getAttribute("ref"))
									#print parameters.getAttribute("ref")
							
							
							self.addProfileAttribute(profileName,objectPathList,parameterList)


		completeName = controlXML
		
		textData = (DOM_FINAL_XML.toxml().encode('utf-8'))
		self.writeNSafeClose(completeName,textData)

		print "Profile attribute successfully inserted\n"
		return 0