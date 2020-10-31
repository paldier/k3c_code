/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.utils;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import com.lantiq.dbtool.base.AccessType;
import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.ui.LantiqFrame;
import com.lantiq.dbtool.ui.LantiqProgressDialog;

public class PythonExecutor {

	private static final Logger logger = LogManager
			.getLogger(PythonExecutor.class);

	private Window parent;
	private LantiqProgressDialog dialog;

	File logFile = new File(System.getProperty("user.dir")
			+ "/../logs/DBToolCmd.log");

	public PythonExecutor(Window parent) {
		this.parent = parent;
	}

	public void displayModalDialog(boolean open) {
		if (open) {
			dialog = new LantiqProgressDialog(true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation(screensize.width / 2 - 125,
					screensize.height / 2 - 200);
			dialog.pack();
			dialog.setVisible(true);
		} else {
			if (dialog != null) {
				dialog.dispose();
				dialog = null;
			}
		}
	}

	public void execPythonScript(String scriptsDir, String command,
			String[] params) {
		logger.log(Level.INFO, null, "Execute Pyton Script");

		// Using Jython
		PythonInterpreter.initialize(System.getProperties(),
				System.getProperties(), new String[0]);

		PythonInterpreter interp = new PythonInterpreter();

		try {
			interp.setOut(new FileOutputStream(logFile, true));
			interp.setErr(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		switch (command) {
		case "c2d.py":
			execPythonScriptC2D(interp, command, scriptsDir, params);
			break;
		case "genCtrlXML.py":
			execPythonScriptGenControl(interp, command, scriptsDir, params);
			break;
		case "x2x.py":
			execPythonScriptX2X(interp, command, scriptsDir, params);
			break;
		}
	}

	// work this out for Gen Control
	public void execPythonScriptGenControl(final PythonInterpreter interp,
			final String command, final String scriptsDir, final String[] params) {

		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
			protected Boolean doInBackground() throws Exception {
				// Here we can return some object of whatever type
				// we specified for the first template parameter.
				// (in this case we're auto-boxing 'true').
				try {
					PySystemState sys = Py.getSystemState();

					/*
					 * sys.path.append(new
					 * PyString("c:/Python27/lib/site-packages/lxml"));
					 * interp.exec("from lxml import etree");
					 */
					sys.path.append(new PyString(scriptsDir + "/action"));
					interp.exec("from pathCorrect import pathCorrect");

					sys.path.append(new PyString(scriptsDir + "/common"));
					interp.exec("from common import *");

					sys.path.append(new PyString(scriptsDir + "/action"));
					interp.exec("from cleanup import *");
					// interp.exec("import restructureControlXML");

					sys.path.append(new PyString(scriptsDir + "/utils"));

					interp.exec("from " + command.replace(".py", "")
							+ " import genCtrlXML");
					PyObject jyGenControlClass = interp.get("genCtrlXML");
					PyObject jynstance = jyGenControlClass.__call__();

					sys.path.append(new PyString(scriptsDir + "/transformers"));
					interp.exec("import transformer1");
					interp.exec("import transformer2");
					interp.exec("import transformer3");
					interp.exec("import transformer4");
					interp.exec("import transformer5");
					interp.exec("import transformer6");
					interp.exec("import transformer7");
					interp.exec("import transformer8");
					interp.exec("import transformer9");

					/*
					 * int block1 =
					 * jyGenControlClass.invoke("genCtrlXML_block1", jynstance,
					 * new PyString(params[0])).asInt();
					 * 
					 * int block2 =
					 * jyGenControlClass.invoke("genCtrlXML_block2", jynstance,
					 * new PyString(params[0])).asInt();
					 * 
					 * int block3 =
					 * jyGenControlClass.invoke("genCtrlXML_block3", jynstance,
					 * new PyString(params[0])).asInt();
					 */

					int block1 = jyGenControlClass.invoke("genCtrlXML",
							jynstance, new PyString(params[0])).asInt();

					/*String servicesDir = LantiqFrame.getProperties()
							.get("servicedir").toString();
					execScript(scriptsDir + "/action", "python", new String[] {
							"restructureControlXML.py", servicesDir + "/*" },
							null);*/

					if (block1 == 0 /* && block2 == 0 && block3 == 0 */) {
						JOptionPane.showMessageDialog(null,
								"Import successful", "Success",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null,
								"Error while Importing", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					return true;
				} catch (Exception e) {
					logger.log(Level.ERROR, "Error Occured" + e.toString());
					throw e;
				}
			}

			protected void done() {
				displayModalDialog(false);
			}
		};
		// Can safely update the GUI from this method.
		worker.execute();

		if (SwingUtilities.isEventDispatchThread()) {
			displayModalDialog(true);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					displayModalDialog(true);
				}
			});
		}
	}

	public void execPythonScriptX2X(PythonInterpreter interp, String command,
			String scriptsDir, String[] params) {
		PySystemState sys = Py.getSystemState();

		sys.path.append(new PyString(
				"C:/Python27/Lib/site-packages/openpyxl/workbook"));
		interp.exec("from openpyxl import Workbook,load_workbook");

		sys.path.append(new PyString(scriptsDir + "/utils/modules"));
		interp.exec("from toExcel import toExcel");
		interp.exec("from toXML import toXML");
		interp.exec("from splitXML import splitXML");

		sys.path.append(new PyString(scriptsDir + "/utils"));

		interp.exec("from " + command.replace(".py", "") + " import x2x");
		PyObject jyX2XClass = interp.get("x2x");
		PyObject jyX2XInstance = jyX2XClass.__call__();
		if (params[0].equals("-toEXL")) {
			jyX2XInstance.invoke("createXL", new PyString(params[1]));
		} else if (params[0].equals("-toXML")) {
			jyX2XInstance.invoke("createXML", new PyString(params[1]));
		}
	}

	public void execPythonScriptC2D(PythonInterpreter interp, String command,
			String scriptsDir, String[] params) {
		PySystemState sys = Py.getSystemState();

		sys.path.append(new PyString(scriptsDir + "/common"));
		interp.exec("from common import *");

		sys.path.append(new PyString(scriptsDir + "/utils/modules"));
		interp.exec("from removeAllAttributes import removeAllAttributes");
		interp.exec("from renameMultiInst import renameMultiInst");

		sys.path.append(new PyString(scriptsDir + "/utils"));
		interp.exec("from " + command.replace(".py", "")
				+ " import c2dConverter");

		PyObject jyc2dClass = (PyObject) interp.get("c2d");
		PyObject jyc2dInstance = (PyObject) jyc2dClass.__call__();

		int i = jyc2dInstance.invoke("c2dConverter", new PyString(params[0]))
				.asInt();

		if (i == 0) {
			JOptionPane.showMessageDialog(parent,
					"Script Completed Succesfully", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(parent, "Error in Script", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public String execPythonScriptVersion(String command,
			String scriptsDir) {
		logger.log(Level.INFO, null, "Execute Pyton Script");

		// Using Jython
		PythonInterpreter.initialize(System.getProperties(),
				System.getProperties(), new String[0]);

		PythonInterpreter interp = new PythonInterpreter();

		try {
			interp.setOut(new FileOutputStream(logFile, true));
			interp.setErr(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		PySystemState sys = Py.getSystemState();

		sys.path.append(new PyString(scriptsDir + "/utils"));
		interp.exec("from " + command.replace(".py", "")
				+ " import getVer");

		PyObject jyc2dClass = (PyObject) interp.get("getVer");
		PyObject jyc2dInstance = (PyObject) jyc2dClass.__call__();

		String version = jyc2dInstance.invoke("getString").asString();

		return version;
	}

	public boolean execScriptC2D(String scriptsDir, String command,
			String[] params) {
		ArrayList<String> commandlst = new ArrayList<>();
		commandlst.add(0, command);

		int i = 1;
		for (String param : params) {
			commandlst.add(i, param);
			i++;
		}

		ProcessBuilder pb = new ProcessBuilder(commandlst);
		pb.directory(new File(scriptsDir));
		pb.redirectErrorStream(true);
		
		pb.redirectError(Redirect.appendTo(logFile));
		pb.redirectOutput(Redirect.appendTo(logFile));
		
		try {
			Process p = pb.start();
			int exitCode = p.waitFor();
			if (exitCode == 0) {
				return true;
				/*
				 * JOptionPane.showMessageDialog(parent,
				 * "Instance file created successfully", "Success",
				 * JOptionPane.INFORMATION_MESSAGE);
				 */

			} else {
				return false;
				/*
				 * JOptionPane.showMessageDialog(parent,
				 * "Error while creating Instance file. Check Logs.", "Error",
				 * JOptionPane.ERROR_MESSAGE);
				 */
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean execScriptD2D(String scriptsDir, String command,
			String[] params) {
		ArrayList<String> commandlst = new ArrayList<>();
		commandlst.add(0, command);

		int i = 1;
		for (String param : params) {
			commandlst.add(i, param);
			i++;
		}

		ProcessBuilder pb = new ProcessBuilder(commandlst);
		pb.directory(new File(scriptsDir));
		pb.redirectErrorStream(true);
		
		pb.redirectError(Redirect.appendTo(logFile));
		pb.redirectOutput(Redirect.appendTo(logFile));
		
		try {
			Process p = pb.start();
			int exitCode = p.waitFor();
			if (exitCode == 0) {
				return true;
				/*
				 * JOptionPane.showMessageDialog(parent,
				 * "Instance file merged successfully", "Success",
				 * JOptionPane.INFORMATION_MESSAGE);
				 */

			} else {
				return false;
				/*
				 * JOptionPane.showMessageDialog(parent,
				 * "Error while merging Instance file. Check Logs.", "Error",
				 * JOptionPane.ERROR_MESSAGE);
				 */
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean execScriptC2MC(String scriptsDir, String command,
			String[] params) {
		ArrayList<String> commandlst = new ArrayList<>();
		commandlst.add(0, command);

		int i = 1;
		for (String param : params) {
			commandlst.add(i, param);
			i++;
		}

		ProcessBuilder pb = new ProcessBuilder(commandlst);
		pb.directory(new File(scriptsDir));
		pb.redirectErrorStream(true);
		
		pb.redirectError(Redirect.appendTo(logFile));
		pb.redirectOutput(Redirect.appendTo(logFile));
		
		try {
			Process p = pb.start();
			int exitCode = p.waitFor();
			if (exitCode == 0) {
				return true;
			} else {
				return false;
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean execScriptMC2MD(String scriptsDir, String command,
			String[] params) {
		ArrayList<String> commandlst = new ArrayList<>();
		commandlst.add(0, command);

		int i = 1;
		for (String param : params) {
			commandlst.add(i, param);
			i++;
		}

		ProcessBuilder pb = new ProcessBuilder(commandlst);
		pb.directory(new File(scriptsDir));
		pb.redirectErrorStream(true);
		
		pb.redirectError(Redirect.appendTo(logFile));
		pb.redirectOutput(Redirect.appendTo(logFile));
		
		try {
			Process p = pb.start();
			int exitCode = p.waitFor();
			if (exitCode == 0) {
				return true;
			} else {
				return false;
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void execScriptX2X(String scriptsDir, String command, String[] params,
			Boolean x2x) {

		ArrayList<String> commandlst = new ArrayList<>();
		commandlst.add(0, command);

		int i = 1;
		for (String param : params) {
			commandlst.add(i, param);
			i++;
		}

		ProcessBuilder pb = new ProcessBuilder(commandlst);
		pb.directory(new File(scriptsDir));
		pb.redirectErrorStream(true);
		
		pb.redirectError(Redirect.appendTo(logFile));
		pb.redirectOutput(Redirect.appendTo(logFile));
		
		try {
			Process p = pb.start();
			int exitCode = p.waitFor();
			if (exitCode == 0) {
				if (x2x != null) {
					if (x2x == false) {
						JOptionPane.showMessageDialog(parent,
								"Custom Command successful", "Success",
								JOptionPane.INFORMATION_MESSAGE);
					} else if (x2x == true) {
						JOptionPane.showMessageDialog(parent,
								"Conversion successful", "Success",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} else {
				if (x2x != null) {
					if (x2x == false) {
						JOptionPane
								.showMessageDialog(
										parent,
										"Error while executing Custom Command. Check Logs.",
										"Error", JOptionPane.ERROR_MESSAGE);
					} else if (x2x == true) {
						JOptionPane.showMessageDialog(parent,
								"Conversion Failed. Check Logs.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean execPythonScriptCheckWaterMark(File file, FileType type) {

		String scriptsDir = LantiqFrame.getProperties().get("scriptsdir")
				.toString();
		if (null == scriptsDir || scriptsDir.length() == 0
				|| scriptsDir.equals("")) {
			scriptsDir = System.getProperty("user.dir") + File.separator
					+ "scripts";
		}

		PythonInterpreter.initialize(System.getProperties(),
				System.getProperties(), new String[0]);

		PythonInterpreter interp = new PythonInterpreter();

		try {
			interp.setOut(new FileOutputStream(logFile, true));
			interp.setErr(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		PySystemState sys = Py.getSystemState();

		sys.path.append(new PyString(scriptsDir + "/action"));
		interp.exec("from xml.dom.minidom import parse,Document");

		sys.path.append(new PyString(scriptsDir + "/common"));
		interp.exec("from common import *");

		interp.exec("from addWaterMark import waterMark");
		PyObject jyX2XClass = interp.get("waterMark");
		PyObject jyX2XInstance = jyX2XClass.__call__();
		String result = jyX2XInstance.invoke("getWMType",
				new PyString(file.getAbsolutePath())).toString();

		if (type == FileType.ALLBUTUNKNOWN) {
			List<FileType> lst = Arrays.asList(FileType.values());
			return lst.toString().indexOf(result) > -1;
		} else {
			return result.equalsIgnoreCase(type.toString());
		}
	}

	public FileType execPythonScriptGetWaterMark(File file) {

		String scriptsDir = LantiqFrame.getProperties().get("scriptsdir")
				.toString();
		if (null == scriptsDir || scriptsDir.length() == 0
				|| scriptsDir.equals("")) {
			scriptsDir = System.getProperty("user.dir") + File.separator
					+ "scripts";
		}

		PythonInterpreter.initialize(System.getProperties(),
				System.getProperties(), new String[0]);

		PythonInterpreter interp = new PythonInterpreter();

		try {
			interp.setOut(new FileOutputStream(logFile, true));
			interp.setErr(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		PySystemState sys = Py.getSystemState();

		sys.path.append(new PyString(scriptsDir + "/action"));
		interp.exec("from xml.dom.minidom import parse,Document");

		sys.path.append(new PyString(scriptsDir + "/common"));
		interp.exec("from common import *");

		interp.exec("from addWaterMark import waterMark");
		PyObject jyX2XClass = interp.get("waterMark");
		PyObject jyX2XInstance = jyX2XClass.__call__();
		String result = jyX2XInstance.invoke("getWMType",
				new PyString(file.getAbsolutePath())).toString();

		FileType ftype = FileType.valueOf(result.toUpperCase());

		return ftype;
	}

	public FileType execPythonScriptAddWaterMark(File file, FileType ftype,
			AccessType atype) {

		String scriptsDir = LantiqFrame.getProperties().get("scriptsdir")
				.toString();
		if (null == scriptsDir || scriptsDir.length() == 0
				|| scriptsDir.equals("")) {
			scriptsDir = System.getProperty("user.dir") + File.separator
					+ "scripts";
		}

		PythonInterpreter.initialize(System.getProperties(),
				System.getProperties(), new String[0]);

		PythonInterpreter interp = new PythonInterpreter();

		try {
			interp.setOut(new FileOutputStream(logFile, true));
			interp.setErr(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		PySystemState sys = Py.getSystemState();

		sys.path.append(new PyString(scriptsDir + "/action"));
		interp.exec("from xml.dom.minidom import parse,Document");

		sys.path.append(new PyString(scriptsDir + "/common"));
		interp.exec("from common import *");

		interp.exec("from addWaterMark import waterMark");
		PyObject jyX2XClass = interp.get("waterMark");
		PyObject jyX2XInstance = jyX2XClass.__call__();
		int result = jyX2XInstance.invoke("addWaterMark", 
				new PyObject[] {new PyString(file.getAbsolutePath()),
				new PyString(ftype.toString()),
				new PyString(atype.toString())}).asInt();
		
		return ftype;
	}

	public AccessType execPythonScriptGetAccessType(File file) {

		String scriptsDir = LantiqFrame.getProperties().get("scriptsdir")
				.toString();
		if (null == scriptsDir || scriptsDir.length() == 0
				|| scriptsDir.equals("")) {
			scriptsDir = System.getProperty("user.dir") + File.separator
					+ "scripts";
		}

		PythonInterpreter.initialize(System.getProperties(),
				System.getProperties(), new String[0]);

		PythonInterpreter interp = new PythonInterpreter();

		try {
			interp.setOut(new FileOutputStream(logFile, true));
			interp.setErr(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		PySystemState sys = Py.getSystemState();

		sys.path.append(new PyString(scriptsDir + "/action"));
		interp.exec("from xml.dom.minidom import parse,Document");

		sys.path.append(new PyString(scriptsDir + "/common"));
		interp.exec("from common import *");

		interp.exec("from addWaterMark import waterMark");
		PyObject jyX2XClass = interp.get("waterMark");
		PyObject jyX2XInstance = jyX2XClass.__call__();

		String result = jyX2XInstance.invoke("getAccessLevel",
				new PyString(file.getAbsolutePath())).toString();

		AccessType atype = AccessType.valueOf(result.toUpperCase());

		return atype;
	}
}
