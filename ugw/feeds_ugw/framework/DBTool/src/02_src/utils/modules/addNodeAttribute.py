
#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import sys,os
import xml.etree.ElementTree as etree
from xml.etree.ElementTree import tostring
from lxml import etree


def insertNodeAttribute(element):
	childList=[]
	for child in element:
		childList.append(child)

	if childList == []:
		element.attrib['node'] = 'parameter'
	else:
		element.attrib['node'] = 'object'
	
	return

def addNodeAttribute(instanceFile):
	if not(instanceFile.endswith("_data.xml")):
		print "Not a valid Instance file"
		return -1
	
	print "Adding 'node' attribute to instanceFile: ",instanceFile
	try:
		tree = etree.parse(instanceFile)
		root = tree.getroot()
	except:
		print "Error: Corrupted XML file: ",instanceFile, "Terminating..!!!"
		exit(0)
	
	
	serviceNodeList = []
	serviceNodeList.append(root)
	for service in root:
		serviceNodeList.append(service)
	
	for element in root.iter():
		if element not in serviceNodeList:
			insertNodeAttribute(element)
	
	data = tostring(root)
	fileFd = open(instanceFile,'w')
	fileFd.write(data)
	fileFd.flush()
	os.fsync(fileFd.fileno())
	fileFd.close()
	print "Successfully added node attribute to: ",instanceFile
	return 0


if __name__ == '__main__':
	try:
		instanceFile = sys.argv[1]
		addNodeAttribute(instanceFile)
	except IndexError:
		print "NO arguments passed"
		print "usage: "
		print "addNodeAttribute.py <service>_data.xml"