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
Master script where XML to EXCEL and vice-verse is done.
In this script we can invoke either of the actions required.
'''
import sys,os

sys.path.append('modules')

from toExcel import toExcel
from toXML import toXML
from splitXML import splitXML

sys.path.append('../common')
from common import *

sys.path.append(r'../action/')
from pathCorrect import pathCorrect


class x2x():

	toXMLOB='NULL'
	toExcelOB='NULL'
	
	def __init__(self):
		print "Start of conversion"
	
	def createXL(self,file):
		
		if not(os.path.lexists(file)):
			print "Error: ", file, " does not exist!"
			return -1
			
		if not(file.lower().endswith('.xml')):
			print "Error: ", file, " is not an XML file!"
			return -1
		
		
		self.toExcelOB = toExcel()
		
		if file.endswith("_data.xml"):
			print "data XML conversion started..."
			self.toExcelOB.createDataExcel(file)
		
		elif file.endswith("_control.xml"):
			print "control XML conversion started..."
			self.toExcelOB.createControlExcel(file)
			
		else:
			print "\nError: faulty input for Excel conversion"
			print "\nInput file should end with _control.xml or _data.xml for respective conversion"
			print "\nKindly follow the naming conventions"
			return -1

	def createXML(self,file):
		
		if not(os.path.lexists(file)):
			print "Error: ", file, " does not exist!"
			return -1
			
		if not(file.lower().endswith('.xlsx')):
			print "Error: ", file, " is not an XML file!"
			return -1
		
		self.toXMLOB = toXML()

		if file.endswith("_data.xlsx"):
			print "data Excel conversion started..."
			self.toXMLOB.createDataXML(file)
		
		elif file.endswith("_control.xlsx"):
			print "control Excel conversion started..."
			self.toXMLOB.createControlXML(file)
		
		else:
			print "\nError: faulty input for XML conversion"
			print "\nInput file should end with _control.xlsx or _data.xlsx for respective conversion"
			print "\nKindly follow the naming conventions"
			return -1
			
	
	def splitXML(self,file):
		splitXML(file)
	
	def errorThrow(self):
		print "\n","Usage:" 
		print "x2x.py -toEXLFullPath <service>_[control|data].xml"
		print "x2x.py -toXMLFullPath <service>_[control|data].xlsx"

''' Code starts from here '''

x2xob = x2x()
try:
	print "##-",sys.argv[1]
	inputFile = sys.argv[2]
	''' Check if the given input file is a valid file and an xml file '''
	
	if sys.argv[1] == '-toEXLFullPath':
		x2xob.createXL(inputFile)
		
	elif sys.argv[1] == '-toXMLFullPath':
		x2xob.createXML(inputFile)
		
	elif sys.argv[1] == '-splitXML':
		x2xob.splitXML(inputFile)
		
	else:
		print "Error: Invalid Arguments!\n"
		x2xob.errorThrow()
		exit(0)

except IndexError:
	print "Error: Index Error!"
	x2xob.errorThrow()
	exit(0)