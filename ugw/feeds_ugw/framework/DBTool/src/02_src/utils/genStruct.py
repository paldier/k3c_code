#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import xml.etree.ElementTree as ET
import os
import sys
from lxml import etree
import argparse
import textwrap
struct_dict = []
map_dict = []
enum_dict = []
sep_enum_dict = []
obj_dict = []
enumStr = ""
enumMapStr = ""
deflen = "64"
logFileName = "./genStruct_logs"
outDir = "./out_hdr"

def getFullPathDot(node):
    sDefine = '' + node.tag
    while node.getparent() != None:
        sDefine = node.getparent().tag + '.' + sDefine
        node = node.getparent()
    return sDefine

def get_full_path(node, exclNode):
    """
        Function for fully qualifying a node name for header files.
    """
    if exclNode == 1:
        lnode = node.getparent()
        if lnode == None:
            return node.tag
    else:
        lnode = node

    sDefine = '' + lnode.tag
    while lnode.getparent() != None and lnode.getparent().tag != 'Device':
        sDefine = lnode.getparent().tag + '_' + sDefine
        lnode = lnode.getparent()
    return sDefine

def change_path(inputFile, destPath):
    """
        Changes abc/def.xml --> xyz/def.xml  (if 'destPath' = xyz)
    """
    return destPath + '/' + inputFile.split('/')[-1]

def add_to_struct_dict(name, val):
    index1 = 0
    found1 = 0
    global struct_dict
    while (index1 < len(struct_dict)):
        if name == struct_dict[index1]["name"]:
            found1 = 1
            break
        index1 = index1 + 1
    if found1 == 1:
        # modify entry at index1
        if struct_dict[index1]["strval"] != val:
            struct_dict[index1]["strval"] = struct_dict[index1]["strval"] + val + "\n"
    else:
        # add entry at end
        struct_dict.append({"name":name, "strval":val})

def add_to_map_dict(name, val):
    index1 = 0
    found1 = 0
    global map_dict
    while (index1 < len(map_dict)):
        if name == map_dict[index1]["name"]:
            found1 = 1
            break
        index1 = index1 + 1
    if found1 == 1:
        # modify entry at index1
        if map_dict[index1]["strval"] != val:
            map_dict[index1]["strval"] = map_dict[index1]["strval"] + val + "\n"
    else:
        # add entry at end
        map_dict.append({"name":name, "strval":val})

def add_to_enum_dict(name, val, flag):
    index1 = 0
    found1 = 0
    global enum_dict
    while (index1 < len(enum_dict)):
        if name == enum_dict[index1]["name"]:
            if flag == 1:
                # request is to add existing enum, return
                return 1
            found1 = 1
            break
        index1 = index1 + 1
    if found1 == 1:
        # modify entry at index1
        enum_dict[index1]["strval"] = enum_dict[index1]["strval"] + val + "\n"
    else:
        # add entry at end
        enum_dict.append({"name":name, "strval":val})
    return 0

def add_to_sep_enum_dict(name, val, flag):
    index1 = 0
    found1 = 0
    global sep_enum_dict
    while (index1 < len(sep_enum_dict)):
        if name == sep_enum_dict[index1]["name"]:
            if flag == 1:
                # request is to add existing enum, return
                return 1
            found1 = 1
            break
        index1 = index1 + 1
    if found1 == 1:
        # modify entry at index1
        sep_enum_dict[index1]["strval"] = sep_enum_dict[index1]["strval"] + val + "\n"
    else:
        # add entry at end
        sep_enum_dict.append({"name":name, "strval":val})
    return 0

def add_to_obj_dict(name, val):
    index1 = 0
    found1 = 0
    global obj_dict
    while (index1 < len(obj_dict)):
        if name == obj_dict[index1]["name"]:
            found1 = 1
            break
        index1 = index1 + 1
    if found1 == 0:
        # add entry at end
        obj_dict.append({"name":name, "strval":val})
    # else: entry already found, no need to update

def write_xml(tree, dest):
    """
    Takes an xml tree, adds ids to it and creates an xml with name 'dest'. E.g: write_xml(tree, 'abc/def.xml')
    """
    tree.write(dest, pretty_print = True)

# function that will read out all tags defined
# and convert into C style datat structures
# pre-requisites :
# 1. XML file is Id generated (i.e, output of genHdr.py)
# 2. flags for each object/parameter to have proper date type
def generate_c_structs(xml, outPath):
    """
    Takes input xml and creates header file with same name in specified path.
    E.g. generate_c_structs('abc/hello.xml', 'xyz/') --> Creates a hello.h in xyz/ folder
    """
    global enumStr
    global enumMapStr
    #changes file name abc/def.xml to generated_header_flies/def.h
    tree = etree.parse(xml)
    root = tree.getroot()
    rootTagVal = root.tag
    objCnt = 1
    objMacroDef = ""

    # add entry in dictionary for Device object, which
    # is not going to be done by loop below.
    strval = "typedef struct Device {\n"
    add_to_struct_dict("Device", strval)

    for child in root:
        parent=""
        for node in child.iter():
            objId = ""
            tagval = ""
            fAddToDict = 1
            parent = node.getparent()
    
            # handle start of new object
            if node.get('minEntries') != None and node.get('minEntries') > 0:
                fAddToDict = 0
                # for struct generation
                parentObj = get_full_path(node, 1)
                runningObj = get_full_path(node, 0)
                runningObjDot = getFullPathDot(node)
    
                strval = "typedef struct " + runningObj + "{\n"
                add_to_struct_dict(runningObj, strval)
    
                # add this structure as parameter inside parent structure
                strvalTmp = "\t" + runningObj + "_t " + "p" + runningObj + ";"
                add_to_struct_dict(parentObj, strvalTmp)
    
                # for object name to id mapping
                runningPar = "DEVICE_" + runningObj.upper()
                add_to_obj_dict(runningObjDot, runningPar)
                # generate Id
                objMacroDef = objMacroDef + "#define " + runningPar + " " + str(objCnt) + "\n"
                objCnt = objCnt + 1
    
            else:
                # parameter of an object -> generate macro
                paramMac = runningPar + "_" + node.tag.upper()
    
            # handle parameters of an object already identified
            if int(node.get('Flag'),16) & 0x000004:
                if (node.get('enum') != None) and (len(node.attrib['enum']) > 0):
                    # enum
                    enumName = runningObj + node.tag
                    enumCnt = 0
                    if not add_to_enum_dict(enumName, "typedef enum " + enumName + " {\n", 1):
                        enumStr = enumStr + "typedef enum " + enumName + " {\n"
                        enumMapStr = enumMapStr + "name_value_t " + enumName + "_nv [] = { \n"
                        for enumVal in node.attrib['enum'].split(','):
                            if len(enumVal):
                                sCap = node.tag.upper() + "_" + enumVal.upper()
                                #replace chars like ' ', '-', '.', '+' in sCap
                                sCap = sCap.replace(" ", "_").replace("-", "_").replace(".", "_").replace("+", "PLUS")
                                enumStr = enumStr + "\t" + runningObj.upper() + "_" + sCap + ",\n"
                                enumMapStr = enumMapStr + "\t{\"" + enumVal + "\", " + runningObj.upper() + "_" + sCap + "},\n"
                                enumCnt = enumCnt + 1
                        enumStr = enumStr + "} " + enumName + "_t;\n\n"
                        add_to_sep_enum_dict(enumName, str(enumCnt), 1)
                        enumMapStr = enumMapStr + "};\n\n"
                    tagval = "\t" + enumName + "_t e" + node.tag + ";"
                else:
                    # standard string
                    # if required param is not found value will be None,
                    # using it as is for strcat below (thru +) gives error
                    leng = node.get("maxLength")
                    if leng == None:
                        leng = deflen
                    tag = node.tag
                    if tag == None:
                        tag = ""
                    tagval = "\t" + "char s" + tag + "[" + leng + "];"
            elif int(node.get('Flag'),16) & 0x000002:
                # unsigned integer
                tagval = "\t" + "unsigned int un" + node.tag + ";"
            elif ((int(node.get('Flag'),16) & 0x000001) or
                    (int(node.get('Flag'),16) & 0x0000020)):
                # integer
                tagval = "\t" + "int n" + node.tag + ";"
            elif (int(node.get('Flag'),16) & 0x000010):
                # boolean
                tagval = "\t" + "bool b" + node.tag + ";"
            elif (int(node.get('Flag'),16) & 0x000040):
                # long
                tagval = "\t" + "long l" + node.tag + ";"
            elif (int(node.get('Flag'),16) & 0x000080):
                # unsigned long
                tagval = "\t" + "unsigned long ul" + node.tag + ";"
            elif (int(node.get('Flag'),16) & 0x000100):
                # datetime
                tagval = "\t" + "struct timeval x" + node.tag + ";"
    
    
            if fAddToDict == 1:
                # for struct generation
                add_to_struct_dict(runningObj, tagval)
    
                # for parameter name to id mapping
                objMacroDef = objMacroDef + "#define " + paramMac + " " + str(objCnt) + "\n"
                objCnt = objCnt + 1

    print "Generating C structure for each object\t\t :",
    sHeaderName = outPath + xml.split("/")[-1].replace('.xml', '.h')
    fHeader = open(sHeaderName, 'w')
    if len(objMacroDef) > 0:
        fHeader.write(objMacroDef)
    fHeader.write("\n")
    # commented objs list print to header file, as macros generated from
    # objs list might conflict with macros generated by genHdr.py
    i = 0
    fHeader.write("name_value_t x_Objs_nv[] =  { \n")
    while(i < len(obj_dict)):
        fHeader.write("\t{\"" + obj_dict[i]["name"] + "\", " + obj_dict[i]["strval"] + "},\n")
        i = i + 1
    fHeader.write("};\n\n")

    # write enums, if available
    if len(enumStr) > 0:
        fHeader.write(enumStr)

    # write enum map, if available
    if len(enumMapStr) > 0:
        fHeader.write(enumMapStr)

    # read each item (struct) from dictionary and write to file
    i = len(struct_dict) - 1
    while (i >= 0):
        fHeader.write(struct_dict[i]["strval"])
        fHeader.write("\tParamMap_t xParamMap;\n")
        fHeader.write("} " + struct_dict[i]["name"] + "_t;\n\n")
        i = i - 1

    fHeader.close()
    print "Done [filename : " + sHeaderName + "]"

# function that will read out all tags defined
# and convert into C style datat structures
# pre-requisites :
# 1. XML file is Id generated (i.e, output of genHdr.py)
# 2. flags for each object/parameter to have proper date type
def generate_param_offset_map(xml, outPath):
    """
    Takes input xml and creates header file with same name in specified path.
    E.g. generate_c_structs('abc/hello.xml', 'xyz/') --> Creates a hello.h in xyz/ folder
    """
    global enumStr
    global enumMapStr
    #changes file name abc/def.xml to generated_header_flies/def.h
    tree = etree.parse(xml)
    root = tree.getroot()
    rootTagVal = root.tag
    objCnt = 1
    objMacroDef = ""

    # add entry in dictionary for Device object, which
    # is not going to be done by loop below.
    strval = "x_LTQ_Param x_LTQ_Device_param [] = {\n"
    add_to_map_dict("x_LTQ_Device", strval)
    logFile = file(logFileName, "w")

    for child in root:
        parent=""
        for node in child.iter():
            objId = ""
            fAddToDict = 1
            parent = node.getparent()
    
            # handle start of new object
            if node.get('minEntries') != None and node.get('minEntries') > 0:
                fAddToDict = 0
    
                # for parameter to offset mapping
                parentObj = get_full_path(node, 1)
                # we have encountered start of new object. store its full path
                # from XML tree into runningObj, which serves as ref for all parameters 
                # of thisobject further.
                runningObj = get_full_path(node, 0)
                runningObjDot = getFullPathDot(node)
    
                map_strname = "x_LTQ_" + runningObj
                map_strval = "x_LTQ_Param " + map_strname + "_param [] = {\n"
                add_to_map_dict(map_strname, map_strval)
    
                strname = runningObj + "_t"
    
                # add mapping inside parent
                strvalTmp = "\t{\"" + node.tag + "\", DEVICE_" + runningObj.upper() + ", LTQ_PARAM_TYPE_STRUCT,\n\toffsetof(" + parentObj + "_t, p" + runningObj + "), NULL, 0},"
                add_to_map_dict("x_LTQ_" + parentObj, strvalTmp)
    
                # for object name to id mapping
                runningPar = "DEVICE_" + runningObj.upper()
                add_to_obj_dict(runningObjDot, runningPar)
    
                # generate Id
                objMacroDef = objMacroDef + "#define " + runningPar + " " + str(objCnt) + "\n"
                objCnt = objCnt + 1
    
            else:
                # parameter of an object -> generate macro
                paramMac = runningPar + "_" + node.tag.upper()
    
            # handle parameters of an object already identified
            if int(node.get('Flag'),16) & 0x000004:
                if (node.get('enum') != None) and (len(node.attrib['enum']) > 0):
                    # enum
                        # for parameter to offset mapping
                    index1 = 0
                    enumCnt = ""
                    global sep_enum_dict
                    enumKey = runningObj + node.tag
                    while (index1 < len(sep_enum_dict)):
                        if enumKey == sep_enum_dict[index1]["name"]:
                            enumCnt = sep_enum_dict[index1]["strval"]
                            break
                        index1 = index1 + 1
                    map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_ENUM,\n\toffsetof(" + strname + ", e" + node.tag + "), " + runningObj + node.tag + "_nv, " + enumCnt + "},"
                else:
                    # standard string
                    # if required param is not found value will be None,
                    # using it as is for strcat below (thru +) gives error
                    leng = node.get("maxLength")
                    if leng == None:
                        leng = deflen
                    tag = node.tag
                    if tag == None:
                        tag = ""
                    map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_STR,\n\toffsetof(" + strname + ", s" + node.tag + "), NULL, 0},"
            elif int(node.get('Flag'),16) & 0x000002:
                # unsigned integer
                map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_UINT,\n\toffsetof(" + strname + ", un" + node.tag + "), NULL, 0},"
            elif ((int(node.get('Flag'),16) & 0x000001) or
                    (int(node.get('Flag'),16) & 0x0000020)):
                # integer
                map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_INT,\n\toffsetof(" + strname + ", n" + node.tag + "), NULL, 0},"
            elif (int(node.get('Flag'),16) & 0x000010):
                # boolean
                map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_BOOL,\n\toffsetof(" + strname + ", b" + node.tag + "), NULL, 0},"
            elif (int(node.get('Flag'),16) & 0x000040):
                # long
                map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_LONG,\n\toffsetof(" + strname + ", l" + node.tag + "), NULL, 0},"
            elif (int(node.get('Flag'),16) & 0x000080):
                # unsigned long
                map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_ULONG,\n\toffsetof(" + strname + ", ul" + node.tag + "), NULL, 0},"
            elif (int(node.get('Flag'),16) & 0x000100):
                # datetime
                map_strval = "\t{\"" + node.tag + "\", " + paramMac + ", LTQ_PARAM_TYPE_DATETIME,\n\toffsetof(" + strname + ", x" + node.tag + "), NULL, 0},"
            else:
                sys.stdout = logFile
                print("Data type can't be resolved for parameter " + runningObjDot + "." + node.tag)
                sys.stdout = sys.__stdout__
    
            if fAddToDict == 1:
                # for parameter to offset mapping
                map_strname = "x_LTQ_" + runningObj
                add_to_map_dict(map_strname, map_strval)
    
                # for parameter name to id mapping
                objMacroDef = objMacroDef + "#define " + paramMac + " " + str(objCnt) + "\n"
                objCnt = objCnt + 1

    print "Generating parameter mapping for each structure\t :",
    sHeaderName = outPath + xml.split("/")[-1].replace('.xml', '_map.h')
    fHeader = open(sHeaderName, 'w')

    i = 0
    while (i < len(map_dict)):
        fHeader.write(map_dict[i]["strval"])
        # map_dict[i]["name"] is of syntax x_LTQ_actualstructname
        # skip x_LTQ_ to get to actual structure name to add
        # offset entry for parammap parameter
        fHeader.write("\t{\"xParamMap\", 0, LTQ_PARAM_TYPE_UINT,\n\toffsetof(" + map_dict[i]["name"][6:] +"_t, xParamMap), NULL, 0},\n")
        fHeader.write("};\n\n")
        i = i + 1

    fHeader.close()
    print "Done [filename : " + sHeaderName + "]"

# starting point of program execution ---->
#parse commandline arguments and options
parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter, description=textwrap.dedent('''\
                            Usage Examples
                    --------------------------------
                    python genStruct.py <ss_control.xml>

                    NOTE: By default, output files are placed in folder with name ''' + outDir))


parser.add_argument('input_files', nargs='+', help='All input xml files')
args = parser.parse_args()

#create a new directory to place generated header files
if not os.path.exists(outDir):
    os.makedirs(outDir)

parser = etree.XMLParser(remove_blank_text=True) # lxml.etree only!

for arg in args.input_files:
    generate_c_structs(arg, outDir + "/") #use given xml & generate <xml_name>.h files in 'out_hdr' folder
    generate_param_offset_map(arg, outDir + "/") #use given xml & generate <xml_name>_map.h fie in 'out_hdr' folder
    print("\nPlease check file " + logFileName + " for errors.")

