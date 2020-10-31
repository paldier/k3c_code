/********************************************************************************
 
  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany
  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.
 
********************************************************************************/

package com.lantiq.dbtool.ui;
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.util.Random;
 
public class LantiqProgressDialog extends JDialog {
 
    private JProgressBar progressBar;
        
    public LantiqProgressDialog(boolean modal) {
    	super();
    	
    	setLayout(new BorderLayout());
    	
    	progressBar = new JProgressBar();
    	progressBar.setSize(new Dimension(300,20));
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Import in Progress. Please Wait.");
 
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(305,25));
        panel.add(progressBar);
 
        add(panel, BorderLayout.PAGE_START);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        setModal(modal);
    }   
    
}