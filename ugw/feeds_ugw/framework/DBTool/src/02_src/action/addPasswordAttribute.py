#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import sys
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
import os

sys.path.append('../common')
from common import *


class addPasswordAttribute():

	def __init__(self):
		pass

		
	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
	
	
	def findElement(self,list,parentNode,index):
		depth = len(list)
		#print list,parentNode,index,depth,"\n"
		
		if index == depth:
			#print list, parentNode,"\n"
			return parentNode
		
		for child in parentNode:
			if child.tag == list[index]:
				index+=1
				parent=child
				return self.findElement(list,parent,index)
			
		print "###############","No Element found",list,"\n"
		return

	def removeInstance(self,pwdList):
		newList=[]
		for each in pwdList:
			list = each.split(".")
			while '{i}' in list:
				list.remove('{i}')

			newList.append('.'.join(list))
		return newList

	def main(self,xmlFile):
		print "Reading Password properties file: ", passwordFile
		
		passwordElemList=[]
		
		try:
			with open(passwordFile,'r') as file:
				#print file
				for line in file:
					if not (line.startswith('#')):
						passwordElemList.append(line.rstrip())
			file.close()
		except:
			print "File not Found: ",passwordFile
			return -1
		
		newPwdList = self.removeInstance(passwordElemList)
		print newPwdList
		tree = etree.parse(xmlFile)
		self.root = tree.getroot()
		
		
		for pwdString in newPwdList:
			pwdList = pwdString.split('.')
			pwdElement = self.findElement(pwdList,self.root,index=1)
			print pwdElement
			if pwdElement != None:
				pwdElement.attrib['password'] = 'y'
				
		textData = (tostring(self.root))
		self.writeNSafeClose(xmlFile,textData)

		return 0