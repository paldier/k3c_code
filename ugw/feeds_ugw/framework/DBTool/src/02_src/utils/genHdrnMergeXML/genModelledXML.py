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



import sys
import os
from lxml import etree
import argparse

def get_full_path(node):
    """
        Function for fully qualifying a node name for header files.
    """
    sDefine = '' + node.tag
    while node.getparent() != None:
        sDefine = node.getparent().tag + '_' + sDefine
        node = node.getparent()
    return sDefine.upper()


def genModeledXML(inputxmltree, modelxmltree):
    inputroot = inputxmltree.getroot()
    modelroot = modelxmltree.getroot()

    for (inputnode,modelnode) in [(i,j) for i in inputroot.iter() for j in modelroot.iter()]:
        #compare only the leaf nodes of model xml
        if len(modelnode) == 0:
        #Just node name comparsion is not enough since there can be
        #multiple nodes with same name. So qualify full path
            if get_full_path(inputnode) == get_full_path(modelnode):
                print(get_full_path(inputnode), modelnode.keys())
                if modelnode.get('oper') == 'replace':
                    try:
                        for attrib in modelnode.keys():
                            if attrib != 'oper':
                                inputnode.attrib[attrib] = modelnode.get(attrib)
                    except:
                        print('Invalid attribute')
                if modelnode.get('oper') == 'del':
                    try:
                        for attrib in modelnode.keys():
                            if attrib != 'oper':
                                del inputnode.attrib[attrib]
                    except:
                        print('Unable to delete attribte')


parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter)

parser.add_argument('-m', '--model', dest="modelxml", help='Model xml')

parser.add_argument('input_files', nargs='+', help='optional input xml files')

parser.add_argument('-o', '--outputfile', dest="outputfilepath", default= "output.xml", help="Output file name")

args = parser.parse_args()

############## GENERATNG MODELED XMLS #################

xmlparser = etree.XMLParser(remove_blank_text=True) # lxml.etree only!

modelxmltree = etree.parse(args.modelxml, xmlparser)

for inputxml in args.input_files:
    inputxmltree = etree.parse(inputxml, xmlparser)
    genModeledXML(inputxmltree, modelxmltree)
    if args.outputfilepath.endswith('_modeled.xml') == False:   #check if output file name is ending with '_id'. If not, add it
        args.outputfilepath = args.outputfilepath.replace('.xml', '_modeled.xml')
    inputxmltree.write(args.outputfilepath, pretty_print=True)
