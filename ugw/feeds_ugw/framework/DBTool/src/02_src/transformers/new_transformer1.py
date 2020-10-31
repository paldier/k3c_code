from lxml import etree, objectify
import sys

def transformer1(xml_path):
    '''
        Creates 'Device.' node
    '''
    parser = etree.XMLParser(remove_blank_text=True)
    fullxml_tree = etree.parse(xml_path, parser)

    #Get 'Device' node and remove all unnecessary stuff
    for elem in fullxml_tree.xpath( '//object[@name="Device."]' ):
        attributes = elem.attrib
        elem.tag = 'Device'

        try:
            del elem.attrib['name']
        except:
            pass
        
        elem_tree = etree.ElementTree(elem)
        etree.strip_elements(elem_tree, "*")
        break

    elem_tree.write('transformer1.xml', pretty_print=True)



if __name__ == '__main__':
    transformer1(sys.argv[1])
