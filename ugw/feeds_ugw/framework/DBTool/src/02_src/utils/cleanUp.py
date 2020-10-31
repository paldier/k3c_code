
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
Clean up XML files
Clean up Excel files
Do this recursively for sub-directories
'''

import os,sys
sys.path.append('../action/')
from pathCorrect import pathCorrect

DB_TOOL_ROOT = r'../../'
I_XML_FILES_PATH = DB_TOOL_ROOT + r"01_db_files/xml_files/03_Intermediates"
PYC_FILES_PATH = DB_TOOL_ROOT + r"02_src/"


def cleanBinFiles():
	for root, dir, files in os.walk(PYC_FILES_PATH):
		if files != []:
			for i in files:
				if i.endswith(".pyc") or i.endswith(".class"):
					file_path=root+"/"+i
					print "removed: ",file_path
					os.remove(pathCorrect(file_path))
	print ".pyc and .class files successfully removed"
	return 0


def cleanInterimFiles():
	for root, dir, files in os.walk(I_XML_FILES_PATH):
		if files != []:
			for i in files:
				if i.endswith(".xml"):
					file_path=root+"/"+i
					print "removed: ",file_path
					os.remove(pathCorrect(file_path))
	print "Intermediate XML files successfully removed"
	return 0

	
def throwError():
	print "   Usage:"
	print "	  cleanUp.py -binaryFiles"
	print "	  cleanUp.py -interimXMLFiles"
	print "	  cleanUp.py -cleanAll"
#
#############################################################

try:
	if sys.argv[1] == '-binaryFiles':
		cleanBinFiles()
		
	elif sys.argv[1] == '-interimXMLFiles':
		cleanInterimFiles()
	
	elif sys.argv[1] == '-cleanAll':
		cleanBinFiles()
		cleanInterimFiles()
		
	else:
		throwError()
		
		
except IndexError:
	throwError()