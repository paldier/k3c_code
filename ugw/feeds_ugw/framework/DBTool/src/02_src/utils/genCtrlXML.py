
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
A single script which invokes all other scripts sequentially, cleanup first and then all transformer functions
'''
import subprocess
from subprocess import *
import sys,os

sys.path.append(r'../common')
from common import *

sys.path.append(r'../transformers/')
sys.path.append(r'../action/')


from pathCorrect import pathCorrect
import shutil
'''
validBBFfiles = ['tr-181-.*-full.xml', 'tr-104-.*-full.xml','tr-098-.*-full.xml']
if base XML is not matching any one of the format, return Error -1, current file is not a valid BBF xml file
'''

class genCtrlXML():

	def __init__(self):
		pass



	def genCtrlXML_block1(self,baseXML,invoker='FrontEnd'):
		print "Received: ",baseXML
		self.inputBBFFile = pathCorrect(baseXML)
		self.XML_NAME = self.inputBBFFile.split('/')[-1]
		
		
		## 1. clean up the XML from the broadband forum
		print "##Entering Stage#1"
		import cleanup
		clean = cleanup.cleanup()
		if (clean.main(pathCorrect(self.inputBBFFile))) != 0:
			print 'Error in Stage: '
			return -1
		else:
			print "Cleanup successful"


		
		## 2. restruct the dataXML.xml file to a readable one.
		print "##Entering Stage#2"
		import restructDataTypeXML
		self.dataType_xml_path = dataTypeXMLPath + self.XML_NAME[:12] + "_dataType.xml"
		ob = restructDataTypeXML.restructDataTypeXML()
		if (ob.main(pathCorrect(self.dataType_xml_path))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "restructDataTypeXML successful\n"



		''' Builds only root node <Device> and the file is transformer1.xml '''
		## 3. transformer1
		print "##Entering Stage#3"
		import transformer1
		self._tr_stripped_xml_path = TR_STRIPPED_XML_PATH + self.XML_NAME[:12] + "-fullStripped.xml"
		t1 = transformer1.transformer1()
		if (t1.main(pathCorrect(self._tr_stripped_xml_path))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer1 successful\n"



		''' Builds service nodes on top of root node from transformer1.xml '''
		## 4. transformer2
		print "##Entering Stage#4"
		import transformer2
		_TRANSFORMER1 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer1.xml"
		t2 = transformer2.transformer2()
		if (t2.main( pathCorrect(self._tr_stripped_xml_path), pathCorrect(_TRANSFORMER1) ))!= 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer2 successful\n"



		''' Completes object hierarcy building on top of transformer2.xml file and generates transformer3.xml '''
		## 5. transformer3
		print "##Entering Stage#5"
		import transformer3
		_TRANSFORMER2 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer2.xml"
		t3 = transformer3.transformer3()
		if (t3.main( pathCorrect(self._tr_stripped_xml_path), pathCorrect(_TRANSFORMER2))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer3 successful\n"



		''' All the parameters are added to services, objects and sub-objects, transformer4.xml is created '''
		## 6. transformer4
		print "##Entering Stage#6"
		import transformer4
		_TRANSFORMER3 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer3.xml"
		t4 = transformer4.transformer4()
		if (t4.main( pathCorrect(self._tr_stripped_xml_path), pathCorrect(_TRANSFORMER3))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer4 successful\n"



		''' adds attributes to object nodes and its child nodes '''
		## 7. transformer5
		print "##Entering Stage#7"
		import transformer5
		_TRANSFORMER4 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer4.xml"
		t5 = transformer5.transformer5()
		if (t5.main( pathCorrect(self._tr_stripped_xml_path), pathCorrect(_TRANSFORMER4))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer5 successful\n"



		''' takes out description node from stripped xml and adds as an attribute to to object and parameter nodes '''
		## 8. transformer6
		print "##Entering Stage#8"
		import transformer6
		_TRANSFORMER5 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer5.xml"
		t6 = transformer6.transformer6()
		if (t6.main( pathCorrect(self._tr_stripped_xml_path), pathCorrect(_TRANSFORMER5)))!=0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer6 successful\n"



		''' syntax nodes in parameter nodes of stripped xml is taken care in this transformer, where syntax nodes becomes attributes. This is one of the highest computing transformer'''
		## 9. transformer7
		print "##Entering Stage#9"
		import transformer7
		_TRANSFORMER6 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer6.xml"
		t7 = transformer7.transformer7()
		if(t7.main( pathCorrect(self._tr_stripped_xml_path), pathCorrect(_TRANSFORMER6), pathCorrect(self.dataType_xml_path))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer7 successful\n"



		''' All objects with tag __INSTANCE__ are replaced by an attribute multiInst="yes" '''
		## 10. transformer8
		print "##Entering Stage#10"
		import transformer8
		_TRANSFORMER7 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer7.xml"
		t8 = transformer8.transformer8()
		if(t8.main( pathCorrect(_TRANSFORMER7))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer8 successful\n"



		''' This script assigns the default value to parameter nodes wherever necessary, default values are available under syntax nodes of parameter '''
		## 11. transformer9
		print "##Entering Stage#11"
		import transformer9
		_TRANSFORMER8 = TRANSFORMER_PATH + self.XML_NAME[:12] + "_transformer8.xml"
		t9 = transformer9.transformer9()
		if(t9.main( pathCorrect(self._tr_stripped_xml_path),  pathCorrect(_TRANSFORMER8))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Transformer9 successful\n"
			return 0
		
		return -1
	###############################################
	###############################################
	
	
	def genCtrlXML_block2(self,baseXML,invoker='FrontEnd'):
		## 12. removeDuplicates.py
		print "##Entering Stage#12"
		import removeDuplicates
		ob = removeDuplicates.removeDuplicates()
		if(ob.main( pathCorrect(self.dataType_xml_path))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "remove Duplicates successful\n"



		## 13. addDynamicAttrib.py - inserts dynamic="true" attributes for 'stats' node and its childNodes
		print "##Entering Stage#13"
		import addDynamicAttrib
		self.TR_FINAL_XML = FINAL_XML_PATH + self.XML_NAME[:12] + "_lq_control.xml"
		ob = addDynamicAttrib.addDynamicAttrib()
		if (ob.main( pathCorrect(self.TR_FINAL_XML))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Adding Dynamic attributes successful\n"



		## 14. addPasswordAttribute.py
		print "##Entering Stage#14"
		import addPasswordAttribute
		ob = addPasswordAttribute.addPasswordAttribute()
		if(ob.main(self.TR_FINAL_XML))!=0:
			print "Error in Stage: "
			return -1
		else:
			print "password='y' successfully added"



		## 15. removeStatusDeprecated.py - removes the elements with attribute status="deprecated"
		print "##Entering Stage#15"
		import removeStatusDeprecated
		ob = removeStatusDeprecated.removeStatusDeprecated()
		if(ob.main( pathCorrect(self.TR_FINAL_XML))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "removed objects with attribute status='deprecated' successful\n"
		
		


		## 16. addProfileAttrib.py - adds profile attribute to main control xml file
		print "##Entering Stage#16"
		import addProfileAttrib
		PROFILE_XML_PATH = profile_path + self.XML_NAME[:12] + '_fullProfile.xml'
		ob = addProfileAttrib.addProfileAttrib()
		if(ob.main( pathCorrect(self.TR_FINAL_XML),pathCorrect(PROFILE_XML_PATH))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Profile attributes are added successfully\n"
		


		## 17. removeServices.py - removes services specified in the skipList
		print "##Entering Stage#17"
		import removeServices
		ob = removeServices.removeServices()
		if(ob.main( pathCorrect(self.TR_FINAL_XML))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Specific services are removed\n"



		## 18. removeAttributes.py - removes attributes from all the element object
		print "##Entering Stage#18"
		import removeAttributes
		ob = removeAttributes.removeAttributes()
		if(ob.main( pathCorrect(self.TR_FINAL_XML))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Unwanted Attributes removed\n" 



		## 19. cleanXMLFile.py - This XML clean up the un-necessary spaces in the xml file (applied for control XML)
		import cleanXMLFile
		print "##Entering Stage#19"
		ob = cleanXMLFile.cleanXMLFile()
		if(ob.main( pathCorrect(self.TR_FINAL_XML))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Cleaned tr-control XML file, returning successfully\n"



		## 20. restructureControlXML.py - this brings parameter nodes above and child nodes below of a node
		print "##Entering Stage#20"
		retVal = subprocess.call(['python',basePath + r'/02_src/action/restructureControlXML.py', self.TR_FINAL_XML])
			
		if (retVal !=0):
			print "Error in return from restructureControlXML: ",retVal
			return -1
		else:
			print "restructureControlXML processed Successfully"
		




		## 21. serviceSplitter.py - create separate service XML files
		print "##Entering Stage#21"
		import serviceSplitter
		ob = serviceSplitter.serviceSplitter()
		if(ob.main( pathCorrect(self.TR_FINAL_XML))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Created separate service XML files\n"
		


		## 22. insertParamNodePPPoA.py - There is a special case in the service PPP_control.xml file, that addresses this issue
		import insertParamNodePPPoA
		print "##Entering Stage#22"
		PPPoAFile = xml_services_path + "PPP_control.xml"
		ob = insertParamNodePPPoA.insertParamNodePPPoA()
		if (ob.main(PPPoAFile) != 0):
			print "Error in stage: "
			return -1
		else:
			print "Successful"
			return 0
			
		return -1
	###############################################
	###############################################
		
	
	
	
	def genCtrlXML_block3(self,baseXML,invoker='FrontEnd'):
		## 24. restructureControlXML.py - this brings parameter nodes above and child nodes below of a node (this we are applying only to service level nodes since the changes happened in the control XML is not reflected in the service level xml files)
		

		print "##Entering Stage#23"
		# dev-dev comment: had to do a sub-process invoke since Jython does not support - lxml.
		# Do not mess with this section!
		#scriptRestCXMLPath = basePath + r'/02_src/action/restructureControlXML.py'
		filesPath = xml_services_path+'*'
		retValRestr = subprocess.call(['python',basePath + r'/02_src/action/restructureControlXML.py', filesPath ])
		if retValRestr != 0:
			print "Error in return from restructureControlXML: ",retValRestr
			return -1
		else:
			print "restructureControlXML processed Successfully"



		## 24. createServiceXML.py - fuses multiple service XML files to a single service XML file. This information is hard-coded in a configuration file mergeControlXMLs.conf
		import createServiceXML
		print "##Entering Stage#24"
		ob = createServiceXML.createServiceXML()
		if(ob.main()) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Merged Service XML created\n"



		## 25. cleanXMLFile.py - This XML clean up the un-necessary spaces in the xml file (applied for dataType.xml)
		import cleanXMLFile
		print "##Entering Stage#25"
		ob = cleanXMLFile.cleanXMLFile()
		if(ob.main(pathCorrect(self.dataType_xml_path))) != 0:
			print "Error in stage: "
			return -1
		else:
			print "Cleaned dataType XML file, returning successfully\n"



		## 26. Add comment tag (or watermark) to the full-control XML file, addComment.py
		print "##Entering Stage#26"
		import addWaterMark
		handler = addWaterMark.waterMark()
		for root, dirs, files in os.walk( pathCorrect(xml_services_path), topdown=False):
			for name in files:
				filePath = (os.path.join(root, name))
				if filePath.endswith("_control.xml"):
					print filePath
					if (handler.addWaterMark(filePath,"schema",'ReadWrite')) != 0:
						print "Error in addWaterMark: ", filePath
					else:
						print "Successfully Watermarked file: ", filePath
		
		if (handler.addWaterMark(self.TR_FINAL_XML,"schema")) !=0:
			print "Error in addWaterMark: ",self.TR_FINAL_XML


		##27. Script addBasicDataType.py will add new basic dataTypes to dataType XML and reformat it.
		print "Entering Stage#27"
		import addBasicDataType
		ob = addBasicDataType.addBasicDataType()
		if (ob.main(self.TR_FINAL_XML,self.dataType_xml_path)) != 0:
			print "Error in adding BasicDataType"
		else:
			print "Successfully added basic data types to file: ",self.dataType_xml_path
			return 0
			
		return -1
	###############################################
	###############################################	
	
	###############################################
	###############################################	
	def genCtrlXML(self,baseXML,invoker='FrontEnd'):
		if(0 == self.genCtrlXML_block1(baseXML, invoker) ):
			if(0 == self.genCtrlXML_block2(baseXML, invoker) ):
				if(0 == self.genCtrlXML_block3(baseXML, invoker) ):
					return 0

		#Always the end
		print "Error in genCtrlXML"
		return -1
	###############################################
	###############################################
		
		
try:
	inputFile = sys.argv[1]
	invoker = sys.argv[2]
	obj = genCtrlXML()
	obj.genCtrlXML(inputFile,invoker)
except IndexError:
	print "No file passed"