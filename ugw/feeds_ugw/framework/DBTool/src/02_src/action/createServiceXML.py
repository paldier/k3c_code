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
This creates service XMLs from set of control XML files.
this configuration is available in the file 'mergeControlXMLs.conf'
'''

import re, sys
from xml.dom.minidom import *
import xml.dom.minidom
import collections
import os

sys.path.append('../common')
from common import *


class createServiceXML():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()
		
		
	def buildServiceXML(self,key,source_files,destination_file):
		print "Processing RULE_KEY:",key
		doc = Document()
		root = doc.createElement('Device')

		for files in source_files:
			FILE = xml_services_path+files
			
			if not (os.path.exists(FILE)):
				print "\n######################################"
				print "File not found: ", FILE
				print "RULE KEY ERROR: ", key
				print "########################################\n"
				return

			DOM = xml.dom.minidom.parse(FILE)
			device = DOM.firstChild
			
			for childs in device.childNodes:
				if childs.nodeType == xml.dom.Node.ELEMENT_NODE:
					#print "childs=",childs.tagName
					_element_of_interest = childs
			
			root.appendChild(_element_of_interest)

		dest_file = xml_services_path+destination_file
		
		textData = (root.toxml())
		self.writeNSafeClose(dest_file,textData)


	def main(self):
		'''-----------------------------------------------------------------------------------------'''
		'''-----------------------------------------------------------------------------------------'''
		'''mergeServicesFile is defined in common.py script '''
		print "Started creation of Merging XML files"
		f = open(mergeServicesFile, 'r')
		text = f.read()
		textList = text.split('\n')
		f.close()

		rule_key_list = []
		source_files_list = []
		destination_list = []

		for lines in textList:
			if lines.startswith('$RULE_KEY'):
				rule_key_list.append(lines)

			elif lines.startswith('$SOURCE_FILES'):
				source_files_list.append(lines)

			elif lines.startswith('$DESTINATION_FILE'):
				destination_list.append(lines)

		'''------------------------------ Error Checks ----------------------------------'''
		if (len(rule_key_list) != len(source_files_list)) and (len(source_files_list) != len(destination_list)):
			print "Error in Configuration file"
			exit(0)

		''' Find the duplicate Key '''
		duplicate = [x for x, y in collections.Counter(rule_key_list).items() if y > 1]
		if duplicate != []:
			print "duplicate RULE KEY exists and cannot be continued..."
			print "Assign unique RULE KEYS"
			exit(0)

		''' Find source file if it exists '''

		'''------------------------------------------------------------------------------'''
		index=0
		while(1):
			try:
				key = rule_key_list[index].split(':')[1]
				source_files = source_files_list[index].split(':')[1]
				destination_file = destination_list[index].split(':')[1]
				t_source_files = source_files.split(',')
				source_files=[]

				for lists in t_source_files:
					corrected = lists.replace(' ','')
					source_files.append(corrected)

				#print "@@@###$$$: ",key,source_files,destination_file
				self.buildServiceXML(key,source_files,destination_file)

				index+=1

			except IndexError:
				break

		'''
		There are many things we need to check in this process:
		1. no two rule key should be same
		2. Throw an error if any of the source files doesn't exists
		3. while throwing an error, print the rule key and file names.
		'''
		return 0