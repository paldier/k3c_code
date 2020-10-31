__author__ = 'tejar'
from lxml import etree
import sys
import unittest
import os

def strip(xml, dir = './stripped_xmls'):
    parser = etree.XMLParser(remove_blank_text=True) # lxml.etree only!
    tree = etree.parse(xml, parser)
    root = tree.getroot()

    for elem in root.iter('*'):
        if elem.text is not None:
            elem.text = elem.text.strip()
        if elem.tail is not None:
            elem.tail = elem.tail.strip()

    if not os.path.exists(dir):
        os.mkdir(dir)

    tree.write(os.path.join(dir, os.path.split(xml)[1]), pretty_print=True)

if __name__ == '__main__':
    for xml in sys.argv[1:]:
        strip(xml)
