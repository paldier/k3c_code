/******************************************************************************** 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 1, or (at your option)
  any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. 
********************************************************************************/

package com.lantiq.dbtool.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import com.lantiq.dbtool.base.FileType;
import com.lantiq.dbtool.ui.LantiqFrame;

/**
 * Inherited FileFilter class to facilitate reuse when
 * multiple file filter selections are required. For example
 * purposes, I used a static nested class, which is defined
 * as below as a member of our original FileChooserExample
 * class.
 */
public class LantiqFileFilter 
    extends javax.swing.filechooser.FileFilter {

    private FileType type;
    private String description;

    public LantiqFileFilter(FileType type, String desc) {
        this.type = type;
        // Using inline if syntax, use input from desc or use
        // a default value.
        // Wrap with an if statement to default as well as
        // avoid NullPointerException when using trim().
        description = (desc != null) ? desc.trim() : "Custom File List";
    }

    // Handles which files are allowed by filter.
    @Override
    public boolean accept(File f) {
    
        // Allow directories to be seen.
        if (f.isDirectory()) return true;
        
        // Allows files with extensions specified to be seen.
        PythonExecutor pe = new PythonExecutor(null);
        
        if (f.getName().toLowerCase().endsWith(".xml")){
        	if(pe.execPythonScriptCheckWaterMark(f,type))
                return true;
        }
        
        // Otherwise file is not shown.
        return false;
    }

    // 'Files of Type' description
    @Override
    public String getDescription() {
        return description;
    }  
    
}
