#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/
from xml.etree import ElementTree
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
import os


class insertParamNodePPPoA():

	def __init__(self):
		pass

	def writeNSafeClose(self,xmlFile, data):
		fileFd = open(xmlFile,'w')
		fileFd.write(data)
		fileFd.flush()
		os.fsync(fileFd.fileno())
		fileFd.close()

	def main(self,file):
		print "Received file: ",file

		if (os.path.exists(file)):
			tree = ElementTree.parse(file)
			root = tree.getroot()
			flag=False

			for child in root.iter():
				if child.tag == 'PPPoA':
					elementNode = child
					flag=True
					break

			if flag == True:
				print elementNode
				dummyElem = SubElement(elementNode,'X_LANTIQ_DummyLeafParam',access="readWrite",syntax="unsignedInt")
				dummyElem.text = '0'
				
				textData = (tostring(root))
				self.writeNSafeClose(file,textData)

				return 0
			
			else:
				return 0
		else:
			print "File Don't exists and proceeding transformation: ", file,"\n"
			return 0