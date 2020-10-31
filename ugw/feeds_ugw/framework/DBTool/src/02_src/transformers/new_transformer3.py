from lxml import etree, objectify
import sys

def add_attributes(node, attributes_dict):
    for key in attributes_dict:
        node.attrib[key] = attributes_dict[key]

def extract_string(syntax_child_node, param_node, datatypexml_tree):

    enum_list = []
    enumRef = ""
    for extras in syntax_child_node:
        if extras.tag == 'size' or extras.tag == 'pattern':
            add_attributes(param_node, extras.attrib)

        if extras.tag == 'enumeration':
            enumRef = param_node.getparent().get('name').split('.')[-2] + param_node.get('name').strip() + 'Enum'
            param_node.attrib['ref'] = enumRef
            enum_list.append(extras)
    
    if enum_list != []:
        enum_ref_node = etree.SubElement(datatypexml_tree.getroot(), enumRef)
        for enum in enum_list:
            enum_ref_node.append(enum)





def add_syntax(param_node, datatypexml_tree):
    for syntax in param_node.xpath('./syntax'):
        for s in syntax:    #iterate through all the nodes in 'syntax'
            print s.tag
            if s.tag == 'string':
                param_node.attrib['syntax'] = s.tag
                extract_string(s, param_node, datatypexml_tree)
            if s.tag == 'unsignedInt':
                param_node.attrib['syntax'] = s.tag
                for child in s:
                    add_attributes(param_node, child.attrib)
            if s.tag == 'int':
                param_node.attrib['syntax'] = s.tag
                for child in s:
                    add_attributes(param_node, child.attrib)
            if s.tag == 'boolean':
                param_node.attrib['syntax'] = s.tag
            if s.tag == 'unsignedLong':
                param_node.attrib['syntax'] = s.tag
                for child in s:
                    add_attributes(param_node, child.attrib)
            if s.tag == 'dataType':
                param_node.attrib['syntax'] = s.tag
            if s.tag == 'dateTime':
                param_node.attrib['syntax'] = s.tag
            if s.tag == 'list':
                param_node.attrib['syntax'] = s.tag
                for child in s:
                    add_attributes(param_node, child.attrib)
            if s.tag == 'hexBinary':
                param_node.attrib['syntax'] = s.tag
                for child in s:
                    add_attributes(param_node, child.attrib)
            if s.tag == 'base64':
                param_node.attrib['syntax'] = s.tag
                for child in s:
                    add_attributes(param_node, child.attrib)
            if s.tag == 'long':
                param_node.attrib['syntax'] = s.tag
                for child in s:
                    add_attributes(param_node, child.attrib)
    syntax.getparent().remove(syntax)
    for description in param_node.xpath('//description'):
        description.getparent().remove(description)
                
def transformer3(fullstripped_xml = 'tr-181fullStripped.xml', transformer2_xml = 'transformer2.xml', datatype_xml = '../action/datatype.xml'):
    '''
        Creates hierarchy
    '''
    parser = etree.XMLParser(remove_blank_text=True)
    fullxml_tree = etree.parse(fullstripped_xml, parser)
    trans2xml_tree = etree.parse(transformer2_xml, parser)
    datatypexml_tree = etree.parse(datatype_xml, parser)

    trans2xml_tree_root = trans2xml_tree.getroot()

    #Get 'Device' node and remove all unnecessary stuff
    for obj in trans2xml_tree.xpath('//object'):
        print '@@@@ Object = ', obj.get('name', datatype_xml)
        for param in obj.xpath('./parameter'):
            print '**** Param = ', param.get('name')
            add_syntax(param, datatypexml_tree)

    trans2xml_tree.write('transformer3.xml', pretty_print=True)
    datatypexml_tree.write('../action/datatype.xml', pretty_print=True)

if __name__ == '__main__':
    transformer3()
