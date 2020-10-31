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
This script adds comment tag in the beginning to describe the file type.
This file type is used by front end scripts to display accordingly
'''

from xml.dom.minidom import parse,Document
import xml.dom.minidom
import sys,os
sys.path.append('../common')
from common import *
sys.path.append(r'../action/')
sys.path.append(r'../utils/')


class waterMark():

	def pathExists(filePath):
		return os.path.lexists(filePath)
		
	def fileExists(fileName):
		pass
	
	def isXML(fileName):
		pass
	
	
	def __init__(self):
		'''-wmFileTypeList- has list of fileTypes which reads from waterMarkFileTypes.properties file '''
		self.wmFileTypeList=[]
		
		if   not (os.path.lexists(waterMarkFileType) ):
			print "Critical Error: ", waterMarkFileType, " file not found!"
			exit(0)

		if not(os.path.lexists(waterMarkFile) ):
			print "Critical Error: ", waterMarkFile, " file not found!"
			exit(0)

			
		with open(waterMarkFileType,'r') as file:
			for line in file:
				if not (line.startswith('#')):
					self.wmFileTypeList.append(line.rstrip())
		
		
		f=open(waterMarkFile,'r')
		comment = f.read()
		self.commentList = comment.split('\n')
		## File Access list 
		self.wmAccessTypeList = ['ReadOnly', 'ReadWrite']
		f.close()
		
		
	# Helper routine
	# Pass the file name and text to this function
	# Takes the file and write a new file with the text provided
	def writeNSafeClose(self,xmlFile, data):
	
		dirName = os.path.dirname(xmlFile)
		if not(os.path.lexists(dirName)):
			print "Error: ", dirName, " does not exist!"
			return -1
	
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		return 0


	def createFileNAccessType(self,fileType,accessType):
		newCommentList =[]
		
		for line in self.commentList:
			if 'AccessType' in line.rstrip():
				newLine = line.replace('#####',accessType)
				newCommentList.append(newLine)
	
			elif 'FileType' in line.rstrip():
				newLine = line.replace('@@@@@@@@',fileType)
				newCommentList.append(newLine)
			
			else:
				newCommentList.append(line)
		
		newComment = '\n'.join(newCommentList)
		return newComment

	#				
    # This is to be invoked for a file that DOES NOT carry watermark and watermark has to be be applied newly on to it.	
	# If your XML file already had watermark, then this method returns an error. Please invoke modifyWaterMark() for such cases.
	#
	def addWaterMark(self,fileName,fileType, accessType='ReadOnly'):
		if not(os.path.lexists(fileName)):
			print "Error: ", fileName, " does not exist!"
			return -1
		
		if not(fileName.lower().endswith('.xml')):
			print "Error: ", fileName, " is not an XML file!"
			return -1	
	
		if(self.isWaterMarked(fileName)):
			print "File ", fileName, " already carries waterMark, no new watermarkbeing added!"
			return -1
	
	
		print "Adding water mark to file: ", fileName,"\n"
		try:
			docFile = xml.dom.minidom.parse(fileName)
		except:
			print "File: ", xmlFile, " is corrupted!"
			return -1			
			
		rootNode = docFile.firstChild

		docComment = Document()
		commentText = self.createFileNAccessType(fileType, accessType)
		_comment = docComment.createComment(commentText)
		docComment.appendChild(_comment)
		docComment.appendChild(rootNode)

		dataText = docComment.toxml().encode('utf-8')
		self.writeNSafeClose(fileName,dataText)
		return 0

	#Invoke this if your XML file carries a watermark already and you need to modify it
	def modifyWaterMark(self,fileName,fileType, accessType='ReadOnly'):
		
		if not(os.path.lexists(fileName)):
			print "Error: ", fileName, " does not exist!"
			return -1
		
		if not(fileName.lower().endswith('.xml')):
			print "Error: ", fileName, " is not an XML file!"
			return -1	
		
		print "Modifying waterMark to file: ", fileName,"\n"
		try:
			DOM = xml.dom.minidom.parse(fileName)			
		except:
			print "File: ", fileName, " is corrupted!"
			return -1	

			
		for child in DOM.childNodes:
			if child.nodeType == xml.dom.Node.COMMENT_NODE:
				child.parentNode.removeChild(child)
		
		dataText = DOM.toxml().encode('utf-8')
		self.writeNSafeClose(fileName,dataText)			
		self.addWaterMark(fileName,fileType, accessType)
		return 0
		
		
	#New  routine - 1 defined for FE
	#should return ReadWrite, ReadOnly, DonotOpen.
	def getAccessLevel(self, fileName):
		print "getAccessLevel: ", fileName
		with open(fileName) as myfile:
			commentString=''
			for line in range(0,7):
				commentString = commentString + myfile.readline(100)
				if 'AccessType' in commentString:
					accessType = commentString.rsplit(":", 1)[1]
					print "AccessType found: ", accessType
					return accessType.strip()
		
		print "File: ", fileName, " has access type DoNotOpen (override with ReadOnly)"
		return 'ReadOnly'


	# getWMType => returns fileType only!
	#New routine - 2 -defined for FE and BE
	#returns one of "schema", "schema_id", "instance", "instance_id" .... etc.
	# refer to list from warerMarkFileTypes.properties file.
	# FE has to read this file and do a comparison.
	def getWMType(self, fileName):
		print "fileName: ",fileName
		
		with open(fileName) as myfile:
			commentString=''
			for line in range(0,7):
				commentString = commentString + myfile.readline(100)
				if 'FileType' in commentString:
					fileType = commentString.rsplit(":", 1)[1]
					print "FileType found: ", fileType
					return fileType.strip()
		
		print "File: ", fileName, " has file type Unknown"
		return 'Unknown'
	
	def isWaterMarked(self,fileName):
		if not(os.path.lexists(fileName)):
			print "Error: ", fileName, " does not exist!"
			return False
		
		if not(fileName.lower().endswith('.xml')):
			print "Error: ", fileName, " is not an XML file!"
			return False


		print "isWaterMarked: ", fileName
		try:
			DOM = xml.dom.minidom.parse(fileName)
		except:
			print "File: ", fileName, " is corrupted!"
			return False
			
		for child in DOM.childNodes:
			if child.nodeType == xml.dom.Node.COMMENT_NODE:
				return True
		return False