
#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

def getIndex(List):
	#print "Merge to start"
	index=0
	indexL = []
	for i in List:
		if i == '{i}':
			indexL.append(index)
		index+=1
	#print "indexL=",indexL
	return mergeList(List,indexL)

def mergeList(List,indexL):
	reduction=0
	indexN=[]
	for i in indexL:
		i = i - reduction
		reduction+=1
		indexN.append(i)	
	#print "indexN=",indexN
	
	for i in indexN:
		List[i-1:i+1] = [''.join(List[i-1:i+1])]

	#print "ListNEW=",List
	
	return addInstance(List)


def addInstance(list):
	#print "list=",list
	for e,i in enumerate(list):
		#print "e=",e
		if '{i}' in i:
			#print "i=",i
			i=i.split('{i}')
			list[e]=i[0]+"__INSTANCE__"
			
	return list
	
def getIndexAddInstance(list):
	return getIndex(list)