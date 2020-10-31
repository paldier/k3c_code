/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/
package com.lantiq.dbtool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.HashMap;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTree;

import com.lantiq.dbtool.base.AccessType;
import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.base.LantiqMutableTreeNode;
import com.lantiq.dbtool.base.NodeType;
import com.lantiq.dbtool.ui.LantiqFrame;

public class FileUtils {

	private static final Logger logger = LogManager.getLogger(FileUtils.class);
	File defaultdir = new File(LantiqFrame.getProperties()
			.get("defaultlocation").toString());
	static File lastOpenedLocation = null;
	 

	public File openFile(JFrame parent, FileFilter ff, boolean reversedorderff) {
		logger.log(Level.INFO, null, "Open File");
		File selectedfile = null;
		JFileChooser fileChooser = new JFileChooser();

		if (getLastOpenedLocation() == null) {
			if (defaultdir != null)
				fileChooser.setCurrentDirectory(defaultdir);
		} else {
			fileChooser.setCurrentDirectory(getLastOpenedLocation());
		}

		fileChooser.addChoosableFileFilter(ff);
		
		if(reversedorderff)
			fileChooser.setFileFilter(ff);
		
		int returnVal = fileChooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedfile = fileChooser.getSelectedFile();
			setLastOpenedLocation(selectedfile.getParentFile());
		} else {
			logger.log(Level.INFO, null, "Open command cancelled by user.");
		}
		return selectedfile;
	}

	private String getFileExtension(File file) {
		String extension = "";

		int i = file.getName().lastIndexOf('.');
		if (i > 0) {
			extension = file.getName().substring(i + 1);
		}

		return extension;
	}

	public File saveFile(JFrame parent, FileFilter ff, boolean saveas) {
		logger.log(Level.INFO, null, "Save File");
		File selectedfile = null;
		JFileChooser fileChooser = new JFileChooser();

		if (getLastOpenedLocation() == null) {
			if (defaultdir != null)
				fileChooser.setCurrentDirectory(defaultdir);
		} else {
			fileChooser.setCurrentDirectory(getLastOpenedLocation());
		}

		fileChooser.setFileFilter(ff);
		int returnVal = fileChooser.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedfile = fileChooser.getSelectedFile();
			if (saveas) {
				String ext = getFileExtension(selectedfile);
				if (ext == null || ext.equals("") || ext.length() <= 0) {
					selectedfile = new File(selectedfile.getAbsolutePath()
							+ ".xml");
				}
			}
			setLastOpenedLocation(selectedfile.getParentFile());
		} else {
			logger.log(Level.INFO, null, "Open command cancelled by user.");
		}
		return selectedfile;
	}

	public File[] openFiles(JFrame parent, FileFilter ff) {
		logger.log(Level.INFO, null, "Open Files");
		File[] selectedfiles = null;
		JFileChooser fileChooser = new JFileChooser();

		if (getLastOpenedLocation() == null) {
			if (defaultdir != null)
				fileChooser.setCurrentDirectory(defaultdir);
		} else {
			fileChooser.setCurrentDirectory(getLastOpenedLocation());
		}

		fileChooser.setFileFilter(ff);
		fileChooser.setMultiSelectionEnabled(true);
		int returnVal = fileChooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedfiles = fileChooser.getSelectedFiles();
			setLastOpenedLocation(selectedfiles[0].getParentFile());
		} else {
			logger.log(Level.INFO, null, "Open command cancelled by user.");
		}
		return selectedfiles;
	}

	public int countLineNumber(File file) {
		int lines = 0;
		try {
			LineNumberReader lineNumberReader = new LineNumberReader(
					new FileReader(file));
			lineNumberReader.skip(Long.MAX_VALUE);
			lines = lineNumberReader.getLineNumber();
			lineNumberReader.close();
		} catch (IOException ex) {
			logger.log(Level.DEBUG, "Count Line Number", ex);
		}
		return lines;
	}

	public StringBuffer readFile(File file) {
		StringBuffer strbuff = new StringBuffer();
		logger.log(Level.INFO, null, "Reading File");
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (!line.equals(""))
					strbuff.append(line);
			}
			br.close();
		} catch (IOException e) {
			logger.log(Level.DEBUG, "Error Occured : Reading File", e);
		}

		logger.log(Level.INFO, null, "Reading File Done");
		return strbuff;
	}

	public void writeObjectToFile(HashMap<String, Object> map, String filename) {
		logger.log(Level.INFO, null, "Writing Object to File");
		String currentdir = System.getProperty("user.dir");
		String newFilename = filename;

		try {
			FileOutputStream fos = new FileOutputStream(currentdir + "/"
					+ newFilename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(map);

			oos.flush();
			oos.close();
			fos.close();
		} catch (IOException e) {
			logger.log(Level.DEBUG, "Error Occured : Writing File", e);
		}
	}

	public HashMap<String, Object> readObjectFromFile(String filename) {
		logger.log(Level.INFO, null, "Reading Object from File");
		String currentdir = System.getProperty("user.dir");
		String newFilename = filename;

		HashMap<String, Object> map = null;
		try {
			FileInputStream fis = new FileInputStream(currentdir + "/"
					+ newFilename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (HashMap<String, Object>) ois.readObject();

			ois.close();
			fis.close();
		} catch (IOException | ClassNotFoundException e) {
			logger.log(Level.DEBUG, "Error Occured : Reading Object", e);
		}

		return map;
	}

	public void writeTreeToFile(JXTree tree, String filepath  ) {
		logger.log(Level.INFO, null, "Writing XML File");
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		try {
			XMLStreamWriter defaultWriter = factory
					.createXMLStreamWriter(new FileWriter(filepath));
			IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(
					defaultWriter);
			readandWriteJTree(tree, writer);
			writer.flush();
			writer.close();

		} catch (XMLStreamException | IOException e) {
			logger.log(Level.DEBUG, "Error Occured : Write Tree File", e);
		}
	}

	public StringBuffer writeTreeToStream(JXTree tree, FileType type,
			AccessType atype) {
		logger.log(Level.INFO, null, "Writing XML File Buffer");
		StringWriter sw = new StringWriter();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		try {
			XMLStreamWriter defaultWriter = factory.createXMLStreamWriter(sw);
			IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(
					defaultWriter);
			readandWriteJTree(tree, writer);
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			logger.log(Level.DEBUG, "Error Occured : Write JTree Stream", e);
		}

		return sw.getBuffer();
	}

	private void readandWriteJTree(JXTree tree, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartDocument();
		LantiqMutableTreeNode root = (LantiqMutableTreeNode) ((DefaultTreeModel) tree
				.getModel()).getRoot();
		writeNode(root, writer);
		writer.writeEndDocument();
	}

	private void writeNode(LantiqMutableTreeNode node, XMLStreamWriter writer)
			throws XMLStreamException {
		String userObject = node.getUserObject().toString();

		if (userObject.contains(".{") && userObject.contains("}")) {
			// write node name
			writer.writeStartElement(userObject.substring(0,
					userObject.indexOf(".{")));
			// write attributes
			/*String value = userObject.substring(userObject.indexOf("{") + 1,
					userObject.indexOf("}"));
			writer.writeAttribute("Instance", value);*/
		} else {
			// write node name
			writer.writeStartElement(userObject);
		}
		// write attributes
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			LantiqMutableTreeNode child = (LantiqMutableTreeNode) node
					.getChildAt(i);
			if (child.getNodetype() == NodeType.ATTRIB) {
				if(child.getValue() != null)
					writer.writeAttribute(child.getUserObject().toString(), child
							.getValue().toString());
			}
		}
		if (null != node.getValue()) {
			writer.writeCharacters(node.getValue().toString());
		}
		// write children
		for (int i = 0; i < count; i++) {
			LantiqMutableTreeNode child = (LantiqMutableTreeNode) node
					.getChildAt(i);
			if (child.getNodetype() != NodeType.ATTRIB) {
				writeNode(child, writer);
			}
		}
		// write end element
		writer.writeEndElement();
	}

	public static File getLastOpenedLocation() {
		return lastOpenedLocation;
	}

	public static void setLastOpenedLocation(File lastOpened) {
		lastOpenedLocation = lastOpened;
	}	
}
