/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
 ********************************************************************************/

package com.lantiq.dbtool.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.base.ModelType;
import com.lantiq.dbtool.utils.FileUtils;
import com.lantiq.dbtool.utils.LantiqFileFilter;
import com.lantiq.dbtool.utils.PythonExecutor;

/**
 * 
 * @author mithil
 */
public class LantiqFrame extends JFrame implements ActionListener,
		ChangeListener {

	private static final Logger logger = LogManager
			.getLogger(LantiqFrame.class);
	// Variables declaration - do not modify
	private LantiqBackgroundPanel filePanel;
	private JTabbedPane tabPane;
	private JPopupMenu popMenu;
	private JMenuBar menuBar;
	private JToolBar toolbar;

	private JMenu filemenu;
	private JMenuItem openXMLTreeMenuItem, saveMenuItem, saveAsMenuItem, openXMLNewItem;

	private JMenu recentFileMenu;

	private JMenu schemaMenu;
	private JMenuItem schemaImportMenuItem, schemaMergeMenuItem;

	private JMenu instanceMenu;
	private JMenuItem instanceCreateMenuItem, instanceMergeMenuItem;

	private JMenu modelMenu;
	private JMenuItem modelCreateSchemaMenuItem, modelCreateInstanceMenuItem;

	private JMenuItem excelImportMenuItem, excelExportMenuItem;

	private JMenu customMenu;
	private JMenuItem command1MenuItem, command2MenuItem, command3MenuItem,
			command4MenuItem;

	private JMenu helpMenu;
	private JMenuItem helpMenuItem, aboutMenuItem;

	private JMenuItem exitMenuItem;

	private JMenuItem popCloseMenuItem, popCloseAllMenuItem;


	private JButton btnRefresh;
	private JButton btnGenId;
	private JToggleButton btnLinkUnlink;

	private JSlider slider;

	// End of variables declaration
	private Dimension defscreenSize, actscreensize;

	private ImageIcon refreshIcon,genIdIcon;
	private ImageIcon linkIcon, unlinkIcon;

	private URL linkImg, unlinkImg;

	private ArrayList<String> recentFileList;

	private HashMap<String, Object> preferences;

	private String prefsFileName = "config.prefs";

	LantiqFileFilter fnf1 = new LantiqFileFilter(FileType.ALLBUTUNKNOWN,
			"All Watermarked Files");
	LantiqFileFilter schemaType = new LantiqFileFilter(FileType.SCHEMA,
			"All Schema Files");
	LantiqFileFilter instanceType = new LantiqFileFilter(FileType.INSTANCE,
			"All Instance Files");
	LantiqFileFilter schemaId = new LantiqFileFilter(FileType.SCHEMA_ID,
			"All Schema ID Files");
	LantiqFileFilter instanceId = new LantiqFileFilter(FileType.INSTANCE_ID,
			"All Instance ID Files");
	FileNameExtensionFilter fnf2 = new FileNameExtensionFilter(
			"excel files (*.xls,*.xlsx)", "xls", "xlsx");

	static String configpath = System.getProperty("user.dir") + "/../"
			+ "configs/";

	static {
		loadProperties();
	}
	private static Properties properties;

	/**
	 * Creates new form ChartDemo
	 */
	public LantiqFrame() {
		setTitle("DBTool");

		readPreferences();
		initComponents();
		addMenuComponents();

		fitToScreen();
	}
	
	public void setClosingOperation(boolean save){
		if(save)
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void fitToScreen() {
		int width = Integer.parseInt(properties.getProperty("screenWidth"));
		int height = Integer.parseInt(properties.getProperty("screenHeight"));

		actscreensize = Toolkit.getDefaultToolkit().getScreenSize();

		if (width <= 0 || height <= 0)
			defscreenSize = Toolkit.getDefaultToolkit().getScreenSize();
		else {
			defscreenSize = new Dimension(width, height);
			setSize(defscreenSize);
		}
	}

	public static void loadProperties() {
		logger.log(Level.INFO, null, "Loading Properties");
		properties = new Properties();
		InputStream in;
		try {
			in = new FileInputStream(new File(configpath + "config.properties"));
			properties.load(in);
			in.close();
		} catch (IOException ex) {
			logger.log(Level.DEBUG, "Error Occured : Load Properties", ex);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {
		logger.log(Level.INFO, null, "Init Components");
		if (recentFileList == null)
			recentFileList = new ArrayList<String>();
		if (preferences == null)
			preferences = new HashMap<>();

		try {
			filePanel = new LantiqBackgroundPanel("/LQIMG1.jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}

		tabPane = new JTabbedPane();
		tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		menuBar = new JMenuBar();

		filemenu = new JMenu();
		schemaMenu = new JMenu();
		instanceMenu = new JMenu();
		helpMenu = new JMenu();
		toolbar = new JToolBar();
		
		openXMLNewItem = new JMenuItem();
		openXMLTreeMenuItem = new JMenuItem();
		saveMenuItem = new JMenuItem();
		saveAsMenuItem = new JMenuItem();
		recentFileMenu = new JMenu();
		customMenu = new JMenu();
		modelMenu = new JMenu();

		schemaImportMenuItem = new JMenuItem();
		schemaMergeMenuItem = new JMenuItem();
		

		instanceCreateMenuItem = new JMenuItem();
		instanceMergeMenuItem = new JMenuItem();

		modelCreateSchemaMenuItem = new JMenuItem();
		modelCreateInstanceMenuItem = new JMenuItem();

		excelImportMenuItem = new JMenuItem();
		excelExportMenuItem = new JMenuItem();

		command1MenuItem = new JMenuItem();
		command2MenuItem = new JMenuItem();
		command3MenuItem = new JMenuItem();
		command4MenuItem = new JMenuItem();

		helpMenuItem = new JMenuItem();
		aboutMenuItem = new JMenuItem();

		exitMenuItem = new JMenuItem();

		popMenu = new JPopupMenu();
		popCloseMenuItem = new JMenuItem();
		popCloseAllMenuItem = new JMenuItem();

		URL refreshImg = this.getClass().getResource("/RefreshIcon16.png");
		refreshIcon = new ImageIcon(refreshImg);
		btnRefresh = new JButton(refreshIcon);
	
		//Generate id icon	
		URL genIdImg = this.getClass().getResource("/GenerateId.png");
		genIdIcon = new ImageIcon(genIdImg);
		btnGenId = new JButton(genIdIcon);
		

		linkImg = this.getClass().getResource("/LinkIcon16.png");
		unlinkImg = this.getClass().getResource("/BrokenLinkIcon16.png");
		linkIcon = new ImageIcon(linkImg);
		unlinkIcon = new ImageIcon(unlinkImg);

		btnLinkUnlink = new JToggleButton(linkIcon, true);
		btnLinkUnlink.setSelectedIcon(linkIcon);
		btnLinkUnlink.setIcon(unlinkIcon);

		slider = new JSlider(0, 10);
		slider.setValue(0);

		filePanel.setLayout(new BorderLayout());
		filePanel.add(tabPane, BorderLayout.CENTER);

		tabPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				checkForPopup(e);
			}

			private void checkForPopup(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					Component c = e.getComponent();
					popMenu.show(c, e.getX(), e.getY());
				}
			}
		});

		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int tabindex = tabPane.getSelectedIndex();
				if (tabindex >= 0) {
					String tabName = tabPane.getTitleAt(tabindex);
					if (tabName.startsWith("File")) {
						// setEnabledOnTabSelect(false);
					}
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (checkAllDirty()) {
					Object[] options = { "Save", "Cancel" };
					int choice = JOptionPane.showOptionDialog(null, "Unsaved Files!", "Error",
					    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, 
					    options, options[0]);
					if(choice == JOptionPane.YES_OPTION){
						saveAllDirty();
					}
				} 
				
				writePreferencesToFile();
				dispose();
			}
		});

		getContentPane().add(toolbar, BorderLayout.PAGE_START);
		getContentPane().add(filePanel, BorderLayout.CENTER);

		tabPane.addChangeListener(this);

		filemenu.setText("File");

		openXMLNewItem.setText("New");
		openXMLNewItem.setActionCommand("NewTree");
		openXMLNewItem.addActionListener(this);
		
		openXMLTreeMenuItem.setText("Open");
		openXMLTreeMenuItem.setActionCommand("OpenTree");
		openXMLTreeMenuItem.addActionListener(this);

		saveMenuItem.setText("Save");
		saveMenuItem.setActionCommand("SaveXML");
		saveMenuItem.addActionListener(this);

		saveAsMenuItem.setText("Save As");
		saveAsMenuItem.setActionCommand("SaveAsXML");
		saveAsMenuItem.addActionListener(this);

		recentFileMenu = new JMenu();
		recentFileMenu.setText("Recent Files");

		if (preferences != null) {
			refreshRecents(true);
		}

		schemaMenu.setText("Schema");

		schemaImportMenuItem.setText("Import");
		schemaImportMenuItem.setActionCommand("SchemaImport");
		schemaImportMenuItem.addActionListener(this);

		schemaMergeMenuItem.setText("Merge");
		schemaMergeMenuItem.setActionCommand("SchemaMerge");
		schemaMergeMenuItem.addActionListener(this);

		instanceMenu.setText("Instance");

		instanceCreateMenuItem.setText("Create");
		instanceCreateMenuItem.setActionCommand("InstanceCreate");
		instanceCreateMenuItem.addActionListener(this);

		instanceMergeMenuItem.setText("Merge");
		instanceMergeMenuItem.setActionCommand("InstanceMerge");
		instanceMergeMenuItem.addActionListener(this);

		modelMenu.setText("Model");

		modelCreateSchemaMenuItem.setText("Schema");
		modelCreateSchemaMenuItem.setActionCommand("ModelCreateSchema");
		modelCreateSchemaMenuItem.addActionListener(this);

		modelCreateInstanceMenuItem.setText("Instance");
		modelCreateInstanceMenuItem.setActionCommand("ModelCreateInstance");
		modelCreateInstanceMenuItem.addActionListener(this);

		excelExportMenuItem.setText("Export to Excel");
		excelExportMenuItem.setActionCommand("ExcelExport");
		excelExportMenuItem.addActionListener(this);

		excelImportMenuItem.setText("Import from Excel");
		excelImportMenuItem.setActionCommand("ExcelImport");
		excelImportMenuItem.addActionListener(this);

		customMenu.setText("Custom");

		String menuName1 = properties.getProperty("MenuName1");
		if (!(menuName1 == null || menuName1.equals("") || menuName1.length() == 0)) {
			command1MenuItem.setText(menuName1);
			command1MenuItem.setActionCommand("Command1");
			command1MenuItem.addActionListener(this);
			customMenu.add(command1MenuItem);
		}

		String menuName2 = properties.getProperty("MenuName2");
		if (!(menuName2 == null || menuName2.equals("") || menuName2.length() == 0)) {
			command2MenuItem.setText(menuName2);
			command2MenuItem.setActionCommand("Command2");
			command2MenuItem.addActionListener(this);
			customMenu.add(command2MenuItem);
		}

		String menuName3 = properties.getProperty("MenuName3");
		if (!(menuName3 == null || menuName3.equals("") || menuName3.length() == 0)) {
			command3MenuItem.setText(menuName3);
			command3MenuItem.setActionCommand("Command3");
			command3MenuItem.addActionListener(this);
			customMenu.add(command3MenuItem);
		}

		String menuName4 = properties.getProperty("MenuName4");
		if (!(menuName4 == null || menuName4.equals("") || menuName4.length() == 0)) {
			command4MenuItem.setText(menuName4);
			command4MenuItem.setActionCommand("Command4");
			command4MenuItem.addActionListener(this);
			customMenu.add(command4MenuItem);
		}

		exitMenuItem.setText("Exit");
		exitMenuItem.setActionCommand("Exit");
		exitMenuItem.addActionListener(this);

		helpMenu.setText("Help");

		helpMenuItem.setText("Help");
		helpMenuItem.setActionCommand("Help");
		helpMenuItem.addActionListener(this);

		aboutMenuItem.setText("About");
		aboutMenuItem.setActionCommand("About");
		aboutMenuItem.addActionListener(this);

		popCloseMenuItem.setText("Close");
		popCloseMenuItem.setActionCommand("PopClose");
		popCloseMenuItem.addActionListener(this);

		popCloseAllMenuItem.setText("Close All");
		popCloseAllMenuItem.setActionCommand("PopCloseAll");
		popCloseAllMenuItem.addActionListener(this);

		pack();
	}// </editor-fold>

	public void addMenuComponents() {
		popMenu.add(popCloseMenuItem);
		popMenu.add(popCloseAllMenuItem);
		
		filemenu.add(openXMLNewItem);
		filemenu.add(openXMLTreeMenuItem);
		filemenu.addSeparator();
		filemenu.add(excelImportMenuItem);
		filemenu.add(excelExportMenuItem);
		filemenu.addSeparator();
		filemenu.add(saveMenuItem);
		filemenu.add(saveAsMenuItem);
		filemenu.add(recentFileMenu);
		filemenu.addSeparator();
		filemenu.add(exitMenuItem);

		schemaMenu.add(schemaImportMenuItem);
		schemaMenu.add(schemaMergeMenuItem);
		
		instanceMenu.add(instanceCreateMenuItem);
		instanceMenu.add(instanceMergeMenuItem);
		
		modelMenu.add(modelCreateSchemaMenuItem);
		modelMenu.add(modelCreateInstanceMenuItem);

		helpMenu.add(helpMenuItem);
		helpMenu.add(aboutMenuItem);

		menuBar.add(filemenu);
		menuBar.add(schemaMenu);
		menuBar.add(instanceMenu);
		menuBar.add(modelMenu);
		// menuBar.add(customMenu);
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);

		btnRefresh.setPreferredSize(new Dimension(25, 25));
		btnRefresh.setActionCommand("Refresh");
		btnRefresh.addActionListener(this);

		btnLinkUnlink.setPreferredSize(new Dimension(25, 25));
		btnLinkUnlink.setActionCommand("LinkUnlink");
		btnLinkUnlink.addActionListener(this);

		/* JPanel sliderpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sliderpanel.setPreferredSize(new Dimension(100, 25));
		sliderpanel.add(slider);
		slider.addChangeListener(this);*/
		btnGenId.setPreferredSize(new Dimension(25, 25));
		btnGenId.setActionCommand("SchemaIdGenerate");
		btnGenId.setToolTipText("Generate Id");
		btnGenId.addActionListener(this);
		
	
		toolbar.add(btnRefresh);
		toolbar.add(btnLinkUnlink);
		toolbar.add(btnGenId);
		//toolbar.add(sliderpanel);
		
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		performAction(evt, evt.getActionCommand());
	}

	private void performAction(ActionEvent evt, String action) {
		logger.log(Level.INFO, null, "Perform Action " + action);

		PythonExecutor pexec = new PythonExecutor(this);

		String scriptsDir = properties.get("scriptsdir").toString();
		if (null == scriptsDir || scriptsDir.length() == 0
				|| scriptsDir.equals("")) {
			scriptsDir = System.getProperty("user.dir") + File.separator
					+ "scripts";
		}

		FileUtils fu = new FileUtils();

		switch (action) {
		case "NewTree":
			openXMLTrees(fu);
			break;
			
		case "OpenTree":
			openXMLTrees(fu);
			break;
		case "SaveXML":
			saveXML(fu);
			break;
		case "SaveAsXML":
			saveAsXML(fu);
			break;
		case "SchemaImport":
			File schImportselected = fu.openFile(this, fnf1, false);

			if (schImportselected != null) {
				if (!validateXMLFile(schImportselected, false)) {
					return;
				}

				pexec.execPythonScript(scriptsDir, "genCtrlXML.py",
						new String[] { schImportselected.getAbsolutePath() });
			}
			break;
		case "SchemaMerge":
			displaySchema2WayCompare();
			break;
			
		case "SchemaIdGenerate":
		{
			File schIdgenerateselected = fu.openFile(this, schemaType, true);

			if (schIdgenerateselected != null) {
				if (!validateXMLFile(schIdgenerateselected, false)) {
					return;
				}
				
				
				StringBuffer dataFile=new StringBuffer(schIdgenerateselected.getAbsolutePath());
				String fileName=new String(schIdgenerateselected.getName());
				dataFile.replace(dataFile.indexOf("_control.xml"), dataFile.indexOf("_control.xml")+dataFile.length(), "_data.xml");
				File file = new File(dataFile.toString());
				
				
				 if( file.exists() ) {
				 
				   int option=JOptionPane.showConfirmDialog(this, 
						   	"Would you like to Continue with Existing Data XML File of "+ fileName.toString() +" ?", 
						   	"Warning", 
						   	JOptionPane.YES_NO_OPTION);
				   				
				   if(option == JOptionPane.YES_OPTION) {
				   	boolean result = pexec.execScriptC2D(scriptsDir + "/utils/genHdrnMergeXML/",
							"python", new String[] { "genHdrnMergeXML.py", "-a", "default", schIdgenerateselected.getAbsolutePath()});
					
					if(result){
						JOptionPane.showMessageDialog(
							this,
							"Schema ID XML File generated Successfully..!",
							"Success",
							JOptionPane.INFORMATION_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(
								this,
								"Schema ID XML File generation Failed..!. Check logs",
								"Failure",
								JOptionPane.ERROR_MESSAGE);
					}
				   }
			   } else {
				   JOptionPane.showMessageDialog(
							this,
							"Corresponding Data XML file doesn't exist, Please generate Data File..!",
							"Failure",
							JOptionPane.ERROR_MESSAGE); 	  
			   }
								
			}
		}
			break;
		case "InstanceCreate":
			createInstanceFile(pexec, scriptsDir);
			break;
		case "InstanceMerge":
			displayInstance2WayCompare();
			break;
			
		case "ModelCreateSchema":
			displayModel2WayCompare(pexec, scriptsDir, true);
			break;
		case "ModelCreateInstance":
			displayModel2WayCompare(pexec, scriptsDir, false);
			break;
		case "ExcelImport":
			File exlImportselected = fu.openFile(this, fnf2, true);

			if (exlImportselected != null)
				if (!validateXLSFile(exlImportselected)) {
					return;
				}
			// execPythonScript(scriptsDir, "x2x.py", new String[] {
			// "-toXML",
			// exlImportselected.getAbsolutePath() });
			pexec.execScriptX2X(scriptsDir + "/utils", "python",
					new String[] { "x2x.py", "-toXMLFullPath",
							exlImportselected.getAbsolutePath() }, true);
			break;
		case "ExcelExport":
			File exlExportselected = fu.openFile(this, fnf1, true);

			if (exlExportselected != null)
				if (!validateXMLFile(exlExportselected, false)) {
					return;
				}
			// execPythonScript(scriptsDir, "x2x.py", new String[] {
			// "-toEXL",
			// exlExportselected.getAbsolutePath() });
			pexec.execScriptX2X(scriptsDir + "/utils", "python",
					new String[] { "x2x.py", "-toEXLFullPath",
							exlExportselected.getAbsolutePath() }, true);
			break;
		case "Command1":
			String arrCmd1[] = properties.getProperty("MenuCmd1").split(" ");
			String argsCmd1[] = new String[arrCmd1.length - 1];
			for (int i = 0; i < argsCmd1.length; i++) {
				argsCmd1[i] = arrCmd1[i + 1];
			}
			pexec.execScriptX2X(scriptsDir + "/utils", arrCmd1[0], argsCmd1,
					false);
			break;
		case "Command2":
			String arrCmd2[] = properties.getProperty("MenuCmd2").split(" ");
			String argsCmd2[] = new String[arrCmd2.length - 1];
			for (int i = 0; i < argsCmd2.length; i++) {
				argsCmd2[i] = arrCmd2[i + 1];
			}
			pexec.execScriptX2X(scriptsDir + "/utils", arrCmd2[0], argsCmd2,
					false);
			break;
		case "Command3":
			String arrCmd3[] = properties.getProperty("MenuCmd3").split(" ");
			String argsCmd3[] = new String[arrCmd3.length - 1];
			for (int i = 0; i < argsCmd3.length; i++) {
				argsCmd3[i] = arrCmd3[i + 1];
			}
			pexec.execScriptX2X(scriptsDir + "/utils", arrCmd3[0], argsCmd3,
					false);
			break;
		case "Command4":
			String arrCmd4[] = properties.getProperty("MenuCmd4").split(" ");
			String argsCmd4[] = new String[arrCmd4.length - 1];
			for (int i = 0; i < argsCmd4.length; i++) {
				argsCmd4[i] = arrCmd4[i + 1];
			}
			pexec.execScriptX2X(scriptsDir + "/utils", arrCmd4[0], argsCmd4,
					false);
			break;
		case "Help":
			break;
		case "About":
			String version = pexec.execPythonScriptVersion("getVer.py",
					scriptsDir);
			LantiqAboutDialog dialog = new LantiqAboutDialog(version);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setLocation(defscreenSize.width / 2 - 100,
					defscreenSize.height / 2 - 200);
			dialog.pack();
			dialog.setVisible(true);
			break;
		case "PopClose":
			if (checkDirty()) {
				Object[] options = { "Save", "Cancel" };
				int choice = JOptionPane.showOptionDialog(this, "Unsaved File!", "Error",
				    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, 
				    options, options[0]);
				if(choice == JOptionPane.YES_OPTION){
					saveDirty();
				}
			}
			tabPane.remove(tabPane.getSelectedIndex());
			break;
		case "PopCloseAll":
			if (checkAllDirty()) {
				Object[] options = { "Save", "Cancel" };
				int choice = JOptionPane.showOptionDialog(this, "Unsaved Files!", "Error",
				    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, 
				    options, options[0]);
				if(choice == JOptionPane.YES_OPTION){
					saveAllDirty();
				}
			}

			int cnt = tabPane.getTabCount() - 1;

			while (cnt >= 0) {
				tabPane.remove(cnt);
				cnt--;
			}
			break;
		case "Refresh":
			refresh();
			break;
		case "LinkUnlink":
			linkUnlinkClicked();
			break;
		case "Recent":
			openRecentFile(evt);
			break;
		case "Exit":
			if (checkAllDirty()) {
				Object[] options = { "Save", "Cancel" };
				int choice = JOptionPane.showOptionDialog(this, "Unsaved Files!", "Error",
				    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, 
				    options, options[0]);
				if(choice == JOptionPane.YES_OPTION){
					saveAllDirty();
				}
			} 
			
			writePreferencesToFile();
			dispose();
			
			break;
		}
	}

	private void createInstanceFile(PythonExecutor pexec, String scriptsDir) {
		LantiqCreateInstanceDialog dialog = new LantiqCreateInstanceDialog(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setLocation(defscreenSize.width / 2 - 200,
				defscreenSize.height / 2 - 200);
		dialog.pack();
		dialog.setVisible(true);

		if (!dialog.getCancelled()) {
			String controlfile = (String) dialog.getInputs().get(0);
			String datafile = (String) dialog.getInputs().get(1);
			boolean merge = (boolean) dialog.getInputs().get(2);
			boolean dfile = (boolean) dialog.getInputs().get(3);

			boolean c2dsuccess = pexec.execScriptC2D(scriptsDir + "/utils",
					"python", new String[] { "c2d.py", controlfile, datafile });

			Boolean d2dsuccess = false;
			if (merge) {
				String datafilewonew = datafile.replace("new_", "");
				d2dsuccess = pexec.execScriptD2D(scriptsDir + "/utils",
						"python", new String[] { "d2d.py", datafilewonew,
								datafile });
			}

			if (c2dsuccess) {
				if (!dfile) {
					if (merge) {
						if (d2dsuccess) {
							JOptionPane
									.showMessageDialog(
											this,
											"All relevant data instances have been merged from the previous instance.",
											"Success",
											JOptionPane.INFORMATION_MESSAGE);
						}
					}
				} else {
					if (!merge) {
						JOptionPane.showMessageDialog(this,
								"A previous version of the Instance file exists. Use the Instance -"
										+ "> Merge menu option to merge.",
								"Merge Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
			} else {
				if (dfile && merge && !d2dsuccess) {
					JOptionPane.showMessageDialog(this,
							"Error while merging Instance file. Check Logs.",
							"Error", JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this,
							"Error while creating Instance file. Check Logs.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			try {
				displayXMLTree(new File(datafile));
			} catch (XMLStreamException | IOException e) {
				e.printStackTrace();
			}
		}
		dialog = null;

		/*
		 * File instCreateselected = fu.openFile(this, fnf1); if
		 * (instCreateselected != null) execPythonScript(scriptsDir, "c2d.py",
		 * new String[] { instCreateselected.getAbsolutePath() });
		 */
	}

	private void refresh() {
		if (tabPane.getSelectedComponent() == null)
			return;
		else {
			int index = tabPane.getSelectedIndex();
			if (index >= 0) {
				if (tabPane.getTitleAt(index).indexOf("Compare") >= 0) {
					Component comp = tabPane.getSelectedComponent();
					if (comp instanceof LantiqSchemaComparePanel) {
						LantiqSchemaComparePanel x2 = (LantiqSchemaComparePanel) comp;
						try {
							x2.runCompare(false);
						} catch (SAXException | IOException e) {
							e.printStackTrace();
						}
					} else if (comp instanceof LantiqInstanceComparePanel) {
						LantiqInstanceComparePanel x2 = (LantiqInstanceComparePanel) comp;
						try {
							x2.runCompare(false);
						} catch (SAXException | IOException e) {
							e.printStackTrace();
						}
					} else if (comp instanceof LantiqModelComparePanel) {
						LantiqModelComparePanel x2 = (LantiqModelComparePanel) comp;
						try {
							x2.runCompare(false);
						} catch (SAXException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void linkUnlinkClicked() {
		if (tabPane.getSelectedComponent() == null)
			return;
		else {
			int index = tabPane.getSelectedIndex();
			if (index >= 0) {
				if (tabPane.getTitleAt(index).indexOf("Compare") >= 0) {
					Component comp = tabPane.getSelectedComponent();

					if (comp instanceof LantiqSchemaComparePanel) {
						LantiqSchemaComparePanel x2 = (LantiqSchemaComparePanel) comp;
						if (x2.isLinked()) {
							btnLinkUnlink.setSelected(false);
							x2.linkScrollBars(false);
						} else {
							btnLinkUnlink.setSelected(true);
							x2.linkScrollBars(true);
						}
					} else if (comp instanceof LantiqInstanceComparePanel) {
						LantiqInstanceComparePanel x2 = (LantiqInstanceComparePanel) comp;
						if (x2.isLinked()) {
							btnLinkUnlink.setSelected(false);
							x2.linkScrollBars(false);
						} else {
							btnLinkUnlink.setSelected(true);
							x2.linkScrollBars(true);
						}
					}
				}

				if (tabPane.getTitleAt(index).indexOf("Model") >= 0) {
					Component comp = tabPane.getSelectedComponent();
					LantiqModelComparePanel x2 = (LantiqModelComparePanel) comp;
					if (x2.isLinked()) {
						btnLinkUnlink.setSelected(false);
						x2.linkScrollBars(false);
					} else {
						btnLinkUnlink.setSelected(true);
						x2.linkScrollBars(true);
					}

				}
			}
		}
	}

	private void openRecentFile(ActionEvent evt) {
		JMenuItem item = (JMenuItem) evt.getSource();
		File file = new File(item.getText().trim());

		if (!validateXMLFile(file, true)) {
			return;
		}

		try {
			displayXMLTree(file);
		} catch (XMLStreamException | IOException e) {
			e.printStackTrace();
		}
	}

	private void saveXML(FileUtils fu) {
		if (tabPane.getSelectedComponent() == null)
			return;
		else {
			int index = tabPane.getSelectedIndex();
			if (index >= 0) {
				if (tabPane.getTitleAt(index).startsWith("XMLTree")) {
					Component comp = tabPane.getSelectedComponent();
					LantiqTreeViewerPanel panel = (LantiqTreeViewerPanel) comp;
					fu.writeTreeToFile(panel.getJTree(), panel
							.getSelectedFile().getAbsolutePath());
					PythonExecutor pe = new PythonExecutor(this);
					pe.execPythonScriptAddWaterMark(panel.getSelectedFile(),
							panel.getFileType(), panel.getAccessType());
					panel.setDirty(false);
				}
			}
		}
	}

	private void saveAsXML(FileUtils fu) {
		if (tabPane.getSelectedComponent() == null)
			return;
		else {
			int index = tabPane.getSelectedIndex();
			if (index >= 0) {
				if (tabPane.getTitleAt(index).startsWith("XMLTree")) {
					Component comp = tabPane.getSelectedComponent();
					LantiqTreeViewerPanel panel = (LantiqTreeViewerPanel) comp;
					File file = fu.saveFile(this, fnf1, true);
					if (file != null) {
						fu.writeTreeToFile(panel.getJTree(),
								file.getAbsolutePath());
						PythonExecutor pe = new PythonExecutor(this);
						pe.execPythonScriptAddWaterMark(file,
								panel.getFileType(), panel.getAccessType());
						panel.setDirty(false);
						tabPane.remove(tabPane.getSelectedIndex());
						addRecentFile(file.getAbsolutePath());
						try {
							displayXMLTree(file);
						} catch (XMLStreamException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public boolean validateXMLFile(File selectedfile, boolean display) {

		if (!checkValidFileExtn(selectedfile, "xml")) {
			JOptionPane.showMessageDialog(this,
					"Unknown File Type, File cannot be opened", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (!checkWatermark(selectedfile)) {
			if (display) {
				JOptionPane.showMessageDialog(this,
						"Unsupported XML file type, Edit will not be possible",
						"Warning", JOptionPane.WARNING_MESSAGE);
			}
		}

		return true;
	}

	public boolean validateXLSFile(File selectedfile) {

		if (!(checkValidFileExtn(selectedfile, "xls") || checkValidFileExtn(
				selectedfile, "xlsx"))) {
			JOptionPane.showMessageDialog(this,
					"Unknown File Type, Cannot load file", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	private void openXMLTrees(FileUtils fu) {
		boolean failedwatermark = false;
		boolean failedextn = false;
		logger.log(Level.INFO, null, "Open XML Tree");
		File selectedfile = fu.openFile(this, fnf1, true);
		if (null != selectedfile) {
			if (!validateXMLFile(selectedfile, true)) {
				return;
			}

			try {
				displayXMLTree(selectedfile);
			} catch (IOException | XMLStreamException ex) {
				logger.log(Level.DEBUG, "Error Occured : Open XML Tree", ex);
			}
		}
	}

	private void displayXMLTree(File file) throws XMLStreamException,
			FileNotFoundException, IOException {
		LantiqTreeViewerPanel xmlPanel = null;
		addRecentFile(file.getAbsolutePath());
		xmlPanel = new LantiqTreeViewerPanel(this, file);
		tabPane.add(xmlPanel, "XMLTree - " + file.getName());

		if (xmlPanel != null)
			tabPane.setSelectedComponent(xmlPanel);
	}

	private void displaySchema2WayCompare() {
		logger.log(Level.INFO, null, "Display Schema XML Compare Panel");
		LantiqSchemaComparePanel xmlPanel = new LantiqSchemaComparePanel(this);
		tabPane.add(xmlPanel, "Schema Compare");
		tabPane.setSelectedComponent(xmlPanel);
	}

	private void displayInstance2WayCompare() {
		logger.log(Level.INFO, null, "Display Instance XML Compare Panel");
		LantiqInstanceComparePanel xmlPanel = new LantiqInstanceComparePanel(
				this);
		tabPane.add(xmlPanel, "Instance Compare");
		tabPane.setSelectedComponent(xmlPanel);
	}

	private void displayModel2WayCompare(PythonExecutor pexec,
			String scriptsDir, boolean model) {
		logger.log(Level.INFO, null, "Display Model XML Compare Panel");
		LantiqModelCreateDialog dialog = new LantiqModelCreateDialog(true,
				model);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setLocation(defscreenSize.width / 2 - 200,
				defscreenSize.height / 2 - 200);
		dialog.pack();
		dialog.setVisible(true);

		if (!dialog.getCancelled()) {
			String filename = (String) dialog.getInputs().get(0);
			File file = new File(filename);
			boolean autoDel;
			ModelType modelType;
			if (model) {
				modelType = (ModelType) dialog.getInputs().get(1);
				createModelSchemaFile(file, modelType, pexec, scriptsDir);
			} else {
				autoDel = (boolean) dialog.getInputs().get(1);
				createModelInstanceFile(file, autoDel, pexec, scriptsDir);
			}

		}
	}

	private void createModelInstanceFile(File file, boolean autoDel,
			PythonExecutor pexec, String scriptsDir) {
		String name = file.getName();
		String datafilename = name.substring(0, name.indexOf("_"));
		datafilename += "_data.xml";
		datafilename = file.getParentFile().getAbsolutePath() + File.separator
				+ datafilename;
		File datafile = new File(datafilename);
		if (datafile.exists()) {
			String datamodelfilename = name.substring(0, name.indexOf("_"));
			datamodelfilename += "_data_model_";
			datamodelfilename += name.substring(name.indexOf(".") - 2,
					name.indexOf("."))
					+ ".xml";
			String datamodelabs = file.getParentFile().getAbsolutePath()
					+ File.separator + datamodelfilename;
			logger.log(Level.INFO, "Data Model File Name " + datamodelfilename);
			boolean mc2mdsuccess = pexec.execScriptMC2MD(scriptsDir + "/utils",
					"python", new String[] { "mc2md.py",
							file.getAbsolutePath(), datamodelabs, datafilename,
							"" + autoDel });

			LantiqModelComparePanel xmlPanel = new LantiqModelComparePanel(this);
			tabPane.add(xmlPanel, "Model Instance Create");
			tabPane.setSelectedComponent(xmlPanel);

			xmlPanel.setModeltype("Instance");
			xmlPanel.setSelectedfile1(datafile);
			xmlPanel.setSelectedfile2(new File(datamodelabs));
			try {
				xmlPanel.runCompare(true);
				xmlPanel.refreshCompareChangeFile();
			} catch (XMLStreamException | SAXException | IOException e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane
					.showMessageDialog(
							this,
							"Unable to locate"
									+ datafile.getName()
									+ "XML File. \n"
									+ "Please ensure, it is present in the same directory as"
									+ file.getName() + "XML File", "Error",
							JOptionPane.ERROR_MESSAGE);
		}
	}

	public void createModelSchemaFile(File file, ModelType modelType,
			PythonExecutor pexec, String scriptsDir) {
		String name = file.getName();
		String mschemafilename = name.substring(0, name.indexOf("_"));
		mschemafilename += "_control_model_";
		mschemafilename += modelType.toString() + ".xml";
		logger.log(Level.INFO, "Model Schema File Name " + mschemafilename);
		logger.log(Level.INFO, "Model Schema Abs File Path "
				+ file.getParentFile().getAbsolutePath() + File.separator
				+ mschemafilename);

		File mschemafile = new File(file.getParentFile().getAbsolutePath()
				+ File.separator + mschemafilename);
		if (mschemafile.exists()) {
			logger.log(Level.INFO, null, "File Exists");
			boolean c2mcsuccess = pexec.execScriptC2MC(scriptsDir + "/utils",
					"python", new String[] { "c2mc.py", file.getAbsolutePath(),
							mschemafile.getAbsolutePath() });
		}

		LantiqModelComparePanel xmlPanel = new LantiqModelComparePanel(this);
		tabPane.add(xmlPanel, "Model Schema Create");
		tabPane.setSelectedComponent(xmlPanel);

		xmlPanel.setModeltype("Schema");
		xmlPanel.setSelectedfile1(file);
		xmlPanel.setSelectedfile2(mschemafile);
		try {
			if (!mschemafile.exists())
				xmlPanel.refreshRightAfterNew(mschemafile);
			xmlPanel.runCompare(true);
			xmlPanel.refreshCompareChangeFile();
		} catch (XMLStreamException | SAXException | IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabPane)) {
			int index = tabPane.getSelectedIndex();
			if (index >= 0) {
				String tabName = tabPane.getTitleAt(index);
				if (tabName.indexOf("Compare") >= 0) {
					Component comp = tabPane.getSelectedComponent();

					if (comp instanceof LantiqSchemaComparePanel) {
						LantiqSchemaComparePanel x2 = (LantiqSchemaComparePanel) comp;
						if (x2.isLinked()) {
							btnLinkUnlink.setSelected(true);
						} else {
							btnLinkUnlink.setSelected(false);
						}
					} else if (comp instanceof LantiqInstanceComparePanel) {
						LantiqInstanceComparePanel x2 = (LantiqInstanceComparePanel) comp;
						if (x2.isLinked()) {
							btnLinkUnlink.setSelected(true);
						} else {
							btnLinkUnlink.setSelected(false);
						}
					} else if (comp instanceof LantiqModelComparePanel) {
						LantiqModelComparePanel x2 = (LantiqModelComparePanel) comp;
						if (x2.isLinked()) {
							btnLinkUnlink.setSelected(true);
						} else {
							btnLinkUnlink.setSelected(false);
						}
					}
				}
			}
		} else if (e.getSource().equals(slider)) {
			int index = tabPane.getSelectedIndex();
			if (index >= 0) {
				String tabName = tabPane.getTitleAt(index);
				Component comp = tabPane.getSelectedComponent();

				if (comp instanceof LantiqTreeViewerPanel) {
					LantiqTreeViewerPanel x2 = (LantiqTreeViewerPanel) comp;
					x2.expandAllAtLevel(slider.getValue());
				}

			}
		}
	}

	public void writePreferencesToFile() {
		// Write preferences to hashmap
		preferences.put("recentFileList", recentFileList);

		// write preferences to file
		FileUtils fu = new FileUtils();
		fu.writeObjectToFile(preferences, prefsFileName);
	}

	public void readPreferences() {
		FileUtils fu = new FileUtils();
		preferences = fu.readObjectFromFile(prefsFileName);
	}

	public void addRecentFile(String path) {
		if (recentFileList == null)
			recentFileList = new ArrayList<String>();

		if (recentFileList.size() >= 10) {
			recentFileList.remove(recentFileList.size() - 1);
		}

		if (!recentFileList.contains(path))
			recentFileList.add(0, path);

		refreshRecents(false);
	}

	public void refreshRecents(boolean fromfile) {
		recentFileMenu.removeAll();
		if (fromfile)
			recentFileList = (ArrayList<String>) preferences
					.get("recentFileList");
		if (recentFileList != null && recentFileList.size() > 0) {
			for (String filepath : recentFileList) {
				JMenuItem recentMenuItem = new JMenuItem(filepath);
				recentMenuItem.setActionCommand("Recent");
				recentMenuItem.addActionListener(this);
				recentFileMenu.add(recentMenuItem);
			}
		}
	}

	public static Properties getProperties() {
		return properties;
	}

	public JTabbedPane getTabPane() {
		return tabPane;
	}

	public void setTabPane(JTabbedPane tabPane) {
		this.tabPane = tabPane;
	}

	public void updateParentTabComponent(String strfile1, String strfile2) {
		int index = tabPane.getSelectedIndex();
		if (index >= 0) {
			String title = tabPane.getTitleAt(index);
			if (title.indexOf("Schema Compare") >= 0) {
				tabPane.setTitleAt(index, "Schema Compare - " + strfile1
						+ " - " + strfile2);
			} else if (title.indexOf("Instance Compare") >= 0) {
				tabPane.setTitleAt(index, "Instance Compare - " + strfile1
						+ " - " + strfile2);
			} else if (title.indexOf("Model Schema Create") >= 0) {
				tabPane.setTitleAt(index, "Model Schema Create - " + strfile1
						+ " - " + strfile2);
			} else if (title.indexOf("Model Instance Create") >= 0) {
				tabPane.setTitleAt(index, "Model Instance Create - " + strfile1
						+ " - " + strfile2);
			}
		}
	}

	public JSlider getSlider() {
		return slider;
	}

	public void setSlider(JSlider slider) {
		this.slider = slider;
	}

	public boolean checkWatermark(File file) {
		PythonExecutor pe = new PythonExecutor(null);
		if (pe.execPythonScriptCheckWaterMark(file, FileType.UNKNOWN) == true)
			return false;
		else
			return true;
	}

	public boolean checkValidFileExtn(File file, String extn) {
		String filename = file.getName();
		String extension = filename.substring(filename.lastIndexOf(".") + 1,
				filename.length());

		if (extension.equalsIgnoreCase(extn)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean checkDirty() {
		boolean dirty = false;
		int index = tabPane.getSelectedIndex();
		if (index >= 0) {
			Component comp = tabPane.getSelectedComponent();
			if (comp instanceof LantiqTreeViewerPanel) {
				LantiqTreeViewerPanel x2 = (LantiqTreeViewerPanel) comp;
				dirty = x2.isDirty();
			} else if (comp instanceof LantiqSchemaComparePanel) {
				LantiqSchemaComparePanel x2 = (LantiqSchemaComparePanel) comp;
				dirty = x2.isDirty();
			} else if (comp instanceof LantiqInstanceComparePanel) {
				LantiqInstanceComparePanel x2 = (LantiqInstanceComparePanel) comp;
				dirty = x2.isDirty();
			} else if (comp instanceof LantiqModelComparePanel) {
				LantiqModelComparePanel x2 = (LantiqModelComparePanel) comp;
				dirty = x2.isDirty();
			}
		}

		return dirty;
	}
	
	public void saveDirty() {
		FileUtils fu = new FileUtils();
		int index = tabPane.getSelectedIndex();
		if (index >= 0) {
			Component comp = tabPane.getSelectedComponent();
			if (comp instanceof LantiqTreeViewerPanel) {
				LantiqTreeViewerPanel x2 = (LantiqTreeViewerPanel) comp;
				if(x2.isDirty()){
					saveXML(fu);
					x2.setDirty(false);
				}
			} else if (comp instanceof LantiqSchemaComparePanel) {
				LantiqSchemaComparePanel x2 = (LantiqSchemaComparePanel) comp;
				if(x2.isDirty()){
					x2.saveFileFromText(fu);
					x2.setDirty(false);
				}
			} else if (comp instanceof LantiqInstanceComparePanel) {
				LantiqInstanceComparePanel x2 = (LantiqInstanceComparePanel) comp;
				if(x2.isDirty()){
					x2.saveFileFromText(fu);
					x2.setDirty(false);
				}
			} else if (comp instanceof LantiqModelComparePanel) {
				LantiqModelComparePanel x2 = (LantiqModelComparePanel) comp;
				if(x2.isDirty()){
					x2.saveFileFromText(fu);
					x2.setDirty(false);
				}
			}
		}
	}

	public boolean checkAllDirty() {
		boolean dirty = false;
		int cnt = tabPane.getTabCount() - 1;
		for (int i = 0; i < tabPane.getTabCount(); i++) {

			Component comp = tabPane.getComponentAt(i);
			if (comp instanceof LantiqTreeViewerPanel) {
				LantiqTreeViewerPanel x2 = (LantiqTreeViewerPanel) comp;
				dirty = x2.isDirty();
				if (dirty)
					break;
			} else if (comp instanceof LantiqSchemaComparePanel) {
				LantiqSchemaComparePanel x2 = (LantiqSchemaComparePanel) comp;
				dirty = x2.isDirty();
				if (dirty)
					break;
			} else if (comp instanceof LantiqInstanceComparePanel) {
				LantiqInstanceComparePanel x2 = (LantiqInstanceComparePanel) comp;
				dirty = x2.isDirty();
				if (dirty)
					break;
			} else if (comp instanceof LantiqModelComparePanel) {
				LantiqModelComparePanel x2 = (LantiqModelComparePanel) comp;
				dirty = x2.isDirty();
				if (dirty)
					break;
			}

		}

		return dirty;
	}
	
	
	public void saveAllDirty() {
		FileUtils fu = new FileUtils();
		int cnt = tabPane.getTabCount() - 1;
		for (int i = 0; i < tabPane.getTabCount(); i++) {
			Component comp = tabPane.getComponentAt(i);
			if (comp instanceof LantiqTreeViewerPanel) {
				LantiqTreeViewerPanel x2 = (LantiqTreeViewerPanel) comp;
				if(x2.isDirty()){
					saveXML(fu);
					x2.setDirty(false);
				}
			} else if (comp instanceof LantiqSchemaComparePanel) {
				LantiqSchemaComparePanel x2 = (LantiqSchemaComparePanel) comp;
				if(x2.isDirty()){
					x2.saveFileFromText(fu);
					x2.setDirty(false);
				}
			} else if (comp instanceof LantiqInstanceComparePanel) {
				LantiqInstanceComparePanel x2 = (LantiqInstanceComparePanel) comp;
				if(x2.isDirty()){
					x2.saveFileFromText(fu);
					x2.setDirty(false);
				}
			} else if (comp instanceof LantiqModelComparePanel) {
				LantiqModelComparePanel x2 = (LantiqModelComparePanel) comp;
				if(x2.isDirty()){
					x2.saveFileFromText(fu);
					x2.setDirty(false);
				}
			}

		}		
	}
}
