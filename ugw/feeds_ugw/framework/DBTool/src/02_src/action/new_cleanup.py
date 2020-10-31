import os
import sys
from lxml import etree
import argparse
import textwrap
import shutil

from copy import deepcopy

sys.path.append('../common/')
import common


class cleanup2():
    def __init__(self):
        pass

    def main(self, xml_path):
        if os.path.isfile(xml_path) == False:
            print('ERROR: No file in the location')
            sys.exit(-1)

        XML_NAME = os.path.split(xml_path)[-1]
        parser = etree.XMLParser(remove_blank_text=True)                # lxml.etree only!
        fullxml_tree = etree.parse(xml_path, parser)
        
        #Remove comments from xml
        etree.strip_tags(fullxml_tree, etree.Comment)

        for node in fullxml_tree.xpath("//parameter|//profile"):
            try:
                node.attrib.pop('dmr:version')
            except:
                pass

        #Move datatype nodes to a different xml
        datatypexml = etree.Element('root')
        profilexml = etree.Element('root')

        #Remove moved nodes. Directly removing while moving is causing
        #iteration problems

        for elem in fullxml_tree.xpath( '//dataType|//profile' ) :
            if elem.tag == 'profile':
                profilexml.append(deepcopy(elem))
            elif elem.tag == 'dataType':
                datatypexml.append(deepcopy(elem))
            elem.getparent().remove(elem)

        datatypexml_tree = datatypexml.getroottree()
        profilexml_tree = profilexml.getroottree()

        
        #Save file to proper output path
        fullxml_tree.write('tr181full.xml', pretty_print=True)
        datatypexml_tree.write('datatype.xml', pretty_print=True)
        profilexml_tree.write('profile.xml', pretty_print=True)


if __name__ == '__main__':
    cleanup = cleanup2()
    cleanup.main(sys.argv[1])
        
         
