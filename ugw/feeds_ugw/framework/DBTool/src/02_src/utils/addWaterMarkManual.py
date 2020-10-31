
#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import sys
import os

sys.path.append(r'../action/')
import addWaterMark
from pathCorrect import pathCorrect

def WaterMarkManual(xmlFile,fileType,accessType='ReadOnly'):
    addWaterMarkInst = addWaterMark.waterMark()
    
    if ((fileType in addWaterMarkInst.wmFileTypeList) and (accessType in addWaterMarkInst.wmAccessTypeList)):
        if (addWaterMarkInst.isWaterMarked(xmlFile)):
            addWaterMarkInst.modifyWaterMark(xmlFile,fileType,accessType)
        else:
            addWaterMarkInst.addWaterMark(xmlFile,fileType,accessType)
            
    else:
        print "##"*60
        print "Error: Invalid fileType/AccessType"
        print "FileType: ['instance','schema','instance_id','schema_id']"
        print "AccessType: ",addWaterMarkInst.wmAccessTypeList
        print "##"*60


'''
*****************************
    MAIN CODE STARTS HERE
*****************************
'''
if __name__ == '__main__':
    arguments = sys.argv[1:]
    
    if len(arguments) >= 2:
        _xmlFile = arguments[0]
        fileType = arguments[1]
        xmlFile = pathCorrect(_xmlFile)
        
        if len(arguments) == 2:
            WaterMarkManual(xmlFile,fileType)
    
        elif len(arguments) == 3:
            accessType = arguments[2]
            WaterMarkManual(xmlFile,fileType,accessType)
    
    else:
        print "Invalid arguments passed\n"
        print "Usage: "
        print "addWaterMarkManual.py <xml-file> <file-type> [access-type]"
        print "default accessType : ReadOnly"