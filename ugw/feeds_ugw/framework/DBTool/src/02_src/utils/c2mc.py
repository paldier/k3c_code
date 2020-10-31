
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
import genModelControl


#######################################################################
########## Main ####################
#######################################################################

arguments = sys.argv[1:]
try:
	_controlXML = arguments[0]
	_ModelControlXML = arguments[1]

	controlXML = pathCorrect(_controlXML)
	modelControlXML = pathCorrect(_ModelControlXML)


	actualModelFile = os.path.basename(modelControlXML)
	dirName = os.path.dirname(modelControlXML)
	oldModelControlXML = dirName + "/old_" + actualModelFile
	shutil.copy(modelControlXML,oldModelControlXML)

	############################
	genModelControl.doCreateControlModel(controlXML, oldModelControlXML, modelControlXML)

	############################

	print "controlXML: ", controlXML
	print "old Model Control XML: ", oldModelControlXML
	print "New generated Model Control XML: ", modelControlXML
	
	
	
except IndexError:
	print "Usage:"
	print "c2mc.py <service>_control.xml  <service>_control_model_<model_category>.xml"