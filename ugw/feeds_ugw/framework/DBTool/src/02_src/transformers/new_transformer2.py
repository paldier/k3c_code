from lxml import etree, objectify
import sys

def create_hierarchy(xml_tree, node):
    node_name = node.get('name')
    if node_name[-1] != '.':
        node_name = node_name + '.'

    #remove instance info. Add multiInst = 'yes' here
    if '{i}.' in node_name:
        node_name = node_name.replace('{i}.', '')
        node.attrib['name'] = node_name

    # if child name --> 'a.b.c.d.'. Parent name --> 'a.b.c.'
    parent_name = node_name.rsplit('.', 2)[0] + '.'
    #print '@@@@ Object Get =', node_name
    #print '**** Parent Get =', parent_name
   
    #------- Create proper hierarcy based on object level for xpath search ---------
    xpath_base = '/object' * (parent_name.count('.')-1)
    xpath_str = '//Device' + xpath_base + '[@name="' + parent_name + '"]'
    #print '^^^^ Xpath Base =', xpath_str
    
    for elem in xml_tree.xpath(xpath_str):
        #print '$$$$ Parent Got =', elem.get('name')
        elem.append(node)

def transformer2(fullstripped_xml = 'tr-181fullStripped.xml', transformer1_xml = 'transformer1.xml'):
    '''
        Creates hierarchy
    '''
    parser = etree.XMLParser(remove_blank_text=True)
    fullxml_tree = etree.parse(fullstripped_xml, parser)
    trans1xml_tree = etree.parse(transformer1_xml, parser)

    trans1xml_tree_root = trans1xml_tree.getroot()

    #Check all node names and create hierarchy from them
    for elem in fullxml_tree.xpath('//model/object[starts-with(@name,"Device.")]'):
        #Service nodes
        if elem.get('name').count('.') == 2:
            trans1xml_tree_root.append(elem)
        #Sub objects of service nodes
        elif elem.get('name').count('.') > 2:
            create_hierarchy(trans1xml_tree, elem)

    trans1xml_tree.write('transformer2.xml', pretty_print=True)



if __name__ == '__main__':
    transformer2()
