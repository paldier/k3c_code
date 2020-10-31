
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
variables common to multiple python modules
'''
import os,sys
import platform
sys.path.append('../action')

from pathCorrect import pathCorrect

_currentPath = os.path.dirname(os.path.abspath(__file__))
currentPath = pathCorrect(_currentPath)
currentPathList = currentPath.split('/')[:-2]
basePath = '/'.join(currentPathList)
print "basePath=",basePath

## Excel path after X2X conversion
excel_path = basePath + r'/01_db_files/excel_files/02_developer/'

## Intermediate path
cleaned_xml_path = basePath + r'/01_db_files/xml_files/03_Intermediates/'

## each profile in the TR-181/TR-104 XML
profile_path = basePath + r'/01_db_files/xml_files/03_Intermediates/profiles/'

## TR-181 stripped xml path
TR_STRIPPED_XML_PATH = basePath + r'/01_db_files/xml_files/03_Intermediates/'

## dataType.xml path
dataTypeXMLPath = basePath + r'/01_db_files/xml_files/03_Intermediates/dataTypes/'

## transformer path
TRANSFORMER_PATH = basePath + r'/01_db_files/xml_files/03_Intermediates/transformers/'

## xml services path
xml_services_path = basePath + r'/01_db_files/xml_files/02_developer/service_level/'

## xml control path
xml_control_path = basePath + r'/01_db_files/xml_files/02_developer/'

## final xml path
FINAL_XML_PATH = basePath + r'/01_db_files/xml_files/02_developer/'

## excel services path
excel_services_path = basePath + r'/01_db_files/excel_files/02_developer/service_level/'

## Skiplist file
skipListFile = basePath + '/configs/skipList.properties'

## Merge services file
mergeServicesFile = basePath + r'/configs/mergeServices.properties'

## Watermark file
waterMarkFile = basePath + r'/configs/waterMark.properties'

## waterMark file type
waterMarkFileType = basePath +  r'/configs/waterMarkFileTypes.properties'

## password addition file
passwordFile = basePath + r'/configs/addPsswdAttrib.properties'