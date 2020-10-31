#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import os,sys
sys.path.append(r'../action/')
from pathCorrect import pathCorrect
import shutil
import genModelData
import chromia


def printErrorMessage():
	print "Usage:"
	print "mc2md.py    <service>_control_<model_category>.xml     <service>_data_<model_category>.xml       <service>_data.xml -autoDelInst=true"
	print "mc2md.py    <service>_control_<model_category>.xml     <service>_data_<model_category>.xml       <service>_data.xml -autoDelInst=false"
	sys.exit(0)


#######################################################################
########## Main ####################
#######################################################################

arguments = sys.argv[1:]

try:
	_dataXML = arguments[2]
	_modelDataXML = arguments[1]
	_modelControlXML =  arguments[0]
	autoDelInstParameter = 'true'
	
	
	dataXML = pathCorrect(_dataXML)
	modelDataXML = pathCorrect(_modelDataXML)
	modelControlXML = pathCorrect(_modelControlXML)
	
	
	''' check if  <service>_data.xml exists '''
	if not(os.path.lexists(dataXML)):
		print "Error: ", dataXML, " does not exist!"
		sys.exit(0)
	
	
	''' Check if the <service>_data_<model_category>.xml exists, if it doesn't create a file with that name '''
	instChromia = chromia.chromia()
	dataText = '<Device/>'
	
	
	## file don't exists, needs to be created
	if not(os.path.lexists(modelDataXML)):
		print "Error: ", modelDataXML, " does not exist! and will be created"
		instChromia.writeNSafeClose(modelDataXML,dataText)
		
		actualFile = os.path.basename(modelDataXML)
		dirName = os.path.dirname(modelDataXML)
		oldModelDataXML = dirName + "/old_" + actualFile
		
		shutil.copy(modelDataXML,oldModelDataXML)

	## file exists and needs to back up
	else:
		actualFile = os.path.basename(modelDataXML)
		dirName = os.path.dirname(modelDataXML)
		oldModelDataXML = dirName + "/old_" + actualFile
		
		shutil.copy(modelDataXML,oldModelDataXML)
		instChromia.writeNSafeClose(modelDataXML,dataText)
	
	
	try:
		if '-autoDelInst=' in arguments[3]:
			optParams = autoDelInstParameter.split('=')

			if optParams[1] == 'false':
				autoDelInstParameter = 'false'
			
	except IndexError:
		pass


	############################
	genModelData.doCreateDataModel(dataXML, oldModelDataXML, modelControlXML, modelDataXML,autoDelInstParameter)
	############################

	print "dataXML: ", dataXML
	print "modelControlXML: ",modelControlXML
	print "modelDataXML: ", modelDataXML
	
	''' Remove the old files '''
	#os.remove(oldModelDataXML)

except IndexError:
	printErrorMessage()