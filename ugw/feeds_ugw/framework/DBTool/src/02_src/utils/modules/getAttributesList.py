
#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

from xml.dom.minidom import parse
import xml.dom.minidom
import sys
import os
from xml.dom.minidom import Document

attribNameList=[]

def appendAttribList(node):
	#print "node(AAL)=",node
	attribList=node.attributes.items()
	#print attribList
	
	for i in attribList:
		if i[0] not in attribNameList:
			attribNameList.append(i[0])
	
def getChildNodes(node):
	childList=[]
	for i in node.childNodes:
		if i.nodeType == xml.dom.Node.ELEMENT_NODE:
			childList.append(i)
	return childList

def depthFirst(node,rootName,flag='NULL'):
	root=node
	#print "rootName=",rootName
	#print "root=",root
	child = getChildNodes(root)
	#print root,flag
	#print "child=",child
	
	if child !=[]:
		#print "It's not a leaf Node"
		randItem = child[-1]
		return depthFirst(randItem,rootName)
	
	elif child == []:
		#print "It's a Leaf Node"
		#print "flag=",flag
		#print "root.tagName(TO BE PRINTED)=",root.tagName
		appendAttribList(root)
		
		if root.tagName == rootName:# and flag == 'NULL':
			#print "breakpoint hit"
			return

		node = root.parentNode									## get Parent Node
		root.parentNode.removeChild(root)						## remove current node
		#print "Parent node=",node
		return depthFirst(node,rootName)

def main(file):
	#DOMTree = xml.dom.minidom.parse('transformer8.xml')
	DOMTree = xml.dom.minidom.parse(file)
	sys.setrecursionlimit(10000)
	
	''' Get the root node ('Device') '''
	for child in DOMTree.childNodes:
		if child.nodeType == xml.dom.Node.ELEMENT_NODE:
			root = child
	
	#print "[ root=",root.tagName,"]"

	for childs in root.childNodes:
		if childs.nodeType == xml.dom.Node.ELEMENT_NODE:
			#print childs
			rootName = childs.tagName
			depthFirst(childs,rootName,"first")
			#print "\n BREAK \n"
			
	#print "OFF THE LOOP"
	#print attribNameList
	#print "total items in list=",len(attribNameList)
	return attribNameList