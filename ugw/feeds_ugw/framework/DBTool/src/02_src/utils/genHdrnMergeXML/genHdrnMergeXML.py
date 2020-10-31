#! /opt/TWWfsw/bin/python

###################################################################################
#        Copyright (c) 2014 
#
#        Lantiq Beteiligungs-GmbH & Co. KG 
#
#        Lilienthalstrasse 15, 85579 Neubiberg, Germany  
#
#        For licensing information, see the file 'LICENSE' in the root folder of 
#
#        this software module.
###################################################################################


""" Generate Header file, id added control and data xmls

This utility is used to perform various manipulations to contol and data xmls to generate
ID added,encoded contol and data xmls

Script performs following actions

1. Add ID to contol and data xmls. In case of data xml, it syncs id with ctrl xml
2. Encode certain attributes to 'Flag' attribute
3. Generate enumarations by referring to tr-181 datatype xml
4. Generate header file for contol xmls
5. Regenerate original xmls from id added, encoded control and data xmls

EXTERANL PYTHON MODULES REQURED:
    1. lxml

HOW TO USE:
    GENERATING ID ADDED CONTROL AND DATA XMLs:

        1. Copy your control and data xml to any other location and go to
           the location where script exists
    
            CASE 1: If both control and data xml exists in the same location,
                    below command will generate id added control and data
                    xmls in the current location in a single shot
            
                    python genHdrnMergeXML -a default <Path to your control XML>

            CASE 2: If data xml doesn't exist exist you need to generate
                    id added control and data xmls separately

                    python genHdrnMergeXML -c default <Path to your control XML>
                    python genHdrnMergeXML -d <Path to your id added control xml from above step>/default <Path to your data XML>


    REVERTING BACK TO NON ID ADDED XMLs:

            python genHdrnMergeXML -p default <Path to your id added control XML>
            python genHdrnMergeXML -p default <Path to your id added data  XML>
    
    
    MORE INFO: 
        python genHdrnMergeXML.py --help
"""

import os
import sys
from lxml import etree
import argparse
import textwrap
import shutil
import errno
import subprocess

sys.path.append(r'../../action/')
from pathCorrect import pathCorrect

sys.path.append(r'../../common')
from common import *

sys.path.append('../')
from chromia import *

sys.path.append('../../action/')
import addWaterMark

TR181_FILEPATH = "../../../01_db_files/xml_files/03_Intermediates/dataTypes/tr-181-2-9-0_dataType.xml"
AUTO_DATAIDGEN = False;

def change_node_name(tree):
    """
    Renames node names as either 'Object' or 'Parameter' to make
    validation with xsd simple

    ARGS:
        INOUT: tree -> xml tree
    """
    root = tree.getroot()
    for child in root:
        for node in child.iter():
            #raise exception if node name length is greater than 64
            if len(node.tag) > 64:
                raise Exception('Name '+ node + 'exceeds max length')
            #if no. of children is 0, that is a parameter node
            if len(node) ==  0:
                node.tag = "Parameter"
            else:
                node.tag = "Object"
    tree.write('./modified.xml', pretty_print=True)
    return tree

def preprocess_xml(tree):
    """
    Some nodes has 'syntax' = 'DatatypeRef' which marks the indirect reference to actual
    types such as string, int, etc. This type of inconsisteny should be
    removed before encoding 
    
    Changes the 'syntax' attribute values with 'DatatypeRef' to their actual
    type as per below mapping
    
    Which kind of datatype reference is marked my 'ref' attribute

    MAPPING:
            "IPAddress":{'syntax': 'string', 'maxLength': '45'},
            "IPv4Address":{'syntax': 'string', 'maxLength': '45'},
            "IPv6Address":{'stntax': 'string', 'maxLength': '45'},
            "IPPrefix":{'syntax': 'string', 'maxLength': '49'},
            "IPv4Prefix":{'syntax': 'string', 'maxLength': '18'},
            "IPv6Prefix":{'syntax': 'string', 'maxLength': '49'},
            "MACAddress":{'syntax': 'string', 'maxLength': '17', 'value':'([0-9A-Fa-f][0-9A-Fa-f]:){5}([0-9A-Fa-f][0-9A-Fa-f])'},
            "StatsCounter32":{'stntax':'unsignedInt'},
            "StatsCounter64":{'syntax':'unsignedLong'},
            "Alias":{'syntax': 'string', 'maxLength':'64'},
            "Dbm1000": {'syntax': 'int'},
            "UUID": {'syntax': 'string', 'minLength': '36', 'maxLength':'36', 'value': '[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}'},
            "IEEE_EUI64": {'syntax': 'string', 'maxLength':'26', 'value':'([0-9A-Fa-f][0-9A-Fa-f]:){7}([0-9A-Fa-f][0-9A-Fa-f])'},
            "ZigBeeNetworkAddress": {'syntax': 'string', 'maxLength':'4', 'value':'([0-9A-Fa-f]){4}'}

    ARGS:
        INOUT: tree -> xml tree
    """

    syntax_map = {
            "IPAddress":{'syntax': 'string', 'maxLength': '45'}, \
            "IPv4Address":{'syntax': 'string', 'maxLength': '45'}, \
            "IPv6Address":{'stntax': 'string', 'maxLength': '45'}, \
            "IPPrefix":{'syntax': 'string', 'maxLength': '49'}, \
            "IPv4Prefix":{'syntax': 'string', 'maxLength': '18'}, \
            "IPv6Prefix":{'syntax': 'string', 'maxLength': '49'}, \
            "MACAddress":{'syntax': 'string', 'maxLength': '17', 'value':'([0-9A-Fa-f][0-9A-Fa-f]:){5}([0-9A-Fa-f][0-9A-Fa-f])'}, \
            "StatsCounter32":{'stntax':'unsignedInt'}, \
            "StatsCounter64":{'syntax':'unsignedLong'}, \
            "Alias":{'syntax': 'string', 'maxLength':'64'}, \
            "Dbm1000": {'syntax': 'int'}, \
            "UUID": {'syntax': 'string', 'minLength': '36', 'maxLength':'36', 'value': '[A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}'}, \
            "IEEE_EUI64": {'syntax': 'string', 'maxLength':'26', 'value':'([0-9A-Fa-f][0-9A-Fa-f]:){7}([0-9A-Fa-f][0-9A-Fa-f])'}, \
            "ZigBeeNetworkAddress": {'syntax': 'string', 'maxLength':'4', 'value':'([0-9A-Fa-f]){4}'} \
    }

    for node in tree.iter():
        #if syntax is 'dataTypeRef', Check the 'ref' attribute value
        #to know its type
        if node.get('syntax') == 'dataTypeRef':
            data_type = node.get('ref')
            if data_type in syntax_map:
                attributes_values_map = syntax_map[data_type]
                #Get attributes for current type
                for attribute in attributes_values_map:
                    node.attrib[attribute] = attributes_values_map[attribute]

                try:
                    del node.attrib['ref']
                except:
                    pass
    #tree.write('./preprocessed.xml', pretty_print = True)
    return tree
                
def preprocess_for_validation(tree):
    '''
    1. Renames node names as either 'Object' or 'Parameter' to make
    validation with xsd simple
    
    2. changes syntax attribute to 'xsi:type' attribute for value validation

    3. remove all other attribtues except type

    tree(IN) -> XML Tree for which you want to change node names
    '''
    XSI = "http://www.w3.org/2001/XMLSchema-instance"
    XSD = "http://www.w3.org/2001/XMLSchema"

    root = tree.getroot()
    for child in root:
        for node in child.iter():
            if 'syntax' in node.attrib:
                node.attrib[etree.QName(XSI, 'type')] = etree.QName(XSD, node.attrib['syntax'])
                pass
            # -- Unable to use xsd:maxLength directly in xml to restrict string length
            #if 'maxLength' in node.attrib:
            #    node.attrib[etree.QName(XSD, 'maxLength')] = node.attrib['maxLength']

            #if no. of children is 0, that is a parameter node
            if len(node) ==  0:
                node.tag = "Parameter"
            else:
                node.tag = "Object"

            #remove all non-namespace attributes
            for attrib in node.attrib:
                if (XSI not in attrib) and (XSD not in attrib):
                    try:
                        del node.attrib[attrib]
                    except:
                        pass
    tree.write('./modified.xml', pretty_print=True)
    return tree

def validate_with_xsd(xml, xsd):
    """
    Validates xml against xsd

    ARGS:
        IN:
            xml -> Input xml which is to be validated
            xsd -> Input xsd against which xml is to be validated
    """
    schematree = etree.XMLSchema(etree.parse(xsd))
    xmltree = etree.parse(xml)
    if schematree.validate(preprocess_for_validation(xmltree)) == False:
        print('!!! VALIDATION FAILURE WITH XSD !!!')
        #xmllint --noout --schema Time_control.xsd modified.xml 
        command = ("xmllint", "--noout", "--schema", sys.argv[2],"modified.xml")
        popen = subprocess.Popen(command,stdout=subprocess.PIPE)
        popen.wait()
        output = popen.stdout.read()
        print(output)
        #raise Exception()
    else:
        print('@@@ XML VALIDATES WITH XSD @@@')

def restructure_xml(tree):
    """
    Returns a restructured xml tree moving parameter nodes above child object nodes.

    ARGS:
        INOUT: tree -> xml tree
    """
    root = tree.getroot()
    #Moving parameter nodes twice because the order will be reversed
    #during 1st iteration
    for count in range(2):
        for child in root:
            for node in child.iter():
                #if no. of children is 0, that is a parameter node
                if len(node) ==  0:
                    node.getparent().insert(0, node)
    return tree

def get_full_path(node):
    """
    Returns full hierarchy of a node

    ARGS:
        IN: node -> xml node

    RETURN:
        String with full hierarchy
    """
    sDefine = '' + node.tag
    while node.getparent() != None:
        sDefine = node.getparent().tag + '.' + sDefine
        node = node.getparent()
    return sDefine

def change_path(inputFile, destPath):
    """
    Changes path. Used to place output xmls in a different path so that
    input xmls are not overwritten

    Changes abc/def.xml --> xyz/def.xml  (if 'destPath' = xyz)

    ARGS:
        IN:
            inputFile -> Input file with full path
            destPath  -> Destination path

    RETURN:
        Xml name appended with destination path 
    """
    return os.path.join(destPath, os.path.split(inputFile)[-1])

flag_dictonary = \
    {'syntax':{'int':0x1,'unsignedInt':0x2,'string':0x4,'dateTime':0x8, 'boolean':0x10, 'float': 0x10000000, \
            'hexBinary':0x20, 'long':0x40,'unsignedLong':0x80,'base64':0x100, 'none':0x200}, \
     'saveToFlash':{'false':0x400}, \
     'informToAcs':{'true':0x800}, \
     'activeNotify':{'active':0x1000, 'passive':0x2000, 'disabled':0x4000, 'canDeny': 0x20000000}, \
     'changeFlag':{'true':0x8000}, \
     'access':{'readOnly':0x200000, 'readWrite':0x400000}, \
     'dynamic':{'true':0x800000}, \
     'password':{'y':0x40000000}, \
    }

def encode_xml(tree):
    """
    Takes an xmls tree and encodes all mapped attributes into 'flag' attribute
    whose value is an encoded encoded hex digit

    Mapping will be based on below dictonary

    MAPPING:
        'syntax':{'int':0x1,'unsignedInt':0x2,'string':0x4,'dateTime':0x8, 'boolean':0x10, 'float': 0x10000000,
                  'hexBinary':0x20, 'long':0x40,'unsignedLong':0x80,'base64':0x100, 'none':0x200},
        'saveToFlash':{'false':0x400},
        'informToAcs':{'true':0x800},
        'activeNotify':{'active':0x1000, 'passive':0x2000, 'disabled':0x4000, 'canDeny': 0x20000000},
        'changeFlag':{'true':0x8000},
        'access':{'readOnly':0x200000, 'readWrite':0x400000},
        'dynamic':{'true':0x800000},

    ARGS:
        INOUT: tree -> xml tree
    """
    root = tree.getroot()
    for child in root: # node by node 
        for node in child.iter():   #all child nodes
            node.attrib['Flag'] = "0x0"
            encoded_value = 0x00
            #iterate through all the keys of 'flag_dictonary'
            for attrib in flag_dictonary:
                #gets attribute value if the attribute is present for that node. Else None.
                #E.g. For dynamic="true" in xml, return 'true'. If 'dynamic' attrib is not present in xml. Returns None
                value = node.get(attrib)
                if value != None:
                    if value in flag_dictonary[attrib]: #if value is listed, get the mapped hex value
                        #print(node.tag, '--> attrib, value, hex', attrib, value, hex(flag_dictonary[attrib][value]))
                        encoded_value = encoded_value | flag_dictonary[attrib][value]
                        #print('encoded value -->', str(hex(encoded_value)))
                        node.attrib['Flag'] = str(hex(encoded_value))
                    #if it is a parameter node, if 'dynamic' attribute is 'true', set dynamic bit in parent's 'Flag' attribute
                    if attrib == 'dynamic' and value == 'true' and len(node) == 0:
                        node.getparent().attrib['Flag'] = str(hex(int(node.getparent().get('Flag'), 16) | flag_dictonary[attrib][value]))
                    try:
                        del node.attrib[attrib]
                    except:
                        pass

def decode_xml(tree):
    """
    Reads the encoded 'Flag' attribute and regenerates back all the attributes.
    This is necessary for generating original xml back from id added xml. It uses
    the same mapping used from encoding to regenerate the encoded attributes.

    ARGS:
        INOUT: tree -> xml tree
    """
    for node in tree.iter():
        flag = node.get('Flag')

        if flag == None:
            print('No "Flag" attribute for', node.tag)
            continue

        for attrib_key in flag_dictonary:
            type_map_dict = flag_dictonary[attrib_key]
            for type_key in type_map_dict:
                #print(node.tag, type_map_dict[type_key], flag)
                flag = flag.strip()
                type_value = type_map_dict[type_key]
                and_val = type_value & int(flag, 16)
                #print(and_val, type_value)
                if and_val == type_value:
                    node.attrib[attrib_key] = type_key

        try:
            del node.attrib['Flag']
            del node.attrib['Id']
        except:
            pass

    return tree

            
def get_enum_from_ref(node):
    """
    Gererates a list of enumarations in a given node. 
    
    ARGS:
        IN: node -> node in tr-181 datatype XML
    
    RETURN:
        Returns string with enumarations and node name for
        which enumerations are listed
    """
    enum = ''
    for child in node:
        if child.tag == 'enumeration' and child.get('value') != None:
            enum = enum + child.get('value') + ','
    #print(node.tag, ' @@@@@@@@@ ' + enum + ' @@@@@@@@@')
    return enum,node.tag

def put_ref(tree, tr69refpath):
    """
    Generates back 'ref' attribute based on enumerations in 'enum' attribute.
    This is necessary for generating original xmls from id added control and
    data XMLs

    ARGS:
        INOUT: tree -> xml tree
        IN   : tr69refpath -> path to tr-181 datatype XML
    """
    root = tree.getroot()
    tr69tree = etree.parse(tr69refpath)
    tr69root = tr69tree.getroot()

    for node in root.iter():
        enum = node.get('enum')
        tr69ref = tr69root.find('ref_types')
        if enum != None and enum != '' and tr69ref != None:
            #print(node.tag + ' ######## ' + enum + ' #########')
            for tr69child in tr69ref:
                tr69enum,ref = get_enum_from_ref(tr69child)
                if tr69enum == enum:
                    node.attrib['ref'] = ref
        try:
            del node.attrib['enum']
        except:
            pass
    return tree

def get_enums(tree, tr69refpath):
    """
    Checks for 'ref' attribute in every node of the 'tree'. If yes, Sees if
    this ref is present in tr-181 data type xml. If yes, creates a 'enum'
    attribute with comma seperated enumataions collected from reference XML.

    ARGS:
        INOUT: tree -> xml tree
        IN   : tr69refpath -> path to tr-181 datatype XML 
    """
    root = tree.getroot()
    tr69tree = etree.parse(tr69refpath)
    tr69root = tr69tree.getroot()
    for child in root:
        #traversing through all the children(sub child too)
        for node in child.iter():
            ref = node.get('ref')
            tr69ref = tr69root.find('ref_types')
            if ref != None and tr69ref != None:
                for tr69child in tr69ref.iter():
                    if ref == tr69child.tag:
                        #print("Mach found to ", ref)
                        final_enum = ""
                        for tr69node in tr69child.iter():
                            if tr69node.tag == 'enumeration':
                                val = tr69node.get('value')
                                final_enum = final_enum + val + ','
                        #print(final_enum)
                        node.attrib['enum'] = final_enum
                        try:
                            del node.attrib['ref']
                        except:
                            pass


def generate_id_added_xml(tree):
    """
    Takes an xml tree, adds ids to it in sequential manner.

    ARGS:
        INOUT: tree -> xml tree
    """
    root = tree.getroot()

    for child in root:
        for node in child.iter():
            if('Instance' in node.attrib and int(node.attrib['Instance']) > 1):
                #print('##############', node.attrib['Instance'])
                generate_id_added_xml.nId = int(node.getprevious().attrib['Id'])
            # Non-leaf Nodes with empty attributes are duplicated nodes
            # which is present is different. Mark them with -1 to
            # be removed in header file
            if node.get('notSupported') == 'true' and len(node) != 0:
                node.attrib['Id'] = str(-1)
            else:
                node.attrib['Id'] = str(generate_id_added_xml.nId)
                generate_id_added_xml.nId = generate_id_added_xml.nId + 1
    return tree


def sync_id_n_flag_with_ctrl_xml(datatree, ctrltree):
    """
    Takes dataxml input, syncs id, flag attributes  with its control xml counterpart

    ARGS:
        INOUT: datatree -> data xml tree
        IN   : ctrltree -> control xml tree
    """
    print('################# syncing ids and flag ###################')
    dataroot = datatree.getroot()
    ctrlroot = ctrltree.getroot()

    for (datanode,ctrlnode) in [(i,j) for i in dataroot.iter() for j in ctrlroot.iter()]:
        #Just node name comparsion is not enough since there can be
        #multiple nodes with same name. So qualify full path
        if get_full_path(datanode) == get_full_path(ctrlnode):
            #print(datanode.tag, ctrlnode.tag)
            try:
                datanode.attrib['Id'] = ctrlnode.get('Id')
                datanode.attrib['Flag'] = ctrlnode.get('Flag')
            except:
                print("Couldn't set Id for node ", datanode.tag)



def generate_header_file(xml):
    """
    Takes input xml and creates header file with same name in specified path which
    contains #defines of Object/Parameter names and Id mapping.
    
    E.g. generate_header_file('abc/hello.xml', 'xyz/') --> Creates a hello.h in xyz/ folder

    ARGS:
        IN:
            xml -> input xml
            outPath -> Output path where headerfile is to be placed
    """
    #changes file name abc/def.xml to generated_header_flies/def.h
    sHeaderName = xml.replace('.xml', '.h')
    #print('Header file name = ', sHeaderName)
    fHeader = open(sHeaderName, 'w')
    tree = etree.parse(xml)
    root = tree.getroot()
    supported_objects = ''
    ChkFlg = 0;
    for node in root.iter():
       #list supported objects
       if node.get('Id') != str(-1) and len(node) != 0 and ChkFlg == 1:
            supported_objects = supported_objects + '\n\t''\"' + get_full_path(node) + '\"' + ','

       #print(get_full_path(node))
       #don't consider duplicated node. Missing number in the
       #headerfile indicate duplicated non-leaf node
       if ChkFlg == 1 and node.get('Id') != str(-1):
           #In case of 'parameter' if parent obj is unsupported, don't generate #define for it
           fHeader.write('#define %-150s%s\n' %(get_full_path(node).replace('.','_').upper(), node.get('Id')))
       if node.tag == 'Device':
           ChkFlg = 1;
    fHeader.write('\nOBJS_SUPPORTED = {' + supported_objects + '\n''};')
    fHeader.flush()
    fHeader.close()


def add_watermark(filepath, file_type, access_type = "ReadOnly"):
    """
    Adds watermark comment at the top of XML for GUI to be able to recognize it

    ARGS:
        IN: filepath -> Full path of XML for which watermark is to be added
    """
    #add watermark to the output xml for GUI compatibility
    chkWm = addWaterMark.waterMark()

    if chkWm.isWaterMarked(filepath) == True:
        chkWm.modifyWaterMark(filepath, file_type, access_type)
    else:
        chkWm.addWaterMark(filepath, file_type, access_type)


def generate_id_control(xml_tree):
    """
    Performs actions necessary to generate an id added control XML

    ACTIONS:
        1. Get actual syntax if it is of type 'dataTypeRef'
        2. Adds ID to every node in sequential manner
        3. Encodes other attributes into 'Flag' attribute
        4. Gets enums based of 'ref' type

    ARGS:
        INOUT: xml_tree -> xml tree
    """
    generate_id_added_xml.nId = 1
    tr69refpath = args.tr181filepath

    preprocess_xml(xml_tree) #Get actual 'syntax' value and other attributes for indirect references

    generate_id_added_xml(xml_tree) #Add id in sequential manner
   
    encode_xml(xml_tree) #encode all necessary attributes to 'Flag' attribute

    get_enums(xml_tree, tr69refpath)  #generate all 'enums' attribute by referring to 'tr-181-2-8-0_dataType.xml'
	

def gen_id_data(data_xml_tree, ctrl_id_xml_tree):
    """
    Calls actions necessary for generating id added data xml

    ARGS:
        INOUT:
            data_xml_tree -> data xml tree
        IN:
            ctrl_id_xml_tree -> id added control xml tree
    """
    sync_id_n_flag_with_ctrl_xml(data_xml_tree, ctrl_id_xml_tree)

##################### for parsing command line options and arguments ######################


def arg_parse_init():
    """
    Defines a structure for command line arguments. All the violations
    in commandline arguments will be reported with a 'help' output
    """
    parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter, description=textwrap.dedent('''\

    STEPS TO GENERATED ID ADDED CONTROL AND DATA XML AT ONCE

    *STEP 1: copy your control and data xml to current directory (If you don't copy, your input file will be overwritten)
    *STEP 2: execute --> python genHdrnMergeXML.py -a default <your input control xml>

    Check the current directory for  id added control and data XMLs

    NOTE:   If name of control xml is AAA_control.xml, in the same directory, name of the data xml
            should be AAA_data.xml. Otherwise you need to generate id added data xml manually as shown below


    You can also generate id added control and data xmls individually

            Examples
    --------------------------------
    pythongenHdrnMergeXML.py -c <tr-181-2-9-0_dataType.xml> <ss_control.xml>
    python genHdrnMergeXML.py -d <ss_control_id.xml> <ss_data.xml>

    NOTE: By default, output file is placed in current folder where 
          script is being executed
    NOTE: Due to some hard codings, it is mandatory that you execute 
          the script from the location where it exists
    NOTE: Since you'll be executing script from its existing location, you
          can replace <tr-181-2-9-0_dataType.xml> path with 'default' which
          uses tr-181 datatype file from the standard location'''))


    group1 = parser.add_mutually_exclusive_group()

    parser.add_argument('-v', '--validate', dest="xsdfile", help = "Performs validation against XSD file specified")
    group1.add_argument('-c', '--ctrl', dest="tr181filepath" , help="Performs --> 1.Generates IDs 2.encodes attributes into 'flag' attrib \
                                                                                  3.Generates enum attribute based on tr181 reference xml \
                                                                                  4.merging xmls 5.generate headerfile")

    group1.add_argument('-d', '--data', dest="ctrlxmlpath", help= "Generates data xml by taking control xml as reference")
    group1.add_argument('-a', '--all', dest='tr181filepath2', help="Autogenerates id data xml \
											if data xml is in the same \
											path as control xml and \
											naming convention \
											is proper")
    group1.add_argument('-p', '--putback', dest="tr181filepath3", help="Generates original xml back")
    parser.add_argument('input_file', nargs=1, help='input xml file')
    args = parser.parse_args()
    #print args
    return args

####################### START PROCESSING #########################

def generate_xmls(args):
    """
    Start generating XMLS based on input options

    ARGS:
        IN: args -> command line arguments
    """
    #Name the output file as per the input
    args.input_file = args.input_file[0]
    OUTPUT_FILEPATH = args.input_file.replace('.xml', '_id.xml') 

    parser = etree.XMLParser(remove_blank_text=True) # lxml.etree only!
    
    # validate with xsd
    if args.xsdfile != None:
        validate_with_xsd(args.input_file, args.xsdfile)


    # generate both ctrl/data id files. -a commandline option
    if args.tr181filepath2 != None:
        if args.tr181filepath2 == 'default':
            args.tr181filepath2 = TR181_FILEPATH
            if os.path.isfile(args.tr181filepath2) == False:
                print('ERROR: Either there is no tr181 datatype xml file in standard path (or) you did not execute the script from its location')
                sys.exit(errno.ENOENT) 
        
        dataXML = args.input_file.replace('_control.xml', '_data.xml')

        global AUTO_DATAIDGEN
        if os.path.exists(dataXML):
            args.ctrlxmlpath = 'default'
            AUTO_DATAIDGEN = True
            args.tr181filepath = args.tr181filepath2  
        else:
            print('NOTE: data xml "'+dataXML+'" not present')
            print('Aborting generation of id added control & data xml')
            print('Generate data xml first and place it in the same location as your control xml')
            print('If name of your ctrl xml is "AAA_control.xml", name of your data xml should be "AAA_data.xml"')

    #generates id added control xml. -c commandline option
    if args.tr181filepath != None:
        print('########################################################################')
        print('   GENERATING CONTROL XML.\n \
                  ID, HEADERS, ENCODING AND MERGING(for multiple files)....    ')
        print('########################################################################')

        if args.tr181filepath == 'default':
            args.tr181filepath = TR181_FILEPATH
            if os.path.isfile(args.tr181filepath) == False:
                print('ERROR: Either there is no tr181 datatype xml file in standard path (or) you did not execute the script from its location')
                sys.exit(errno.ENOENT)
    
	
        xml_tree = etree.parse(args.input_file, parser)
        generate_id_control(xml_tree)
        xml_tree.write(OUTPUT_FILEPATH, pretty_print=True)
        generate_header_file(OUTPUT_FILEPATH)

        #add watermark to the output xml for GUI compatibility
        add_watermark(OUTPUT_FILEPATH, "schema_id", "ReadOnly")
    
        print('############## DONE GENERATING ID ADDED CONTROL XML #################')

    #Create data xml by taking control xml into consideration. -d cmdline option
    if args.ctrlxmlpath != None:
        print('########################################################################')
        print('   GENERATING DATA XML')
        print('########################################################################')

        if args.ctrlxmlpath == 'default':
            #if we came here through auto generation instead of manually
            #invoking the script for generating id added xmls, change
            #input file name
            if AUTO_DATAIDGEN == True:
                #-- Change input file name to _data.xml if control is
                #coming from '-a' option
                args.input_file = args.input_file.replace('_control.xml', '_data.xml')
                OUTPUT_FILEPATH = args.input_file.replace('.xml', '_id.xml')
            args.ctrlxmlpath = args.input_file.replace('_data.xml', '_control_id.xml')
            if os.path.isfile(args.ctrlxmlpath) == False:
                print('ERROR: Either there is no id added control xml file in standard path (or) you did not execute the script from its location')
                sys.exit(errno.ENOENT)

        ctrltree = etree.parse(args.ctrlxmlpath, parser)
        datatree = etree.parse(args.input_file, parser)

        gen_id_data(datatree, ctrltree)
        datatree.write(OUTPUT_FILEPATH, pretty_print=True)

        #add watermark to the output xml for GUI compatibility
        add_watermark(OUTPUT_FILEPATH, "instance_id", "ReadWrite")
        print('############## DONE GENERATING ID ADDED DATA XML #################')

    # Generate original XMLs back from ID added xmls. '-p' option
    if args.tr181filepath3 != None:
    
        if args.tr181filepath3 == 'default':
            args.tr181filepath3 = TR181_FILEPATH
            if os.path.isfile(args.tr181filepath3) == False:
                print('ERROR: Either there is no tr181 datatype xml file in standard path (or) you did not execute the script from its location')
                sys.exit(errno.ENOENT)

        # All the id xmls will be generated to the place where original
        # input xmls are present because we usually do this from GUI. 
        # But putback option doesn't do this as this might not be
        # integrated into GUI. It generates xml to the same path as
        # script. Change this line incase you want to generate
        # 'putback xml' to the same path as input xml.
    
        tree = etree.parse(args.input_file, parser)
  
        #If input is a control xml
        if '_control_id.xml' in args.input_file:
            tree = put_ref(tree, args.tr181filepath3)
            tree = decode_xml(tree)
        elif '_data_id.xml' in args.input_file:
            for node in tree.iter():
                try:
                    del node.attrib['Flag']
                    del node.attrib['Id']
                except:
                    pass
        else:
            print('Invalid xml file to putback. Name should be "_control_id.xml" or "_data_id.xml"')
            sys.exit(errno.EINVAL)
        
        OUTPUT_FILEPATH = change_path(args.input_file, './').replace('_id.xml', '.xml')
        tree.write(OUTPUT_FILEPATH, pretty_print=True)

        if '_control.xml' in args.input_file:
            add_watermark(OUTPUT_FILEPATH, "schema", "ReadWrite")
        elif '_data.xml' in args.input_file:
            add_watermark(OUTPUT_FILEPATH, "instance", "ReadWrite")
    
        print('############## ORIGINAL FILES REGENERATED FROM ENCODED XML #################')

if __name__ == '__main__':
    args = arg_parse_init()
    generate_xmls(args)
