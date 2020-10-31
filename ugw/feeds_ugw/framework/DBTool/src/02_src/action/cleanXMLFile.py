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
This XML clean up the un-necessary spaces in the xml file
'''
import re
import os

class cleanXMLFile():
	def __init__(self):
		pass

	#customised for this module - reuse after checking the file handle logic if being copied elsewhere	
	def writeNSafeClose(self,xmlFile, data):
		fileFd2 = open(xmlFile,'w')
		fileFd2.write(data)
		fileFd2.flush()
		os.fsync(fileFd2.fileno())
		fileFd2.close()

		
	def main(self,xmlFile):
		print "Cleaning XML by removing spaces\n",xmlFile
		fileFd1 = open(xmlFile,'r')
		text = fileFd1.read()
		fileFd1.flush()
		fileFd1.close()
		
		textList = text.split('\n')

		newTextList=[]
		for line in textList:
			if re.match(r'^\s*$', line):
				pass
			else:
				newTextList.append(line)

		newText = '\n'.join(newTextList)

		textData = newText
		self.writeNSafeClose(xmlFile,textData)

		return 0